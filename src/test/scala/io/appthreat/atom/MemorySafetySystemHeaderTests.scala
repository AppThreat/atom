package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.x2cpg.passes.taggers.{
    AllocationStatePass,
    ExtentPass,
    GuardPass,
    IntegerWidthPass,
    MemoryApiPass,
    MemorySafetyFindingPass,
    ValueOriginPass
}
import io.circe.Json
import io.circe.parser.parse
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The command end to end over a project whose compilation database passes an include directory
  * outside the input and one inside it: a header of the input stays project code and prints a path
  * relative to the input, in JSON and in SARIF.
  */
class MemorySafetySystemHeaderTests extends AnyWordSpec with Matchers:

  // reads past the table, whichever file it is written in
  private def defect(name: String) =
      s"""int $name(int i)
       |{
       |    int table[8];
       |    return table[i];
       |}
       |""".stripMargin

  private def json(strings: String*): Json = Json.arr(strings.map(Json.fromString)*)

  /** Run `memory-safety` with `extra` options and give the findings file's text and the project
    * directory.
    */
  private def run(extra: String*)(check: (File, String) => Unit): Unit =
      File.usingTemporaryDirectory("atom-system-header") { dir =>
        val src = (dir / "src").createDirectory()
        (src / "inc").createDirectory()
        (src / "main.c").writeText("#include <local.h>\n" + defect("project_read"))
        (src / "inc" / "local.h").writeText("static inline " + defect("local_read"))
        val sdk      = (dir / "sdk").createDirectory()
        val database = dir / "compile_commands.json"
        def entry(file: File, flags: String*) = Json.obj(
          "directory" -> Json.fromString(dir.pathAsString),
          "file"      -> Json.fromString(file.pathAsString),
          "arguments" -> json((Seq("cc") ++ flags ++ Seq("-c", file.pathAsString))*)
        )
        database.writeText(
          Json.arr(
            entry(src / "main.c", "-isystem", sdk.pathAsString, "-I", (src / "inc").pathAsString)
          ).noSpaces
        )
        val out = dir / "findings.out"
        Atom.run((Seq(
          "memory-safety",
          "-l",
          "c",
          "--cache",
          "none",
          "--compile-commands",
          database.pathAsString,
          "-o",
          (dir / "app.atom").pathAsString,
          "-s",
          out.pathAsString
        ) ++ extra :+ src.pathAsString).toArray).isRight shouldBe true
        check(dir, out.contentAsString)
      }

  private def findings(text: String): List[Json] =
      parse(text).toOption.flatMap(_.asArray).toList.flatten

  private def fileOf(f: Json): String = f.hcursor.get[String]("file").toOption.get

  private def isAbsolute(path: String): Boolean = java.nio.file.Paths.get(path).isAbsolute

  "memory-safety over a project with an include directory outside the input" should {
      "report project code by a path relative to the input, a header of the input included" in {
          run() { (_, text) =>
            val files = findings(text).map(fileOf)
            files should contain("src/main.c")
            // a header of the input stays project code even though an include path covers it
            files should contain("src/inc/local.h")
            files.foreach(f => isAbsolute(f) shouldBe false)
          }
      }

      "write SARIF paths under %SRCROOT% without repeating the input's name" in {
          run("--format", "sarif") { (_, text) =>
            val results = parse(text).toOption.get.hcursor.downField("runs")
                .downArray.downField("results").as[List[Json]].toOption.get
            val artifacts = results.map(_.hcursor.downField("locations").downArray
                .downField("physicalLocation").downField("artifactLocation"))
            val uris = artifacts.flatMap(_.get[String]("uri").toOption)
            // relative to %SRCROOT%, which is `src`: the directory's own name is not repeated
            uris should contain("main.c")
            uris should contain("inc/local.h")
            uris.foreach(u => (u should not).startWith("src/"))
          }
      }
  }
end MemorySafetySystemHeaderTests

/** The rendering and the filter over a graph in which one file is named as a frontend names a file
  * outside the input: by its absolute path.
  */
class MemorySafetyOutsideFileTests extends DataFlowCodeToCpgSuite:

  private val OutsideName =
      if scala.util.Properties.isWin then "C:/Program Files/sdk/include/sdk.h"
      else "/Applications/Xcode.app/MacOSX.sdk/usr/include/sdk.h"

  private val defect =
      """int read_at(int i)
      |{
      |    int table[8];
      |    return table[i];
      |}
      |""".stripMargin

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(defect.replace("read_at", "project_read"), "project.c")
      .moreCode(defect.replace("read_at", "sdk_read"), "sdk.c")

  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new IntegerWidthPass(cpg).createAndApply()
  new AllocationStatePass(cpg).createAndApply()
  new MemorySafetyFindingPass(cpg).createAndApply()
  Atom.storeMemorySafetyOverlayMarker(cpg)

  // the frontend names a file outside the input by its absolute path
  private lazy val renamed: Unit =
    cpg.file.nameExact("sdk.c").foreach(f => f.setProperty("NAME", OutsideName))
    cpg.method.nameExact("sdk_read").foreach(m => m.setProperty("FILENAME", OutsideName))

  private def run(config: AtomMemorySafetyConfig): List[Json] =
    renamed
    val out = File.newTemporaryFile("memory-safety", ".json")
    out.deleteOnExit()
    config.outputSliceFile = out
    MemorySafetyCommands.runMemorySafety(cpg, config, out).isRight shouldBe true
    parse(out.contentAsString).toOption.flatMap(_.asArray).toList.flatten

  private def files(findings: List[Json]): List[String] =
      findings.flatMap(_.hcursor.get[String]("file").toOption)

  "memory-safety over a graph with a file outside the input" should {
      "drop the finding located there by default, and keep the project's" in {
          val shown = files(run(AtomMemorySafetyConfig()))
          shown.exists(_.endsWith("project.c")) shouldBe true
          shown.exists(_.contains("sdk")) shouldBe false
      }

      "keep it under --include-system-headers, as the absolute path it is" in {
          val shown = files(run(AtomMemorySafetyConfig().withIncludeSystemHeaders(true)))
          shown should contain(OutsideName)
          shown.foreach(f => (f should not).include(s"//"))
          shown.filter(_.endsWith("project.c")).foreach(f =>
              java.nio.file.Paths.get(f).isAbsolute shouldBe false
          )
      }

      "name the outside file the same way in the flow of a kept finding" in {
          val kept = run(AtomMemorySafetyConfig().withIncludeSystemHeaders(true))
              .filter(_.hcursor.get[String]("file").toOption.contains(OutsideName))
          kept should not be empty
          val flowFiles = kept.flatMap(
            _.hcursor.downField("flow").as[List[Json]].toOption.toList.flatten
          ).flatMap(_.hcursor.get[String]("file").toOption)
          flowFiles should not be empty
          flowFiles.foreach(_ shouldBe OutsideName)
      }

      "give SARIF an absolute URI for the outside file and a source-root path for the rest" in {
          val findings = run(AtomMemorySafetyConfig().withIncludeSystemHeaders(true))
          val sarif    = MemorySafetySarif.document(findings, "/work/project/src", None)
          val uris = sarif.hcursor.downField("runs").downArray.downField("results")
              .as[List[Json]].toOption.get.flatMap(
                _.hcursor.downField("locations").downArray.downField("physicalLocation")
                    .downField("artifactLocation").get[String]("uri").toOption
              )
          uris.exists(u => u.startsWith("file:") && u.endsWith("/sdk.h")) shouldBe true
          uris.exists(_.endsWith("project.c")) shouldBe true
      }
  }
end MemorySafetyOutsideFileTests
