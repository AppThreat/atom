package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, ProgramCallSites}
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
  *     besides its own in `nested-macro` tags; the front ends also keep a call for every macro use
  *     the expression tree itself lost (a constant the front end folded, an invocation nested in
  *     another's arguments or definition).
  *
  * A use is a reference from project code to something a header outside the project declares. The
  * graph names the project's own files relative to the input root and everything else by its
  * absolute path, so the resolved path decides what is outside, never a name. `module` is the
  * declaring header relative to its include root, as the package layout spells it: after the
  * include directory of an installed package (`openssl/ssl.h` for a Homebrew prefix's
  * `.../include/openssl/ssl.h`), after the platform's own roots (`c++/v1/string`'s `string`,
  * `sys/types.h`), and the file's own name anywhere else. The platform and compiler roots are a
  * dependency (`builtin: true`), decided from where the header resolved.
  *
  * Positions follow the oracle the slice is scored against: a macro use sits at its invocation
  * site, a nested macro at the outermost invocation there, and a call a project macro's body spells
  * at the macro's own definition - the site a reader attributes it to - never at each expansion. A
  * call spelled inside a library macro's body is part of that macro's use and gets no record of its
  * own. Nothing nobody wrote becomes a record: no include lines, no compiler-inserted conversions,
  * no type mentions in declarations, no position with `line <= 0`.
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

  /** A macro definition's spelling as the frontends write it in a call's method full name. */
  private val MacroFullName = "^(.+):(\\d+):(\\d+):([^:]+):(\\d+)$".r

  // ----------------------------------------------------------------------------------- the project
  /** The input root as the atom's metadata names it, and as the file system resolves it: a
    * declaring-header tag spells an absolute path, which the root itself may or may not have had
    * spelled through a symlink.
    */
  private val roots: Seq[String] =
      atom.metaData.root.headOption.toSeq.flatMap(root =>
          Seq(root, Try(Paths.get(root).toRealPath().toString).getOrElse(root))
      ).map(_.stripSuffix(java.io.File.separator)).filter(_.nonEmpty).distinct

  /** Whether `file` is one of the project's own: the graph names those relative to the input root,
    * everything else by an absolute path.
    */
  private def isProjectFile(file: String): Boolean =
      file.nonEmpty && !file.startsWith("<") && !Paths.get(file).isAbsolute

  /** Whether `header` (absolute, as the declaring-header tags spell it) is outside the project. */
  private def isExternalHeader(header: String): Boolean =
      header.nonEmpty && !header.startsWith("<") && Paths.get(header).isAbsolute &&
          !underRoot(header)

  /** The vendored part of a project path the `--external-paths` pattern names (`third_party/zlib`
    * for `third_party/zlib/zlib.h`), the pattern's own group when it has one; `None` when the
    * pattern names none of it.
    */
  private def vendoredPath(file: String): Option[String] =
      externalPaths.flatMap(_.r.findFirstMatchIn(file.replace('\\', '/')).map { m =>
        val matched = if m.groupCount > 0 && m.group(1) != null then m.group(1) else m.matched
        matched.stripSuffix("/")
      })

  private def underRoot(path: String): Boolean = roots.exists(root =>
      path == root || path.startsWith(root + java.io.File.separator)
  )

  /** A header under the input root as the graph names the project's own files, relative to it;
    * anything else unchanged. A macro's definition can be spelled by an absolute path that is the
    * project's own through a symlink the root was not spelled through.
    */
  private def projectRelative(header: String): String =
      roots.find(root => header == root || header.startsWith(root + java.io.File.separator))
          .map(root => header.substring(root.length + java.io.File.separator.length))
          .getOrElse(header)

  /** An external header as the dependency's layout spells it, with the platform and compiler roots
    * marked builtin: after the include directory of an installed package or the platform's own
    * roots, else the header's own name (the location says nothing beyond the file).
    */
  private def moduleOf(header: String): Option[(String, Boolean)] =
      // a vendored header under the project root is a dependency when the option names it: the file
      // is the project's by path, someone else's by meaning
      if underRoot(header) then vendoredPath(projectRelative(header)).map(_ -> false)
      else if !isExternalHeader(header) then None
      else if header.contains("/usr/include/c++/v1/") then
        Some(header.substring(header.indexOf("/usr/include/c++/v1/") + 21) -> true)
      else if header.contains("/usr/include/") then
        Some(header.substring(header.indexOf("/usr/include/") + 13) -> true)
      else if header.contains(".framework/") then
        Some(header.substring(header.indexOf(".framework/") + 12) -> true)
      else if header.contains("/lib/clang/") || header.contains("/lib/gcc/") then
        Some(substringAfterLast(header, '/') -> true)
      else if PackagePrefixes.exists(header.contains) then
        // an installed package: its headers live under .../include/
        val at = header.indexOf("/include/")
        if at >= 0 then Some(header.substring(at + 9) -> false)
        else
          Some(
            substringAfterLast(header, '/') -> false
          )
      else Some(substringAfterLast(header, '/') -> false)

  /** Where installed packages keep their headers on this class of hosts. */
  private val PackagePrefixes =
      Seq("/opt/homebrew/opt/", "/opt/homebrew/Cellar/", "/usr/local/opt/", "/usr/local/Cellar/")

  private def substringAfterLast(s: String, c: Char): String =
      s.lastIndexOf(c) match
        case -1 => s
        case at => s.substring(at + 1)

  // ------------------------------------------------------------------------------ the macro context
  /** The INLINED macro calls by their invocation index, for the calls their expansions produced. */
  private lazy val macroCallByInvocation: Map[String, Call] =
      atom.call.dispatchType(DispatchTypes.INLINED).flatMap { c =>
          c.tag.nameExact("macro-invocation").value.headOption.map(_ -> c)
      }.toMap

  /** What a call inside a macro expansion is part of: the invocation's own macro (`None` when the
    * expression tree carries no such invocation), with the file its definition sits in and its
    * first line.
    */
  private def enclosingMacro(c: Call): Option[(String, Int)] =
      c.tag.nameExact("macro-invocation").value.headOption.flatMap { index =>
          macroCallByInvocation.get(index).flatMap { invocation =>
              invocation.methodFullName match
                case MacroFullName(file, firstLine, _, _, _) => Some((file, firstLine.toInt))
                case _                                       => None
          }
      }

  /** Where a call inside a macro expansion was spelled, when the frontend kept it (EDG's
    * `macro-origin`, `<file>:<line>:<col>` relative to the root for project files).
    */
  private def spelledAt(c: Call): Option[(String, Int)] =
      c.tag.nameExact(MacroOrigin).value.headOption.flatMap {
          case s"$file:$line:$col" => Try((file, line.toInt)).toOption
          case _                   => None
      }

  // ------------------------------------------------------------------------------------ calculate
  def calculate(): ProgramCallSites =
    macroUses()
    calls()
    references()
    fieldAccesses()
    collector.result()

  /** Every macro invocation at a project site whose macro a header outside the project defines: the
    * invocation's own call, and each macro it names in its expansion at the same site.
    */
  private def macroUses(): Unit =
      atom.call.dispatchType(DispatchTypes.INLINED).foreach { c =>
          MacroFullName.unapplySeq(c.methodFullName) match
            case Some(Seq(file, _, _, name, _)) =>
                // the macros this invocation's expansion names are uses at its own site,
                // whatever file the invocation's own macro is defined in
                c.tag.nameExact(NestedMacro).value.foreach {
                    case s"$nested@$nestedFile" if isExternalHeader(nestedFile) =>
                        moduleOf(nestedFile).foreach((m, b) => addMacroRecord(c, m, nested, b))
                    case _ =>
                }
                if isExternalHeader(file) then
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

  /** Calls whose function, method or constructor a header outside the project declares. A call
    * spelled inside a project macro's body is a use at the macro's own definition, not at each
    * expansion; one spelled in a library macro's body is that macro's use and no record of its own.
    */
  private def calls(): Unit =
      atom.call.where(_.tag.nameExact(CalleeDeclaredIn)).foreach { c =>
        val header      = c.tag.nameExact(CalleeDeclaredIn).value.headOption.getOrElse("")
        val constructed = c.tag.nameExact("constructs-type").value.headOption.getOrElse("")
        // a call inside a macro expansion belongs to the macro's own spelling
        val inMacro = enclosingMacro(c)
        val origin  = spelledAt(c)
        val (macroFile, macroExternal) = inMacro
            .map((file, _) => file -> isExternalHeader(file))
            .getOrElse("" -> false)
        val originExternal = origin.exists((file, _) => isExternalHeader(file))
        if originExternal || (inMacro.isDefined && macroExternal) then
          // the library macro's own use covers this site
          collector.skip(c)
        else
          val (file, line, col): (String, Int, Integer) =
              if inMacro.isDefined then
                // the project macro's definition: where its body spells the call (EDG), else
                // the definition's own first line (CDT)
                origin match
                  case Some((atFile, atLine)) if isProjectFile(projectRelative(atFile)) =>
                      (
                        projectRelative(atFile),
                        atLine,
                        c.columnNumber.getOrElse(Integer.valueOf(-1))
                      )
                  case Some(_) => (c.method.filename, -1, Integer.valueOf(-1))
                  case None =>
                      inMacro.foreach((macroDefFile, _) =>
                          if !isProjectFile(projectRelative(macroDefFile)) then
                            collector.skip(c)
                      )
                      (
                        projectRelative(macroFile),
                        inMacro.map(_._2).getOrElse(-1),
                        Integer.valueOf(-1)
                      )
              else
                (
                  c.method.filename,
                  c.lineNumber.map(_.intValue).getOrElse(-1),
                  c.columnNumber.getOrElse(Integer.valueOf(-1))
                )
          if line <= 0 then collector.skip(c)
          else
            moduleOf(header) match
              case None => collector.skip(c)
              case Some((module, builtin)) =>
                  val method = c.method
                  if !isProjectFile(file) then collector.skip(c)
                  else
                    collector.add(c) {
                        val kind = if constructed.nonEmpty then "new" else "call"
                        val via = if kind == "new" then "binding"
                        else if c.methodFullName.contains('.') &&
                          !c.methodFullName.startsWith("<")
                        then "type"
                        else "binding"
                        CallSiteRecord(
                          file,
                          line,
                          col,
                          method.fullName,
                          module,
                          if constructed.nonEmpty then constructed else c.name,
                          kind,
                          via,
                          c.methodFullName,
                          builtin = builtin
                        )
                    }
                  end if
          end if
        end if
      }

  /** Identifiers that read a global or an enumerator a header outside the project declares, and
    * functions named as values.
    */
  private def references(): Unit =
    atom.identifier.where(_.tag.nameExact(RefDeclaredIn)).foreach { i =>
      val header = i.tag.nameExact(RefDeclaredIn).value.headOption.getOrElse("")
      moduleOf(header) match
        case None => collector.skip(i)
        case Some((module, builtin)) =>
            methodOf(i) match
              case None => collector.skip(i)
              case Some(method) =>
                  val file = method.filename
                  val line = i.lineNumber.map(_.intValue).getOrElse(-1)
                  if !isProjectFile(file) || line <= 0 then collector.skip(i)
                  else
                    collector.add(i) {
                        CallSiteRecord(
                          file,
                          line,
                          i.columnNumber.getOrElse(-1),
                          method.fullName,
                          module,
                          i.name,
                          "ref",
                          "binding",
                          i.name,
                          builtin = builtin
                        )
                    }
      end match
    }
    atom.methodRef.where(_.tag.nameExact(RefDeclaredIn)).foreach { r =>
      val header = r.tag.nameExact(RefDeclaredIn).value.headOption.getOrElse("")
      moduleOf(header) match
        case None => collector.skip(r)
        case Some((module, builtin)) =>
            methodOf(r) match
              case None => collector.skip(r)
              case Some(method) =>
                  val file = method.filename
                  val line = r.lineNumber.map(_.intValue).getOrElse(-1)
                  if !isProjectFile(file) || line <= 0 then collector.skip(r)
                  else
                    collector.add(r) {
                        CallSiteRecord(
                          file,
                          line,
                          r.columnNumber.getOrElse(-1),
                          method.fullName,
                          module,
                          r.methodFullName,
                          "ref",
                          "binding",
                          r.methodFullName,
                          builtin = builtin
                        )
                    }
      end match
    }
  end references

  /** A `p->f` or `o.f` through a struct or class a header outside the project declares. */
  private def fieldAccesses(): Unit =
      atom.call
          .nameExact("<operator>.fieldAccess", "<operator>.indirectFieldAccess")
          .where(_.tag.nameExact(TypeDeclaredIn))
          .foreach { access =>
            val header = access.tag.nameExact(TypeDeclaredIn).value.headOption.getOrElse("")
            moduleOf(header) match
              case None => collector.skip(access)
              case Some((module, builtin)) =>
                  access.argument.collectFirst { case f: FieldIdentifier => f.canonicalName }
                      .foreach { field =>
                          methodOf(access) match
                            case None => collector.skip(access)
                            case Some(method) =>
                                val file = method.filename
                                val line = access.lineNumber.map(_.intValue).getOrElse(-1)
                                if !isProjectFile(file) || line <= 0 then collector.skip(access)
                                else
                                  collector.add(access) {
                                      CallSiteRecord(
                                        file,
                                        line,
                                        access.columnNumber.getOrElse(-1),
                                        method.fullName,
                                        module,
                                        field,
                                        "type",
                                        "type",
                                        access.methodFullName,
                                        builtin = builtin
                                      )
                                  }
                      }
            end match
          }
end CCallSiteSource
