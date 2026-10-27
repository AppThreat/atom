package io.appthreat.atom.slicing

import better.files.File as BFile
import io.shiftleft.codepropertygraph.Cpg
import io.appthreat.atom.Atom
import io.appthreat.dataflowengineoss.language.Path
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** `collectDynamicLanguageFlows` shares its source and sink sets between queries. When they were
  * single-use traversals, `flow1` drained them and the f2 fallback's first query (f2q1) ran on
  * nothing. The fixture has no framework parameters, so the f2 gate is open and f2q1 must find the
  * DOM route itself. The collector is called directly because `collectBasicFlows` reports the same
  * route, which hides the bug in the command's output.
  */
class ReachablesIteratorReuseTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomReachablesIterReuse")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private lazy val astgenUsable: Boolean =
      Try(scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  /** Builds the fixture project, runs `reachables` over it (frontend, passes, taggers, engine) and
    * returns the CPG the command wrote, for direct collector queries.
    */
  private def buildFixtureAtom(): Cpg =
    val projectDir = workspace / "js-dom-project"
    (projectDir / "src").createDirectories()
    (projectDir / "src" / "main.js").write(
      """const panel = document.getElementById("panel");
        |const fragment = decodeURIComponent(location.hash.slice(1));
        |if (panel != null) {
        |    panel.innerHTML = fragment;
        |}
        |""".stripMargin
    )
    val atomFile  = workspace / "js-dom-project.atom"
    val sliceFile = workspace / "js-dom-project-slices.json"
    val result = Atom.run(
      Seq(
        "reachables",
        "--cache",
        "none",
        "-l",
        "javascript",
        "-o",
        atomFile.pathAsString,
        "-s",
        sliceFile.pathAsString,
        projectDir.pathAsString
      ).toArray
    )
    result.isRight shouldBe true
    val odb = overflowdb.Config.withDefaults().withStorageLocation(atomFile.pathAsString)
    CpgLoader.loadFromOverflowDb(CpgLoaderConfig().withOverflowConfig(odb).doNotCreateIndexesOnLoad)
  end buildFixtureAtom

  "collectDynamicLanguageFlows" should {
      "give the f2 fallback the source and sink sets flow1 also uses" in {
          if !astgenUsable then
            cancel("astgen 'astgen' unavailable or too old; the JS fixture needs it on PATH")
          val cpg = buildFixtureAtom()
          try
            val config = ReachablesConfig(
              sourceTag = Atom.DEFAULT_SOURCE_TAGS,
              sinkTag = Atom.DEFAULT_SINK_TAGS,
              sliceDepth = 20,
              includeCryptoFlows = false
            )
            // Slots: dynCallInFlows, flow1, f1, f2q1, f2q2.
            val slots = ReachableSlicing
                .collectDynamicLanguageFlows(
                  cpg,
                  config,
                  Languages.JSSRC,
                  defaultTagsMode = true,
                  sourceTagRegex = raw"""(${Atom.DEFAULT_SOURCE_TAGS.mkString("|")})""",
                  sinkTagRegex = raw"""(${Atom.DEFAULT_SINK_TAGS.mkString("|")})"""
                )
                .map(_.toVector)
                .toVector
            def routes(paths: Vector[Path]) =
                paths.map(p => s"${p.elements.head.code}->${p.elements.last.code}").distinct
            slots.size should be(5)
            routes(slots(1)) should be(Seq("location->fragment"))
            routes(slots(3)) should be(Seq("location->fragment"))
          finally cpg.close()
          end try
      }
  }
end ReachablesIteratorReuseTests
