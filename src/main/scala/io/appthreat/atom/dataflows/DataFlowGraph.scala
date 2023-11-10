package io.appthreat.atom.dataflows

import io.appthreat.atom.slicing.{DataFlowSlice, SliceNode}
import io.shiftleft.codepropertygraph.generated.EdgeTypes

import java.util.concurrent.*
import scala.collection.mutable

private class DataFlowGraph(nodes: Set[Option[DFNode]]):

    private type Path = List[Long]
    // Maximum number of data-flow paths to compute
    private val MAX_PATHS = 100

    def paths: Set[Path] =
        implicit val finalSet: mutable.Set[Path] = mutable.Set.empty
        implicit val nMap: Map[Long, DFNode]     = nodes.map(x => x.get.id -> x.get).toMap
        nodes.foreach { n =>
            val currPath = List(n.get.id)
            follow(currPath, n.get.out.flatMap(nMap.get))
        }
        finalSet.toSet

    private def isSubList[A](short: List[A], long: List[A]): Boolean =
        val sLong  = long.to(LazyList)
        val sShort = short.to(LazyList)
        sLong.tails exists (_.startsWith(sShort))

    private def isSubList[A](lst: List[A])(implicit finalSet: mutable.Set[Path]): Boolean =
        finalSet.filterNot(_.size < lst.size).exists(xs => isSubList(lst, xs))

    /** Is there an existing path that starts and ends with the same node
      */
    private def isDuplicate(finalSet: mutable.Set[Path], path: Path): Boolean =
        finalSet.exists(apath =>
            apath.headOption == path.headOption && apath.lastOption == path.lastOption
        )

    private def follow(currPath: List[Long], outNodes: Set[DFNode])(
      implicit
      nMap: Map[Long, DFNode],
      finalSet: mutable.Set[Path]
    ): Unit =
        outNodes.foreach { x =>
            val path  = currPath :+ x.id
            val queue = x.out.filterNot(currPath.contains)
            if queue.isEmpty then
                if !isDuplicate(finalSet, path) && !isSubList(path) then
                    finalSet.add(path)
            else if finalSet.size < MAX_PATHS then
                follow(path, queue.flatMap(nMap.get))
        }
end DataFlowGraph

private final case class DFNode(
  id: Long,
  isExternal: Boolean,
  label: String,
  in: Set[Long],
  out: Set[Long]
)

object DataFlowGraph:

    private def DF_EDGES =
        Set(EdgeTypes.REACHING_DEF, EdgeTypes.CALL, EdgeTypes.REF)
    val exec: ExecutorService =
        Executors.newWorkStealingPool(Runtime.getRuntime.availableProcessors / 2)

    def buildFromSlice(slice: DataFlowSlice): DataFlowGraph =
        val dfNodes = slice.nodes
            .flatMap {
                case n if n.fullName.startsWith("<operator") || n.name.equals("this") => None
                case n                                                                => Some(n)
            }
            .map(n => exec.submit(new DFNodeTask(slice, n)))
            .map(TimedGet)
            .filter(_.isDefined)
        new DataFlowGraph(dfNodes)

    private def TimedGet(dfn: Future[DFNode]) =
        try
            Option(dfn.get(5, TimeUnit.SECONDS))
        catch
            case _: Throwable => None

    private class DFNodeTask(slice: DataFlowSlice, n: SliceNode) extends Callable[DFNode]:
        override def call(): DFNode =
            val inEs =
                slice.edges.filter(e => DF_EDGES.contains(e.label) && e.dst == n.id).map(_.src)
            val outEs =
                slice.edges.filter(e => DF_EDGES.contains(e.label) && e.src == n.id).map(_.dst)
            DFNode(n.id, n.isExternal, n.label, inEs, outEs)
end DataFlowGraph
