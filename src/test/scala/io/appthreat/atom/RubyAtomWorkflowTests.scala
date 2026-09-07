package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.ruby2atom.parser.RubyAstGenRunner
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** End-to-end Ruby workflow through atom's own CLI entry point (`Atom.run`): atom generation, usage
  * slicing and data-flow slicing over a small Ruby project.
  *
  * These are the only Ruby tests that exercise the whole chain - `rbastgen` writes AST JSON,
  * ruby2atom rebuilds it into a CPG, atom's overlays and slicers run on top. That needs a working
  * generator, so the suite is **cancelled** (not failed) when none is reachable.
  *
  * `rbastgen` is a Node wrapper (from atom-parsetools) around the `ruby_ast_gen` Ruby script, so a
  * generator branch is validated by repointing the wrapper, or - when the executable itself is
  * being replaced - through ruby2atom's own override:
  *
  * {{{
  * RUBY_ASTGEN_BIN=/path/to/ruby_ast_gen/exe/ruby_ast_gen sbt test
  * RBASTGEN_PATH=/path/to/rbastgen sbt test
  * sbt -Drbastgen.path=/path/to/rbastgen test
  * }}}
  *
  * Both are needed because atom loads ruby2atom in-process: the frontend inherits this JVM's
  * environment and cannot otherwise be pointed elsewhere.
  */
class RubyAtomWorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val MinimumGeneratorMajor = 2

  private val rbastgen: String = RubyAstGenRunner.resolveProgram()

  private lazy val generatorVersion: Option[String] =
      Try(
        scala.sys.process.Process(Seq(rbastgen, "--version")).lazyLines_!.headOption.map(_.trim)
      ).toOption.flatten

  private var workspace: BFile  = scala.compiletime.uninitialized
  private var projectDir: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomRubyWorkflow")
    projectDir = (workspace / "project").createDirectories()
    (projectDir / "lib").createDirectories()

    (projectDir / "lib" / "user_store.rb").write(
      """class UserStore
        |  def initialize
        |    @rows = []
        |  end
        |
        |  def add(row)
        |    @rows << row
        |    row
        |  end
        |end
        |""".stripMargin
    )

    // A source (ARGV) reaching a sink (system) through a method call and a local, plus 2.0.0-era
    // syntax the generator emits facts for: a heredoc, a percent array, an `it` block and a
    // `case/in` pattern match.
    (projectDir / "app.rb").write(
      """require "lib/user_store"
        |
        |class UserService
        |  def initialize(store)
        |    @store = store
        |  end
        |
        |  def report(raw_name)
        |    label = normalize(raw_name)
        |    @store.add(label)
        |    system("echo #{label}")
        |    label
        |  end
        |
        |  def normalize(value)
        |    value.strip
        |  end
        |
        |  def summary
        |    <<~TEXT
        |      user report
        |    TEXT
        |  end
        |end
        |
        |FIELDS = %w[name email]
        |FIELDS.each { it.upcase }
        |
        |service = UserService.new(UserStore.new)
        |service.report(ARGV[0])
        |
        |case ARGV
        |in [String => first, *]
        |  puts first
        |else
        |  puts "none"
        |end
        |""".stripMargin
    )
  end beforeAll

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def majorVersion(version: String): Option[Int] =
      version.takeWhile(_.isDigit).toIntOption

  /** Cancels the enclosing test unless a generator implementing the 2.x JSON contract is reachable.
    * The assertions below describe that contract (syntax facts, `itblock`, pattern nodes), and an
    * older - or, as seen in the wild, a broken - binary silently produces an empty atom rather than
    * a failure, which would show up here as a puzzling assertion error.
    */
  private def requireGenerator(): Unit =
      generatorVersion.flatMap(majorVersion) match
        case Some(major) if major >= MinimumGeneratorMajor =>
        case other =>
            cancel(
              s"rbastgen '$rbastgen' reports ${generatorVersion.getOrElse("no version")}; " +
                  s"these tests need $MinimumGeneratorMajor.x - set ${RubyAstGenRunner.ProgramEnvVar} " +
                  s"or -D${RubyAstGenRunner.ProgramProperty} to such a build"
            )

  private def runAtom(args: String*): Either[String, String] = Atom.run(args.toArray)

  "atom generation for Ruby" should {

      "build an atom from a Ruby project" in {
          requireGenerator()
          val atomFile = workspace / "generation.atom"

          val result = runAtom("-l", "ruby", "-o", atomFile.pathAsString, projectDir.pathAsString)

          result.isRight shouldBe true
          atomFile.exists shouldBe true
          atomFile.size should be > 0L
      }
  }

  "usage slicing for Ruby" should {

      "report the project's own types and their methods" in {
          requireGenerator()
          val atomFile  = workspace / "usages.atom"
          val sliceFile = workspace / "usages.json"

          val result = runAtom(
            "usages",
            "-l",
            "ruby",
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
          val objectSlices     = slice("objectSlices").arr

          // The project's own classes are described with the methods they declare, i.e. the frontend
          // produced real TypeDecls and METHODs rather than placeholders.
          val serviceType = userDefinedTypes
              .find(_.obj("name").str.endsWith(".UserService"))
              .getOrElse(fail(
                s"no UserService type in ${userDefinedTypes.map(_.obj("name").str).mkString(", ")}"
              ))
          serviceType("fileName").str should endWith("app.rb")
          (serviceType("procedures").arr.map(_.obj("callName").str) should contain).allOf(
            "report",
            "normalize"
          )
          serviceType("fields").arr.map(_.obj("name").str) should contain("@store")

          // Both files contribute slices, and `report`'s local `label` is attributed to the call that
          // defines it and to the call it is passed to - the point of a usage slice.
          (objectSlices.map(_.obj("fileName").str).toSet should contain).allOf(
            "app.rb",
            "lib/user_store.rb"
          )

          val reportSlice = objectSlices
              .find(_.obj("fullName").str.endsWith("UserService.report"))
              .getOrElse(fail("no usage slice for UserService.report"))
          val labelUsage = reportSlice("usages").arr
              .find(_.obj("targetObj").obj("name").str == "label")
              .getOrElse(fail("`label` is not a tracked usage in UserService.report"))
          labelUsage("definedBy").obj("name").str shouldBe "normalize"
          labelUsage("invokedCalls").arr.map(_.obj("callName").str) should contain("add")
      }
  }

  "data-flow slicing for Ruby" should {

      "produce a data-dependency graph covering the tainted argument" in {
          requireGenerator()
          val atomFile  = workspace / "dataflow.atom"
          val sliceFile = workspace / "dataflow.json"

          val result = runAtom(
            "data-flow",
            "-l",
            "ruby",
            "-o",
            atomFile.pathAsString,
            "-s",
            sliceFile.pathAsString,
            projectDir.pathAsString
          )

          result.isRight shouldBe true
          sliceFile.exists shouldBe true

          val slice = ujson.read(sliceFile.contentAsString)
          val graph = slice("graph").obj
          val nodes = graph("nodes").arr.map(node => node.obj("id").num.toLong -> node).toMap
          nodes should not be empty
          graph("edges").arr should not be empty

          // Follow REACHING_DEF from the tainted parameter: `report(raw_name)` -> `normalize(...)` ->
          // `label` -> the interpolated command string. A slice that merely listed the file's nodes
          // would not connect them.
          val reachingDef = graph("edges").arr
              .filter(_.obj("label").str == "REACHING_DEF")
              .groupBy(_.obj("src").num.toLong)
              .view.mapValues(_.map(_.obj("dst").num.toLong).toSet).toMap

          val taintSource = nodes.collectFirst {
              case (id, node)
                  if node.obj.get("label").exists(_.str == "METHOD_PARAMETER_IN") &&
                      node.obj.get("code").exists(_.str == "raw_name") => id
          }.getOrElse(fail("the `raw_name` parameter is missing from the data-flow slice"))

          def reachable(from: Long): Set[Long] =
            var seen    = Set(from)
            var pending = List(from)
            while pending.nonEmpty do
              val current = pending.head
              pending = pending.tail
              reachingDef.getOrElse(current, Set.empty).filterNot(seen.contains).foreach { next =>
                seen += next
                pending = next :: pending
              }
            seen

          val taintedCode = reachable(taintSource)
              .flatMap(id => nodes.get(id).flatMap(_.obj.get("code").map(_.str)))
          taintedCode should contain("label")
          taintedCode.exists(_.contains("echo #{label}")) shouldBe true
      }
  }
end RubyAtomWorkflowTests
