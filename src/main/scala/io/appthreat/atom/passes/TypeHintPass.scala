package io.appthreat.atom.passes

import io.joern.x2cpg.passes.frontend.XTypeHintCallLinker
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Call
import io.shiftleft.semanticcpg.language.*
import overflowdb.traversal.Traversal

class TypeHintPass(cpg: Cpg) extends XTypeHintCallLinker(cpg) {

  override protected val pathSep = ':'

  override protected def calls: Traversal[Call] = cpg.call
    .or(_.nameNot("<operator>.*", "<operators>.*"), _.name("<operator>.new"))
    .filterNot(c => c.code.startsWith("$(") || c.code.startsWith("_tmp_") || c.code.startsWith("{"))
    .filter(c => calleeNames(c).nonEmpty && c.callee.isEmpty)

}
