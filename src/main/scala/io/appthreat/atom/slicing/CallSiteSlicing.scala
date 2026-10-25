package io.appthreat.atom.slicing

import io.appthreat.atom.slicing.CallSiteSlicing.BindingResolver
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import java.nio.file.Paths
import scala.collection.mutable
import scala.util.Try

/** One record per external use site: where and how a module import is used. See [[CallSiteRecord]]
  * for the fields.
  *
  * Every record carries the position of the use itself (the identifier, the member name, the
  * decorator), never a position borrowed from another call. `kind` says what the use is (call, new,
  * jsx, arg, ref, decorator, extends, tag); `via` says how the module was resolved:
  *   - `binding`: the identifier resolves, scope-correctly through captures, to an imported binding
  *     of its file;
  *   - `type`: the callee's resolved method full name starts with a module specifier this project
  *     imports (a typed receiver, `res.status()` on an express response);
  *   - `name`: the frontend links no identifier to the use (decorators, JSX tags, tagged templates,
  *     heritage clauses), so the name is matched against the file's import bindings.
  *
  * JavaScript/TypeScript only; other languages produce an empty slice.
  */
object CallSiteSlicing:

  /** Primitive-method names that type recovery puts on module-prefixed names when a local holds a
    * plain string or array (`node:path:path:includes` for `someString.includes`): measured false
    * positives on cdxgen and juice-shop (Part 1). A `via=type` record whose callee member is one of
    * these is dropped; binding-resolved records keep them (`path.join` through a binding is real,
    * `join` is deliberately not listed).
    */
  private val primitiveMembers = Set(
    "includes",
    "replace",
    "replaceAll",
    "split",
    "startsWith",
    "endsWith",
    "pop",
    "trim",
    "trimStart",
    "trimEnd",
    "charAt",
    "substring",
    "substr",
    "indexOf",
    "toLowerCase",
    "toUpperCase"
  )

  def calculateCallSitesSlice(atom: Cpg, config: CallsitesConfig): ProgramCallSites =
    if !JsNames.isJs(atom.metaData.language.headOption) then return ProgramCallSites(Nil)

    val js       = JsNames(atom)
    val resolver = new BindingResolver(atom, js)

    val recs = mutable.LinkedHashSet[CallSiteRecord]()

    // Pass A: every identifier that resolves to an imported binding (via=binding). The
    // frontend's `_tmp` lowering artifacts are definition machinery, never a use.
    val coveredCalls = mutable.HashSet[Long]()
    atom.identifier.filterNot(_.name.startsWith("_tmp")).foreach { id =>
        resolver.originOf(id).foreach { origin =>
          // the binding's own definition (`l = require(...)`) is not a use
          val isDef = id.argumentIndex == 1 && DeclarationRefs.parentOf(id).exists {
              case c: Call => c.name == Operators.assignment
              case _       => false
          }
          if !isDef then
            val top         = DeclarationRefs.chainTop(id)
            val involvement = DeclarationRefs.receiverOf(top)
            involvement.foreach(c => coveredCalls += c.id())
            val kind = involvement.map { c =>
              val isJsx = c.code.startsWith("<") && !c.name.startsWith("<operator")
              if c.name == "<operator>.new" then "new"
              else if isJsx then "jsx"
              else "call"
            }.getOrElse(DeclarationRefs.argumentOf(top).map(_ => "arg").getOrElse("ref"))
            val method = id.method
            recs += CallSiteRecord(
              method.filename,
              line(id),
              col(id),
              method.fullName,
              origin.module,
              (origin.path ++ resolver.membersOf(id)).mkString("."),
              kind,
              "binding",
              involvement.map(_.methodFullName).getOrElse("")
            )
          end if
        }
    }

    // Pass B: calls attributed only through their resolved name, a typed receiver (via=type)
    atom.call
        .filterNot(c => c.name.startsWith("<operator>") || coveredCalls.contains(c.id()))
        .foreach { c =>
            js.split(c.methodFullName).foreach { (module, symbol) =>
              // the symbol path is dotted (`path.includes`); the callee member is its last segment
              val member = symbol.map(_.split('.').last).getOrElse("")
              if !primitiveMembers.contains(member) then
                val file = c.method.filename
                // A resolved name may carry a module the file never imports while its type name is a
                // binding of this file (type recovery chose `codemirror:EditorView:*` in a file that
                // imports `@codemirror/view`). The file's own binding wins.
                val preferred = symbol.toList match
                  case first :: _ =>
                      js.ofBindingName(file, first.takeWhile(_ != '.')) match
                        case Some((m, _)) if m != module && !resolver.fileImports(file, module) => m
                        case _ => module
                  case _ => module
                // `consumer\n  .apply(x)`: report the member name's position, not the chain's start
                val memberPos =
                    c.receiver.collectFirst {
                        case f: Call if f.name == Operators.fieldAccess =>
                            f.argument.collectFirst { case fi: FieldIdentifier => fi }
                    }.flatten.filter(_.lineNumber.exists(_.intValue > 0))
                recs += CallSiteRecord(
                  file,
                  memberPos.map(fi => line(fi)).getOrElse(line(c)),
                  memberPos.map(fi => col(fi)).getOrElse(col(c)),
                  c.method.fullName,
                  preferred,
                  symbol.getOrElse(""),
                  "call",
                  "type",
                  c.methodFullName
                )
              end if
            }
        }

    // Pass C: decorators, heritage clauses, tagged templates and JSX tags, by name (via=name)
    recs ++= nameResolvedRecords(atom, js)

    // The frontend can duplicate an identifier node (the base of `jwt.sign(...)` appears both as
    // the member chain's base and as the call's receiver argument); one use site keeps one
    // record, the most specific kind wins.
    val kindRank =
        Map(
          "call"      -> 0,
          "new"       -> 1,
          "jsx"       -> 2,
          "tag"       -> 3,
          "decorator" -> 4,
          "arg"       -> 5,
          "extends"   -> 6,
          "ref"       -> 7
        )
    val deduped = recs
        .groupBy(r => (r.file, r.line, r.col, r.module))
        .values
        .map(rs => rs.minBy(r => (kindRank.getOrElse(r.kind, 8), r.path, r.via, r.mfn)))
    ProgramCallSites(
      deduped.toList.sortBy(r => (r.file, r.line.intValue, r.col.intValue))
    )
  end calculateCallSitesSlice

  private def line(n: AstNode): Integer = n match
    case e: Expression => e.lineNumber.getOrElse(-1)
    case a: Annotation => a.lineNumber.getOrElse(-1)
    case t: TypeDecl   => t.lineNumber.getOrElse(-1)
    case _             => -1

  private def col(n: AstNode): Integer = n match
    case e: Expression => e.columnNumber.getOrElse(-1)
    case a: Annotation => a.columnNumber.getOrElse(-1)
    case _             => -1

  private def enclosingMethod(n: AstNode): Option[Method] =
      Iterator
          .iterate(DeclarationRefs.parentOf(n))(_.flatMap(DeclarationRefs.parentOf))
          .takeWhile(_.isDefined).flatten.collectFirst { case m: Method => m }

  /** Decorators, heritage clauses, tagged templates and JSX tags. The frontend links no identifier
    * to any of them (chen T9 for JSX; decorators, `extends` bases and tagged-template tags have no
    * REF to their binding), so their names are matched against the file's import bindings. JSX
    * stays name-based until T9 lands.
    */
  private def nameResolvedRecords(atom: Cpg, js: JsNames): List[CallSiteRecord] =
    val root      = atom.metaData.root.headOption.getOrElse("")
    val fileLines = mutable.HashMap[String, IndexedSeq[String]]()
    def linesOf(f: String): IndexedSeq[String] =
        fileLines.getOrElseUpdate(
          f,
          Try {
              val file = if Paths.get(f).isAbsolute then better.files.File(f)
              else better.files.File(root) / f
              file.lines.toIndexedSeq
          }.getOrElse(IndexedSeq.empty)
        )
    // A decorated class's TYPE_DECL starts at its first decorator, so the heritage clause's line
    // is looked up in the source from there to the class body.
    def heritageLine(t: TypeDecl, file: String, baseName: String): Option[Integer] =
      val clause = java.util.regex.Pattern.compile(
        s"\\b(extends|implements)\\b[^{]*\\b${java.util.regex.Pattern.quote(baseName)}\\b"
      )
      t.lineNumber.map(_.intValue).flatMap { from =>
        val lines = linesOf(file)
        (from to math.min(from + 40, lines.size)).find(n => clause.matcher(lines(n - 1)).find())
      }.map(Integer.valueOf)

    val decorators = atom.annotation.l.flatMap { a =>
      val file = a.file.name.headOption.getOrElse("")
      js.ofBindingName(file, a.name).map { (module, symbol) =>
          CallSiteRecord(
            file,
            line(a),
            col(a),
            enclosingMethod(a).map(_.fullName).getOrElse(""),
            module,
            symbol.getOrElse(""),
            "decorator",
            "name",
            a.fullName
          )
      }
    }
    // A method's decorator hangs off both its METHOD and its MEMBER node.
    val uniqueDecorators =
        decorators.groupBy(r => (r.file, r.line, r.col, r.path)).values.map(_.head)

    val heritage = atom.typeDecl.filterNot(_.isExternal).l.flatMap { t =>
      val file = t.file.name.headOption.getOrElse("")
      t.inheritsFromTypeFullName.filterNot(n => n == "ANY" || n.isEmpty).flatMap { base =>
        // the frontend guesses a file for an imported base (`events.js::program:X`)
        val baseName = base.split("[:.]").lastOption.getOrElse(base)
        js.ofBindingName(file, baseName).map { (module, symbol) =>
            CallSiteRecord(
              file,
              heritageLine(t, file, baseName).getOrElse(line(t)),
              -1,
              enclosingMethod(t).map(_.fullName).getOrElse(""),
              module,
              symbol.getOrElse(""),
              "extends",
              "name",
              base
            )
        }
      }
    }

    val taggedAndJsx = atom.call.filterNot(_.name.startsWith("<operator>")).l.flatMap { c =>
      val file = c.method.filename
      if c.code.startsWith("<") then
        // JSX element: the tag name (`Button`, `Foo.Bar`) against the file's bindings
        val parts = c.name.split('.').toList
        js.ofBindingName(file, parts.head).map { (module, symbol) =>
            CallSiteRecord(
              file,
              line(c),
              col(c),
              c.method.fullName,
              module,
              (symbol.toList ++ parts.tail).mkString("."),
              "jsx",
              "name",
              c.methodFullName.take(80)
            )
        }
      else if !c.argument.exists(_.argumentIndex == 0) then
        // a call with no receiver/callee argument: the frontend's lowering of a tagged
        // template (`` gql`...` ``) names the call after the tag
        js.ofBindingName(file, c.name).map { (module, symbol) =>
            CallSiteRecord(
              file,
              line(c),
              col(c),
              c.method.fullName,
              module,
              symbol.getOrElse(""),
              "tag",
              "name",
              c.methodFullName
            )
        }
      else None
      end if
    }

    (uniqueDecorators.toList ++ heritage ++ taggedAndJsx)
  end nameResolvedRecords

  private[slicing] case class Origin(module: String, path: List[String])

  /** Where identifiers come from: an import binding's origin, (module, export path), resolved
    * through the assignments the frontend lowers imports to.
    *
    * A local gets an origin only from its single assignment: a local reassigned anywhere
    * (conditionally, or from another source) may hold something else at a use, so attributing it to
    * one assignment would misattribute (a `statusCode` local, destructured from an error and
    * conditionally set from `httpStatus.INTERNAL_SERVER_ERROR`, is not an http-status binding).
    *
    * The frontend's destructuring temporaries (`_tmp_0 = await import("m")`, `X = _tmp_0.X`) create
    * identifier uses with no REF edge to a local; those resolve to the unique local of that name in
    * the enclosing method.
    */
  private class BindingResolver(atom: Cpg, js: JsNames):
    private val declRefs = DeclarationRefs(atom)

    // method id -> locals by name, for the no-REF compiler temporaries
    private val methodLocals: Map[Long, Map[String, List[Local]]] =
        atom.method
            .flatMap(m => m.local.l.map(l => (m.id(), l.name, l)))
            .toList
            .groupMap(_._1)(t => (t._2, t._3))
            .view
            .mapValues(_.groupMap(_._1)(_._2).view.mapValues(_.sortBy(_.id())).toMap)
            .toMap

    private def localOf(id: Identifier): Option[Declaration] =
        id._refOut.collectFirst { case d: Declaration => declRefs.outermost(d) }.orElse {
            // the frontend's `_tmp` lowering: some identifiers of the lowered destructuring
            // carry no REF edge; a compiler temporary resolves to the unique same-named local
            // of its method. Only `_tmp*` names: any other no-REF identifier (an export-list
            // entry, say) must not be attributed to a same-named local.
            if id.name.startsWith("_tmp") then
              methodLocals.get(id.method.id()).flatMap(_.get(id.name)).flatMap {
                  case List(one) => Option(declRefs.outermost(one))
                  case _         => None
              }
            else None
        }

    private def baseOf(c: Call): Option[Expression] =
        c.argument.collectFirst { case b: Expression if b.argumentIndex == 1 => b }

    private val origins = mutable.HashMap[Long, Origin]()

    private def originOfExpr(e: Expression, depth: Int): Option[Origin] =
        if depth > 8 then None
        else
          e match
            case c: Call if c.name == "require" || c.name == "import" =>
                js.ofImportExpression(c).map((m, s) => Origin(m, s.toList))
            case c: Call if c.name == Operators.fieldAccess =>
                val field = c.argument.collectFirst { case f: FieldIdentifier => f.canonicalName }
                baseOf(c).flatMap(originOfExpr(_, depth + 1))
                    .map(o => o.copy(path = o.path ++ field))
            case c: Call if c.name == "<operator>.await" =>
                c.argument.collectFirst { case b: Expression => b }
                    .flatMap(originOfExpr(_, depth + 1))
            case id: Identifier => localOf(id).flatMap(d => origins.get(d.id()))
            case _              => None

    // assignments per local id, each with its right-hand side
    private val assignmentsOf: Map[Long, List[Expression]] =
      val byLocal = mutable.HashMap[Long, List[Expression]]()
      atom.call.nameExact(Operators.assignment).foreach { a =>
          a.argument.l.sortBy(_.argumentIndex) match
            case (lhs: Identifier) :: (rhs: Expression) :: _ =>
                localOf(lhs).foreach(d => byLocal(d.id()) = rhs :: byLocal.getOrElse(d.id(), Nil))
            case _ =>
      }
      byLocal.toMap

    // to a fixpoint: one round of `_tmp`/alias assignments resolved per pass
    locally {
        var changed = true
        var round   = 0
        while changed && round < 10 do
          changed = false
          round += 1
          assignmentsOf.foreach { (localId, rhsList) =>
              if !origins.contains(localId) then
                // only a single assignment may define the local's origin (class doc)
                rhsList match
                  case List(rhs) =>
                      originOfExpr(rhs, 0).foreach { o =>
                        origins(localId) = o
                        changed = true
                      }
                  case _ =>
          }
    }

    // file -> module specifiers its import bindings bring in
    private val fileModules: Map[String, Set[String]] =
        atom.method.nameExact(":program").l
            .flatMap(p =>
                p.local.l.flatMap(l => origins.get(l.id())).map(o => (p.filename, o.module))
            )
            .groupMap(_._1)(_._2)
            .view
            .mapValues(_.toSet)
            .toMap

    def fileImports(file: String, module: String): Boolean =
        fileModules.getOrElse(file, Set.empty).contains(module)

    /** The (module, export path) an identifier's declaration is an import binding of, if any. */
    def originOf(id: Identifier): Option[Origin] =
        localOf(id).flatMap(d => origins.get(d.id())).filter(o => JsNames.isBareSpecifier(o.module))

    /** The member names between an identifier and the top of its member chain (`id.a.b`). */
    def membersOf(id: Identifier): List[String] =
      val members      = mutable.ListBuffer[String]()
      var top: AstNode = id
      var climbing     = true
      while climbing do
        DeclarationRefs.parentOf(top) match
          case Some(c: Call)
              if c.name == Operators.fieldAccess &&
                  c.argument.exists(a => a.id() == top.id() && a.argumentIndex == 1) =>
              c.argument.collect { case f: FieldIdentifier => f.canonicalName }
                  .foreach(name => members += name)
              top = c
          case _ => climbing = false
      members.toList
  end BindingResolver
end CallSiteSlicing
