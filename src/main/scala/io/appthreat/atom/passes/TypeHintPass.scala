package io.appthreat.atom.passes

import io.appthreat.jssrc2cpg.passes.JavaScriptTypeHintCallLinker
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Call
import io.shiftleft.semanticcpg.language.*
import overflowdb.traversal.Traversal

/** Extends the JS/TS linker so a recovered `Type:method` hint resolves to the project's own
  * `<file>::program:Type:method` when exactly one exists (a constructor-injected service call in a
  * NestJS controller, `this.articleService.findAll`): otherwise the linker would create an external
  * stub and the call would never reach the internal method.
  */
class TypeHintPass(atom: Cpg) extends JavaScriptTypeHintCallLinker(atom):

  override protected def calls: Traversal[Call] = atom.call
      .or(_.nameNot("<operator>.*", "<operators>.*"), _.name("<operator>.new"))
      .filterNot(c =>
          c.code.startsWith("$(") || c.code.startsWith("_tmp_") || c.code.startsWith("{")
      )
      .filter(c => calleeNames(c).nonEmpty && c.callee.isEmpty)
