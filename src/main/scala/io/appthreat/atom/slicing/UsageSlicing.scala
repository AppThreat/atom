package io.appthreat.atom.slicing

import better.files.File
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{Languages, Operators, PropertyNames}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.codedumper.CodeDumper.dump
import overflowdb.PropertyKey

import java.util.regex.Pattern
import scala.collection.concurrent.TrieMap
import scala.util.Try

object UsageSlicing:

  private val resolver               = NoResolve
  private val logger                 = org.slf4j.LoggerFactory.getLogger(getClass)
  private val constructorTypeMatcher = Pattern.compile(".*new (\\w+)\\(.*")
  private val FRAMEWORK_ROUTE        = "framework-route"

  def calculateUsageSlice(atom: Cpg, config: UsagesConfig): ProgramSlice =
    val declarations = getDeclarations(atom, config)
    val typeMap      = atom.typeDecl.map(f => (f.name, f.fullName)).toMap
    val slices       = computeSlices(atom, declarations, typeMap, config)
    val language     = atom.metaData.language.headOption
    val userDefTypes = userDefinedTypes(atom)

    createProgramUsageSlice(atom, language, slices, userDefTypes, typeMap)

  private def getDeclarations(atom: Cpg, config: UsagesConfig): Traversal[Declaration] =
    val methods = config.fileFilter match
      case Some(fileName) => atom.file.nameExact(fileName).method
      case None           => atom.method

    methods
        .withMethodNameFilter(using config)
        .withMethodParameterFilter(using config)
        .withMethodAnnotationFilter(using config)
        .declaration

  private def computeSlices(
    atom: Cpg,
    declarations: Traversal[Declaration],
    typeMap: Map[String, String],
    config: UsagesConfig
  ): List[MethodUsageSlice] =
    val language = atom.metaData.language.headOption
    val root     = atom.metaData.root.headOption
    val declRefs = DeclarationRefs(atom)

    val filteredDecls = declarations
        .to(LazyList)
        .filterNot(_.name == "*")
        .filter(d =>
            !d.name.startsWith("_tmp_") && atLeastNCalls(
              d,
              config.minNumCalls,
              config.excludeOperatorCalls,
              declRefs
            )
        )

    // One slice at a time, as before (holding them all in flight needs far more heap), and none dropped: a slice
    // only visits its declaration's references, and skipping those slower than a timeout made the output depend on
    // the machine's load.
    val mainSlices = filteredDecls.flatMap { decl =>
        Try(computeUsageSlice(atom, decl, typeMap, config.excludeOperatorCalls, declRefs)).recover {
            case e =>
                logger.warn(s"Usage slice for ${decl.name} failed", e)
                Nil
        }.getOrElse(Nil)
    }

    val annotationSlices = filteredDecls.collect {
        case param: MethodParameterIn if !param.name.matches("(this|self)") => param
    }.flatMap(param => paramAnnotationSlices(param, typeMap))

    (mainSlices ++ annotationSlices)
        .groupBy { case (method, _) => method }
        .view
        .filterKeys(m => !isExcludedMethod(m))
        .toList
        .sortBy(_._1.fullName)
        .map { case (method, sliceList) =>
            createMethodUsageSlice(method, sliceList.map(_._2).toSet, language, root, config)
        }
  end computeSlices

  private def computeUsageSlice(
    atom: Cpg,
    tgt: Declaration,
    typeMap: Map[String, String],
    excludeOperatorCalls: Boolean,
    declRefs: DeclarationRefs
  ): List[(Method, ObjectUsageSlice)] =
    tgt match
      // External method stubs carry no body of their own; their usages are the call sites that
      // reach them. One entry per call, in the slice of the method containing the call and at the
      // call's own position. Deriving a single entry's position from an arbitrary `callIn` element
      // placed it in a random caller's file (atom#240).
      case m: Method if m.isExternal || m.filename == "<empty>" =>
          externalStubCallSiteSlices(m)
      case _ =>
          val defNode = getDefNode(tgt)
          val (invokedCalls, argToCalls) =
              partitionInvolvementInCalls(atom, tgt, typeMap, excludeOperatorCalls, declRefs)

          (tgt, defNode) match
            case (local: Local, Some(genCall: Call)) =>
                List(local.method.head -> ObjectUsageSlice(
                  targetObj = DefComponent.fromNode(local, genCall, typeMap),
                  definedBy = Some(DefComponent.fromNode(genCall, null, typeMap)),
                  invokedCalls = invokedCalls,
                  argToCalls = argToCalls
                ))
            case (param: MethodParameterIn, _) if !param.name.matches("(this|self)") =>
                List(param.method -> ObjectUsageSlice(
                  targetObj = DefComponent.fromNode(param, null, typeMap),
                  definedBy = Some(DefComponent.fromNode(param, null, typeMap)),
                  invokedCalls = invokedCalls,
                  argToCalls = argToCalls
                ))
            case (m: Method, _) =>
                createMethodObjectUsageSlice(m, invokedCalls, argToCalls, typeMap)
            case _ => Nil
    end match
  end computeUsageSlice

  /** Stubs of methods this project only calls, modelled as external METHOD nodes with no body:
    * library entry points (`debug:createDebug`, `@angular/core:signal`), operators and the
    * synthetic `require` stubs of the JS frontends.
    */
  private def externalStubCallSiteSlices(m: Method): List[(Method, ObjectUsageSlice)] =
      // Operator calls and `require` stubs carry no SBOM evidence; their per-call entries would
      // only flood the slices (an `<operator>.assignment` stub's callIn covers every assignment).
      if m.name.startsWith("<operator") || m.fullName.endsWith(":require")
      then Nil
      else
        m.callIn.l
            .sortBy { call =>
                (
                  call.method.filename,
                  call.lineNumber.map(_.intValue()).getOrElse(Int.MaxValue),
                  call.columnNumber.map(_.intValue()).getOrElse(Int.MaxValue)
                )
            }
            .map { call =>
              val defComp = CallDef(
                call.name,
                call.typeFullName,
                Option(m.fullName),
                Option(true),
                call.lineNumber.map(_.intValue()),
                call.columnNumber.map(_.intValue())
              )
              call.method -> ObjectUsageSlice(
                targetObj = defComp,
                definedBy = Some(defComp),
                invokedCalls = List.empty,
                argToCalls = List.empty
              )
            }
  end externalStubCallSiteSlices

  private def paramAnnotationSlices(
    param: MethodParameterIn,
    typeMap: Map[String, String]
  ): List[(Method, ObjectUsageSlice)] =
      param.annotation.map { ann =>
        val annDef = CallDef(
          param.name,
          ann.fullName,
          Option(ann.code).filter(_.nonEmpty).orElse(Option(ann.fullName)),
          Some(param.method.isExternal),
          ann.lineNumber.map(_.intValue()),
          ann.columnNumber.map(_.intValue()),
          label = ann.label
        )
        val annCall = ObservedCall(
          if ann.fullName.nonEmpty then ann.fullName else ann.name,
          Option(ann.code).filter(_.nonEmpty).orElse(Option(ann.fullName)),
          List.empty,
          "",
          Some(param.method.isExternal),
          ann.lineNumber.map(_.intValue()),
          ann.columnNumber.map(_.intValue())
        )
        param.method -> ObjectUsageSlice(
          targetObj = annDef,
          definedBy = Some(annDef),
          invokedCalls = List(annCall),
          argToCalls = List.empty
        )
      }.toList
  end paramAnnotationSlices

  private def createMethodObjectUsageSlice(
    m: Method,
    invokedCalls: List[ObservedCall],
    argToCalls: List[ObservedCallWithArgPos],
    typeMap: Map[String, String]
  ): List[(Method, ObjectUsageSlice)] =
    val defComp = DefComponent.fromNode(m, null, typeMap)

    val annotationCalls = m.annotation.map { a =>
        ObservedCall(
          if a.fullName.nonEmpty then a.fullName else a.name,
          Option(a.code).filter(_.nonEmpty).orElse(Option(a.fullName)),
          List.empty,
          "",
          Option(m.isExternal),
          a.lineNumber.map(_.intValue()),
          a.columnNumber.map(_.intValue())
        )
    }.toList

    List(m -> ObjectUsageSlice(
      targetObj = defComp,
      definedBy = Some(defComp),
      invokedCalls = invokedCalls ++ annotationCalls,
      argToCalls = argToCalls
    ))
  end createMethodObjectUsageSlice

  private def partitionInvolvementInCalls(
    atom: Cpg,
    tgt: Declaration,
    typeMap: Map[String, String],
    excludeOperatorCalls: Boolean,
    declRefs: DeclarationRefs
  ): (List[ObservedCall], List[ObservedCallWithArgPos]) =
    import DeclarationRefs.Role
    val involvements = involvementsOf(tgt, excludeOperatorCalls, declRefs)

    // A call the declaration is passed to (any argument but the receiver-like index 0) is an
    // invoked call; calls *on* it - index 0, a named argument, or the callee itself - are
    // argument calls. The callee case carries position -1.
    val (invoked, args) = involvements.map(i => (i.call, i.expr, i.role)).partition {
        case (_, _, Role.Receiver) => false
        case (_, arg, _) =>
            if arg.argumentName.isDefined then false
            else arg.argumentIndex != 0
    }

    val invokedObserved =
        invoked.map(_._1).flatMap(c => exprToObservedCall(atom, c, typeMap))

    val argObserved = args.flatMap { case (c: Call, arg, role) =>
        val argPos =
            if role == Role.Receiver then Right(-1)
            else if arg.argumentName.isDefined then Left(arg.argumentName.get)
            else Right(arg.argumentIndex)
        exprToObservedCall(atom, c, typeMap).map(oc =>
            ObservedCallWithArgPos.fromObservedCall(oc, argPos)
        )
    }

    (invokedObserved, argObserved)
  end partitionInvolvementInCalls

  private def exprToObservedCall(
    atom: Cpg,
    baseCall: Call,
    typeMap: Map[String, String]
  ): Option[ObservedCall] =
    val language = atom.metaData.language.headOption
    val isMember = baseCall.name == Operators.fieldAccess
    val isConstructor =
        baseCall.name == Operators.alloc || baseCall.name == "<operator>.new" ||
            baseCall.ast.isCall.nameExact(Operators.alloc).nonEmpty

    def getResolved(x: Call): Option[String] =
        Option(x.methodFullName).filterNot(n =>
            DefComponent.unresolvedCallPattern.matcher(n).matches()
        )

    val (callName, resolvedMethod) = if isMember then
      baseCall.argumentOut.collectFirst {
          case x: FieldIdentifier => Option(x.code) -> None
          case x: Call            => Option(x.name) -> getResolved(x)
      }.getOrElse((None, None))
    else if isConstructor then
      val m = constructorTypeMatcher.matcher(baseCall.code)
      val typeName =
          if m.find() then m.group(1) else baseCall.code.stripPrefix("new ").takeWhile(_ != '(')
      Option(typeName) -> typeMap.get(typeName)
    else
      Option(baseCall.name) -> getResolved(baseCall)

    callName.map { name =>
      val params        = getCallParameters(baseCall, isMember, isConstructor)
      val returnType    = getReturnType(atom, baseCall, isConstructor, resolvedMethod)
      val finalResolved = handleJavaScriptLogic(baseCall, resolvedMethod, language)

      ObservedCall(
        name,
        finalResolved,
        params,
        returnType,
        baseCall.callee(using resolver).isExternal.headOption,
        baseCall.lineNumber.map(_.intValue()),
        baseCall.columnNumber.map(_.intValue())
      )
    }
  end exprToObservedCall

  private def getCallParameters(
    baseCall: Call,
    isMember: Boolean,
    isConstructor: Boolean
  ): List[String] =
    val args = if isMember then baseCall.inCall.argument
    else if isConstructor then
      baseCall.ast.isCall.nameExact("<operator>.new").lastOption.map(_.argument).getOrElse(
        Iterator.empty
      )
    else baseCall.argument

    args.collect { case n: Expression if n.argumentIndex > 0 => n }
        .flatMap {
            case _: MethodRef => Some("LAMBDA")
            case x => Option(x.property(
                  PropertyNames.TYPE_FULL_NAME,
                  x.property(PropertyNames.DYNAMIC_TYPE_HINT_FULL_NAME, Seq("ANY")).headOption
                ))
        }
        .collect { case x: String => x }
        .toList
  end getCallParameters

  private def getReturnType(
    atom: Cpg,
    baseCall: Call,
    isConstructor: Boolean,
    resolvedMethod: Option[String]
  ): String =
      if isConstructor then resolvedMethod.getOrElse("ANY")
      else
        baseCall.argumentOut.collectFirst {
            case x: Call
                if !DefComponent.unresolvedCallPattern.matcher(x.methodFullName).matches() =>
                atom.method.fullNameExact(x.methodFullName).methodReturn.typeFullName.headOption
            case x: Call =>
                x.callee(using resolver).methodReturn.typeFullName.headOption
        }.flatten.getOrElse("ANY")

  private def handleJavaScriptLogic(
    baseCall: Call,
    resolvedMethod: Option[String],
    language: Option[String]
  ): Option[String] =
    val isJs = language.contains(Languages.JSSRC) || language.contains(Languages.JAVASCRIPT)
    if !isJs || baseCall.code.isEmpty || !baseCall.code.contains("(") then
      resolvedMethod
    else
      val taggedArg =
          baseCall.argument.filter(_.tag.nameExact(FRAMEWORK_ROUTE).nonEmpty).isLiteral.headOption
      if taggedArg.isDefined then
        Option(taggedArg.get.code)
      else
        var code = baseCall.code.takeWhile(_ != '(')
        if code.contains(" ") then code = code.split(" ").last

        if code.startsWith("app.use") && baseCall.argument.isLiteral.exists(_.code != "*") then
          code = baseCall.argument.isLiteral.filterNot(_.code == "*").head.code
        else if code.startsWith("route") || code.startsWith("app") then
          code = baseCall.code.replaceAll("\n", "\\n").replaceAll(" {4}", " {2}").replaceAll(
            " {2}",
            "\\t"
          )
        Option(code)
    end if
  end handleJavaScriptLogic

  private def getDefNode(tgt: Declaration): Option[AstNode] = tgt match
    case local: Local =>
        local.referencingIdentifiers.inCall.astParent.assignment
            .where(_.argument(1).codeExact(tgt.name))
            .argument(2).headOption match
          case Some(b: Block) =>
              b.ast.isCall.or(_.nameExact("<operator>.new"), _.name(".*__init__.*")).lastOption
          case x => x
    case x: AstNode => Some(x)
    case _          => None

  private def atLeastNCalls(
    decl: Declaration,
    n: Int,
    excludeOp: Boolean,
    declRefs: DeclarationRefs
  ): Boolean =
      decl.label == "METHOD" || decl.name.contains("init") ||
          involvementsOf(decl, excludeOp, declRefs).size >= n

  /** The calls a declaration takes part in, one per call, ordered by position.
    *
    * Its references are resolved through closure captures (see [[DeclarationRefs]]). Each reference
    * counts where it is an argument (as before), and also where it roots a member chain
    * (`path.sep`, `process.stdout`) that is passed to a call or is a call's callee, and where it is
    * itself the callee (`createDebug(...)`, an alias `f()`). In the JS CPG a callee is the call's
    * receiver, not one of its arguments, so argument edges alone never see it.
    */
  private def involvementsOf(
    decl: Declaration,
    excludeOp: Boolean,
    declRefs: DeclarationRefs
  ): List[DeclarationRefs.Involvement] =
    import DeclarationRefs.{Involvement, Role, argumentOf, chainTop, receiverOf}
    val refs: List[Identifier] = decl match
      // A method's own name passed to one of its calls; anything else passed to it is a use of
      // that argument's declaration, not of the method.
      case m: Method => m.callIn.argument.isIdentifier.nameExact(m.name).l
      case d         => declRefs.referencesOf(d)

    val direct = refs.flatMap { id =>
        id.inCall.collect {
            case c
                if c.name.startsWith(Operators.assignment) &&
                    c.ast.isCall.name(Operators.alloc).nonEmpty =>
                Involvement(c, id, Role.Argument)
            case c if !excludeOp || DeclarationRefs.isInvocation(c) =>
                Involvement(c, id, Role.Argument)
        }
    }
    val viaChains = if decl.isInstanceOf[Method] then Nil
    else
      refs.flatMap { id =>
        val top = chainTop(id)
        receiverOf(top).map(c => Involvement(c, top, Role.Receiver))
            .orElse(if top ne id then argumentOf(top).map(c => Involvement(c, top, Role.Argument))
            else None)
      }

    // One involvement per call: an argument role wins over the callee role (`x.f()` passes `x`
    // as argument 0 and also roots the callee `x.f`), and the lowest argument index wins.
    (direct ++ viaChains)
        .groupBy(_.call.id())
        .values
        .map(_.minBy(i =>
            (if i.role == Role.Argument then 0 else 1, i.expr.argumentIndex, i.expr.id())
        ))
        .toList
        .sortBy(i =>
            (
              i.call.lineNumber.map(_.intValue()).getOrElse(Int.MaxValue),
              i.call.columnNumber.map(_.intValue()).getOrElse(Int.MaxValue),
              i.call.id()
            )
        )
  end involvementsOf

  private def createProgramUsageSlice(
    atom: Cpg,
    language: Option[String],
    slices: List[MethodUsageSlice],
    userDefTypes: List[UserDefinedType],
    typeMap: Map[String, String]
  ): ProgramUsageSlice =
    val (extraSlices, extraTypes) = language match
      case Some(Languages.NEWC | Languages.C | Languages.PHP) =>
          (importsAsSlices(atom), routesAsUDT(atom))
      case Some(Languages.PYTHON | Languages.PYTHONSRC) =>
          (externalCalleesAsSlices(atom), routesAsUDT(atom))
      case Some(Languages.RUBYSRC) =>
          (danglingRouteCallsAsSlices(atom) ++ httpEndpointsAsSlices(atom), routesAsUDT(atom))
      case Some(lang) if lang == Languages.JSSRC || lang == Languages.JAVASCRIPT =>
          (unusedTypeDeclAsSlices(atom), routesAsUDT(atom))
      case _ =>
          (unusedTypeDeclAsSlices(atom), Nil)

    ProgramUsageSlice(slices ++ extraSlices, userDefTypes ++ extraTypes, sourceIntegrityOf(atom))
  end createProgramUsageSlice

  /** The source-integrity tags of the atom as findings, one per file, line, kind and name. */
  private def sourceIntegrityOf(atom: Cpg): List[SourceIntegrityFinding] =
    val confusable = io.appthreat.x2cpg.Defines.UnicodeConfusableTag
    val bidi       = io.appthreat.x2cpg.Defines.UnicodeBidiControlTag
    val hidden     = io.appthreat.x2cpg.Defines.UnicodeHiddenCodeTag
    def fileOf(n: StoredNode): String = n match
      case m: Method            => m.filename
      case t: TypeDecl          => t.filename
      case mb: Member           => mb.typeDecl.filename
      case e: Expression        => Option(e.method).map(_.filename).getOrElse("")
      case l: Local             => l.method.filename.headOption.getOrElse("")
      case p: MethodParameterIn => p.method.filename
      case _                    => ""
    def lineOf(n: StoredNode): Option[Int] =
        Option(n.propertiesMap.get(PropertyNames.LINE_NUMBER)).collect { case i: Integer =>
            i.toInt
        }
    def nameOf(n: StoredNode): String =
        Option(n.propertiesMap.get(PropertyNames.NAME)).orElse(
          Option(n.propertiesMap.get(PropertyNames.CODE))
        ).collect { case s: String => s }.getOrElse("")
    val findings = atom.tag.nameExact(confusable, bidi, hidden).l.flatMap { t =>
        t._taggedByIn.collectAll[StoredNode].l.map { n =>
            (t.name, n) match
              // a comment's controls are on its method, valued `<line>:<code points>`
              case (`bidi`, m: Method) =>
                  val (line, points) = t.value.span(_ != ':')
                  SourceIntegrityFinding(
                    bidi,
                    m.filename,
                    line.toIntOption,
                    "comment",
                    points.drop(1)
                  )
              // Java escapes that end a line, comment or literal are on their method, valued
              // `<line>:<escapes>`
              case (`hidden`, m: Method) =>
                  val (line, escapes) = t.value.span(_ != ':')
                  SourceIntegrityFinding(
                    hidden,
                    m.filename,
                    line.toIntOption,
                    m.name,
                    escapes.drop(1)
                  )
              case (`bidi`, other) =>
                  SourceIntegrityFinding(bidi, fileOf(other), lineOf(other), nameOf(other), t.value)
              case (_, other) =>
                  SourceIntegrityFinding(
                    confusable,
                    fileOf(other),
                    lineOf(other),
                    nameOf(other),
                    t.value
                  )
        }
    }
    findings.distinct.sortBy(f => (f.fileName, f.lineNumber.getOrElse(0), f.kind, f.name))
  end sourceIntegrityOf

  private def createMethodUsageSlice(
    method: Method,
    slices: Set[ObjectUsageSlice],
    language: Option[String],
    root: Option[String],
    config: UsagesConfig
  ): MethodUsageSlice =
    val code = if config.excludeMethodSource || !File(method.filename).exists then ""
    else
      Try(dump(method.location, language, root, highlight = false, withArrow = false)).getOrElse("")

    createSlice(
      method.fullName,
      method.signature,
      method.filename,
      code,
      slices,
      method.lineNumber,
      method.columnNumber
    )
  end createMethodUsageSlice

  /** A C/C++ include's IMPORT node carries the file it resolved to and whether it is a system
    * include as tags (written by the C/C++ frontend).
    */
  private val IncludeResolvedPathTag = "include-resolved-path"
  private val IncludeSystemTag       = "include-system"

  /** On a C/C++ call to a function only a header declares: that header. */
  private val CalleeDeclaredInTag = "callee-declared-in"

  private def importsAsSlices(atom: Cpg): List[MethodUsageSlice] =
    // the functions each file calls, by the header that declares them
    val calleesByFileAndHeader: Map[(String, String), List[String]] =
        atom.call.where(_.tag.nameExact(CalleeDeclaredInTag)).l
            .flatMap(call =>
                call.tag.nameExact(CalleeDeclaredInTag).value.headOption.map(header =>
                    ((call.method.filename, header), call.name)
                )
            )
            .groupMap(_._1)(_._2).view.mapValues(_.distinct.sorted).toMap
    // Deduplicate by (importedEntity, importedAs, resolved file): in multi-TU languages (e.g.
    // C/C++) the same header can be included once per translation unit, flooding the output with
    // hundreds of identical import slices. A single representative entry per unique import
    // identity is kept; the same name resolved to two different files is two imports.
    atom.imports
        .map(i => (i, i.tag.nameExact(IncludeResolvedPathTag).value.headOption))
        .distinctBy((i, resolved) =>
            (i.importedEntity.getOrElse(""), i.importedAs.getOrElse(""), resolved.getOrElse(""))
        )
        .l
        .map((i, resolved) =>
            createSlice(
              i.importedEntity.getOrElse(""),
              i.importedAs.getOrElse(""),
              i.file.map(_.name).headOption.getOrElse(""),
              if i.code.nonEmpty then i.code.replaceFirst("^use", "").trim else "",
              Set.empty,
              i.lineNumber,
              i.columnNumber
            ).copy(
              resolvedPath = resolved,
              isSystem = Option.when(i.tag.nameExact(IncludeSystemTag).nonEmpty)(true),
              importedSymbols = resolved.flatMap(header =>
                  calleesByFileAndHeader.get((i.file.map(_.name).headOption.getOrElse(""), header))
              )
            )
        )
  end importsAsSlices

  private def unusedTypeDeclAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.typeDecl.annotation.filter(_.method.isEmpty).l.map(a =>
          createSlice(
            a.fullName,
            s"@${a.name}",
            a.file.map(_.name).headOption.getOrElse(""),
            a.code.replaceAll("\\s*", ""),
            Set.empty,
            a.lineNumber,
            a.columnNumber
          )
      )

  private def externalCalleesAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.call.where(_.callee(using resolver).isExternal).filterNot(_.name.startsWith("<operator"))
          .l.map { call =>
            val (taobj, ocall) = createGenericCallDefs(call)
            createSlice(
              call.method.fullName,
              call.method.signature,
              call.method.filename,
              "",
              Set(ObjectUsageSlice(taobj, Option(taobj), ocall, Nil)),
              call.method.lineNumber,
              call.method.columnNumber
            )
          }

  private def httpEndpointsAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.call.where(_.argument.tag.name("http-endpoint")).l.map { call =>
        val firstLit = call.argument.isLiteral.headOption.map(_.code).getOrElse("")
        val (taobj, ocall) = createGenericCallDefs(
          call,
          targetName = firstLit,
          targetType = "HttpEndpoint",
          paramTypes = call.argument.typ.fullName.toList
        )
        createSlice(
          call.method.fullName,
          call.method.signature,
          call.method.filename,
          "",
          Set(ObjectUsageSlice(taobj, Option(taobj), ocall, Nil)),
          call.method.lineNumber,
          call.method.columnNumber
        )
      }

  private def danglingRouteCallsAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.call.where(_.method.filename(".*routes.*")).code(
        ".*(resources|scope|namespace|get|post|patch|delete|match|options).*"
      ).l.map { call =>
        val (taobj, ocall) =
            createGenericCallDefs(call, targetName = call.method.code, isExternal = false)
        createSlice(
          call.method.fullName,
          call.method.signature,
          call.method.filename,
          "",
          Set(ObjectUsageSlice(taobj, Option(taobj), ocall, Nil)),
          call.method.lineNumber,
          call.method.columnNumber
        )
      }

  private def createGenericCallDefs(
    call: Call,
    targetName: String = "",
    targetType: String = "",
    paramTypes: List[String] = Nil,
    isExternal: Boolean = true
  ): (CallDef, List[ObservedCall]) =
    val resolved = call.callee(using resolver).method.headOption.map(_.fullName)
    val taobj = CallDef(
      if targetName.isEmpty && call.callee(using resolver).method.nonEmpty then
        call.callee(using resolver).method.head.name
      else targetName,
      targetType,
      resolved,
      Option(isExternal),
      call.method.lineNumber.map(_.intValue()),
      call.method.columnNumber.map(_.intValue())
    )
    val ocall = List(ObservedCall(
      call.name,
      resolved.orElse(Option(call.name)),
      paramTypes,
      "",
      Option(true),
      call.lineNumber.map(_.intValue()),
      call.columnNumber.map(_.intValue())
    ))
    (taobj, ocall)
  end createGenericCallDefs

  private def createSlice(
    fullName: String,
    signature: String,
    fileName: String,
    code: String,
    slices: Set[ObjectUsageSlice],
    line: Option[Integer],
    col: Option[Integer]
  ): MethodUsageSlice =
      MethodUsageSlice(
        code,
        fullName,
        signature,
        fileName,
        slices,
        line.map(_.intValue()),
        col.map(_.intValue())
      )

  private def routesAsUDT(atom: Cpg): List[UserDefinedType] =
    // Collected once: the call-based branch below and the tagged-literal id filter walk the
    // same `argument.tag` traversal, which is not cheap on a large graph.
    val routeCalls = atom.call.where(_.argument.tag.nameExact(FRAMEWORK_ROUTE)).l
    val callRoutes = routeCalls.map(generateRouteUDT)
    // Route paths tagged outside a call argument - a React Router `<Route path="/x" .../>`
    // attribute whose literal sits under a TEMPLATE_DOM node - never reach the call-based
    // branch above. Emit them as literal-only route records, minus the ones already covered.
    // The UDT and its single field are both named after the unquoted path so consumers see one
    // consistent shape.
    val callTaggedLiteralIds = routeCalls.flatMap(_.argument.isLiteral).map(_.id()).toSet
    val templateRoutes = atom.literal
        .where(_.tag.nameExact(FRAMEWORK_ROUTE))
        .filterNot(lit => callTaggedLiteralIds.contains(lit.id()))
        .map { lit =>
          val unquoted = lit.code.stripPrefix("\"").stripSuffix("\"")
          UserDefinedType(
            unquoted,
            List(LocalDef(
              unquoted,
              lit.typeFullName,
              lit.lineNumber.map(_.toInt),
              lit.columnNumber.map(_.toInt)
            )),
            Nil,
            lit.location.filename,
            lit.lineNumber.map(_.intValue()),
            lit.columnNumber.map(_.intValue())
          )
        }
        .l
    (callRoutes ++ templateRoutes).filter(u => u.fields.nonEmpty || u.procedures.nonEmpty)
  end routesAsUDT

  private def generateRouteUDT(call: Call): UserDefinedType =
    val locals = call.argument.isLiteral.map(m =>
        LocalDef(m.code, m.typeFullName, m.lineNumber.map(_.toInt), m.columnNumber.map(_.toInt))
    ).l
    val procedures =
        call.callee(using resolver).method.filterNot(_.name.startsWith("<clinit>")).map(m =>
            ObservedCall(
              m.name,
              Option(m.fullName),
              m.parameter.map(_.typeFullName).toList,
              m.methodReturn.typeFullName,
              Option(m.isExternal),
              m.lineNumber.map(_.intValue()),
              m.columnNumber.map(_.intValue())
            )
        ).l ++
            call.argument.inCall.map(c =>
                ObservedCall(
                  c.code.takeWhile(_ != '('),
                  Option(c.code),
                  c.argument.map(_.code.replaceAll("\"", "")).toList,
                  "",
                  Option(true),
                  c.lineNumber.map(_.intValue()),
                  c.columnNumber.map(_.intValue())
                )
            ).l
    UserDefinedType(
      call.name,
      locals,
      procedures,
      call.location.filename,
      call.lineNumber.map(_.intValue()),
      call.columnNumber.map(_.intValue())
    )
  end generateRouteUDT

  def userDefinedTypes(atom: Cpg): List[UserDefinedType] =
      atom.typeDecl.filterNot(t =>
          t.isExternal || t.name.matches(
            "(:program|<module>|<init>|<meta>|<body>|<global>|<clinit>)"
          )
      ).map(t =>
          UserDefinedType(
            t.fullName,
            t.member.map(m => DefComponent.fromNode(m, null)).collect { case x: LocalDef => x }.l,
            t.method.filterNot(_.name.startsWith("<clinit>")).map(m =>
              // For constructors the method return type in the CPG is `void` (or ANY) but the
              // meaningful type is the enclosing class.  Use the TypeDecl fullName instead so
              // that `returnType` carries a useful type name rather than `void`.
              val isConstructor = m.name == t.name
              val returnType =
                  if isConstructor then t.fullName
                  else m.methodReturn.typeFullName
              ObservedCall(
                m.name,
                Option(m.fullName),
                m.parameter.map(_.typeFullName).toList,
                returnType,
                Option(m.isExternal),
                m.lineNumber.map(_.intValue()),
                m.columnNumber.map(_.intValue())
              )
            ).l,
            t.filename,
            t.lineNumber.map(_.intValue()),
            t.columnNumber.map(_.intValue())
          )
      ).filter(u => u.fields.nonEmpty || u.procedures.nonEmpty).l

  private def isExcludedMethod(m: Method): Boolean =
      m.fullName.startsWith("<operator") || m.fullName.startsWith("__builtin") || m.name.startsWith(
        "<global>"
      ) || m.name.startsWith("<clinit>")

  implicit class MethodDataSourceExt(trav: Iterator[Method]):
    def declaration: Iterator[Declaration] = trav.ast.collect { case d: Declaration => d }

end UsageSlicing
