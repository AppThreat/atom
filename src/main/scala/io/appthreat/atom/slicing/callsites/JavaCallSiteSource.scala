package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.utils.IOUtils

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** The Java source of the callsites slice: a port of the measured prototype (callsites-eval
  * `harness/proto/java_callsites.scala`), extended with the kinds decision 0001 counts.
  *
  * Every reference whose resolved class is not a project TYPE_DECL is a use, each with its own
  * `kind`: calls and constructors (`call`, `new`), annotations, heritage clauses (`extends`,
  * `implements`), static field and enum constant reads and class literals (`ref`), method
  * references (`methodref`), and declaration types (`decltype`: parameters, fields, locals, return
  * and `throws` types, cast and `instanceof` types). Lombok-generated members are uses too; the
  * frontend models them (chen#245), so nothing here reads source text.
  *
  * A typed call belongs to the receiver's static type (decision 0001, P2): `module` is the class
  * the receiver is declared to hold, and the class that declares the callee rides along as
  * `declaringModule` when it differs. `via=binding` where the class is named in the source (static
  * calls, constructors, annotations, heritage, declaration types), `via=type` for calls and reads
  * through a typed receiver.
  *
  * The JDK is a dependency (`builtin: true`): membership comes from the committed list of the JDK's
  * exported packages, generated once from the module image's `jmod` descriptions, never from a
  * hand-written prefix list. `javax.servlet` is not on it; `javax.xml` is.
  */
final class JavaCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "java"):

  private val projectTypes: Set[String] =
      atom.typeDecl.isExternal(false).fullName.toSet

  private def unresolved(cls: String): Boolean =
      cls.isEmpty || cls.startsWith("<unresolved") || cls.startsWith("<unknown") ||
          cls == "ANY" || cls == "void" || cls.contains("<unresolvedNamespace>")

  /** The use's class is external when it resolved to something and is not a project type, the
    * graph's own verdict (`TypeDecl.isExternal`), never a package-name guess. Nested classes
    * resolve under the top-level class's file, so a `$` suffix names the same project type.
    */
  private def externalClass(cls: String): Boolean =
      cls.nonEmpty && !unresolved(cls) && !projectTypes.contains(cls) &&
          !projectTypes.contains(cls.takeWhile(_ != '$'))

  /** The declaring class of a resolved method full name (`pkg.Type.method:sig()`). */
  private def classOfMethod(fullName: String): String =
    val member = fullName.takeWhile(_ != ':')
    member.lastIndexOf('.') match
      case -1 => ""
      case i  => member.substring(0, i)

  private val jdkPackages: Set[String] =
      Try {
          val stream = getClass.getResourceAsStream("/callsites/jdk-packages.txt")
          val text   = Try(scala.io.Source.fromInputStream(stream, "UTF-8").mkString).toOption
          stream.close()
          text.getOrElse("")
      }.getOrElse("").split("\n").map(_.trim).filter(_.nonEmpty).toSet

  /** Whether a class belongs to the JDK: its package is one the JDK exports. */
  private def isJdk(cls: String): Boolean =
      cls.lastIndexOf('.') match
        case -1 => false
        case i  => jdkPackages.contains(cls.substring(0, i))

  private def lineOf(n: AstNode): Integer = n match
    case e: Expression        => e.lineNumber.getOrElse(-1)
    case a: Annotation        => a.lineNumber.getOrElse(-1)
    case t: TypeDecl          => t.lineNumber.getOrElse(-1)
    case p: MethodParameterIn => p.lineNumber.getOrElse(-1)
    case l: Local             => l.lineNumber.getOrElse(-1)
    case m: Member            => m.lineNumber.getOrElse(-1)
    case r: MethodReturn      => r.lineNumber.getOrElse(-1)
    case t: TypeRef           => t.lineNumber.getOrElse(-1)
    case m: MethodRef         => m.lineNumber.getOrElse(-1)
    case _                    => -1

  private def colOf(n: AstNode): Integer = n match
    case e: Expression => e.columnNumber.getOrElse(-1)
    case a: Annotation => a.columnNumber.getOrElse(-1)
    case _             => -1

  /** The type an expression carries in the graph, when it resolved to something. */
  private def staticTypeOf(e: Expression): Option[String] = e match
    case c: Call       => Some(c.typeFullName)
    case i: Identifier => Some(i.typeFullName)
    case l: Literal    => Some(l.typeFullName)
    case m: MethodRef  => Some(m.typeFullName)
    case t: TypeRef    => Some(t.typeFullName)
    case _             => None

  private def resolvedTypeOf(e: Expression): Option[String] =
      staticTypeOf(e).filter(t => t.nonEmpty && t != "ANY" && t != "void")

  /** The static type of a call's receiver: the base argument's type, when it resolved to something.
    * This is the class the code relies on (decision 0001, P2), which may differ from the class that
    * declares the callee.
    */
  private def receiverStaticType(c: Call): Option[String] =
      c.argument.collectFirst {
          case e: Expression if e.argumentIndex == 0 => resolvedTypeOf(e)
      }.flatten

  private def record(node: AstNode, file: String, enclosingMethod: String)(
    build: => CallSiteRecord
  ): Unit =
      if file.nonEmpty && file != "<empty>" then collector.add(node)(build)
      else collector.skip(node)

  def calculate(): ProgramCallSites =
    calls()
    annotations()
    heritage()
    refs()
    methodRefs()
    declarationTypes()
    collector.result()

  private def calls(): Unit =
      atom.call.filterNot(_.name.startsWith("<operator>")).foreach { c =>
        val cls = classOfMethod(c.methodFullName)
        if externalClass(cls) then
          methodOf(c) match
            case None => collector.skip(c)
            case Some(method) =>
                record(c, method.filename, method.fullName) {
                    val isNew = c.name == "<init>"
                    // A static call names its class in the source, as the receiver the frontend
                    // keeps (the class-name identifier), or with none at all.
                    val static       = c.dispatchType == DispatchTypes.STATIC_DISPATCH
                    val receiverType = receiverStaticType(c)
                    val module =
                        if isNew || static || receiverType.isEmpty || unresolved(receiverType.get)
                        then cls
                        else receiverType.get
                    CallSiteRecord(
                      method.filename,
                      lineOf(c),
                      colOf(c),
                      method.fullName,
                      module,
                      if isNew then "" else c.name,
                      if isNew then "new" else "call",
                      if isNew || static then "binding" else "type",
                      c.methodFullName,
                      isJdk(module),
                      declaringModule = if module != cls then cls else ""
                    )
                }
        end if
      }

  /** One record per annotation site: the annotation of a method and that of its parameter are
    * different sites. A class annotation has no enclosing method; its record names the class's file
    * the way the JS source names a decorator's.
    */
  private def annotations(): Unit =
      atom.annotation.foreach { a =>
        val cls = a.fullName
        if externalClass(cls) then
          methodOf(a) match
            case None =>
                record(
                  a,
                  fileOf(a).getOrElse(""),
                  enclosingMethodOf(a).map(_.fullName).getOrElse("")
                ) {
                    CallSiteRecord(
                      fileOf(a).getOrElse(""),
                      lineOf(a),
                      colOf(a),
                      enclosingMethodOf(a).map(_.fullName).getOrElse(""),
                      cls,
                      "",
                      "annotation",
                      "binding",
                      cls,
                      isJdk(cls)
                    )
                }
            case Some(method) =>
                record(a, method.filename, method.fullName) {
                    CallSiteRecord(
                      method.filename,
                      lineOf(a),
                      colOf(a),
                      method.fullName,
                      cls,
                      "",
                      "annotation",
                      "binding",
                      cls,
                      isJdk(cls)
                    )
                }
        end if
      }

  /** The heritage clauses of project classes: the first base the frontend records is the
    * superclass, the rest are interfaces; an implicit `java.lang.Object` is nobody's writing and
    * never a record (decision 0001, P1).
    */
  private def heritage(): Unit =
      atom.typeDecl.isExternal(false).foreach { t =>
        val bases = t.inheritsFromTypeFullName.l.filterNot(b => b == "ANY" || b.isEmpty)
        val written = bases match
          case "java.lang.Object" :: rest => rest
          case _                          => bases
        val file = fileOf(t).getOrElse("")
        written.zipWithIndex.foreach { (base, idx) =>
            if externalClass(base) then
              record(t, file, enclosingMethodOf(t).map(_.fullName).getOrElse("")) {
                  CallSiteRecord(
                    file,
                    lineOf(t),
                    -1,
                    enclosingMethodOf(t).map(_.fullName).getOrElse(""),
                    base,
                    "",
                    if idx == 0 then "extends" else "implements",
                    "binding",
                    base,
                    isJdk(base)
                  )
              }
        }
      }

  /** Static field and enum constant reads and class literals, all with the class named in the
    * source (`via=binding`). A field access that only carries a method callee (`sec.formLogin()`)
    * is not a read: its field identifier names the method of the call it feeds.
    */
  private def refs(): Unit =
      atom.call.nameExact("<operator>.fieldAccess").foreach { fa =>
          fa.argument.collectFirst { case fi: FieldIdentifier => fi.canonicalName }.foreach {
              member =>
                // A field access that only carries a method callee sits in the call's base slot
                // (argument 0) and names the method; a read sits anywhere else.
                val isMethodCallee = member != "class" && fa.argumentIndex == 0 &&
                    DeclarationRefs.parentOf(fa).exists {
                        case c: Call => c.name == member
                        case _       => false
                    }
                if !isMethodCallee then
                  // `Foo.class`: the class literal's base identifier carries the class itself;
                  // a field read's base carries the receiver's type
                  val classLiteral = member == "class"
                  // the field access's base sits at argument 1 (its field identifier at 2)
                  fa.argument.collectFirst {
                      case e: Expression if e.argumentIndex == 1 => e
                  }.flatMap(resolvedTypeOf).foreach { module =>
                      if externalClass(module) then
                        methodOf(fa) match
                          case None => collector.skip(fa)
                          case Some(method) =>
                              val at = if classLiteral then
                                fa.argument.collectFirst {
                                    case e: Expression if e.argumentIndex == 1 => e
                                }.getOrElse(fa)
                              else fa
                              record(fa, method.filename, method.fullName) {
                                  CallSiteRecord(
                                    method.filename,
                                    lineOf(at),
                                    colOf(at),
                                    method.fullName,
                                    module,
                                    if classLiteral then "" else member,
                                    "ref",
                                    "binding",
                                    module,
                                    isJdk(module)
                                  )
                              }
                  }
                end if
          }
      }

  /** `Foo::bar` and bound method references: the frontend resolves them to METHOD_REF nodes no
    * slice read until now.
    */
  private def methodRefs(): Unit =
      atom.methodRef.foreach { m =>
        val cls = classOfMethod(m.methodFullName)
        if externalClass(cls) then
          methodOf(m) match
            case None => collector.skip(m)
            case Some(method) =>
                record(m, method.filename, method.fullName) {
                    CallSiteRecord(
                      method.filename,
                      lineOf(m),
                      colOf(m),
                      method.fullName,
                      cls,
                      m.methodFullName.split(':').head.split('.').lastOption.getOrElse(""),
                      "methodref",
                      "binding",
                      m.methodFullName,
                      isJdk(cls)
                    )
                }
      }

  /** Declaration types (decision 0001, P1): parameters, fields, locals, return types, cast and
    * `instanceof` types, and the `throws` clauses the frontend tags on the method. Each keeps the
    * position of the type itself; the METHOD_RETURN node carries the return type's own line.
    */
  private def declarationTypes(): Unit =
    def recordDecl(node: AstNode, cls: String, enclosing: Option[Method]): Unit =
        if externalClass(cls) then
          enclosing match
            case Some(method) =>
                record(node, method.filename, method.fullName) {
                    CallSiteRecord(
                      method.filename,
                      lineOf(node),
                      colOf(node),
                      method.fullName,
                      cls,
                      "",
                      "decltype",
                      "binding",
                      cls,
                      isJdk(cls)
                    )
                }
            case None => collector.skip(node)

    atom.parameter.filterNot(_.name == "this").foreach { p =>
        recordDecl(p, p.typeFullName, methodOf(p))
    }
    // A field's type sits on the member, under its type declaration, not under any method. A
    // member without a position is a generated one nobody wrote (the Lombok log field).
    atom.member.foreach { m =>
        if externalClass(m.typeFullName) && m.lineNumber.exists(_.intValue > 0) then
          val typeDecl = m.typeDecl
          val file     = typeDecl.filename
          if file.nonEmpty then
            collector.add(m) {
                CallSiteRecord(
                  file,
                  m.lineNumber.getOrElse(-1),
                  m.columnNumber.getOrElse(-1),
                  enclosingMethodOf(m).map(_.fullName).getOrElse(""),
                  m.typeFullName,
                  "",
                  "decltype",
                  "binding",
                  m.typeFullName,
                  isJdk(m.typeFullName)
                )
            }
    }
    atom.local.foreach { l => recordDecl(l, l.typeFullName, methodOf(l)) }
    atom.methodReturn.foreach { r => recordDecl(r, r.typeFullName, enclosingMethodOf(r)) }
    atom.typeRef.foreach { t => recordDecl(t, t.typeFullName, methodOf(t)) }

    // `throws IOException`: read from the frontend's tag, which carries the type and its line
    atom.method.foreach { method =>
        method.tag.nameExact("callsites-throws").foreach { tag =>
            parseThrowsTag(tag.value).foreach { (throwsType, line) =>
                if externalClass(throwsType) then
                  collector.add(method) {
                      CallSiteRecord(
                        method.filename,
                        line,
                        -1,
                        method.fullName,
                        throwsType,
                        "",
                        "decltype",
                        "binding",
                        throwsType,
                        isJdk(throwsType)
                      )
                  }
            }
        }
    }
  end declarationTypes

  private def parseThrowsTag(value: String): Option[(String, Integer)] =
      io.circe.parser.parse(value).toOption.flatMap { json =>
          for
            t <- json.hcursor.downField("type").as[String].toOption
            l <- json.hcursor.downField("line").as[Int].toOption
          yield (t, Integer.valueOf(l))
      }

end JavaCallSiteSource
