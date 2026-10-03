package io.appthreat.atom

import io.appthreat.atom.slicing.*
import io.appthreat.c2cpg.Config
import io.appthreat.c2cpg.testfixtures.CCodeToCpgSuite
import io.appthreat.x2cpg.passes.taggers.SourceIntegrityPass
import io.circe.parser.decode
import io.circe.syntax.*

/** The usages output reports Unicode that hides what the code does, and says nothing of it when
  * there is none.
  */
class SourceIntegrityUsageSliceTests extends CCodeToCpgSuite:

  "the usages output of a file with look-alike names and bidi controls" should {
      // `vаlue` spells its `a` with CYRILLIC SMALL LETTER A
      val cpg = code(
        "int check(int value) {\n" +
            "  int vаlue = 0;\n" +
            "  /* ‮ } if (admin) { */\n" +
            "  const char *msg = \"user‮\";\n" +
            "  return value + vаlue;\n" +
            "}\n",
        "trojan.c"
      ).withConfig(Config().withIncludeComments(true))
      new SourceIntegrityPass(cpg).createAndApply()
      val slice =
          UsageSlicing.calculateUsageSlice(cpg, UsagesConfig()).asInstanceOf[ProgramUsageSlice]

      "list each finding with its file, line and detail" in {
          val kinds = slice.sourceIntegrity.map(f => (f.kind, f.lineNumber, f.detail)).distinct
          kinds should contain(("unicode-bidi-control", Some(3), "U+202E"))
          kinds should contain(("unicode-bidi-control", Some(4), "U+202E"))
          kinds should contain(("unicode-confusable", Some(1), "vаlue"))
          kinds should contain(("unicode-confusable", Some(2), "value"))
          slice.sourceIntegrity.map(_.fileName).distinct shouldBe List("trojan.c")
      }

      "write the findings and read them back" in {
          val json = slice.asJson
          json.hcursor.downField("sourceIntegrity").as[List[SourceIntegrityFinding]].toOption shouldBe
              Some(slice.sourceIntegrity)
          decode[ProgramUsageSlice](json.noSpaces).toOption.map(_.sourceIntegrity) shouldBe
              Some(slice.sourceIntegrity)
      }
  }

  "the usages output of a plain file" should {
      val cpg = code("int f(int a) { return a; }\n", "plain.c")
      new SourceIntegrityPass(cpg).createAndApply()
      val slice =
          UsageSlicing.calculateUsageSlice(cpg, UsagesConfig()).asInstanceOf[ProgramUsageSlice]

      "carry no source-integrity section" in {
          slice.sourceIntegrity shouldBe empty
          slice.asJson.hcursor.downField("sourceIntegrity").succeeded shouldBe false
      }
  }
end SourceIntegrityUsageSliceTests
