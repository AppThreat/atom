package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** End-to-end JS/TS workflow for the usage slice of external method stubs (AppThreat/atom#240).
  *
  * Every call to an external method stub (`debug:createDebug`, `@angular/core:signal`, ...) used to
  * collapse into ONE usage entry, placed in an arbitrary caller's slice with a position taken from
  * a different, arbitrary `callIn` element - often in another file or past its end. The slice now
  * carries one entry per call site, in the slice of the method containing the call, at the call's
  * own position, and emits nothing for operator and `require` stubs.
  *
  * Runs atom's CLI (`Atom.run`) over four fixtures from resources/js-usages and parses the JSON
  * output. The JavaScript frontend needs the external `astgen` generator, so the suite cancels (not
  * fails) without one, exactly like [[ReachablesCrossLanguageWorkflowTests]].
  */
class JsUsagesExternalCallSiteTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val fixtureCases = Seq(
    "issue240-cross-file",
    "issue240-local-cross-file",
    "issue240-same-file",
    "multiplicity"
  )

  private var workspace: BFile                    = scala.compiletime.uninitialized
  private var sliceJson: Map[String, ujson.Value] = Map.empty

  override def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomJsUsages240")
    if astgenUsable then
      sliceJson = fixtureCases.map(caseName => caseName -> runUsages(caseName)).toMap

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def runUsages(caseName: String): ujson.Value =
    val fixture    = BFile(getClass.getResource(s"/js-usages/$caseName").toURI)
    val projectDir = workspace / caseName
    copyFixture(fixture, projectDir)
    val atomFile  = workspace / s"$caseName.atom"
    val sliceFile = workspace / s"$caseName-usages.json"
    val result = Atom.run(
      Seq(
        "usages",
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
    withClue(s"atom usages failed for $caseName: "):
      result.isRight shouldBe true
    ujson.read(sliceFile.contentAsString)
  end runUsages

  private def copyFixture(source: BFile, target: BFile): Unit =
      if source.isDirectory then
        target.createDirectories()
        source.children.foreach(child => copyFixture(child, target / child.name))
      else
        target.parent.createDirectories()
        source.copyTo(target, overwrite = true)

  /** One flat view over everything a usage entry records, for the position and naming assertions.
    */
  private final case class UsageEntry(
    sliceFullName: String,
    sliceFile: String,
    field: String,
    name: String,
    resolvedMethod: String,
    label: String,
    isExternal: Option[Boolean],
    line: Option[Int],
    column: Option[Int]
  )

  private def entriesOf(caseName: String): Seq[UsageEntry] =
      for
        os    <- sliceJson(caseName)("objectSlices").arr.toSeq
        usage <- os("usages").arr.toSeq
        component <- (Seq("targetObj", "definedBy")
            .flatMap(f => usage.obj.get(f).map(f -> _)) ++
            usage.obj.get("invokedCalls").map(_.arr.toSeq.map("invokedCalls" -> _)).getOrElse(
              Nil
            ) ++
            usage.obj.get("argToCalls").map(_.arr.toSeq.map("argToCalls" -> _)).getOrElse(Nil))
      yield
        val c = component._2
        UsageEntry(
          os("fullName").str,
          os("fileName").str,
          component._1,
          c.obj.get("name").map(_.str).orElse(c.obj.get("callName").map(_.str)).getOrElse(""),
          c.obj.get("resolvedMethod").filterNot(_.isNull).map(_.str).getOrElse(""),
          c.obj.get("label").map(_.str).getOrElse(""),
          c.obj.get("isExternal").filterNot(_.isNull).map(_.bool),
          c.obj.get("lineNumber").filterNot(_.isNull).map(_.num.toInt),
          c.obj.get("columnNumber").filterNot(_.isNull).map(_.num.toInt)
        )

  /** The per-call-site entries of one external stub: the `targetObj` of each emitted usage. */
  private def callSitesOf(caseName: String, resolvedMethod: String): Seq[UsageEntry] =
      entriesOf(caseName)
          .filter(e => e.field == "targetObj" && e.resolvedMethod == resolvedMethod)

  private def withSliceFile(entry: UsageEntry): Option[(String, Int, String)] =
      entry.line.map(l => (entry.sliceFile, l, entry.sliceFullName))

  "usage slices for external method stubs" should {

      "emit one createDebug usage per call site, each in the slice of its enclosing method" in {
          requireAstgen()
          val callSites = callSitesOf("multiplicity", "debug:createDebug")
          withClue(s"expected 9 createDebug call sites, got ${callSites.map(withSliceFile)}: "):
            callSites should have size 9

          // multiplicity has 9 createDebug(...) calls across 2 files; the enclosing methods are the
          // top-level script, one(), two(), the arrow function bound to `three`, K.m, K.s, b1 and b2.
          callSites.flatMap(withSliceFile).sortBy(t => (t._1, t._2)) shouldBe Seq(
            ("src/a.js", 2, "src/a.js::program"),
            ("src/a.js", 3, "src/a.js::program"),
            ("src/a.js", 7, "src/a.js::program:one"),
            ("src/a.js", 11, "src/a.js::program:two"),
            ("src/a.js", 13, "src/a.js::program:three"),
            ("src/a.js", 16, "src/a.js::program:K:m"),
            ("src/a.js", 19, "src/a.js::program:K:s"),
            ("src/b.js", 3, "src/b.js::program:b1"),
            ("src/b.js", 6, "src/b.js::program:b2")
          )

          callSites.foreach { e =>
              withClue(s"entry at ${e.sliceFile}:${e.line}: "):
                e.name shouldBe "createDebug"
                e.label shouldBe "CALL"
                e.isExternal shouldBe Some(true)
          }
      }

      "attribute each signal call in two files to its own file and method" in {
          requireAstgen()
          val callSites = callSitesOf("issue240-cross-file", "@angular/core:signal")
          callSites.flatMap(withSliceFile).sortBy(t => (t._1, t._2)) shouldBe Seq(
            ("src/first.ts", 4, "src/first.ts::program:first"),
            ("src/second.ts", 6, "src/second.ts::program:second")
          )
          // The old slice placed second.ts's call (line 6) in first.ts's slice - past first.ts's end.
          entriesOf("issue240-cross-file")
              .filter(e => e.sliceFile == "src/first.ts" && e.line.contains(6)) shouldBe empty
      }

      "attribute same-file signal and computed calls to the function they are called in" in {
          requireAstgen()
          callSitesOf("issue240-same-file", "@angular/core:signal")
              .flatMap(withSliceFile).sortBy(_._2) shouldBe Seq(
            ("src/funcs.ts", 4, "src/funcs.ts::program:a"),
            ("src/funcs.ts", 8, "src/funcs.ts::program:b"),
            ("src/funcs.ts", 13, "src/funcs.ts::program:c")
          )
          callSitesOf("issue240-same-file", "@angular/core:computed")
              .flatMap(withSliceFile) shouldBe Seq(("src/funcs.ts", 9, "src/funcs.ts::program:b"))
      }

      "keep a project method's position at its own definition" in {
          requireAstgen()
          // `make` is called twice from use.ts; its slice must carry make.ts:1, not a caller's line.
          val make = entriesOf("issue240-local-cross-file").filter(e =>
              e.field == "targetObj" && e.resolvedMethod == "src/make.ts::program:make"
          )
          make.flatMap(withSliceFile) shouldBe Seq(("src/make.ts", 1, "src/make.ts::program:make"))
      }

      "never place a usage past the end of its slice's file" in {
          requireAstgen()
          for caseName <- fixtureCases
          do
            for e <- entriesOf(caseName)
            do
              val source = workspace / caseName / e.sliceFile
              if source.exists then
                val lineCount = source.lines.size
                withClue(
                  s"$caseName: ${e.field} '${e.name}' of slice ${e.sliceFullName} " +
                      s"(${e.sliceFile}, ${lineCount} lines): "
                ):
                  e.line.foreach(_ should be <= lineCount)
      }

      "emit no entries for operator or require stubs" in {
          requireAstgen()
          for caseName <- fixtureCases
          do
            for e <- entriesOf(caseName)
            do
              withClue(s"$caseName: ${e.field} of slice ${e.sliceFullName}: "):
                (e.name should not).startWith("<operator")
                (e.resolvedMethod should not).startWith("<operator")
                e.name should not be "require"
                (e.resolvedMethod should not).endWith(":require")
      }
  }

  // ------------------------------------------------------------------ generator gate

  /** astgen binary the way jssrc2cpg's `AstGenRunner` resolves it: plain `astgen` from `PATH`. */
  private lazy val astgenUsable: Boolean =
      Try(scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  private def requireAstgen(): Unit =
      if !astgenUsable then
        cancel(
          "astgen unavailable or too old; the JavaScript usages fixtures need the astgen from " +
              "@appthreat/atom-parsetools on PATH"
        )
end JsUsagesExternalCallSiteTests
