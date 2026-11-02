package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, ProgramCallSites}
import io.appthreat.ruby2atom.passes.GlobalTypes
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

/** The Ruby source of the callsites slice, on the graph ruby2atom builds.
  *
  * Every constant reference that resolves outside the project is a use (decision 0001, P1), at the
  * start of the written constant, with the `kind` the oracle gives it: `call` (the constant is a
  * call's receiver, or an attribute-style call's, and the method is not `new`; `path` is the
  * method), `new` (`X::Y.new`), `extends` (a superclass, or the argument of a receiver-less
  * `include`, `extend` or `prepend`) and `ref` (any other: arguments, `rescue` lists, `X::CONST`).
  * `require` lines are not runtime uses. `module` is the constant path with Ruby's `::`, resolved
  * the way Ruby resolves it: ruby2atom decides that a reference reaches outside the project (a
  * project class, module, constant or file-implied name is never a record) and lists the names the
  * enclosing classes would give it first; the first of those that Ruby's core library or a gem's
  * installed sources define wins, else the written path stays.
  *
  * A local bound exactly once to `Foo::Bar.new(...)` carries the class to `x.m`, which is a `type`
  * record at the call (`module` is the class, `path` the method); it needs the class to be provably
  * a plain `Class#new` one, which only Ruby's core library and standard library (the committed
  * list) can prove, as gem classes are never parsed.
  *
  * Ruby's core and standard library are a dependency (`builtin: true`), decided from the committed
  * list `ruby-builtins.txt` generated from a Ruby binary. A record's `purl` is the one gem that
  * defines the constant, from `Gemfile.lock` and the gem's installed sources (see [[RubyGemMap]]);
  * without them there is none.
  */
final class RubyCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "ruby"):

  private val gemMap = RubyGemMap(atom)

  /** The project's own types: a reference to one is not a dependency's (ruby2atom makes no
    * reference for those it resolves itself).
    */
  private val projectTypes: Set[String] = atom.typeDecl.isExternal(false).fullName.toSet

  /** The names of the classes and modules the project gives content to (a method of its own, or a
    * member): a core class it reopens to add methods is neither wholly the project's nor wholly the
    * library's, and the oracle does not credit it either way. A module that only nests others, the
    * way a project reopens a gem's namespace, is not one of them.
    */
  private val projectNames: Set[String] =
      atom.typeDecl.isExternal(false).filter(t =>
          t.method.exists(m => m.name != "<body>" && m.name != "initialize") || t.member.nonEmpty
      ).name.map(_.stripSuffix("<class>")).toSet

  private def lineOf(n: AstNode): Integer = n match
    case e: Expression => e.lineNumber.getOrElse(-1)
    case _             => -1

  private def colOf(n: AstNode): Integer = n match
    case e: Expression => e.columnNumber.getOrElse(-1)
    case _             => -1

  def calculate(): ProgramCallSites =
    references()
    typedCalls()
    collector.result()

  private def isTmp(n: AstNode): Boolean = n match
    case i: Identifier => i.name.startsWith("<tmp-")
    case _             => false

  private def argumentAt(call: Call, index: Int): Option[Expression] =
      call.argument.find(_.argumentIndex == index)

  /** What the reference is: the kind of use and, for a call, the method. */
  private def useOf(ref: TypeRef): (String, String) =
    // `(<tmp> = X).m(...)`: the constant sits in the assignment to a temporary of the receiver
    val holder = DeclarationRefs.parentOf(ref) match
      case Some(assignment: Call)
          if assignment.name == Operators.assignment &&
              argumentAt(assignment, 1).exists(isTmp) =>
          assignment
      case _ => ref
    DeclarationRefs.parentOf(holder) match
      case Some(_: TypeDecl)         => ("extends", "")
      case Some(_: ControlStructure) => ("ref", "")
      case Some(index: Call)
          if index.name == Operators.indexAccess && argumentAt(index, 1).exists(
            _.id() == holder.id()
          ) =>
          // `Migration[7.2]` and `CONFIG[:key]` call `[]` on the constant
          ("call", "[]")
      case Some(access: Call)
          if access.name == Operators.fieldAccess && argumentAt(access, 1).exists(
            _.id() == holder.id()
          ) =>
          memberUse(access)
      case Some(call: Call)
          if RubyCallSiteSource.Mixins.contains(call.name) && receiverIsSelf(call) =>
          ("extends", "")
      case _ => ("ref", "")
  end useOf

  /** A field access on a constant is a call of the member: the method of a call that has this
    * access as its receiver, or an attribute-style call on its own (`Rails.root`).
    */
  private def memberUse(access: Call): (String, String) =
    val member = argumentAt(access, 2).collect {
        // the frontend writes a lowercase member of a non-constant target as `@name`
        case f: FieldIdentifier => f.canonicalName.stripPrefix("@")
        case i: Identifier      => i.name
    }.getOrElse("")
    if member.isEmpty then ("ref", "")
    else if member.head.isUpper then ("ref", "")
    else if member == RubyCallSiteSource.Constructor then ("new", "new")
    else ("call", member)

  /** `include X` written without a receiver is a call on `self`. */
  private def receiverIsSelf(call: Call): Boolean =
      argumentAt(call, 0).forall {
          case i: Identifier => i.name == "self"
          case _             => false
      }

  /** The constant a reference means, as segments: the first of the names its enclosing classes give
    * it that core Ruby or an installed gem defines, else the written path.
    */
  private def resolve(ref: TypeRef): List[String] =
    val bundled = ref.typeFullName.startsWith(s"${GlobalTypes.builtinPrefix}.")
    val written =
        (if bundled then ref.typeFullName.stripPrefix(s"${GlobalTypes.builtinPrefix}.")
         else ref.typeFullName).split('.').toList
    // a candidate is the written path under an enclosing class or module: Ruby takes it when the
    // library or a gem defines that enclosing name's constant, the candidate's own first name
    ref.dynamicTypeHintFullName.map(_.split('.').toList).find { candidate =>
      val owner = candidate.take(candidate.size - written.size + 1)
      RubyBuiltins.exact(owner).isDefined || gemMap.definesExactly(owner)
    }.getOrElse(written)

  /** The enclosing type's declaration, for a reference that sits in a class body the frontend put
    * under a namespace block (`class Admin::User < Base`) instead of under a method.
    */
  private def enclosingTypeDeclOf(n: AstNode): Option[TypeDecl] =
      Iterator
          .iterate(DeclarationRefs.parentOf(n))(_.flatMap(DeclarationRefs.parentOf))
          .takeWhile(_.isDefined).flatten.collectFirst { case t: TypeDecl => t }

  /** The file and the method a reference is in; the method is empty for a class body that is not in
    * one.
    */
  private def locationOf(ref: TypeRef): Option[(String, String)] =
      methodOf(ref).map(m => (m.filename, m.fullName)).orElse(
        enclosingTypeDeclOf(ref).map(t => (t.filename, ""))
      )

  private def references(): Unit =
      atom.typeRef.filterNot(t => projectTypes.contains(t.typeFullName)).foreach { ref =>
          locationOf(ref).foreach { (file, methodName) =>
            val path = resolve(ref)
            // a core class the project reopens is not credited to the library
            val reopened = path.size == 1 && projectNames.contains(path.head)
            if reopened then ()
            // a constant the parser made up while lowering (`Array` for `Array(value)`) has no
            // position: nobody wrote it
            else if lineOf(ref) <= 0 then ()
            else if file.nonEmpty && file != "<empty>" then
              val (kind, member) = useOf(ref)
              val builtin        = RubyBuiltins.contains(path)
              collector.add(ref) {
                  CallSiteRecord(
                    file,
                    lineOf(ref),
                    colOf(ref),
                    methodName,
                    path.mkString("::"),
                    member,
                    kind,
                    "binding",
                    ref.typeFullName,
                    builtin,
                    purl = if builtin then "" else gemMap.purlOf(path)
                  )
              }
            else collector.skip(ref)
            end if
          }
      }

  /** `x.m` on a local that holds an instance of a class whose `new` is the plain one. */
  private def typedCalls(): Unit =
      atom.identifier.filter(i =>
          i.typeFullName != "ANY" && !i.name.startsWith("<") && i.name != "self" &&
              i._refOut.exists(_.isInstanceOf[Local])
      ).foreach { receiver =>
        val path = receiver.typeFullName.split('.').toList
        if RubyBuiltins.exact(path).exists(e => e.kind == 'c' && e.plainNew) then
          DeclarationRefs.parentOf(receiver).foreach {
              case call: Call if !call.name.startsWith("<operator>") =>
                  typedRecord(receiver, call, call.name, path)
              case access: Call if access.name == Operators.fieldAccess =>
                  argumentAt(access, 2).collect { case f: FieldIdentifier =>
                      f.canonicalName.stripPrefix("@")
                  }.foreach(typedRecord(receiver, access, _, path))
              case _ =>
          }
      }

  private def typedRecord(
    receiver: Identifier,
    call: Call,
    member: String,
    path: List[String]
  ): Unit =
      methodOf(call).foreach { method =>
          if lineOf(call) > 0 && method.filename.nonEmpty then
            collector.add(call) {
                CallSiteRecord(
                  method.filename,
                  lineOf(call),
                  colOf(call),
                  method.fullName,
                  path.mkString("::"),
                  member,
                  "call",
                  "type",
                  call.methodFullName,
                  builtin = true
                )
            }
          else collector.skip(call)
      }
end RubyCallSiteSource

object RubyCallSiteSource:

  /** The method a field access on a constant calls when it constructs. */
  val Constructor = "initialize"

  /** The calls that mix a module into the class they are written in. */
  val Mixins: Set[String] = Set("include", "extend", "prepend")
