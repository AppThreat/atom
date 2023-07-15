package io.appthreat.atom.dataflows

import io.appthreat.atom.slicing.{DataFlowSlice, SliceNode}
import io.shiftleft.codepropertygraph.generated.EdgeTypes

import scala.collection.mutable
import java.util.concurrent.{Callable, Executors, Future, TimeUnit}

private class DataFlowGraph(nodes: Set[Option[DFNode]]) {

  private type Path      = List[Long]
  private type LabelPath = List[String]
  // Maximum number of data-flow paths to compute
  private val MAX_PATHS = 100

  private val USEFUL_PATH_LABELS = List("METHOD_PARAMETER_IN", "CALL")

  def paths: Set[Path] = {
    implicit val finalSet: mutable.Set[Path] = mutable.Set.empty
    implicit val nMap: Map[Long, DFNode]     = nodes.map(x => x.get.id -> x.get).toMap
    nodes.foreach { n =>
      val currPath      = List(n.get.id)
      val currLabelPath = List(n.get.label)
      follow(currPath, currLabelPath, n.get.out.flatMap(nMap.get))
    }
    finalSet.toSet
  }

  private def isSubList[A](short: List[A], long: List[A]): Boolean = {
    val sLong  = long.to(LazyList)
    val sShort = short.to(LazyList)
    sLong.tails exists (_.startsWith(sShort))
  }

  private def isSubList[A](lst: List[A])(implicit finalSet: mutable.Set[Path]): Boolean =
    finalSet.filterNot(_.size < lst.size).exists(xs => isSubList(lst, xs))

  /** A given path is useful if it starts with a METHOD_PARAMETER_IN contains at least 1 CALL and a METHOD_PARAMETER_IN
    * nodes
    */
  private def isUsefulPath(path: List[String]): Boolean =
    path.head == "METHOD_PARAMETER_IN" && path
      .filter(x => USEFUL_PATH_LABELS.contains(x))
      .size > 2

  /** Is there an existing path that starts and ends with the same node
    */
  private def isDuplicate(finalSet: mutable.Set[Path], path: Path): Boolean =
    finalSet.exists(apath => apath.head == path.head && apath.last == path.last)

  private def follow(currPath: List[Long], currLabelPath: List[String], outNodes: Set[DFNode])(implicit
    nMap: Map[Long, DFNode],
    finalSet: mutable.Set[Path]
  ): Unit = {
    outNodes.foreach { x =>
      val path      = currPath :+ x.id
      val labelPath = currLabelPath :+ x.label
      val queue     = x.out.filterNot(currPath.contains)
      if (queue.isEmpty) {
        if (isUsefulPath(labelPath) && !isDuplicate(finalSet, path) && !isSubList(path)) {
          finalSet.add(path)
        }
      } else if (finalSet.size < MAX_PATHS) {
        follow(path, labelPath, queue.flatMap(nMap.get))
      }
    }
  }

}

private case class DFNode(id: Long, isExternal: Boolean, label: String, in: Set[Long], out: Set[Long])

object DataFlowGraph {

  private def DF_EDGES = Set(EdgeTypes.REACHING_DEF, EdgeTypes.CALL, EdgeTypes.REF)
  val exec             = Executors.newWorkStealingPool(Runtime.getRuntime().availableProcessors())

  def buildFromSlice(slice: DataFlowSlice): DataFlowGraph = {
    val dfNodes = slice.nodes
      .flatMap {
        case n if n.fullName.startsWith("<operator") || n.name.equals("this") => None
        case n                                                                => Some(n)
      }
      .map(n => exec.submit(new DFNodeTask(slice, n)))
      .map(TimedGet)
      .filter(_.isDefined)
    new DataFlowGraph(dfNodes)
  }

  private def TimedGet(dfn: Future[DFNode]) = {
    try {
      Option(dfn.get(5, TimeUnit.SECONDS))
    } catch {
      case err: Throwable => None
    }
  }

  private class DFNodeTask(slice: DataFlowSlice, n: SliceNode) extends Callable[DFNode] {
    override def call(): DFNode = {
      val inEs  = slice.edges.filter(e => DF_EDGES.contains(e.label) && e.dst == n.id).map(_.src)
      val outEs = slice.edges.filter(e => DF_EDGES.contains(e.label) && e.src == n.id).map(_.dst)
      DFNode(n.id, n.isExternal, n.label, inEs, outEs)
    }
  }

}
