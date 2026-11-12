package io.appthreat.atom

import better.files.File
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The C default ignore pattern matches path segments under the input root, never the input root's
  * own name (an input someone called `demos` is the whole project, not a docs folder of it), and
  * whenever it removes files atom says how many. With a compilation database and
  * `--compile-commands-only`, a project header's function bodies are sliced on both frontends: a
  * header is where a project defines many of its functions (leveldb's `port/port_stdcxx.h`), and
  * parsing it as its own unit is what makes them visible.
  */
class CHeaderBodiesAndIgnorePatternTests extends AnyWordSpec with Matchers:

  private def capturedStderr(body: => Unit): String =
    val original = System.err
    val buffer   = new java.io.ByteArrayOutputStream()
    System.setErr(new java.io.PrintStream(buffer, true, "UTF-8"))
    try body
    finally System.setErr(original)
    new String(buffer.toByteArray, java.nio.charset.StandardCharsets.UTF_8)

  private def writeLibraryProject(dir: File): Unit =
    val library = dir / "externlib"
    (library / "libdir").createDirectories()
    (library / "libdir" / "lib.h").write(
      """#ifndef LIB_H
          |#define LIB_H
          |int lib_func(int x);
          |#endif
          |""".stripMargin
    )
    val project = dir / "proj"
    project.createDirectories()
    (project / "proj_hdr.h").write(
      """#ifndef PROJ_HDR_H
          |#define PROJ_HDR_H
          |#include "libdir/lib.h"
          |static inline int proj_wrapped(int x) { return lib_func(x) + 1; }
          |#endif
          |""".stripMargin
    )
    (project / "main.c").write(
      """#include "proj_hdr.h"
          |int main(void) { return proj_wrapped(1); }
          |""".stripMargin
    )
    def quoted(f: File): String = f.pathAsString.replace("\\", "\\\\")
    (dir / "compile_commands.json").writeText(
      s"""[{"directory": "${quoted(project)}", "command": "cc -c -I${quoted(library)} main.c",
          |"file": "${quoted(project / "main.c")}"}]""".stripMargin
    )
  end writeLibraryProject

  private def runUsages(dir: File, atomName: String, extra: String*): (List[String], String) =
    val atomFile = dir / atomName
    val slice    = dir / s"$atomName.json"
    val stderr = capturedStderr {
        val result = Atom.run(
          Array(
            "usages",
            "-l",
            "c",
            "--cache",
            "none",
            "--compile-commands",
            (dir / "compile_commands.json").pathAsString
          ) ++ extra ++ Array(
            "-o",
            atomFile.pathAsString,
            "-s",
            slice.pathAsString,
            (dir / "proj").pathAsString
          )
        )
        result.isRight shouldBe true
    }
    val odb = overflowdb.Config.withDefaults().withStorageLocation(atomFile.pathAsString)
    val cpg = CpgLoader.loadFromOverflowDb(
      CpgLoaderConfig().withOverflowConfig(odb).doNotCreateIndexesOnLoad
    )
    try (cpg.method.filename(".*proj_hdr.h").name.l, stderr)
    finally cpg.close()
  end runUsages

  "an input directory whose own name is an ignore word" should {
      "still be analysed whole, with no warning" in {
          File.usingTemporaryDirectory("atom-demos-input") { dir =>
            writeLibraryProject(dir)
            // the input the analysis names is the project itself, called `demos`
            (dir / "proj" / "keep.c").write(
              "#include \"proj_hdr.h\"\nint keep(void) { return proj_wrapped(2); }\n"
            )
            val (methods, stderr) = runUsages(dir, "demos.atom")
            // the header's body is sliced and the project's files are all there
            methods should contain("proj_wrapped")
            (stderr should not).include("drops")
          }
      }
  }

  "a docs directory under the input" should {
      "be dropped, with a warning that says how many files" in {
          File.usingTemporaryDirectory("atom-docs-under") { dir =>
            writeLibraryProject(dir)
            // a header the compiled unit includes, which a database run parses as the project's
            // own unless the pattern drops it
            (dir / "proj" / "docs").createDirectories()
            (dir / "proj" / "docs" / "guide.h").write("int guide(void);\n")
            (dir / "proj" / "main.c").write(
              "#include \"proj_hdr.h\"\n#include \"docs/guide.h\"\nint main(void) { return proj_wrapped(1); }\n"
            )
            val (methods, stderr) = runUsages(dir, "docs.atom")
            stderr should include("drops 1 of 3 source and header files")
            stderr should include("--exclude-regex")
            // the project's own files are untouched
            methods should contain("proj_wrapped")
          }
      }
  }

  "a project header's function bodies with --compile-commands-only" should {
      "be sliced through the CDT frontend" in {
          File.usingTemporaryDirectory("atom-hdr-bodies") { dir =>
            writeLibraryProject(dir)
            val (methods, _) = runUsages(dir, "bodies.atom", "--compile-commands-only")
            methods should contain("proj_wrapped")
          }
      }
      "be sliced through the EDG frontend" in {
          assume(
            sys.env.get("EDGA_PATH").orElse(sys.props.get("edga.path")).exists { p =>
                File(p).isRegularFile
            },
            "edga is not installed (set EDGA_PATH)"
          )
          File.usingTemporaryDirectory("atom-hdr-bodies-edg") { dir =>
            writeLibraryProject(dir)
            val (methods, _) = runUsages(
              dir,
              "bodies-edg.atom",
              "--compile-commands-only",
              "--c-frontend",
              "edg",
              "--edga-path",
              sys.env.getOrElse("EDGA_PATH", "edga")
            )
            methods should contain("proj_wrapped")
          }
      }
  }
end CHeaderBodiesAndIgnorePatternTests
