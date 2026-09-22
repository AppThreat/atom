package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.x2cpg.passes.taggers.{
    ExtentPass,
    GuardPass,
    MemoryApiPass,
    MemorySafetyFindingPass,
    ValueOriginPass
}
import io.circe.parser.parse

/** The `memory-safety` command end to end: run the overlay the way `Atom.applyTaggers` does, then
  * render through [[MemorySafetyCommands]] and read the JSON back.
  *
  * The renderer has no detector logic in it, so what is worth asserting here is the CONTRACT the
  * corpus scorer reads (rule, cwe, kind, file, line, confidence) and the evidence rows the flow
  * exists to carry - the origin, guard and extent facts each rule actually consulted. Both of those
  * shipped broken once: the evidence entries were built and thrown away, and `Tag.value` is a
  * String, so iterating it rendered `struct-field` as twelve single-letter rows.
  */
class MemorySafetyCommandTests extends DataFlowCodeToCpgSuite:

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(
        """
        |#include <string.h>
        |
        |struct data_block { unsigned char *payload; int payload_len; };
        |
        |/* MS-BOUND-001: the caller's capacity parameter is destroyed before the write. */
        |int overwrites_size_param(unsigned char *buf, int size, struct data_block *db)
        |{
        |    size = db->payload_len;
        |    memcpy(buf, db->payload, size);
        |    return size;
        |}
        |
        |/* Honours the contract: nothing to report. */
        |int honours_size_param(unsigned char *buf, int size, struct data_block *db)
        |{
        |    memcpy(buf, db->payload, size);
        |    return size;
        |}
        |""".stripMargin,
        "copy.c"
      )

  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new MemorySafetyFindingPass(cpg).createAndApply()

  private def render(config: AtomMemorySafetyConfig): Either[String, List[io.circe.Json]] =
    val out = File.newTemporaryFile("memory-safety", ".json")
    out.deleteOnExit()
    config.outputSliceFile = out
    MemorySafetyCommands.runMemorySafety(cpg, config, out).map { _ =>
        parse(out.contentAsString).toOption.flatMap(_.asArray.map(_.toList)).getOrElse(Nil)
    }

  "the memory-safety command" should {

      "render the finding contract the corpus scorer reads" in {
          val findings = render(AtomMemorySafetyConfig()).toOption.getOrElse(Nil)
          findings.size shouldBe 1
          val f = findings.head.hcursor
          f.get[String]("rule").toOption shouldBe Some(
            MemorySafetyFindingPass.RuleSizeParamContract
          )
          f.get[String]("cwe").toOption shouldBe Some("CWE-787")
          f.get[String]("kind").toOption shouldBe Some("size-param-contract-violation")
          f.get[String]("confidence").toOption shouldBe Some("high")
          f.get[String]("file").toOption.exists(_.endsWith("copy.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "carry the evidence the rule read, one row per fact" in {
          val findings = render(AtomMemorySafetyConfig()).toOption.getOrElse(Nil)
          val flow = findings.head.hcursor
              .downField("flow")
              .as[List[Map[String, io.circe.Json]]]
              .toOption
              .getOrElse(Nil)
          val codes = flow.flatMap(_.get("code").flatMap(_.asString))

          codes.exists(_.startsWith("origin: ")) shouldBe true
          // one row per origin, not one per character of it
          codes.count(_.startsWith("origin: ")) shouldBe 1
          codes.exists(_.contains("destination extent param:size")) shouldBe true
          flow.flatMap(_.get("role").flatMap(_.asString)) should contain("memory-operation")
      }

      "reject a format or confidence it does not implement rather than ignoring it" in {
          render(AtomMemorySafetyConfig(format = "sarif")).isLeft shouldBe true
          render(AtomMemorySafetyConfig(minConfidence = "very-high")).isLeft shouldBe true
          render(AtomMemorySafetyConfig(minConfidence = "HIGH")).isRight shouldBe true
      }

      "drop findings below the requested confidence" in {
          // the only finding here is high-confidence, so a high floor keeps it and nothing is lost
          render(AtomMemorySafetyConfig(minConfidence = "high")).toOption.map(_.size) shouldBe Some(
            1
          )
      }
  }
end MemorySafetyCommandTests
