package io.appthreat.atom

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

  private val fixtureCases = Seq("callsites", "decorators")

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

      "record each binding use at the use's own position" in {
          val rs = records("callsites")
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
          at("decorators", "src/cats.ts", 21, "events") should contain theSameElementsAs List(
            CallSite("src/cats.ts", 21, -1, "events", "EventEmitter", "extends", "name")
          )
      }

      "record JSX tags by name until the frontend links them (chen T9)" in {
          at("callsites", "src/ui.jsx", 3, "antd") should contain theSameElementsAs List(
            CallSite("src/ui.jsx", 3, 25, "antd", "Button", "jsx", "name")
          )
      }

      "record decorators by their binding, one per decorator" in {
          val rs = records("decorators")
          rs.filter(r => r.kind == "decorator").map(r => (r.module, r.line, r.path)) should
              contain theSameElementsAs List(
                ("typeorm", 5, "Entity"),
                ("typeorm", 7, "Column"),
                ("@nestjs/common", 11, "Controller"),
                ("@nestjs/common", 13, "Get"),
                ("@nestjs/common", 14, "Param"),
                ("@nestjs/common", 19, "Injectable")
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
