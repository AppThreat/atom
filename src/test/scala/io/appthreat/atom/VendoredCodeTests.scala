package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.mutable
import scala.util.Try

/** Vendored code (decision 0001, P5; atom#346): `--external-paths` and the BOM's component
  * locations, in every source frontend. Each fixture under `resources/vendored/<language>` calls
  * into `third_party/` and into `src/not_third_party/`, which is the project's own code and the
  * anchoring twin: the pattern `third_party` must not reach it.
  *
  * Four runs per language: without the option (the project's slice, as on main), with the pattern,
  * with a BOM whose one component lives in `third_party` (`cdx:vendored:path`), and with a BOM
  * whose two components claim that path (unmarked, so as the first run). The expected records were
  * read off the real output of each run and checked against the rules; a run that needs a tool the
  * machine lacks cancels with the reason.
  */
class VendoredCodeTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  import VendoredCodeTests.*
  private def cLang = VendoredCodeTests.cLang

  private var workspace: BFile = scala.compiletime.uninitialized
  private val runs             = mutable.HashMap.empty[(String, String), Either[String, List[Rec]]]

  override protected def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomVendored")

  override protected def afterAll(): Unit = workspace.delete(swallowIOExceptions = true)

  private val BomOne =
      """{"bomFormat":"CycloneDX","specVersion":"1.5","components":[
      | {"type":"library","name":"vendlib","version":"1.2.3","purl":"pkg:generic/vendlib@1.2.3",
      |  "properties":[{"name":"cdx:vendored","value":"true"},
      |                {"name":"cdx:vendored:path","value":"third_party"}]}]}""".stripMargin

  private val BomTwo =
      """{"bomFormat":"CycloneDX","specVersion":"1.5","components":[
      | {"type":"library","name":"vendlib","version":"1.2.3","purl":"pkg:generic/vendlib@1.2.3",
      |  "properties":[{"name":"cdx:vendored:path","value":"third_party"}]},
      | {"type":"library","name":"otherlib","version":"9.9","purl":"pkg:generic/otherlib@9.9",
      |  "properties":[{"name":"cdx:vendored:path","value":"third_party"}]}]}""".stripMargin

  /** The records of one variant, memoised: the non-builtin ones, as the assertions read them. */
  private def records(lang: Lang, variant: String): List[Rec] =
      runs.getOrElseUpdate((lang.fixture, variant), run(lang, variant)) match
        case Right(rs)   => rs
        case Left(error) => cancel(s"${lang.fixture} $variant: $error")

  private def run(lang: Lang, variant: String): Either[String, List[Rec]] =
    unavailable(lang.fixture).foreach(reason => return Left(reason))
    val dir     = workspace / lang.fixture / variant
    val project = dir / "proj"
    BFile(getClass.getResource(s"/vendored/${lang.fixture.stripSuffix("-edg")}").toURI).copyTo(
      project
    )
    val bom = dir / "bom.json"
    val extra = variant match
      case "none"    => Nil
      case "pattern" => List("--external-paths", "third_party")
      case "bomone" =>
          bom.write(BomOne)
          List("--bom-file", bom.pathAsString)
      case "bomtwo" =>
          bom.write(BomTwo)
          List("--bom-file", bom.pathAsString)
    val slice = dir / "callsites.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: lang.language :: lang.extraArgs ::: extra :::
          "-o" :: (dir / "out.atom").pathAsString :: "-s" :: slice.pathAsString ::
          List(project.pathAsString)).toArray
    )
    result match
      case Left(error) => Left(s"atom failed: $error")
      case Right(_) =>
          Right(
            ujson.read(slice.contentAsString).obj("callsites").arr
                .filterNot(r => r.obj.get("builtin").exists(_.bool))
                .map(r =>
                    Rec(
                      // Java names files with the platform's separator, as on main
                      r("file").str.replace('\\', '/'),
                      r("line").num.toInt,
                      r("module").str,
                      r("path").str,
                      r("kind").str,
                      r.obj.get("purl").map(_.str).getOrElse("")
                    )
                ).toList.sortBy(r => (r.file, r.line, r.module, r.path, r.kind))
          )
  end run

  private def sorted(rs: List[Rec]): List[Rec] =
      rs.sortBy(r => (r.file, r.line, r.module, r.path, r.kind))

  private def check(lang: Lang): Unit =
      lang.fixture should {
          "give the project's slice without the option" in {
              records(lang, "none") shouldBe sorted(lang.none)
          }
          "name the vendored directory as module with the pattern, and leave not_third_party alone" in {
              records(lang, "pattern") shouldBe sorted(lang.pattern)
          }
          "name the BOM component and its purl for a component located in the input" in {
              records(lang, "bomone") shouldBe sorted(lang.bomOne)
          }
          "leave a path two components claim unmarked" in {
              records(lang, "bomtwo") shouldBe sorted(lang.none)
          }
      }

  private val noAstgen: Option[String] =
      Option.unless(
        Try(
          scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim)
        )
            .toOption.flatten.exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))
      )(
        "astgen unavailable or too old; JavaScript needs the astgen from @appthreat/atom-parsetools"
      )

  private val noPhp: Option[String] =
      Option.when(Try(scala.sys.process.Process(Seq("php", "--version")).!!).isFailure)(
        "php is not installed"
      )

  private val noRuby: Option[String] =
      Option.when(Try(scala.sys.process.Process(Seq("rbastgen", "--version")).!!).isFailure)(
        "rbastgen is not installed"
      )

  private val edgaPath: Option[String] =
      sys.env.get("EDGA_PATH").orElse(sys.props.get("edga.path")).filter(p =>
          BFile(p).isRegularFile
      ).orElse(
        Try(scala.sys.process.Process(Seq("which", "edga")).!!.trim).toOption.filter(_.nonEmpty)
      )

  /** The tool a language's frontend needs, when the machine lacks it. */
  private def unavailable(fixture: String): Option[String] = fixture match
    case "javascript" => noAstgen
    case "php"        => noPhp
    case "ruby"       => noRuby
    case _            => None

  "atom callsites with vendored code" should {
      "have one meaning of --external-paths for the languages it covers" in {
          // the per-language tables below are the evidence; this names the set
          VendoredCodeTests.languages.map(_.fixture) shouldBe
              List("python", "java", "javascript", "php", "ruby", "c")
      }
  }

  VendoredCodeTests.languages.foreach { lang =>
      lang.fixture.capitalize should {
          check(lang)
      }
  }

  "C through EDG" should {
      def edg: Lang =
        assume(edgaPath.isDefined, "edga is not installed (set EDGA_PATH or put edga on PATH)")
        VendoredCodeTests.cLang.copy(
          fixture = "c-edg",
          extraArgs = List("--c-frontend", "edg", "--edga-path", edgaPath.get)
        )
      "give the project's slice without the option" in {
          records(edg, "none") shouldBe cLang.none
      }
      "name the vendored directory as module with the pattern, and leave not_third_party alone" in {
          records(edg, "pattern") shouldBe cLang.pattern
      }
      "name the BOM component and its purl for a component located in the input" in {
          records(edg, "bomone") shouldBe cLang.bomOne
      }
      "leave a path two components claim unmarked" in {
          records(edg, "bomtwo") shouldBe cLang.none
      }
  }
end VendoredCodeTests

object VendoredCodeTests:

  final case class Rec(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    purl: String
  )

  final case class Lang(
    fixture: String,
    language: String,
    none: List[Rec],
    pattern: List[Rec],
    bomOne: List[Rec],
    extraArgs: List[String] = Nil
  )

  private val Purl = "pkg:generic/vendlib@1.2.3"

  // the JavaScript source does not flag Node built-ins (`builtin` is for the other languages)
  private val fs = Rec("main.js", 11, "fs", "readFileSync", "call", "")

  private def rec(file: String, line: Int, module: String, path: String, kind: String) =
      Rec(file, line, module, path, kind, "")

  /** The vendored records of a language as the pattern sees them, and as the BOM component does. */
  private def vendored(
    module: String,
    purl: String,
    rows: (String, Int, String, String)*
  ): List[Rec] = rows.map((f, l, p, k) => Rec(f, l, module, p, k, purl)).toList

  val cLang: Lang = Lang(
    "c",
    "c",
    none = Nil,
    pattern = vendored("third_party", "", ("main.c", 3, "zlib_compress", "call")),
    bomOne = vendored("vendlib", Purl, ("main.c", 3, "zlib_compress", "call"))
  )

  def languages: List[Lang] = List(
    Lang(
      "python",
      "python",
      // `third_party` and `src` hold no `__init__.py`: the frontend names their modules by the
      // last segment, so without the option the imports are uses of modules the project lacks
      none = List(
        rec("main.py", 11, "third_party.lib", "run", "call"),
        rec("main.py", 12, "src.not_third_party", "helper", "call"),
        rec("use.py", 4, "third_party.lib", "Base", "decltype"),
        rec("use.py", 5, "third_party.lib", "Base", "call"),
        rec("use.py", 6, "third_party.lib", "Base.go", "ref"),
        rec("use.py", 6, "third_party.lib", "Base.go", "ref")
      ),
      pattern = pythonVendored("third_party", ""),
      bomOne = pythonVendored("vendlib", Purl)
    ),
    Lang(
      "java",
      "java",
      none = Nil,
      pattern = javaVendored("third_party", ""),
      bomOne = javaVendored("vendlib", Purl)
    ),
    Lang(
      "javascript",
      "javascript",
      none = List(fs),
      pattern = jsVendored("third_party", "") :+ fs,
      bomOne = jsVendored("vendlib", Purl) :+ fs
    ),
    Lang(
      "php",
      "php",
      none = Nil,
      pattern = phpVendored("third_party", ""),
      bomOne = phpVendored("vendlib", Purl)
    ),
    Lang(
      "ruby",
      "ruby",
      none = Nil,
      pattern = rubyVendored("third_party", ""),
      bomOne = rubyVendored("vendlib", Purl)
    ),
    cLang
  )

  // Each language source records its uses of vendored code as it records any dependency's, with
  // its own kinds; the module is the vendored directory (or the BOM component), the path the
  // member for a call and the type for `new`, `extends`, `implements` and `decltype`.

  private def pythonVendored(module: String, purl: String): List[Rec] = vendored(
    module,
    purl,
    ("main.py", 6, "Base", "extends"),
    ("main.py", 11, "run", "call"),
    ("use.py", 4, "Base", "decltype"),
    ("use.py", 5, "Base", "call"),
    ("use.py", 6, "Base.go", "ref"),
    ("use.py", 6, "Base.go", "ref")
  ) :+ rec("main.py", 12, "src.not_third_party", "helper", "call")

  private def javaVendored(module: String, purl: String): List[Rec] = vendored(
    module,
    purl,
    ("src/main/java/app/Main.java", 6, "Helper", "extends"),
    ("src/main/java/app/Main.java", 8, "run", "call"),
    ("src/main/java/app/Main.java", 9, "Helper", "new"),
    ("src/main/java/app/Main.java", 9, "go", "call"),
    // a class that names no superclass implements what it names
    ("src/main/java/app/Use.java", 6, "Shape", "implements"),
    ("src/main/java/app/Use.java", 7, "Helper", "decltype"),
    ("src/main/java/app/Use.java", 11, "Helper", "decltype"),
    ("src/main/java/app/Use.java", 12, "Helper", "new"),
    ("src/main/java/app/Use.java", 13, "go", "call"),
    ("src/main/java/app/Use.java", 13, "go", "call")
  )

  private def jsVendored(module: String, purl: String): List[Rec] = vendored(
    module,
    purl,
    ("main.js", 6, "Base", "extends"),
    ("main.js", 8, "run", "call"),
    ("main.js", 9, "run", "call"),
    ("use.js", 3, "Base", "new")
  )

  private def phpVendored(module: String, purl: String): List[Rec] = vendored(
    module,
    purl,
    ("main.php", 5, "run", "call"),
    ("main.php", 6, "helper_fn", "call"),
    ("use.php", 4, "Helper", "extends"),
    ("use.php", 6, "Tool", "decltype"),
    // one record for the typed call, the PHP source's
    ("use.php", 7, "go", "call"),
    ("use.php", 10, "Tool", "new")
  )

  private def rubyVendored(module: String, purl: String): List[Rec] = vendored(
    module,
    purl,
    ("main.rb", 4, "run", "call"),
    // ruby2atom resolves these constants itself, so the graph's edges give the records
    ("use.rb", 3, "Tool", "extends"),
    ("use.rb", 6, "Tool", "new")
  )
end VendoredCodeTests
