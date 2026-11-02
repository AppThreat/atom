package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{CallSitesMetadata, DeclarationRefs, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import scala.collection.mutable
import scala.util.Try

/** The bytecode source of the callsites slice, for the atoms jimple2cpg builds from APKs, DEX
  * files, jars and class directories (Scala included, atom#327). A port of the measured prototype
  * (callsites-eval `harness/proto/jimple_callsites.scala`), finished to the oracle's rules.
  *
  * Every instruction of a method of an app class whose owner class is outside the application
  * boundary is a use (atom#326), each with the oracle's `kind` and `via`: static calls
  * `call`/`binding`; virtual, interface, super and direct calls `call`/`type`; a constructor `new`
  * /`binding` (the `<init>` call is the one record; the allocator is an operator nobody wrote);
  * field gets and puts `ref`/`binding`; `X.class`, `instanceof` and casts `ref`/`binding`; and a
  * JVM `invokedynamic`'s method-handle arguments (`String::length`, `Foo::new`) by their handle's
  * kind at the `invokedynamic` line. String concatenation through `invokedynamic` has no handle in
  * its bootstrap and gets no record, and its dummy owner `soot.dummy.InvokeDynamic` never names
  * one. Records only come from methods inside the boundary, and never for `line <= 0`: nobody wrote
  * those positions. Declaration types and annotations have no instruction in bytecode and are out
  * of scope here by decision (tranche 5 brief, section 2).
  *
  * Bytecode names the receiver's static type, so a call on `this` to a member the app class only
  * inherits is named on the app class: the owner is resolved up the class hierarchy to the first
  * ancestor that declares the member, or to the first ancestor outside the input, and that ancestor
  * is the record's `module` (AppManager's `AppCompatActivity.findViewById` through app activities).
  * Several candidate ancestors (interfaces with default methods) keep the first.
  *
  * The file is the class's real name (`<package path>/<SourceFile attribute>`, `Foo.kt` included),
  * never the temporary path the frontend unpacked the class to; when R8 rewrote the attribute to
  * the literal `SourceFile` the file is the outer class with a `.java` guess.
  *
  * The JDK, the Android platform and the compilers' support classes are dependencies (`builtin:
  * true`), decided from the committed JDK package list and the platform list generated from the
  * `android.jar` this build is made against. A record's `purl` is the Maven artifact of the bundled
  * `META-INF` metadata that claims the class's package, when exactly one does (atom#329, atom#321).
  */
final class JvmBytecodeCallSiteSource(atom: Cpg, appPrefixes: Option[Seq[String]] = None)
    extends BaseCallSiteSource(atom, "jvm-bytecode"):

  // bytecode positions carry a line and no column
  override protected def dedupeByMember: Boolean = true

  private val index: JvmAppBoundary.BoundaryData =
      JvmAppBoundary.boundaryOf(atom, JvmAppBoundary.inputOf(atom), appPrefixes)

  /** The application boundary (atom#326): `--app-prefix` replaces the rest, then what the input
    * declares as its own.
    */
  private val boundary: AppBoundary = index.boundary

  private val inputClassesByName: Set[String] = index.classFiles.map(_._1).toSet

  /** Whether `name` is an app class. A class directory makes every class of the input app code, and
    * only those: the oracle checks the class against the input, never a bare `appAll`.
    */
  private def isAppClass(name: String): Boolean =
      if boundary.appAll then inputClassesByName.contains(name) else boundary.isApp(name)

  private val classFiles: Map[String, String] = index.classFiles.toMap

  private val indySitesByClass: Map[String, List[JvmIndySite]] = index.indySites.toMap

  /** The boundary and the synthetic line tables the slice reports beside the records. */
  override def metadata: Option[CallSitesMetadata] = Some(
    CallSitesMetadata(
      appPrefixes = index.appPrefixes,
      appAll = index.appAll,
      boundarySource = index.boundarySource,
      syntheticLineTables = index.syntheticLineTables,
      methodsWithLineTables = index.methodsWithLineTables,
      lineQuality = index.lineQuality,
      sourceFileGuesses = index.sourceFileGuesses
    )
  )

  private def unresolved(cls: String): Boolean =
      cls.isEmpty || cls.startsWith("<unresolved") || cls.startsWith("<unknown") ||
          cls == "ANY" || cls == "void" || cls.contains("<unresolvedNamespace>")

  /** The class a type names, with array layers stripped; empty for a primitive, which no class
    * provides.
    */
  private def classOfType(typeName: String): String =
    var element = typeName
    var changed = true
    while changed do
      changed = false
      if element.endsWith("[]") then
        element = element.dropRight(2)
        changed = true
      else if element.startsWith("[") then
        element = element.substring(1)
        changed = true
      else if element.startsWith("L") && element.endsWith(";") && element.length > 2 then
        element = element.substring(1, element.length - 1)
        changed = true
    if Primitives.contains(element) then "" else element

  private val Primitives =
      Set("boolean", "byte", "char", "short", "int", "long", "float", "double", "void")

  /** The declaring class of a resolved method full name (`pkg.Type.method:sig()`), `$` kept. */
  private def classOfMethod(fullName: String): String =
    val member = fullName.takeWhile(_ != ':')
    member.lastIndexOf('.') match
      case -1 => ""
      case i  => member.substring(0, i)

  // --------------------------------------------------------------------------------- member index
  /** The members, parents and positions of every input class, indexed in one pass (a per-node graph
    * query would make the pass quadratic; AppManager has 30,717 classes).
    */
  private val declaredMethods: mutable.HashMap[String, Set[String]] = mutable.HashMap.empty
  private val declaredFields: mutable.HashMap[String, Set[String]]  = mutable.HashMap.empty
  private val parentsOf: mutable.HashMap[String, List[String]]      = mutable.HashMap.empty
  atom.typeDecl.filter(t => inputClassesByName.contains(t.fullName)).foreach { t =>
    declaredMethods.update(t.fullName, t.method.map(m => s"${m.name}:${m.signature}").toSet)
    declaredFields.update(t.fullName, t.member.name.toSet)
    parentsOf.update(
      t.fullName,
      t.inheritsFromTypeFullName.toList.filterNot(b => b.isEmpty || b == "ANY")
    )
  }

  /** The owner of an inherited member referenced through an app class: the first ancestor that
    * declares it, or the first ancestor outside the input, by the oracle's breadth-first walk.
    * `None` when an app class declares it (the app's own member, not a use). The second value is
    * the oracle's certainty: several candidate ancestors keep the first and drop it.
    */
  private def resolveOwner(
    owner: String,
    member: String,
    isField: Boolean
  ): Option[(String, Boolean)] =
      resolvedOwners.getOrElseUpdate((owner, member, isField), walkOwner(owner, member, isField))

  /** A method member as the index keys it, `name:signature`, so an overload the app class does not
    * declare is still inherited (the oracle matches the name and the descriptor).
    */
  private def methodKey(c: Call): String = s"${c.name}:${c.signature}"

  private val resolvedOwners =
      mutable.HashMap.empty[(String, String, Boolean), Option[(String, Boolean)]]

  private def walkOwner(
    owner: String,
    member: String,
    isField: Boolean
  ): Option[(String, Boolean)] =
    val decls = if isField then declaredFields else declaredMethods
    // a handle names its member without a descriptor: any overload of that name declares it
    def declares(members: Set[String]): Boolean =
        members.contains(member) || (!isField && !member.contains(':') &&
            members.exists(_.startsWith(member + ":")))
    val seen     = mutable.Set.empty[String]
    val queue    = mutable.Queue.empty[String]
    val frontier = mutable.ListBuffer.empty[String]
    queue.enqueue(owner)
    while queue.nonEmpty do
      val n = queue.dequeue()
      if seen.add(n) then
        decls.get(n) match
          case None =>
              // the first ancestor outside the input names the record: the oracle reads only the
              // input's classes and never walks the platform's or a library's own hierarchy
              frontier.addOne(n)
          case Some(declared) if declares(declared) =>
              // an input class declares the member: app means no use, library names the record
              return if isAppClass(n) then None else Some((n, true))
          case Some(_) =>
              // the superclass first, then the interfaces, as the frontend lists them
              parentsOf.get(n).foreach(_.foreach(queue.enqueue))
    val candidates = frontier.toList.filter(_ != "java.lang.Object")
    candidates match
      case single :: rest if rest.isEmpty => Some((single, true))
      case first :: _                     => Some((first, false))
      case Nil                            => Some(("java.lang.Object", true))
  end walkOwner

  // -------------------------------------------------------------------------------------- builtin
  /** The JDK's packages: the list the Java source adapter uses and every package of the JDK's
    * system modules, internal ones included (`sun.misc`, `jdk.internal.misc`). Both are generated.
    */
  private val jdkPackages: Set[String] =
      packageList("/callsites/jdk-packages.txt") ++ packageList(
        "/callsites/jdk-runtime-packages.txt"
      )

  /** The packages of the Android platform, generated from the SDK's `android.jar`. */
  private val androidPlatformPackages: Set[String] =
      packageList("/callsites/android-platform-packages.txt")

  private def packageList(resource: String): Set[String] =
      Try {
          val stream = getClass.getResourceAsStream(resource)
          try scala.io.Source.fromInputStream(stream, "UTF-8").mkString
          finally stream.close()
      }.getOrElse("").split("\n").map(_.trim).filter(l => l.nonEmpty && !l.startsWith("#")).toSet

  private val CompilerSupportExact = Set("kotlin.Metadata", "kotlin.jvm.JvmStatic")

  /** The compilers' support classes and the runtime packages that have no classes in the stub jar,
    * per the oracle's rules. The Android platform list itself is generated.
    */
  private val CompilerSupportPrefix =
      List("kotlin.jvm.internal.", "scala.runtime.", "j$.", "libcore.")

  /** d8's and R8's synthetic lambda classes, which desugar the app's own lambdas. */
  private val SyntheticOwnerMarkers = List("$$ExternalSynthetic", "$$Lambda$")

  /** The oracle's builtin label for a class: the JDK, the Android platform, the compilers' support
    * classes and the synthetic lambdas. The label decides `builtin`, and builtins get no `purl`.
    */
  private def builtinLabel(fqn: String): Option[String] =
      if SyntheticOwnerMarkers.exists(fqn.contains) then Some("synthetic")
      else if CompilerSupportExact.contains(fqn) then Some("compiler-support")
      else if CompilerSupportPrefix.exists(fqn.startsWith) then Some("compiler-support")
      else
        fqn.lastIndexOf('.') match
          case -1 => None
          case i =>
              val pkg = fqn.substring(0, i)
              if jdkPackages.contains(pkg) then Some("jdk")
              else if androidPlatformPackages.contains(pkg) then Some("android-platform")
              else None

  // ---------------------------------------------------------------------------------------- purl
  /** The `purl` of the bundled artifact that holds `cls` (atom#329, atom#321): the one cached
    * artifact whose published `.aar` or `.jar` carries the class, else the one artifact whose
    * `META-INF` metadata claims the class's longest package prefix. Builtins are nobody's artifact.
    */
  private def purlOf(cls: String): String = purls.getOrElseUpdate(cls, claimingPurl(cls))

  private val purls = mutable.HashMap.empty[String, String]

  private def claimingPurl(cls: String): String =
      if cls.isEmpty then ""
      else
        classArtifacts.get(cls)
            .orElse(
              index.artifacts
                  .filter((prefix, _) => cls == prefix || cls.startsWith(prefix + "."))
                  .maxByOption((prefix, _) => prefix.length)
                  .map(_._2)
            )
            .map { (group, artifact, version) =>
                if version.isEmpty then s"pkg:maven/$group/$artifact"
                else s"pkg:maven/$group/$artifact@$version"
            }
            .getOrElse("")

  private val classArtifacts: Map[String, (String, String, String)] =
      index.classArtifacts.toMap

  // --------------------------------------------------------------------------------------- files
  /** The class's real file name, `<package path>/<SourceFile>`; every class of the input has one
    * (R8's literal `SourceFile` becomes the outer class with a `.java` guess, counted in the
    * slice's metadata).
    */
  private def fileOfCaller(cls: String): String = classFiles.getOrElse(cls, cls)

  // ------------------------------------------------------------------------------------ calculate
  def calculate(): ProgramCallSites =
    calls()
    fieldAccesses()
    casts()
    classLiterals()
    collector.result()

  /** Casts and `instanceof` (`ref`/`binding`), with the checked type as the owner: the TYPE_REF the
    * frontend lowers each check to.
    */
  private def casts(): Unit =
      atom.call.nameExact("<operator>.cast", "<operator>.instanceOf").foreach { c =>
        val callerCls = classOfMethod(c.method.fullName)
        if isAppClass(callerCls) then
          c.argument.collectFirst { case t: TypeRef => t }.foreach { tref =>
            val owner = classOfType(tref.typeFullName)
            // a check against the app's own types is no use (the oracle skips it)
            if unresolved(owner) || owner.isEmpty || isAppClass(owner) then collector.skip(tref)
            else
              methodOf(tref) match
                case None => collector.skip(tref)
                case Some(method) =>
                    val (line, col) = at(tref)
                    if line <= 0 then collector.skip(tref)
                    else
                      val file = fileOfCaller(callerCls)
                      if file.isEmpty then collector.skip(tref)
                      else
                        collector.add(tref) {
                            CallSiteRecord(
                              file,
                              line,
                              col,
                              method.fullName,
                              owner,
                              "",
                              "ref",
                              "binding",
                              owner,
                              builtin = builtinLabel(owner).isDefined,
                              purl = purlOf(owner)
                            )
                        }
                    end if
            end if
          }
        end if
      }
  end casts

  /** The position of a use; nothing with a missing or impossible line becomes a record. */
  private def at(e: Expression): (Integer, Integer) =
      (e.lineNumber.getOrElse(-1), e.columnNumber.getOrElse(-1))

  /** Every non-operator call of an app method whose resolved owner is outside the boundary. The
    * `invokedynamic` calls are gathered and resolved in line order, so each one pairs with its own
    * bootstrap site when a method carries several with the same name.
    */
  private def calls(): Unit =
    // by (class, method name, method signature, line): the key an `invokedynamic` site carries
    val indyCalls =
        mutable.LinkedHashMap.empty[(String, String, String, Int), mutable.ListBuffer[Call]]
    atom.call.filterNot(_.name.startsWith("<operator>")).foreach { c =>
      val callerCls = classOfMethod(c.method.fullName)
      if isAppClass(callerCls) then
        val member = c.name
        val owner  = arrayElementOwner(c).getOrElse(classOfMethod(c.methodFullName))
        if owner == "soot.dummy.InvokeDynamic" ||
          // soot lowers a lambda's invokedynamic into a synthetic bootstrap class it
          // generates itself (`App$isEmpty__1.bootstrap$`): it is in no input, and its handle
          // site is the one the input's class file records
          (member == "bootstrap$" && !inputClassesByName.contains(owner))
        then
          val m = c.method
          indyCalls.getOrElseUpdate(
            (callerCls, m.name, m.signature, c.lineNumber.map(_.intValue).getOrElse(-1)),
            mutable.ListBuffer.empty
          ).addOne(c)
        else if unresolved(owner) then collector.skip(c)
        else
          val member = c.name
          val resolved =
              if isAppClass(owner) && member != "<init>" then
                resolveOwner(owner, methodKey(c), isField = false)
              else None
          resolved match
            case None =>
                if !isAppClass(owner) then recordCall(c, callerCls, owner, member)
            case Some((ancestor, _)) =>
                recordCall(c, callerCls, ancestor, member)
        end if
      end if
    }
    val sitesByKey = indySitesByClass.toList.flatMap { (cls, sites) =>
        sites.map(site => (cls, site.methodName, site.methodSignature, site.line) -> site)
    }.groupMap(_._1)(_._2)
    indyCalls.foreach { (key, found) =>
      // each site pairs with one call: the dummy owner's call by the interface method it names,
      // and Soot's `<Outer>$<member>__N.bootstrap$` by the handle member in its class name
      val sites = mutable.ListBuffer.from(sitesByKey.getOrElse(key, Nil))
      def take(matches: JvmIndySite => Boolean): Option[JvmIndySite] =
          sites.indexWhere(matches) match
            case -1 => None
            case i  => Some(sites.remove(i))
      found.foreach { c =>
        val site =
            if c.name == "bootstrap$" then
              val member = bootstrapMember(classOfMethod(c.methodFullName))
              take(_.handles.exists(_.name == member)).orElse(take(_ => true))
            else take(_.indyName == c.name)
        site match
          case Some(s) => handleInvokeDynamic(c, key._1, s)
          case None    => collector.skip(c)
      }
    }
  end calls

  /** The owner of a call on an array (`args.clone()`), which bytecode names by the array type and
    * Soot by `java.lang.Object`: the element class, as the oracle names it, or an empty owner (no
    * record) for an array of primitives.
    */
  private def arrayElementOwner(c: Call): Option[String] =
      // javac names the array type for `clone` only; every other call on an array is Object's
      if c.name != "clone" then None
      else
        c.argument.argumentIndex(0).collectFirst { case i: Identifier => i.typeFullName }
            .filter(_.endsWith("[]"))
            .map(classOfType)

  /** The handle member Soot puts in the name of the class it generates for a lambda's
    * `invokedynamic` (`me.app.App$length__1` for `String::length`; `init` for a constructor).
    */
  private def bootstrapMember(generated: String): String =
    val simple = generated.substring(generated.lastIndexOf('$') + 1)
    val member = simple.substring(0, math.max(0, simple.lastIndexOf("__")))
    if member == "init" then "<init>" else member

  private def recordCall(
    c: Call,
    callerCls: String,
    owner: String,
    member: String
  ): Unit =
      methodOf(c) match
        case None => collector.skip(c)
        case Some(method) =>
            val isInit = member == "<init>"
            // `super(...)` in a constructor runs the parent's constructor on `this`: no object is
            // allocated, so it is a reference to the parent, not a `new`
            val isNew = isInit && !isSuperConstructor(c, method)
            // a `new` sits on the line of its allocation, which a constructor call whose
            // arguments span lines leaves behind
            val (line, col) =
                if isNew then
                  allocationLine(c, method).map(l => (l, Integer.valueOf(-1))).getOrElse(at(c))
                else at(c)
            if line <= 0 then collector.skip(c)
            else
              val file = fileOfCaller(callerCls)
              if file.isEmpty then collector.skip(c)
              else
                val static = c.dispatchType == DispatchTypes.STATIC_DISPATCH
                collector.add(c) {
                    CallSiteRecord(
                      file,
                      line,
                      col,
                      method.fullName,
                      owner,
                      if isInit then "<init>" else member,
                      if isNew then "new" else if isInit then "ref" else "call",
                      if isInit || static then "binding" else "type",
                      c.methodFullName,
                      builtin = builtinLabel(owner).isDefined,
                      purl = purlOf(owner)
                    )
                }
            end if
  end recordCall

  /** A constructor call in a constructor on a receiver no `new` of that method allocated: the
    * `super(...)` call on `this`, whatever name the frontend gives the `this` local.
    */
  private def isSuperConstructor(c: Call, method: Method): Boolean =
      method.name == "<init>" && receiverOf(c).exists(r => !allocatedIn(method).contains(r))

  private def receiverOf(c: Call): Option[String] =
      c.argument.argumentIndex(0).collectFirst { case i: Identifier => i.name }

  /** The line of the allocation a constructor call initialises: the latest allocation into its
    * receiver at or before the call's line (Soot reuses its stack locals).
    */
  private def allocationLine(c: Call, method: Method): Option[Integer] =
    val callLine = c.lineNumber.map(_.intValue).getOrElse(Int.MaxValue)
    receiverOf(c).flatMap(r => allocatedIn(method).get(r))
        .flatMap(_.filter(_ <= callLine).maxOption)
        .map(Integer.valueOf)

  /** The lines of the allocations (`$r1 = new X`) of a method, by the local they assign. */
  private def allocatedIn(method: Method): Map[String, List[Int]] =
      allocated.getOrElseUpdate(
        method.fullName,
        method.call.nameExact("<operator>.assignment").flatMap { a =>
            for
              target <- a.argument.argumentIndex(1).isIdentifier.name.headOption
              alloc  <- a.argument.argumentIndex(2).isCall.nameExact("<operator>.alloc").headOption
              line   <- alloc.lineNumber.orElse(a.lineNumber)
            yield target -> line.intValue
        }.l.groupMap(_._1)(_._2)
      )

  private val allocated = mutable.HashMap.empty[String, Map[String, List[Int]]]

  /** The method handles a JVM `invokedynamic` bootstraps, by their handle's kind at the
    * `invokedynamic` line (atom#327). Concatenation has no handle in its bootstrap, and the dummy
    * owner is never recorded.
    */
  private def handleInvokeDynamic(c: Call, callerCls: String, site: JvmIndySite): Unit =
      methodOf(c) match
        case None => collector.skip(c)
        case Some(method) =>
            val (line, col) = at(c)
            if line <= 0 then collector.skip(c)
            else
              val file = fileOfCaller(callerCls)
              site.handles.foreach { h =>
                // a handle on an app class (a lambda body, `App::helper`) is the app's own code
                // unless the member is inherited from outside, as for a call
                val owner =
                    if !isAppClass(h.owner) then Some(h.owner)
                    else if h.kind == "new" then None
                    else resolveOwner(h.owner, h.name, isField = h.kind == "ref").map(_._1)
                owner match
                  case Some(o) if file.nonEmpty =>
                      collector.add(c) {
                          CallSiteRecord(
                            file,
                            line,
                            col,
                            method.fullName,
                            o,
                            h.name,
                            h.kind,
                            h.via,
                            s"${o}.${h.name}",
                            builtin = builtinLabel(o).isDefined,
                            purl = purlOf(o)
                          )
                      }
                  case _ => collector.skip(c)
              }
            end if
  end handleInvokeDynamic

  /** Field gets and puts (`ref`/`binding`), with the base's type as the owner; `this.f` on a member
    * the app class only inherits is resolved like a call.
    */
  private def fieldAccesses(): Unit =
      atom.call.nameExact("<operator>.fieldAccess").foreach { fa =>
        val callerCls = classOfMethod(fa.method.fullName)
        if isAppClass(callerCls) then
          // the base sits at argument 1 for a static field, and at a synthetic -1 for an instance
          // field's local; the field identifier is argument 2 either way
          val base   = fa.argument.collect { case e: Expression => e }.minByOption(_.argumentIndex)
          val member = fa.argument.collectFirst { case f: FieldIdentifier => f.canonicalName }
          base.foreach { b =>
              member.foreach { m =>
                val owner = classOfType(staticTypeOf(b))
                if unresolved(owner) || m.isEmpty then collector.skip(fa)
                else
                  val resolved =
                      if isAppClass(owner) then resolveOwner(owner, m, isField = true)
                      else None
                  resolved match
                    case None =>
                        if !isAppClass(owner) then recordField(fa, callerCls, owner, m)
                    case Some((ancestor, _)) =>
                        recordField(fa, callerCls, ancestor, m)
              }
          }
        end if
      }
  end fieldAccesses

  private def staticTypeOf(e: Expression): String = e match
    case i: Identifier => i.typeFullName
    case l: Literal    => l.typeFullName
    case c: Call       => c.typeFullName
    case _             => ""

  private def recordField(
    fa: Call,
    callerCls: String,
    owner: String,
    member: String
  ): Unit =
      methodOf(fa) match
        case None => collector.skip(fa)
        case Some(method) =>
            val (line, col) = at(fa)
            if line <= 0 then collector.skip(fa)
            else
              val file = fileOfCaller(callerCls)
              if file.isEmpty then collector.skip(fa)
              else
                collector.add(fa) {
                    CallSiteRecord(
                      file,
                      line,
                      col,
                      method.fullName,
                      owner,
                      member,
                      "ref",
                      "binding",
                      owner,
                      builtin = builtinLabel(owner).isDefined,
                      purl = purlOf(owner)
                    )
                }
            end if
  end recordField

  /** `X.class`, as a LITERAL the frontend lowers the class constant to, at the line of the
    * statement that carries it when the literal itself has none (an array initialiser's
    * `const-class` has no own line in Soot).
    */
  private def classLiterals(): Unit =
      atom.literal.code(".*\\.class").foreach { l =>
          methodOf(l) match
            case None => collector.skip(l)
            case Some(method) =>
                val callerCls = classOfMethod(method.fullName)
                if isAppClass(callerCls) then
                  val owner = classOfType(l.code.stripSuffix(".class"))
                  // a literal of the app's own types is no use (the oracle skips it)
                  if unresolved(owner) || owner.isEmpty || isAppClass(owner) then
                    collector.skip(
                      l
                    )
                  else
                    val (litLine, litCol) = at(l)
                    val (line, col, holder) =
                        if litLine.intValue > 0 then (litLine, litCol, l: AstNode)
                        else
                          // the statement's own line: the class constant is one instruction of it
                          DeclarationRefs.parentOf(l).flatMap { p =>
                            val pos = p match
                              case e: Expression => e.lineNumber
                              case r: Return     => r.lineNumber
                              case _             => None
                            pos.map(l2 =>
                                (l2, p.columnNumber.getOrElse(Integer.valueOf(-1)), p)
                            )
                          }.getOrElse((Integer.valueOf(-1), Integer.valueOf(-1), l: AstNode))
                    if line <= 0 then collector.skip(holder)
                    else
                      val file = fileOfCaller(callerCls)
                      if file.isEmpty then collector.skip(holder)
                      else
                        collector.add(holder) {
                            CallSiteRecord(
                              file,
                              line,
                              col,
                              method.fullName,
                              owner,
                              "",
                              "ref",
                              "binding",
                              owner,
                              builtin = builtinLabel(owner).isDefined,
                              purl = purlOf(owner)
                            )
                        }
                    end if
                  end if
                end if
      }
  end classLiterals
end JvmBytecodeCallSiteSource
