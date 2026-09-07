package io.appthreat.atom

import better.files.File as BFile
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.Config as OdbConfig

/** Re-exporting an UNCHANGED atom with `--reuse-atom` must be idempotent.
  *
  * The regression this guards: `enhanceCpg` re-applied the tagger passes whenever an existing atom
  * was loaded, appending a fresh set of TAG nodes on every export (measured: 331 -> 340 -> 349 ->
  * 358 on a 331-node probe). Export the same source three times and assert the node count never
  * moves.
  */
class ReuseAtomIdempotencyTests extends AnyWordSpec with Matchers:

  private def nodeCount(atomFile: BFile): Int =
    val odbConfig = OdbConfig.withDefaults().withStorageLocation(atomFile.pathAsString)
    val cpg = CpgLoader.loadFromOverflowDb(
      CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    )
    try cpg.graph.nodeCount.intValue
    finally cpg.close()

  private def runExport(atomFile: BFile, graphDir: BFile, src: BFile, reuse: Boolean) =
    val args = (
      Seq(
        "export",
        "--cache",
        "none",
        "-l",
        "python",
        "-o",
        atomFile.pathAsString,
        "--format",
        "graphml",
        "--out",
        graphDir.pathAsString
      ) ++ (if reuse then Seq("--reuse-atom") else Nil) ++ Seq(src.pathAsString)
    ).toArray
    Atom.run(args)

  "re-exporting an unchanged atom with --reuse-atom" should:

    "not grow the graph across repeated exports" in:
      BFile.usingTemporaryDirectory("atom-reuse-p72") { dir =>
        val src = dir / "src"
        src.createDirectory()
        // Deliberately includes validate/sanitize/authorize/admin names: EasyTagsPass and
        // the framework recognizers tag several sites here - exactly the passes that used
        // to append on every re-export.
        (src / "app.py").write(
          """
                |import os
                |
                |def validate(user_input):
                |    if not user_input:
                |        return False
                |    return len(user_input) < 100
                |
                |def is_admin(username):
                |    return username == "admin"
                |
                |def sanitize(path):
                |    return os.path.normpath(path)
                |
                |def authorize(token):
                |    if not token:
                |        return False
                |    return token.startswith("Bearer ")
                |
                |def run(command):
                |    stream = os.popen(command)
                |    return stream.read()
                |
                |def main():
                |    data = input()
                |    if validate(data) and is_admin(data):
                |        print(run(sanitize(data)))
                |""".stripMargin
        )
        val atomFile = dir / "p.atom"
        val graphDir = dir / "graphml"

        runExport(atomFile, graphDir, src, reuse = false) shouldBe a[Right[?, ?]]
        val first = nodeCount(atomFile)
        runExport(atomFile, graphDir, src, reuse = true) shouldBe a[Right[?, ?]]
        val second = nodeCount(atomFile)
        runExport(atomFile, graphDir, src, reuse = true) shouldBe a[Right[?, ?]]
        val third = nodeCount(atomFile)

        withClue(s"node counts across the three exports were $first, $second, $third; ") {
            second shouldBe first
            third shouldBe first
        }
      }
end ReuseAtomIdempotencyTests
