package io.appthreat.atom

import better.files.File as BFile
import io.circe.parser.decode
import io.circe.generic.auto.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.AppendedClues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `atom callsites` over a C fixture: one record per external use site, asserted exactly on (file,
  * line, module, path, kind, via, builtin). The expected records are the clang oracle's rows for
  * the same fixture (`oracle_c.py <proj> --compile-commands <db> --cpp`, with the library outside
  * the project and `-I externlib`), so the fixture and this suite agree with the ground truth the
  * slice is scored against, not with a remembered list.
  *
  * The library lives outside the analysed input, reached through `--include-path`, as a real
  * dependency is. The bad twins must produce nothing: the project's own same-named function, its
  * own global, enumerator and struct member, a project macro's invocation, and every expansion of a
  * project macro that wraps a library call (the one record is at the macro's own definition).
  */
class CCallSitesTests extends AnyWordSpec with Matchers with AppendedClues with BeforeAndAfterAll:

  import CCallSitesTests.*

  private var workspace: BFile            = scala.compiletime.uninitialized
  private var cdt: List[Expected]         = Nil
  private var edg: Option[List[Expected]] = None

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomCCallsites")
    val fixture = BFile(getClass.getResource("/c-callsites").toURI)
    val project = workspace / "proj"
    (fixture / "proj").copyTo(project)
    val library = workspace / "externlib"
    (fixture / "externlib").copyTo(library)
    cdt = run("cdt", extra = Seq("--include-path", library.pathAsString))
    if edgaAvailable then
      edg = Some(
        run(
          "edg",
          Seq(
            "--include-path",
            library.pathAsString,
            "--c-frontend",
            "edg",
            "--edga-path",
            sys.env.getOrElse("EDGA_PATH", "edga")
          )
        )
      )
  end beforeAll

  private def run(frontend: String, extra: Seq[String]): List[Expected] =
    val atomFile = workspace / s"$frontend.atom"
    val slice    = workspace / s"$frontend-callsites.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: "c" :: extra.toList :::
          "-o" :: atomFile.pathAsString :: "-s" :: slice.pathAsString :: List(
            (workspace / "proj").pathAsString
          )).toArray
    )
    (result.isRight shouldBe true).withClue(s"the $frontend run failed: $result")
    decode[Slice](slice.contentAsString).fold(
      e => fail(s"the $frontend slice is not JSON: $e"),
      s =>
          s.callsites.map(r => Expected(r.file, r.line, r.module, r.path, r.kind, r.via, r.builtin))
    )

  private def edgaAvailable: Boolean =
      sys.env.get("EDGA_PATH").orElse(sys.props.get("edga.path")).exists { path =>
          BFile(path).isRegularFile
      } || scala.util.Try(
        scala.sys.process.Process(Seq("which", "edga")).!!.trim.nonEmpty
      ).getOrElse(false)

  private val oracleRows = List(
    // klass.cpp: a library class's constructor, a member call through a project subclass of it,
    // a member through a pointer, a `new`, and another member call
    Expected("klass.cpp", 4, "libhpp.h", "LibClass", "new", "binding", builtin = false),
    Expected("klass.cpp", 7, "libhpp.h", "value", "call", "type", builtin = false),
    Expected("klass.cpp", 8, "libhpp.h", "mem", "type", "type", builtin = false),
    Expected("klass.cpp", 9, "libhpp.h", "LibClass", "new", "binding", builtin = false),
    Expected("klass.cpp", 10, "libhpp.h", "value", "call", "type", builtin = false),
    // main.c: object-like and function-like macros, a nested one in another's definition and in an
    // argument, a global, an enumerator, a function as a value, a call, and struct members
    Expected("main.c", 5, "lib.h", "LIB_VERSION", "macro", "macro", builtin = false),
    Expected("main.c", 6, "lib.h", "LIB_MAX", "macro", "macro", builtin = false),
    Expected("main.c", 7, "lib.h", "LIB_WRAP", "macro", "macro", builtin = false),
    Expected("main.c", 9, "lib.h", "lib_global", "ref", "binding", builtin = false),
    Expected("main.c", 10, "lib.h", "LIB_BAD", "ref", "binding", builtin = false),
    Expected("main.c", 14, "lib.h", "field_a", "type", "type", builtin = false),
    Expected("main.c", 17, "lib.h", "lib_func", "ref", "binding", builtin = false),
    Expected("main.c", 18, "lib.h", "lib_func", "call", "binding", builtin = false),
    Expected("main.c", 19, "lib.h", "field_a", "type", "type", builtin = false),
    Expected("main.c", 20, "lib.h", "LIB_INNER", "macro", "macro", builtin = false),
    Expected("main.c", 20, "lib.h", "LIB_OUTER", "macro", "macro", builtin = false),
    Expected("main.c", 21, "lib.h", "LIB_MAX", "macro", "macro", builtin = false),
    Expected("main.c", 21, "lib.h", "LIB_VERSION", "macro", "macro", builtin = false),
    Expected("main.c", 23, "lib.h", "LIB_MAX", "macro", "macro", builtin = false),
    // a library call written in a variadic project macro's arguments is the file's own, at its line
    Expected("main.c", 29, "lib.h", "lib_func", "call", "binding", builtin = false),
    // proj.h: the project macro that wraps a library call - one record at its definition
    Expected("proj.h", 4, "lib.h", "lib_func", "call", "binding", builtin = false)
  )

  "the C callsites slice through the CDT frontend" should {
      "emit the oracle's records exactly, and nothing else" in {
          cdt shouldBe oracleRows
      }
  }

  "the C callsites slice through the EDG frontend" should {
      "emit the oracle's records exactly, and nothing else" in {
          assume(edg.isDefined, "edga is not installed (set EDGA_PATH)")
          edg.get shouldBe oracleRows
      }
  }

  "no project twin" should {
      "produce a record" in {
          // both frontends' records, as one set: they emit the same sites
          val records = (cdt ++ edg.getOrElse(Nil)).distinct
          // the project's own function with the library's name
          records.filter(r => r.path == "same_name") shouldBe empty
          // its own global, enumerator and struct member
          records.filter(r => r.path == "proj_global" || r.path == "PROJ_ONE") shouldBe empty
          records.filter(r => r.path == "field_a" && r.module == "proj.h") shouldBe empty
          // a project macro's invocation, at its site or anywhere
          records.filter(r => r.path == "PROJ_DOUBLE" || r.path == "PROJ_CALL") shouldBe empty
          // the library call a project macro wraps: one record at the macro's definition, none at
          // its expansion (main.c:22), beside the plain call the file itself spells (main.c:18)
          val wrapped = records.filter(r => r.path == "lib_func" && r.kind == "call")
          wrapped shouldBe List(
            Expected("main.c", 18, "lib.h", "lib_func", "call", "binding", builtin = false),
            Expected("main.c", 29, "lib.h", "lib_func", "call", "binding", builtin = false),
            Expected("proj.h", 4, "lib.h", "lib_func", "call", "binding", builtin = false)
          )
          // the project's own classes: no constructor, member or new of theirs
          records.filter(r => r.path == "ProjClass" || r.path == "ProjSub") shouldBe empty
          records.filter(r => r.path == "value" && r.module == "proj.h") shouldBe empty
      }
  }

  override protected def afterAll(): Unit =
      workspace.delete(swallowIOExceptions = true)
end CCallSitesTests

object CCallSitesTests:
  final case class Expected(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean
  )

  final case class Record(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean
  )

  final case class Slice(callsites: List[Record])
end CCallSitesTests
