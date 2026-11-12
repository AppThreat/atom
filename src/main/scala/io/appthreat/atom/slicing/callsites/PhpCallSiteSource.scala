package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import scala.util.Try

/** The PHP source of the callsites slice, on the graph php2atom builds with `vendor/` left out.
  *
  * Every reference whose class or function is not the project's is a use (decision 0001, P1), each
  * with its own `kind` and the position of the name the source writes: `new`, `call` (functions,
  * static calls, and instance calls on a receiver whose class is declared), `ref` (`Foo::C`,
  * `Foo::class`, `Foo::$p`, `instanceof` and `catch` types), `extends` (extends, implements,
  * interface extends, trait `use`, enum implements), `attribute` and `decltype` (each class a
  * parameter, return, property or class-constant type writes). `self`, `static`, `parent` and the
  * scalar types are never records, and neither is a `use` statement.
  *
  * A typed call belongs to the receiver's static type (decision 0001, P2): the module is the
  * receiver's class and the path the member. A class is external when the graph has no project
  * declaration of it, never by a namespace guess; with Composer's metadata in the graph a class
  * must also map to an installed package, as a class that exists nowhere is nobody's use. PHP's own
  * functions and classes are a dependency (`builtin: true`), decided from the committed list
  * generated from a PHP binary (`php-builtins.txt`), never a hand-written one.
  *
  * Each record carries the `purl` of the Composer package that declares the class, from
  * `vendor/composer/installed.json` and the autoload maps php2atom stores as config files. A
  * function declared in the global namespace has no namespace to map by and stays without one.
  */
final class PhpCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "php"):

  private val projectTypes: Set[String] =
      atom.typeDecl.isExternal(false).fullName.map(_.toLowerCase).toSet

  /** The project's own functions, by their lower-cased full name: PHP's names are case-insensitive.
    */
  private val projectFunctions: Set[String] =
      atom.method.isExternal(false).fullName.filterNot(n => n.contains("->") || n.contains("::"))
          .map(_.toLowerCase).toSet

  private val (builtinFunctions, builtinClasses) =
    val lines = Try {
        val stream = getClass.getResourceAsStream(PhpCallSiteSource.BuiltinsResource)
        val text   = scala.io.Source.fromInputStream(stream, "UTF-8").mkString
        stream.close()
        text.split("\n").toList
    }.getOrElse(Nil)
    (
      lines.filter(_.startsWith("f ")).map(_.drop(2).trim).toSet,
      lines.filter(_.startsWith("c ")).map(_.drop(2).trim).toSet
    )

  private lazy val composer = PhpComposerMap(atom)

  private def clean(name: String): String = name.stripPrefix("\\")

  /** The class a call's full name names, read the way `calls()` reads it: an instance call carries
    * the receiver's class before `->` (php2atom names the constructor of `new Foo` the same way), a
    * static one the class before `::`. None for a plain function call.
    */
  def classOfCallName(full: String): Option[String] =
      full.indexOf("->") match
        case i if i > 0 => Some(clean(full.substring(0, i)))
        case _ =>
            full.indexOf("::") match
              case i if i > 0 => Some(clean(full.substring(0, i)))
              case _          => None

  private def unusable(name: String): Boolean =
      name.isEmpty || name.exists(c => c == '<' || c == '$' || c == ' ' || c == '(') ||
          name == "ANY" || PhpCallSiteSource.SpecialNames.contains(name.toLowerCase)

  private def isBuiltinClass(cls: String): Boolean = builtinClasses.contains(cls.toLowerCase)

  /** Whether `cls` is a use of something the project does not declare: PHP's own class, or one a
    * Composer package declares (any class the project does not declare, without Composer metadata).
    */
  def externalClass(cls: String): Boolean =
      !unusable(cls) && !projectTypes.contains(cls.toLowerCase) &&
          (isBuiltinClass(cls) || composer.declaredByPackage(cls))

  /** The package URL of the Composer package that declares `cls`; a PHP built-in has none. */
  def purlOf(cls: String): String =
      if isBuiltinClass(cls) then "" else composer.purlOfClass(cls)

  /** The name of the Composer package that declares `cls`, as its purl spells it; a PHP built-in is
    * none's.
    */
  def packageNameOf(cls: String): Option[String] =
      if isBuiltinClass(cls) then None else composer.packageNameOfClass(cls)

  private def lineOf(n: AstNode): Integer = n match
    case e: Expression        => e.lineNumber.getOrElse(-1)
    case t: TypeDecl          => t.lineNumber.getOrElse(-1)
    case p: MethodParameterIn => p.lineNumber.getOrElse(-1)
    case l: Local             => l.lineNumber.getOrElse(-1)
    case m: Member            => m.lineNumber.getOrElse(-1)
    case r: MethodReturn      => r.lineNumber.getOrElse(-1)
    case m: Method            => m.lineNumber.getOrElse(-1)
    case _                    => -1

  private def colOf(n: AstNode): Integer = n match
    case e: Expression => e.columnNumber.getOrElse(-1)
    case _             => -1

  private def record(node: AstNode, file: String)(build: => CallSiteRecord): Unit =
      if file.nonEmpty && file != "<empty>" && lineOf(node) > 0 then collector.add(node)(build)
      else collector.skip(node)

  def calculate(): ProgramCallSites =
    calls()
    typeRefs()
    attributes()
    writtenNames()
    collector.result()

  /** A language construct lowered to a call named after its keyword (`echo`, `isset`, `declare`):
    * the function-call form only, `Rule::array()` and `$x->empty()` are methods.
    */
  private def isConstruct(c: Call): Boolean =
      PhpCallSiteSource.Constructs.contains(c.name) && c.methodFullName == c.name

  /** The calls php2atom makes up while lowering (`$xs[] = v`, the iterator calls of a `foreach`):
    * nobody wrote them, so none is a use.
    */
  private lazy val synthetic: Set[Long] =
      atom.tag.name(PhpCallSiteSource.SyntheticCallTag)._taggedByIn.map(_.id()).toSet

  /** Where the method name of a typed instance call sits; the call node itself is at the start of
    * its receiver, which a fluent chain writes on another line.
    */
  private lazy val namePositions: Map[Long, (Integer, Integer)] =
      atom.tag.name(PhpCallSiteSource.NamePositionTag).flatMap { tag =>
          for
            json <- io.circe.parser.parse(tag.value).toOption
            line <- json.hcursor.downField("line").as[Int].toOption
            col = json.hcursor.downField("col").as[Int].getOrElse(-1)
            holder <- tag._taggedByIn.headOption
          yield holder.id() -> (Integer.valueOf(line), Integer.valueOf(col))
      }.toMap

  private def calls(): Unit =
      atom.call.filterNot(c =>
          c.name.startsWith("<operator>") || isConstruct(c) ||
              synthetic.contains(c.id())
      ).foreach { c =>
          methodOf(c).foreach { method =>
            val full = c.methodFullName
            val add = (module: String, path: String, kind: String, via: String) =>
              val at = namePositions.get(c.id()).filter(_ => kind == "call" && via == "type")
              record(c, method.filename) {
                  CallSiteRecord(
                    method.filename,
                    at.map(_._1).getOrElse(lineOf(c)),
                    at.map(_._2).getOrElse(colOf(c)),
                    method.fullName,
                    module,
                    path,
                    kind,
                    via,
                    full,
                    isBuiltinClass(module) || (kind == "call" && path.isEmpty && isBuiltinFunction(
                      module
                    )),
                    purl = if path.isEmpty && kind == "call" then purlOfFunction(module)
                    else purlOf(module)
                  )
              }
            val static = c.dispatchType == DispatchTypes.STATIC_DISPATCH
            full.indexOf("::") match
              // `Foo::m()`: the class is named in the source
              case i if i > 0 && static =>
                  val cls = clean(full.substring(0, i))
                  if externalClass(cls) then
                    add(composer.declaredCase(cls), full.substring(i + 2), "call", "binding")
              case _ =>
                  full.indexOf("->") match
                    case i if i > 0 =>
                        val cls    = clean(full.substring(0, i))
                        val member = full.substring(i + 2)
                        if externalClass(cls) then
                          // php2atom names the constructor of `new Foo` this way
                          if member == "__construct" then
                            add(composer.declaredCase(cls), "", "new", "binding")
                          else add(composer.declaredCase(cls), member, "call", "type")
                    case _ => functionCall(c, full, add)
          }
      }

  private def isBuiltinFunction(name: String): Boolean = builtinFunctions.contains(name.toLowerCase)

  /** A function call, `foo()`: the namespace's function when the project or a package declares one,
    * else the global function PHP falls back to (php2atom carries its name on the call).
    */
  private def functionCall(
    c: Call,
    full: String,
    add: (String, String, String, String) => Unit
  ): Unit =
    val name     = clean(full)
    val fallback = c.dynamicTypeHintFullName.headOption.map(clean)
    if unusable(name) || c.name.startsWith("<") then ()
    else if projectFunctions.contains(name.toLowerCase) then ()
    else
      val target = fallback match
        case Some(global) if !projectFunctions.contains(global.toLowerCase) => Some(global)
        case Some(_)                                                        => None
        case None                                                           => Some(name)
      target.filterNot(unusable).foreach { fn =>
          if isBuiltinFunction(fn) then add(fn.toLowerCase, "", "call", "binding")
          else if composer.functionExternal(fn) then add(fn, "", "call", "binding")
      }

  private def purlOfFunction(fn: String): String =
      if isBuiltinFunction(fn) then "" else composer.purlOfFunction(fn)

  /** `Foo::C`, `Foo::class`, `Foo::$p`, `instanceof Foo` and `catch (Foo $e)`: the type reference
    * php2atom makes of a class name, at the name's own position.
    */
  private def typeRefs(): Unit =
      atom.typeRef.foreach { t =>
        val cls = clean(t.typeFullName)
        if externalClass(cls) then
          methodOf(t).foreach { method =>
            val member = DeclarationRefs.parentOf(t).collect {
                case fa: Call if fa.name == "<operator>.fieldAccess" =>
                    fa.argument.collectFirst {
                        case f: FieldIdentifier                    => f.canonicalName
                        case i: Identifier if i.argumentIndex == 2 => i.name
                    }.getOrElse("")
            }.getOrElse("")
            record(t, method.filename) {
                CallSiteRecord(
                  method.filename,
                  lineOf(t),
                  colOf(t),
                  method.fullName,
                  composer.declaredCase(cls),
                  member,
                  "ref",
                  "binding",
                  cls,
                  isBuiltinClass(cls),
                  purl = purlOf(cls)
                )
            }
          }
        end if
      }

  private def attributes(): Unit =
      atom.annotation.foreach { a =>
        val cls = clean(a.fullName)
        if externalClass(cls) then
          val file   = fileOf(a).getOrElse("")
          val method = enclosingMethodOf(a).map(_.fullName).getOrElse("")
          record(a, file) {
              CallSiteRecord(
                file,
                lineOf(a),
                colOf(a),
                method,
                composer.declaredCase(cls),
                "",
                "attribute",
                "binding",
                cls,
                isBuiltinClass(cls),
                purl = purlOf(cls)
              )
          }
      }

  /** The class names a declaration writes, which php2atom tags with their own positions: each
    * member of a parameter, return, property or class-constant type (`decltype`), and a class's
    * `extends`, `implements` and trait `use` names (all `extends`).
    */
  private def writtenNames(): Unit =
      atom.tag.name(PhpCallSiteSource.WrittenNameTag).foreach { tag =>
          parseWrittenName(tag.value).foreach { (cls, line, col, kind, member) =>
              if externalClass(cls) then
                tag._taggedByIn.foreach { holder =>
                  val node = holder.asInstanceOf[AstNode]
                  val file = holderFile(node)
                  val enclosing = node match
                    case m: Method => m.fullName
                    case other     => methodOf(other).map(_.fullName).getOrElse("")
                  if file.nonEmpty && line > 0 then
                    collector.add(node) {
                        CallSiteRecord(
                          file,
                          line,
                          col,
                          enclosing,
                          composer.declaredCase(cls),
                          member,
                          kind match
                            case "decltype" | "ref" | "new" => kind
                            case _                          => "extends"
                          ,
                          "binding",
                          cls,
                          isBuiltinClass(cls),
                          purl = purlOf(cls)
                        )
                    }
                  else collector.skip(node)
                  end if
                }
          }
      }

  private def holderFile(node: StoredNode): String = node match
    case m: Method      => m.filename
    case m: Member      => Try(m.typeDecl.filename).getOrElse("")
    case other: AstNode => fileOf(other).getOrElse("")
    case _              => ""

  private def parseWrittenName(
    value: String
  ): Option[(String, Integer, Integer, String, String)] =
      io.circe.parser.parse(value).toOption.flatMap { json =>
        val c = json.hcursor
        for
          t <- c.downField("type").as[String].toOption
          l <- c.downField("line").as[Int].toOption
          k <- c.downField("kind").as[String].toOption
        yield (
          clean(t),
          Integer.valueOf(l),
          Integer.valueOf(c.downField("col").as[Int].getOrElse(-1)),
          k,
          c.downField("member").as[String].getOrElse("")
        )
      }
end PhpCallSiteSource

object PhpCallSiteSource:
  /** The tag php2atom puts on a node for each class name a declaration writes. */
  val WrittenNameTag = "callsites-type"

  /** The tag php2atom puts on a call it synthesizes while lowering. */
  val SyntheticCallTag = "callsites-synthetic"

  /** The tag php2atom puts on an instance call whose method name is on another line. */
  val NamePositionTag = "callsites-name"

  val BuiltinsResource = "/callsites/php-builtins.txt"

  /** `self`, `static` and `parent` name the class they are written in; no record. */
  val SpecialNames: Set[String] = Set("self", "static", "parent")

  /** The language constructs php2atom lowers to calls named after their keyword. */
  val Constructs: Set[String] = Set(
    "echo",
    "print",
    "exit",
    "die",
    "isset",
    "unset",
    "empty",
    "eval",
    "include",
    "include_once",
    "require",
    "require_once",
    "list",
    "array",
    "declare",
    "encaps",
    "global",
    "clone",
    "__halt_compiler"
  )
end PhpCallSiteSource
