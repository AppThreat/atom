package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import scala.collection.mutable
import scala.util.Try

/** The Python source of the callsites slice: a port of the measured prototype (callsites-eval
  * `harness/proto/py_callsites.scala`), extended with the kinds the prototype lacked.
  *
  * The frontend lowers every import to an assignment `x = import(from, name[, alias])`, links
  * captured module-level names through closure bindings, and lowers a dotted callee to `tmpN = a.b;
  * tmpN.c()`, so the binding -> origin -> use design of the JS source carries over. Origins come
  * from those import assignments under the single-assignment rule; a name that is assigned anywhere
  * else may hold something else at a use, so it stays unattributed. A guarded fallback import
  * (`try: import x except ImportError: x = None`) is the one exception: at run time the name is
  * still the module, so one import assignment and one `None` fallback count as a single origin.
  *
  * What the prototype measured without, and what this adds:
  *   - heritage (`class X(click.Command)`), from `inheritsFromTypeFullName` checked against the
  *     file's import bindings;
  *   - `except X` handler classes, which the frontend lowers to references since chen#256, and
  *     which therefore need no pass of their own;
  *   - typed attribute reads (`request.method`), via=type with kind `ref`;
  *   - declaration types (parameters, annotated locals and return types), kind `decltype`;
  *   - decorators, whose base is either an import binding or a typed value;
  *   - the standard library, recorded with `builtin: true`.
  *
  * Whether an import names project code or a dependency is the import resolver's verdict
  * (`RESOLVED_*` tags name entities the graph contains, `UNKNOWN_*` name external ones), never a
  * guess from path components; a relative import is project code by construction.
  */
final class PythonCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "py"):

  private val stdlibModules: Set[String] =
      Try {
          val stream = getClass.getResourceAsStream("/callsites/python-stdlib.json")
          val text = io.circe.parser
              .parse(scala.io.Source.fromInputStream(stream, "UTF-8").mkString).toOption
          stream.close()
          text.flatMap(_.asArray).toSeq.flatten.flatMap(_.asString).toSet
      }.getOrElse(Set.empty)

  /** Whether a module (`os`, `os.path`, `email.mime.text`) belongs to the standard library: the
    * list names top-level packages only.
    */
  private def isStdlib(module: String): Boolean =
      stdlibModules.contains(module.takeWhile(_ != '.'))

  /** The purl of the distribution that provides `module`: the longest mapped prefix, so that
    * `requests.adapters` takes the purl of `requests`.
    */
  private def purlOf(module: String): String =
      Iterator.iterate(module)(m => m.substring(0, math.max(m.lastIndexOf('.'), 0)))
          .takeWhile(_.nonEmpty)
          .flatMap(purlsByModule.get)
          .nextOption()
          .getOrElse("")

  /** Top-level import name -> purl, from the BOMs the atom carries (a component's ImportedModules
    * property) and from the python-deps index of the input's dependency origins. A module claimed
    * with different purls (yaml from PyYAML and ruamel-yaml) stays unmapped.
    */
  private lazy val purlsByModule: Map[String, String] =
    val bomClaims = atom.configFile
        .filter(f => io.appthreat.x2cpg.passes.taggers.BomFiles.isBom(f.name))
        .toList
        .flatMap(f => io.circe.parser.parse(f.content).toOption.toList)
        .flatMap { json =>
            json.asObject.flatMap(_("components")).flatMap(_.asArray).toSeq.flatten.flatMap {
                comp =>
                    for
                      obj   <- comp.asObject
                      purl  <- obj("purl").flatMap(_.asString)
                      props <- obj("properties").flatMap(_.asArray)
                      modules <- props.collectFirst {
                          case p
                              if p.asObject.exists(
                                _("name").exists(_.asString.contains("ImportedModules"))
                              ) =>
                              p.asObject.flatMap(_("value")).flatMap(_.asString).getOrElse("")
                      }
                    yield modules.split(',').map(_.trim).filter(_.nonEmpty).map(_ -> purl).toList
            }
        }
    val depsClaims = Try {
        val root = java.nio.file.Paths.get(atom.metaData.root.headOption.getOrElse("."))
        io.appthreat.pysrc2cpg.PythonDependencyStubs.modulePurlIndex(
          root,
          io.appthreat.pysrc2cpg.Py2CpgOnFileSystemConfig()
        )
    }.getOrElse(Map.empty)
    val claims = bomClaims.flatten.toList ++ depsClaims.toList
    claims
        .groupBy(_._1)
        .view
        .mapValues(_.map(_._2).toSet)
        .collect { case (m, ps) if ps.size == 1 => m -> ps.head }
        .toMap
  end purlsByModule

  def calculate(): ProgramCallSites =
    // import origins per declaration, and assignment counts (the single-assignment rule)
    val bindings    = mutable.HashMap.empty[Long, Binding]
    val importBound = mutable.HashSet.empty[Long]
    val assignCount = mutable.HashMap.empty[Long, Int].withDefaultValue(0)
    val fileModules = mutable.HashMap.empty[String, mutable.Set[String]]

    def declOf(id: Identifier): Option[Declaration] =
        id._refOut.collectFirst { case d: Declaration => declRefs.outermost(d) }

    def declOfNode(n: AstNode): Option[Declaration] = n match
      case i: Identifier => declOf(i)
      case l: Local      => Some(declRefs.outermost(l))
      case _             => None

    atom.call.nameExact(Operators.assignment).foreach { a =>
        a.argument.l.sortBy(_.argumentIndex) match
          case (lhs: Identifier) :: (rhs: Expression) :: _ =>
              declOf(lhs).foreach { d =>
                assignCount(d.id()) += 1
                importBound += d.id()
                rhs match
                  case importCall: Call if importCall.name == "import" =>
                      val args = importArgs(importCall)
                      val from = args.headOption.getOrElse("")
                      val name = args.lift(1).getOrElse("")
                      // `import a.b` binds the package `a` (chen#244); a from-import names
                      // the module it imports from
                      val module = if from.isEmpty then name.split('.').head else from
                      val path   = if from.isEmpty then Nil else List(name)
                      // a relative import is project code; the resolver's RESOLVED_* tags
                      // name entities the graph contains (project code), UNKNOWN_* externals
                      val isProject = from.startsWith(".") ||
                          importCall.tag.name.exists(_.startsWith("RESOLVED_"))
                      methodOf(a).map(_.filename).filter(_.nonEmpty).foreach { file =>
                          if !isProject then
                            fileModules.getOrElseUpdate(file, mutable.Set.empty) += module
                            bindings(d.id()) = Binding(file, module, path)
                      }
                  case _ =>
              }
          case _ =>
    }

    // the right-hand sides assigned to each declaration, for the guarded-fallback rule
    val assignmentsOfDecl: Map[Long, List[AssignmentShape]] =
      val out = mutable.HashMap.empty[Long, List[AssignmentShape]]
      atom.call.nameExact(Operators.assignment).foreach { a =>
          a.argument.l.sortBy(_.argumentIndex) match
            case (lhs: Identifier) :: (rhs: Expression) :: _ =>
                declOf(lhs).foreach { d =>
                  val shape = rhs match
                    case c: Call if c.name == "import" =>
                        AssignmentShape.Import(
                          importArgs(c).lift(1).getOrElse("").split('.').head
                        )
                    case l: Literal if l.code == "None" => AssignmentShape.NoneLiteral
                    case _                              => AssignmentShape.Other
                  out(d.id()) = shape :: out.getOrElse(d.id(), Nil)
                }
            case _ =>
      }
      out.toMap

    def singleOrigin(id: Long): Boolean =
        assignCount(id) == 1 ||
            assignCount(id) > 1 && {
                val shapes = assignmentsOfDecl.getOrElse(id, Nil)
                // a guarded fallback (`try: import x except ImportError: x = None`) is one origin
                (assignCount(id) == 2 && shapes.count {
                    case AssignmentShape.Import(_) => true
                    case _                         => false
                } == 1 && shapes.count(_ == AssignmentShape.NoneLiteral) == 1) ||
                // `import os` and `import os.path` both bind the name `os` to the same package
                shapes.forall {
                    case AssignmentShape.Import(_) => true
                    case _                         => false
                } && shapes.collect { case AssignmentShape.Import(m) => m }.distinct.size == 1
            }

    def bindingOf(id: Identifier): Option[Binding] =
        declOf(id).flatMap { d => if singleOrigin(d.id()) then bindings.get(d.id()) else None }

    def isImportLhs(id: Identifier): Boolean =
        id.argumentIndex == 1 && DeclarationRefs.parentOf(id).exists {
            case c: Call =>
                c.name == Operators.assignment && c.argument.exists {
                    case r: Call => r.name == "import"
                    case _       => false
                }
            case _ => false
        }

    /** The target of the guarded fallback's `x = None` write: not a use of the binding, the same
      * way the import's own lowering target is not.
      */
    def isFallbackNoneLhs(id: Identifier): Boolean =
        id.argumentIndex == 1 && DeclarationRefs.parentOf(id).exists {
            case c: Call =>
                c.name == Operators.assignment && c.argument.exists {
                    case l: Literal => l.code == "None"
                    case _          => false
                }
            case _ => false
        }

    /** Per method, the lowering temporaries read as the base of a member access, by name: one walk
      * of each method's AST, not one per temporary.
      */
    val tmpBases = mutable.HashMap.empty[Long, Map[String, List[Identifier]]]
    def tmpBasesIn(m: Method): Map[String, List[Identifier]] =
        tmpBases.getOrElseUpdate(
          m.id(),
          m.ast.isIdentifier
              .filter(_.name.matches("tmp\\d+"))
              .filter(i =>
                  DeclarationRefs.parentOf(i).exists {
                      case p: Call => p.name == Operators.fieldAccess
                      case _       => false
                  }
              )
              .l
              .groupBy(_.name)
        )

    /** The member accesses of chains rooted at an import binding: the binding pass records them, so
      * the typed pass must not record them a second time under the module of an inner link.
      */
    val bindingChains = mutable.HashSet.empty[Long]

    /** The member chain from a use, through the frontend's `tmpN = a.b` callee lowering. */
    def climb(id: Expression): (List[String], Expression) =
      val members         = mutable.ListBuffer[String]()
      var top: Expression = id
      var go              = true
      while go do
        DeclarationRefs.parentOf(top) match
          case Some(c: Call) if c.name == Operators.fieldAccess && top.argumentIndex == 1 =>
              c.argument.collect { case f: FieldIdentifier => f.canonicalName }
                  .foreach(members += _)
              bindingChains += c.id()
              top = c
          case Some(c: Call)
              if c.name == Operators.assignment && top.argumentIndex == 2 &&
                  c.argument.exists {
                      case i: Identifier => i.argumentIndex == 1 && i.name.matches("tmp\\d+")
                      case _             => false
                  } =>
              // continue at the temporary's use as a receiver/base in the same method
              val tmp = c.argument.collectFirst {
                  case i: Identifier if i.argumentIndex == 1 => i.name
              }.get
              val inMethod = methodOf(c).map(tmpBasesIn(_).getOrElse(tmp, Nil)).getOrElse(Nil)
              inMethod.find(_.id() != c.argument(1).id()) match
                case Some(next) => top = next
                case None       => go = false
          case _ => go = false
      end while
      (members.toList, top)
    end climb

    // Pass 1: every identifier that resolves to an imported binding (via=binding). The frontend
    // can duplicate a receiver identifier node (the base of a chain appears twice at one
    // position); the dedupe keeps one record per site.
    atom.identifier.filterNot(_.name.matches("tmp\\d+")).foreach { id =>
        if !isImportLhs(id) && !isFallbackNoneLhs(id) then
          bindingOf(id).foreach { binding =>
              methodOf(id) match
                case None => collector.skip(id)
                case Some(method) =>
                    collector.add(id) {
                        val (members, top) = climb(id)
                        val involvement    = DeclarationRefs.receiverOf(top)
                        involvement.foreach(bindingChains += _.id())
                        val kind = involvement.map(_ => "call").getOrElse(
                          DeclarationRefs.argumentOf(top).map(_ => "arg").getOrElse("ref")
                        )
                        CallSiteRecord(
                          method.filename,
                          line(id),
                          col(id),
                          method.fullName,
                          binding.module,
                          (binding.path ++ members).mkString("."),
                          kind,
                          "binding",
                          involvement.map(_.methodFullName).getOrElse(""),
                          builtin = isStdlib(binding.module)
                        )
                    }
          }
    }

    // Pass 2: a call attributed only through its resolved name (via=type), and a typed attribute
    // read (`request.method`). A receiver chain names its module through the static type of the
    // identifier at its root (the receiver's declared or recovered type, PEP 484 wrappers
    // unwrapped), never through the recovery's typing of the chain's inner links: `self.flag`
    // typed as its field's declared class would name `self.flag.startswith` after the field, not
    // after the str it holds. Type recovery names a call even where a parameter or local shadows
    // the import: a root of the module's own top-level name that no binding resolves is that
    // local, not the module.
    val covered = mutable.HashSet.empty[Long]
    def receiverRootOf(e: Expression): Option[Expression] =
        Some {
            var cur: AstNode = e
            while cur.isInstanceOf[Call] && cur.asInstanceOf[Call].name == Operators.fieldAccess
            do cur = cur.asInstanceOf[Call].argument(1)
            cur.asInstanceOf[Expression]
        }

    /** A receiver chain rooted at a `super()` call: the receiver is the enclosing class, project
      * code, whose inherited-base uses the heritage record already attributes at the class line.
      */
    def isSuperReceiver(e: Expression): Boolean =
        receiverRootOf(e) match
          case Some(c: Call) => c.name == "super"
          case _             => false

    /** The static type of the identifier at the root of a receiver chain: unwrapped to the runtime
      * type, `Wrapper` when the root is declared as a PEP 484 wrapper that names no single runtime
      * type (the recovery echoes the wrapper into the callee's name), or `NoType` when the root
      * carries no type at all.
      *
      * An untyped `self`/`cls` root is `NoType` too: its type is the enclosing class, project code,
      * and a member read through it is attributed by the class's own records (heritage names the
      * base's module at the class line); the recovery's naming of such a read after the inherited
      * base would put a record on every inherited member's line.
      */
    enum RootType:
      case Unwrapped(t: String)
      case Wrapper
      case NoType

    def rootTypeOf(e: Expression): RootType =
        receiverRootOf(e) match
          case Some(i: Identifier) if i.name == "self" || i.name == "cls" =>
              // the enclosing class is project code; the recovery types self with the inherited
              // base, which the heritage record already attributes at the class's line
              RootType.NoType
          case Some(i: Identifier) =>
              val types = (i.typeFullName +: i.dynamicTypeHintFullName)
                  .filterNot(t => t == "ANY" || t.isEmpty)
              val unwrapped = types.flatMap(receiverTypeOf)
              unwrapped.headOption.map(RootType.Unwrapped.apply).getOrElse {
                  if types.exists(t => t.startsWith("typing.") || t.startsWith("__builtin."))
                  then RootType.Wrapper
                  else RootType.NoType
              }
          case _ => RootType.NoType
    def shadowed(m: String, root: Option[Expression]): Boolean =
        root.exists {
            case i: Identifier => i.name == m.split('.').head && bindingOf(i).isEmpty
            case _             => false
        }
    def moduleOfPrefix(file: String, t: String): Option[String] =
        fileModules.get(file).toList.flatten.filter(m => t == m || t.startsWith(m + "."))
            .sortBy(-_.length).headOption

    /** The static type a receiver is read through. A PEP 484 wrapper (typing.Optional[User],
      * typing.List[str]) is not a value: the receiver's runtime type is the wrapped one, so
      * `user.is_admin` on `user: Optional[User]` belongs to User, and the other wrappers resolve to
      * builtins the slice does not name. A wrapper over exactly one type unwraps to it; the rest
      * yield None, and so does the wrapper's own module (typing is recorded from annotations, not
      * from attribute reads through wrappers).
      */
    def receiverTypeOf(t: String): Option[String] =
      val bare = t.startsWith("typing.") || t.startsWith("__builtin.")
      if !bare then Some(t)
      else if t.endsWith("]") && t.contains("[") then
        val inner = t.substring(t.indexOf('[') + 1, t.length - 1)
        if !inner.contains(",") && !inner.contains("[") && inner.nonEmpty &&
          !inner.startsWith("typing.") && !inner.startsWith("__builtin.")
        then Some(inner)
        else None
      else None

    atom.call.nameExact(Operators.fieldAccess).filterNot(fa => bindingChains.contains(fa.id())).foreach {
        fa =>
            methodOf(fa) match
              case None => collector.skip(fa)
              case Some(method) =>
                  val base = fa.argument.l.find(_.argumentIndex == 1)
                  val file = method.filename
                  val member = fa.argument.collectFirst {
                      case f: FieldIdentifier => f.canonicalName
                  }.getOrElse("")
                  val rootT = rootTypeOf(fa)
                  val chainTypes = rootT match
                    case RootType.Unwrapped(t) => List(t)
                    case _                     => Nil
                  chainTypes.foreach { t =>
                      moduleOfPrefix(file, t).foreach { m =>
                          if !shadowed(m, base) then
                            collector.add(fa) {
                                CallSiteRecord(
                                  file,
                                  line(fa),
                                  col(fa),
                                  method.fullName,
                                  m,
                                  s"${t.stripPrefix(m).stripPrefix(".")}.$member".stripPrefix("."),
                                  "ref",
                                  "type",
                                  "",
                                  builtin = isStdlib(m)
                                )
                            }
                            // the read's base is covered: a call on the same chain is the binding
                            // pass's or this pass's, never a second typed record
                            DeclarationRefs.receiverOf(fa).foreach(covered += _.id())
                      }
                  if rootT == RootType.NoType && chainTypes.isEmpty then
                    // no root type: the raw recovered name of the field access is all there is
                    moduleOfPrefix(file, fa.methodFullName).foreach { m =>
                        if !shadowed(m, base) then
                          collector.add(fa) {
                              CallSiteRecord(
                                file,
                                line(fa),
                                col(fa),
                                method.fullName,
                                m,
                                fa.methodFullName.stripPrefix(m).stripPrefix("."),
                                "ref",
                                "type",
                                "",
                                builtin = isStdlib(m)
                              )
                          }
                          DeclarationRefs.receiverOf(fa).foreach(covered += _.id())
                    }
                  end if
                  }
    }

    atom.call
        .filterNot(c =>
            c.name.startsWith("<operator>") || covered.contains(c.id()) ||
                bindingChains.contains(c.id())
        )
        .foreach { c =>
            methodOf(c) match
              case None => collector.skip(c)
              case Some(method) =>
                  val file = method.filename
                  // when the receiver chain's root has a static type, the callee is named through
                  // it (the recovery may have prefixed an inner link's type instead:
                  // Optional[User].is_admin, or a field's declared class for a str method)
                  val rootT = rootTypeOf(c)
                  val receiverNamed: Map[String, String] = rootT match
                    case RootType.Unwrapped(t) =>
                        (for m <- moduleOfPrefix(file, s"$t.${c.name}")
                        yield m -> s"$t.${c.name}".stripPrefix(m).stripPrefix(".")).toMap
                    case _ => Map.empty
                  if receiverNamed.nonEmpty then
                    receiverNamed.foreach { (m, path) =>
                        if !shadowed(m, c.receiver.l.headOption.flatMap(receiverRootOf)) then
                          collector.add(c) {
                              CallSiteRecord(
                                file,
                                line(c),
                                col(c),
                                method.fullName,
                                m,
                                path,
                                "call",
                                "type",
                                s"$m.$path",
                                builtin = isStdlib(m)
                              )
                          }
                    }
                  else if rootT == RootType.NoType && !isSuperReceiver(c) then
                    // the receiver chain has no typed root: the recovery's name for the callee
                    // is all there is. A name under the typing module is that module's wrapper
                    // echoed into the callee (ClassVar[List[X]].append) — wrappers are not
                    // runtime receivers, and the typing records the slice emits come from
                    // annotations (decltype)
                    moduleOfPrefix(file, c.methodFullName).foreach { m =>
                        if m != "typing" &&
                          !shadowed(m, c.receiver.l.headOption.flatMap(receiverRootOf))
                        then
                          collector.add(c) {
                              CallSiteRecord(
                                file,
                                line(c),
                                col(c),
                                method.fullName,
                                m,
                                c.methodFullName.stripPrefix(m).stripPrefix("."),
                                "call",
                                "type",
                                c.methodFullName,
                                builtin = isStdlib(m)
                              )
                          }
                    }
                  end if
        }

    // Pass 3: heritage, decorators and declaration types, matched against the file's import
    // bindings by name.
    val bindingsByName: Map[(String, String), Binding] =
        bindings.values.toList
            .flatMap(b => b.path.headOption.map(n => (b.file, n) -> b))
            .groupBy(_._1)
            .map((k, bs) => k -> bs.map(_._2).minBy(b => (b.module, b.path.mkString("."))))
    def bindingByName(file: String, name: String): Option[Binding] =
        bindingsByName.get((file, name))

    // heritage: `class Handler(Celery)` - the base names a module the file imports, or a member
    // of one; the record sits on the class's own line
    atom.typeDecl.filterNot(_.isExternal).foreach { t =>
      val file = t.filename
      t.inheritsFromTypeFullName.filterNot(n => n == "ANY" || n.isEmpty).foreach { base =>
        val moduleOf: Option[(String, String, Boolean)] =
            moduleOfPrefix(file, base)
                .map(m => (m, base.stripPrefix(m).stripPrefix("."), isStdlib(m)))
                .orElse {
                    bindingByName(file, base.split('.').head).map { b =>
                        (b.module, base, isStdlib(b.module))
                    }
                }
        moduleOf.foreach { (m, path, builtin) =>
            collector.add(t) {
                CallSiteRecord(
                  file,
                  line(t),
                  -1,
                  enclosingMethodOf(t).map(_.fullName).getOrElse(""),
                  m,
                  path,
                  "extends",
                  "name",
                  base,
                  builtin = builtin
                )
            }
        }
      }
    }

    // decorators: `@app.route("/x")` - the base is either an import binding of the file, or a
    // value whose type names one; the annotation and its lowered field access share the line
    atom.annotation.foreach { a =>
        methodOf(a) match
          case None => collector.skip(a)
          case Some(method) =>
              val file = method.filename
              val base = a.name.takeWhile(_ != '.')
              val rest = a.name.drop(base.length + 1).split('.').toList.filter(_.nonEmpty)
              bindingByName(file, base)
                  .map { b =>
                      CallSiteRecord(
                        file,
                        line(a),
                        col(a),
                        method.fullName,
                        b.module,
                        (b.path ++ rest).mkString("."),
                        "decorator",
                        "name",
                        a.fullName,
                        builtin = isStdlib(b.module)
                      )
                  }
                  .orElse {
                      val baseTypes = atom.identifier.nameExact(base)
                          .filter(i =>
                              i.lineNumber.map(_.intValue) == a.lineNumber.map(_.intValue)
                          )
                          .flatMap(i => i.typeFullName +: i.dynamicTypeHintFullName)
                          .filterNot(t => t == "ANY" || t.isEmpty)
                      baseTypes.collectFirst { t =>
                          moduleOfPrefix(file, t).map { m =>
                              CallSiteRecord(
                                file,
                                line(a),
                                col(a),
                                method.fullName,
                                m,
                                s"${t.stripPrefix(m).stripPrefix(".")}.${
                                        rest.mkString(".")
                                    }".stripPrefix("."),
                                "decorator",
                                "type",
                                a.fullName,
                                builtin = isStdlib(m)
                              )
                          }
                      }.flatten
                  }
                  .foreach(r => collector.add(a)(r))
    }

    // declaration types (kind decltype): parameter, local and return annotations, from the types
    // the frontend put on the nodes
    def typeOrigin(file: String, t: String): Option[(String, String, Boolean)] =
        // an annotation never names a bare module of this file's imports; a type that is one
        // comes from the recovery's naming of a binding or a lowering temporary, not a
        // declaration
        if fileModules.get(file).exists(_.contains(t)) then None
        else
          moduleOfPrefix(file, t)
              .map(m => (m, genericBase(t.stripPrefix(m).stripPrefix(".")), isStdlib(m)))
              .orElse {
                  bindingByName(file, t.split('.').head).map { b =>
                      (b.module, genericBase(t), isStdlib(b.module))
                  }
              }

    /** `Iterator[typing.Tuple[str, Path]]` names `Iterator`; the full type stays in resolvedMethod.
      */
    def genericBase(path: String): String = path.takeWhile(_ != '[')

    def decltype(node: AstNode, method: Method, t: String): Unit =
      // a return type sits on the annotation's own line, from the tag the frontend writes for
      // an annotated return only (a multi-line signature puts it on a later line than the
      // def); the type recovery also infers return types, which are not declarations, so a
      // method without the tag has no return decltype
      val at = node match
        case _: MethodReturn =>
            method.tag.nameExact("callsites-return-line").value.flatMap(v =>
                Try(Integer.valueOf(v.trim.toInt)).toOption
            ).headOption
        case other => other.lineNumber
      if at.exists(_.intValue > 0) && method.filename.nonEmpty then
        typeOrigin(method.filename, t).foreach { (m, path, builtin) =>
            collector.add(node) {
                CallSiteRecord(
                  method.filename,
                  at.get,
                  -1,
                  method.fullName,
                  m,
                  path,
                  "decltype",
                  "name",
                  t,
                  builtin = builtin
                )
            }
        }
    end decltype

    atom.method.parameter.filterNot(p => p.name == "self" || p.name == "cls").foreach { p =>
        methodOf(p).foreach { method =>
            (p.typeFullName +: p.dynamicTypeHintFullName)
                .filterNot(t => t == "ANY" || t.isEmpty || t.startsWith(method.fullName))
                .foreach(t => decltype(p, method, t))
        }
    }
    atom.local.filterNot(_.name.matches("tmp\\d+")).foreach { l =>
        methodOf(l).foreach { method =>
          val isImportBinding = declOfNode(l).exists(d => importBound.contains(d.id()))
          if !isImportBinding then
            l.dynamicTypeHintFullName
                .filterNot(t =>
                    t == "ANY" || t.isEmpty || t.contains("<") ||
                        t.startsWith(method.fullName)
                )
                .foreach(t => decltype(l, method, t))
        }
    }
    atom.method.methodReturn.collect { case mr if !mr.method.name.contains("<") => mr }.foreach {
        mr =>
          val m = mr.method
          // the declared return annotation is the METHOD_RETURN's own type; the recovery's
          // inferred hints land in the dynamic hint and are not declarations, and the synthetic
          // class-adapter copies of a method carry the class's line, not the def's
          Seq(mr.typeFullName)
              .filterNot(t =>
                  t == "ANY" || t.isEmpty || t.contains("<") ||
                      t.startsWith(m.fullName)
              )
              .foreach(t => decltype(mr, m, t))
    }

    val slice = collector.result()
    if purlsByModule.isEmpty then slice
    else
      slice.copy(callsites =
          slice.callsites.map(r =>
              r.copy(purl = purlOf(r.module))
          )
      )
  end calculate

  /** The import call's arguments, unquoted: `(from, name[, alias])`. */
  private def importArgs(importCall: Call): List[String] =
      importCall.argument.l.sortBy(_.argumentIndex).collect { case l: Literal =>
          l.code.stripPrefix("\"").stripSuffix("\"").stripPrefix("'").stripSuffix("'")
      }.toList
end PythonCallSiteSource

private[callsites] final case class Binding(file: String, module: String, path: List[String])

private[callsites] sealed trait AssignmentShape
private[callsites] object AssignmentShape:
  final case class Import(module: String) extends AssignmentShape
  case object NoneLiteral                 extends AssignmentShape
  case object Other                       extends AssignmentShape
