package io.appthreat.atom.slicing

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import scala.jdk.CollectionConverters.*

/** Where a declaration is used, resolved through scopes rather than by name.
  *
  * A variable captured by a nested function is a separate LOCAL in that function, carrying a
  * `closureBindingId` whose CLOSURE_BINDING refers to the captured declaration, possibly through
  * further captures. Identifiers in the nested function refer to the inner local, so a
  * declaration's own `referencingIdentifiers` miss them. A shadowing local of the same name has no
  * closure binding and is never attributed to the outer declaration, which name matching would do.
  *
  * Built once per atom; lookups afterwards are map reads.
  */
final class DeclarationRefs(atom: Cpg):

  /** closure binding id -> the declaration it captures (one level). */
  private val capturedBy: Map[String, Declaration] =
      atom.graph.nodes("CLOSURE_BINDING").asScala
          .collect { case cb: ClosureBinding => cb }
          .flatMap(cb =>
              cb.closureBindingId.flatMap(id =>
                  cb._refOut.collectFirst { case d: Declaration => id -> d }
              )
          )
          .toMap

  /** The outermost declaration a local stands for, following captures. */
  def outermost(decl: Declaration): Declaration =
    var current = decl
    var depth   = 0
    var next    = capturedOf(current)
    while next.isDefined && depth < 32 do
      current = next.get
      depth += 1
      next = capturedOf(current)
    current

  private def capturedOf(decl: Declaration): Option[Declaration] = decl match
    case l: Local => l.closureBindingId.flatMap(capturedBy.get).filterNot(_.id() == l.id())
    case _        => None

  /** outermost declaration id -> the locals of nested functions that capture it. */
  private lazy val capturesOf: Map[Long, List[Local]] =
      atom.local.filter(_.closureBindingId.isDefined).l
          .groupBy(l => outermost(l).id())
          .view.mapValues(_.sortBy(_.id())).toMap

  /** The declaration an identifier refers to, through captures. */
  private def declarationOf(id: Identifier): Option[Declaration] =
      id._refOut.collectFirst { case d: Declaration => outermost(d) }

  /** outermost declaration id -> locals that are pure aliases of it: assigned exactly once, from an
    * identifier referring to it (`const f = flatten`). A use of the alias is a use of the original.
    * Locals assigned more than once may hold something else at a use, so they are not aliases.
    */
  private lazy val aliasesOf: Map[Long, List[Local]] =
    val assignedFrom = atom.call.nameExact(Operators.assignment).l.flatMap { a =>
        a.argument.l.sortBy(_.argumentIndex) match
          case (lhs: Identifier) :: rhs :: _ =>
              lhs._refOut.collectFirst { case l: Local => outermost(l) }.collect {
                  case l: Local => l -> rhs
              }
          case _ => None
    }
    assignedFrom.groupBy(_._1.id()).values.flatMap { assignments =>
        assignments match
          case List((alias, rhs: Identifier)) =>
              declarationOf(rhs).filterNot(_.id() == alias.id()).map(target => target.id() -> alias)
          case _ => None
    }.toList.groupBy(_._1).view.mapValues(_.map(_._2).sortBy(_.id())).toMap

  /** Identifiers referring to `decl`: directly, through any depth of closure capture, and through
    * pure aliases (excluding the alias's own defining assignment target).
    */
  def referencesOf(decl: Declaration): List[Identifier] = referencesOf(decl, 0)

  private def referencesOf(decl: Declaration, depth: Int): List[Identifier] =
    val own = decl match
      case l: Local             => l.referencingIdentifiers.l
      case p: MethodParameterIn => p.referencingIdentifiers.l
      case _                    => Nil
    val captured = capturesOf.getOrElse(decl.id(), Nil).flatMap(_.referencingIdentifiers.l)
    val viaAliases =
        if depth >= 8 then Nil
        else
          aliasesOf.getOrElse(decl.id(), Nil).flatMap { alias =>
              referencesOf(alias, depth + 1).filterNot(isAssignmentTarget)
          }
    own ++ captured ++ viaAliases

  private def isAssignmentTarget(id: Identifier): Boolean =
      id.argumentIndex == 1 && DeclarationRefs.parentOf(id).exists {
          case c: Call => c.name == Operators.assignment
          case _       => false
      }

end DeclarationRefs

object DeclarationRefs:

  private val chainOperators = Set(Operators.fieldAccess, Operators.indexAccess)

  /** How an expression rooted at a declaration takes part in a call. */
  enum Role:
    /** passed to the call at this argument (the expression is the argument node) */
    case Argument

    /** the call's callee/receiver: the declaration is called, or a member chain on it is */
    case Receiver

  final case class Involvement(call: Call, expr: Expression, role: Role)

  /** The AST parent, if any: some frontends leave orphan nodes, and `astParent` throws on them. */
  def parentOf(n: AstNode): Option[AstNode] = n._astIn.collectFirst { case p: AstNode => p }

  /** The top of the member-access chain rooted at `id` (`id`, `id.a`, `id.a.b`, `id[k]`, ...). */
  def chainTop(id: Expression): Expression =
    var top: Expression = id
    var climbing        = true
    while climbing do
      parentOf(top) match
        case Some(c: Call) if chainOperators.contains(c.name) && top.argumentIndex == 1 => top = c
        // The JS frontend lowers the callee of `a.b.c(x)` to `(_tmp_0 = a.b).c`, passing `_tmp_0`
        // as the call's argument 0, so a chain continues through that temporary's assignment.
        case Some(c: Call)
            if c.name == Operators.assignment && top.argumentIndex == 2 && isTmpAssignment(c) =>
            top = c
        case _ => climbing = false
    top

  private def isTmpAssignment(c: Call): Boolean =
      c.argument.exists {
          case i: Identifier => i.argumentIndex == 1 && i.name.startsWith("_tmp_")
          case _             => false
      } && (parentOf(c) match
        case Some(p: Call) => chainOperators.contains(p.name) && c.argumentIndex == 1
        case _             => false
      )

  /** A call that stands for a real invocation: not an operator, except `new` (a constructor call,
    * whose arguments and callee are the constructor's).
    */
  def isInvocation(c: Call): Boolean =
      !c.name.startsWith("<operator") || c.name == "<operator>.new"

  /** The invocation `expr` is the receiver of, if any. */
  def receiverOf(expr: Expression): Option[Call] =
      expr._receiverIn.collectFirst { case c: Call if isInvocation(c) => c }

  /** The non-operator call `expr` is a regular argument (index >= 1 or named) of, if any. */
  def argumentOf(expr: Expression): Option[Call] =
      parentOf(expr) match
        case Some(c: Call)
            if isInvocation(c) &&
                (expr.argumentIndex >= 1 || expr.argumentName.isDefined) &&
                !c.receiver.exists(_.id() == expr.id()) =>
            Some(c)
        case _ => None
end DeclarationRefs
