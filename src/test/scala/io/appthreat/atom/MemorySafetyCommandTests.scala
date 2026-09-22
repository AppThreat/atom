package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.x2cpg.passes.taggers.{
    AllocationStatePass,
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
      .moreCode(
        """
        |#include <stdlib.h>
        |
        |/* MS-ALLOC-003 at the implicit end of the function: the fact lands on METHOD_RETURN,
        |   which is a CFG_NODE and not an Expression. */
        |void leaks_at_the_implicit_end(void)
        |{
        |    char *p = (char *)malloc(64);
        |    p[0] = 'x';
        |}
        |""".stripMargin,
        "leak.c"
      )

  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new AllocationStatePass(cpg).createAndApply()
  new MemorySafetyFindingPass(cpg).createAndApply()

  private def render(config: AtomMemorySafetyConfig): Either[String, List[io.circe.Json]] =
    val out = File.newTemporaryFile("memory-safety", ".json")
    out.deleteOnExit()
    config.outputSliceFile = out
    MemorySafetyCommands.runMemorySafety(cpg, config, out).map { _ =>
        parse(out.contentAsString).toOption.flatMap(_.asArray.map(_.toList)).getOrElse(Nil)
    }

  private def findingOf(rule: String): io.circe.Json =
      render(AtomMemorySafetyConfig()).toOption
          .getOrElse(Nil)
          .find(_.hcursor.get[String]("rule").toOption.contains(rule))
          .getOrElse(fail(s"no $rule finding was rendered"))

  "the memory-safety command" should {

      "render the finding contract the corpus scorer reads" in {
          val f = findingOf(MemorySafetyFindingPass.RuleSizeParamContract).hcursor
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
          val flow = findingOf(MemorySafetyFindingPass.RuleSizeParamContract).hcursor
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
          render(AtomMemorySafetyConfig().withFormat("sarif")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("very-high")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("HIGH")).isRight shouldBe true
      }

      "keep the flags set before it: a command option must not reset the config" in {
          // AtomConfig holds language/dataDeps/output paths in the TRAIT's vars, so a `copy`-based
          // option silently returns them to their defaults. `atom memory-safety -l c
          // --min-confidence medium` lost the `-l` and died with "No language frontend supported
          // for language ''" - invisible to any test that builds the config directly.
          val config = AtomMemorySafetyConfig()
          config.withLanguage("c").withDataDependencies(true)
          config.withMinConfidence("medium").withFormat("json")
          config.language shouldBe "c"
          config.dataDeps shouldBe true
      }

      "render an allocation-state finding" in {
          // MS-ALLOC-003 is the first rule whose findings do not sit on a length argument, and
          // the renderer dropped every one of them: it matched `Expression | ControlStructure`,
          // which is just Expression, while an exit-anchored finding can be a METHOD_RETURN
          val f = findingOf(MemorySafetyFindingPass.RuleLeak).hcursor
          f.get[String]("cwe").toOption shouldBe Some("CWE-401")
          f.get[String]("file").toOption.exists(_.endsWith("leak.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "drop findings below the requested confidence" in {
          // the leak is medium; a high floor leaves only the high-confidence contract violation
          val high = render(AtomMemorySafetyConfig().withMinConfidence("high")).toOption
              .getOrElse(Nil)
              .flatMap(_.hcursor.get[String]("rule").toOption)
          high shouldBe List(MemorySafetyFindingPass.RuleSizeParamContract)
      }
  }
end MemorySafetyCommandTests
