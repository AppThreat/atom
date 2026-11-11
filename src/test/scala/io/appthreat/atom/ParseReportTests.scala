package io.appthreat.atom

import better.files.File
import io.appthreat.x2cpg.parse.ParseReporter
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
