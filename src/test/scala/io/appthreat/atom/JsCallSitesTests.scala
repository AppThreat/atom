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
  * decorator). The JavaScript frontend needs the external `astgen` generator, so the suite cancels
  * (not fails) without one, exactly like [[JsUsagesExternalCallSiteTests]].
  */
class JsCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val fixtureCases = Seq("callsites", "decorators", "callsites-fields", "orphans")

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
    via: String
  )

  override def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomJsCallsites")
    if astgenUsable then
      slices = fixtureCases.map(caseName => caseName -> runCallsites(caseName)).toMap

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def runCallsites(caseName: String): List[CallSite] =
    val fixture    = BFile(getClass.getResource(s"/js-usages/$caseName").toURI)
    val projectDir = workspace / caseName
    copyFixture(fixture, projectDir)
    val atomFile  = workspace / s"$caseName.atom"
    val sliceFile = workspace / s"$caseName-callsites.json"
    val result = Atom.run(
      Seq(
        "callsites",
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
              r("via").str
            )
        ).toList
  end runCallsites

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

  private def requireAstgen(): Unit =
      if !astgenUsable then
        cancel(
          "astgen unavailable or too old; JavaScript fixture needs the astgen from @appthreat/atom-parsetools on PATH"
        )

  private def records(caseName: String): List[CallSite] =
    requireAstgen()
    slices.getOrElse(caseName, fail(s"no slice for $caseName"))

  private def at(caseName: String, file: String, line: Int, module: String): List[CallSite] =
      records(caseName).filter(r => r.file == file && r.line == line && r.module == module)

  "atom callsites" should {

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
            ("src/actions.js", Integer.valueOf(9), Integer.valueOf(5), "i18next", "t", "ref", "binding"),
            // its parented sibling on the same line
            ("src/actions.js", Integer.valueOf(9), Integer.valueOf(9), "i18next", "t", "call", "binding")
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
          at("decorators", "src/cats.ts", 23, "@angular/core") should contain theSameElementsAs List(
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
            CallSite("src/widgets.ts", 13, 15, "rxjs", "Subject", "new", "binding")
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
          at("callsites", "src/ui.jsx", 3, "antd") should contain theSameElementsAs List(
            CallSite("src/ui.jsx", 3, 25, "antd", "Button", "jsx", "name")
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
  }
end JsCallSitesTests
