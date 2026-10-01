package io.appthreat.atom.dataflows

import io.appthreat.atom.passes.DataDepsPass
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.passes.reachingdef.StaticMemberDefUsePass
import io.appthreat.dataflowengineoss.semanticsloader.{FlowSemantic, Semantics}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.layers.{LayerCreator, LayerCreatorContext, LayerCreatorOptions}

object OssDataFlow:
  val overlayName: String = "dataflowOss"
  val description: String = "Layer to support the atom data flow tracker"

  def defaultOpts = new OssDataFlowOptions()

  /** `base` plus the flows that only apply to the graph's language: the C library summaries
    * (`strcpy`, `memcpy`, ...) for C and C++, the request readers for the JVM languages. The same
    * semantics the slicers query with, so the edges built here are the edges they expect.
    */
  def withLanguageFlows(base: Semantics, cpg: Cpg): Semantics =
    val extra = cpg.metaData.language.headOption
        .map(DefaultSemantics.flowsForLanguage)
        .getOrElse(List.empty)
    if extra.isEmpty then base else Semantics.fromList(base.elements ++ extra)

class OssDataFlowOptions(
  var maxNumberOfDefinitions: Int = 2000,
  var extraFlows: List[FlowSemantic] = List.empty[FlowSemantic],
  var useFluxEngine: Boolean = false
) extends LayerCreatorOptions {}

class OssDataFlow(opts: OssDataFlowOptions)(implicit
  s: Semantics = Semantics.fromList(DefaultSemantics().elements ++ opts.extraFlows)
) extends LayerCreator:

  override val overlayName: String = OssDataFlow.overlayName
  override val description: String = OssDataFlow.description

  override def create(context: LayerCreatorContext, storeUndoInfo: Boolean): Unit =
    val cpg                  = context.cpg
    val semantics: Semantics = OssDataFlow.withLanguageFlows(s, cpg)
    // The static-member pass runs after the per-method reaching definitions: it adds the edges
    // BETWEEN methods that a per-method computation cannot see - a value parked in a static field
    // by one method and read by another.
    val enhancementExecList =
        Iterator(
          new DataDepsPass(cpg, opts.maxNumberOfDefinitions, opts.useFluxEngine)(using semantics),
          new StaticMemberDefUsePass(cpg)
        )
    enhancementExecList.zipWithIndex.foreach { case (pass, index) =>
        runPass(pass, context, storeUndoInfo, index)
    }
end OssDataFlow
