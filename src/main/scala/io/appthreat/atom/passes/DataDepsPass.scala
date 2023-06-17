package io.appthreat.atom.passes

import io.joern.dataflowengineoss.passes.reachingdef.{
  DataFlowProblem,
  DataFlowSolver,
  DdgGenerator,
  ReachingDefProblem,
  ReachingDefTransferFunction
}
import io.joern.dataflowengineoss.semanticsloader.Semantics
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes._
import io.shiftleft.semanticcpg.language._

import scala.collection.mutable

/** A pass that calculates reaching definitions ("data dependencies") based on ReachingDefPass
  */
class DataDepsPass(cpg: Cpg, maxNumberOfDefinitions: Int = 2000)(implicit s: Semantics)
    extends SafeConcurrentCpgPass[Method](cpg) {

  // If there are any regex method full names, load them early
  s.loadRegexSemantics(cpg)

  override def generateParts(): Array[Method] = cpg.method.toArray

  override def runOnPart(dstGraph: DiffGraphBuilder, method: Method): Unit = {
    if (
      method.isExternal || method.parameter.isEmpty || method.methodReturn.typeFullName
        .toLowerCase()
        .contains("boolean")
    ) {
      return
    }
    val problem = ReachingDefProblem.create(method)
    if (shouldBailOut(method, problem)) {
      return
    }

    val solution     = new DataFlowSolver().calculateMopSolutionForwards(problem)
    val ddgGenerator = new DdgGenerator(s)
    ddgGenerator.addReachingDefEdges(dstGraph, method, problem, solution)
  }

  /** Before we start propagating definitions in the graph, which is the bulk of the work, we check how many definitions
    * were are dealing with in total. If a threshold is reached, we bail out instead, leaving reaching definitions
    * uncalculated for the method in question. Users can increase the threshold if desired.
    */
  private def shouldBailOut(method: Method, problem: DataFlowProblem[StoredNode, mutable.BitSet]): Boolean = {
    val transferFunction = problem.transferFunction.asInstanceOf[ReachingDefTransferFunction]
    // For each node, the `gen` map contains the list of definitions it generates
    // We add up the sizes of these lists to obtain the total number of definitions
    val numberOfDefinitions = transferFunction.gen.foldLeft(0)(_ + _._2.size)
    if (numberOfDefinitions > maxNumberOfDefinitions) {
      true
    } else {
      false
    }
  }

}
