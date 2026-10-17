package io.appthreat.atom

import better.files.File
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The default C/C++ ignore pattern (`.*(docs|example|samples|mocks|Documentation|demos).*`)
  * filters every file whose path contains one of those words. When it filters out EVERY unit a
  * compile database lists, the analysis would see an empty project silently: atom says so, naming
  * the pattern and the override.
  */
class IgnoredUnitsWarningTests extends AnyWordSpec with Matchers:

  private def capturedStderr(body: => Unit): String =
    val original = System.err
    val buffer   = new java.io.ByteArrayOutputStream()
    System.setErr(new java.io.PrintStream(buffer, true, "UTF-8"))
    try body
    finally System.setErr(original)
    new String(buffer.toByteArray, java.nio.charset.StandardCharsets.UTF_8)

  /** Runs memory-safety over `root` with a one-unit database for `unit`, and returns stderr. */
  private def warningFor(dir: File, root: File, unit: File, extra: String*): String =
    unit.parent.createDirectories()
    unit.writeText("int main(void) { return 0; }\n")
    val db = dir / "compile_commands.json"
    db.writeText(
      s"""[{"directory": "${unit.parent.pathAsString}", "command": "cc -c ${unit
              .name}", "file": "${unit.pathAsString}"}]"""
    )
    capturedStderr {
        Atom.run(
          Array(
            "memory-safety",
            "-l",
            "c",
            "--cache",
            "none",
            "--compile-commands",
            db.pathAsString,
            "-o",
            (dir / "app.atom").pathAsString,
            "-s",
            (dir / "findings.json").pathAsString
          ) ++ extra ++ Array(root.pathAsString)
        )
    }
  end warningFor

  private val warning = "filtered out by the default C/C++ ignore pattern"

  "a compile database whose every unit sits under an ignored directory of the input" should {
      "warn, naming the pattern and the option that replaces it" in {
          File.usingTemporaryDirectory("atom-ignored-units") { dir =>
            val stderr = warningFor(dir, dir, dir / "examples" / "prog" / "main.c")
            stderr should include(warning)
            stderr should include("--exclude-regex")
          }
      }
  }

  "an input root that is itself inside such a directory" should {
      "not warn: the pattern is matched against paths relative to the root" in {
          File.usingTemporaryDirectory("atom-root-inside") { dir =>
            val root = dir / "examples" / "prog"
            (warningFor(dir, root, root / "main.c") should not).include(warning)
          }
      }
  }

  "an ignore pattern of the user's own" should {
      "replace the default, with nothing to warn about" in {
          File.usingTemporaryDirectory("atom-own-pattern") { dir =>
            val stderr = warningFor(
              dir,
              dir,
              dir / "examples" / "prog" / "main.c",
              "--exclude-regex",
              ".*vendor.*"
            )
            (stderr should not).include(warning)
          }
      }
  }
end IgnoredUnitsWarningTests
