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
    "multiplicity",
    "binding-uses",
    "decorators"
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

  /** The calls recorded in the slice of an import binding (a LOCAL defined by `require`), as
    * (field, line, position) with position -1 marking the callee.
    */
  private def bindingCalls(caseName: String, binding: String): Seq[(String, Int, Int)] =
      for
        os    <- sliceJson(caseName)("objectSlices").arr.toSeq
        usage <- os("usages").arr.toSeq
        if usage.obj.get("targetObj").exists(t =>
            t("label").str == "LOCAL" && t("name").str == binding
        )
        if usage.obj.get("definedBy").exists(d => d.obj.get("name").exists(_.str == "require"))
        field <- Seq("invokedCalls", "argToCalls")
        call  <- usage.obj.get(field).map(_.arr.toSeq).getOrElse(Nil)
        line  <- call.obj.get("lineNumber").filterNot(_.isNull).map(_.num.toInt)
      yield (
        field,
        line,
        call.obj.get("position").flatMap(p => Try(p.num.toInt).toOption).getOrElse(Int.MinValue)
      )

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
                // an import binding's definedBy is its require call; no other entry may be one
                if e.field != "definedBy" then
                  e.name should not be "require"
                  (e.resolvedMethod should not).endWith(":require")
      }
  }

  "usage slices of import bindings" should {

      "record a call through a pure alias as a callee use of the original binding" in {
          requireAstgen()
          bindingCalls("binding-uses", "flatten") should contain(("argToCalls", 9, -1))
      }

      "record calls on member chains rooted at a binding" in {
          requireAstgen()
          // process.stdout.write("x"): the binding roots the callee chain
          bindingCalls("binding-uses", "process") should contain(("argToCalls", 12, -1))
          // path.join("a", path.sep): path.sep is passed as argument 2
          bindingCalls("binding-uses", "path").map(_._2) should contain(13)
      }

      "name the module and symbol of a resolved call and of the import itself" in {
          requireAstgen()
          val js = sliceJson("binding-uses")
          val readFile =
              for
                os    <- js("objectSlices").arr.toSeq
                usage <- os("usages").arr.toSeq
                if usage.obj.get("targetObj").exists(_("name").str == "readFileSync")
              yield usage
          readFile should not be empty
          val definedBy = readFile.head("definedBy")
          definedBy("module").str shouldBe "node:fs"
          definedBy("symbol").str shouldBe "readFileSync"
          definedBy.obj.get("code").map(_.str) shouldBe Some("require(\"node:fs\").readFileSync")
          val calls = readFile.flatMap(u => u("argToCalls").arr.toSeq)
          calls.map(c => (c("resolvedMethod").str, c("module").str, c("symbol").str)).distinct shouldBe
              Seq(("node:fs:readFileSync", "node:fs", "readFileSync"))
      }

      "record a callee use inside a nested arrow function" in {
          requireAstgen()
          bindingCalls("binding-uses", "readFileSync") should contain(("argToCalls", 17, -1))
      }

      "not attribute a shadowing local's calls to the import" in {
          requireAstgen()
          val lines = bindingCalls("binding-uses", "debugLib").map(_._2)
          lines should contain(25)
          lines should not contain 23
      }
  }

  "usage slices of decorators and heritage clauses" should {

      def declEntries(label: String): Seq[(String, Int, String, String)] =
          for
            os    <- sliceJson("decorators")("objectSlices").arr.toSeq
            usage <- os("usages").arr.toSeq
            t     <- usage.obj.get("targetObj").toSeq
            if t("label").str == label
          yield (
            t("name").str,
            t("lineNumber").num.toInt,
            t.obj.get("module").map(_.str).getOrElse(""),
            t.obj.get("resolvedMethod").filterNot(_.isNull).map(_.str).getOrElse("")
          )

      "record every decorator at its own line with its module, including unused parameters" in {
          requireAstgen()
          val decorators = declEntries("ANNOTATION").distinct.sortBy(_._2)
          decorators shouldBe Seq(
            ("Entity", 5, "typeorm", "typeorm:Entity"),
            ("Column", 7, "typeorm", "typeorm:Column"),
            ("Controller", 11, "@nestjs/common", "@nestjs/common:Controller"),
            ("Get", 13, "@nestjs/common", "@nestjs/common:Get"),
            ("Param", 14, "@nestjs/common", "@nestjs/common:Param"),
            ("Injectable", 19, "@nestjs/common", "@nestjs/common:Injectable")
          )
      }

      "record an extends clause at its own line, not the decorator's" in {
          requireAstgen()
          declEntries("EXTENDS") shouldBe Seq(("EventEmitter", 21, "events", "events:EventEmitter"))
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
