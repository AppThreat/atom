package io.appthreat.atom.slicing

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{Languages, Operators, PropertyNames}
import io.shiftleft.semanticcpg.language.*
import overflowdb.PropertyKey

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import scala.annotation.unused
import scala.collection.concurrent.TrieMap
import scala.util.Try

/** A utility for slicing based off of usage references for identifiers and parameters.
  */
object UsageSlicing:

  private val resolver = NoResolve
  val exec: ExecutorService =
      Executors.newVirtualThreadPerTaskExecutor()
  private val constructorTypeMatcher = Pattern.compile(".*new (\\w+)\\(.*")
  private val excludeOperatorCalls   = new AtomicBoolean(true)
  private val FRAMEWORK_ROUTE        = "framework-route"

  /** Generates object slices from the given CPG.
    *
    * @param atom
    *   the atom to slice.
    * @return
    *   a set of object slices.
    */
  def calculateUsageSlice(atom: Cpg, config: UsagesConfig): ProgramSlice =
    excludeOperatorCalls.set(config.excludeOperatorCalls)

    val declarations = getDeclarations(atom, config)
    val typeMap      = TrieMap.from(atom.typeDecl.map(f => (f.name, f.fullName)).toMap)
    val slices       = usageSlices(atom, declarations, typeMap)(using config)
    val language     = atom.metaData.language.headOption
    val userDefTypes = userDefinedTypes(atom)

    createProgramUsageSlice(atom, language, slices, userDefTypes, typeMap, config)

  private def getDeclarations(atom: Cpg, config: UsagesConfig): Traversal[Declaration] =
    val methods = config.fileFilter match
      case Some(fileName) => atom.file.nameExact(fileName).method
      case None           => atom.method

    methods
        .withMethodNameFilter(using config)
        .withMethodParameterFilter(using config)
        .withMethodAnnotationFilter(using config)
        .declaration

  private def createProgramUsageSlice(
    atom: Cpg,
    language: Option[String],
    slices: List[MethodUsageSlice],
    userDefTypes: List[UserDefinedType],
    typeMap: TrieMap[String, String],
    config: UsagesConfig
  ): ProgramUsageSlice =
      language match
        case Some(Languages.NEWC | Languages.C | Languages.PHP) =>
            ProgramUsageSlice(slices ++ importsAsSlices(atom), userDefTypes ++ routesAsUDT(atom))
        case Some(Languages.PYTHON | Languages.PYTHONSRC) =>
            ProgramUsageSlice(
              slices ++ externalCalleesAsSlices(atom),
              userDefTypes ++ routesAsUDT(atom)
            )
        case Some(Languages.RUBYSRC) =>
            ProgramUsageSlice(
              slices ++ danglingRouteCallsAsSlices(atom, typeMap) ++ httpEndpointsAsSlices(
                atom,
                typeMap
              ),
              userDefTypes ++ routesAsUDT(atom)
            )
        case _ =>
            ProgramUsageSlice(slices ++ unusedTypeDeclAsSlices(atom), userDefTypes)

  import io.shiftleft.semanticcpg.codedumper.CodeDumper.dump

  private def usageSlices(
    atom: Cpg,
    getDeclIdentifiers: Traversal[Declaration],
    typeMap: TrieMap[String, String]
  )(implicit config: UsagesConfig): List[MethodUsageSlice] =
    val language = atom.metaData.language.headOption
    val root     = atom.metaData.root.headOption

    getDeclIdentifiers
        .to(LazyList)
        .filterNot(_.name.equals("*"))
        .filter(a => !a.name.startsWith("_tmp_") && atLeastNCalls(a, config.minNumCalls))
        .map(a => exec.submit(new TrackUsageTask(atom, a, typeMap)(using config)))
        .flatMap(TimedGet)
        .groupBy { case (scope, _) => scope }
        .view
        .filterNot((m, _) =>
            m.fullName.startsWith("<operator") || m.fullName.startsWith(
              "__builtin"
            ) || m.name.startsWith(
              "<global>"
            ) || m.name.startsWith("<clinit>")
        )
        .sortBy(_._1.fullName)
        .map { case (method, sliceList) =>
            val slices = sliceList.map(_._2).toSet
            createMethodUsageSlice(method, slices, language, root, config)
        }
        .toList
  end usageSlices

  private def createMethodUsageSlice(
    method: Method,
    slices: Set[ObjectUsageSlice],
    language: Option[String],
    root: Option[String],
    config: UsagesConfig
  ): MethodUsageSlice =
      MethodUsageSlice(
        code = getSourceCode(method, config, language, root),
        fullName = method.fullName,
        signature = method.signature,
        fileName = method.filename,
        slices = slices,
        lineNumber = method.lineNumber.map(_.intValue()),
        columnNumber = method.columnNumber.map(_.intValue())
      )

  private def getSourceCode(
    method: Method,
    config: UsagesConfig,
    language: Option[String],
    root: Option[String]
  ): String =
      if config.excludeMethodSource || !better.files.File(method.filename).exists then
        ""
      else
        Try(dump(
          method.location,
          language,
          root,
          highlight = false,
          withArrow = false
        )).getOrElse("")

  private def cleanupImportCode(code: String): String =
      if code.startsWith("use") then code else code.replaceAll("\\s*", "")

  private def importLineNumber(im: Import): Option[Int] =
      if im.file.nonEmpty && im.file.head.lineNumber.nonEmpty then
        im.file.head.lineNumber.map(_.intValue())
      else if im.lineNumber.nonEmpty then im.lineNumber.map(_.intValue())
      else None

  private def importColumnNumber(im: Import): Option[Int] =
      if im.file.nonEmpty && im.file.head.columnNumber.nonEmpty then
        im.file.head.columnNumber.map(_.intValue())
      else if im.columnNumber.nonEmpty then im.columnNumber.map(_.intValue())
      else None

  private def importsAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.imports.l.map(createImportSlice)

  private def createImportSlice(im: Import): MethodUsageSlice =
      MethodUsageSlice(
        code = if im.code.nonEmpty then cleanupImportCode(im.code) else "",
        fullName = im.importedEntity.get,
        signature = im.importedAs.getOrElse(""),
        fileName = if im.file.nonEmpty then im.file.head.name else "",
        slices = Set.empty[ObjectUsageSlice],
        lineNumber = importLineNumber(im),
        columnNumber = importColumnNumber(im)
      )

  private def unusedTypeDeclAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.typeDecl.annotation.filter(_.method.isEmpty).l.map(createUnusedTypeDeclSlice)

  private def createUnusedTypeDeclSlice(annot: Annotation): MethodUsageSlice =
      MethodUsageSlice(
        code = if annot.code.nonEmpty then annot.code.replaceAll("\\s*", "") else "",
        fullName = annot.fullName,
        signature = s"@${annot.name}",
        fileName = if annot.file.nonEmpty then annot.file.head.name else "",
        slices = Set.empty[ObjectUsageSlice],
        lineNumber = annot.lineNumber.map(_.intValue()),
        columnNumber = annot.columnNumber.map(_.intValue())
      )

  private def externalCalleesAsSlices(atom: Cpg): List[MethodUsageSlice] =
      atom.call
          .where(_.callee(using NoResolve).isExternal)
          .filterNot(_.name.startsWith("<operator"))
          .l
          .map(createExternalCalleeSlice)

  private def createExternalCalleeSlice(call: Call): MethodUsageSlice =
    val taobj = CallDef(
      if call.callee(using NoResolve).method.nonEmpty then
        call.callee(using NoResolve).method.head.name
      else "",
      "",
      if call.callee(using NoResolve).method.nonEmpty then
        Option(call.callee(using NoResolve).method.head.fullName)
      else Option(""),
      Option(call.callee(using NoResolve).head.isExternal),
      call.callee(using NoResolve).head.method.lineNumber.map(_.intValue()),
      call.callee(using NoResolve).head.method.columnNumber.map(_.intValue())
    )
    val ocall = List(
      ObservedCall(
        callName = call.name,
        resolvedMethod =
            if call.callee(using NoResolve).method.nonEmpty then
              Option(call.callee(using NoResolve).method.head.fullName)
            else Option(""),
        paramTypes = List.empty[String],
        returnType = "",
        isExternal = Option(true),
        lineNumber = call.lineNumber.map(_.intValue()),
        columnNumber = call.columnNumber.map(_.intValue())
      )
    )
    MethodUsageSlice(
      code = "",
      fullName = call.method.fullName,
      signature = call.method.signature,
      fileName = call.method.filename,
      slices = Set(
        ObjectUsageSlice(
          targetObj = taobj,
          definedBy = Option(taobj),
          invokedCalls = ocall,
          argToCalls = List.empty[ObservedCallWithArgPos]
        )
      ),
      lineNumber = call.method.lineNumber.map(_.intValue()),
      columnNumber = call.method.columnNumber.map(_.intValue())
    )
  end createExternalCalleeSlice

  private def httpEndpointsAsSlices(
    atom: Cpg,
    typeMap: TrieMap[String, String]
  ): List[MethodUsageSlice] =
      atom.call.where(_.argument.tag.name("http-endpoint")).l.map(createHttpEndpointSlice)

  private def createHttpEndpointSlice(call: Call): MethodUsageSlice =
    val firstLiteral = call.argument.isLiteral.head
    val taobj = CallDef(
      firstLiteral.code,
      "HttpEndpoint",
      if call.callee(using NoResolve).method.nonEmpty then
        Option(call.callee(using NoResolve).method.head.fullName)
      else Option(call.name),
      Option(true),
      call.method.lineNumber.map(_.intValue()),
      call.method.columnNumber.map(_.intValue())
    )
    val ocall = List(
      ObservedCall(
        callName = call.name,
        resolvedMethod =
            if call.callee(using NoResolve).method.nonEmpty then
              Option(call.callee(using NoResolve).method.head.fullName)
            else Option(call.name),
        paramTypes = call.argument.typ.fullName.toList,
        returnType = "",
        isExternal = Option(true),
        lineNumber = call.lineNumber.map(_.intValue()),
        columnNumber = call.columnNumber.map(_.intValue())
      )
    )
    MethodUsageSlice(
      code = "",
      fullName = call.method.fullName,
      signature = call.method.signature,
      fileName = call.method.filename,
      slices = Set(
        ObjectUsageSlice(
          targetObj = taobj,
          definedBy = Option(taobj),
          invokedCalls = ocall,
          argToCalls = List.empty[ObservedCallWithArgPos]
        )
      ),
      lineNumber = call.method.lineNumber.map(_.intValue()),
      columnNumber = call.method.columnNumber.map(_.intValue())
    )
  end createHttpEndpointSlice

  private def danglingRouteCallsAsSlices(
    atom: Cpg,
    typeMap: TrieMap[String, String]
  ): List[MethodUsageSlice] =
      atom.call.where(_.method.filename(".*routes.*")).code(
        ".*(resources|scope|namespace|get|post|patch|delete|match|options).*"
      )
          .l
          .map(createDanglingRouteSlice)

  private def createDanglingRouteSlice(call: Call): MethodUsageSlice =
    val taobj = CallDef(
      call.method.code,
      "",
      if call.callee(using NoResolve).method.nonEmpty then
        Option(call.callee(using NoResolve).method.head.fullName)
      else Option(""),
      Option(false),
      call.method.lineNumber.map(_.intValue()),
      call.method.columnNumber.map(_.intValue())
    )
    val ocall = List(
      ObservedCall(
        callName = call.name,
        resolvedMethod =
            if call.callee(using NoResolve).method.nonEmpty then
              Option(call.callee(using NoResolve).method.head.fullName)
            else Option(""),
        paramTypes = List.empty[String],
        returnType = "",
        isExternal = Option(true),
        lineNumber = call.lineNumber.map(_.intValue()),
        columnNumber = call.columnNumber.map(_.intValue())
      )
    )
    MethodUsageSlice(
      code = "",
      fullName = call.method.fullName,
      signature = call.method.signature,
      fileName = call.method.filename,
      slices = Set(
        ObjectUsageSlice(
          targetObj = taobj,
          definedBy = Option(taobj),
          invokedCalls = ocall,
          argToCalls = List.empty[ObservedCallWithArgPos]
        )
      ),
      lineNumber = call.method.lineNumber.map(_.intValue()),
      columnNumber = call.method.columnNumber.map(_.intValue())
    )
  end createDanglingRouteSlice

  /** Discovers internally defined routes.
    *
    * @param atom
    *   the CPG to query for types.
    * @return
    *   a list of user defined types.
    */
  def routesAsUDT(atom: Cpg): List[UserDefinedType] =
      atom.call
          .where(_.argument.tag.nameExact(FRAMEWORK_ROUTE))
          .map(generateRouteUDT)
          .filter(udt => udt.fields.nonEmpty || udt.procedures.nonEmpty)
          .l

  private def generateRouteUDT(call: Call): UserDefinedType =
      UserDefinedType(
        call.name,
        call.argument.isLiteral
            .map(m =>
                LocalDef(
                  name = m.code,
                  typeFullName = m.typeFullName,
                  lineNumber = Option(
                    m.property(new PropertyKey[Integer](PropertyNames.LINE_NUMBER))
                  ).map(_.toInt),
                  columnNumber = Option(
                    m.property(new PropertyKey[Integer](PropertyNames.COLUMN_NUMBER))
                  ).map(_.toInt)
                )
            )
            .collectAll[LocalDef]
            .l,
        call
            .callee(using NoResolve)
            .method
            .filterNot(m => m.name.startsWith("<clinit>"))
            .map(m =>
                ObservedCall(
                  m.name,
                  Option(m.fullName),
                  m.parameter.map(_.typeFullName).toList,
                  m.methodReturn.typeFullName,
                  Option(m.isExternal),
                  m.lineNumber.map(_.intValue()),
                  m.columnNumber.map(_.intValue())
                )
            )
            .l ++ call.argument.inCall.map(c =>
            ObservedCall(
              c.code.takeWhile(_ != '('),
              Option(c.code),
              c.argument.map(_.code.replaceAll("\"", "")).toList,
              "",
              Option(true),
              c.lineNumber.map(_.intValue()),
              c.columnNumber.map(_.intValue())
            )
        ).l,
        call.location.filename,
        call.lineNumber.map(_.intValue()),
        call.columnNumber.map(_.intValue())
      )

  private def TimedGet(dsf: Future[Option[(Method, ObjectUsageSlice)]]) =
      try
        dsf.get(5, TimeUnit.SECONDS)
      catch
        case _: Throwable => None

  /** Returns true if the given declaration is found to have at least n non-operator calls within
    * its referenced identifiers' scope.
    *
    * @param decl
    *   the declaration to check.
    * @param n
    *   number of calls.
    * @return
    *   true if the call count condition is satisfied.
    */
  private def atLeastNCalls(decl: Declaration, n: Int): Boolean =
      decl.label == "METHOD" || decl.name.contains("init") || getInCallsForReferencedIdentifiers(
        decl
      ).size >= n

  private def getInCallsForReferencedIdentifiers(decl: Declaration): List[Call] =
    // Cross closure boundaries
    val capturedVars =
        decl.capturedByMethodRef.referencedMethod.ast.isIdentifier.nameExact(decl.name)
    decl
        .flatMap {
            case local: Local             => local.referencingIdentifiers ++ capturedVars
            case param: MethodParameterIn => param.referencingIdentifiers ++ capturedVars
            case m: Method                => m.callIn.argument.isIdentifier
            case _                        => Seq()
        }
        .inCall
        .flatMap {
            case c
                if c.name.startsWith(Operators.assignment) && c.ast.isCall.name(
                  Operators.alloc
                ).nonEmpty => Some(c)
            case c if excludeOperatorCalls.get() && c.name.startsWith("<operator") => None
            case c                                                                 => Some(c)
        }
        .dedup
        .toList
  end getInCallsForReferencedIdentifiers

  /** Discovers internally defined types.
    *
    * @param atom
    *   the CPG to query for types.
    * @return
    *   a list of user defined types.
    */
  def userDefinedTypes(atom: Cpg): List[UserDefinedType] =
      atom.typeDecl
          .filterNot(t =>
              t.isExternal || t.name.matches(
                "(:program|<module>|<init>|<meta>|<body>|<global>|<clinit>)"
              )
          )
          .map(generateTypeUDT)
          .filter(udt => udt.fields.nonEmpty || udt.procedures.nonEmpty)
          .l

  private def generateTypeUDT(typeDecl: TypeDecl): UserDefinedType =
      UserDefinedType(
        typeDecl.fullName,
        typeDecl.member.map(m => DefComponent.fromNode(m, null)).collectAll[LocalDef].l,
        typeDecl.method
            .filterNot(m => m.name.startsWith("<clinit>"))
            .map(m =>
                ObservedCall(
                  m.name,
                  Option(m.fullName),
                  m.parameter.map(_.typeFullName).toList,
                  m.methodReturn.typeFullName,
                  Option(m.isExternal),
                  m.lineNumber.map(_.intValue()),
                  m.columnNumber.map(_.intValue())
                )
            )
            .l,
        typeDecl.filename,
        typeDecl.lineNumber.map(_.intValue()),
        typeDecl.columnNumber.map(_.intValue())
      )

  private class TrackUsageTask(atom: Cpg, tgt: Declaration, typeMap: TrieMap[String, String])(
    implicit config: UsagesConfig
  ) extends Callable[Option[(Method, ObjectUsageSlice)]]:

    override def call(): Option[(Method, ObjectUsageSlice)] =
      val defNode         = getDefNode(tgt)
      val partitionResult = partitionInvolvementInCalls

      createObjectUsageSlice(tgt, defNode, partitionResult) match
        case Some((method, slice)) => Some((method, slice))
        case None                  => None

    private def getDefNode(tgt: Declaration): Option[AstNode] =
        tgt match
          case local: Local =>
              local.referencingIdentifiers.inCall.astParent.assignment
                  .where(_.argument(1).code(tgt.name))
                  .argument(2)
                  .headOption match
                // In the case of a constructor, we should get the "new" call
                case Some(block: Block) =>
                    block.ast.isCall.or(
                      _.nameExact("<operator>.new"),
                      _.name(".*__init__.*")
                    ).lastOption
                case x => x
          case x: AstNode => Some(x)
          case _          => None

    private def createObjectUsageSlice(
      tgt: Declaration,
      defNode: Option[AstNode],
      partitionResult: (List[ObservedCall], List[ObservedCallWithArgPos])
    ): Option[(Method, ObjectUsageSlice)] =
      val (invokedCalls, argToCalls) = partitionResult

      (tgt, defNode, partitionResult) match
        // Case 1: Generated by variable assignment
        case (local: Local, Some(genCall: Call), _) =>
            Some(
              (
                local.method.head,
                ObjectUsageSlice(
                  targetObj = createDefComponent(local, genCall),
                  definedBy = Option(createDefComponent(genCall, null)),
                  invokedCalls = invokedCalls,
                  argToCalls = argToCalls
                )
              )
            )
        // Case 2: Generated by incoming parameter
        case (param: MethodParameterIn, _, _)
            if !param.name.matches("(this|self)") =>
            Some(
              (
                param.method,
                ObjectUsageSlice(
                  targetObj = createDefComponent(param, null),
                  definedBy = Option(createDefComponent(param, null)),
                  invokedCalls = invokedCalls,
                  argToCalls = argToCalls
                )
              )
            )
        case (m: Method, _, _) =>
            createMethodObjectUsageSlice(m, invokedCalls, argToCalls)
        case _ =>
            None
      end match
    end createObjectUsageSlice

    private def createMethodObjectUsageSlice(
      m: Method,
      invokedCalls: List[ObservedCall],
      argToCalls: List[ObservedCallWithArgPos]
    ): Option[(Method, ObjectUsageSlice)] =
      var method  = m
      val defComp = createDefComponent(m, null)
      if method.filename == "<empty>" && defComp.label == "CALL" && method.callIn.nonEmpty then
        method = method.callIn.head.method
      val annotationCalls = m.annotation
          .map(a =>
              ObservedCall(
                if a.fullName.nonEmpty then a.fullName else a.name,
                if a.code.nonEmpty then Option(a.code) else Option(a.fullName),
                List.empty,
                "",
                Option(m.isExternal),
                a.lineNumber.map(_.intValue()),
                a.columnNumber.map(_.intValue())
              )
          )
          .toList
      Some(
        method,
        ObjectUsageSlice(
          targetObj = defComp,
          definedBy = Option(defComp),
          invokedCalls = invokedCalls ++ annotationCalls,
          argToCalls = argToCalls
        )
      )
    end createMethodObjectUsageSlice

    private def partitionInvolvementInCalls: (List[ObservedCall], List[ObservedCallWithArgPos]) =
      val (invokedCalls, argToCalls) = getInCallsForReferencedIdentifiers(tgt)
          .sortBy(f => (f.lineNumber, f.columnNumber))
          .flatMap(c => c.argument.find(p => p.code == tgt.name).map(f => (c, f)).headOption)
          .map { case (c, arg) =>
              if arg.argumentName.isDefined then (c, arg, Left(arg.argumentName.get))
              else (c, arg, Right(arg.argumentIndex))
          }
          .partition { case (_, _, argIdx) =>
              argIdx match
                case Left(_)       => false // receivers/bases are never named
                case Right(argIdx) => argIdx == 0
          }
      (
        invokedCalls.map(_._1).isCall.flatMap(exprToObservedCall).toList,
        argToCalls
            .flatMap { case (c: Call, _, argAt: Either[String, Int]) =>
                exprToObservedCall(c).map(oc =>
                    ObservedCallWithArgPos.fromObservedCall(oc, argAt)
                )
            }
      )
    end partitionInvolvementInCalls

    /** Will attempt to get the API call from the expression if this is a procedure call.
      *
      * @param baseCall
      *   the expression to extract the API call from.
      * @return
      *   an API call if present.
      */
    private def exprToObservedCall(baseCall: Call): Option[ObservedCall] =
      val language           = atom.metaData.language.headOption
      val isMemberInvocation = baseCall.name.equals(Operators.fieldAccess)
      val isConstructor =
          baseCall.name.equals(Operators.alloc) || baseCall.ast.isCall.nameExact(
            Operators.alloc
          ).nonEmpty

      def getResolvedMethod(x: Call): Option[String] =
          if DefComponent.unresolvedCallPattern.matcher(x.methodFullName).matches() then None
          else Option(x.methodFullName)

      // Handle the case where a call is an invocation of a field member (lambda) or function/method call
      val (callName, resolvedMethod) =
          getCallInfo(baseCall, isMemberInvocation, isConstructor, getResolvedMethod)

      if callName.isEmpty then return None

      val params     = getCallParameters(baseCall, isMemberInvocation, isConstructor)
      val returnType = getReturnType(baseCall, isConstructor, resolvedMethod)

      // Handle JavaScript specific logic
      val finalResolvedMethod = handleJavaScriptLogic(baseCall, resolvedMethod, language)

      Option(
        ObservedCall(
          callName.get,
          finalResolvedMethod,
          params,
          returnType,
          baseCall.callee(using resolver).isExternal.headOption,
          baseCall.lineNumber.map(_.intValue()),
          baseCall.columnNumber.map(_.intValue())
        )
      )
    end exprToObservedCall

    private def getCallInfo(
      baseCall: Call,
      isMemberInvocation: Boolean,
      isConstructor: Boolean,
      getResolvedMethod: Call => Option[String]
    ): (Option[String], Option[String]) =
        if isMemberInvocation then
          baseCall.argumentOut
              .flatMap {
                  case x: FieldIdentifier =>
                      Option(Option(x.code) -> None)
                  case x: Call => Option(Option(x.name) -> getResolvedMethod(x))
                  case _       => None
              }
              .headOption
              .getOrElse((None, None))
        else if isConstructor then
          val m = constructorTypeMatcher.matcher(baseCall.code)
          val typeName =
              if m.find() then m.group(1)
              else baseCall.code.stripPrefix("new ").takeWhile(!_.equals('('))
          Option(typeName) -> typeMap.get(typeName)
        else Option(baseCall.name) -> getResolvedMethod(baseCall)

    private def getCallParameters(
      baseCall: Call,
      isMemberInvocation: Boolean,
      isConstructor: Boolean
    ): List[String] =
      val paramSource =
          if isMemberInvocation then baseCall.inCall.argument
          else if isConstructor then
            baseCall.ast.isCall
                .nameExact("<operator>.new")
                .lastOption
                .map(_.argument)
                .getOrElse(Iterator.empty)
          else baseCall.argument

      paramSource
          .collect { case n: Expression if n.argumentIndex > 0 => n }
          .flatMap {
              case _: MethodRef => Option("LAMBDA")
              case x =>
                  Option(
                    x.property(
                      PropertyNames.TYPE_FULL_NAME,
                      x.property(PropertyNames.DYNAMIC_TYPE_HINT_FULL_NAME, Seq("ANY")).headOption
                    )
                  )
          }
          .collect { case x: String => x }
          .toList
    end getCallParameters

    private def getReturnType(
      baseCall: Call,
      isConstructor: Boolean,
      resolvedMethod: Option[String]
    ): String =
        if isConstructor then
          resolvedMethod match
            case Some(methodFullName) => methodFullName
            case None                 => "ANY"
        else
          baseCall.argumentOut
              .flatMap {
                  case x: Call
                      if !DefComponent.unresolvedCallPattern.matcher(
                        x.methodFullName
                      ).matches() =>
                      atom.method.fullNameExact(
                        x.methodFullName
                      ).methodReturn.typeFullName.headOption
                  case x: Call =>
                      x.callee(using resolver).methodReturn.typeFullName.headOption
                  case _ => None
              }
              .headOption
              .getOrElse("ANY")

    private def handleJavaScriptLogic(
      baseCall: Call,
      resolvedMethod: Option[String],
      language: Option[String]
    ): Option[String] =
      var finalResolvedMethod = resolvedMethod

      if baseCall.code.nonEmpty && baseCall.code.contains(
          "("
        ) && language.contains(Languages.JSSRC)
      then
        var baseCallCode = baseCall.code.takeWhile(_ != '(')
        if baseCallCode.contains(" ") then
          baseCallCode = baseCallCode.split(" ").last

        // Retain the full code for route detection purposes
        if language.contains(Languages.JSSRC) then
          if baseCallCode.startsWith("app.use") then
            if baseCall.argument.nonEmpty && baseCall.argument.isLiteral.nonEmpty then
              baseCallCode = baseCall.argument.isLiteral.filterNot(_.code == "*").head.code
          else if baseCallCode.startsWith("route") || baseCallCode.startsWith("app") then
            baseCallCode = baseCall.code
                .replaceAll("\n", "\\n").replaceAll(
                  " {4}",
                  " {2}"
                ).replaceAll(" {2}", "\\t")

        finalResolvedMethod = Option(baseCallCode)
      end if

      finalResolvedMethod
    end handleJavaScriptLogic

    /** Creates a def component with the workaround of correcting the type full name if it is only a
      * type name.
      */
    private def createDefComponent(node: AstNode, definedCallNode: AstNode) =
        DefComponent.fromNode(node, definedCallNode, typeMap.toMap)

    // TODO: Slicing may run before post-processing so we cannot assume a call graph
    //  implement this in a next step to combine slices
    @unused
    private def linkSlices(slices: Map[String, Set[ObjectUsageSlice]]): Unit =
        slices.foreach { case (_, usageSlices) =>
            usageSlices.foreach { slice =>
              slice.definedBy match
                case Some(CallDef(_, _, Some(resolvedMethod), _, _, _, _)) =>
                    slices.get(resolvedMethod) match
                      case Some(_) => // TODO: Handle match
                      case None    => // No match
                case _ =>
              slice.argToCalls
                  .flatMap {
                      case ObservedCallWithArgPos(
                            _,
                            Some(resolvedMethod),
                            _,
                            _,
                            Left(argName),
                            _,
                            _,
                            _
                          ) =>
                          slices.get(resolvedMethod).flatMap { calleeSlices =>
                              calleeSlices.find { s =>
                                  s.targetObj match
                                    case p: ParamDef => p.name == argName
                                    case _           => false
                              }
                          }
                      case ObservedCallWithArgPos(
                            _,
                            Some(resolvedMethod),
                            _,
                            _,
                            Right(argIdx),
                            _,
                            _,
                            _
                          ) =>
                          slices.get(resolvedMethod).flatMap { calleeSlices =>
                              calleeSlices.find { s =>
                                  s.targetObj match
                                    case p: ParamDef => p.position == argIdx
                                    case _           => false
                              }
                          }
                      case _ => None
                  }
                  .foreach { s =>
                      // todo: Handle slice linking
                  }
            }
        }
  end TrackUsageTask

  /** Adds extensions to extract all assignments from method bodies.
    */
  implicit class MethodDataSourceExt(trav: Iterator[Method]):
    def declaration: Iterator[Declaration] = trav.ast.collectAll[Declaration]
end UsageSlicing
