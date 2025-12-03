package io.appthreat.atom.slicing

import better.files.File
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{Languages, Operators, PropertyNames}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.codedumper.CodeDumper.dump
import overflowdb.PropertyKey

import java.util.concurrent.*
import java.util.regex.Pattern
import scala.collection.concurrent.TrieMap
import scala.util.Try

object UsageSlicing:

  private val resolver               = NoResolve
  private val exec: ExecutorService  = Executors.newVirtualThreadPerTaskExecutor()
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

    declarations
        .to(LazyList)
        .filterNot(_.name == "*")
        .filter(d =>
            !d.name.startsWith("_tmp_") && atLeastNCalls(
              d,
              config.minNumCalls,
              config.excludeOperatorCalls
            )
        )
        .map { decl =>
            exec.submit(() => computeUsageSlice(atom, decl, typeMap, config.excludeOperatorCalls))
        }
        .flatMap(f => Try(f.get(5, TimeUnit.SECONDS)).toOption.flatten)
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
    excludeOperatorCalls: Boolean
  ): Option[(Method, ObjectUsageSlice)] =
    val defNode = getDefNode(tgt)
    val (invokedCalls, argToCalls) =
        partitionInvolvementInCalls(atom, tgt, typeMap, excludeOperatorCalls)

    (tgt, defNode) match
      case (local: Local, Some(genCall: Call)) =>
          Some(local.method.head -> ObjectUsageSlice(
            targetObj = DefComponent.fromNode(local, genCall, typeMap),
            definedBy = Some(DefComponent.fromNode(genCall, null, typeMap)),
            invokedCalls = invokedCalls,
            argToCalls = argToCalls
          ))
      case (param: MethodParameterIn, _) if !param.name.matches("(this|self)") =>
          Some(param.method -> ObjectUsageSlice(
            targetObj = DefComponent.fromNode(param, null, typeMap),
            definedBy = Some(DefComponent.fromNode(param, null, typeMap)),
            invokedCalls = invokedCalls,
            argToCalls = argToCalls
          ))
      case (m: Method, _) =>
          createMethodObjectUsageSlice(m, invokedCalls, argToCalls, typeMap)
      case _ => None
  end computeUsageSlice

  private def createMethodObjectUsageSlice(
    m: Method,
    invokedCalls: List[ObservedCall],
    argToCalls: List[ObservedCallWithArgPos],
    typeMap: Map[String, String]
  ): Option[(Method, ObjectUsageSlice)] =
    val method  = if m.filename == "<empty>" && m.callIn.nonEmpty then m.callIn.head.method else m
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

    Some(method -> ObjectUsageSlice(
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
    excludeOperatorCalls: Boolean
  ): (List[ObservedCall], List[ObservedCallWithArgPos]) =
    val callsToCheck = getInCallsForReferencedIdentifiers(tgt, excludeOperatorCalls)
        .sortBy(f => (f.lineNumber, f.columnNumber))

    val (invoked, args) = callsToCheck.flatMap { c =>
        c.argument.find(_.code == tgt.name).map(arg => (c, arg))
    }.partition { case (_, arg) =>
        if arg.argumentName.isDefined then false
        else arg.argumentIndex != 0
    }

    val invokedObserved =
        invoked.map(_._1).isCall.flatMap(c => exprToObservedCall(atom, c, typeMap)).toList

    val argObserved = args.flatMap { case (c: Call, arg) =>
        val argPos = if arg.argumentName.isDefined then Left(arg.argumentName.get)
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
        baseCall.name == Operators.alloc || baseCall.ast.isCall.nameExact(Operators.alloc).nonEmpty

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
      if !language.contains(Languages.JSSRC) || baseCall.code.isEmpty || !baseCall.code.contains(
          "("
        )
      then
        resolvedMethod
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

  private def atLeastNCalls(decl: Declaration, n: Int, excludeOp: Boolean): Boolean =
      decl.label == "METHOD" || decl.name.contains("init") || getInCallsForReferencedIdentifiers(
        decl,
        excludeOp
      ).size >= n

  private def getInCallsForReferencedIdentifiers(
    decl: Declaration,
    excludeOp: Boolean
  ): List[Call] =
    val capturedVars =
        decl.capturedByMethodRef.referencedMethod.ast.isIdentifier.nameExact(decl.name)
    val refs = decl.flatMap {
        case local: Local             => local.referencingIdentifiers ++ capturedVars
        case param: MethodParameterIn => param.referencingIdentifiers ++ capturedVars
        case m: Method                => m.callIn.argument.isIdentifier
        case _                        => Seq()
    }

    refs.inCall.collect {
        case c
            if c.name.startsWith(Operators.assignment) && c.ast.isCall.name(Operators.alloc).nonEmpty =>
            c
        case c if !excludeOp || !c.name.startsWith("<operator") => c
    }.dedup.toList

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
      case _ =>
          (unusedTypeDeclAsSlices(atom), Nil)

    ProgramUsageSlice(slices ++ extraSlices, userDefTypes ++ extraTypes)

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

  private def importsAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.imports.l.map(i =>
          createSlice(
            i.importedEntity.getOrElse(""),
            i.importedAs.getOrElse(""),
            i.file.map(_.name).headOption.getOrElse(""),
            if i.code.nonEmpty then i.code.replaceFirst("^use", "").trim else "",
            Set.empty,
            i.lineNumber,
            i.columnNumber
          )
      )

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

  def routesAsUDT(atom: Cpg): List[UserDefinedType] =
      atom.call.where(_.argument.tag.nameExact(FRAMEWORK_ROUTE)).map(generateRouteUDT).filter(u =>
          u.fields.nonEmpty || u.procedures.nonEmpty
      ).l

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
                ObservedCall(
                  m.name,
                  Option(m.fullName),
                  m.parameter.map(_.typeFullName).toList,
                  m.methodReturn.typeFullName,
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
