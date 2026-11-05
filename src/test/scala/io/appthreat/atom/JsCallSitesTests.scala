package io.appthreat.atom

import io.shiftleft.semanticcpg.language.*

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** `atom callsites` over fixtures from resources/js-usages: one record per external use site of an
  * imported module, asserted on (file, line, module, kind) per case.
  *
  * The fixtures cover each resolution path: `via=binding` (call, new, arg, ref — including `new` on
  * a destructured dynamic import), `via=name` (tagged template, heritage clause, JSX tag,
  * decorator), and the imports that bind nothing, which are uses of their own (decision 0002, Q1:
  * `import 'y'`, a bare `require('y')` statement, a re-export). The JavaScript frontend needs the
  * external `astgen` generator, so the suite cancels (not fails) without one, exactly like
  * [[JsUsagesExternalCallSiteTests]].
  */
class JsCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val fixtureCases =
      Seq(
        "callsites",
        "decorators",
        "callsites-fields",
        "orphans",
        "sfc",
        "accessors",
        "sfc-positions",
        "purl-bom",
        "side-effect-imports"
      )

  private var workspace: BFile                    = scala.compiletime.uninitialized
  private var slices: Map[String, List[CallSite]] = Map.empty

  /** The fields the assertions need, one per record. */
  private final case class CallSite(
    file: String,
    line: Int,
    col: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    purl: String = ""
  )

  override def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomJsCallsites")
    slices = fixtureCases
        .filter(caseName =>
            caseName match
              case "sfc-positions" => sfcPositionAstgenUsable
              case "accessors"     => autoAccessorAstgenUsable
              case _               => astgenUsable
        )
        .map(caseName => caseName -> runCallsites(caseName)).toMap

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def runCallsites(caseName: String): List[CallSite] =
    val fixture    = BFile(getClass.getResource(s"/js-usages/$caseName").toURI)
    val projectDir = workspace / caseName
    copyFixture(fixture, projectDir)
    val atomFile  = workspace / s"$caseName.atom"
    val sliceFile = workspace / s"$caseName-callsites.json"
    val result = Atom.run(
      (Seq(
        "callsites",
        "--cache",
        "none",
        "-l",
        "javascript",
        "-o",
        atomFile.pathAsString,
        "-s",
        sliceFile.pathAsString
      ) ++ bomArgs(caseName, projectDir) :+ projectDir.pathAsString).toArray
    )
    withClue(s"atom callsites failed for $caseName: "):
      result.isRight shouldBe true
    ujson.read(sliceFile.contentAsString).obj("callsites").arr
        .map(r =>
            CallSite(
              r("file").str,
              r("line").num.toInt,
              r("col").num.toInt,
              r("module").str,
              r("path").str,
              r("kind").str,
              r("via").str,
              r.obj.get("purl").map(_.str).getOrElse("")
            )
        ).toList
  end runCallsites

  /** The purl fixture hands its SBOM to atom; the other fixtures have none. */
  private def bomArgs(caseName: String, projectDir: BFile): Seq[String] =
      if caseName == "purl-bom" then Seq("--bom-file", (projectDir / "bom.json").pathAsString)
      else Nil

  private def copyFixture(source: BFile, target: BFile): Unit =
      if source.isDirectory then
        target.createDirectories()
        source.children.foreach(child => copyFixture(child, target / child.name))
      else
        target.parent.createDirectories()
        source.copyTo(target, overwrite = true)

  private lazy val astgenUsable: Boolean =
      Try(scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  /** The astgen command the JavaScript frontend runs, resolved the way chen's AstGenRunner resolves
    * it, so a probe sees the astgen the fixture will actually use.
    */
  private lazy val astgenCommand: Seq[String] =
      sys.props
          .get("jssrc2cpg.test.astgenCmd")
          .map(_.trim)
          .filter(_.nonEmpty)
          .orElse(sys.env.get("JSSRC2CPG_TEST_ASTGEN_CMD").map(_.trim).filter(_.nonEmpty))
          .getOrElse("astgen")
          .split("\\s+")
          .toSeq

  /** Runs astgen over one small file and returns its emitted AST, or None when astgen cannot run or
    * the file produced no AST. Used for behaviour probes: `astgen --version` cannot tell the astgen
    * releases apart here, because the version it prints was not bumped between the npm release with
    * the Vue position defect (1.10.6) and the release/1.x branch that fixed it.
    */
  private def probeAstgenAst(
    fileName: String,
    content: String,
    fileType: String
  ): Option[ujson.Value] =
      Try {
          val dir = BFile.newTemporaryDirectory("atomJsAstgenProbe")
          try
            val out    = dir / "ast_out"
            val source = dir / "src" / fileName
            source.parent.createDirectories()
            source.write(content)
            val exit = scala.sys.process
                .Process(astgenCommand ++ Seq(
                  "-i",
                  dir.pathAsString,
                  "-t",
                  fileType,
                  "-o",
                  out.pathAsString
                ))
                .!
            if exit != 0 then None
            else
              out.listRecursively()
                  .find(f => f.name == s"$fileName.json")
                  .map(f => ujson.read(f.contentAsString)("ast"))
          finally dir.delete(swallowIOExceptions = true)
          end try
      }.toOption.flatten

  /** Whether the astgen in use keeps a Vue single-file component's script positions when the
    * template holds an unclosed void element (AppThreat/atom-parsetools#83 and #84). With the
    * defect the whole-file candidate is rejected and the fallback parses the script first, so the
    * import lands on the wrong line; the probe reads the line the AST reports for the import.
    */
  private lazy val sfcPositionAstgenUsable: Boolean =
    val probeLines = List(
      "<template>",
      "  <form><input :disabled=\"off\" type=\"text\"><hr></form>",
      "</template>",
      "",
      "<script setup lang=\"ts\">",
      "import { computed } from 'vue'",
      "const label = computed(() => 'x')",
      "</script>",
      ""
    )
    val expectedLine = probeLines.takeWhile(_ != "import { computed } from 'vue'").size + 1
    probeAstgenAst("Probe.vue", probeLines.mkString("\n"), "vue")
        .exists(ast =>
            ast("program")("body").arr
                .filter(_("type").str == "ImportDeclaration")
                .map(_("loc")("start")("line").num.toInt)
                .headOption
                .contains(expectedLine)
        )
  end sfcPositionAstgenUsable

  /** Whether the astgen in use parses TC39 auto-accessors (AppThreat/atom-parsetools#34): without
    * the plugin the whole file fails, with it the class body holds a ClassAccessorProperty.
    */
  private lazy val autoAccessorAstgenUsable: Boolean =
      probeAstgenAst("Probe.js", "class A {\n  accessor x = 1;\n}\n", "js").exists { ast =>
        val types = scala.collection.mutable.ListBuffer.empty[String]
        def walk(v: ujson.Value): Unit =
            v match
              case ujson.Arr(items) => items.foreach(walk)
              case ujson.Obj(fields) =>
                  fields.get("type").foreach(t => types += t.str)
                  fields.values.foreach(walk)
              case _ =>
        walk(ast)
        types.contains("ClassAccessorProperty")
      }

  private def requireAstgen(): Unit =
      if !astgenUsable then
        cancel(
          "astgen unavailable or too old; JavaScript fixture needs the astgen from @appthreat/atom-parsetools on PATH"
        )

  private def requireSfcPositionAstgen(): Unit =
      if !sfcPositionAstgenUsable then
        cancel(
          s"astgen (${astgenCommand.mkString(" ")}) predates the Vue single-file component position fix (AppThreat/atom-parsetools#83, #84); the sfc-positions fixture needs it"
        )

  private def requireAutoAccessorAstgen(): Unit =
      if !autoAccessorAstgenUsable then
        cancel(
          s"astgen (${astgenCommand.mkString(" ")}) predates TC39 auto-accessor parsing (AppThreat/atom-parsetools#34); the accessors fixture needs it"
        )

  private def records(caseName: String): List[CallSite] =
    requireAstgen()
    slices.getOrElse(caseName, fail(s"no slice for $caseName"))

  /** The sfc-positions fixture's records, gated on the astgen behaviour they pin. */
  private def sfcPositionRecords(): List[CallSite] =
    requireSfcPositionAstgen()
    slices.getOrElse("sfc-positions", fail("no slice for sfc-positions"))

  /** The accessors fixture's records, gated on astgen parsing auto-accessors. The chen frontend
    * must handle ClassAccessorProperty too (its own change), which the records below check.
    */
  private def accessorRecords(): List[CallSite] =
    requireAutoAccessorAstgen()
    slices.getOrElse("accessors", fail("no slice for accessors"))

  private def at(caseName: String, file: String, line: Int, module: String): List[CallSite] =
      records(caseName).filter(r => r.file == file && r.line == line && r.module == module)

  "atom callsites" should {

      "carry the SBOM's purl of the npm package, and none where the SBOM cannot say (atom#321)" in {
          val purls = records("purl-bom").filter(_.file == "src/app.js").map(r =>
              (r.module, r.line) -> r.purl
          ).toMap
          // one purl: the package, also behind an encoded scope
          purls(("express", 9)) shouldBe "pkg:npm/express@4.19.2"
          purls(("@babel/parser", 13)) shouldBe "pkg:npm/%40babel/parser@7.24.0"
          // two versions of lodash: unsure, so none
          purls(("lodash", 10)) shouldBe ""
          // Node built-ins, even `events`, which the SBOM also lists as an npm package
          purls(("fs", 11)) shouldBe ""
          purls(("events", 12)) shouldBe ""
          // a tsconfig path alias for a project file, which the SBOM lists as `@lib/db` anyway
          records("purl-bom").filter(_.module == "@lib/db").map(_.purl).distinct should
              contain only ""
          // a package the SBOM does not list
          purls(("left-pad", 15)) shouldBe ""
      }

      "leave purl out of every record when the atom carries no SBOM (atom#321)" in {
          records("callsites").map(_.purl).distinct shouldBe List("")
      }

      "keep every record of a component with unclosed void elements and slot shorthand on a script line or a template binding (atom#320)" in {
          // The template holds an unclosed `<input>`, an `<hr>` and a `<template #fallback>` slot,
          // the shapes that made astgen 1.10.6 reject the whole-file candidate and move every
          // script line onto the template. Every expected line comes from the source files, and
          // the whole slice is exactly these six: nothing on a template line that uses no import.
          val expected = List(
            // Article.vue: the dayjs template binding at its own line, axios in the script
            CallSite("src/Article.vue", 10, 13, "dayjs", "", "call", "binding"),
            CallSite("src/Article.vue", 23, 9, "axios", "post", "call", "binding"),
            // Card.svelte: the script helper and the template binding beside it
            CallSite("src/Card.svelte", 5, 9, "date-fns", "format", "call", "binding"),
            CallSite("src/Card.svelte", 10, 8, "date-fns", "format", "call", "binding"),
            // Stats.astro: the frontmatter and the template expression
            CallSite("src/Stats.astro", 4, 19, "marked", "marked.parse", "call", "binding"),
            CallSite("src/Stats.astro", 6, 19, "marked", "marked.parse", "call", "binding")
          )
          sfcPositionRecords() should contain theSameElementsAs expected
      }

      "record a call in an auto-accessor's initialiser (atom-parsetools#34)" in {
          // `accessor id = uuidv4()` lowers to the field assignment `this.id = uuidv4()`, so the
          // initialiser's call is a use of the imported package, in TypeScript and in JavaScript
          val expected = List(
            CallSite("src/accessor.ts", 5, 16, "uuid", "v4", "call", "binding"),
            CallSite("src/accessor.ts", 6, 21, "dayjs", "", "call", "binding"),
            CallSite("src/plain.js", 4, 19, "nanoid", "", "call", "binding")
          )
          accessorRecords() should contain theSameElementsAs expected
      }

      "record single-file component uses at their script lines (atom#320)" in {
          // Vue, Svelte and Astro records carry the line the frontmatter or script block sits
          // on, and a template record only where the binding uses the import.
          val sfc = records("sfc")
          sfc.filter(r => r.file.endsWith(".vue")) should contain theSameElementsAs List(
            CallSite(
              "src/Comp.vue",
              16,
              20,
              "@/composables/useProfile",
              "useProfile",
              "call",
              "binding"
            ),
            CallSite("src/Comp.vue", 17, 14, "vue", "computed", "call", "binding"),
            CallSite("src/Comp.vue", 18, 13, "vue", "computed", "call", "binding"),
            CallSite("src/Comp.vue", 21, 9, "axios", "post", "call", "binding")
          )
          sfc.filter(r => r.file.endsWith(".svelte")) should contain theSameElementsAs List(
            CallSite("src/Widget.svelte", 5, 21, "svelte/store", "derived", "call", "binding"),
            CallSite("src/Widget.svelte", 5, 29, "$app/stores", "page", "arg", "binding"),
            // the template binding reads the imported store: a real use, at its own line
            CallSite("src/Widget.svelte", 9, 9, "$app/stores", "page", "ref", "binding")
          )
          sfc.filter(r => r.file.endsWith(".astro")) should contain theSameElementsAs List(
            CallSite("src/Page.astro", 5, 22, "axios", "get", "call", "binding")
          )
      }

      "slice a project whose destructured arrow parameters the frontend used to detach (outline shape)" in {
          // on the pre-tranche pairing this failed the whole slice with
          // NullPointerException: "method" is null in CallSiteSlicing.useRecord. The
          // destructured parameters shadow the import, so their uses are typed records
          val slice = records("orphans").filter(r => r.file == "src/actions.js")
          slice should contain theSameElementsAs List(
            CallSite("src/actions.js", 4, 27, "i18next", "t", "call", "type"),
            CallSite("src/actions.js", 5, 34, "i18next", "t", "call", "binding"),
            CallSite("src/actions.js", 9, 9, "i18next", "t", "call", "binding")
          )
      }

      "attribute a use the frontend detached from the AST and never fail the slice on it" in {
          requireAstgen()
          val atomFile = workspace / "orphans.atom"
          val cpg = io.shiftleft.codepropertygraph.cpgloading.CpgLoader
              .loadFromOverflowDb(
                io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig()
                    .withOverflowConfig(
                      overflowdb.Config.withDefaults()
                          .withStorageLocation(atomFile.pathAsString)
                    )
                    .doNotCreateIndexesOnLoad
              )
          // simulate the frontend defect: an identifier use with no AST parent, but with the
          // REF edge to the binding's local, exactly like outline's 7,296 detached identifiers
          val local = cpg.identifier.nameExact("t")
              .filter(_.lineNumber.exists(_.intValue == 9))
              .head.refsTo.head
          val builder = overflowdb.BatchedUpdate.DiffGraphBuilder()
          val orphan = io.shiftleft.codepropertygraph.generated.nodes.NewIdentifier()
              .name("t")
              .code("t")
              .order(0)
              .argumentIndex(0)
              .lineNumber(9)
              .columnNumber(5)
          builder.addNode(orphan)
          builder.addEdge(orphan, local, io.shiftleft.codepropertygraph.generated.EdgeTypes.REF)
          overflowdb.BatchedUpdate.applyDiff(cpg.graph, builder)
          val slice = io.appthreat.atom.slicing.CallSiteSlicing.calculateCallSitesSlice(cpg)
          // the detached use resolves through its REF edge like its parented sibling: the same
          // module, the same kind, attributed to the method of the local's declaration
          val detached = slice.callsites.filter(r => r.line == 9 || r.col == 5)
          detached.map(r =>
              (r.file, r.line, r.col, r.module, r.path, r.kind, r.via)
          ) should contain theSameElementsAs List(
            // the orphan, attributed through its REF edge to the binding's local
            (
              "src/actions.js",
              Integer.valueOf(9),
              Integer.valueOf(5),
              "i18next",
              "t",
              "ref",
              "binding"
            ),
            // its parented sibling on the same line
            (
              "src/actions.js",
              Integer.valueOf(9),
              Integer.valueOf(9),
              "i18next",
              "t",
              "call",
              "binding"
            )
          )
          detached.map(_.method) should not contain ""
          cpg.close()
      }

      "record each binding use at the use's own position" in {
          // `client.send(jwt.sign({ a: 1 }))`: jwt is called (its record carries the member path)
          at("callsites", "src/app.js", 13, "jsonwebtoken") should contain theSameElementsAs List(
            CallSite("src/app.js", 13, 21, "jsonwebtoken", "sign", "call", "binding")
          )
          // `[[1]].map(flatten)`: the binding is passed, not called
          at("callsites", "src/app.js", 18, "lodash") should contain theSameElementsAs List(
            CallSite("src/app.js", 18, 28, "lodash", "flatten", "arg", "binding")
          )
          // `return jwt;` is a plain reference
          at("callsites", "src/app.js", 21, "jsonwebtoken") should contain theSameElementsAs List(
            CallSite("src/app.js", 21, 9, "jsonwebtoken", "", "ref", "binding")
          )
      }

      "follow a destructured dynamic import into `new`" in {
          at(
            "callsites",
            "src/app.js",
            12,
            "@aws-sdk/client-s3"
          ) should contain theSameElementsAs List(
            CallSite("src/app.js", 12, 21, "@aws-sdk/client-s3", "S3Client", "new", "binding")
          )
      }

      "record a member call on an awaited dynamic import in expression position (atom#322)" in {
          // `(await import('uuid')).v4()`: no local takes the module, the frontend lowers it to
          // an assignment of a temporary, and the member read follows that value. The TS twin is
          // the same shape; the import clause itself still gives no definition-site record.
          at("callsites", "src/app.js", 25, "dayjs") should contain theSameElementsAs List(
            CallSite("src/app.js", 25, 9, "dayjs", "unix", "call", "binding")
          )
          at("callsites", "src/widgets.ts", 18, "dayjs") should contain theSameElementsAs List(
            CallSite("src/widgets.ts", 18, 9, "dayjs", "unix", "call", "binding")
          )
      }

      "record tagged templates by the tag's binding" in {
          at("callsites", "src/app.js", 6, "graphql-tag") should contain theSameElementsAs List(
            CallSite("src/app.js", 6, 17, "graphql-tag", "", "tag", "name")
          )
      }

      "record heritage clauses at the extends line" in {
          at("callsites", "src/app.js", 16, "sequelize") should contain theSameElementsAs List(
            CallSite("src/app.js", 16, -1, "sequelize", "Model", "extends", "name")
          )
          at("decorators", "src/cats.ts", 22, "events") should contain theSameElementsAs List(
            CallSite("src/cats.ts", 22, -1, "events", "EventEmitter", "extends", "name")
          )
      }

      "record an implements clause as its own kind" in {
          // interfaces are erased at run time; the clause is a use of its own kind
          at(
            "decorators",
            "src/cats.ts",
            23,
            "@angular/core"
          ) should contain theSameElementsAs List(
            CallSite("src/cats.ts", 23, -1, "@angular/core", "OnInit", "implements", "name")
          )
      }

      "keep an import's uses when a class field copies it under its own name" in {
          // `readonly Subject = Subject;` sets a field of `this`; it is not a second assignment
          // of the imported `Subject` (atom#293 review)
          records("callsites").filter(r =>
              r.file == "src/widgets.ts"
          ) should contain theSameElementsAs List(
            CallSite("src/widgets.ts", 4, 21, "rxjs", "Subject", "ref", "binding"),
            CallSite("src/widgets.ts", 7, 15, "rxjs", "Subject", "new", "binding"),
            CallSite("src/widgets.ts", 13, 15, "rxjs", "Subject", "new", "binding"),
            // the awaited dynamic import twin of the atom#322 test, in this file too
            CallSite("src/widgets.ts", 18, 9, "dayjs", "unix", "call", "binding")
          )
      }

      "follow a class field that holds an import into its reads" in {
          def in(file: String) =
              records("callsites-fields").filter(r => r.file == file && r.via == "binding")
                  .map(r => (r.line, r.kind, r.module, r.path))
          // `this.Subject` in the class and in a subclass; `mutable` is written twice: no alias
          in("src/aliases.ts") should contain theSameElementsAs List(
            (5, "ref", "rxjs", "Subject"),
            (6, "ref", "lodash-es", "uniq"),
            (9, "new", "rxjs", "Subject"),
            (13, "ref", "lodash-es", "chunk"),
            (19, "ref", "rxjs", "BehaviorSubject"),
            (22, "new", "rxjs", "Subject"),
            (23, "new", "rxjs", "BehaviorSubject")
          )
          // a static field read off the class, a private field, a string-keyed field
          (in("src/shapes.ts") should contain).allOf(
            (6, "ref", "dayjs", ""),
            (19, "call", "dayjs", ""),
            (23, "call", "lodash-es", "debounce"),
            (23, "call", "lodash-es", "throttle"),
            (23, "call", "lodash-es", "chunk")
          )
      }

      "record a component template's reads of a field that holds an import" in {
          def in(file: String) =
              records("callsites-fields").filter(r => r.file == file && r.via == "binding")
                  .map(r => (r.line, r.col, r.kind, r.module, r.path))
          // the templateUrl file: a property binding and an interpolated call
          in("src/app/icons.component.html") should contain theSameElementsAs List(
            (1, 17, "ref", "@fortawesome/free-solid-svg-icons", "faCoffee"),
            (2, 9, "call", "date-fns", "formatDistance")
          )
          // an inline template, positioned in the decorator; `| uppercase` is a pipe, not a read
          in("src/app/icons.component.ts").filter(_._1 == 16) should contain theSameElementsAs List(
            (16, 30, "ref", "@fortawesome/free-solid-svg-icons", "faStar"),
            (16, 52, "ref", "@fortawesome/free-solid-svg-icons", "faStar.iconName")
          )
      }

      "not take a field written any other way for an alias" in {
          def in(file: String) =
              records("callsites-fields").filter(r => r.file == file && r.via == "binding")
                  .map(r => (r.line, r.kind, r.module, r.path))
          // only the initialisers' own values: no read of `this.g` (bracket write), `this.k` (`??=`),
          // `this.h` (destructuring), `this.q` (Object.assign), `this.S` in an instance method,
          // `this.f` (overwritten by a subclass); `new this.S()` in a static method is a read
          in("src/negatives.ts") should contain theSameElementsAs List(
            (4, "ref", "rxjs", "of"),
            (5, "ref", "rxjs", "from"),
            (10, "ref", "rxjs", "timer"),
            (11, "ref", "rxjs", "interval"),
            (16, "ref", "rxjs", "from"),
            (22, "ref", "rxjs", "Subject"),
            (28, "ref", "rxjs", "Subject"),
            (30, "new", "rxjs", "Subject"),
            (34, "ref", "rxjs", "Subject"),
            (39, "ref", "rxjs", "BehaviorSubject")
          )
          // `Widget extends Component` from react: not the project's Component and its field
          in("src/widget.ts") shouldBe empty
      }

      "read templates as Angular does" in {
          def in(file: String) =
              records("callsites-fields").filter(r => r.file == file && r.via == "binding")
                  .map(r => (r.line, r.col, r.kind, r.path))
          // escaped quotes in an inline template; `'star'` is a string; a template that writes
          // `icon` makes it no alias
          in("src/app/more.component.ts").filter(_._1 == 7) should contain theSameElementsAs List(
            (7, 33, "ref", "faStar"),
            (7, 73, "call", "format")
          )
          in("src/app/more.component.ts").filter(_._1 > 25).map(_._1) shouldBe List(28)
          // a templateUrl without `./`; `href="#coffee"` and prose declare nothing; the `@for`
          // variable hides the field only in its block; an unquoted value; a string is no read
          in("src/app/plain.component.html") should contain theSameElementsAs List(
            (1, 17, "ref", "faCoffee"),
            (3, 11, "ref", "faStar")
          )
      }

      "record JSX tags by name until the frontend links them (chen T9)" in {
          at("callsites", "src/ui.jsx", 4, "antd") should contain theSameElementsAs List(
            CallSite("src/ui.jsx", 4, 25, "antd", "Button", "jsx", "name")
          )
      }

      "record decorators by their binding, one per decorator" in {
          records("decorators").filter(r => r.kind == "decorator").map(r =>
              (r.module, r.line, r.path)
          ) should
              contain theSameElementsAs List(
                ("typeorm", 6, "Entity"),
                ("typeorm", 8, "Column"),
                ("@nestjs/common", 12, "Controller"),
                ("@nestjs/common", 14, "Get"),
                ("@nestjs/common", 15, "Param"),
                ("@nestjs/common", 20, "Injectable")
              )
      }

      "not emit definition-site or `_tmp` records" in {
          // line 11 is `const { S3Client } = await import(...)` — the destructuring definition
          records("callsites").filter(r => r.file == "src/app.js" && r.line == 11) shouldBe empty
          records("callsites").filter(r =>
              r.path.contains("_tmp") || r.module.contains("_tmp")
          ) shouldBe empty
      }

      "record each import that binds nothing as one record at the statement (atom#367)" in {
          // The import runs the module (decision 0002, Q1), so the statement itself is the use.
          // In polyfills.ts: the side-effect `import 'y'` of lines 1 to 4 (ngx-admin's polyfills),
          // `import {} from 'mitt'` of line 8, `export * from 'left-pad'` of line 9 and the bare
          // `require('dot-prop')` statement of line 10.
          records("side-effect-imports").filter(r =>
              r.file == "src/polyfills.ts" && r.kind == "import"
          ) should contain theSameElementsAs List(
            CallSite("src/polyfills.ts", 1, 0, "zone.js", "", "import", "binding"),
            CallSite("src/polyfills.ts", 2, 0, "core-js/es6/reflect", "", "import", "binding"),
            CallSite("src/polyfills.ts", 3, 0, "classlist.js", "", "import", "binding"),
            CallSite("src/polyfills.ts", 4, 0, "web-animations-js", "", "import", "binding"),
            CallSite("src/polyfills.ts", 8, 0, "mitt", "", "import", "binding"),
            CallSite("src/polyfills.ts", 9, 0, "left-pad", "", "import", "binding"),
            CallSite("src/polyfills.ts", 10, 0, "dot-prop", "", "import", "binding")
          )
          // In forms.js: the dynamic `import('left-pad');` and `require("double-quoted");`
          // statements, the re-exports `export { pick } from 'lodash-es'` and
          // `export * as nsx from 'axios'`, and the `require('mitt')` statement inside a block.
          records("side-effect-imports").filter(r =>
              r.file == "src/forms.js" && r.kind == "import"
          ) should contain theSameElementsAs List(
            CallSite("src/forms.js", 1, 0, "left-pad", "", "import", "binding"),
            CallSite("src/forms.js", 2, 0, "double-quoted", "", "import", "binding"),
            CallSite("src/forms.js", 3, 0, "lodash-es", "", "import", "binding"),
            CallSite("src/forms.js", 4, 0, "axios", "", "import", "binding"),
            CallSite("src/forms.js", 8, 2, "mitt", "", "import", "binding")
          )
          // In cjs.js: a require whose value no local name takes still loads the module: the
          // CommonJS re-exports of lines 1 and 2, the module called at once on line 3, the awaited
          // dynamic import of line 5, the body of an `if` on line 7 and an argument on line 8.
          records("side-effect-imports").filter(r =>
              r.file == "src/cjs.js" && r.kind == "import"
          ).map(r => (r.line, r.module)) should contain theSameElementsAs List(
            (1, "express"),
            (2, "koa-router"),
            (3, "debug"),
            (5, "pino-pretty"),
            (7, "why-is-node-running"),
            (8, "ts-node")
          )
      }

      "give binding imports and relative specifiers no record of their own (atom#367)" in {
          // the binding twins of polyfills.ts lines 5 to 7 (`import { computed }`, `import type`,
          // `import * as`) and 11 to 12 (`const flat = require`, hand-written `var pino =
          // require('pino')`, whose import node looks like a side-effect import's), and the
          // import-equals of lines 18 and 19 (which binds `vim` and `ioredis`): a record only at
          // the use, at lines 14 to 17 and 21
          records("side-effect-imports").filter(r =>
              r.file == "src/polyfills.ts" && r.via == "binding" && r.kind != "import"
          ).map(r => (r.line, r.module, r.path)) should contain theSameElementsAs List(
            (14, "vue", "computed"),
            (15, "flatted", ""),
            (16, "axios", ""),
            (17, "pino", ""),
            (21, "vim", ""),
            (21, "ioredis", "")
          )
          // the hand-written `var hand = require('hand-written')` of forms.js line 11 and the
          // `const chalk = require('chalk')` of line 14 (whose XImportsPass import node looks
          // like a side-effect import's, without a line and without `=` in its code) keep the
          // same rule: a record only at the use
          records("side-effect-imports").filter(r =>
              r.file == "src/forms.js" && r.kind != "import"
          ).map(r => (r.line, r.module, r.kind)) shouldBe List(
            (12, "hand-written", "ref"),
            (15, "chalk", "ref")
          )
          // cjs.js: the conditional of line 9 binds `maybe` and the dynamic require of line 10
          // binds `_local` (the re-export lowering is static), so neither has a record of its
          // own, only `_local`'s use on line 13; `require('dotenv').config()` on line 11 is the
          // member call's record only; TypeScript's `__importDefault(require("config"))` on line 14
          // hands the module to `config_1`, which gives the records at its uses on lines 15 and
          // 16, the second through the helper's `default`, which is the module itself, as for the
          // ESM default import; `__importStar` on line 17 gives a namespace, used on line 18
          records("side-effect-imports").filter(r => r.file == "src/cjs.js" && r.line >= 9)
              .map(r => (r.line, r.module, r.path, r.kind)) shouldBe List(
            (11, "dotenv", "config", "call"),
            (13, "underscored", "", "ref"),
            (15, "config", "", "ref"),
            (16, "config", "get", "call"),
            (18, "ns-pkg", "pick", "call")
          )
          // `export * from './own.js'` and `require('./relative.js')` name the project's own files
          records("side-effect-imports").filter(r =>
              r.module.startsWith("./") || r.module == "own.js" || r.module == "relative.js"
          ) shouldBe empty
      }
  }
end JsCallSitesTests
