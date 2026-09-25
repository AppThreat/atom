package io.appthreat.atom

import better.files.File
import io.appthreat.c2cpg.testfixtures.{DataFlowCodeToCpgSuite, DataFlowTestCpg}
import io.appthreat.x2cpg.passes.taggers.*
import io.circe.parser.parse

/** Part 11 end to end: the three recall arms render through `memory-safety` with the contract the
  * corpus scorer reads - the counter-indexed array (MS-BOUND-004 write through an escaped element
  * address, CVE-2026-64830), the source overread (MS-BOUND-008, CVE-2026-64833, with the
  * config-declared AVPacket-style extent pair) and the advance by a decoded size (MS-BOUND-003,
  * CVE-2026-75147, with the MemorySemanticsPass out-param summary the arm turns on). Each arm's
  * fixed shape renders nothing.
  */
class Part11MemorySafetyCommandTests extends DataFlowCodeToCpgSuite:

  private val pairConfig =
      """{"apis": [], "bufferExtents": [{"type": "packet", "buffer": "data", "capacity": "size"}]}"""

  private val cpg: DataFlowTestCpg = new DataFlowTestCpg()
      .moreCode(
        """
        |#include <stdint.h>
        |#include <stdlib.h>
        |#include <string.h>
        |#include <stdio.h>
        |
        |#define ARRAY_ELEMS(a) (sizeof(a) / sizeof((a)[0]))
        |
        |struct entry { long pos; };
        |struct fmt_ctx { unsigned int nb_streams; };
        |struct sub_ctx { struct entry q[32]; };
        |
        |static void add_stream(struct fmt_ctx *ctx)
        |{
        |    ctx->nb_streams++;
        |}
        |
        |static void put(struct entry *e, long pos)
        |{
        |    e->pos = pos;
        |}
        |
        |int bad_counter_index(struct sub_ctx *sub, struct fmt_ctx *ctx, FILE *idx)
        |{
        |    char line[256];
        |    while (fgets(line, sizeof(line), idx))
        |    {
        |        add_stream(ctx);
        |        put(&sub->q[ctx->nb_streams - 1], 1);
        |    }
        |    return 0;
        |}
        |
        |int good_exit_bounds_count(struct sub_ctx *sub, struct fmt_ctx *ctx, FILE *idx)
        |{
        |    char line[256];
        |    while (fgets(line, sizeof(line), idx))
        |    {
        |        if (ctx->nb_streams >= ARRAY_ELEMS(sub->q))
        |            return -1;
        |        add_stream(ctx);
        |        put(&sub->q[ctx->nb_streams - 1], 1);
        |    }
        |    return 0;
        |}
        |""".stripMargin,
        "counter.c"
      )
      .moreCode(
        """
        |#include <stdint.h>
        |#include <stdlib.h>
        |#include <string.h>
        |
        |struct packet {
        |    const uint8_t *data; /* the config names size as this member's extent */
        |    int size;
        |};
        |
        |static int emit_hd(struct packet *pkt, int core_size)
        |{
        |    int pkt_size = pkt->size;
        |    uint8_t *dst;
        |    pkt_size = core_size;
        |    dst = (uint8_t *)malloc(pkt_size + 4);
        |    memcpy(dst + 4, pkt->data, pkt_size);
        |    free(dst);
        |    return 0;
        |}
        |
        |int bad_source_overread(struct packet *pkt)
        |{
        |    int core = ((((uint32_t)pkt->data[5] << 16 | (uint32_t)pkt->data[6] << 8 | (uint32_t)pkt->data[7]) >> 4) & 0x3fff) + 1;
        |    return emit_hd(pkt, core);
        |}
        |
        |int good_source_overread_guarded(struct packet *pkt)
        |{
        |    int core = ((((uint32_t)pkt->data[5] << 16 | (uint32_t)pkt->data[6] << 8 | (uint32_t)pkt->data[7]) >> 4) & 0x3fff) + 1;
        |    int pkt_size = pkt->size;
        |    uint8_t *dst;
        |    if (core <= pkt->size)
        |        pkt_size = core;
        |    dst = (uint8_t *)malloc(pkt_size + 4);
        |    memcpy(dst + 4, pkt->data, pkt_size);
        |    free(dst);
        |    return 0;
        |}
        |""".stripMargin,
        "source.c"
      )
      .moreCode(
        """
        |#include <stdint.h>
        |
        |static int parse_leb(const uint8_t *buf, int buf_size, uint32_t *out)
        |{
        |    uint32_t v = 0;
        |    int i = 0;
        |    while (i < buf_size && i < 8)
        |    {
        |        uint8_t b = buf[i];
        |        v |= (uint32_t)(b & 0x7f) << (7 * i);
        |        i++;
        |        if (!(b & 0x80))
        |        {
        |            *out = v;
        |            return i;
        |        }
        |    }
        |    return 0;
        |}
        |
        |int bad_advance_by_decoded_size(const uint8_t *frame, int frame_size)
        |{
        |    const uint8_t *p = frame;
        |    int rem = frame_size;
        |    while (rem > 0)
        |    {
        |        uint32_t sz;
        |        int n;
        |        uint8_t hdr = *p++;
        |        (void)hdr;
        |        rem--;
        |        n = parse_leb(p, rem, &sz);
        |        if (!n)
        |            break;
        |        p += n + sz;
        |        rem -= n + sz;
        |    }
        |    return 0;
        |}
        |
        |int good_advance_guarded_unsigned(const uint8_t *frame, int frame_size)
        |{
        |    const uint8_t *p = frame;
        |    int rem = frame_size;
        |    while (rem > 0)
        |    {
        |        uint32_t sz;
        |        int n;
        |        uint8_t hdr = *p++;
        |        (void)hdr;
        |        rem--;
        |        n = parse_leb(p, rem, &sz);
        |        if (!n)
        |            break;
        |        if (sz > (uint32_t)rem)
        |            break;
        |        p += n + sz;
        |        rem -= n + sz;
        |    }
        |    return 0;
        |}
        |""".stripMargin,
        "advance.c"
      )

  new MemorySemanticsPass(cpg).createAndApply()
  new MemoryApiPass(cpg).createAndApply()
  new ExtentPass(cpg).createAndApply()
  new GuardPass(cpg).createAndApply()
  new ValueOriginPass(cpg).createAndApply()
  new IntegerWidthPass(cpg).createAndApply()
  new AllocationStatePass(cpg).createAndApply()
  new MemorySafetyFindingPass(cpg, Some(pairConfig)).createAndApply()

  private def rendered(floor: String = "medium"): List[io.circe.Json] =
    val out = File.newTemporaryFile("memory-safety", ".json")
    out.deleteOnExit()
    val config = AtomMemorySafetyConfig()
    config.outputSliceFile = out
    config.minConfidence = floor
    MemorySafetyCommands.runMemorySafety(cpg, config, out)
    parse(out.contentAsString).toOption.flatMap(_.asArray).map(_.toList).getOrElse(Nil)

  private def strOf(row: io.circe.Json, field: String): Option[String] =
      row.hcursor.get[String](field).toOption

  private def lineOf(row: io.circe.Json): Option[Int] =
      row.hcursor.get[Int]("line").toOption

  private def rulesIn(file: String): Set[String] =
      rendered().filter(strOf(_, "file").exists(_.endsWith(file)))
          .flatMap(strOf(_, "rule"))
          .toSet

  "the part 11 arms end to end" should {

      "render the counter-indexed array as an oob write (CWE-787, the 64830 CWE)" in {
          val rows = rendered().filter(strOf(_, "file").exists(_.endsWith("counter.c")))
              .filter(strOf(_, "rule").exists(_.contains("MS-BOUND-004")))
          rows should not be empty
          strOf(rows.head, "cwe") shouldBe Some("CWE-787")
      }

      "keep the guarded counter shape silent end to end" in {
          // the fixed tree's exit names the array's element count: nothing may render inside
          // good_exit_bounds_count's body, whatever else renders in the file
          import io.shiftleft.semanticcpg.language.*
          val goodStart = cpg.method.nameExact("good_exit_bounds_count").l.headOption
              .flatMap(_.lineNumber.map(_.toInt)).getOrElse(Int.MaxValue)
          val lines = rendered("low")
              .filter(strOf(_, "file").exists(_.endsWith("counter.c")))
              .flatMap(lineOf)
          lines.foreach(l => l should be < goodStart)
      }

      "render the source overread as MS-BOUND-008 (CWE-125) at the length's definition" in {
          val rows = rendered().filter(strOf(_, "file").exists(_.endsWith("source.c")))
              .filter(strOf(_, "rule").exists(_.contains("MS-BOUND-008")))
          rows should not be empty
          strOf(rows.head, "cwe") shouldBe Some("CWE-125")
          strOf(rows.head, "kind") shouldBe Some("source-overread")
      }

      "keep the extent-guarded copy silent end to end" in {
          // the guarded twin renders nothing: the only MS-BOUND-008 in the file is emit_hd's
          // unguarded `pkt_size = core_size` definition
          val badLines = rendered("low")
              .filter(strOf(_, "file").exists(_.endsWith("source.c")))
              .filter(strOf(_, "rule").exists(_.contains("MS-BOUND-008")))
              .flatMap(lineOf)
          badLines should have size 1
      }

      "render the decoded-size advance as MS-BOUND-003 (CWE-125)" in {
          val rows = rendered().filter(strOf(_, "file").exists(_.endsWith("advance.c")))
              .filter(strOf(_, "rule").exists(_.contains("MS-BOUND-003")))
          rows should not be empty
          strOf(rows.head, "cwe") shouldBe Some("CWE-125")
      }

      "keep the unsigned-guarded advance silent end to end" in {
          val lines = rendered("low")
              .filter(strOf(_, "file").exists(_.endsWith("advance.c")))
              .filter(strOf(_, "rule").exists(_.contains("MS-BOUND-003")))
              .flatMap(lineOf)
          lines should have size 1 // only the unguarded walk's advance
      }
  }
end Part11MemorySafetyCommandTests
