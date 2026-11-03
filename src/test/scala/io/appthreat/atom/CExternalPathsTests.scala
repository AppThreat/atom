package io.appthreat.atom

import better.files.File as BFile
import io.circe.generic.auto.*
import io.circe.parser.decode
import org.scalatest.{AppendedClues, BeforeAndAfterAll}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `--external-paths` for C: a call into a vendored library under the input is no record while the
  * library is project code, and names the vendored path as its module once the option says the path
  * is external. The other languages are in [[VendoredCodeTests]]; a pattern that names nothing of a
  * project leaves its slice byte-identical, which the JavaScript case here pins.
  */
class CExternalPathsTests extends AnyWordSpec with Matchers with AppendedClues
    with BeforeAndAfterAll:

  import CExternalPathsTests.*

  private var workspace: BFile = scala.compiletime.uninitialized

  override protected def beforeAll(): Unit = workspace = BFile.newTemporaryDirectory("atomExtPaths")

  override protected def afterAll(): Unit = workspace.delete(swallowIOExceptions = true)

  private def runC(extra: String*): List[Record] =
    val project = workspace / "proj"
    if !project.exists then
      (BFile(getClass.getResource("/c-callsites-vendored/proj").toURI)).copyTo(project)
    val slice = workspace / "c-callsites.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: "c" :: extra.toList :::
          "-o" :: (workspace / "c.atom").pathAsString :: "-s" :: slice.pathAsString :: List(
            project.pathAsString
          )).toArray
    )
    (result.isRight shouldBe true).withClue(s"the C run failed: $result")
    decode[Slice](slice.contentAsString).fold(
      e => fail(s"the C slice is not JSON: $e"),
      s => s.callsites
    )

  "a call into vendored code under the input" should {
      "produce no record while the vendored library is project code" in {
          runC() shouldBe empty
      }
      "name the vendored path as its module once the option marks it external" in {
          runC("--external-paths", "third_party/") shouldBe List(
            Record("main.c", 2, "third_party", "zlib_compress", "call", "binding", builtin = false)
          )
      }
  }

  "a pattern that names nothing of a JavaScript project" should {
      "leave the slice byte-identical" in {
          val project = workspace / "jsproj"
          if !project.exists then
            project.createDirectories()
            (project / "index.js").write(
              "const fs = require('fs');\nfs.readFileSync('x');\n"
            )
          def slice(extra: String*): String =
            val out  = workspace / "js.atom"
            val file = workspace / "js-callsites.json"
            val result = Atom.run(
              ("callsites" :: "--cache" :: "none" :: "-l" :: "javascript" :: extra.toList :::
                  "-o" :: out.pathAsString :: "-s" :: file.pathAsString :: List(
                    project.pathAsString
                  )).toArray
            )
            (result.isRight shouldBe true).withClue(s"the JavaScript run failed: $result")
            file.contentAsString
          slice() shouldBe slice("--external-paths", "vendor/")
      }
  }
end CExternalPathsTests

object CExternalPathsTests:
  final case class Record(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean
  )
  final case class Slice(callsites: List[Record])
