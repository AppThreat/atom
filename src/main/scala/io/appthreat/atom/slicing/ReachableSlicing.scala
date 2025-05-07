package io.appthreat.atom.slicing

import io.appthreat.atom.Atom.{DEFAULT_SINK_TAGS, DEFAULT_SOURCE_TAGS, FRAMEWORK_INPUT_TAG}
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.language.*
import io.appthreat.dataflowengineoss.queryengine.{EngineConfig, EngineContext}
import io.appthreat.dataflowengineoss.semanticsloader.Semantics
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import java.io.File
import java.util.regex.Pattern
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

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

  def calculateReachableSlice(atom: Cpg, config: ReachablesConfig): ReachableSlice =
    val language = atom.metaData.language.head
    val defaultTagsMode =
        config.sourceTag == DEFAULT_SOURCE_TAGS && config.sinkTag == DEFAULT_SINK_TAGS
    val sourceTagRegex = raw"""(${config.sourceTag.mkString("|")})"""
    val sinkTagRegex   = raw"""(${config.sinkTag.mkString("|")})"""
    def sourceP        = atom.tag.name(sourceTagRegex).parameter
    def sourceI        = atom.tag.name(sourceTagRegex).identifier
    def sink           = atom.ret.where(_.tag.name(sinkTagRegex))
    def atomSemanticTags =
        atom.tag.name.filterNot(t => t.startsWith("pkg:") || t.toUpperCase().equals(t)).toSet
    def atomPossibleSinkTags =
        atomSemanticTags.filterNot(t =>
            t.contains("source") || t.contains("input") || t.contains("route")
        )
    val flowSlices = ListBuffer.empty[Iterator[io.appthreat.dataflowengineoss.language.Path]]
    flowSlices += sink.reachableByFlows(sourceP, sourceI)
    if defaultTagsMode then
      flowSlices += atom.ret.where(_.method.tag.name(sourceTagRegex)).reachableByFlows(
        sourceP,
        sourceI
      )
      flowSlices +=
          atom.tag.name(API_TAG).parameter.reachableByFlows(atom.tag.name(API_TAG).parameter)
    // For Java and Python, we support crypto flows
    if config.includeCryptoFlows then
      language match
        case l if Seq(Languages.JAVA, Languages.JAVASRC).contains(l) =>
            flowSlices += atom
                .tag
                .name(CRYPTO_GENERATE_TAG)
                .call
                .reachableByFlows(atom.tag.name(CRYPTO_ALGORITHM_TAG).literal)

        case l if Seq(Languages.PYTHON, Languages.PYTHONSRC).contains(l) =>
            flowSlices += atom
                .tag
                .name(CRYPTO_GENERATE_TAG)
                .call
                .reachableByFlows(atom.tag.name(CRYPTO_ALGORITHM_TAG).call)

        case _ =>
    // For JavaScript, Python, and Ruby, we need flows between arguments of call nodes to track callbacks and middlewares
    if
      Array(
        Languages.JSSRC,
        Languages.JAVASCRIPT,
        Languages.PYTHON,
        Languages.PYTHONSRC,
        Languages.RUBYSRC
      ).contains(language)
    then
      def dynCallSource          = atom.tag.name(sourceTagRegex).call.argument.isIdentifier
      def dynFrameworkIdentifier = atom.tag.name(FRAMEWORK_TAG).identifier
      def dynFrameworkParameter  = atom.tag.name(FRAMEWORK_TAG).parameter
      def dynSink                = atom.tag.name(sinkTagRegex).call.argument.isIdentifier
      flowSlices += dynSink
          .reachableByFlows(dynCallSource, dynFrameworkParameter)
      if defaultTagsMode
      then
        flowSlices += atom.tag
            .name(FRAMEWORK_TAG)
            .call
            .argument
            .reachableByFlows(dynFrameworkParameter, sourceP)
        if dynFrameworkParameter.isEmpty then
          // Too slow
          flowSlices += dynSink
              .reachableByFlows(dynCallSource, dynFrameworkIdentifier)
          flowSlices += atom.tag
              .name(FRAMEWORK_TAG)
              .call
              .argument
              .isIdentifier
              .reachableByFlows(sourceI)
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
    end if
    if Array(Languages.PHP, Languages.RUBYSRC).contains(language)
    then
      flowSlices += atom.tag.name(FRAMEWORK_TAG).parameter.reachableByFlows(
        sourceP
      )
    if language == Languages.RUBYSRC && defaultTagsMode
    then
      flowSlices += atom.tag.name("pkg.*").call.argument.reachableByFlows(
        atom.tag.name(
          "pkg.*"
        ).call.method.repeat(_.filename(
          s"((app|config)${Pattern.quote(File.separator)})?(routes|controller(s)?|model(s)?|application).*\\.rb.*"
        ))(
          _.maxDepth(config.sliceDepth)
        ).parameter
      )
    if Array(Languages.NEWC, Languages.C).contains(language)
    then
      flowSlices += atom.tag.name(LIBRARY_CALL_TAG).call.reachableByFlows(atom.tag.name(
        s"(${CLI_SOURCE_TAG}|${DRIVER_SOURCE_TAG})"
      ).parameter)
      flowSlices += atom.tag.name(HTTP_TAG).parameter.reachableByFlows(atom.tag.name(
        s"(${CLI_SOURCE_TAG}|${HTTP_TAG})"
      ).parameter)
      if defaultTagsMode then
        if atomPossibleSinkTags.nonEmpty then
          flowSlices += atom.tag.name(s"(${atomPossibleSinkTags.slice(0, 5).mkString("|")})")
              .identifier.reachableByFlows(
                atom.tag.name(s"(${EVENT_TAG}|${CLI_SOURCE_TAG}|${HTTP_TAG})").parameter
              )
        println("Collecting additional slices with Reverse Reachability.")
        flowSlices += atom.tag.name(LIBRARY_CALL_TAG).call.reachableByFlows(
          atom.tag.name(
            LIBRARY_CALL_TAG
          ).call.method.repeat(_.caller(NoResolve))(
            _.maxDepth(config.sliceDepth)
          ).parameter
        )
        flowSlices += atom.tag.name(s"(${LIBRARY_CALL_TAG}|${HTTP_TAG})").parameter.reachableByFlows(
          atom.tag.name(
            s"(${LIBRARY_CALL_TAG}|${HTTP_TAG})"
          ).parameter.method.repeat(_.caller(NoResolve))(
            _.maxDepth(config.sliceDepth)
          ).parameter
        )
        flowSlices += atom.tag.name(LIBRARY_CALL_TAG).method
            .parameter.reachableByFlows(atom.tag.name(FRAMEWORK_INPUT_TAG).parameter)
        flowSlices += atom.tag.name(LIBRARY_CALL_TAG).identifier.inCall.argument.reachableByFlows(
          atom.method.internal.parameter
        )
      end if
    end if
    val slicesList = flowSlices.flatten.map(toSlice).toList
    if slicesList.isEmpty then
      println(
        s"No Reachable Flows identified for the given source tags: ${config.sourceTag} and sink tags: ${config.sinkTag}"
      )
      if atomSemanticTags.nonEmpty then
        println(
          s"List of semantic tags found in the atom file:\n${atomSemanticTags.mkString("\n")}"
        )
    ReachableSlice(slicesList)
  end calculateReachableSlice

  private def tagAsString(tag: Iterator[Tag]): String =
      if tag.nonEmpty then
        tag.name.filterNot(v => v.toUpperCase() == v && v.contains("_")).mkString(", ")
      else ""
  private def purlsFromTag(tag: Iterator[Tag]) =
      if tag.nonEmpty then tag.name.filter(_.startsWith("pkg:")).toSet else Set.empty

  private def toSlice(path: Path) =
    val tableRows  = ArrayBuffer[SliceNode]()
    val addedPaths = mutable.Set[String]()
    val purls      = mutable.Set[String]()
    path.elements.foreach { astNode =>
      val lineNumber   = astNode.lineNumber.map(_.intValue())
      val fileName     = astNode.file.name.headOption.getOrElse("").replace("<unknown>", "")
      var fileLocation = s"$fileName#$lineNumber"
      var tags: String = tagAsString(astNode.tag)
      purls ++= purlsFromTag(astNode.tag)
      if fileLocation == "#" then fileLocation = "N/A"
      var sliceNode = SliceNode(
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
            val methodName = methodParameterIn.method.name
            if tags.isEmpty && methodParameterIn.method.tag.nonEmpty then
              tags = tagAsString(methodParameterIn.method.tag)
              purls ++= purlsFromTag(methodParameterIn.method.tag)
            if tags.isEmpty && methodParameterIn.tag.nonEmpty then
              tags = tagAsString(methodParameterIn.tag)
              purls ++= purlsFromTag(methodParameterIn.tag)
            sliceNode = sliceNode.copy(
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
            tableRows += sliceNode
        case ret: Return =>
            val methodName = ret.method.name
            sliceNode = sliceNode.copy(
              name = ret.argumentName.getOrElse(""),
              code = ret.code,
              parentMethodName = methodName,
              parentMethodSignature = ret.method.signature,
              parentPackageName = ret.method.location.packageName,
              parentClassName = ret.method.location.className,
              lineNumber = ret.lineNumber,
              columnNumber = ret.columnNumber
            )
            tableRows += sliceNode
        case literal: Literal =>
            val methodName = literal.method.name
            if tags.isEmpty && literal.inCall.nonEmpty && literal.inCall.head.tag.nonEmpty
            then
              tags = tagAsString(literal.inCall.head.tag)
              purls ++= purlsFromTag(literal.inCall.head.tag)
            if !addedPaths.contains(
                s"$fileName#$lineNumber"
              )
            then
              sliceNode = sliceNode.copy(
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
              tableRows += sliceNode
        case identifier: Identifier =>
            val methodName = identifier.method.name
            if tags.isEmpty && identifier.inCall.nonEmpty && identifier.inCall.head.tag.nonEmpty
            then
              tags = tagAsString(identifier.inCall.head.tag)
              purls ++= purlsFromTag(identifier.inCall.head.tag)
            if !addedPaths.contains(
                s"$fileName#$lineNumber"
              ) && identifier.inCall.nonEmpty
            then
              sliceNode = sliceNode.copy(
                name = identifier.name,
                code =
                    if identifier.inCall.nonEmpty then identifier.inCall.head.code
                    else identifier.code,
                parentMethodName = methodName,
                parentMethodSignature = identifier.method.signature,
                parentPackageName = identifier.method.location.packageName,
                parentClassName = identifier.method.location.className,
                lineNumber = identifier.lineNumber,
                columnNumber = identifier.columnNumber,
                tags = tags
              )
              tableRows += sliceNode
        case member: Member =>
            val methodName = "<not-in-method>"
            sliceNode = sliceNode.copy(
              name = member.name,
              code = member.code,
              parentMethodName = methodName
            )
            tableRows += sliceNode
        case call: Call =>
            if !call.code.startsWith("<operator") || !call.methodFullName.startsWith(
                "<operator"
              )
            then
              if
                tags.isEmpty && call.callee(NoResolve).nonEmpty && call
                    .callee(NoResolve)
                    .head
                    .isExternal && !call.methodFullName.startsWith(
                  "<operator"
                ) && !call.name
                    .startsWith("<operator") && !call.methodFullName.startsWith("new ")
              then
                tags = tagAsString(call.callee(NoResolve).head.tag)
                purls ++= purlsFromTag(call.callee(NoResolve).head.tag)
              var isExternal =
                  if
                    call.callee(NoResolve).nonEmpty && call.callee(
                      NoResolve
                    ).head.isExternal && !call.name
                        .startsWith("<operator") && !call.methodFullName.startsWith(
                      "new "
                    )
                  then true
                  else false
              if call.methodFullName.startsWith("<operator") then isExternal = false
              sliceNode = sliceNode.copy(
                name = call.name,
                fullName = if call.callee(NoResolve).nonEmpty then
                  call.callee(NoResolve).head.fullName
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
              tableRows += sliceNode
        case cfgNode: CfgNode =>
            val method = cfgNode.method
            if tags.isEmpty && method.tag.nonEmpty then
              tags = tagAsString(method.tag)
              purls ++= purlsFromTag(method.tag)
            val methodName = method.name
            val statement = cfgNode match
              case _: MethodParameterIn =>
                  if tags.isEmpty && method.parameter.tag.nonEmpty then
                    tags = tagAsString(method.parameter.tag)
                    purls ++= purlsFromTag(method.parameter.tag)
                  val paramsPretty =
                      method.parameter.toList.sortBy(_.index).map(_.code).mkString(", ")
                  s"$methodName($paramsPretty)"
              case _ =>
                  if tags.isEmpty && cfgNode.statement.tag.nonEmpty then
                    tags = tagAsString(cfgNode.statement.tag)
                    purls ++= purlsFromTag(cfgNode.statement.tag)
                  cfgNode.statement.repr
            sliceNode =
                sliceNode.copy(parentMethodName = methodName, code = statement, tags = tags)
            tableRows += sliceNode
      end match
      addedPaths += s"$fileName#$lineNumber"
    }
    ReachableFlows(flows = tableRows.toList, purls = purls.toSet)
  end toSlice
end ReachableSlicing
