package io.appthreat.atom

import better.files.File
import io.circe.Json
import io.circe.parser.parse
import io.circe.syntax.EncoderOps
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.{ByteArrayOutputStream, PrintStream}

/** `memory-safety --reuse-atom` over an atom another command built: the overlay's passes run over
  * the loaded graph, in memory, following the current run's options, and the atom file on disk is
  * not rewritten. An atom whose language is not C or C++ reports zero findings, as it always did:
  * the overlay has nothing to tag there.
  */
class MemorySafetyReuseAtomTests extends AnyWordSpec with Matchers:

  /** Findings from several rule families, so the comparison is not one rule's accident. */
  private val cSource =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |void unbounded_copy(unsigned char *buf, const unsigned char *src, int n)
        |{
        |    memcpy(buf, src, n);
        |}
        |
        |void leaks(void)
        |{
        |    char *p = (char *)malloc(64);
        |    p[0] = 'x';
        |}
        |
        |void double_free(void)
        |{
        |    char *p = (char *)malloc(32);
        |    free(p);
        |    free(p);
        |}
        |""".stripMargin

  private def cProject(dir: File): File =
    val src = (dir / "src").createDirectory()
    (src / "main.c").writeText(cSource)
    src

  private def run(args: String*): Either[String, String] =
      Atom.run(args.toArray)

  private def findingsOf(file: File): List[Json] =
      parse(file.contentAsString).toOption.flatMap(_.asArray).toList.flatten

  /** The finding as a record without the atom path, which differs between the two builds. */
  private def withoutAtom(f: Json): Json =
      f.asObject.map(_.remove("atom").asJson).getOrElse(f)

  private def places(findings: List[Json]): Set[(String, String, Int)] =
      findings.flatMap { f =>
          for
            rule <- f.hcursor.get[String]("rule").toOption
            file <- f.hcursor.get[String]("file").toOption
            line <- f.hcursor.get[Int]("line").toOption
          yield (rule, file, line)
      }.toSet

  /** Captures stderr, where the memory-api options warning is printed. */
  private def captureErr[A](body: => A): String =
    val original = System.err
    val captured = new ByteArrayOutputStream()
    System.setErr(new PrintStream(captured, true, "UTF-8"))
    try body
    finally System.setErr(original)
    captured.toString("UTF-8")

  "memory-safety with a reused atom" should {

      "render the same findings as a fresh build from an atom the usages command built, without rewriting it" in {
          File.usingTemporaryDirectory("atom-ms-reuse") { dir =>
            val src = cProject(dir)
            // an atom built by another command, with the data dependencies the overlay's rules
            // read, so the flows can be identical too
            val usagesAtom   = dir / "usages.atom"
            val usagesSlices = dir / "usages.json"
            run(
              "usages",
              "--with-data-deps",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              usagesAtom.pathAsString,
              "-s",
              usagesSlices.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val bytesBefore = usagesAtom.byteArray

            // a fresh memory-safety build of the same source
            val freshAtom         = dir / "fresh.atom"
            val freshFindingsFile = dir / "fresh-findings.json"
            run(
              "memory-safety",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              freshAtom.pathAsString,
              "-s",
              freshFindingsFile.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val fresh = findingsOf(freshFindingsFile)
            fresh should not be empty

            // the reused run: the atom carries no overlay marker, so the overlay runs over the
            // loaded graph, in memory, with this run's options
            val reusedFindingsFile = dir / "reused-findings.json"
            run(
              "memory-safety",
              "--reuse-atom",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              usagesAtom.pathAsString,
              "-s",
              reusedFindingsFile.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val reused = findingsOf(reusedFindingsFile)

            places(reused) shouldBe places(fresh)
            reused.map(withoutAtom) shouldBe fresh.map(withoutAtom)

            // the atom file on disk is not rewritten: the overlay ran in memory only
            usagesAtom.byteArray.sameElements(bytesBefore) shouldBe true
          }
      }

      "build the data-flow layer for an atom built without data dependencies, and leave no copy behind" in {
          // the overlay's guard and origin facts read REACHING_DEF edges: over an atom without
          // them the guarded copy below would be reported, where a fresh build stays silent
          File.usingTemporaryDirectory("atom-ms-reuse-nodeps") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "guard.c").writeText(
              """#include <string.h>
                |
                |void guarded(unsigned char *buf, const unsigned char *src, int n)
                |{
                |    if (n > 16) return;
                |    memcpy(buf, src, n);
                |}
                |
                |void unguarded(unsigned char *buf, const unsigned char *src, int n)
                |{
                |    memcpy(buf, src, n);
                |}
                |""".stripMargin
            )
            val atom = dir / "plain.atom"
            run(
              "usages",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              atom.pathAsString,
              "-s",
              (dir / "usages.json").pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val freshFindings = dir / "fresh.json"
            run(
              "memory-safety",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              (dir / "fresh.atom").pathAsString,
              "-s",
              freshFindings.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val reusedFindings = dir / "reused.json"
            run(
              "memory-safety",
              "--reuse-atom",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              atom.pathAsString,
              "-s",
              reusedFindings.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val fresh = findingsOf(freshFindings)
            val freshFunctions = fresh.flatMap(_.hcursor.get[String]("function").toOption)
            freshFunctions should contain("unguarded")
            (freshFunctions should not).contain("guarded")
            findingsOf(reusedFindings).map(withoutAtom) shouldBe fresh.map(withoutAtom)
            // the scratch copy the overlay ran in is gone once the command returns
            dir.list.map(_.name).filter(_.startsWith(".plain.atom")).toList shouldBe empty
          }
      }

      "report zero findings for a non-C reused atom, as it always did" in {
          File.usingTemporaryDirectory("atom-ms-reuse-py") { dir =>
            val src = (dir / "src").createDirectory()
            (src / "app.py").writeText("def run(x):\n    return x + 1\n")
            val atom   = dir / "app.atom"
            val slices = dir / "usages.json"
            run(
              "usages",
              "-l",
              "python",
              "--no-ast-cache",
              "-o",
              atom.pathAsString,
              "-s",
              slices.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val out = dir / "findings.json"
            run(
              "memory-safety",
              "--reuse-atom",
              "-l",
              "python",
              "--no-ast-cache",
              "-o",
              atom.pathAsString,
              "-s",
              out.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            findingsOf(out) shouldBe empty
          }
      }

      "warn about memory-api options only for a reused atom that already carries the overlay marker" in {
          File.usingTemporaryDirectory("atom-ms-reuse-warn") { dir =>
            val src       = cProject(dir)
            val atom      = dir / "marked.atom"
            val firstFind = dir / "first.json"
            run(
              "memory-safety",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              atom.pathAsString,
              "-s",
              firstFind.pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            // the marker-carrying atom consumed its build-time options: this run's are ignored
            // with the warning
            val warned = captureErr {
                run(
                  "memory-safety",
                  "--reuse-atom",
                  "--memory-api-profiles",
                  "none",
                  "-l",
                  "c",
                  "--no-ast-cache",
                  "-o",
                  atom.pathAsString,
                  "-s",
                  (dir / "again.json").pathAsString,
                  src.pathAsString
                )
            }
            warned should include("warn: --memory-api-profiles and --memory-api-config")

            // an atom without the marker runs the overlay now, with the current options, so the
            // same option prints no warning
            val usagesAtom = dir / "plain.atom"
            run(
              "usages",
              "-l",
              "c",
              "--no-ast-cache",
              "-o",
              usagesAtom.pathAsString,
              "-s",
              (dir / "usages.json").pathAsString,
              src.pathAsString
            ) shouldBe a[Right[?, ?]]
            val quiet = captureErr {
                run(
                  "memory-safety",
                  "--reuse-atom",
                  "--memory-api-profiles",
                  "none",
                  "-l",
                  "c",
                  "--no-ast-cache",
                  "-o",
                  usagesAtom.pathAsString,
                  "-s",
                  (dir / "plain-findings.json").pathAsString,
                  src.pathAsString
                )
            }
            (quiet should not).include("warn: --memory-api-profiles and --memory-api-config")
          }
      }
  }
end MemorySafetyReuseAtomTests
