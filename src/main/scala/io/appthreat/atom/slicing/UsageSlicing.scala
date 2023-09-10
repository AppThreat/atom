package io.appthreat.atom.slicing

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{Languages, Operators, PropertyNames}
import io.shiftleft.semanticcpg.language.*

import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern
import scala.annotation.unused
import scala.collection.concurrent.TrieMap
import scala.util.Try

import java.util.concurrent.*

/** A utility for slicing based off of usage references for identifiers and parameters. This is mainly tested around
  * JavaScript CPGs.
  */
object UsageSlicing {

  private val resolver               = NoResolve
  val exec: ExecutorService          = Executors.newWorkStealingPool(Runtime.getRuntime.availableProcessors() / 2)
  private val constructorTypeMatcher = Pattern.compile(".*new (\\w+)\\(.*")
  private val excludeOperatorCalls   = new AtomicBoolean(true)

  /** Generates object slices from the given CPG.
    *
    * @param cpg
    *   the CPG to slice.
    * @return
    *   a set of object slices.
    */
  def calculateUsageSlice(cpg: Cpg, config: UsagesConfig): ProgramSlice = {
    implicit val implicitConfig: UsagesConfig = config
    excludeOperatorCalls.set(config.excludeOperatorCalls)

    def getDeclarations: Traversal[Declaration] = (config.fileFilter match {
      case Some(fileName) => cpg.file.nameExact(fileName).method
      case None           => cpg.method
    }).withMethodNameFilter.withMethodParameterFilter.withMethodAnnotationFilter.declaration

    def typeMap      = TrieMap.from(cpg.typeDecl.map(f => (f.name, f.fullName)).toMap)
    val slices       = usageSlices(cpg, () => getDeclarations, typeMap)
    val language     = cpg.metaData.language.headOption
    val userDefTypes = userDefinedTypes(cpg)
    if (language.get == Languages.NEWC || language.get == Languages.C)
      ProgramUsageSlice(slices ++ importsAsSlices(cpg), userDefTypes)
    else
      ProgramUsageSlice(slices, userDefTypes)
  }

  import io.shiftleft.semanticcpg.codedumper.CodeDumper.dump

  private def usageSlices(cpg: Cpg, getDeclIdentifiers: () => Traversal[Declaration], typeMap: TrieMap[String, String])(
    implicit config: UsagesConfig
  ): List[MethodUsageSlice] = {
    val language = cpg.metaData.language.headOption
    val root     = cpg.metaData.root.headOption
    getDeclIdentifiers()
      .to(LazyList)
      .filterNot(a => a.name.equals("*"))
      .filter(a => !a.name.startsWith("_tmp_") && atLeastNCalls(a, config.minNumCalls))
      .map(a => exec.submit(new TrackUsageTask(cpg, a, typeMap)))
      .flatMap(TimedGet)
      .groupBy { case (scope, _) => scope }
      .view
      .filterNot((m, _) =>
        (m.fullName.startsWith("<operator") || m.fullName.startsWith("__builtin") || m.name.startsWith(
          "<global>"
        ) || m.name.startsWith("<clinit>"))
      )
      .sortBy(_._1.fullName)
      .map { case (method, slices) =>
        MethodUsageSlice(
          code =
            if (config.excludeMethodSource || !better.files.File(method.filename).exists) ""
            else Try(dump(method.location, language, root, highlight = false, withArrow = false)).getOrElse(""),
          fullName = method.fullName,
          signature = method.signature,
          fileName = method.filename,
          slices = slices.iterator.map(_._2).toSet,
          lineNumber = method.lineNumber.map(_.intValue()),
          columnNumber = method.columnNumber.map(_.intValue())
        )
      }
      .toList
  }

  private def importsAsSlices(cpg: Cpg): List[MethodUsageSlice] = {
    cpg.imports.l.map(im => {
      MethodUsageSlice(
        code = im.code,
        fullName = im.importedEntity.get,
        signature = im.importedAs.get,
        fileName = if (im.file.nonEmpty) im.file.head.name else "",
        slices = Seq[ObjectUsageSlice]().toSet,
        lineNumber = if (im.file.nonEmpty) im.file.head.lineNumber.map(_.intValue()) else None,
        columnNumber = if (im.file.nonEmpty) im.file.head.columnNumber.map(_.intValue()) else None
      )
    })
  }

  private def TimedGet(dsf: Future[Option[(Method, ObjectUsageSlice)]]) = {
    try {
      dsf.get(5, TimeUnit.SECONDS)
    } catch {
      case _: Throwable => None
    }
  }

  /** Returns true if the given declaration is found to have at least n non-operator calls within its referenced
    * identifiers' scope.
    *
    * @param decl
    *   the declaration to check.
    * @param n
    *   number of calls.
    * @return
    *   true if the call count condition is satisfied.
    */
  private def atLeastNCalls(decl: Declaration, n: Int): Boolean =
    decl.label == "METHOD" || decl.name.contains("init") || getInCallsForReferencedIdentifiers(decl).size >= n

  private def getInCallsForReferencedIdentifiers(decl: Declaration): List[Call] = {
    // Cross closure boundaries
    val capturedVars = decl.capturedByMethodRef.referencedMethod.ast.isIdentifier.nameExact(decl.name)
    decl
      .flatMap {
        case local: Local             => local.referencingIdentifiers ++ capturedVars
        case param: MethodParameterIn => param.referencingIdentifiers ++ capturedVars
        case m: Method                => m.callIn.argument.isIdentifier
        case _                        => Seq()
      }
      .inCall
      .flatMap {
        case c if c.name.startsWith(Operators.assignment) && c.ast.isCall.name(Operators.alloc).nonEmpty => Some(c)
        case c if excludeOperatorCalls.get() && c.name.startsWith("<operator")                           => None
        case c                                                                                           => Some(c)
      }
      .dedup
      .toList
  }

  /** Discovers internally defined types.
    *
    * @param cpg
    *   the CPG to query for types.
    * @return
    *   a list of user defined types.
    */
  def userDefinedTypes(cpg: Cpg): List[UserDefinedType] = {

    def generateUDT(typeDecl: TypeDecl): UserDefinedType = {
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
    }

    cpg.typeDecl
      .filterNot(t => t.isExternal || t.name.matches("(:program|<module>|<init>|<meta>|<body>|<global>|<clinit>)"))
      .map(generateUDT)
      .filter(udt => udt.fields.nonEmpty || udt.procedures.nonEmpty)
      .l
  }

  private class TrackUsageTask(cpg: Cpg, tgt: Declaration, typeMap: TrieMap[String, String])(implicit
    config: UsagesConfig
  ) extends Callable[Option[(Method, ObjectUsageSlice)]] {

    override def call(): Option[(Method, ObjectUsageSlice)] = {
      val defNode = tgt match {
        case local: Local =>
          local.referencingIdentifiers.inCall.astParent.assignment
            .where(_.argument(1).code(tgt.name))
            .argument(2)
            .headOption match {
            // In the case of a constructor, we should get the "new" call
            case Some(block: Block) =>
              block.ast.isCall.or(_.nameExact("<operator>.new"), _.name(".*__init__.*")).lastOption
            case x => x
          }
        case x => Some(x)
      }

      (tgt, defNode, partitionInvolvementInCalls) match {
        // Case 1: Generated by variable assignment
        case (local: Local, Some(genCall: Call), (invokedCalls, argToCalls)) =>
          Option(
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
        case (param: MethodParameterIn, _, (invokedCalls, argToCalls)) if !param.name.matches("(this|self)") =>
          Option(
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
        case (m: Method, _, (invokedCalls, argToCalls)) =>
          var method  = m
          val defComp = createDefComponent(m, null)
          if (method.filename == "<empty>" && defComp.label == "CALL" && method.callIn.nonEmpty) {
            method = method.callIn.head.method
          }
          val annotationCalls = m.annotation
            .map(a =>
              ObservedCall(
                a.name,
                Option(a.fullName),
                List.empty,
                "",
                Option(m.isExternal),
                a.lineNumber.map(_.intValue()),
                a.columnNumber.map(_.intValue())
              )
            )
            .toList
          Option(
            method,
            ObjectUsageSlice(
              targetObj = defComp,
              definedBy = Option(defComp),
              invokedCalls = invokedCalls ++ annotationCalls,
              argToCalls = argToCalls
            )
          )
        case _ =>
          None
      }
    }

    private def partitionInvolvementInCalls: (List[ObservedCall], List[ObservedCallWithArgPos]) = {
      val (invokedCalls, argToCalls) = getInCallsForReferencedIdentifiers(tgt)
        .sortBy(f => (f.lineNumber, f.columnNumber))
        .flatMap(c => c.argument.find(p => p.code == tgt.name).map(f => (c, f)).headOption)
        .map { case (c, arg) =>
          if (arg.argumentName.isDefined) (c, arg, Left(arg.argumentName.get))
          else (c, arg, Right(arg.argumentIndex))
        }
        .partition { case (_, _, argIdx) =>
          argIdx match {
            case Left(_)       => false // receivers/bases are never named
            case Right(argIdx) => argIdx == 0
          }
        }
      (
        invokedCalls.map(_._1).isCall.flatMap(exprToObservedCall).toList,
        argToCalls
          .flatMap { case (c: Call, _, argAt: Either[String, Int]) =>
            exprToObservedCall(c).map(oc => ObservedCallWithArgPos.fromObservedCall(oc, argAt))
          }
      )
    }

    /** Will attempt to get the API call from the expression if this is a procedure call.
      *
      * @param baseCall
      *   the expression to extract the API call from.
      * @return
      *   an API call if present.
      */
    private def exprToObservedCall(baseCall: Call): Option[ObservedCall] = {
      val language           = cpg.metaData.language.headOption
      val isMemberInvocation = baseCall.name.equals(Operators.fieldAccess)
      val isConstructor =
        baseCall.name.equals(Operators.alloc) || baseCall.ast.isCall.nameExact(Operators.alloc).nonEmpty

      def getResolvedMethod(x: Call): Option[String] = if (
        DefComponent.unresolvedCallPattern.matcher(x.methodFullName).matches()
      ) None
      else Option(x.methodFullName)

      // Handle the case where a call is an invocation of a field member (lambda) or function/method call
      var (callName, resolvedMethod): (Option[String], Option[String]) =
        if (isMemberInvocation)
          baseCall.argumentOut
            .flatMap {
              case x: FieldIdentifier =>
                Option(Option(x.code) -> None)
              case x: Call => Option(Option(x.name) -> getResolvedMethod(x))
              case _       => None
            }
            .headOption
            .getOrElse((None, None))
        else if (isConstructor) {
          val m = constructorTypeMatcher.matcher(baseCall.code)
          val typeName =
            if (m.find()) m.group(1)
            else baseCall.code.stripPrefix("new ").takeWhile(!_.equals('('))
          Option(typeName) -> typeMap.get(typeName)
        } else Option(baseCall.name) -> getResolvedMethod(baseCall)

      if (callName.isEmpty) return None

      val params = (if (isMemberInvocation) baseCall.inCall.argument
                    else if (isConstructor)
                      baseCall.ast.isCall
                        .nameExact("<operator>.new")
                        .lastOption
                        .map(_.argument)
                        .getOrElse(Iterator.empty)
                    else baseCall.argument)
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
      // Not sure how we can get the return type unless it's typescript or we can resolve the callee?
      val returnType = if (isConstructor) {
        resolvedMethod match {
          case Some(methodFullName) => methodFullName
          case None                 => "ANY"
        }
      } else {
        baseCall.argumentOut
          .flatMap {
            case x: Call if !DefComponent.unresolvedCallPattern.matcher(x.methodFullName).matches() =>
              cpg.method.fullNameExact(x.methodFullName).methodReturn.typeFullName.headOption
            case x: Call =>
              x.callee(resolver).methodReturn.typeFullName.headOption
            case _ => None
          }
          .headOption
          .getOrElse("ANY")
      }
      // If resolvedMethod is null then use the code property to construct the method name
      if (baseCall.code.nonEmpty && baseCall.code.contains("(") && language.get == Languages.JSSRC) {
        var baseCallCode = baseCall.code.takeWhile(_ != '(')
        if (baseCallCode.contains(" ")) {
          baseCallCode = baseCallCode.split(" ").last
        }
        // Retain the full code for route detection purposes
        if (language.get == Languages.JSSRC && (baseCallCode.startsWith("route") || baseCallCode.startsWith("app"))) {
          baseCallCode = baseCall.code.replaceAll("\n", "").replaceAll(" ", "")
        }
        resolvedMethod = Option(baseCallCode)
      }
      Option(
        ObservedCall(
          callName.get,
          resolvedMethod,
          params,
          returnType,
          baseCall.callee(resolver).isExternal.headOption,
          baseCall.lineNumber.map(_.intValue()),
          baseCall.columnNumber.map(_.intValue())
        )
      )
    }

    /** Creates a def component with the workaround of correcting the type full name if it is only a type name.
      */
    private def createDefComponent(node: AstNode, definedCallNode: AstNode) =
      DefComponent.fromNode(node, definedCallNode, typeMap.toMap)

    // TODO: Slicing may run before post-processing so we cannot assume a call graph
    //  implement this in a next step to combine slices
    @unused
    private def linkSlices(slices: Map[String, Set[ObjectUsageSlice]]): Unit = {
      slices.foreach { case (_, usageSlices) =>
        usageSlices.foreach { slice =>
          slice.definedBy match {
            case Some(CallDef(_, _, Some(resolvedMethod), _, _, _, _)) =>
              slices.get(resolvedMethod) match {
                case Some(_) => // TODO: Handle match
                case None    => // No match
              }
            case _ =>
          }
          slice.argToCalls
            .flatMap {
              case ObservedCallWithArgPos(_, Some(resolvedMethod), _, _, Left(argName), _, _, _) =>
                slices.get(resolvedMethod).flatMap { calleeSlices =>
                  calleeSlices.find { s =>
                    s.targetObj match {
                      case p: ParamDef => p.name == argName
                      case _           => false
                    }
                  }
                }
              case ObservedCallWithArgPos(_, Some(resolvedMethod), _, _, Right(argIdx), _, _, _) =>
                slices.get(resolvedMethod).flatMap { calleeSlices =>
                  calleeSlices.find { s =>
                    s.targetObj match {
                      case p: ParamDef => p.position == argIdx
                      case _           => false
                    }
                  }
                }
              case _ => None
            }
            .foreach { s =>
              // todo: Handle slice linking
            }
        }
      }
    }
  }

  /** Adds extensions to extract all assignments from method bodies.
    */
  implicit class MethodDataSourceExt(trav: Iterator[Method]) {
    def declaration: Iterator[Declaration] = trav.ast.collectAll[Declaration]

  }

}
