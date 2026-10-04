package io.appthreat.atom

import better.files.File
import io.appthreat.edg2atom.parser.EdgaRunner
import io.circe.parser.parse
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.Config as OdbConfig

import scala.jdk.CollectionConverters.*

/** The commands that read a C/C++ graph work with every `--c-frontend`: `memory-safety`, `usages`,
  * `reachables` and `--with-data-deps`. The EDG frontends need edga; their cases are cancelled
  * without it.
  */
class CFrontendWorkflowTests extends AnyWordSpec with Matchers:

  private val source =
      """#include <stdlib.h>
        |#include <string.h>
        |
        |char *copy_name(const char *name) {
        |  char *buf = malloc(strlen(name) + 1);
        |  strcpy(buf, name);
        |  return buf;
        |}
        |
        |void release_twice(void) {
        |  char *p = malloc(16);
        |  free(p);
        |  free(p);
        |}
        |
        |int main(int argc, char **argv) {
        |  char *n = copy_name(argv[0]);
        |  free(n);
        |  release_twice();
        |  return 0;
        |}
        |""".stripMargin

  private def project(test: (File, File) => Unit): Unit =
      File.usingTemporaryDirectory("atom-c-frontend") { dir =>
        val src = (dir / "src").createDirectory()
        (src / "main.c").writeText(source)
        test(dir, src)
      }

  private def atom(args: String*): Either[String, String] = Atom.run(args.toArray)

  private def dataDependencyEdges(atomFile: File): Int =
    val odb = OdbConfig.withoutOverflow().withStorageLocation(atomFile.pathAsString)
    val cpg = CpgLoader.loadFromOverflowDb(
      CpgLoaderConfig().withOverflowConfig(odb).doNotCreateIndexesOnLoad
    )
    try cpg.graph.edges(EdgeTypes.REACHING_DEF).asScala.size
    finally cpg.close()

  Seq("cdt", "edg", "edg-fallback").foreach { frontend =>
      s"--c-frontend $frontend" should {
          def requireFrontend(): Unit =
              if frontend != "cdt" then
                assume(EdgaRunner.locate().isDefined, "edga is not installed")

          "find the double free with memory-safety" in {
              requireFrontend()
              project { (dir, src) =>
                val findings = dir / "findings.json"
                atom(
                  "memory-safety",
                  "-l",
                  "c",
                  "--c-frontend",
                  frontend,
                  "--cache",
                  "none",
                  "-o",
                  (dir / "app.atom").pathAsString,
                  "-s",
                  findings.pathAsString,
                  src.pathAsString
                ).isRight shouldBe true
                val kinds =
                    parse(findings.contentAsString).toOption.flatMap(_.asArray).toList.flatten
                        .flatMap(f => f.hcursor.get[String]("kind").toOption)
                kinds should contain("double-free")
              }
          }

          "slice usages" in {
              requireFrontend()
              project { (dir, src) =>
                val slices = dir / "usages.json"
                atom(
                  "usages",
                  "-l",
                  "c",
                  "--c-frontend",
                  frontend,
                  "--cache",
                  "none",
                  "-o",
                  (dir / "app.atom").pathAsString,
                  "-s",
                  slices.pathAsString,
                  src.pathAsString
                ).isRight shouldBe true
                slices.contentAsString should include("copy_name")
              }
          }

          "slice reachables" in {
              requireFrontend()
              project { (dir, src) =>
                val slices = dir / "reachables.json"
                atom(
                  "reachables",
                  "-l",
                  "c",
                  "--c-frontend",
                  frontend,
                  "--cache",
                  "none",
                  "-o",
                  (dir / "app.atom").pathAsString,
                  "-s",
                  slices.pathAsString,
                  src.pathAsString
                ).isRight shouldBe true
                slices.exists shouldBe true
              }
          }

          "write data dependencies" in {
              requireFrontend()
              project { (dir, src) =>
                val atomFile = dir / "app.atom"
                atom(
                  "-l",
                  "c",
                  "--c-frontend",
                  frontend,
                  "--with-data-deps",
                  "--cache",
                  "none",
                  "-o",
                  atomFile.pathAsString,
                  src.pathAsString
                ).isRight shouldBe true
                dataDependencyEdges(atomFile) should be > 0
              }
          }
      }
  }

  "an unknown --c-frontend" should {
      "be refused" in {
          project { (dir, src) =>
              atom(
                "-l",
                "c",
                "--c-frontend",
                "gcc",
                "--cache",
                "none",
                "-o",
                (dir / "app.atom").pathAsString,
                src.pathAsString
              ).isLeft shouldBe true
          }
      }
  }
end CFrontendWorkflowTests
