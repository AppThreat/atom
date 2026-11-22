package io.appthreat.atom

import better.files.File as BFile
import io.circe.generic.auto.*
import io.circe.parser.decode
import org.scalatest.{AppendedClues, BeforeAndAfterAll, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Paths

/** A C or C++ callsites record whose module is a header also names the file the include of that
  * header resolved to (`resolvedPath`) and whether it was written as a system include (`isSystem`),
  * the same tags the usages slice reads for an include's entry, so the two slices say the same
  * thing about one include. The library lives outside the analysed input, reached through
  * `--include-path`, so a resolved path is absolute. A header that reached the record's file only
  * through another header carries neither field: the file's own includes do not name it.
  */
class CCallSiteResolvedPathTests extends AnyWordSpec with Matchers with AppendedClues
    with BeforeAndAfterAll with OptionValues:

  import CCallSiteResolvedPathTests.*

  private var workspace: BFile             = scala.compiletime.uninitialized
  private var cdt: List[Record]            = Nil
  private var edg: Option[List[Record]]    = None
  private var cdtUsages: List[ImportEntry] = Nil

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomCCallsitesInc")
    val fixture = BFile(getClass.getResource("/c-callsites-includes").toURI)
    (fixture / "proj").copyTo(workspace / "proj")
    (fixture / "externlib").copyTo(workspace / "externlib")
    cdt = runCallsites("cdt", extra = Seq("--include-path", library.pathAsString))
    cdtUsages = runUsages("cdt", extra = Seq("--include-path", library.pathAsString))
    if edgaAvailable then
      edg = Some(
        runCallsites(
          "edg",
          Seq(
            "--include-path",
            library.pathAsString,
            "--c-frontend",
            "edg",
            "--edga-path",
            edgaPath
          )
        )
      )
  end beforeAll

  /** Where edga is, from the environment or the `edga.path` property a run without the environment
    * variable can set for the forked test JVM.
    */
  private def edgaPath: String =
      sys.env.getOrElse("EDGA_PATH", sys.props.getOrElse("edga.path", "edga"))

  private def library: BFile = workspace / "externlib"

  private def runCallsites(frontend: String, extra: Seq[String]): List[Record] =
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
      s => s.callsites
    )

  /** The usages slice of the same fixture (`--also-callsites` builds the atom once for both), whose
    * include entries are the other reader of the same tags.
    */
  private def runUsages(frontend: String, extra: Seq[String]): List[ImportEntry] =
    val usages = workspace / s"$frontend-usages.json"
    val result = Atom.run(
      ("usages" :: "--cache" :: "none" :: "-l" :: "c" :: extra.toList ++ List(
        "--also-callsites",
        (workspace / s"$frontend-also-callsites.json").pathAsString,
        "-o",
        (workspace / s"$frontend-usages.atom").pathAsString,
        "-s",
        usages.pathAsString,
        (workspace / "proj").pathAsString
      )).toArray
    )
    (result.isRight shouldBe true).withClue(s"the $frontend usages run failed: $result")
    decode[Usages](usages.contentAsString).fold(
      e => fail(s"the $frontend usages slice is not JSON: $e"),
      s => s.objectSlices
    )

  private def edgaAvailable: Boolean =
      sys.env.get("EDGA_PATH").orElse(sys.props.get("edga.path")).exists { path =>
          BFile(path).isRegularFile
      } || scala.util.Try(
        scala.sys.process.Process(Seq("which", "edga")).!!.trim.nonEmpty
      ).getOrElse(false)

  private def recordOf(module: String, path: String): Record =
    val ofModule = cdt.filter(r => r.module == module && r.path == path)
    withClue(s"the records of module $module: $ofModule: ")(ofModule should have size 1)
    ofModule.head

  "the C callsites slice" should {

      "write the records this fixture is, and nothing else" in {
          cdt.map(r => (r.file, r.module, r.path)).toSet shouldBe Set(
            ("main.c", "exth.h", "ext_func"),
            ("main.c", "exth2.h", "ext2_func"),
            ("main.c", "other.h", "other_func")
          )
      }

      "name the absolute file a system include of an outside include path resolved to" in {
          val rec      = recordOf("exth.h", "ext_func")
          val resolved = rec.resolvedPath.value.replace('\\', '/')
          resolved should endWith("/externlib/extdir/exth.h")
          Paths.get(rec.resolvedPath.value).isAbsolute shouldBe true
          rec.isSystem shouldBe Some(true)
      }

      "name the file a quoted include resolved to, with no system flag" in {
          val rec = recordOf("exth2.h", "ext2_func")
          rec.resolvedPath.value.replace('\\', '/') should endWith("/externlib/extdir/exth2.h")
          rec.isSystem shouldBe None
      }

      "leave both fields off a header the file includes only through another header" in {
          val rec = recordOf("other.h", "other_func")
          rec.resolvedPath shouldBe None
          rec.isSystem shouldBe None
      }

      "carry what the usages slice's entry for the same include carries" in {
          // one entry per include of main.c that resolved, three includes the file spells itself
          val entries = cdtUsages.filter(e => e.fileName == "main.c" && e.resolvedPath.isDefined)
          entries.map(_.fullName).toSet shouldBe Set("extdir/exth.h", "extdir/exth2.h", "wrap.h")
          cdt.filter(r => r.resolvedPath.isDefined).foreach { rec =>
            val ofPath = entries.filter(e => e.resolvedPath == rec.resolvedPath)
            withClue(s"the usages entries for ${rec.resolvedPath}: $ofPath: ")(
              ofPath should have size 1
            )
            ofPath.head.isSystem shouldBe rec.isSystem
          }
      }
  }

  "the C callsites slice through the EDG frontend" should {

      "carry the same resolved paths and system flags" in {
          assume(edg.isDefined, "edga is not installed (set EDGA_PATH)")
          edg.get.map(r => (r.file, r.module, r.path, r.resolvedPath, r.isSystem)) shouldBe
              cdt.map(r => (r.file, r.module, r.path, r.resolvedPath, r.isSystem))
      }
  }

  override protected def afterAll(): Unit =
      workspace.delete(swallowIOExceptions = true)
end CCallSiteResolvedPathTests

object CCallSiteResolvedPathTests:
  final case class Record(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean,
    resolvedPath: Option[String] = None,
    isSystem: Option[Boolean] = None
  )

  final case class Slice(callsites: List[Record])

  /** The fields of a usages entry an include writes (an entry without `resolvedPath` is a
    * method's).
    */
  final case class ImportEntry(
    fullName: String,
    fileName: String,
    resolvedPath: Option[String] = None,
    isSystem: Option[Boolean] = None
  )

  final case class Usages(objectSlices: List[ImportEntry])
end CCallSiteResolvedPathTests
