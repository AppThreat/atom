package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.atom.slicing.callsites.RubyGemMap
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `atom callsites` over a Ruby fixture: one record per constant that resolves outside the project,
  * asserted exactly on (line, col, module, path, kind, via, builtin, purl).
  *
  * The fixture project (`ruby-callsites`) holds one example of each shape in `lib/shapes.rb`: a
  * superclass and `include`, `extend` and `prepend` (a gem's, the standard library's, a project
  * concern), calls and `new` on constants of a gem and of Ruby, `::JSON`, an attribute-style call
  * (`Rails.root`), `raise`, `rescue` lists, a lexical lookup that reaches a gem (`module I18n`), a
  * constant nobody defines, and the typed shape with its twins. Its good twins have no record: a
  * `require` line, a nested project class, a Rails autoload-implied constant, a project concern, a
  * DSL call without a receiver (`has_many`), a core class the project reopens, a setter, a local
  * bound twice or by a parameter, a class whose `new` is not the plain one, and a gem class (which
  * is never proved to have the plain `new`). `vendor/bundle` is a tiny installed-gem tree (stub
  * files whose paths name the constants) that gives the records their gem, and it is not parsed.
  */
class RubyCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  import RubyCallSitesTests.Rec

  private var workspace: BFile       = scala.compiletime.uninitialized
  private var records: List[Rec]     = Nil
  private var withoutGems: List[Rec] = Nil
  private var withoutLock: List[Rec] = Nil
  private var atomRan: Boolean       = false

  private def runAtom(project: BFile, name: String): Either[String, List[Rec]] =
    val sliceFile = workspace / s"$name.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: "ruby" ::
          "-o" :: (workspace / s"$name.atom").pathAsString ::
          "-s" :: sliceFile.pathAsString ::
          List(project.pathAsString)).toArray
    )
    result.left.map(_.toString).map(_ => read(sliceFile))

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomRubyCallsites")
    val fixture = BFile(getClass.getResource("/ruby-callsites").toURI)
    val project = workspace / "proj"
    fixture.copyTo(project)
    atomRan = runAtom(project, "slice") match
      case Right(recs) =>
          records = recs
          true
      case Left(_) => false
    if atomRan then
      // the same sources with no installed gems, and with no lock file: no gem to name
      val noGems = workspace / "nogems"
      fixture.copyTo(noGems)
      (noGems / "vendor").delete()
      runAtom(noGems, "nogems").foreach(withoutGems = _)
      val noLock = workspace / "nolock"
      fixture.copyTo(noLock)
      (noLock / "Gemfile.lock").delete()
      runAtom(noLock, "nolock").foreach(withoutLock = _)
  end beforeAll

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

  private def shapes(from: List[Rec] = records): List[Rec] =
    if !atomRan then cancel("the Ruby fixture run failed")
    from.filter(_.file == "shapes.rb")

  private def kind(k: String, from: List[Rec] = records): List[Rec] =
      shapes(from).filter(_.kind == k)

  "atom callsites for Ruby" should {

      "emit exactly the records of every shape in the fixture" in {
          shapes().map(_.copy(file = "")) shouldBe RubyCallSitesTests.expected
      }

      "make a superclass and the argument of include, extend and prepend `extends` records" in {
          kind("extends").map(r => (r.line, r.col, r.module)) shouldBe List(
            (7, 19, "Faraday::Middleware"),
            (8, 12, "Rack::Utils"),
            (10, 11, "Forwardable"),
            (11, 12, "Observable")
          )
      }

      "record a call on a constant at the constant, with the method as path" in {
          kind("call").filter(_.via == "binding").map(r =>
              (r.line, r.col, r.module, r.path)
          ) shouldBe
              List(
                (15, 6, "Faraday::Utils", "escape"),
                (21, 6, "JSON", "parse"),
                (22, 6, "JSON", "generate"),
                (24, 6, "Rails", "root"),
                (39, 6, "Cache", "fetch"),
                (81, 6, "I18n::Backend", "reset"),
                (82, 6, "Missing", "reset")
              )
      }

      "record `X::Y.new` as a new, and every other reference as a ref" in {
          kind("new").map(r => (r.line, r.module)) shouldBe List(
            (16, "Faraday::Connection"),
            (20, "Receipt"),
            (23, "Rack::Request"),
            (25, "Net::HTTP::Get"),
            (58, "Logger"),
            (62, "Logger"),
            (63, "Logger"),
            (66, "Net::HTTP"),
            (69, "Faraday::Connection"),
            (74, "Logger")
          )
          kind("ref").map(r => (r.line, r.col, r.module)) shouldBe List(
            (26, 12, "ArgumentError"),
            (31, 11, "Net::ReadTimeout"),
            (31, 29, "Timeout::Error"),
            (33, 11, "StandardError"),
            (53, 4, "Limit::MAX")
          )
      }

      "find the constant a nested class reaches through its enclosing module" in {
          // `Backend` in `module I18n` is I18n::Backend, which a gem defines; `Missing` is nobody's
          val lexical = shapes().filter(_.line >= 81)
          lexical.map(r => (r.module, r.purl)) shouldBe
              List(("I18n::Backend", "pkg:gem/i18n@1.14.1"), ("Missing", ""))
          // and a name written plainly stays as written
          shapes().filter(_.line == 39).map(_.module) shouldBe List("Cache")
      }

      "find what a project class reaches through a namespace it reopens, a namespaced class name and an index" in {
          // `lib/reopened.rb`: `module Paperclip` only nests classes (a gem's namespace the project
          // reopens), so `Paperclip::Processor` and `Paperclip.options` are the gem's while the
          // project's own `Paperclip::Transcoder.new` (line 19) is not a use; `class
          // Admin::Account` is lowered under a namespace block, `Migration[7.2]` calls `[]` on its
          // constant, `StandardError` is a bundled superclass and `::Thread` an absolute core class
          if !atomRan then cancel("the Ruby fixture run failed")
          records.filter(_.file == "reopened.rb").map(_.copy(file = "")) shouldBe List(
            Rec(2, 21, "Paperclip::Processor", "", "extends", "binding", false, ""),
            Rec(6, 23, "ActiveModel::Serializer", "", "extends", "binding", false, ""),
            Rec(9, 17, "ActiveRecord::Migration", "[]", "call", "binding", false, ""),
            Rec(12, 16, "StandardError", "", "extends", "binding", true, ""),
            Rec(17, 4, "Thread", "current", "call", "binding", true, ""),
            Rec(18, 4, "Paperclip", "options", "call", "binding", false, "")
          )
      }

      "emit no record for a constant the parser makes up while it lowers" in {
          // `lib/reopened.rb` from line 22: `Array(value)` calls a Kernel function, `raise "boom"`
          // raises a StandardError nobody named and `(..10)` has no Float::INFINITY written
          if !atomRan then cancel("the Ruby fixture run failed")
          records.filter(r => r.file == "reopened.rb" && r.line >= 22) shouldBe Nil
      }

      "emit no record for a require line, a project class, an autoloaded constant or a DSL call" in {
          val lines = shapes().map(_.line).toSet
          // the requires, `class Cart`, `include Trackable`, `has_many`, the project's own classes
          // (`Cart`, `Widget`, `Gadget` autoload-implied by its path) and the project's `Cart.find`
          List(1, 2, 5, 9, 12, 17, 18, 19, 38).filter(lines.contains) shouldBe Nil
          records.foreach(r => r.line should be > 0)
      }

      "emit no record for a core class the project reopens" in {
          // `class Array` on line 44 and `Array.new` on line 52
          shapes().filter(r => r.line == 52 || r.line == 44) shouldBe Nil
      }

      "attribute a call to the class of a local bound once to a class with the plain new" in {
          kind("call", records).filter(_.via == "type").map(r =>
              (r.line, r.col, r.module, r.path)
          ) shouldBe
              List((59, 2, "Logger", "info"))
      }

      "not type a setter, a local bound twice, a parameter, a custom new or a gem class" in {
          val lines = shapes().filter(_.via == "type").map(_.line).toSet
          // `log.level = 1`, `again.warn`, `http.get` (Net::HTTP.new is not Class#new),
          // `conn.get` (a gem's class is never proved plain) and the parameter's `log.error`
          List(60, 64, 67, 70, 75).filter(lines.contains) shouldBe Nil
      }

      "mark Ruby's own constants builtin, without a gem" in {
          val builtin = shapes().filter(_.builtin)
          builtin.map(_.module).toSet shouldBe
              Set(
                "Forwardable",
                "JSON",
                "Net::HTTP::Get",
                "ArgumentError",
                "Net::ReadTimeout",
                "Timeout::Error",
                "StandardError",
                "Logger",
                "Net::HTTP"
              )
          builtin.foreach(_.purl shouldBe "")
      }

      "name a gem only when exactly one gem with installed sources defines the constant" in {
          val purls = shapes().filter(_.purl.nonEmpty).map(r => (r.module, r.purl)).toSet
          purls shouldBe Set(
            ("Faraday::Middleware", "pkg:gem/faraday@2.9.0"),
            ("Faraday::Utils", "pkg:gem/faraday@2.9.0"),
            ("Faraday::Connection", "pkg:gem/faraday@2.9.0"),
            ("Rack::Request", "pkg:gem/rack@3.0.8"),
            ("I18n::Backend", "pkg:gem/i18n@1.14.1")
          )
          // `Rack::Utils` is defined by both rack and rack-compat: no gem
          shapes().filter(_.module == "Rack::Utils").map(_.purl) shouldBe List("")
      }

      "name no gem without installed sources, whatever the constant looks like, and keep every record" in {
          val bare = shapes(withoutGems)
          // without the gem's sources `Backend` in `module I18n` stays as written
          val expectedBare = RubyCallSitesTests.expected.map(r =>
              if r.module == "I18n::Backend" then r.copy(module = "Backend") else r
          )
          bare.map(r => (r.line, r.col, r.module, r.path, r.kind, r.via, r.builtin)) shouldBe
              expectedBare.map(r => (r.line, r.col, r.module, r.path, r.kind, r.via, r.builtin))
          bare.foreach(_.purl shouldBe "")
      }

      "name no gem without a lock file" in {
          shapes(withoutLock).map(r => (r.line, r.module)).toSet.contains(
            (15, "Faraday::Utils")
          ) shouldBe true
          shapes(withoutLock).foreach(_.purl shouldBe "")
      }
  }

  "RubyGemMap.parseLock" should {
      "read the gems of the GEM, PATH and GIT sections, without a platform in the version" in {
          val lock =
              """GIT
                |  remote: https://example.com/x.git
                |  specs:
                |    pinned (0.1.0)
                |
                |GEM
                |  remote: https://rubygems.org/
                |  specs:
                |    faraday (2.9.0)
                |      faraday-net_http (>= 2.0)
                |    nokogiri (1.16.0-x86_64-linux)
                |
                |PLATFORMS
                |  ruby
                |
                |DEPENDENCIES
                |  faraday
                |""".stripMargin
          RubyGemMap.parseLock(lock).map(g => (g.name, g.version)) shouldBe
              List(("pinned", "0.1.0"), ("faraday", "2.9.0"), ("nokogiri", "1.16.0"))
      }
  }
end RubyCallSitesTests

object RubyCallSitesTests:

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

  private val faraday = "pkg:gem/faraday@2.9.0"

  // format: off
  val expected: List[Rec] = List(
    Rec(7, 19, "Faraday::Middleware", "", "extends", "binding", false, faraday),
    Rec(8, 12, "Rack::Utils", "", "extends", "binding", false, ""),
    Rec(10, 11, "Forwardable", "", "extends", "binding", true, ""),
    Rec(11, 12, "Observable", "", "extends", "binding", false, ""),
    Rec(15, 6, "Faraday::Utils", "escape", "call", "binding", false, faraday),
    Rec(16, 6, "Faraday::Connection", "new", "new", "binding", false, faraday),
    Rec(20, 6, "Receipt", "new", "new", "binding", false, ""),
    Rec(21, 6, "JSON", "parse", "call", "binding", true, ""),
    Rec(22, 6, "JSON", "generate", "call", "binding", true, ""),
    Rec(23, 6, "Rack::Request", "new", "new", "binding", false, "pkg:gem/rack@3.0.8"),
    Rec(24, 6, "Rails", "root", "call", "binding", false, ""),
    Rec(25, 6, "Net::HTTP::Get", "new", "new", "binding", true, ""),
    Rec(26, 12, "ArgumentError", "", "ref", "binding", true, ""),
    Rec(31, 11, "Net::ReadTimeout", "", "ref", "binding", true, ""),
    Rec(31, 29, "Timeout::Error", "", "ref", "binding", true, ""),
    Rec(33, 11, "StandardError", "", "ref", "binding", true, ""),
    Rec(39, 6, "Cache", "fetch", "call", "binding", false, ""),
    Rec(53, 4, "Limit::MAX", "", "ref", "binding", false, ""),
    Rec(58, 8, "Logger", "new", "new", "binding", true, ""),
    Rec(59, 2, "Logger", "info", "call", "type", true, ""),
    Rec(62, 10, "Logger", "new", "new", "binding", true, ""),
    Rec(63, 10, "Logger", "new", "new", "binding", true, ""),
    Rec(66, 9, "Net::HTTP", "new", "new", "binding", true, ""),
    Rec(69, 9, "Faraday::Connection", "new", "new", "binding", false, faraday),
    Rec(74, 8, "Logger", "new", "new", "binding", true, ""),
    Rec(81, 6, "I18n::Backend", "reset", "call", "binding", false, "pkg:gem/i18n@1.14.1"),
    Rec(82, 6, "Missing", "reset", "call", "binding", false, "")
  )
  // format: on
end RubyCallSitesTests
