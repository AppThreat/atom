package io.appthreat.atom.slicing

import io.joern.dataflowengineoss.language.*
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, PropertyNames}
import io.shiftleft.semanticcpg.language.*

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.concurrent.TrieMap

object DataFlowSlicing {

  implicit val resolver: ICallResolver = NoResolve
  val exec: ExecutorService            = Executors.newWorkStealingPool(Runtime.getRuntime.availableProcessors())
  private val excludeOperatorCalls     = new AtomicBoolean(true)
  private val nodeCache                = new TrieMap[Long, SliceNode]()
  private var language: Option[String] = _

  def calculateDataFlowSlice(cpg: Cpg, config: DataFlowConfig): Option[DataFlowSlice] = {
    language = cpg.metaData.language.headOption
    implicit val implicitConfig: BaseConfig = config
    excludeOperatorCalls.set(config.excludeOperatorCalls)

    val dataFlowSlice = (config.fileFilter match {
      case Some(fileRegex) => cpg.call.where(_.file.name(fileRegex))
      case None            => cpg.call
    })
      .where(c => c.callee.isExternal)
      .flatMap {
        case c
            if excludeOperatorCalls.get() && (c.name.startsWith("<operator") || c.methodFullName.contains(
              ".lambda$"
            ) || c.location.filename.contains("Exception")) =>
          None
        case c => Some(c)
      }
      .map(c => exec.submit(new TrackDataFlowTask(config, c)))
      .flatMap(TimedGet)
      .reduceOption { (a, b) => DataFlowSlice(a.nodes ++ b.nodes, a.edges ++ b.edges) }
    nodeCache.clear()
    dataFlowSlice
  }

  private def TimedGet(dsf: Future[Option[DataFlowSlice]]) = {
    try {
      dsf.get(5, TimeUnit.SECONDS)
    } catch {
      case err: Throwable => None
    }
  }

  private def DF_EDGES = Set(EdgeTypes.REACHING_DEF, EdgeTypes.CALL, EdgeTypes.REF)

  /** Convert cfg node to a sliceable node with backing cache
    */
  private def fromCfgNode(cfgNode: CfgNode): SliceNode = {
    nodeCache.getOrElseUpdate(cfgNode.id(), cfgNodeToSliceNode(cfgNode))
  }

  private def cfgNodeToSliceNode(cfgNode: CfgNode): SliceNode = {
    val sliceNode = SliceNode(
      cfgNode.id(),
      cfgNode.label,
      code = cfgNode.code,
      parentMethodName = cfgNode.method.fullName,
      parentMethodSignature = cfgNode.method.signature,
      parentFileName = cfgNode.file.name.headOption.getOrElse(""),
      parentPackageName = cfgNode.method.location.packageName,
      parentClassName = cfgNode.method.location.className,
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
      case n: Block => {
        var typeFullName = n.property(PropertyNames.TYPE_FULL_NAME, "")
        if (typeFullName == "ANY") {
          if (n.code.startsWith("[")) typeFullName = "Array"
          else if (n.code.startsWith("{")) typeFullName = "Object"
        }
        sliceNode.copy(
          name = n.property(PropertyNames.NAME, ""),
          fullName = n.property(PropertyNames.FULL_NAME, ""),
          isExternal = n.property(PropertyNames.IS_EXTERNAL, false),
          typeFullName = typeFullName,
          signature = n.property(PropertyNames.SIGNATURE, "")
        )
      }
      case n: Identifier => {
        var typeFullName = n.property(PropertyNames.TYPE_FULL_NAME, "")
        if (typeFullName == "ANY") {
          if (n.code.startsWith("[")) typeFullName = "Array"
          else if (n.code.startsWith("{")) typeFullName = "Object"
        }
        sliceNode.copy(
          name = n.property(PropertyNames.NAME, ""),
          fullName = n.property(PropertyNames.FULL_NAME, ""),
          isExternal = n.property(PropertyNames.IS_EXTERNAL, false),
          typeFullName = typeFullName,
          signature = n.property(PropertyNames.SIGNATURE, "")
        )
      }
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

  private class TrackDataFlowTask(config: DataFlowConfig, c: Call) extends Callable[Option[DataFlowSlice]] {
    override def call(): Option[DataFlowSlice] = {
      val sinks = config.sinkPatternFilter.map(filter => c.argument.code(filter).l).getOrElse(c.argument.l)
      // Slow operation
      val sliceNodes = sinks.repeat(_.ddgIn)(_.maxDepth(config.sliceDepth).emit).dedup.l
      // This is required to create paths
      val sliceNodesIdSet = sliceNodes.id.toSet
      // Lazily set up the rest if the filters are satisfied
      lazy val sliceEdges = sliceNodes
        .flatMap(_.outE)
        .filter(x => sliceNodesIdSet.contains(x.inNode().id()))
        .map { e => SliceEdge(e.outNode().id(), e.inNode().id(), e.label()) }
        .filter(e => DF_EDGES.contains(e.label))
        .toSet
      lazy val slice = Option(DataFlowSlice(sliceNodes.map(fromCfgNode).toSet, sliceEdges))
      if (sliceNodes.isEmpty) None else slice
    }
  }

}
