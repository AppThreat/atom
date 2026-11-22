package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.circe.parser.parse

import java.nio.file.Files
import scala.util.{Failure, Success, Try}

/** The findings output of the `memory-safety` command must not follow a symbolic link committed at
  * its own name.
  *
  * The command writes its JSON in the directory that holds the slices, which sits beside the
  * scanned tree, so the tree's checkout decides the names there. The output path is chosen by the
  * caller, which is why no private run is needed to learn it: the link is planted at that exact
  * name before the command runs, pointing at a file outside the analysed tree. This is the case
  * that fails when the findings file is opened by truncation and passes once the write goes through
  * SliceFiles.writeAtomically, whose rename replaces a link at the final name.
  */
class MemorySafetyOutputSymlinkTests extends DataFlowCodeToCpgSuite:

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(
        """#include <string.h>
          |
          |struct data_block { unsigned char *payload; int payload_len; };
          |
          |/* MS-BOUND-002: an externally reachable helper copying a caller-chosen count. */
          |void unbounded_copy(unsigned char *buf, const unsigned char *src, int n)
          |{
          |    memcpy(buf, src, n);
          |}
          |""".stripMargin,
        "copy.c"
      )

  // The overlay with the marker that tells runMemorySafety the tags are already on this graph,
  // exactly as a built atom would carry them.
  Atom.applyMemorySafetyOverlay(cpg, AtomMemorySafetyConfig())
  Atom.storeMemorySafetyOverlayMarker(cpg)

  "the memory-safety command with a link at the findings output name" should {

      "leave the link target untouched and write a regular findings file" in {
          File.usingTemporaryDirectory("atomMsOutSymlink") { dir =>
            val out   = dir / "findings.json"
            val notes = dir / "notes.txt"
            notes.write("operator data")
            Try(Files.createSymbolicLink(out.path, notes.path)) match
              case Failure(e) =>
                  cancel(s"this platform refuses to create a symbolic link: ${e.getMessage}")
              case Success(_) => ()

            val config = AtomMemorySafetyConfig()
            config.outputSliceFile = out
            val result = MemorySafetyCommands.runMemorySafety(cpg, config, out)
            withClue(s"memory-safety did not complete: $result"):
              result.isRight shouldBe true

            notes.contentAsString shouldBe "operator data"
            withClue("the findings output"):
              out.isSymbolicLink shouldBe false
              out.isRegularFile shouldBe true
            val findings = parse(out.contentAsString).toOption.flatMap(_.asArray).getOrElse(Nil)
            findings should not be empty
            findings.exists(f =>
                f.hcursor.get[String]("rule").toOption.contains("MS-BOUND-002")
            ) shouldBe true
          }
      }
  }
end MemorySafetyOutputSymlinkTests
