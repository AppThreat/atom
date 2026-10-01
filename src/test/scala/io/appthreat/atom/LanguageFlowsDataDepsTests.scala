package io.appthreat.atom

import io.appthreat.atom.dataflows.OssDataFlow
import io.appthreat.c2cpg.testfixtures.CCodeToCpgSuite
import io.shiftleft.codepropertygraph.generated.nodes.Expression
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.layers.LayerCreatorContext

/** The data dependencies atom builds for a C graph follow the C library summaries: `memcpy(dst,
  * src, n)` copies `src` into `dst`, and its length `n` is not copied anywhere. Without the C
  * summaries every argument of an unknown call is assumed to flow into every other one.
  */
class LanguageFlowsDataDepsTests extends CCodeToCpgSuite:

  private val cpg = code(
    """
      |#include <string.h>
      |void sink(char *p);
      |void copy(char *dst, const char *src, unsigned long n, char **argv) {
      |  memcpy(dst, src, n);
      |  strcpy(dst, argv[1]);
      |  sink(dst);
      |}
      |""".stripMargin,
    "copy.c"
  )

  new OssDataFlow(OssDataFlow.defaultOpts).run(new LayerCreatorContext(cpg))

  private def argument(call: String, index: Int): Expression =
      cpg.call.nameExact(call).argument(index).head

  /** Sources of the REACHING_DEF edges into `arg` that come from another argument of its call. */
  private def fromSameCall(arg: Expression): List[String] =
      arg._reachingDefIn.collect {
          case e: Expression if e.inCall.headOption == arg.inCall.headOption => e.code
      }.l

  "memcpy" should {
      "define its destination from its source, not from its length" in {
          fromSameCall(argument("memcpy", 1)) shouldBe List("src")
      }
  }

  "strcpy" should {
      "define its destination from its source" in {
          fromSameCall(argument("strcpy", 1)) shouldBe List("argv[1]")
      }

      "make the copied value reach a later use of the destination" in {
          val strcpyDst = argument("strcpy", 1)
          val sinkArg   = argument("sink", 1)
          sinkArg._reachingDefIn.l should contain(strcpyDst)
      }
  }
end LanguageFlowsDataDepsTests
