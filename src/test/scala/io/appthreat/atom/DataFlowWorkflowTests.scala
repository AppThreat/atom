package io.appthreat.atom

import io.appthreat.atom.passes.DataDepsPass
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.semanticsloader.Semantics
import io.appthreat.pysrc2cpg.PySrc2CpgFixture
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.EdgeTypes

import scala.jdk.CollectionConverters.IteratorHasAsScala

/** End-to-end workflow tests for atom's data-dependency pass, exercising BOTH the default Flux
  * engine and the classic engine reached via `--legacy-dataflow`.
  *
  * `DataDepsPass` is what atom's `OssDataFlow` overlay runs; `useFluxEngine = false` is exactly the
  * code path `--legacy-dataflow` selects. Building the CPG without OSS dataflow (`withOssDataflow =
  * false`) leaves the base + control-flow overlays in place, so the pass has a CFG to work with and
  * we can apply each engine to an otherwise-identical graph.
  */
class DataFlowWorkflowTests extends PySrc2CpgFixture(withOssDataflow = false):

  private implicit val semantics: Semantics = Semantics.fromList(DefaultSemantics().elements)

  private val sampleCode =
      """
          |def sink(x):
          |    print(x)
          |
          |def passthrough(a):
          |    b = a
          |    return b
          |
          |def main():
          |    source = input()
          |    tainted = passthrough(source)
          |    derived = tainted + "!"
          |    sink(derived)
          |""".stripMargin

  /** A structural, ID-independent fingerprint of the REACHING_DEF edges so the two engines can be
    * compared across separately-built graphs (raw node IDs need not match).
    */
  private def reachingDefFingerprint(cpg: Cpg): List[String] =
      cpg.graph
          .edges(EdgeTypes.REACHING_DEF)
          .asScala
          .map { e =>
            val src      = e.outNode()
            val dst      = e.inNode()
            val variable = Option(e.property("VARIABLE")).map(_.asInstanceOf[String]).getOrElse("")
            val srcCode  = Option(src.property("CODE")).map(_.asInstanceOf[String]).getOrElse("")
            val dstCode  = Option(dst.property("CODE")).map(_.asInstanceOf[String]).getOrElse("")
            s"${src.label()}:$srcCode->${dst.label()}:$dstCode|$variable"
          }
          .toList
          .sorted

  "atom data-flow workflow" should {

      "produce REACHING_DEF edges with the classic (legacy-dataflow) engine" in {
          val cpg = code(sampleCode)
          new DataDepsPass(cpg, useFluxEngine = false).createAndApply()
          cpg.graph.edges(EdgeTypes.REACHING_DEF).asScala.toList should not be empty
      }

      "produce REACHING_DEF edges with the default Flux engine" in {
          val cpg = code(sampleCode)
          new DataDepsPass(cpg, useFluxEngine = true).createAndApply()
          cpg.graph.edges(EdgeTypes.REACHING_DEF).asScala.toList should not be empty
      }

      "produce identical REACHING_DEF edges from both engines" in {
          val classicCpg = code(sampleCode)
          new DataDepsPass(classicCpg, useFluxEngine = false).createAndApply()
          val classic = reachingDefFingerprint(classicCpg)

          val fluxCpg = code(sampleCode)
          new DataDepsPass(fluxCpg, useFluxEngine = true).createAndApply()
          val flux = reachingDefFingerprint(fluxCpg)

          flux shouldBe classic
      }
  }
end DataFlowWorkflowTests
