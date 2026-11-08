package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** The reachability lesson (`docs/docs/lessons/reachability.md`) shows commands and the records
  * they write. This suite runs the commands of the page, as written, on the fixtures under
  * `src/test/resources`, and checks that every record the page shows is a record of the real
  * output. The contract follows `CallSitesLessonTests`: an example is marked with `<!-- example:
  * NAME -->`, the `bash` block after `<!-- command: NAME -->` holds the `atom reachables` line, and
  * the `json` block after `<!-- output: NAME -->` is the excerpt.
  */
class ReachabilityLessonTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val page = BFile("docs/docs/lessons/reachability.md")

  private var workspace: BFile = scala.compiletime.uninitialized

  override protected def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomReachabilityLesson")

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def resource(path: String): BFile = BFile(getClass.getResource(s"/$path").toURI)

  private def probe(command: String*): Option[String] =
      Try(scala.sys.process.Process(command).lazyLines_!.headOption.map(_.trim)).toOption.flatten

  private def needsAstgen(name: String): Option[String] =
      if probe(name, "--version").exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))
      then None
      else Some(s"$name 2.x is not installed")

  /** The examples of the page: the input they prepare, keyed by name. */
  private val examples = List(
    ("forwarded-headers", () => needsAstgen("astgen"), "forwarded-headers")
  )

  /** The lines of the first fenced block after the `<!-- kind: name -->` marker. */
  private def block(name: String, kind: String): List[String] =
    val lines  = page.lines.toList
    val marker = s"<!-- $kind: $name -->"
    val at     = lines.indexOf(marker)
    withClue(s"the page has no `$marker`: ")(at should be >= 0)
    lines.drop(at + 1).dropWhile(!_.startsWith("```")).drop(1).takeWhile(!_.startsWith("```"))

  examples.foreach { (name, needs, fixture) =>
      s"the $name example of the lesson" should {
          "write the records the page shows" in {
              needs().foreach(reason => cancel(reason))
              val input = workspace / s"ex-$name" / "input"
              input.parent.createDirectories()
              resource(fixture).copyTo(input)
              // The command may span lines with `\` continuations; join them into one line.
              val command = block(name, "command")
                  .map(_.stripSuffix("\\").trim)
                  .filter(_.nonEmpty)
                  .mkString(" ")
              withClue(s"the $name command block has no atom reachables command: ")(command
                  .startsWith("atom reachables ") shouldBe true)
              val tokens = command.split("\\s+").toList.drop(1).map(
                _.replace("/tmp/ex-", s"${workspace.pathAsString}/ex-")
              )
              val slice = tokens(tokens.indexOf("-s") + 1)
              Atom.run(tokens.toArray).isRight shouldBe true
              val written = ujson.read(BFile(slice).contentAsString)
              // The reachables slice is an array of { flows, purls } items; every record of the
              // page is one node of one item's flows. Node ids differ between runs, so the
              // excerpt shows records without them and the comparison drops them here.
              val real = written.arr.flatMap(_.obj("flows").arr).map { r =>
                r.obj.remove("id")
                r.obj.remove("columnNumber")
                r
              }.toSet
              val shown = block(name, "output").filter(_.trim.nonEmpty).map(ujson.read(_))
              shown should not be empty
              shown.foreach { record =>
                record.obj.remove("id")
                record.obj.remove("columnNumber")
                withClue(
                  s"this record of the $name example is not in the real output: $record: "
                ) {
                    // The excerpt may omit fields the real record carries; every field it
                    // shows must match one real record.
                    real.exists(r =>
                        record.obj.forall((k, v) => r.obj.get(k).contains(v))
                    ) shouldBe true
                }
              }
          }
      }
  }
end ReachabilityLessonTests
