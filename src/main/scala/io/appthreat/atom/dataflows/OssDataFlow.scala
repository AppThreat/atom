package io.appthreat.atom.dataflows

import io.appthreat.atom.passes.DataDepsPass
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.passes.reachingdef.StaticMemberDefUsePass
import io.appthreat.dataflowengineoss.semanticsloader.{FlowSemantic, Semantics}
import io.shiftleft.semanticcpg.layers.{LayerCreator, LayerCreatorContext, LayerCreatorOptions}

object OssDataFlow:
  val overlayName: String = "dataflowOss"
  val description: String = "Layer to support the atom data flow tracker"

  def defaultOpts = new OssDataFlowOptions()

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
    val cpg = context.cpg
    // The static-member pass runs after the per-method reaching definitions: it adds the edges
    // BETWEEN methods that a per-method computation cannot see - a value parked in a static field
    // by one method and read by another.
    val enhancementExecList =
        Iterator(
          new DataDepsPass(cpg, opts.maxNumberOfDefinitions, opts.useFluxEngine),
          new StaticMemberDefUsePass(cpg)
        )
    enhancementExecList.zipWithIndex.foreach { case (pass, index) =>
        runPass(pass, context, storeUndoInfo, index)
    }
end OssDataFlow
