package io.appthreat.atom.passes

import io.appthreat.x2cpg.passes.frontend.XTypeHintCallLinker
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Call
import io.shiftleft.semanticcpg.language.*
import overflowdb.traversal.Traversal

class TypeHintPass(atom: Cpg) extends XTypeHintCallLinker(atom):

  override protected val pathSep = ':'

  override protected def calls: Traversal[Call] = atom.call
      .or(_.nameNot("<operator>.*", "<operators>.*"), _.name("<operator>.new"))
      .filterNot(c =>
          c.code.startsWith("$(") || c.code.startsWith("_tmp_") || c.code.startsWith("{")
      )
      .filter(c => calleeNames(c).nonEmpty && c.callee.isEmpty)
