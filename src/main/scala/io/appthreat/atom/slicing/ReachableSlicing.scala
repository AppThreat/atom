package io.appthreat.atom.slicing

import better.files.File
import io.appthreat.atom.Atom.{DEFAULT_SINK_TAGS, DEFAULT_SOURCE_TAGS, FRAMEWORK_INPUT_TAG}
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.language.*
import io.appthreat.dataflowengineoss.queryengine.{EngineConfig, EngineContext}
import io.appthreat.dataflowengineoss.semanticsloader.Semantics
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import java.io.{File as JFile, BufferedWriter, FileWriter}
import java.util.regex.Pattern
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import io.circe.generic.auto.*
import io.circe.syntax.*

object ReachableSlicing:

  implicit val semantics: Semantics      = DefaultSemantics()
  private val engineConfig: EngineConfig = EngineConfig()
  implicit val context: EngineContext    = EngineContext(semantics, engineConfig)
  private def API_TAG                    = "api"
  private def FRAMEWORK_TAG              = "framework"

  private def LIBRARY_CALL_TAG     = "library-call"
  private def CLI_SOURCE_TAG       = "cli-source"
  private def DRIVER_SOURCE_TAG    = "driver-source"
  private def HTTP_TAG             = "http"
  private def EVENT_TAG            = "event"
  private def PARSE_TAG            = "parse"
  private def CRYPTO_GENERATE_TAG  = "crypto-generate"
  private def CRYPTO_ALGORITHM_TAG = "crypto-algorithm"
  private val JVM_BASED_LANGUAGES =
      Set(Languages.JAVA, Languages.JAVASRC, "JAR", "JIMPLE", "ANDROID", "APK", "DEX")

  private val DEFAULT_CHUNK_SIZE = 1000

  def calculateReachableSliceAndPersist(
    atom: Cpg,
    config: ReachablesConfig,
    outputBasePath: String,
    chunkSize: Int = DEFAULT_CHUNK_SIZE
  ): Unit =
    val language        = atom.metaData.language.head
    val flowSlices      = collectFlowSlices(atom, config, language)
    var fileCounter     = 0
    val currentChunk    = ListBuffer[ReachableFlows]()
    var totalFlowsCount = 0L
    val currentOutputFile = File(if fileCounter == 0 then s"$outputBasePath.json"
    else s"${outputBasePath}_$fileCounter.json")
    var writer = new BufferedWriter(new FileWriter(currentOutputFile.toJava))
    writer.write("[")
    var isFirstInFile = true

    try
      flowSlices.foreach { pathIterator =>
          pathIterator.foreach { path =>
            val reachableFlow = toSlice(path)
            currentChunk += reachableFlow
            totalFlowsCount += 1

            if currentChunk.size >= chunkSize then
              currentChunk.foreach { flow =>
                if !isFirstInFile then writer.write(",")
                writer.write(flow.asJson.noSpaces)
                isFirstInFile = false
              }
              currentChunk.clear()
              writer.write("]")
              writer.close()
              fileCounter += 1
              val nextOutputFile = File(s"${outputBasePath}_$fileCounter.json")
              val nextWriter     = new BufferedWriter(new FileWriter(nextOutputFile.toJava))
              nextWriter.write("[")
              writer = nextWriter
              isFirstInFile = true
          }
      }
      if currentChunk.nonEmpty then
        currentChunk.foreach { flow =>
          if !isFirstInFile then writer.write(",")
          writer.write(flow.asJson.noSpaces)
          isFirstInFile = false
        }
        currentChunk.clear()
      writer.write("]")
      writer.close()
      if totalFlowsCount == 0 then
        handleEmptySlices(atom, config)
        if fileCounter == 1 && isFirstInFile then
          writer.close()
          val finalWriter = new BufferedWriter(new FileWriter(currentOutputFile.toJava))
          finalWriter.write("[]")
          finalWriter.close()
        else if fileCounter >= 1 && !isFirstInFile then
          writer.write("]")
          writer.close()
    catch
      case t: Throwable =>
          writer.close()
          throw t
    end try
  end calculateReachableSliceAndPersist

  @deprecated(
    "Use calculateReachableSliceAndPersist for large datasets",
    "Since Chunking Implementation"
  )
  def calculateReachableSlice(atom: Cpg, config: ReachablesConfig): ReachableSlice =
    val language   = atom.metaData.language.head
    val flowSlices = collectFlowSlices(atom, config, language)
    val slicesList = flowSlices.flatten.map(toSlice).toList

    if slicesList.isEmpty then
      handleEmptySlices(atom, config)

    ReachableSlice(slicesList)

  private def collectFlowSlices(
    atom: Cpg,
    config: ReachablesConfig,
    language: String
  ): ListBuffer[Iterator[Path]] =
    val flowSlices = ListBuffer.empty[Iterator[Path]]
    val defaultTagsMode =
        config.sourceTag == DEFAULT_SOURCE_TAGS && config.sinkTag == DEFAULT_SINK_TAGS
    val sourceTagRegex = raw"""(${config.sourceTag.mkString("|")})"""
    val sinkTagRegex   = raw"""(${config.sinkTag.mkString("|")})"""

    // Basic flows
    flowSlices += collectBasicFlows(atom, sourceTagRegex, sinkTagRegex)

    if defaultTagsMode then
      flowSlices ++= collectDefaultTagFlows(atom, sourceTagRegex)

    // Crypto flows
    if config.includeCryptoFlows then
      flowSlices ++= collectCryptoFlows(atom, language)

    // Language-specific flows
    collectLanguageSpecificFlows(
      atom,
      config,
      language,
      defaultTagsMode,
      sourceTagRegex,
      sinkTagRegex,
      flowSlices
    )

    flowSlices
  end collectFlowSlices

  private def collectBasicFlows(
    atom: Cpg,
    sourceTagRegex: String,
    sinkTagRegex: String
  ): Iterator[Path] =
    val sourceP = atom.tag.name(sourceTagRegex).parameter
    val sourceI = atom.tag.name(sourceTagRegex).identifier
    val sink    = atom.ret.where(_.tag.name(sinkTagRegex))
    sink.reachableByFlows(sourceP, sourceI)

  private def collectDefaultTagFlows(atom: Cpg, sourceTagRegex: String): List[Iterator[Path]] =
      List(
        atom.ret.where(_.method.tag.name(sourceTagRegex)).reachableByFlows(
          atom.tag.name(sourceTagRegex).parameter,
          atom.tag.name(sourceTagRegex).identifier
        ),
        atom.tag.name(API_TAG).parameter.reachableByFlows(atom.tag.name(API_TAG).parameter)
      )

  private def collectCryptoFlows(atom: Cpg, language: String): List[Iterator[Path]] =
      language match
        case l if JVM_BASED_LANGUAGES.contains(l) =>
            List(atom.tag.name(CRYPTO_GENERATE_TAG).call.reachableByFlows(
              atom.tag.name(CRYPTO_ALGORITHM_TAG).literal
            ))
        case l if Seq(Languages.PYTHON, Languages.PYTHONSRC).contains(l) =>
            List(atom.tag.name(CRYPTO_GENERATE_TAG).call.reachableByFlows(
              atom.tag.name(CRYPTO_ALGORITHM_TAG).call
            ))
        case _ =>
            List.empty

  private def collectLanguageSpecificFlows(
    atom: Cpg,
    config: ReachablesConfig,
    language: String,
    defaultTagsMode: Boolean,
    sourceTagRegex: String,
    sinkTagRegex: String,
    flowSlices: ListBuffer[Iterator[Path]]
  ): Unit =
    collectDynamicLanguageFlows(
      atom,
      config,
      language,
      defaultTagsMode,
      sourceTagRegex,
      sinkTagRegex,
      flowSlices
    )
    collectPHPRubyFlows(atom, language, sourceTagRegex, flowSlices)
    collectRubySpecificFlows(atom, language, defaultTagsMode, config, flowSlices)
    collectCLanguageFlows(atom, language, defaultTagsMode, config, flowSlices)
  end collectLanguageSpecificFlows

  private def collectDynamicLanguageFlows(
    atom: Cpg,
    config: ReachablesConfig,
    language: String,
    defaultTagsMode: Boolean,
    sourceTagRegex: String,
    sinkTagRegex: String,
    flowSlices: ListBuffer[Iterator[Path]]
  ): Unit =
      if Array(
          Languages.JSSRC,
          Languages.JAVASCRIPT,
          Languages.PYTHON,
          Languages.PYTHONSRC,
          Languages.RUBYSRC
        ).contains(language)
      then
        val dynCallSource          = atom.tag.name(sourceTagRegex).call.argument.isIdentifier
        val dynFrameworkIdentifier = atom.tag.name(FRAMEWORK_TAG).identifier
        val dynFrameworkParameter  = atom.tag.name(FRAMEWORK_TAG).parameter
        val dynSink                = atom.tag.name(sinkTagRegex).call.argument.isIdentifier

        flowSlices += dynSink.reachableByFlows(dynCallSource, dynFrameworkParameter)

        if defaultTagsMode then
          flowSlices += atom.tag.name(FRAMEWORK_TAG).call.argument.reachableByFlows(
            dynFrameworkParameter,
            atom.tag.name(sourceTagRegex).parameter
          )

          if dynFrameworkParameter.isEmpty then
            flowSlices ++= List(
              dynSink.reachableByFlows(dynCallSource, dynFrameworkIdentifier),
              atom.tag.name(FRAMEWORK_TAG).call.argument.isIdentifier.reachableByFlows(
                atom.tag.name(sourceTagRegex).identifier
              )
            )

        collectPythonSpecificFlows(atom, language, defaultTagsMode, flowSlices, sinkTagRegex)

  private def collectPythonSpecificFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    flowSlices: ListBuffer[Iterator[Path]],
    sinkTagRegex: String
  ): Unit =
    val dynSink = atom.tag.name(sinkTagRegex).call.argument.isIdentifier
    val dynCallSource =
        atom.tag.name(sinkTagRegex).call.argument.isIdentifier // This should use sourceTagRegex instead
    val correctedDynCallSource =
        atom.tag.name(raw"""(${DEFAULT_SOURCE_TAGS.mkString("|")})""").call.argument.isIdentifier

    if Array(Languages.PYTHON, Languages.PYTHONSRC).contains(language) && defaultTagsMode then
      if atom.tag.name(CLI_SOURCE_TAG).identifier.nonEmpty then
        flowSlices += atom.tag.name("pkg.*").identifier.reachableByFlows(
          atom.tag.name(CLI_SOURCE_TAG).identifier
        )
      else
        flowSlices += atom.tag.name("pkg.*").identifier.reachableByFlows(
          atom.tag.name(FRAMEWORK_TAG).parameter
        )
    else
      flowSlices += atom.tag.name("pkg.*").identifier.reachableByFlows(
        atom.tag.name(CLI_SOURCE_TAG).call
      )
  end collectPythonSpecificFlows

  private def collectPHPRubyFlows(
    atom: Cpg,
    language: String,
    sourceTagRegex: String,
    flowSlices: ListBuffer[Iterator[Path]]
  ): Unit =
      if Array(Languages.PHP, Languages.RUBYSRC).contains(language) then
        flowSlices += atom.tag.name(FRAMEWORK_TAG).parameter.reachableByFlows(
          atom.tag.name(sourceTagRegex).parameter
        )

  private def collectRubySpecificFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    config: ReachablesConfig,
    flowSlices: ListBuffer[Iterator[Path]]
  ): Unit =
      if language == Languages.RUBYSRC && defaultTagsMode then
        flowSlices += atom.tag.name("pkg.*").call.argument.reachableByFlows(
          atom.tag.name("pkg.*").call.method.repeat(_.filename(
            s"((app|config)${Pattern.quote(JFile.separator)})?(routes|controller(s)?|model(s)?|application).*\\.rb.*"
          ))(using _.maxDepth(config.sliceDepth)).parameter
        )

  private def collectCLanguageFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    config: ReachablesConfig,
    flowSlices: ListBuffer[Iterator[Path]]
  ): Unit =
      if Array(Languages.NEWC, Languages.C).contains(language) then
        flowSlices ++= collectCBasicFlows(atom)

        if defaultTagsMode then
          flowSlices ++= collectCDefaultFlows(atom, config)

  private def collectCBasicFlows(atom: Cpg): List[Iterator[Path]] =
      List(
        atom.tag.name(LIBRARY_CALL_TAG).call.reachableByFlows(
          atom.tag.name(s"($CLI_SOURCE_TAG|$DRIVER_SOURCE_TAG)").parameter
        ),
        atom.tag.name(HTTP_TAG).parameter.reachableByFlows(
          atom.tag.name(s"($CLI_SOURCE_TAG|$HTTP_TAG)").parameter
        )
      )

  private def collectCDefaultFlows(atom: Cpg, config: ReachablesConfig): List[Iterator[Path]] =
    val atomSemanticTags =
        atom.tag.name.filterNot(t => t.startsWith("pkg:") || t.toUpperCase().equals(t)).toSet
    val atomPossibleSinkTags = atomSemanticTags.filterNot(t =>
        t.contains("source") || t.contains("input") || t.contains("route")
    )

    val flows = ListBuffer[Iterator[Path]]()

    if atomPossibleSinkTags.nonEmpty then
      flows += atom.tag.name(s"(${atomPossibleSinkTags.slice(0, 5).mkString("|")})")
          .identifier.reachableByFlows(atom.tag.name(
            s"($EVENT_TAG|$CLI_SOURCE_TAG|$HTTP_TAG)"
          ).parameter)

    println("Collecting additional slices with Reverse Reachability.")
    flows ++= List(
      atom.tag.name(LIBRARY_CALL_TAG).call.reachableByFlows(
        atom.tag.name(LIBRARY_CALL_TAG).call.method.repeat(_.caller(using NoResolve))(using
        _.maxDepth(config.sliceDepth)).parameter
      ),
      atom.tag.name(s"($LIBRARY_CALL_TAG|$HTTP_TAG)").parameter.reachableByFlows(
        atom.tag.name(s"($LIBRARY_CALL_TAG|$HTTP_TAG)").parameter.method.repeat(_.caller(using
        NoResolve))(using _.maxDepth(config.sliceDepth)).parameter
      ),
      atom.tag.name(LIBRARY_CALL_TAG).method.parameter.reachableByFlows(
        atom.tag.name(FRAMEWORK_INPUT_TAG).parameter
      ),
      atom.tag.name(LIBRARY_CALL_TAG).identifier.inCall.argument.reachableByFlows(
        atom.method.internal.parameter
      )
    )

    flows.toList
  end collectCDefaultFlows

  private def handleEmptySlices(atom: Cpg, config: ReachablesConfig): Unit =
    println(
      s"No Reachable Flows identified for the given source tags: ${config.sourceTag} and sink tags: ${config.sinkTag}"
    )
    val atomSemanticTags =
        atom.tag.name.filterNot(t => t.startsWith("pkg:") || t.toUpperCase().equals(t)).toSet
    if atomSemanticTags.nonEmpty then
      println(s"List of semantic tags found in the atom file:\n${atomSemanticTags.mkString("\n")}")

  private def tagAsString(tag: Iterator[Tag]): String =
      if tag.nonEmpty then
        tag.name.filterNot(v => v.toUpperCase() == v && v.contains("_")).mkString(", ")
      else ""

  private def purlsFromTag(tag: Iterator[Tag]): Set[String] =
      if tag.nonEmpty then tag.name.filter(_.startsWith("pkg:")).toSet else Set.empty

  private def toSlice(path: Path): ReachableFlows =
    val tableRows  = ArrayBuffer[SliceNode]()
    val addedPaths = mutable.Set[String]()
    val purls      = mutable.Set[String]()

    path.elements.foreach { astNode =>
        processAstNode(astNode, tableRows, addedPaths, purls)
    }

    ReachableFlows(flows = tableRows.toList, purls = purls.toSet)

  private def processAstNode(
    astNode: AstNode,
    tableRows: ArrayBuffer[SliceNode],
    addedPaths: mutable.Set[String],
    purls: mutable.Set[String]
  ): Unit =
    val lineNumber   = astNode.lineNumber.map(_.intValue())
    val fileName     = astNode.file.name.headOption.getOrElse("").replace("<unknown>", "")
    var fileLocation = s"$fileName#$lineNumber"
    val tags: String = tagAsString(astNode.tag)
    purls ++= purlsFromTag(astNode.tag)

    if fileLocation == "#" then fileLocation = "N/A"

    val sliceNode = SliceNode(
      astNode.id(),
      astNode.label,
      code = astNode.code,
      parentFileName = astNode.file.name.headOption.getOrElse(""),
      lineNumber = astNode.lineNumber,
      columnNumber = astNode.columnNumber,
      tags = tags
    )

    astNode match
      case _: MethodReturn =>
      case _: Block        =>
      case methodParameterIn: MethodParameterIn =>
          processMethodParameterIn(methodParameterIn, sliceNode, tableRows, purls)
      case ret: Return =>
          processReturn(ret, sliceNode, tableRows)
      case literal: Literal =>
          processLiteral(literal, sliceNode, tableRows, addedPaths, fileName, lineNumber, purls)
      case identifier: Identifier =>
          processIdentifier(
            identifier,
            sliceNode,
            tableRows,
            addedPaths,
            fileName,
            lineNumber,
            purls
          )
      case member: Member =>
          processMember(member, sliceNode, tableRows)
      case call: Call =>
          processCall(call, sliceNode, tableRows, purls)
      case cfgNode: CfgNode =>
          processCfgNode(cfgNode, sliceNode, tableRows, purls)
    end match

    addedPaths += s"$fileName#$lineNumber"
  end processAstNode

  private def processMethodParameterIn(
    methodParameterIn: MethodParameterIn,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode],
    purls: mutable.Set[String]
  ): Unit =
    val methodName = methodParameterIn.method.name
    var tags       = tagAsString(methodParameterIn.tag)
    var localPurls = purlsFromTag(methodParameterIn.tag)

    if tags.isEmpty && methodParameterIn.method.tag.nonEmpty then
      tags = tagAsString(methodParameterIn.method.tag)
      localPurls ++= purlsFromTag(methodParameterIn.method.tag)

    if tags.isEmpty && methodParameterIn.tag.nonEmpty then
      tags = tagAsString(methodParameterIn.tag)
      localPurls ++= purlsFromTag(methodParameterIn.tag)

    purls ++= localPurls

    val updatedSliceNode = sliceNode.copy(
      name = methodParameterIn.name,
      code = methodParameterIn.code,
      typeFullName = methodParameterIn.typeFullName,
      parentMethodName = methodName,
      parentMethodSignature = methodParameterIn.method.signature,
      parentPackageName = methodParameterIn.method.location.packageName,
      parentClassName = methodParameterIn.method.location.className,
      isExternal = methodParameterIn.method.isExternal,
      lineNumber = methodParameterIn.lineNumber,
      columnNumber = methodParameterIn.columnNumber,
      tags = tags
    )
    tableRows += updatedSliceNode
  end processMethodParameterIn

  private def processReturn(
    ret: Return,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode]
  ): Unit =
    val methodName = ret.method.name
    val updatedSliceNode = sliceNode.copy(
      name = ret.argumentName.getOrElse(""),
      code = ret.code,
      parentMethodName = methodName,
      parentMethodSignature = ret.method.signature,
      parentPackageName = ret.method.location.packageName,
      parentClassName = ret.method.location.className,
      lineNumber = ret.lineNumber,
      columnNumber = ret.columnNumber
    )
    tableRows += updatedSliceNode

  private def processLiteral(
    literal: Literal,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode],
    addedPaths: mutable.Set[String],
    fileName: String,
    lineNumber: Option[Int],
    purls: mutable.Set[String]
  ): Unit =
    val methodName = literal.method.name
    var tags       = tagAsString(literal.tag)
    var localPurls = purlsFromTag(literal.tag)

    if tags.isEmpty && literal.inCall.nonEmpty && literal.inCall.head.tag.nonEmpty then
      tags = tagAsString(literal.inCall.head.tag)
      localPurls ++= purlsFromTag(literal.inCall.head.tag)

    purls ++= localPurls

    if !addedPaths.contains(s"$fileName#$lineNumber") then
      val updatedSliceNode = sliceNode.copy(
        name = literal.code.replaceAll("""(['"])""", ""),
        code = literal.code.replaceAll("""(['"])""", ""),
        typeFullName = literal.typeFullName,
        parentMethodName = methodName,
        parentMethodSignature = literal.method.signature,
        parentPackageName = literal.method.location.packageName,
        parentClassName = literal.method.location.className,
        lineNumber = literal.lineNumber,
        columnNumber = literal.columnNumber,
        tags = tags
      )
      tableRows += updatedSliceNode
  end processLiteral

  private def processIdentifier(
    identifier: Identifier,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode],
    addedPaths: mutable.Set[String],
    fileName: String,
    lineNumber: Option[Int],
    purls: mutable.Set[String]
  ): Unit =
    val methodName = identifier.method.name
    var tags       = tagAsString(identifier.tag)
    var localPurls = purlsFromTag(identifier.tag)

    if tags.isEmpty && identifier.inCall.nonEmpty && identifier.inCall.head.tag.nonEmpty then
      tags = tagAsString(identifier.inCall.head.tag)
      localPurls ++= purlsFromTag(identifier.inCall.head.tag)

    purls ++= localPurls

    if !addedPaths.contains(s"$fileName#$lineNumber") && identifier.inCall.nonEmpty then
      val updatedSliceNode = sliceNode.copy(
        name = identifier.name,
        code = if identifier.inCall.nonEmpty then identifier.inCall.head.code else identifier.code,
        parentMethodName = methodName,
        parentMethodSignature = identifier.method.signature,
        parentPackageName = identifier.method.location.packageName,
        parentClassName = identifier.method.location.className,
        lineNumber = identifier.lineNumber,
        columnNumber = identifier.columnNumber,
        tags = tags
      )
      tableRows += updatedSliceNode
  end processIdentifier

  private def processMember(
    member: Member,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode]
  ): Unit =
    val methodName = "<not-in-method>"
    val updatedSliceNode = sliceNode.copy(
      name = member.name,
      code = member.code,
      parentMethodName = methodName
    )
    tableRows += updatedSliceNode

  private def processCall(
    call: Call,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode],
    purls: mutable.Set[String]
  ): Unit =
      if !call.code.startsWith("<operator") || !call.methodFullName.startsWith("<operator") then
        var tags       = tagAsString(call.tag)
        var localPurls = purlsFromTag(call.tag)
        var isExternal = false

        if tags.isEmpty && call.callee(using NoResolve).nonEmpty && call.callee(using NoResolve)
              .head.isExternal &&
          !call.methodFullName.startsWith("<operator") && !call.name.startsWith(
            "<operator"
          ) && !call.methodFullName.startsWith("new ")
        then
          tags = tagAsString(call.callee(using NoResolve).head.tag)
          localPurls ++= purlsFromTag(call.callee(using NoResolve).head.tag)

        isExternal =
            if call.callee(using NoResolve).nonEmpty && call.callee(using NoResolve).head.isExternal &&
              !call.name.startsWith("<operator") && !call.methodFullName.startsWith("new ")
            then
              true
            else false

        if call.methodFullName.startsWith("<operator") then isExternal = false

        purls ++= localPurls

        val updatedSliceNode = sliceNode.copy(
          name = call.name,
          fullName = if call.callee(using NoResolve).nonEmpty then
            call.callee(using NoResolve).head.fullName
          else "",
          code = call.code,
          isExternal = isExternal,
          parentMethodName = call.method.name,
          parentMethodSignature = call.method.signature,
          parentPackageName = call.method.location.packageName,
          parentClassName = call.method.location.className,
          lineNumber = call.lineNumber,
          columnNumber = call.columnNumber,
          tags = tags
        )
        tableRows += updatedSliceNode

  private def processCfgNode(
    cfgNode: CfgNode,
    sliceNode: SliceNode,
    tableRows: ArrayBuffer[SliceNode],
    purls: mutable.Set[String]
  ): Unit =
    val method     = cfgNode.method
    var tags       = tagAsString(cfgNode.tag)
    var localPurls = purlsFromTag(cfgNode.tag)

    if tags.isEmpty && method.tag.nonEmpty then
      tags = tagAsString(method.tag)
      localPurls ++= purlsFromTag(method.tag)

    val methodName = method.name
    val statement = cfgNode match
      case param: MethodParameterIn =>
          val paramTags  = tagAsString(method.parameter.tag)
          var paramPurls = purlsFromTag(method.parameter.tag)

          if tags.isEmpty && method.parameter.tag.nonEmpty then
            tags = paramTags
            localPurls ++= paramPurls

          val paramsPretty = method.parameter.toList.sortBy(_.index).map(_.code).mkString(", ")
          s"$methodName($paramsPretty)"
      case _ =>
          val stmtTags  = tagAsString(cfgNode.statement.tag)
          var stmtPurls = purlsFromTag(cfgNode.statement.tag)

          if tags.isEmpty && cfgNode.statement.tag.nonEmpty then
            tags = stmtTags
            localPurls ++= stmtPurls

          cfgNode.statement.repr

    purls ++= localPurls

    val updatedSliceNode = sliceNode.copy(
      parentMethodName = methodName,
      code = statement,
      tags = tags
    )
    tableRows += updatedSliceNode
  end processCfgNode
end ReachableSlicing
