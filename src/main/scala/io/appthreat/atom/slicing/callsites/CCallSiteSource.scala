package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.ProgramCallSites
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import java.nio.file.Paths
import scala.collection.mutable
import scala.util.Try

/** The C and C++ source of the callsites slice, for the atoms both C frontends build (CDT through
  * c2cpg, EDG through edg2atom; both name their language NEWC). The two frontends write the same
  * facts, and this source reads only those:
  *
  *   - a call carries `callee-declared-in` when a header declares its function, method or
  *     constructor and the file itself does not; a constructor (a declaration's constructor
  *     initializer, a `new`) also says `constructs-type`;
  *   - an identifier that reads a global or an enumerator, and a method ref for a function named as
  *     a value, carry `ref-declared-in`;
  *   - a `p->f` member access through a header's struct or class carries `type-declared-in`;
  *   - a macro invocation is an INLINED call whose method full name spells its definition
  *     (`<file>:<line>:<last line>:<NAME>:<params>`), and names every macro its expansion invokes
  *     besides its own in `nested-macro` tags. Every node of an expansion carries the invocation's
  *     index (`macro-invocation`, numbered per file by CDT and per translation unit by EDG), and
  *     EDG also says where its text was written (`macro-origin`). An argument's copy under the
  *     invocation carries `macro-argument-copy`.
  *
  * A use is a reference from project code to something a header outside the project declares. The
  * project is the input root and the build directories of its compilation database, where the build
  * writes the headers it generates (`CProjectLayout`); where a header sits decides the rest
  * (`CHeaders`), never its name.
  *
  * Positions follow the oracle the slice is scored against: a macro use sits at its invocation
  * site, a nested macro at the outermost invocation there, and a call a project macro's body spells
  * at the macro's own definition, never at each expansion. Whatever a library macro's body spells
  * (a call, a global, a field) is that macro's use and no record of its own; what the invocation's
  * own arguments spell is the project's, at the invocation. Nothing nobody wrote becomes a record:
  * no include lines, no compiler-inserted conversions, no type mentions in declarations, no
  * position with `line <= 0`.
  */
final class CCallSiteSource(atom: Cpg, externalPaths: Option[String] = None)
    extends BaseCallSiteSource(atom, "c"):

  // two macros of one header can share an invocation site: the member keeps them apart
  override protected def dedupeByMember: Boolean = true

  private val CalleeDeclaredIn = "callee-declared-in"
  private val RefDeclaredIn    = "ref-declared-in"
  private val TypeDeclaredIn   = "type-declared-in"
  private val NestedMacro      = "nested-macro"
  private val MacroOrigin      = "macro-origin"
  private val MacroInvocation  = "macro-invocation"
  private val ArgumentCopy     = "macro-argument-copy"
  private val MacroUse         = "macro-use"

  /** A macro definition's spelling as the frontends write it in a call's method full name. */
  private val MacroFullName = "^(.+):(\\d+):(\\d+):([^:]+):(\\d+)$".r

  // ----------------------------------------------------------------------------------- the project
  private val stored = CProjectLayout.read(atom)

  /** The vendored-code pattern of this run, else the one the atom was built with. */
  private val vendored: VendoredCode = VendoredCode.forAtom(atom, externalPaths)

  /** The input root as the metadata names it and as the file system resolves it, and the build
    * directories: the declaring-header tags spell absolute paths, through symlinks or not.
    */
  private val roots: Seq[String] =
    val root = atom.metaData.root.headOption.toSeq
    (root ++ root.map(r => Try(Paths.get(r).toRealPath().toString).getOrElse(r)))
        .map(r => CHeaders.slashes(r).stripSuffix("/"))
        .filter(_.nonEmpty)
        .distinct

  private val buildDirs: Seq[String] = stored.map(_.buildDirs).getOrElse(Nil)

  /** Whether `file` is one of the project's own source files: the graph names those relative to the
    * input root, everything else by an absolute path.
    */
  private def isProjectFile(file: String): Boolean =
      file.nonEmpty && !file.startsWith("<") && !Paths.get(file).isAbsolute

  /** A header under the input root as the graph names the project's own files, relative to it;
    * anything else unchanged.
    */
  private def projectRelative(header: String): String =
    val h = CHeaders.slashes(header)
    roots.find(r => h == r || h.startsWith(r + "/")).map(r => h.substring(r.length + 1))
        .getOrElse(header)

  /** What a declaring or defining header is to the project: `None` for its own (under the root or a
    * build directory, unless the vendored pattern names it), else the module and builtin flag.
    */
  private def moduleOf(header: String): Option[(String, Boolean)] =
      if header.isEmpty || header.startsWith("<") || !Paths.get(header).isAbsolute then None
      else if CHeaders.under(header, roots) then
        vendored.of(projectRelative(header)).map(v => v.module -> false)
      else if CHeaders.under(header, buildDirs) then None
      else Some(CHeaders.external(header))

  private def isExternal(header: String): Boolean = moduleOf(header).isDefined

  // ------------------------------------------------------------------------------ the macro context
  /** The file of the method a node sits in, which with its index names a macro invocation. */
  private def methodFile(n: AstNode): String = fileOf(n).getOrElse("")

  /** The INLINED macro calls by (file, invocation index). */
  private lazy val macroCallByInvocation: Map[(String, String), Call] =
      atom.call.dispatchType(DispatchTypes.INLINED).flatMap { c =>
          c.tag.nameExact(MacroInvocation).value.headOption.map(i => (methodFile(c), i) -> c)
      }.toMap

  private def tagValue(n: AstNode, name: String): Option[String] =
      n.tag.nameExact(name).value.headOption

  private def isArgumentCopy(n: AstNode): Boolean = tagValue(n, ArgumentCopy).contains("true")

  /** The invocation whose expansion produced `n` (not counting the invocation's own call node). */
  private def enclosingInvocation(n: AstNode): Option[Call] =
      if isArgumentCopy(n) then None
      else
        tagValue(n, MacroInvocation).flatMap(i => macroCallByInvocation.get((methodFile(n), i)))
            .filterNot(_ == n)

  /** The macro whose expansion produced `n`: the invocation's definition file and first line. */
  private def enclosingMacro(n: AstNode): Option[(String, Int)] =
      enclosingInvocation(n).flatMap(_.methodFullName match
        case MacroFullName(file, firstLine, _, _, _) => Some((file, firstLine.toInt))
        case _                                       => None
      )

  /** Whether the text that became `n` is one of the invocation's arguments: a node of the argument
    * copies spells the same code. The copy is the record, at the argument's own line.
    */
  private def writtenInArgument(n: AstNode): Boolean =
    val code = n match
      case e: Expression => e.code
      case _             => ""
    code.nonEmpty && enclosingInvocation(n).exists(inv =>
        inv.argument.filter(isArgumentCopy).ast.exists {
            case e: Expression => e.code == code && e.label == n.label
            case _             => false
        }
    )

  /** Where a node of a macro expansion was spelled, when the frontend kept it (EDG's
    * `macro-origin`, `<file>:<line>:<col>`, relative to the root for project files).
    */
  private def spelledAt(n: AstNode): Option[(String, Int)] =
      tagValue(n, MacroOrigin).flatMap {
          case s"$file:$line:$col" => line.toIntOption.map(file -> _)
          case _                   => None
      }

  /** Where the text that became `n` was written, by the oracle's rule: the file and line of the
    * node itself outside any macro and in an argument; the place a project macro's body spells it
    * (EDG says where; CDT, which does not, gives the definition's first line); `None` inside a
    * library macro's body, whose own use covers it.
    */
  private def writtenAt(n: AstNode, line: Option[Integer]): Option[(String, Int)] =
      if isArgumentCopy(n) then siteOf(n, line)
      else
        val origin  = spelledAt(n).filterNot((file, _) => file.isEmpty)
        val inMacro = enclosingMacro(n)
        origin match
          case Some((file, _)) if isExternal(file) => None
          case Some((file, at))
              if inMacro.isDefined || !isProjectFile(file) || file != methodFile(n) =>
              Some(projectRelative(file) -> at)
          case _ =>
              inMacro match
                case Some((defFile, _)) if isExternal(defFile)   => None
                case Some(_) if edgFiles.contains(methodFile(n)) => siteOf(n, line)
                // CDT: the argument copy records what an argument spells
                case Some(_) if writtenInArgument(n) => None
                case Some((defFile, first))          => Some(projectRelative(defFile) -> first)
                case None                            => siteOf(n, line)
  end writtenAt

  /** The files the EDG frontend built (edg2atom tags each FILE with its frontend). EDG positions
    * every node at its own text, so a node of a project macro's expansion that carries no
    * `macro-origin` (an argument of a variadic macro) is where it stands; CDT positions an
    * expansion's nodes at the invocation, and the definition is the nearest it says.
    */
  private lazy val edgFiles: Set[String] =
      atom.file.where(_.tag.nameExact("frontend").valueExact("edg")).name.toSet

  private def siteOf(n: AstNode, line: Option[Integer]): Option[(String, Int)] =
      line.map(l => methodFile(n) -> l.intValue)

  // ------------------------------------------------------------------------------------ calculate
  def calculate(): ProgramCallSites =
    macroUses()
    calls()
    references()
    fieldAccesses()
    val result = collector.result()
    // a record of a BOM component's code carries the component's purl (decision 0001, P5)
    val purlOfModule = vendored.components.groupBy(_.module).collect {
        case (m, cs) if cs.map(_.purl).distinct.size == 1 => m -> cs.head.purl
    }
    if purlOfModule.isEmpty then result
    else
      result.copy(callsites =
          result.callsites.map(r => r.copy(purl = purlOfModule.getOrElse(r.module, r.purl)))
      )

  /** Every macro invocation at a project site whose macro a header outside the project defines: the
    * invocation's own call, each macro it names in its expansion at the same site, and the
    * invocations no expression carries (EDG's `macro-use` tags on a file's method: a constant the
    * front end folded, such as an object-like macro in a `case` label).
    */
  private def macroUses(): Unit =
    invocationCalls()
    atom.method.where(_.tag.nameExact(MacroUse)).foreach { m =>
        m.tag.nameExact(MacroUse).value.foreach {
            case s"$name@$line:$col@$file" =>
                for
                  (module, builtin) <- moduleOf(file)
                  at                <- line.toIntOption
                  if at > 0 && isProjectFile(m.filename)
                do
                  val enclosing = methodAt(m.filename, at).getOrElse(m)
                  collector.add(m) {
                      CallSiteRecord(
                        m.filename,
                        at,
                        col.toIntOption.map(Integer.valueOf).getOrElse(Integer.valueOf(-1)),
                        enclosing.fullName,
                        module,
                        name,
                        "macro",
                        "macro",
                        s"$file:$name",
                        builtin = builtin
                      )
                  }
            case _ =>
        }
    }
  end macroUses

  /** The innermost method of `file` whose lines hold `line`. */
  private def methodAt(file: String, line: Int): Option[Method] =
      methodsByFile.getOrElse(file, Nil).filter(m =>
          m.lineNumber.exists(_.intValue <= line) && m.lineNumberEnd.exists(_.intValue >= line)
      ).minByOption(m => m.lineNumberEnd.get.intValue - m.lineNumber.get.intValue)

  private lazy val methodsByFile: Map[String, List[Method]] =
      atom.method.filterNot(_.name == "<global>").l.groupBy(_.filename)

  private def invocationCalls(): Unit =
      atom.call.dispatchType(DispatchTypes.INLINED).foreach { c =>
          MacroFullName.unapplySeq(c.methodFullName) match
            case Some(Seq(file, _, _, name, _)) =>
                // the macros this invocation's expansion names are uses at its own site,
                // whatever file the invocation's own macro is defined in
                c.tag.nameExact(NestedMacro).value.foreach {
                    case s"$nested@$nestedFile" =>
                        moduleOf(nestedFile).foreach((m, b) => addMacroRecord(c, m, nested, b))
                    case _ =>
                }
                moduleOf(file).foreach((module, builtin) =>
                    addMacroRecord(c, module, c.name, builtin)
                )
            case _ =>
      }

  private def addMacroRecord(c: Call, module: String, member: String, builtin: Boolean): Unit =
      methodOf(c) match
        case None => collector.skip(c)
        case Some(method) =>
            val file = method.filename
            val line = c.lineNumber.map(_.intValue).getOrElse(-1)
            if !isProjectFile(file) || line <= 0 then collector.skip(c)
            else
              collector.add(c) {
                  CallSiteRecord(
                    file,
                    line,
                    c.columnNumber.getOrElse(-1),
                    method.fullName,
                    module,
                    member,
                    "macro",
                    "macro",
                    c.methodFullName,
                    builtin = builtin
                  )
              }

  /** One record for `node` at the place its text was written, when that is a project file. */
  private def record(
    node: AstNode,
    line: Option[Integer],
    column: Option[Integer],
    header: String,
    member: String,
    kind: String,
    via: String,
    resolved: String
  ): Unit =
      (moduleOf(header), writtenAt(node, line), methodOf(node)) match
        case (Some((module, builtin)), Some((file, at)), Some(method))
            if isProjectFile(file) && at > 0 =>
            // inside a project macro's body the column is the invocation's, not the spelling's
            val col = if file == methodFile(node) && line.exists(_.intValue == at) then
              column.getOrElse(Integer.valueOf(-1))
            else Integer.valueOf(-1)
            collector.add(node) {
                CallSiteRecord(
                  file,
                  at,
                  col,
                  // text a macro definition spells sits in no method; naming one of the
                  // methods that expand it would depend on which expansion came first
                  if file == methodFile(node) then method.fullName else "",
                  module,
                  member,
                  kind,
                  via,
                  resolved,
                  builtin = builtin
                )
            }
        case _ => collector.skip(node)

  /** Calls whose function, method or constructor a header outside the project declares. A member
    * call through an object or a pointer is `type`; a constructor is `new`.
    */
  private def calls(): Unit =
      atom.call.where(_.tag.nameExact(CalleeDeclaredIn)).foreach { c =>
        val header      = tagValue(c, CalleeDeclaredIn).getOrElse("")
        val constructed = tagValue(c, "constructs-type").getOrElse("")
        val member = c.dispatchType != DispatchTypes.INLINED &&
            c.argument.argumentIndex(0).nonEmpty
        val (kind, via) =
            if constructed.nonEmpty then ("new", "binding")
            else if member then ("call", "type")
            else ("call", "binding")
        record(
          c,
          c.lineNumber,
          c.columnNumber,
          header,
          if constructed.nonEmpty then constructed else c.name,
          kind,
          via,
          c.methodFullName
        )
      }

  /** Identifiers that read a global or an enumerator a header outside the project declares, and
    * functions named as values.
    */
  private def references(): Unit =
    atom.identifier.where(_.tag.nameExact(RefDeclaredIn)).foreach { i =>
      val header = tagValue(i, RefDeclaredIn).getOrElse("")
      record(i, i.lineNumber, i.columnNumber, header, i.name, "ref", "binding", i.name)
    }
    atom.methodRef.where(_.tag.nameExact(RefDeclaredIn)).foreach { r =>
      val header = tagValue(r, RefDeclaredIn).getOrElse("")
      val name   = r.methodFullName.takeWhile(_ != ':')
      record(r, r.lineNumber, r.columnNumber, header, name, "ref", "binding", r.methodFullName)
    }

  /** A `p->f` or `o.f` through a struct or class a header outside the project declares. */
  private def fieldAccesses(): Unit =
      atom.call
          .nameExact("<operator>.fieldAccess", "<operator>.indirectFieldAccess")
          .where(_.tag.nameExact(TypeDeclaredIn))
          .foreach { access =>
            val header = tagValue(access, TypeDeclaredIn).getOrElse("")
            access.argument.collectFirst { case f: FieldIdentifier => f.canonicalName }.foreach {
                field =>
                    record(
                      access,
                      access.lineNumber,
                      access.columnNumber,
                      header,
                      field,
                      "type",
                      "type",
                      access.methodFullName
                    )
            }
          }
end CCallSiteSource
