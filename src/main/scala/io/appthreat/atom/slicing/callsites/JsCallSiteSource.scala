package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{AngularTemplate, DeclarationRefs, JsNames, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.appthreat.x2cpg.passes.taggers.js.{JsModules, JsPathAliases, JsShapes}
import io.shiftleft.codepropertygraph.generated.{DispatchTypes, ModifierTypes, Operators}
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import java.nio.file.Paths
import scala.collection.mutable
import scala.util.Try

/** The JavaScript/TypeScript source of the callsites slice: the binding, field-alias, Angular
  * template, typed and name passes, and the resolver that links identifiers to the imports the
  * frontend lowered to assignments.
  */
final class JsCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "js"):

  private val js = JsNames(atom)
  // the templates read the regexes declared further down, so these wait for them
  private lazy val templates = componentTemplates(atom)
  // template statements write component members (`(click)="open = true"`)
  private lazy val templateWrites =
      templates.flatMap(t => AngularTemplate.writes(t.text).map(t.component.fullName -> _)).toSet
  private lazy val resolver = BindingResolver(atom, js, templateWrites)

  def calculate(): ProgramCallSites =
    // A use of a binding at `use` (an identifier, or the `this.f` of a field alias): its kind is how
    // the top of its member chain takes part in a call.
    val coveredCalls = mutable.HashSet.empty[Long]
    def useRecord(use: Expression, origin: Origin): Unit =
      methodOf(use) match
        case None => collector.skip(use)
        case Some(method) =>
          collector.add(use) {
            val top         = DeclarationRefs.chainTop(use)
            val involvement = DeclarationRefs.receiverOf(top)
            involvement.foreach(c => coveredCalls += c.id())
            val kind = involvement.map { c =>
              val isJsx = c.code.startsWith("<") && !c.name.startsWith("<operator")
              if c.name == "<operator>.new" then "new"
              else if isJsx then "jsx"
              else "call"
            }.getOrElse(DeclarationRefs.argumentOf(top).map(_ => "arg").getOrElse("ref"))
            CallSiteRecord(
              method.filename,
              line(use),
              col(use),
              method.fullName,
              origin.module,
              (origin.path ++ resolver.membersOf(use)).mkString("."),
              kind,
              "binding",
              involvement.map(c => resolvedName(c.methodFullName)).getOrElse("")
            )
          }
    end useRecord

    // Pass A: every identifier that resolves to an imported binding (via=binding). The
    // frontend's `_tmp` lowering artifacts are definition machinery, never a use.
    atom.identifier.filterNot(_.name.startsWith("_tmp")).foreach { id =>
        resolver.originOf(id).foreach { origin =>
          // the binding's own definition (`l = require(...)`) is not a use
          val isDef = id.argumentIndex == 1 && DeclarationRefs.parentOf(id).exists {
              case c: Call => c.name == Operators.assignment
              case _       => false
          }
          if !isDef then useRecord(id, origin)
        }
    }

    // Pass A2: reads of a class field that holds an import (`readonly Subject = Subject;` then
    // `new this.Subject()`), through `this` - inherited fields included - or, for a static field,
    // the class's name (via=binding: the field's single write is scope-resolved like a local's).
    atom.call.nameExact(Operators.fieldAccess).filterNot(JsShapes.isAssignmentTarget).foreach {
        fa => resolver.fieldOriginOf(fa).foreach(origin => useRecord(fa, origin))
    }

    // Angular templates read the component's members by bare name: a read of a field alias in
    // the component's template (`{{ translations.title }}`, `[icon]="faCoffee"`) is a use too.
    collector.addAll(templateRecords(templates, resolver))

    // Pass B: calls attributed only through their resolved name, a typed receiver (via=type)
    atom.call
        .filterNot(c => c.name.startsWith("<operator>") || coveredCalls.contains(c.id()))
        .foreach { c =>
            methodOf(c) match
              case None => collector.skip(c)
              case Some(method) =>
                js.split(c.methodFullName).foreach { (module, symbol) =>
                  // the symbol path is dotted (`path.includes`); the callee member is its last segment
                  val member = symbol.map(_.split('.').last).getOrElse("")
                  if !primitiveMembers.contains(member) then
                    // `consumer\n  .apply(x)`: report the member name's position, not the chain's start
                    val memberPos =
                        c.receiver.collectFirst {
                            case f: Call if f.name == Operators.fieldAccess =>
                                f.argument.collectFirst { case fi: FieldIdentifier => fi }
                        }.flatten.filter(_.lineNumber.exists(_.intValue > 0))
                    collector.add(c) {
                      CallSiteRecord(
                        method.filename,
                        memberPos.map(fi => line(fi)).getOrElse(line(c)),
                        memberPos.map(fi => col(fi)).getOrElse(col(c)),
                        method.fullName,
                        module,
                        symbol.getOrElse(""),
                        "call",
                        "type",
                        c.methodFullName
                      )
                    }
                }
        }

    // Pass C: decorators, heritage clauses, tagged templates and JSX tags, by name (via=name)
    collector.addAll(nameResolvedRecords(atom, js))

    collector.result()
  end calculate

  private val primitiveMembers: Set[String] = Set(
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

  private val TemplateUrl    = """templateUrl\s*:\s*['"`]([^'"`]+)['"`]""".r
  private val InlineTemplate = """(?s)\btemplate\s*:\s*(`|'|")""".r

  /** An Angular component's template: its text, where it is, and how an offset into the text maps
    * to a line and column there.
    */
  private final case class ComponentTemplate(
    component: TypeDecl,
    text: String,
    file: String,
    method: String,
    at: Int => (Integer, Integer)
  )

  /** The template of each component: its `templateUrl` file (resolved against the component's file,
    * as Angular does, `./` or not) or its inline `template` string. Both are read as text: the
    * frontend lowers only some of an external template's bindings, and none of an inline one.
    */
  private def componentTemplates(atom: Cpg): List[ComponentTemplate] =
    val root = atom.metaData.root.headOption.getOrElse("")
    def sourceOf(file: String): Option[String] =
        Try {
            val f =
                if Paths.get(file).isAbsolute then better.files.File(file)
                else better.files.File(root) / file
            f.contentAsString
        }.toOption
    atom.annotation.nameExact("Component").l.flatMap { annotation =>
        annotation.astParent match
          case td: TypeDecl =>
              val external = TemplateUrl.findFirstMatchIn(annotation.code).toList.flatMap { m =>
                val url = m.group(1)
                val html = JsModules.resolveRelative(
                  td.filename,
                  if url.startsWith(".") || url.startsWith("/") then url else s"./$url"
                )
                sourceOf(html).toList.map { text =>
                  val program = atom.method.filenameExact(html).nameExact(":program").fullName
                      .headOption.getOrElse("")
                  ComponentTemplate(td, text, html, program, offset => textPosition(text, offset))
                }
              }
              val inline = InlineTemplate.findFirstMatchIn(annotation.code).toList.map { m =>
                val (text, rawOffsets) = stringLiteral(annotation.code, m.end, m.group(1).head)
                ComponentTemplate(
                  td,
                  text,
                  td.filename,
                  enclosingMethodOf(annotation).map(_.fullName).getOrElse(""),
                  offset => position(annotation, rawOffsets(offset))
                )
              }
              external ++ inline
          case _ => Nil
    }
  end componentTemplates

  /** Reads of field aliases in the components' templates, with Angular's rule: a bare name in a
    * template expression is a member of the component.
    */
  private def templateRecords(
    templates: List[ComponentTemplate],
    resolver: BindingResolver
  ): List[CallSiteRecord] =
      templates.flatMap { t =>
          AngularTemplate.reads(t.text).flatMap { (offset, name, members, called) =>
              resolver.fieldOriginIn(t.component, name).map { origin =>
                val (l, c) = t.at(offset)
                CallSiteRecord(
                  t.file,
                  l,
                  c,
                  t.method,
                  origin.module,
                  (origin.path ++ members).mkString("."),
                  if called then "call" else "ref",
                  "binding",
                  ""
                )
              }
          }
      }

  /** The value of the string literal whose opening `quote` is just before `start` in `code`, with
    * the offset in `code` of each of its characters (escapes like `\'` are one character).
    */
  private def stringLiteral(code: String, start: Int, quote: Char): (String, Array[Int]) =
    val text    = new StringBuilder
    val offsets = mutable.ArrayBuffer[Int]()
    var i       = start
    while i < code.length && code(i) != quote do
      if code(i) == '\\' && i + 1 < code.length then
        val escaped = code(i + 1) match
          case 'n'   => '\n'
          case 't'   => '\t'
          case 'r'   => '\r'
          case other => other
        text += escaped
        offsets += i
        i += 2
      else
        text += code(i)
        offsets += i
        i += 1
    offsets += i
    (text.toString, offsets.toArray)
  end stringLiteral

  /** The line (1-based) and column (0-based) of an offset into a file's text. */
  private def textPosition(text: String, offset: Int): (Integer, Integer) =
    val before = text.substring(0, offset)
    (
      Integer.valueOf(before.count(_ == '\n') + 1),
      Integer.valueOf(offset - before.lastIndexOf('\n') - 1)
    )

  /** The line and column of an offset into an annotation's code. */
  private def position(annotation: Annotation, offset: Int): (Integer, Integer) =
    val before    = annotation.code.substring(0, offset)
    val newline   = before.lastIndexOf('\n')
    val startLine = annotation.lineNumber.map(_.intValue).getOrElse(-1)
    val startCol  = annotation.columnNumber.map(_.intValue).getOrElse(-1)
    if startLine < 0 then (Integer.valueOf(-1), Integer.valueOf(-1))
    else if newline < 0 then (Integer.valueOf(startLine), Integer.valueOf(startCol + offset))
    else
      (
        Integer.valueOf(startLine + before.count(_ == '\n')),
        Integer.valueOf(offset - newline - 1)
      )

  /** Each base's heritage line, from the `callsites-heritage` tags the frontend writes on the
    * class's TYPE_REF. No node marks a heritage base and the TYPE_DECL's line is its first
    * decorator's, so the position travels with the clause.
    */
  private def heritageLineOf(t: TypeDecl): Map[String, Integer] =
      atom.typeRef.filter(_.typeFullName == t.fullName)
          .flatMap(_.tag.nameExact("callsites-heritage"))
          .flatMap(tag => io.circe.parser.parse(tag.value).toOption)
          .flatMap { json =>
              for
                obj  <- json.asObject
                base <- obj("base").flatMap(_.asString)
                ln   <- obj("line").flatMap(_.asNumber).flatMap(_.toInt)
              yield simpleName(base) -> Integer.valueOf(ln)
          }
          .toMap

  private def simpleName(base: String): String =
      base.split("[:.]").lastOption.getOrElse(base)

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
    // is looked up in the source from there to the class body. Atoms built by a frontend that
    // already tags the heritage (see heritageLineOf) never reach this scan.
    def legacyHeritageLine(t: TypeDecl, file: String, baseName: String): Option[Integer] =
      val clause = java.util.regex.Pattern.compile(
        s"\\b(extends|implements)\\b[^{]*\\b${java.util.regex.Pattern.quote(baseName)}\\b"
      )
      t.lineNumber.map(n => math.max(n.intValue, 1)).flatMap { from =>
        val lines = linesOf(file)
        (from to math.min(from + 40, lines.size)).find(n => clause.matcher(lines(n - 1)).find())
      }.map(Integer.valueOf)

    val decorators = atom.annotation.l.flatMap { a =>
      fileOf(a).getOrElse("") match
        case file if file.nonEmpty =>
          js.ofBindingName(file, a.name).map { (module, symbol) =>
              CallSiteRecord(
                file,
                line(a),
                col(a),
                enclosingMethodOf(a).map(_.fullName).getOrElse(""),
                module,
                symbol.getOrElse(""),
                "decorator",
                "name",
                a.fullName
              )
          }
        case _ => None
    }
    // A method's decorator hangs off both its METHOD and its MEMBER node.
    val uniqueDecorators =
        decorators.groupBy(r => (r.file, r.line, r.col, r.path)).values.map(_.head)

    val heritage = atom.typeDecl.filterNot(_.isExternal).l.flatMap { t =>
      val taggedLines = heritageLineOf(t)
      fileOf(t).getOrElse("") match
        case file if file.nonEmpty =>
          t.inheritsFromTypeFullName.filterNot(n => n == "ANY" || n.isEmpty).flatMap { base =>
            val baseName = simpleName(base)
            js.ofBindingName(file, baseName).map { (module, symbol) =>
                val heritageLine = taggedLines.getOrElse(baseName,
                    legacyHeritageLine(t, file, baseName).getOrElse(line(t)))
                CallSiteRecord(
                  file,
                  heritageLine,
                  -1,
                  enclosingMethodOf(t).map(_.fullName).getOrElse(""),
                  module,
                  symbol.getOrElse(""),
                  "extends",
                  "name",
                  base
                )
            }
          }
        case _ => None
    }

    val taggedAndJsx = atom.call.filterNot(_.name.startsWith("<operator>")).l.flatMap { c =>
      methodOf(c) match
        case Some(method) =>
          val file = method.filename
          if c.code.startsWith("<") then
            // JSX element: the tag name (`Button`, `Foo.Bar`) against the file's bindings
            val parts = c.name.split('.').toList
            js.ofBindingName(file, parts.head).map { (module, symbol) =>
                CallSiteRecord(
                  file,
                  line(c),
                  col(c),
                  method.fullName,
                  module,
                  (symbol.toList ++ parts.tail).mkString("."),
                  "jsx",
                  "name",
                  resolvedName(c.methodFullName)
                )
            }
          else if !c.argument.exists(_.argumentIndex == 0) && c.methodFullName == c.name then
            // a call with no receiver/callee argument whose full name is the bare name: the
            // frontend's lowering of a tagged template (`` gql`...` ``) names the call after the
            // tag and resolves nothing. A resolved name (`JSON.parse`) is a builtin call whose
            // member happens to match a file binding, not a tag.
            js.ofBindingName(file, c.name).map { (module, symbol) =>
                CallSiteRecord(
                  file,
                  line(c),
                  col(c),
                  method.fullName,
                  module,
                  symbol.getOrElse(""),
                  "tag",
                  "name",
                  c.methodFullName
                )
            }
          else None
          end if
        case None => None
    }

    (uniqueDecorators.toList ++ heritage ++ taggedAndJsx)
  end nameResolvedRecords

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
  private class BindingResolver(atom: Cpg, js: JsNames, templateWrites: Set[(String, String)]):
    private val aliases = JsPathAliases(atom)

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
              methodOf(id).flatMap(m => methodLocals.get(m.id())).flatMap(_.get(id.name)).flatMap {
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
            case c: Call
                if c.name == Operators.fieldAccess && baseOf(c).exists(_.code == "this") =>
                fieldNameOf(c).flatMap(name =>
                    classOf(c.method).flatMap(td =>
                        lineageOf(td).iterator
                            .map(t => (t.fullName, name, isStaticContext(c.method)))
                            .find(fieldWrites.contains).filter(isAliasCandidate)
                            .flatMap(fieldOrigins.get)
                    )
                )
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

    private def fieldNameOf(fa: Call): Option[String] =
        fa.argument.collectFirst { case f: FieldIdentifier => f.canonicalName }

    private val classOfMethod = mutable.HashMap[Long, Option[TypeDecl]]()
    private def classOf(m: Method): Option[TypeDecl] =
        classOfMethod.getOrElseUpdate(m.id(), JsShapes.enclosingTypeDecl(m))

    // A static initialiser or static method, or a function nested in one (whose full name extends
    // the enclosing method's): its `this` is the class, not an instance.
    private val staticOfMethod = mutable.HashMap[Long, Boolean]()
    private def isStaticContext(m: Method): Boolean =
        staticOfMethod.getOrElseUpdate(
          m.id(), {
              val parts = m.fullName.split(':')
              (1 to parts.length).iterator.map(n => parts.take(n).mkString(":"))
                  .flatMap(atom.method.fullNameExact(_))
                  .exists(x =>
                      x.name == io.appthreat.x2cpg.Defines.StaticInitMethodName ||
                          x.modifier.modifierTypeExact(ModifierTypes.STATIC).nonEmpty
                  )
          }
        )

    /** A field of a class: the class's full name, the field's name, and whether it is static. */
    private type FieldKey = (String, String, Boolean)

    private val memberNamesOfWholeWrites = Set(
      "assign",
      "defineProperty",
      "defineProperties",
      "set"
    )

    // Writes of class fields: the value of each plain `this.f = v` (or `this["f"] = v`), None for
    // any other write (`this.f += v`, `this.f ??= v`, `this.f++`, `delete this.f`). A field written
    // off any other base (`comp.f = v`, `self.f = v`, `Klass.f = v`) has writes the slice cannot
    // place: no field of that name is an alias. Neither is any field of a class that writes `this`
    // by a key computed at run time (`this[k] = v`) or wholesale (`Object.assign(this, x)`).
    private val (fieldWrites, foreignWrites, opaqueClasses) =
      val writes  = mutable.HashMap[FieldKey, List[Option[Expression]]]()
      val foreign = mutable.HashSet[String]()
      val opaque  = mutable.HashSet[String]()
      def literalName(e: Expression): Option[String] = e match
        case l: Literal =>
            Some(l.code.stripPrefix("\"").stripSuffix("\"").stripPrefix("'").stripSuffix("'")
                .stripPrefix("`").stripSuffix("`"))
        case _ => None
      atom.call.filter(c =>
          JsShapes.isAssignment(c.name) || c.name == "<operator>.delete" ||
              c.name.matches("<operator>\\.(pre|post)(Increment|Decrement)")
      ).foreach { w =>
          w.argument.l.sortBy(_.argumentIndex).headOption match
            case Some(target: Call)
                if target.name == Operators.fieldAccess || target.name == Operators.indexAccess =>
                val name =
                    if target.name == Operators.fieldAccess then fieldNameOf(target)
                    else target.argument.l.find(_.argumentIndex == 2).flatMap(literalName)
                baseOf(target) match
                  case Some(b: Identifier) if b.name == "this" =>
                      classOf(w.method).foreach { td =>
                          name match
                            case Some(n) =>
                                val key = (td.fullName, n, isStaticContext(w.method))
                                val value =
                                    if w.name == Operators.assignment then
                                      w.argument.l.find(_.argumentIndex == 2)
                                    else None
                                writes(key) = value :: writes.getOrElse(key, Nil)
                            // `this[k] = v` at run time; a computed field declaration (`[k] = v`,
                            // lowered with the declaration's code) has a key evaluated once, a
                            // symbol or a constant, and writes no field by name
                            case None =>
                                if w.code.trim.startsWith("this") then opaque += td.fullName
                      }
                  case _ => name.foreach(foreign += _)
            case _ =>
      }
      // `Object.assign(this, x)` and friends write the receiver's fields wholesale. The graph
      // read replaces the source-text scan over every call's code: the callee is a member of
      // Object or Reflect, and the first argument is the receiver's `this`.
      atom.call.filter(writesThisWholesale).foreach { c =>
          classOf(c.method).foreach(td => opaque += td.fullName)
      }
      (writes.toMap, foreign.toSet, opaque.toSet)
    end val

    private def writesThisWholesale(c: Call): Boolean =
      def calleeShape(fa: Call): Boolean =
          fa.name == Operators.fieldAccess && {
              val base = fa.argument.collectFirst {
                  case b: Identifier if b.argumentIndex == 1 => b.name
              }
              val member = fa.argument.collectFirst {
                  case f: FieldIdentifier => f.canonicalName
              }
              base.exists(b => b == "Object" || b == "Reflect") &&
              member.exists(memberNamesOfWholeWrites.contains)
          }
      // static dispatch names the callee after the member access itself (`Object.assign`)
      def staticShape: Boolean =
          c.dispatchType == DispatchTypes.STATIC_DISPATCH && {
              val parts = c.methodFullName.split('.')
              parts.length == 2 && Set("Object", "Reflect").contains(parts.head) &&
              memberNamesOfWholeWrites.contains(parts.last)
          }
      // dynamic dispatch carries the member access as the receiver, or as the callee argument
      val dynamicShape =
          c.receiver.collectFirst { case fa: Call => fa }.exists(calleeShape) ||
              c.argument.find(_.argumentIndex == 0).exists {
                  case fa: Call => calleeShape(fa)
                  case _        => false
              }
      (staticShape || dynamicShape) && c.argument
          .find(_.argumentIndex >= 1)
          .exists(a => a.code.matches("(?s)this\\b.*"))
    end writesThisWholesale

    private val fieldOrigins = mutable.HashMap[FieldKey, Origin]()

    private val projectTypes: Map[String, List[TypeDecl]] =
        atom.typeDecl.isExternal(false).l.groupBy(_.name)

    // The project class a heritage clause names: by full name, else by a name that is unique in the
    // project - unless the file imports that name from a package (`extends Component` from react is
    // not a project class called Component).
    private def baseClass(td: TypeDecl, base: String): Option[TypeDecl] =
        atom.typeDecl.fullNameExact(base).isExternal(false).headOption.orElse {
            val simple = simpleName(base)
            val fromPackage = js.ofBindingName(td.filename, simple).exists((module, _) =>
                JsNames.isBareSpecifier(module) && aliases.resolve(td.filename, module).isEmpty
            )
            if fromPackage then None
            else
              projectTypes.get(simple) match
                case Some(List(one)) => Some(one)
                case _               => None
        }

    // A class and its ancestors in the project, nearest first.
    private val lineage = mutable.HashMap[String, List[TypeDecl]]()
    private def lineageOf(td: TypeDecl): List[TypeDecl] =
        lineage.getOrElseUpdate(
          td.fullName, {
              val seen = mutable.LinkedHashMap[String, TypeDecl](td.fullName -> td)
              var next = List(td)
              while next.nonEmpty do
                next = next.flatMap(t => t.inheritsFromTypeFullName.flatMap(baseClass(t, _)))
                    .filterNot(t => seen.contains(t.fullName))
                next.foreach(t => seen(t.fullName) = t)
              seen.values.toList
          }
        )

    // classes by the full names of all their ancestors: a subclass writes its ancestors' fields
    private lazy val descendants: Map[String, List[String]] =
        atom.typeDecl.isExternal(false).l
            .flatMap(td => lineageOf(td).drop(1).map(a => a.fullName -> td.fullName))
            .groupMap(_._1)(_._2)

    /** The writes of a field of `cls`: its own and its subclasses'. */
    private def allWrites(key: FieldKey): List[Option[Expression]] =
      val (cls, name, isStatic) = key
      (cls :: descendants.getOrElse(cls, Nil)).flatMap(c =>
          fieldWrites.getOrElse((c, name, isStatic), Nil)
      )

    private def isAliasCandidate(key: FieldKey): Boolean =
      val (cls, name, isStatic) = key
      val family                = cls :: descendants.getOrElse(cls, Nil)
      !foreignWrites.contains(name) && !family.exists(opaqueClasses.contains) &&
      !(!isStatic && family.exists(c => templateWrites.contains(c -> name)))

    /** The origin of a field of a class (or of the nearest ancestor that writes it). */
    private def fieldOriginIn(td: TypeDecl, name: String, isStatic: Boolean): Option[Origin] =
        if name.isEmpty then None
        else
          lineageOf(td).iterator.map(t => (t.fullName, name, isStatic))
              .find(fieldWrites.contains)
              .filter(isAliasCandidate)
              .flatMap(fieldOrigins.get)
              .filter(o => JsNames.isBareSpecifier(o.module))

    /** The origin of an instance field of a component (a template reads instance members). */
    def fieldOriginIn(td: TypeDecl, name: String): Option[Origin] =
        fieldOriginIn(td, name, isStatic = false)

    /** The origin of a field read: `this.f` in a method of the class (an instance field, or a
      * static one in a static method), or `Klass.f` of a static field.
      */
    def fieldOriginOf(fa: Call): Option[Origin] =
        fieldNameOf(fa).flatMap { name =>
            baseOf(fa) match
              case Some(b: Identifier) if b.name == "this" =>
                  classOf(fa.method).flatMap(fieldOriginIn(_, name, isStaticContext(fa.method)))
              case Some(b: Identifier) =>
                  // `Klass.f`: the base names a class of this file
                  projectTypes.getOrElse(b.name, Nil)
                      .find(_.filename == fa.method.filename)
                      .flatMap(fieldOriginIn(_, name, isStatic = true))
              case _ => None
        }

    // to a fixpoint: one round of `_tmp`/alias assignments (and field aliases) resolved per pass
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
          // a field gets an origin from its single write (its subclasses' writes included), by
          // the same rule
          fieldWrites.keys.foreach { key =>
              if !fieldOrigins.contains(key) && isAliasCandidate(key) then
                allWrites(key) match
                  case List(Some(rhs)) =>
                      originOfExpr(rhs, 0).foreach { o =>
                        fieldOrigins(key) = o
                        changed = true
                      }
                  case _ =>
          }
        end while
    }

    /** The (module, export path) an identifier's declaration is an import binding of, if any. */
    def originOf(id: Identifier): Option[Origin] =
        localOf(id).flatMap(d => origins.get(d.id())).filter(o => JsNames.isBareSpecifier(o.module))

    /** The member names between a use and the top of its member chain (`id.a.b`). */
    def membersOf(id: Expression): List[String] =
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
end JsCallSiteSource
