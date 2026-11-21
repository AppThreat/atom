package io.appthreat.atom

import better.files.File
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** chen#255: the `units` section of the parse report over a real C run. The project's compilation
  * database names one file twice, with different `-D` options that select different bodies and
  * different standards, and names a second unit whose driver no table entry matches. The document
  * the run writes carries every command the database has for the twice-named file with the position
  * of the one used, reads the unknown driver as unknown, and gives the known unit its front end,
  * table entry, macro source, include search order and owned headers. The failure section keeps its
  * shape beside the units one.
  */
class ParseReportUnitsSectionTests extends AnyWordSpec with Matchers:

  private def runAtom(args: String*): Either[String, String] = Atom.run(args.toArray)

  /** The project: a unit the database names twice, one only the unknown driver compiles, and the
    * header the twice-named unit includes and so owns. The directories the commands search exist so
    * the commands could run, and hold no headers: the only header the run parses is the one beside
    * the sources.
    */
  private def writeProject(dir: File): File =
    val project = dir / "proj"
    project.createDirectories()
    Seq("inc", "api", "lib").foreach(name => (project / name).createDirectories())
    (project / "shared.h").writeText(
      """#ifndef SHARED_H
        |#define SHARED_H
        |int shared(void);
        |#endif
        |""".stripMargin
    )
    // `-DFIRST_VARIANT` and `-DSECOND_VARIANT` select the two bodies the unit can be parsed from
    (project / "chosen.c").writeText(
      """#include "shared.h"
        |#if defined(FIRST_VARIANT)
        |int chosen(void) { return 1; }
        |#else
        |int chosen(void) { return 2; }
        |#endif
        |""".stripMargin
    )
    (project / "second.c").writeText("int second(void) { return 3; }\n")
    def entry(file: File, arguments: String*): String =
        ujson.write(
          ujson.Obj(
            "directory" -> project.pathAsString,
            "file"      -> file.pathAsString,
            "arguments" -> ujson.Arr(arguments.map(ujson.Str.apply)*)
          )
        )
    // the database keeps the first of a file's commands, so `chosen.c` is parsed with
    // `-DFIRST_VARIANT -std=c99` and the report must name both commands and that one among them
    (project / "compile_commands.json").writeText(
      Seq(
        entry(
          project / "chosen.c",
          "cc",
          s"-I${(project / "inc").pathAsString}",
          s"-I${(project / "api").pathAsString}",
          "-DFIRST_VARIANT",
          "-std=c99",
          "-c",
          "chosen.c"
        ),
        entry(
          project / "chosen.c",
          "cc",
          s"-I${(project / "lib").pathAsString}",
          "-DSECOND_VARIANT",
          "-std=c11",
          "-c",
          "chosen.c"
        ),
        // no table entry matches a driver whose name carries no family suffix
        entry(
          project / "second.c",
          "podbuild",
          s"-I${(project / "inc").pathAsString}",
          "-c",
          "second.c"
        )
      ).mkString("[", ",", "]")
    )
    project
  end writeProject

  "the units section over a C run with a compilation database" should {

      "report the twice-named file's commands, the unknown driver, and the known unit's settings" in {
          File.usingTemporaryDirectory("atom-units-section") { dir =>
            val project = writeProject(dir)
            val out     = dir / "out"
            out.createDirectories()
            val report = out / "units-parse.json"
            runAtom(
              "-l",
              "c",
              "-o",
              (out / "units.atom").pathAsString,
              "--compile-commands",
              (project / "compile_commands.json").pathAsString,
              "--parse-report",
              report.pathAsString,
              project.pathAsString
            ) match
              case Left(err) => fail(s"c run failed: $err")
              case Right(_) =>
                  val document = ujson.read(report.contentAsString)
                  // the failure section keeps its shape beside the units one
                  document.obj.contains("atomVersion") shouldBe true
                  document("language").str shouldBe "c"
                  document("inputRoot").str shouldBe project.canonicalPath
                  document("total").num shouldBe 0
                  document("records").arr shouldBe empty
                  document.obj.contains("units") shouldBe true

                  val units   = document("units")
                  val records = units("records").arr
                  def recordOf(name: String): ujson.Value =
                      records.find(v => v("file").str == name)
                          .getOrElse(fail(s"the units section holds no record for $name"))

                  // the two compiled units, plus the header the run parses with its includer's flags
                  units("total").num shouldBe 3
                  units("counts")("byFrontend")("c").num shouldBe 3
                  units("counts")("byMacroSource")("probed").num shouldBe 2
                  units("counts")("byMacroSource")("unknown").num shouldBe 1

                  // the twice-named file: both commands, the one used, and the settings of that one
                  val incPath = (project / "inc").pathAsString
                  val apiPath = (project / "api").pathAsString
                  val chosen  = recordOf("chosen.c")
                  chosen("frontend").str shouldBe "c"
                  chosen("driver").str shouldBe "cc"
                  chosen("entry").str shouldBe "GCC"
                  chosen("macroSource").str shouldBe "probed"
                  chosen("language").str shouldBe "c"
                  // the standard of the command the database hands out, not of its second entry
                  chosen("standard").str shouldBe "c99"
                  chosen("macroFingerprint").str should have size 32
                  val commands = chosen("databaseCommands").arr
                  commands should have size 2
                  commands.head("directory").str shouldBe project.pathAsString
                  commands.head("arguments").arr.map(_.str).toList shouldBe List(
                    "cc",
                    s"-I$incPath",
                    s"-I$apiPath",
                    "-DFIRST_VARIANT",
                    "-std=c99",
                    "-c",
                    "chosen.c"
                  )
                  commands(1)("arguments").arr.map(_.str).toList shouldBe List(
                    "cc",
                    s"-I${(project / "lib").pathAsString}",
                    "-DSECOND_VARIANT",
                    "-std=c11",
                    "-c",
                    "chosen.c"
                  )
                  chosen("usedDatabaseCommand").num shouldBe 0
                  // the search order: the used command's directories first, in the order it lists
                  // them, and the unused command's directory nowhere
                  chosen("includeDirectories").arr.map(_.str).toList.take(2) shouldBe
                      List(incPath, apiPath)
                  (chosen("includeDirectories").arr.map(_.str).toList should not).contain(
                    (project / "lib").pathAsString
                  )
                  // the header the unit includes is the one it owns
                  chosen("ownedHeaders").arr.map(_.str).toList shouldBe List("shared.h")
                  chosen.obj.contains("edgaCommandLine") shouldBe false

                  // the driver no table entry matches
                  val second = recordOf("second.c")
                  second("driver").str shouldBe "podbuild"
                  second("entry").str shouldBe "unknown"
                  second("macroSource").str shouldBe "unknown"
                  second("language").str shouldBe "c"
                  second.obj.contains("databaseCommands") shouldBe false
                  second.obj.contains("usedDatabaseCommand") shouldBe false

                  // the header is parsed with the flags of the unit that includes it, and owns nothing
                  val header = recordOf("shared.h")
                  header("driver").str shouldBe "cc"
                  header("entry").str shouldBe "GCC"
                  header("ownedHeaders").arr shouldBe empty
            end match
          }
      }
  }
end ParseReportUnitsSectionTests
