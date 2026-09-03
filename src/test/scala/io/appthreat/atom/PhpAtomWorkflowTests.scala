package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.php2atom.parser.PhpParser
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Paths
import scala.util.Try

/** End-to-end PHP workflow through atom's own CLI entry point (`Atom.run`): atom generation and
  * usage slicing over a small PHP project.
  *
  * These are the only PHP tests that exercise the whole chain - `phpastgen` writes AST JSON,
  * php2atom rebuilds it into a CPG, and atom's overlays and slicers run on top. That needs a
  * working generator, so the suite is **cancelled** (not failed) when none is reachable or when it
  * is too old to emit the 2.x JSON contract these assertions rely on.
  *
  * php2atom loads in-process inside atom, so the frontend inherits this JVM's environment. The
  * generator binary is resolved exactly as php2atom's [[PhpParser]] does: the `PHP_PARSER_BIN`
  * environment variable when set, otherwise `phpastgen` on `PATH`. To exercise a specific generator
  * build:
  *
  * {{{
  * PHP_PARSER_BIN=/path/to/phpastgen sbt test
  * }}}
  *
  * The forked test JVM only sees `PHP_PARSER_BIN` because build.sbt propagates it into `Test /
  * envVars`.
  */
class PhpAtomWorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val MinimumGeneratorMajor = 2

  /** A vendored `php-parse` shipped next to the php2atom jar, when one is present.
    *
    * php2atom's own `defaultPhpParserBin` prefers this over a `PATH` lookup, so the gate must too:
    * otherwise a packaged build could gate against a stale `PATH` phpastgen while the frontend
    * silently used a different vendored copy - reintroducing, one layer removed, the very "broken
    * generator exits 0 with an empty atom" risk this gate exists to prevent (finding D-M1).
    */
  private def vendoredPhpParse: Option[String] =
      Try {
          val codeSource =
              Paths.get(
                classOf[PhpParser].getProtectionDomain.getCodeSource.getLocation.toURI
              ).toAbsolutePath.toString
          val marker = "php2atom"
          val idx    = codeSource.indexOf(marker)
          if idx < 0 then None
          else
            val base   = new java.io.File(codeSource.substring(0, idx)).toString
            val binary = Paths.get(base, marker, "vendor", "bin", "php-parse").toAbsolutePath
            Option.when(binary.toFile.isFile)(binary.toString)
      }.toOption.flatten

  /** Generator binary, resolved in php2atom's real order (finding D-M1): the `PHP_PARSER_BIN`
    * environment variable, then a vendored `php-parse` next to the php2atom jar, then `phpastgen`
    * on `PATH`. (php2atom also honours a `--frontend-arg`-style config flag ahead of the env var;
    * that has no analogue here because these tests always drive `Atom.run` without one.)
    */
  private val phpastgen: String =
      sys.env
          .get(PhpParser.PhpParserBinEnvVar)
          .map(_.trim)
          .filter(_.nonEmpty)
          .orElse(vendoredPhpParse)
          .getOrElse("phpastgen")

  /** Raw `--parser-info` output lines, or `None` when the probe cannot even be launched. */
  private lazy val parserInfo: Option[Seq[String]] =
      Try(scala.sys.process.Process(Seq(phpastgen, "--parser-info")).lazyLines_!.toList).toOption

  /** The version reported on the generator's `Generator version:` line, when present. */
  private lazy val generatorVersion: Option[String] =
      parserInfo.flatMap { lines =>
          lines
              .find(_.contains("Generator version:"))
              .map(_.split("Generator version:", 2).last.trim)
              .filter(_.nonEmpty)
      }

  private var workspace: BFile  = scala.compiletime.uninitialized
  private var projectDir: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomPhpWorkflow")
    projectDir = (workspace / "project").createDirectories()
    (projectDir / "src").createDirectories()

    (projectDir / "src" / "UserStore.php").write(
      """<?php
        |
        |namespace App;
        |
        |class UserStore
        |{
        |    private array $rows = [];
        |
        |    public function add($row)
        |    {
        |        $this->rows[] = $row;
        |        return $row;
        |    }
        |}
        |""".stripMargin
    )

    // A source (`$_GET` / `$argv`) reaching a sink (`system`) through a method call and a local.
    (projectDir / "app.php").write(
      """<?php
        |
        |namespace App;
        |
        |require __DIR__ . '/src/UserStore.php';
        |
        |class UserService
        |{
        |    private $store;
        |
        |    public function __construct($store)
        |    {
        |        $this->store = $store;
        |    }
        |
        |    public function report($rawName)
        |    {
        |        $label = $this->normalize($rawName);
        |        $this->store->add($label);
        |        system("echo " . $label);
        |        return $label;
        |    }
        |
        |    public function normalize($value)
        |    {
        |        return trim($value);
        |    }
        |}
        |
        |$service = new UserService(new UserStore());
        |$name = $_GET['name'] ?? ($argv[1] ?? 'anon');
        |$service->report($name);
        |""".stripMargin
    )

    // Framework shapes the Milestone E tagging pass must mark (finding D-H2): a routed controller
    // method (`#[Route]` -> framework-route entrypoint), an unsanitized WordPress-style superglobal
    // source reaching an `echo` sink, the same source sanitized via `esc_html` (which must NOT
    // remain tainted), a Laravel-style request source reaching a raw `DB::raw` query sink, and a
    // WordPress hook registration.
    (projectDir / "src" / "Controller.php").write(
      """<?php
        |
        |namespace App;
        |
        |class UserController
        |{
        |    #[Route("/users")]
        |    public function index($id)
        |    {
        |        $q = $_GET['q'];
        |        echo $q;
        |        return $id;
        |    }
        |
        |    public function safeIndex()
        |    {
        |        $q = $_GET['q'];
        |        echo esc_html($q);
        |    }
        |
        |    public function search($request)
        |    {
        |        $name = $request->input('name');
        |        DB::raw($name);
        |    }
        |}
        |
        |function my_handler($data) { return $data; }
        |add_action('init', 'my_handler');
        |""".stripMargin
    )
  end beforeAll

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  /** major.minor as an `(Int, Int)`, tolerating a bare `2` and trailing pre-release/build metadata
    * (e.g. `2.0.0-rc1`).
    */
  private def majorVersion(version: String): Option[Int] =
      version.takeWhile(c => c.isDigit).toIntOption

  /** Cancels the enclosing test unless a generator implementing the 2.x JSON contract is reachable.
    * Mirrors [[RubyAtomWorkflowTests.requireGenerator]] and php2atom's own capability probe
    * (`--parser-info` + a `Generator version:` line): a missing PHP runtime, an unbuilt/ absent
    * generator, or a pre-2.x binary each silently produces an empty atom rather than a failure,
    * which would surface here as a puzzling assertion error - so we cancel instead.
    */
  /** The gate decision as a pure function of the generator's `--parser-info` output: `Some(reason)`
    * when the suite must be cancelled, `None` when the generator is usable.
    *
    * Split out from [[requireGenerator]] so all three cancel branches are exercised
    * deterministically in CI (finding D-M2). Previously they were correct only by inspection:
    * nothing proved a missing PHP runtime, an unbuilt generator or a pre-2.x binary actually
    * cancels, because doing so needs a stubbed generator. Feeding synthetic `--parser-info` text to
    * this function tests the decision without spawning anything.
    *
    * @param infoLines
    *   the generator's `--parser-info` lines, or `None` when the probe could not even be launched.
    */
  private[atom] def gateFailure(infoLines: Option[Seq[String]]): Option[String] =
    val infoText = infoLines.map(_.mkString("\n")).getOrElse("")
    val version =
        infoLines.flatMap(
          _.find(_.contains("Generator version:"))
              .map(_.split("Generator version:", 2).last.trim)
              .filter(_.nonEmpty)
        )
    if infoText.contains("PHP is not installed") then
      Some(s"phpastgen '$phpastgen' reports PHP is not installed; these tests need PHP on PATH")
    else if version.isEmpty then
      Some(
        s"phpastgen '$phpastgen' produced no 'Generator version:' line " +
            s"(plugins not built / generator unavailable); set ${PhpParser.PhpParserBinEnvVar} to a 2.x build"
      )
    else
      version.flatMap(majorVersion) match
        case Some(major) if major >= MinimumGeneratorMajor => None
        case _ =>
            Some(
              s"phpastgen '$phpastgen' reports ${version.getOrElse("no version")}; " +
                  s"these tests need $MinimumGeneratorMajor.x - set ${PhpParser.PhpParserBinEnvVar} to such a build"
            )
  end gateFailure

  private def requireGenerator(): Unit =
      gateFailure(parserInfo).foreach(reason => cancel(reason))

  private def runAtom(args: String*): Either[String, String] = Atom.run(args.toArray)

  "atom generation for PHP" should {

      "build an atom from a PHP project" in {
          requireGenerator()
          val atomFile = workspace / "generation.atom"

          val result = runAtom("-l", "php", "-o", atomFile.pathAsString, projectDir.pathAsString)

          result.isRight shouldBe true
          atomFile.exists shouldBe true
          atomFile.size should be > 0L
      }
  }

  "usage slicing for PHP" should {

      "report the project's own types and their methods" in {
          requireGenerator()
          val atomFile  = workspace / "usages.atom"
          val sliceFile = workspace / "usages.json"

          val result = runAtom(
            "usages",
            "-l",
            "php",
            "-o",
            atomFile.pathAsString,
            "-s",
            sliceFile.pathAsString,
            projectDir.pathAsString
          )

          result.isRight shouldBe true
          sliceFile.exists shouldBe true

          val slice            = ujson.read(sliceFile.contentAsString)
          val userDefinedTypes = slice("userDefinedTypes").arr

          // The project's own class is described with the methods it declares, i.e. the frontend
          // produced a real TypeDecl and METHODs rather than placeholders. Name matching is by
          // suffix because PHP fully-qualified names are namespace-prefixed (e.g. `App\UserService`).
          val serviceType = userDefinedTypes
              .find(_.obj("name").str.split("[.\\\\]").last == "UserService")
              .getOrElse(fail(
                s"no UserService type in ${userDefinedTypes.map(_.obj("name").str).mkString(", ")}"
              ))
          (serviceType("procedures").arr.map(_.obj("callName").str) should contain).allOf(
            "report",
            "normalize"
          )
      }
  }

  "framework tagging and dataflow for PHP" should {

      // The one place that proves the frontend, the Milestone E tagging pass and the dataflow
      // engine actually COMPOSE through the real `Atom.run` pipeline (finding D-H2). `reachables`
      // is driven entirely by source/sink tags, so if `PhpFrameworkTagsPass` did not run - or wrote
      // tag names nothing consumes - this produces no framework flows and the assertions fail.
      "surface framework source/sink tags in a reachables slice" in {
          requireGenerator()
          val atomFile  = workspace / "reachables.atom"
          val sliceFile = workspace / "reachables.json"

          val result = runAtom(
            "reachables",
            "-l",
            "php",
            "-o",
            atomFile.pathAsString,
            "-s",
            sliceFile.pathAsString,
            projectDir.pathAsString
          )

          result.isRight shouldBe true
          sliceFile.exists shouldBe true

          val flowGroups = ujson.read(sliceFile.contentAsString).arr
          flowGroups should not be empty

          // Every node of every reported flow, with the tag string the slicer attached.
          val nodes = flowGroups.flatMap(_.obj.get("flows").map(_.arr).getOrElse(ujson.Arr().arr))
          def tagsOf(node: ujson.Value): Seq[String] =
              node.obj
                  .get("tags")
                  .map(_.str)
                  .getOrElse("")
                  .split(",")
                  .toSeq
                  .map(_.trim)
                  .filter(_.nonEmpty)
          val allTags = nodes.flatMap(tagsOf).toSet

          // The tag vocabulary the pass writes and atom's reachability engine consumes.
          allTags should contain("framework-input")
          allTags should contain("framework-output")

          // ...and they compose: at least one reported flow runs from a tagged framework source to a
          // tagged framework sink, i.e. tagging + dataflow together, not tags in isolation.
          val sourceToSink = flowGroups.exists { group =>
            val flow = group.obj.get("flows").map(_.arr).getOrElse(ujson.Arr().arr)
            flow.exists(n => tagsOf(n).contains("framework-input")) &&
            flow.exists(n => tagsOf(n).contains("framework-output"))
          }
          withClue(
            s"no flow linked a framework-input source to a framework-output sink; tags seen: ${allTags
                    .toSeq.sorted.mkString(", ")}: "
          ) {
              sourceToSink shouldBe true
          }
      }
  }

  "the generator version gate" should {

      // D-M2: the three cancel branches, exercised deterministically without a stub generator.
      "cancel when the generator reports that PHP is not installed" in {
          gateFailure(Some(Seq("Parser backend: nikic/php-parser@5.8.0", "PHP is not installed")))
              .getOrElse(fail("expected a cancel reason")) should include("PHP is not installed")
      }

      "cancel when no 'Generator version:' line is present" in {
          gateFailure(Some(Seq("Parser backend: nikic/php-parser@5.8.0")))
              .getOrElse(fail("expected a cancel reason")) should include("Generator version")
      }

      "cancel when the probe could not be launched at all" in {
          gateFailure(None) should be(defined)
      }

      "cancel when the generator is older than the required major version" in {
          gateFailure(Some(Seq("Generator version: 1.9.9")))
              .getOrElse(fail("expected a cancel reason")) should include("1.9.9")
      }

      "accept a generator at or above the required major version" in {
          gateFailure(Some(Seq("Generator version: 2.0.0"))) shouldBe None
          gateFailure(Some(Seq("Generator version: 3.1.4-rc1"))) shouldBe None
      }
  }

end PhpAtomWorkflowTests
