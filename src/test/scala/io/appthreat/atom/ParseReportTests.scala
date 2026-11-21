package io.appthreat.atom

import better.files.File
import io.appthreat.x2cpg.parse.{
    DatabaseCommand,
    ParseReporter,
    ParseUnitRecord,
    UnitSettingsReporter
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** chen#255: `--parse-report` writes one JSON document naming what the frontends left out, and
  * nothing about the atom changes when it is off. The fixture is a Java project with a good twin
  * and a broken one, so the report has exactly one file to name.
  */
class ParseReportTests extends AnyWordSpec with Matchers:

  private def runAtom(args: String*): Either[String, String] = Atom.run(args.toArray)

  private def projectWithBrokenFile: File =
    val dir = File.newTemporaryDirectory("atom-parse-report")
    (dir / "Good.java").writeText(
      "public class Good {\n" +
          "  public int add(int a, int b) { return a + b; }\n" +
          "}\n"
    )
    (dir / "Broken.java").writeText("class Broken {{{\n")
    dir

  private def workspace: File = File.newTemporaryDirectory("atom-parse-report-out")

  /** One reachables run, returning the slice's bytes. */
  private def runReachables(out: File, project: File, parseReport: Option[File]): Array[Byte] =
    val slice =
        out / (if parseReport.isDefined then "pair-reachables.json" else "plain-reachables.json")
    val atom = out / (if parseReport.isDefined then "pair-r.atom" else "plain-r.atom")
    val args = List(
      "reachables",
      "-l",
      "java",
      "--cache",
      "none",
      "-o",
      atom.pathAsString,
      "-s",
      slice.pathAsString
    ) ++ parseReport.map(f => List("--parse-report", f.pathAsString)).getOrElse(Nil) ++
        List(project.pathAsString)
    runAtom(args*) match
      case Left(err) => fail(s"reachables run failed: $err")
      case Right(_)  => slice.bytes.toArray

  "the parse report" should {
      "write the report only when the option is passed, and name the lost file" in {
          val project = projectWithBrokenFile
          val out     = workspace
          val atom    = out / "quiet.atom"
          runAtom("-l", "java", "-o", atom.pathAsString, project.pathAsString).isRight shouldBe true
          val report = out / "report.json"
          report.exists shouldBe false
          ParseReporter.records shouldBe empty

          val atomWithReport = out / "reported.atom"
          val result = runAtom(
            "-l",
            "java",
            "-o",
            atomWithReport.pathAsString,
            "--parse-report",
            report.pathAsString,
            project.pathAsString
          )
          result.isRight shouldBe true
          report.exists shouldBe true

          val document = ujson.read(report.contentAsString)
          document("language").str shouldBe "java"
          document("inputRoot").str shouldBe project.canonicalPath
          document("total").num shouldBe 1
          document("counts")("byOutcome")("unparsed").num shouldBe 1
          document("counts")("byFrontend")("javasrc").num shouldBe 1
          val records = document("records").arr
          records should have size 1
          records.head("frontend").str shouldBe "javasrc"
          records.head("file").str shouldBe "Broken.java"
          records.head("outcome").str shouldBe "unparsed"
          records.head("stage").str shouldBe "parse"
          records.head("message").str should startWith("Parse error.")
          document.obj.contains("atomVersion") shouldBe true
          // the property does not leak into a later run of the same process
          System.getProperty("chen.parse.report") shouldBe null
      }

      "leave the atom and the slices the same whether or not it ran" in {
          val project = projectWithBrokenFile
          val out     = workspace
          // The atom FILE is not byte-deterministic across any two runs, with or without this
          // change: the concurrent AST passes write nodes in completion order, so two plain
          // runs differ too. The slices are byte-compared; the atom is compared by content.
          val atomFile = out / "pair.atom"
          val usgFile  = out / "pair-usages.json"

          def runUsages(parseReport: Option[File]): Unit =
            val args = List(
              "usages",
              "-l",
              "java",
              "--cache",
              "none",
              "-o",
              atomFile.pathAsString,
              "-s",
              usgFile.pathAsString
            ) ++ parseReport.map(f => List("--parse-report", f.pathAsString)).getOrElse(Nil) ++
                List(project.pathAsString)
            runAtom(args*) match
              case Left(err) => fail(s"usages run failed: $err")
              case Right(_)  =>

          runUsages(parseReport = None)
          val atomPlain   = atomFile.copyTo(out / "plain-atom.snapshot").pathAsString
          val usagesPlain = usgFile.bytes.toArray

          runUsages(parseReport = Some(out / "pair-parse.json"))
          usgFile.bytes.toArray.sameElements(usagesPlain) shouldBe true
          atomContent(atomPlain) shouldBe atomContent(atomFile.pathAsString)

          val reachPlain = runReachables(out, project, parseReport = None)
          val reachReported =
              runReachables(out, project, parseReport = Some(out / "pair-r-parse.json"))
          reachReported.sameElements(reachPlain) shouldBe true
      }

  }

  "the units section" should {

      "render the fields the frontends collect, from the records they keep" in {
          val cUnit = ParseUnitRecord(
            frontend = ParseReporter.Frontend.C,
            file = "main.c",
            driver = "cc",
            entry = "GCC",
            macroSource = UnitSettingsReporter.MacroSource.Probed,
            language = UnitSettingsReporter.Language.C,
            standard = "gnu11",
            macroFingerprint = "6f1c0d9e1c4a2b7d8e5f3a2b1c0d9e8f",
            includeDirectories = Seq("/work/proj/inc", "/usr/include"),
            forcedIncludes = Seq("pre.h"),
            ownedHeaders = Seq("local.h")
          )
          val edgUnit = ParseUnitRecord(
            frontend = ParseReporter.Frontend.Edg,
            file = "main.c",
            driver = "cc",
            entry = "GCC",
            macroSource = UnitSettingsReporter.MacroSource.Probed,
            language = UnitSettingsReporter.Language.C,
            standard = "gnu11",
            macroFingerprint = "6f1c0d9e1c4a2b7d8e5f3a2b1c0d9e8f",
            includeDirectories = Seq("/work/proj"),
            forcedIncludes = Nil,
            ownedHeaders = Nil,
            edgaCommandLine =
                Seq("/opt/edga", "--c11", "--edga-out", "/tmp/edga-1.json", "main.c")
          )
          val json = ujson.read(
            ParseReport.document("4.0.3", "c", "/work/proj", Nil, List(cUnit, edgUnit)).noSpaces
          )
          json("units")("total").num shouldBe 2
          json("units")("counts")("byFrontend")("c").num shouldBe 1
          json("units")("counts")("byFrontend")("edg").num shouldBe 1
          json("units")("counts")("byMacroSource")("probed").num shouldBe 2
          val records = json("units")("records").arr
          records should have size 2
          // the search order the frontend was given is the order the section renders
          val cRecord = records.head
          cRecord("frontend").str shouldBe "c"
          cRecord("file").str shouldBe "main.c"
          cRecord("driver").str shouldBe "cc"
          cRecord("entry").str shouldBe "GCC"
          cRecord("macroSource").str shouldBe "probed"
          cRecord("language").str shouldBe "c"
          cRecord("standard").str shouldBe "gnu11"
          cRecord("macroFingerprint").str shouldBe "6f1c0d9e1c4a2b7d8e5f3a2b1c0d9e8f"
          cRecord("includeDirectories").arr.map(_.str).toList shouldBe
              Seq("/work/proj/inc", "/usr/include")
          cRecord("forcedIncludes").arr.map(_.str).toList shouldBe Seq("pre.h")
          cRecord("ownedHeaders").arr.map(_.str).toList shouldBe Seq("local.h")
          cRecord.obj.contains("databaseCommands") shouldBe false
          cRecord.obj.contains("usedDatabaseCommand") shouldBe false
          cRecord.obj.contains("edgaCommandLine") shouldBe false
          // the exact command line the EDG export ran, as it ran
          val edgRecord = records(1)
          edgRecord("edgaCommandLine").arr.map(_.str).toList shouldBe
              Seq("/opt/edga", "--c11", "--edga-out", "/tmp/edga-1.json", "main.c")
          // the failure section keeps its shape beside the new one
          json("total").num shouldBe 0
          json("records").arr shouldBe empty
      }

      "render a driver the table does not know as unknown" in {
          val unit = ParseUnitRecord(
            frontend = ParseReporter.Frontend.C,
            file = "main.c",
            driver = "mycc",
            entry = UnitSettingsReporter.MacroSource.Unknown,
            macroSource = UnitSettingsReporter.MacroSource.Unknown,
            language = UnitSettingsReporter.Language.C,
            standard = "",
            macroFingerprint = "6f1c0d9e1c4a2b7d8e5f3a2b1c0d9e8f",
            includeDirectories = Seq("/usr/include"),
            forcedIncludes = Nil,
            ownedHeaders = Nil
          )
          val json =
              ujson.read(ParseReport.document("4.0.3", "c", "/work/proj", Nil, List(unit)).noSpaces)
          val record = json("units")("records").arr.head
          record("driver").str shouldBe "mycc"
          record("entry").str shouldBe "unknown"
          record("macroSource").str shouldBe "unknown"
          json("units")("counts")("byMacroSource")("unknown").num shouldBe 1
      }

      "render every command of a file the database names twice, and the one used" in {
          val unit = ParseUnitRecord(
            frontend = ParseReporter.Frontend.C,
            file = "dup.c",
            driver = "cc",
            entry = "GCC",
            macroSource = UnitSettingsReporter.MacroSource.Probed,
            language = UnitSettingsReporter.Language.C,
            standard = "",
            macroFingerprint = "6f1c0d9e1c4a2b7d8e5f3a2b1c0d9e8f",
            includeDirectories = Nil,
            forcedIncludes = Nil,
            ownedHeaders = Nil,
            databaseCommands = Seq(
              DatabaseCommand("/work/proj", Seq("cc", "-DFIRST=1", "-c", "dup.c")),
              DatabaseCommand("/work/proj", Seq("cc", "-DSECOND=1", "-Ilib", "-c", "dup.c"))
            ),
            usedDatabaseCommand = Some(0)
          )
          val json =
              ujson.read(ParseReport.document("4.0.3", "c", "/work/proj", Nil, List(unit)).noSpaces)
          val record   = json("units")("records").arr.head
          val commands = record("databaseCommands").arr
          commands should have size 2
          commands.head("directory").str shouldBe "/work/proj"
          commands.head("arguments").arr.map(_.str).toList shouldBe
              Seq("cc", "-DFIRST=1", "-c", "dup.c")
          commands(1)("arguments").arr.map(_.str).toList shouldBe
              Seq("cc", "-DSECOND=1", "-Ilib", "-c", "dup.c")
          record("usedDatabaseCommand").num shouldBe 0
      }

      "carry no units section when nothing was collected" in {
          val failure = io.appthreat.x2cpg.parse.ParseFailureRecord(
            "javasrc",
            "Broken.java",
            "unparsed",
            "parse",
            "Parse error."
          )
          val json = ujson.read(
            ParseReport.document("4.0.3", "java", "/work/proj", List(failure), Nil).noSpaces
          )
          json.obj.contains("units") shouldBe false
          // the failure section is what such a report holds
          json("total").num shouldBe 1
          json("records").arr should have size 1
      }

      "collect and render the units of a C run, and nothing without the flag" in {
          val project = projectWithCUnit
          val out     = workspace
          val report  = out / "c-parse.json"
          runAtom(
            "-l",
            "c",
            "-o",
            (out / "c.atom").pathAsString,
            "--parse-report",
            report.pathAsString,
            project.pathAsString
          ) match
            case Left(err) => fail(s"c run failed: $err")
            case Right(_) =>
                val json = ujson.read(report.contentAsString)
                json.obj.contains("units") shouldBe true
                json("units")("total").num shouldBe 1
                val record = json("units")("records").arr.head
                record("frontend").str shouldBe "c"
                record("file").str shouldBe "main.c"
                record("language").str shouldBe "c"
                record("driver").str shouldBe ""
                record("entry").str shouldBe ""
                // the host compiler answers for a project no compilation database names, and the
                // record says so
                record("macroSource").str shouldBe UnitSettingsReporter.MacroSource.Host
                record("standard").str shouldBe ""
                record("includeDirectories").arr should not be empty
                record("forcedIncludes").arr shouldBe empty
                record("ownedHeaders").arr shouldBe empty
                record.obj.contains("databaseCommands") shouldBe false
                record.obj.contains("edgaCommandLine") shouldBe false
                json("units")("counts")("byFrontend")("c").num shouldBe 1
                json("units")("counts")("byMacroSource")("host").num shouldBe 1
                // the report is written: the collectors start empty for the next run
                UnitSettingsReporter.records shouldBe empty
                ParseReporter.records shouldBe empty
          end match

          val quiet = out / "c-quiet.json"
          runAtom(
            "-l",
            "c",
            "-o",
            (out / "c-plain.atom").pathAsString,
            project.pathAsString
          ).isRight shouldBe true
          quiet.exists shouldBe false
          UnitSettingsReporter.records shouldBe empty
      }
  }

  private def projectWithCUnit: File =
    val dir = File.newTemporaryDirectory("atom-parse-report-c")
    (dir / "main.c").writeText("int main(void) {\n  return 0;\n}\n")
    dir

  /** Loads an atom file and digests its content: node and edge counts by label and the sorted names
    * that say what the graph holds. Independent of the node order the serializer wrote.
    */
  private def atomContent(path: String): String =
    import scala.jdk.CollectionConverters.*
    val odbConfig = overflowdb.Config.withDefaults().withStorageLocation(path)
        .withHeapPercentageThreshold(90)
    val config =
        io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    val cpg = io.shiftleft.codepropertygraph.cpgloading.CpgLoader.loadFromOverflowDb(config)
    try
      import io.shiftleft.semanticcpg.language.*
      val nodes = cpg.graph.nodes.asScala.groupBy(_.label()).view.mapValues(_.size).toList.sorted
      val edges = cpg.graph.edges.asScala.groupBy(_.label()).view.mapValues(_.size).toList.sorted
      val names = cpg.method.fullName.l.sorted
      val files = cpg.file.name.l.sorted
      val types = cpg.typeDecl.fullName.l.sorted
      s"nodes=$nodes edges=$edges methods=$names files=$files types=$types"
    finally cpg.close()
end ParseReportTests
