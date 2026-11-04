package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.jar.{JarEntry, JarOutputStream, Manifest}
import javax.tools.ToolProvider
import scala.util.{Try, Using}

/** The callsites lesson (`docs/docs/lessons/callsites.md`) shows commands and the records they
  * write. This suite runs the commands of the page, as written, on the fixtures under
  * `src/test/resources`, and checks that every record the page shows is a record of the real
  * output.
  *
  * The page marks an example with `<!-- example: NAME -->`. The `atom callsites` line of the `bash`
  * block that follows is run through `Atom.run` with `/tmp/ex-` replaced by this suite's workspace
  * and `$JAVA_HOME` by the running JVM's home; the lines before it only prepare the input (`cp -R`
  * of a fixture, `javac` and `jar` for the jar example) and this suite does the same preparation by
  * itself. The `json` block that follows the command is the excerpt: each of its lines is a record
  * (or, for the `metadata` marker, the metadata object) that must be in the slice the command
  * wrote, compared as JSON so the order of fields does not matter. An example whose tool is missing
  * (astgen, php, rbastgen) is cancelled with the reason.
  */
class CallSitesLessonTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val page = BFile("docs/docs/lessons/callsites.md")

  private var workspace: BFile = scala.compiletime.uninitialized

  override protected def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomLesson")

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def resource(path: String): BFile = BFile(getClass.getResource(s"/$path").toURI)

  private def probe(command: String*): Option[String] =
      Try(scala.sys.process.Process(command).lazyLines_!.headOption.map(_.trim)).toOption.flatten

  private def needsAstgen(name: String): Option[String] =
      if probe(name, "--version").exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))
      then None
      else Some(s"$name 2.x is not installed")

  /** The tool an example needs and the input it prepares; `/tmp/ex-<name>` is its input. */
  private final case class Example(
    name: String,
    needs: () => Option[String],
    prepare: BFile => Unit,
    suffix: String = ""
  )

  private def copyFixture(fixture: String)(target: BFile): Unit =
      resource(fixture).copyTo(target)

  private def buildJar(target: BFile): Unit =
    val compiler = ToolProvider.getSystemJavaCompiler
    val fixture  = resource("jvm-callsites")
    val classes  = target.parent / "ex-jar-classes"
    classes.createDirectories()
    val sources = (fixture / "src").listRecursively.filter(_.extension.contains(".java"))
        .map(_.pathAsString).toList
    compiler.run(null, null, null, "-d" :: classes.pathAsString :: sources*) shouldBe 0
    (fixture / "meta").copyTo(classes, overwrite = true)
    val manifest = new Manifest()
    manifest.getMainAttributes.putValue("Manifest-Version", "1.0")
    manifest.getMainAttributes.putValue("Automatic-Module-Name", "me.app")
    Using.resource(new JarOutputStream(target.newOutputStream, manifest)) { stream =>
        classes.listRecursively.filter(_.isRegularFile).foreach { f =>
          stream.putNextEntry(new JarEntry(classes.relativize(f).toString.replace('\\', '/')))
          stream.write(f.byteArray)
          stream.closeEntry()
        }
    }

  private val examples = List(
    Example("javascript", () => needsAstgen("astgen"), copyFixture("js-usages/callsites")),
    Example("python", () => None, copyFixture("py-usages/callsites")),
    Example("java", () => None, copyFixture("java-callsites/src")),
    Example(
      "php",
      () =>
          Try(scala.sys.process.Process(Seq("php", "--version")).!!).failed.toOption
              .map(_ => "php is not installed"),
      copyFixture("php-callsites")
    ),
    Example("ruby", () => needsAstgen("rbastgen"), copyFixture("ruby-callsites")),
    Example("c", () => None, copyFixture("c-callsites")),
    Example(
      "jar",
      () =>
          if ToolProvider.getSystemJavaCompiler == null then Some("no system Java compiler")
          else None,
      buildJar,
      ".jar"
    ),
    Example("dex", () => None, f => resource("jvm-callsites/dex/classes.dex").copyTo(f), ".dex"),
    Example("vendored-javascript", () => needsAstgen("astgen"), copyFixture("vendored/javascript"))
  )

  /** The lines of the first fenced block after the marker of `name` and `kind` (`command` or
    * `output`).
    */
  private def block(name: String, kind: String): List[String] =
    val lines  = page.lines.toList
    val marker = s"<!-- $kind: $name -->"
    val at     = lines.indexOf(marker)
    withClue(s"the page has no `$marker`: ")(at should be >= 0)
    lines.drop(at + 1).dropWhile(!_.startsWith("```")).drop(1).takeWhile(!_.startsWith("```"))

  examples.foreach { example =>
      s"the ${example.name} example of the lesson" should {
          "write the records the page shows" in {
              example.needs().foreach(reason => cancel(reason))
              val input = workspace / s"ex-${example.name}${example.suffix}"
              example.prepare(input)
              val command =
                  block(example.name, "command").find(_.startsWith("atom callsites ")).getOrElse(
                    fail(s"the ${example.name} command block has no `atom callsites` line")
                  )
              val tokens = command.split("\\s+").toList.drop(1).map(
                _.replace("/tmp/ex-", s"${workspace.pathAsString}/ex-")
                    .replace("$JAVA_HOME", sys.props.getOrElse("java.home", ""))
              )
              val slice = tokens(tokens.indexOf("-s") + 1)
              Atom.run(tokens.toArray).isRight shouldBe true
              val written = ujson.read(BFile(slice).contentAsString)
              val real    = written.obj("callsites").arr.toSet
              val shown   = block(example.name, "output").filter(_.trim.nonEmpty).map(ujson.read(_))
              shown should not be empty
              shown.foreach { record =>
                  withClue(
                    s"this record of the ${example.name} example is not in the real output: "
                  ) {
                      (real.contains(record) || written.obj.get("metadata").contains(
                        record
                      )) shouldBe true
                  }
              }
          }
      }
  }
end CallSitesLessonTests
