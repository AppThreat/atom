package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `atom callsites` over PHP fixtures: one record per use of a class or function the project does
  * not declare, asserted exactly on (line, col, module, path, kind, via, builtin, purl).
  *
  * The fixture project (`php-callsites`) holds one example of each shape in `src/Shapes.php`: the
  * calls of functions (namespaced, global fallback, `use function`, builtin), static calls, `new`,
  * class constants, `X::class`, static properties, `instanceof`, `catch`, attributes on every
  * target, extends, implements, trait use, every declared-type position (nullable, union), each
  * source of a typed call, a pipe line and a property hook. Its good twins have no record: a
  * project function, a project class, a reassigned variable, a union receiver, an untyped call on a
  * call's result, a `use` statement, a `declare` and a project class that shadows an imported name
  * (`src/Shadow.php`). The `vendor/` directory is a tiny Composer tree (installed.json, the
  * autoload maps and stub classes) that gives the records their package, and it is not parsed.
  */
class PhpCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  import PhpCallSitesTests.Rec

  private var workspace: BFile           = scala.compiletime.uninitialized
  private var records: List[Rec]         = Nil
  private var withoutComposer: List[Rec] = Nil
  private var atomRan: Boolean           = false

  private def runAtom(project: BFile, name: String): Either[String, List[Rec]] =
    val sliceFile = workspace / s"$name.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: "php" ::
          "-o" :: (workspace / s"$name.atom").pathAsString ::
          "-s" :: sliceFile.pathAsString ::
          List(project.pathAsString)).toArray
    )
    result.left.map(_.toString).map(_ => read(sliceFile))

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomPhpCallsites")
    val fixture = BFile(getClass.getResource("/php-callsites").toURI)
    val project = workspace / "proj"
    fixture.copyTo(project)
    atomRan = runAtom(project, "slice") match
      case Right(recs) =>
          records = recs
          true
      case Left(_) => false
    if atomRan then
      // the same sources without Composer's metadata: every class the project does not declare is
      // a dependency's, with no package to name
      val bare = workspace / "bare"
      (fixture / "src").copyTo(bare / "src")
      runAtom(bare, "bare").foreach(withoutComposer = _)

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def read(sliceFile: BFile): List[Rec] =
      ujson.read(sliceFile.contentAsString).obj("callsites").arr
          .map(r =>
              Rec(
                r("file").str.split(Array('/', '\\')).last,
                r("line").num.toInt,
                r("col").num.toInt,
                r("module").str,
                r("path").str,
                r("kind").str,
                r("via").str,
                r.obj.get("builtin").map(_.bool).getOrElse(false),
                r.obj.get("purl").map(_.str).getOrElse("")
              )
          )
          .toList

  private def in(file: String, from: List[Rec] = records): List[Rec] =
    if !atomRan then cancel("the PHP fixture run failed")
    from.filter(_.file == file)

  private def shape(rs: List[Rec]): List[Rec] = rs.sortBy(r => (r.line, r.col, r.module, r.path))

  "atom callsites for PHP" should {

      "emit exactly the records of every shape in the fixture" in {
          in("Shapes.php").map(_.copy(file = "")) shouldBe PhpCallSitesTests.expectedShapes
      }

      "emit no record for a project class that shadows an imported name, or for the project's own code" in {
          in("Shadow.php") shouldBe Nil
          in("Local.php") shouldBe Nil
      }

      "emit no record for a use statement, a declare, a project function or a project class" in {
          val lines = in("Shapes.php").map(_.line).toSet
          // use statements sit on lines 6 to 12, the declare on line 2
          (1 to 13).filter(lines.contains) shouldBe Nil
          // `local()` on line 38 and `new Local()` on line 41
          lines.contains(38) shouldBe false
          lines.contains(41) shouldBe false
      }

      "name each record where the source writes the name, with no record at a line before 1" in {
          records.foreach(r => r.line should be > 0)
          // a typed call is at its method name, not at the receiver the line starts with
          val typed = in("Shapes.php").filter(_.via == "type")
          typed.map(r => (r.line, r.col)) shouldBe List(
            (47, 16),
            (52, 12),
            (53, 24),
            (54, 23),
            (55, 21),
            (56, 47),
            // `$g\n->run()` on lines 107 and 108: the use is the method name, on line 108
            (108, 14)
          )
      }

      "leave PHP's own functions and classes marked builtin, without a package" in {
          val builtin = in("Shapes.php").filter(_.builtin)
          builtin.map(r => (r.module, r.kind)).toSet shouldBe
              Set(
                ("Countable", "extends"),
                ("strlen", "call"),
                ("PDO", "new"),
                ("trim", "call"),
                ("strval", "call")
              )
          builtin.foreach(_.purl shouldBe "")
      }

      "name no package for a class when Composer's metadata is absent, and keep every record" in {
          val bare = in("Shapes.php", withoutComposer)
          bare.map(r => (r.line, r.col, r.module, r.path, r.kind, r.via, r.builtin)) shouldBe
              PhpCallSitesTests.expectedShapes.map(r =>
                  (r.line, r.col, r.module, r.path, r.kind, r.via, r.builtin)
              )
          bare.foreach(_.purl shouldBe "")
      }
  }
end PhpCallSitesTests

object PhpCallSitesTests:

  final case class Rec(
    file: String,
    line: Int,
    col: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean,
    purl: String
  )

  object Rec:
    def apply(
      line: Int,
      col: Int,
      module: String,
      path: String,
      kind: String,
      via: String,
      builtin: Boolean,
      purl: String
    ): Rec = Rec("", line, col, module, path, kind, via, builtin, purl)

  // format: off
  val expectedShapes: List[Rec] = List(
    Rec(14, 2, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(15, 21, "Acme\\Widgets\\Widget", "", "extends", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(15, 39, "Countable", "", "extends", "binding", true, ""),
    Rec(17, 8, "Acme\\Widgets\\Shares", "", "extends", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(19, 6, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(20, 13, "Acme\\Widgets\\Widget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(21, 17, "Acme\\Widgets\\Flag", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(23, 6, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(24, 34, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(24, 48, "Acme\\Widgets\\Gadget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(24, 65, "Acme\\Widgets\\Widget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(33, 26, "Acme\\Widgets\\Widget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(33, 37, "Acme\\Widgets\\Gadget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(33, 44, "Acme\\Widgets\\Failure", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(33, 69, "Acme\\Widgets\\Widget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(35, 8, "Acme\\Util\\helper", "", "call", "binding", false, "pkg:composer/acme/util@2.0.1"),
    Rec(36, 8, "strlen", "", "call", "binding", true, ""),
    Rec(37, 8, "collect", "", "call", "binding", false, ""),
    Rec(39, 13, "Acme\\Widgets\\Widget", "make", "call", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(40, 17, "Acme\\Widgets\\Widget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(42, 13, "Acme\\Widgets\\Flag", "LIMIT", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(43, 13, "Acme\\Widgets\\Flag", "", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(44, 13, "Acme\\Widgets\\Flag", "count", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(45, 22, "Acme\\Widgets\\Failure", "", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(47, 16, "Acme\\Widgets\\Widget", "render", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(48, 17, "Acme\\Widgets\\Failure", "", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(49, 21, "PDO", "", "new", "binding", true, ""),
    Rec(51, 17, "Acme\\Widgets\\Gadget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(52, 12, "Acme\\Widgets\\Gadget", "run", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(53, 13, "Acme\\Widgets\\Gadget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(53, 24, "Acme\\Widgets\\Gadget", "go", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(54, 23, "Acme\\Widgets\\Gadget", "go", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(55, 21, "Acme\\Widgets\\Widget", "render", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(56, 47, "Acme\\Widgets\\Widget", "render", "call", "type", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(57, 18, "Acme\\Widgets\\Gadget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(58, 18, "Acme\\Widgets\\Gadget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(61, 13, "Acme\\Widgets\\Widget", "make", "call", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(68, 20, "trim", "", "call", "binding", true, ""),
    Rec(68, 38, "Acme\\Widgets\\Widget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(69, 13, "Acme\\Widgets\\Widget", "make", "call", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(69, 31, "strval", "", "call", "binding", true, ""),
    Rec(77, 2, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(80, 6, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(87, 15, "Acme\\Widgets\\Widget", "make", "call", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(91, 2, "Acme\\Widgets\\Mark", "", "attribute", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(91, 7, "Acme\\Widgets\\Flag", "LIMIT", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(95, 27, "Acme\\Widgets\\Flag", "LIMIT", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(96, 30, "Acme\\Widgets\\Flag", "class", "ref", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(97, 16, "Acme\\Widgets\\Gadget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(97, 37, "Acme\\Widgets\\Gadget", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(104, 26, "Acme\\Widgets\\Gadget", "", "decltype", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(106, 17, "Acme\\Util\\Legacy", "", "new", "binding", false, "pkg:composer/acme/widgets@1.2.0"),
    Rec(108, 14, "Acme\\Widgets\\Gadget", "run", "call", "type", false, "pkg:composer/acme/widgets@1.2.0")
  )
  // format: on
end PhpCallSitesTests
