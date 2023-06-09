package io.appthreat.atom.slicing;

import io.joern.dataflowengineoss.language._
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.PropertyNames
import io.shiftleft.codepropertygraph.generated.nodes._
import io.shiftleft.semanticcpg.language._

import java.util.concurrent.atomic.AtomicBoolean

object DataFlowSlicing {

  implicit val resolver: ICallResolver = NoResolve
  private val excludeOperatorCalls     = new AtomicBoolean(true)

  def calculateDataFlowSlice(cpg: Cpg, config: DataFlowConfig): Option[DataFlowSlice] = {
    implicit val implicitConfig: BaseConfig = config
    excludeOperatorCalls.set(config.excludeOperatorCalls)

    (config.fileFilter match {
      case Some(fileName) => cpg.file.nameExact(fileName).ast.isCall
      case None           => cpg.call
    }).where(c => c.callee.isExternal)
      .flatMap {
        case c if excludeOperatorCalls.get() && c.name.startsWith("<operator") => None
        case c                                                                 => Some(c)
      }
      .flatMap { c =>
        val sinks           = config.sinkPatternFilter.map(filter => c.argument.code(filter).l).getOrElse(c.argument.l)
        val sliceNodes      = sinks.iterator.repeat(_.ddgIn)(_.maxDepth(config.sliceDepth).emit).dedup.l
        val sliceNodesIdSet = sliceNodes.id.toSet
        // Lazily set up the rest if the filters are satisfied
        lazy val sliceEdges = sliceNodes
          .flatMap(_.outE)
          .filter(x => sliceNodesIdSet.contains(x.inNode().id()))
          .map { e => SliceEdge(e.outNode().id(), e.inNode().id(), e.label()) }
          .toSet
        lazy val slice = Option(DataFlowSlice(sliceNodes.map(cfgNodeToSliceNode).toSet, sliceEdges))
        // Filtering
        sliceNodes match {
          case Nil                                                                     => None
          case _ if config.mustEndAtExternalMethod && !sinksEndAtExternalMethod(sinks) => None
          case _                                                                       => slice
        }
      }
      .reduceOption { (a, b) => DataFlowSlice(a.nodes ++ b.nodes, a.edges ++ b.edges) }
  }

  /** True if the sinks are either calls to external methods or are in external method stubs.
    */
  private def sinksEndAtExternalMethod(sinks: List[Expression]) =
    sinks.isCall.callee.isExternal.nonEmpty || sinks.method.isExternal.nonEmpty

  private def cfgNodeToSliceNode(cfgNode: CfgNode): SliceNode = {
    val sliceNode = SliceNode(
      cfgNode.id(),
      cfgNode.label,
      code = cfgNode.code,
      parentMethod = cfgNode.method.fullName,
      parentMethodSignature = cfgNode.method.signature,
      parentFile = cfgNode.file.name.headOption.getOrElse(""),
      lineNumber = cfgNode.lineNumber,
      columnNumber = cfgNode.columnNumber
    )
    cfgNode match {
      case n: Call =>
        sliceNode.copy(
          name = n.name,
          fullName = n.methodFullName,
          isExternal = n.callee.isExternal.headOption.getOrElse(false),
          signature = n.callee.signature.headOption.getOrElse(""),
          typeFullName = n.typeFullName
        )
      case n: Method =>
        sliceNode.copy(
          name = n.name,
          fullName = n.fullName,
          isExternal = n.isExternal,
          signature = n.signature,
          typeFullName = n.methodReturn.typeFullName
        )
      case n: Return    => sliceNode.copy(name = "RET", typeFullName = n.method.methodReturn.typeFullName)
      case n: MethodRef => sliceNode.copy(name = n.methodFullName, code = n.code)
      case n: TypeRef   => sliceNode.copy(name = n.typeFullName, code = n.code)
      case n =>
        sliceNode.copy(
          name = n.property(PropertyNames.NAME, ""),
          fullName = n.property(PropertyNames.FULL_NAME, ""),
          isExternal = n.property(PropertyNames.IS_EXTERNAL, false),
          typeFullName = n.property(PropertyNames.TYPE_FULL_NAME, ""),
          signature = n.property(PropertyNames.SIGNATURE, "")
        )
    }
  }

}
