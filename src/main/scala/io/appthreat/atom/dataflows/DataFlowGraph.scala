package io.appthreat.atom.dataflows

import io.appthreat.atom.slicing.{DataFlowSlice, SliceNode}
import io.shiftleft.codepropertygraph.generated.EdgeTypes

import scala.collection.mutable

private class DataFlowGraph(nodes: Set[DFNode]) {

  private type Path = List[Long]

  def paths: Set[Path] = {
    implicit val finalSet: mutable.Set[Path] = mutable.Set.empty
    implicit val nMap: Map[Long, DFNode]     = nodes.map(x => x.id -> x).toMap
    nodes.foreach { n =>
      val currPath = List(n.id)
      follow(currPath, n.out.flatMap(nMap.get))
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

  private def follow(currPath: List[Long], outNodes: Set[DFNode])(implicit
    nMap: Map[Long, DFNode],
    finalSet: mutable.Set[Path]
  ): Unit = {
    outNodes.foreach { x =>
      val path  = currPath :+ x.id
      val queue = x.out.filterNot(currPath.contains)
      if (queue.isEmpty) {
        if (!isSubList(path)) finalSet.add(path)
      } else {
        follow(path, queue.flatMap(nMap.get))
      }
    }
  }

}

private case class DFNode(id: Long, in: Set[Long], out: Set[Long])

object DataFlowGraph {

  private def DF_EDGES = Set(EdgeTypes.REACHING_DEF, EdgeTypes.CALL)

  def buildFromSlice(slice: DataFlowSlice): DataFlowGraph = {
    val dfNodes = slice.nodes.map { n =>
      val inEs  = slice.edges.filter(e => DF_EDGES.contains(e.label) && e.dst == n.id).map(_.src)
      val outEs = slice.edges.filter(e => DF_EDGES.contains(e.label) && e.src == n.id).map(_.dst)
      DFNode(n.id, inEs, outEs)
    }
    new DataFlowGraph(dfNodes)
  }

}
