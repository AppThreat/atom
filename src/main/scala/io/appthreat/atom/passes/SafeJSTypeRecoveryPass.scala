package io.appthreat.atom.passes

import io.appthreat.jssrc2cpg.passes.{Defines, GlobalBuiltins}
import io.appthreat.x2cpg.Defines.ConstructorMethodName
import io.appthreat.x2cpg.passes.frontend.*
import io.appthreat.x2cpg.Defines as XDefines
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.codepropertygraph.generated.{Operators, PropertyNames}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.language.operatorextension.OpNodes.FieldAccess
import overflowdb.BatchedUpdate.DiffGraphBuilder

class SafeJSTypeRecoveryPass(
  cpg: Cpg,
  config: XTypeRecoveryConfig = XTypeRecoveryConfig(enabledDummyTypes = false)
) extends XTypeRecoveryPass[File](cpg, config):
    override protected def generateRecoveryPass(state: XTypeRecoveryState): XTypeRecovery[File] =
        new SafeJSTypeRecovery(cpg, state)

private class SafeJSTypeRecovery(cpg: Cpg, state: XTypeRecoveryState)
    extends XTypeRecovery[File](cpg, state):

    override def compilationUnit: Iterator[File] = cpg.file.iterator

    override def generateRecoveryForCompilationUnitTask(
      unit: File,
      builder: DiffGraphBuilder
    ): RecoverForXCompilationUnit[File] =
        val newConfig = state.config.copy(enabledDummyTypes = false)
        new RecoverForJavaScriptFile(cpg, unit, builder, state.copy(config = newConfig))

private class RecoverForJavaScriptFile(
  cpg: Cpg,
  cu: File,
  builder: DiffGraphBuilder,
  state: XTypeRecoveryState
) extends RecoverForXCompilationUnit[File](cpg, cu, builder, state):

    override protected val pathSep = ':'

    /** A heuristic method to determine if a call is a constructor or not.
      */
    override protected def isConstructor(c: Call): Boolean =
        c.name.endsWith("factory") && c.inCall.astParent.headOption.exists(_.isInstanceOf[Block])

    override protected def isConstructor(name: String): Boolean =
        !name.isBlank && (name.charAt(0).isUpper || name.endsWith("factory"))

    override protected def prepopulateSymbolTableEntry(x: AstNode): Unit = x match
        case x @ (_: Identifier | _: Local | _: MethodParameterIn)
            if x.property(PropertyNames.TYPE_FULL_NAME, Defines.Any) != Defines.Any =>
            val typeFullName = x.property(PropertyNames.TYPE_FULL_NAME, Defines.Any)
            val typeHints = symbolTable.get(LocalVar(x.property(
              PropertyNames.TYPE_FULL_NAME,
              Defines.Any
            ))) - typeFullName
            lazy val cpgTypeFullName = cpg.typeDecl.nameExact(typeFullName).fullName.toSet
            val resolvedTypeHints: Set[String] =
                if typeHints.nonEmpty then symbolTable.put(x, typeHints)
                else if cpgTypeFullName.nonEmpty then
                    if !cpgTypeFullName.head.startsWith("{") then
                        symbolTable.put(x, cpgTypeFullName)
                    else null
                else symbolTable.put(x, x.getKnownTypes)
            if resolvedTypeHints != null && resolvedTypeHints.sizeIs == 1 && !resolvedTypeHints.contains(
                  typeFullName
                )
            then
                if !resolvedTypeHints.head.contains(".js") then
                    builder.setNodeProperty(x, PropertyNames.TYPE_FULL_NAME, resolvedTypeHints.head)
        case x @ (_: Identifier | _: Local | _: MethodParameterIn) =>
            symbolTable.put(x, x.getKnownTypes)
        case x: Call =>
            if !x.methodFullName.contains(".js") then
                builder.setNodeProperty(x, PropertyNames.IS_EXTERNAL, true)
            symbolTable.put(x, (x.methodFullName +: x.dynamicTypeHintFullName).toSet)
        case _ =>

    override protected def prepopulateSymbolTable(): Unit =
        super.prepopulateSymbolTable()
        cu.ast.isMethod.foreach(f =>
            symbolTable.put(CallAlias(f.name, Option("this")), Set(f.fullName))
        )
        (cu.ast.isParameter.whereNot(_.nameExact("this")) ++ cu.ast.isMethod.methodReturn).filter(
          hasTypes
        ).foreach { p =>
            val resolvedHints = p.getKnownTypes
                .map { t =>
                    t.split("\\.").headOption match
                        case Some(base) if symbolTable.contains(LocalVar(base)) =>
                            (t, symbolTable.get(LocalVar(base)).map(x => x + t.stripPrefix(base)))
                        case _ => (t, Set(t))
                }
                .flatMap {
                    case (t, ts) if Set(t) == ts => Set(t)
                    case (_, ts) => ts.map(_.replaceAll("\\.(?!js::program)", pathSep.toString))
                }
            p match
                case _: MethodParameterIn => symbolTable.put(p, resolvedHints)
                case _: MethodReturn
                    if resolvedHints.sizeIs == 1 && !resolvedHints.head.contains(".js") =>
                    builder.setNodeProperty(p, PropertyNames.TYPE_FULL_NAME, resolvedHints.head)
                case _ =>
        }
    end prepopulateSymbolTable

    private lazy val exportedIdentifiers = cu.method
        .nameExact(":program")
        .ast
        .isCall
        .nameExact(Operators.assignment)
        .filter(_.code.startsWith("exports.*"))
        .argument
        .isIdentifier
        .name
        .toSet

    override protected def isField(i: Identifier): Boolean =
        state.isFieldCache.getOrElseUpdate(
          i.id(),
          exportedIdentifiers.contains(i.name) || super.isField(i)
        )

    override protected def visitIdentifierAssignedToConstructor(
      i: Identifier,
      c: Call
    ): Set[String] =
        val constructorPaths = if c.methodFullName.endsWith(".alloc") then
            def newChildren =
                c.inAssignment.astSiblings.isCall.nameExact("<operator>.new").astChildren
            val possibleImportIdentifier = newChildren.isIdentifier.headOption match
                case Some(i) if GlobalBuiltins.builtins.contains(i.name) => Set(s"__ecma.${i.name}")
                case Some(i)                                             => symbolTable.get(i)
                case None                                                => Set.empty[String]
            lazy val possibleConstructorPointer =
                newChildren.astChildren.isFieldIdentifier.map(f =>
                    CallAlias(f.canonicalName, Some("this"))
                ).headOption match
                    case Some(fi) => symbolTable.get(fi)
                    case None     => Set.empty[String]

            if possibleImportIdentifier.nonEmpty then possibleImportIdentifier
            else if possibleConstructorPointer.nonEmpty then possibleConstructorPointer
            else Set.empty[String]
        else (symbolTable.get(c) + c.methodFullName).map(t => t.stripSuffix(".factory"))
        associateTypes(i, constructorPaths)
    end visitIdentifierAssignedToConstructor

    override protected def visitIdentifierAssignedToOperator(
      i: Identifier,
      c: Call,
      operation: String
    ): Set[String] =
        operation match
            case "<operator>.new" =>
                c.astChildren.l match
                    case ::(fa: Call, ::(i: Identifier, _)) if fa.name == Operators.fieldAccess =>
                        symbolTable.append(
                          c,
                          visitIdentifierAssignedToFieldLoad(i, new FieldAccess(fa)).map(t =>
                              s"$t$pathSep$ConstructorMethodName"
                          )
                        )
                    case _ => Set.empty
            case _ => super.visitIdentifierAssignedToOperator(i, c, operation)

    override protected def associateInterproceduralTypes(
      i: Identifier,
      fieldFullName: String,
      fieldName: String,
      globalTypes: Set[String],
      baseTypes: Set[String]
    ): Set[String] =
        // Monkey patch for req and res
        if fieldFullName.startsWith("req:") || fieldFullName.startsWith("request:") then
            symbolTable.append(i, Set("Request", "http.IncomingMessage"))
        else if fieldFullName.startsWith("res:") || fieldFullName.startsWith("response:") then
            symbolTable.append(i, Set("Response", "http.IncomingMessage"))
        if symbolTable.contains(LocalVar(fieldName)) then
            val fieldTypes = symbolTable.get(LocalVar(fieldName))
            symbolTable.append(i, fieldTypes)
        else if symbolTable.contains(CallAlias(fieldName, Option("this"))) then
            symbolTable.get(CallAlias(fieldName, Option("this")))
        else
            Set.empty
    end associateInterproceduralTypes

    override protected def visitIdentifierAssignedToCall(i: Identifier, c: Call): Set[String] =
        Set.empty

    override protected def visitIdentifierAssignedToMethodRef(
      i: Identifier,
      m: MethodRef,
      rec: Option[String] = None
    ): Set[String] =
        Set.empty

    override protected def visitIdentifierAssignedToTypeRef(
      i: Identifier,
      t: TypeRef,
      rec: Option[String] = None
    ): Set[String] =
        Set.empty

    override protected def postSetTypeInformation(): Unit =
        // often there are "this" identifiers with type hints but this can be set to a type hint if they meet the criteria
        cu.ast.isIdentifier
            .nameExact("this")
            .where(_.typeFullNameExact(Defines.Any))
            .filterNot(_.dynamicTypeHintFullName.isEmpty)
            .foreach(setTypeFromTypeHints)

    protected override def storeIdentifierTypeInfo(i: Identifier, types: Seq[String]): Unit =
        super.storeIdentifierTypeInfo(
          i,
          types.map(_.stripSuffix(s"$pathSep${XDefines.ConstructorMethodName}"))
        )

    protected override def storeLocalTypeInfo(i: Local, types: Seq[String]): Unit =
        super.storeLocalTypeInfo(
          i,
          types.map(_.stripSuffix(s"$pathSep${XDefines.ConstructorMethodName}"))
        )
end RecoverForJavaScriptFile
