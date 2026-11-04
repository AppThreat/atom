package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.atom.slicing.CallSiteSlicing
import io.appthreat.atom.slicing.callsites.GraphReorder
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.semanticcpg.language.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.jar.{JarEntry, JarOutputStream, Manifest}
import javax.tools.ToolProvider
import scala.util.{Try, Using}

/** The callsites slice gives the same bytes for the same input (AppThreat/atom#340): across fresh
  * builds, across slices of one atom, and across atoms whose graphs were created in another order.
  *
  * The streaming passes of the frontends apply their diffs in the order the parts (files) finish,
  * so two builds of one input number their nodes differently, and a slice that reads the first of
  * several matches, ties a `minBy`, builds a map from several nodes or iterates a fixpoint in node
  * order differs between builds. Building a small fixture twice shows that only when the threads
  * happen to finish differently, so every case here does that (two fresh builds in this JVM, whose
  * slice files must be equal byte for byte) and then slices the stored atom again, slices a copy of
  * it with the parts created in the opposite order, and slices copies with every node created at a
  * random position (eight seeds); see [[slicing.callsites.GraphReorder]]. A slice that depends on
  * node ids or creation order gives other bytes for one of the copies.
  *
  * One test per source (`CallSiteSource.forAtom` picks them by the atom's language) runs the
  * fixtures its test suite uses; further tests run the shape that exposed a dependence, each with
  * the records the sources write for it. `--external-paths` is run with and without for every
  * source that takes it. A run that needs a tool the machine lacks cancels with the reason.
  */
class CallSitesDeterminismTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override protected def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomDeterminism")

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  // ------------------------------------------------------------------------------------- tools
  private def probe(command: String*): Option[String] =
      Try(scala.sys.process.Process(command).lazyLines_!.headOption.map(_.trim)).toOption.flatten

  private def requireAstgen(): Unit =
      if !probe("astgen", "--version").exists(v =>
            v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2)
        )
      then cancel("astgen unavailable or too old; JavaScript needs the astgen from atom-parsetools")

  private def requirePhp(): Unit =
      if Try(scala.sys.process.Process(Seq("php", "--version")).!!).isFailure then
        cancel("php is not installed")

  private def requireRuby(): Unit =
      if !probe("rbastgen", "--version").exists(v =>
            v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2)
        )
      then cancel("rbastgen unavailable or older than 2.x; the Ruby fixture needs it")

  private def edgaPath: String =
    val path = sys.env.get("EDGA_PATH").orElse(sys.props.get("edga.path")).filter(p =>
        BFile(p).isRegularFile
    ).orElse(probe("which", "edga").filter(_.nonEmpty))
    if path.isEmpty then cancel("edga is not installed (set EDGA_PATH or put edga on PATH)")
    path.get

  // ------------------------------------------------------------------------------------ helpers
  private def copyOf(resource: String, name: String): BFile =
    val target = workspace / name
    BFile(getClass.getResource(resource).toURI).copyTo(target)
    target

  private def build(language: String, input: BFile, name: String, run: Int, args: Seq[String]) =
    val atomFile = workspace / s"$name-$run.atom"
    val slice    = workspace / s"$name-$run.json"
    val result = Atom.run(
      ("callsites" +: "--cache" +: "none" +: "-l" +: language +: args ++:
          Seq("-o", atomFile.pathAsString, "-s", slice.pathAsString, input.pathAsString)).toArray
    )
    withClue(s"atom callsites failed for $name: ")(result.isRight shouldBe true)
    (atomFile, slice)

  private def load(atomFile: BFile): Cpg =
      CpgLoader.loadFromOverflowDb(
        CpgLoaderConfig().withOverflowConfig(
          overflowdb.Config.withDefaults().withStorageLocation(atomFile.pathAsString)
        ).doNotCreateIndexesOnLoad
      )

  private def records(slice: String): Vector[String] =
      ujson.read(slice).obj("callsites").arr.map(_.render()).toVector

  /** What differs between two slices, as the records only one of them has and the first position
    * where their order parts.
    */
  private def difference(expected: String, actual: String): String =
    val e              = records(expected)
    val a              = records(actual)
    val firstDiffering = e.zip(a).indexWhere((x, y) => x != y)
    s"""${e.size} records against ${a.size}; first position that differs: $firstDiffering
       |  only in the first: ${e.filterNot(a.contains).take(5).mkString("\n    ", "\n    ", "")}
       |  only in the second: ${a.filterNot(e.contains).take(5).mkString("\n    ", "\n    ", "")}""".stripMargin

  private def same(expected: String, actual: String): Unit =
      if expected != actual then fail(difference(expected, actual))

  /** The files an atom stores for the slice and the taggers (`atom-tag-state.json`,
    * `callsites-c-layout.json`, `callsites-vendored.json`, `callsites-app-boundary.json`,
    * `java-classpath.json`), by name: the same input must store the same bytes.
    */
  private def stateFiles(atomFile: BFile): Map[String, List[String]] =
    val cpg = load(atomFile)
    try
        CallSitesDeterminismTests.StateFiles.map(name =>
            name -> cpg.configFile.name(name).content.l
        ).filter(_._2.nonEmpty).toMap
    finally cpg.close()

  /** The `--app-prefix` and `--external-paths` of a run, which the slice of a stored atom needs. */
  private def prefixesOf(args: Seq[String]): Option[Seq[String]] =
      Option(args.sliding(2).collect { case Seq("--app-prefix", p) => p }.toSeq)
          .filter(_.nonEmpty)

  private def externalPathsOf(args: Seq[String]): Option[String] =
      args.sliding(2).collectFirst { case Seq("--external-paths", p) => p }

  /** Builds `input` twice and slices the first atom five more ways; every slice must be the bytes
    * of the first build. Returns them.
    */
  private def deterministic(
    name: String,
    language: String,
    input: BFile,
    args: Seq[String] = Nil
  ): String =
    val (atomFile, firstSlice)    = build(language, input, name, 1, args)
    val (secondAtom, secondSlice) = build(language, input, name, 2, args)
    val first                     = firstSlice.contentAsString
    withClue(s"$name, two fresh builds: ")(same(first, secondSlice.contentAsString))
    val state = stateFiles(atomFile)
    state.keySet should contain("atom-tag-state.json")
    withClue(s"$name, the state files of two fresh builds: ")(stateFiles(secondAtom) shouldBe state)
    val cpg = load(atomFile)
    def sliceOf(graph: Cpg): String =
        CallSiteSlicing.calculateCallSitesSlice(graph, prefixesOf(args), externalPathsOf(args))
            .toJson
    try
      withClue(s"$name, a second slice of the stored atom: ")(same(first, sliceOf(cpg)))
      val reversed = GraphReorder.reversed(cpg)
      try withClue(s"$name, the files created in the opposite order: ")(same(
            first,
            sliceOf(reversed)
          ))
      finally reversed.close()
      (1L to 8L).foreach { seed =>
        val shuffled = GraphReorder.shuffled(cpg, seed)
        try
            withClue(s"$name, the nodes created in the order of seed $seed: ")(
              same(first, sliceOf(shuffled))
            )
        finally shuffled.close()
      }
    finally cpg.close()
    first
  end deterministic

  private def rows(slice: String): List[(String, Int, String, String, String, String, String)] =
      ujson.read(slice).obj("callsites").arr.map(r =>
          (
            r("file").str.replace('\\', '/'),
            r("line").num.toInt,
            r("module").str,
            r("path").str,
            r("kind").str,
            r("via").str,
            r.obj.get("purl").map(_.str).getOrElse("")
          )
      ).toList

  // ------------------------------------------------------------------------------- the sources
  "the JavaScript source" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          requireAstgen()
          deterministic(
            "js-fields",
            "javascript",
            copyOf("/js-usages/callsites-fields", "js-fields")
          )
          deterministic("js-plain", "javascript", copyOf("/js-usages/callsites", "js-plain"))
          deterministic("js-sfc", "javascript", copyOf("/js-usages/sfc", "js-sfc"))
          // an SBOM in the atom: the purls come from it
          val withBom = copyOf("/js-usages/purl-bom", "js-purl-bom")
          deterministic(
            "js-purl-bom",
            "javascript",
            withBom,
            Seq("--bom-file", (withBom / "bom.json").pathAsString)
          )
      }

      "record a method decorator once, at the decorated method, whatever order the graph was created in" in {
          requireAstgen()
          val slice = deterministic(
            "js-decorators",
            "javascript",
            copyOf("/js-usages/decorators", "js-decorators")
          )
          // the METHOD and the MEMBER node both carry `@Get(":id")`; the METHOD's names the method
          ujson.read(slice).obj("callsites").arr.filter(r => r("path").str == "Get")
              .map(r => (r("line").num.toInt, r("method").str)).toList shouldBe
              List((14, "src/cats.ts::program:CatsController:findOne"))
      }

      "resolve a long chain of aliases the same way, whatever order the graph was created in" in {
          requireAstgen()
          val slice =
              deterministic("js-chain", "javascript", copyOf("/determinism/js-chain", "js-chain"))
          // `const c1 = lodash; const c2 = c1; ...; c40.chunk(...)`: every link and the call are
          // uses of lodash, found in one round when the assignments are taken in the order they
          // are written; the rounds are capped, so the order decides how far a chain gets
          val uses = rows(slice).filter(_._1 == "src/chain.js")
          uses.map(_._2) shouldBe (3 to 42).toList :+ 44
          uses.last shouldBe ("src/chain.js", 44, "lodash", "chunk", "call", "binding", "")
      }

      "give a vendored module two components share no purl, whatever order the graph was created in" in {
          requireAstgen()
          val project = copyOf("/determinism/js-twin", "js-twin")
          val slice = deterministic(
            "js-twin",
            "javascript",
            project,
            Seq("--bom-file", (project / "bom.json").pathAsString)
          )
          // Two versions of `vendlib` side by side. A call names the vendored method it reaches,
          // which belongs to one component; a plain reference names only the module, and the BOM
          // does not say which of the two purls is the reference's
          rows(slice) shouldBe List(
            ("main.js", 3, "vendlib", "run", "call", "binding", "pkg:generic/vendlib@1.0.0"),
            ("main.js", 4, "vendlib", "", "ref", "binding", ""),
            ("use.js", 3, "vendlib", "run", "call", "binding", "pkg:generic/vendlib@2.0.0"),
            ("use.js", 4, "vendlib", "", "ref", "binding", "")
          )
      }

      "give the same bytes with and without --external-paths" in {
          requireAstgen()
          deterministic(
            "js-no-option",
            "javascript",
            copyOf("/vendored/javascript", "js-no-option")
          )
          deterministic(
            "js-pattern",
            "javascript",
            copyOf("/vendored/javascript", "js-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the Python source" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          deterministic("py-fixture", "python", copyOf("/py-usages/callsites", "py-fixture"))
          val withBom = copyOf("/py-usages/purl-bom", "py-purl-bom")
          deterministic(
            "py-purl-bom",
            "python",
            withBom,
            Seq("--bom-file", (withBom / "bom.json").pathAsString)
          )
      }

      "read a decorator's base from its own file, whatever order the graph was created in" in {
          val slice = deterministic(
            "py-decorators",
            "python",
            copyOf("/determinism/py-decorators", "py-decorators")
          )
          // both files write `@app.route` on line 6 with another `app`: Flask and Blueprint
          rows(slice).filter(_._5 == "decorator").map(r => (r._1, r._2, r._3, r._4)) shouldBe List(
            ("app_a.py", 6, "flask", "Flask.route"),
            ("app_b.py", 6, "flask", "Blueprint.route")
          )
      }

      "bind a name imported twice to the import written last, whatever order the graph was created in" in {
          val slice = deterministic(
            "py-fallback",
            "python",
            copyOf("/determinism/py-fallback", "py-fallback")
          )
          // `try: from urllib.parse import quote` / `except ImportError: from urllib import quote`
          rows(slice).map(r => (r._1, r._2, r._3, r._4, r._5)) shouldBe
              List(("compat.py", 6, "urllib", "quote", "call"))
      }

      "give the same bytes with and without --external-paths" in {
          deterministic("py-no-option", "python", copyOf("/vendored/python", "py-no-option"))
          deterministic(
            "py-pattern",
            "python",
            copyOf("/vendored/python", "py-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the Java source" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          val project = workspace / "java-fixture"
          val fixture = BFile(getClass.getResource("/java-callsites").toURI)
          (fixture / "src").copyTo(project / "src")
          (fixture / "lib").copyTo(project / "lib")
          val slice = deterministic(
            "java-fixture",
            "java",
            project,
            Seq("--frontend-args", s"inference-jar-paths=${(project / "lib").pathAsString}")
          )
          // Lombok's `@Data` puts `@SuppressWarnings` on each setter it generates, all at the
          // `@Data` (line 6): the setter of the first field, whichever setter the graph holds
          // first; each getter sits at its own field
          ujson.read(slice).obj("callsites").arr
              .filter(r =>
                  r("file").str.endsWith("Dto.java") && r("module").str.endsWith("SuppressWarnings")
              )
              .map(r => (r("line").num.toInt, r("method").str)).toList shouldBe List(
            (6, "proj.Dto.setSecurity:void(web.Security)"),
            (8, "proj.Dto.getSecurity:web.Security()"),
            (9, "proj.Dto.getCount:int()"),
            (10, "proj.Dto.getTags:java.lang.String[]()")
          )
      }

      "give the same bytes with and without --external-paths" in {
          deterministic("java-no-option", "java", copyOf("/vendored/java", "java-no-option"))
          deterministic(
            "java-pattern",
            "java",
            copyOf("/vendored/java", "java-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the PHP source" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          requirePhp()
          deterministic("php-fixture", "php", copyOf("/php-callsites", "php-fixture"))
      }

      "map a class that two Composer trees name to the nearest tree, whatever order the graph was created in" in {
          requirePhp()
          val slice = deterministic(
            "php-composer",
            "php",
            copyOf("/determinism/php-composer", "php-composer")
          )
          // `vendor/` and `tools/vendor/` each map Acme\Widgets\Gadget to their own copy
          rows(slice).map(r => (r._2, r._5, r._7)) shouldBe List(
            (8, "decltype", "pkg:composer/acme/widgets@1.0.0"),
            (10, "new", "pkg:composer/acme/widgets@1.0.0")
          )
      }

      "give the same bytes with and without --external-paths" in {
          requirePhp()
          deterministic("php-no-option", "php", copyOf("/vendored/php", "php-no-option"))
          deterministic(
            "php-pattern",
            "php",
            copyOf("/vendored/php", "php-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the Ruby source" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          requireRuby()
          deterministic("ruby-fixture", "ruby", copyOf("/ruby-callsites", "ruby-fixture"))
      }

      "give the same bytes with and without --external-paths" in {
          requireRuby()
          deterministic("ruby-no-option", "ruby", copyOf("/vendored/ruby", "ruby-no-option"))
          deterministic(
            "ruby-pattern",
            "ruby",
            copyOf("/vendored/ruby", "ruby-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the C source through CDT" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          val library = copyOf("/c-callsites/externlib", "cdt-lib")
          deterministic(
            "c-cdt",
            "c",
            copyOf("/c-callsites/proj", "c-cdt"),
            Seq("--include-path", library.pathAsString)
          )
          deterministic(
            "c-cdt-headers",
            "c",
            copyOf("/determinism/c-headers/proj", "c-cdt-headers"),
            Seq("--include-path", library.pathAsString)
          )
      }

      "give the same bytes with and without --external-paths" in {
          deterministic("c-no-option", "c", copyOf("/vendored/c", "c-no-option"))
          deterministic(
            "c-pattern",
            "c",
            copyOf("/vendored/c", "c-pattern"),
            Seq("--external-paths", "third_party")
          )
      }
  }

  "the C source through EDG" should {

      "give the same bytes for the same input, whatever order the graph was created in" in {
          val edga    = edgaPath
          val library = copyOf("/c-callsites/externlib", "edg-lib")
          val edg     = Seq("--c-frontend", "edg", "--edga-path", edga)
          deterministic(
            "c-edg",
            "c",
            copyOf("/c-callsites/proj", "c-edg"),
            Seq("--include-path", library.pathAsString) ++ edg
          )
          deterministic(
            "c-edg-headers",
            "c",
            copyOf("/determinism/c-headers/proj", "c-edg-headers"),
            Seq("--include-path", library.pathAsString) ++ edg
          )
      }

      "give the same bytes with and without --external-paths" in {
          val edg = Seq("--c-frontend", "edg", "--edga-path", edgaPath)
          deterministic("c-edg-no-option", "c", copyOf("/vendored/c", "c-edg-no-option"), edg)
          deterministic(
            "c-edg-pattern",
            "c",
            copyOf("/vendored/c", "c-edg-pattern"),
            edg ++ Seq("--external-paths", "third_party")
          )
      }
  }

  "usages --also-callsites" should {

      "write the bytes the callsites command writes" in {
          val input         = copyOf("/py-usages/callsites", "py-also")
          val (_, separate) = build("python", input, "py-also", 1, Nil)
          val also          = workspace / "py-also-callsites.json"
          val result = Atom.run(
            Array(
              "usages",
              "--cache",
              "none",
              "-l",
              "python",
              "--also-callsites",
              also.pathAsString,
              "-o",
              (workspace / "py-also-usages.atom").pathAsString,
              "-s",
              (workspace / "py-also-usages.json").pathAsString,
              input.pathAsString
            )
          )
          withClue("atom usages --also-callsites: ")(result.isRight shouldBe true)
          same(separate.contentAsString, also.contentAsString)
      }
  }

  "the JVM bytecode source" should {

      /** The jar of the fixture, as `BytecodeCallSitesTests` builds it. */
      def fixtureJar(): BFile =
        val compiler = ToolProvider.getSystemJavaCompiler
        if compiler == null then cancel("no system Java compiler in this JVM")
        val fixture = BFile(getClass.getResource("/jvm-callsites").toURI)
        val classes = workspace / "bytecode-classes"
        classes.createDirectories()
        val sources = (fixture / "src").listRecursively.filter(_.extension.contains(".java"))
            .map(_.pathAsString).toList
        compiler.run(null, null, null, "-d" :: classes.pathAsString :: sources*) shouldBe 0
        val manifest = new Manifest()
        manifest.getMainAttributes.putValue("Manifest-Version", "1.0")
        manifest.getMainAttributes.putValue("Automatic-Module-Name", "me.app")
        val jar = workspace / "bytecode-fixture.jar"
        Using.resource(new JarOutputStream(jar.newOutputStream, manifest)) { stream =>
          def add(root: BFile): Unit =
              root.listRecursively.filter(_.isRegularFile).foreach { f =>
                stream.putNextEntry(new JarEntry(root.relativize(f).toString.replace('\\', '/')))
                stream.write(f.byteArray)
                stream.closeEntry()
              }
          add(classes)
          add(fixture / "meta")
        }
        jar
      end fixtureJar

      val jdk = Seq("--jdk-path", sys.props.getOrElse("java.home", ""))

      "give the same bytes for the same input, whatever order the graph was created in" in {
          val jar = fixtureJar()
          // the manifest's boundary, the library as the application, and the class directory
          deterministic("bytecode-jar", "jar", jar, jdk)
          deterministic("bytecode-flipped", "jar", jar, jdk ++ Seq("--app-prefix", "me.lib"))
          val dirOnly = workspace / "bytecode-dir"
          (workspace / "bytecode-classes").copyTo(dirOnly)
          (dirOnly / "me" / "lib").delete()
          deterministic("bytecode-dir", "jar", dirOnly, jdk)
      }

      "give the same bytes for a dex input" in {
          val dex = BFile(getClass.getResource("/jvm-callsites/dex/classes.dex").toURI)
          deterministic("bytecode-dex", "dex", dex, jdk ++ Seq("--app-prefix", "dexy.app"))
      }
  }
end CallSitesDeterminismTests

object CallSitesDeterminismTests:
  val StateFiles: List[String] = List(
    "atom-tag-state.json",
    "callsites-c-layout.json",
    "callsites-vendored.json",
    "callsites-app-boundary.json",
    "java-classpath.json"
  )
