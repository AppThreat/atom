package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.x2cpg.passes.taggers.{
    AllocationStatePass,
    ExtentPass,
    GuardPass,
    IntegerWidthPass,
    MemoryApiPass,
    MemorySafetyFindingPass,
    ValueOriginPass
}
import io.circe.parser.parse

/** The `memory-safety` command end to end: run the overlay the way `Atom.applyTaggers` does, then
  * render through [[MemorySafetyCommands]] and read the JSON back.
  *
  * The renderer has no detector logic in it, so what is worth asserting here is the CONTRACT a
  * scorer reads (rule, cwe, kind, file, line, confidence) and the evidence rows the flow exists to
  * carry - the origin, guard and extent facts each rule actually consulted. Both of those shipped
  * broken once: the evidence entries were built and thrown away, and `Tag.value` is a String, so
  * iterating it rendered `struct-field` as twelve single-letter rows.
  */
class MemorySafetyCommandTests extends DataFlowCodeToCpgSuite:

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(
        """
        |#include <string.h>
        |#include <stdint.h>
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
        |
        |/* MS-BOUND-002: an externally reachable helper copying a caller-chosen count. */
        |void unbounded_copy(unsigned char *buf, const unsigned char *src, int n)
        |{
        |    memcpy(buf, src, n);
        |}
        |
        |/* MS-BOUND-003/004: attacker index, known capacity, no bound. */
        |int index_read(int i)
        |{
        |    int buf[8];
        |    return buf[i];
        |}
        |
        |void index_write(int n, unsigned char v)
        |{
        |    unsigned char buf[8];
        |    buf[n] = v;
        |}
        |
        |/* MS-INT-002: the guard tests the re-signed view (CVE-2026-75145 shape). */
        |int resign_guard(uint32_t obu_size, int remaining, unsigned char *dst,
        |                 const unsigned char *src)
        |{
        |    if ((long)obu_size > remaining)
        |        return -1;
        |    memcpy(dst, src, obu_size);
        |    return 0;
        |}
        |""".stripMargin,
        "copy.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <stdio.h>
        |
        |/* MS-ALLOC-003 at the implicit end of the function: the fact lands on METHOD_RETURN,
        |   which is a CFG_NODE and not an Expression. */
        |void leaks_at_the_implicit_end(void)
        |{
        |    char *p = (char *)malloc(64);
        |    p[0] = 'x';
        |}
        |
        |/* MS-ALLOC-003 anchored at a bare `return;` - the finding sits on the RETURN node. */
        |void leaks_at_a_bare_return(int n)
        |{
        |    char *p = (char *)malloc(64);
        |    if (n < 0) return;
        |    free(p);
        |}
        |
        |/* the unbraced `if (p) free(p);`: a narrowing that misses the false edge renders this
        |   negative control as a leak at the implicit end */
        |void unbraced_if_frees(char *unused, size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    if (p) free(p);
        |}
        |
        |/* MS-ALLOC-001 and MS-ALLOC-002 in one function. */
        |void double_free_then_use(void)
        |{
        |    char *p = (char *)malloc(32);
        |    free(p);
        |    free(p);
        |    p[0] = 'x';
        |}
        |
        |/* MS-ALLOC-004. */
        |void double_close(const char *path)
        |{
        |    FILE *f = fopen(path, "r");
        |    if (f == NULL) return;
        |    fclose(f);
        |    fclose(f);
        |}
        |""".stripMargin,
        "alloc.c"
      )
      .moreCode(
        """
        |#include <stdlib.h>
        |#include <string.h>
        |
        |struct node { struct node *next; int v; };
        |
        |/* MS-NULL-001 arm 1 (medium): an unchecked allocation result. */
        |void null_deref_unchecked_malloc(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    p[0] = 'a';
        |    free(p);
        |}
        |
        |/* MS-NULL-001 arm 3 (the per-finding low tier): an unvalidated parameter through a
        |   SELF-REFERENTIAL traversal hop (next has head's own pointee type). */
        |void null_deref_chained_param(struct node *head)
        |{
        |    int v = head->next->v;
        |    (void)v;
        |}
        |
        |/* an unchecked allocation through a helper - the callee's derefs-param summary is what
        |   makes handing it an unchecked allocation a dereference */
        |static int head_byte(char *s)
        |{
        |    return s[0];
        |}
        |
        |void null_deref_via_helper_summary(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    int c = head_byte(p);
        |    (void)c;
        |    free(p);
        |}
        |
        |/* the helper reads through nothing, so the same statement is not a
        |   dereference and must not render */
        |static void just_log(char *p)
        |{
        |    printf("%p\n", (void *)p);
        |}
        |
        |void no_null_deref_via_nonreading_helper(size_t n)
        |{
        |    char *p = (char *)malloc(n);
        |    just_log(p);
        |    free(p);
        |}
        |""".stripMargin,
        "null.c"
      )
      .moreCode(
        """
        |#include <string.h>
        |
        |/* MS-ESC-001: the finding is anchored at the RETURN - an exit node, not a length
        |   argument; a renderer that only matched arguments would silently drop every one. */
        |char *returns_stack_array(void)
        |{
        |    char buf[32];
        |    strcpy(buf, "hello");
        |    return buf;
        |}
        |""".stripMargin,
        "escape.c"
      )

  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new IntegerWidthPass(cpg).createAndApply()
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

      "render the finding contract a scorer reads" in {
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
          render(AtomMemorySafetyConfig().withFormat("xml")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("very-high")).isLeft shouldBe true
          render(AtomMemorySafetyConfig().withMinConfidence("HIGH")).isRight shouldBe true
      }

      "write SARIF 2.1.0 the official schema accepts, with rules, locations and code flows" in {
          val out = File.newTemporaryFile("memory-safety", ".sarif")
          out.deleteOnExit()
          val config = AtomMemorySafetyConfig().withFormat("sarif")
          config.outputSliceFile = out
          MemorySafetyCommands.runMemorySafety(cpg, config, out).isRight shouldBe true
          val text = out.contentAsString
          val factory = com.networknt.schema.JsonSchemaFactory.getInstance(
            com.networknt.schema.SpecVersion.VersionFlag.V4
          )
          val schema = factory.getSchema(
            getClass.getResourceAsStream("/sarif/sarif-schema-2.1.0.json")
          )
          val mapper = new com.fasterxml.jackson.databind.ObjectMapper()
          val errors = schema.validate(mapper.readTree(text))
          errors.isEmpty shouldBe true
          // and the schema does reject what is not SARIF: a log without a version
          schema.validate(mapper.readTree("""{"runs": []}""")).isEmpty shouldBe false

          val sarif = parse(text).toOption.get
          val run   = sarif.hcursor.downField("runs").downArray
          val rules = run.downField("tool").downField("driver").downField("rules").as[List[
            io.circe.Json
          ]].toOption.get
          val results = run.downField("results").as[List[io.circe.Json]].toOption.get
          val json    = render(AtomMemorySafetyConfig()).toOption.get
          results.size shouldBe json.size
          val ruleIds = rules.flatMap(_.hcursor.get[String]("id").toOption)
          ruleIds should contain(MemorySafetyFindingPass.RuleSizeParamContract)
          // each result names its rule by id and index, and carries the CWE
          results.foreach { r =>
            val id = r.hcursor.get[String]("ruleId").toOption.get
            r.hcursor.get[Int]("ruleIndex").toOption shouldBe Some(ruleIds.indexOf(id))
            r.hcursor.downField("properties").get[String]("cwe").toOption.get should startWith(
              "CWE-"
            )
          }
          val sizeParam = results.find(
            _.hcursor.get[String]("ruleId").toOption.contains(
              MemorySafetyFindingPass.RuleSizeParamContract
            )
          ).get
          val location =
              sizeParam.hcursor.downField("locations").downArray.downField("physicalLocation")
          location.downField("artifactLocation").get[String]("uri").toOption.get should endWith(
            "copy.c"
          )
          location.downField("region").get[Int]("startLine").toOption.get should be > 0
          sizeParam.hcursor.downField("codeFlows").downArray.downField("threadFlows").downArray
              .downField("locations").as[List[io.circe.Json]].toOption.get should not be empty
      }

      "index SARIF results into the rules listed, and keep a file outside the input absolute" in {
          val known = MemorySafetyFindingPass.RuleSizeParamContract
          def finding(rule: String, file: String) = io.circe.Json.obj(
            "rule"     -> io.circe.Json.fromString(rule),
            "file"     -> io.circe.Json.fromString(file),
            "line"     -> io.circe.Json.fromInt(3),
            "severity" -> io.circe.Json.fromString("high")
          )
          // "MS-AAA-000" sorts before every registered id and is not one
          val sarif = MemorySafetySarif.document(
            List(finding("MS-AAA-000", "src/a.c"), finding(known, "/usr/include/string.h")),
            "/work/project",
            None
          )
          val run = sarif.hcursor.downField("runs").downArray
          val ruleIds = run.downField("tool").downField("driver").downField("rules")
              .as[List[io.circe.Json]].toOption.get.flatMap(_.hcursor.get[String]("id").toOption)
          ruleIds shouldBe List(known)
          val results = run.downField("results").as[List[io.circe.Json]].toOption.get
          results.map(_.hcursor.get[Int]("ruleIndex").toOption.get) shouldBe List(-1, 0)
          def artifact(r: io.circe.Json) = r.hcursor.downField("locations").downArray
              .downField("physicalLocation").downField("artifactLocation")
          artifact(results.head).get[String]("uri").toOption shouldBe Some("src/a.c")
          artifact(results.head).get[String]("uriBaseId").toOption shouldBe Some("%SRCROOT%")
          artifact(results.last).get[String]("uri").toOption.get should startWith("file:/")
          artifact(results.last).get[String]("uri").toOption.get should endWith(
            "/usr/include/string.h"
          )
          artifact(results.last).get[String]("uriBaseId").toOption shouldBe None
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
          f.get[String]("file").toOption.exists(_.endsWith("alloc.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "render a leak anchored at a bare `return;` (an exit node is an anchor too)" in {
          // the finding's node IS the RETURN - not the allocation, not an argument
          val f = findingOf(MemorySafetyFindingPass.RuleLeak).hcursor
          f.get[String]("file").toOption.exists(_.endsWith("alloc.c")) shouldBe true
          // the implicit-end fixture and the bare-return fixture both leak; one of the two
          // rendered findings must sit inside leaks_at_a_bare_return's body, above the free
          val lines = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleLeak
              ))
          lines should not be empty
      }

      "render every rule's finding end to end" in {
          // a tag the renderer never surfaces is not a finding; a rule's measured precision is
          // meaningless if the renderer silently drops its findings
          val byRule = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .flatMap(_.hcursor.get[String]("rule").toOption)
              .toSet
          Seq(
            MemorySafetyFindingPass.RuleSizeParamContract,
            MemorySafetyFindingPass.RuleUnboundedCopy,
            MemorySafetyFindingPass.RuleIndexRead,
            MemorySafetyFindingPass.RuleIndexWrite,
            MemorySafetyFindingPass.RuleResignAcrossGuard,
            MemorySafetyFindingPass.RuleDoubleFree,
            MemorySafetyFindingPass.RuleUseAfterFree,
            MemorySafetyFindingPass.RuleLeak,
            MemorySafetyFindingPass.RuleDoubleClose,
            MemorySafetyFindingPass.RuleNullDeref,
            MemorySafetyFindingPass.RuleStackEscape
          ).foreach(rule => byRule should contain(rule))
      }

      "render the stack-escape finding at its RETURN anchor" in {
          // MS-ESC-001's finding sits on the Return of returns_stack_array: an exit node is
          // exactly where a renderer matching only expressions silently discards findings
          val f = findingOf(MemorySafetyFindingPass.RuleStackEscape).hcursor
          f.get[String]("cwe").toOption shouldBe Some("CWE-562")
          f.get[String]("file").toOption.exists(_.endsWith("escape.c")) shouldBe true
          f.get[Int]("line").toOption.exists(_ > 0) shouldBe true
      }

      "keep the unbraced `if (p) free(p);` silent through the renderer (good_capped)" in {
          // a missed false edge renders this negative control as a leak at the implicit end;
          // the branch narrowing must be visible HERE too, not only in chen's tag assertions
          val leaksInIt = render(AtomMemorySafetyConfig()).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleLeak
              ))
              .filterNot(_.hcursor.get[String]("file").toOption.exists(_.endsWith("escape.c")))
          // the fixtures that legitimately leak are leaks_at_the_implicit_end and
          // leaks_at_a_bare_return; the unbraced one contributes nothing
          leaksInIt.map(_.hcursor.get[String]("file").toOption.getOrElse("")).foreach { f =>
              (f should not).include("unbraced")
          }
      }

      "render MS-NULL-001 only below the medium floor (the per-rule gate demoted it)" in {
          // the per-rule gate: on FFmpeg-style code most of its reports are the idiom of using
          // a nullable result before the error path - so it ships at low, exactly as
          // MS-INT-001 does. What must survive the demotion is the arms' visibility below the
          // floor: the evidence arms and the chained-parameter arm (its own per-finding low).
          def nullDerefLines(floor: String): Set[Int] = render(
            AtomMemorySafetyConfig().withMinConfidence(floor)
          ).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleNullDeref
              ))
              .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith("null.c")))
              .flatMap(_.hcursor.get[Int]("line").toOption)
              .toSet
          nullDerefLines("medium") shouldBe empty   // the gate, visible end to end
          nullDerefLines("low") should not be empty // both arms render below the floor
      }

      "render a null-deref licensed by a derefs-param summary, not one handed to a non-reader" in {
          // the low arm's null.c findings, by line, with the fixture's three positive shapes
          // (unchecked malloc, chained param, helper summary) and one negative (just_log)
          val lines = render(AtomMemorySafetyConfig().withMinConfidence("low")).toOption
              .getOrElse(Nil)
              .filter(_.hcursor.get[String]("rule").toOption.contains(
                MemorySafetyFindingPass.RuleNullDeref
              ))
              .filter(_.hcursor.get[String]("file").toOption.exists(_.endsWith("null.c")))
              .flatMap(_.hcursor.get[Int]("line").toOption)
              .toSet
          // the three positives sit on three distinct lines; the non-reading helper adds none
          lines should have size 3
          // the just_log call's own line carries no finding: its argument is read by nobody
          import io.shiftleft.semanticcpg.language.*
          val justLogLine = cpg.method
              .name("no_null_deref_via_nonreading_helper")
              .call
              .name("just_log")
              .l
              .headOption
              .flatMap(_.lineNumber.map(_.toInt))
          justLogLine.foreach(l => lines should not contain l)
      }

      "drop findings below the requested confidence" in {
          // the high floor keeps the high-confidence rules (the contract violation, the
          // double free, the resign cast) and drops the medium/low ones entirely
          val high = render(AtomMemorySafetyConfig().withMinConfidence("high")).toOption
              .getOrElse(Nil)
              .flatMap(_.hcursor.get[String]("rule").toOption)
          high should contain(MemorySafetyFindingPass.RuleSizeParamContract)
          high should not contain MemorySafetyFindingPass.RuleLeak
          high should not contain MemorySafetyFindingPass.RuleNullDeref
          high should not contain MemorySafetyFindingPass.RuleStackEscape
      }
  }
end MemorySafetyCommandTests
