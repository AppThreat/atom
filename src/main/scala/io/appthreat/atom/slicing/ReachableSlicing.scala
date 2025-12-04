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

import java.io.{BufferedWriter, FileWriter, File as JFile}
import java.util.regex.Pattern
import io.circe.generic.auto.*
import io.circe.syntax.*

object ReachableSlicing:

  implicit val semantics: Semantics      = DefaultSemantics()
  private val engineConfig: EngineConfig = EngineConfig()
  implicit val context: EngineContext    = EngineContext(semantics, engineConfig)

  private val API_TAG              = "api"
  private val FRAMEWORK_TAG        = "framework"
  private val LIBRARY_CALL_TAG     = "library-call"
  private val CLI_SOURCE_TAG       = "cli-source"
  private val DRIVER_SOURCE_TAG    = "driver-source"
  private val HTTP_TAG             = "http"
  private val EVENT_TAG            = "event"
  private val PARSE_TAG            = "parse"
  private val CRYPTO_GENERATE_TAG  = "crypto-generate"
  private val CRYPTO_ALGORITHM_TAG = "crypto-algorithm"
  private val PURL_PREFIX          = "pkg.*"

  private val JVM_BASED_LANGUAGES =
      Set(Languages.JAVA, Languages.JAVASRC, "JAR", "JIMPLE", "ANDROID", "APK", "DEX")
  private val DYNAMIC_LANGUAGES =
      Set(
        Languages.JSSRC,
        Languages.JAVASCRIPT,
        Languages.PYTHON,
        Languages.PYTHONSRC,
        Languages.RUBYSRC
      )
  private val PHP_RUBY_LANGUAGES =
      Set(Languages.PHP, Languages.RUBYSRC)
  private val C_LANGUAGES =
      Set(Languages.NEWC, Languages.C)
  private val PYTHON_LANGUAGES =
      Set(Languages.PYTHON, Languages.PYTHONSRC)

  private val DEFAULT_CHUNK_SIZE = 1000

  def calculateReachableSliceAndPersist(
    atom: Cpg,
    config: ReachablesConfig,
    outputBasePath: String,
    chunkSize: Int = DEFAULT_CHUNK_SIZE
  ): Unit =
    val language = atom.metaData.language.head
    val flowIterator = collectFlowSlices(atom, config, language)
        .iterator
        .flatten
        .map(toSlice)

    val chunkedIterator = flowIterator.grouped(chunkSize).zipWithIndex
    var hasFlows        = false

    chunkedIterator.foreach { case (chunk, index) =>
        hasFlows = true
        val fileName =
            if index == 0 then s"$outputBasePath.json" else s"${outputBasePath}_$index.json"
        File(fileName).writeText(chunk.asJson.noSpaces)
    }

    if !hasFlows then
      handleEmptySlices(atom, config)
      File(s"$outputBasePath.json").writeText("[]")
  end calculateReachableSliceAndPersist

  private def collectFlowSlices(
    atom: Cpg,
    config: ReachablesConfig,
    language: String
  ): Iterator[Iterator[Path]] =
    val defaultTagsMode =
        config.sourceTag == DEFAULT_SOURCE_TAGS && config.sinkTag == DEFAULT_SINK_TAGS
    val sourceTagRegex = raw"""(${config.sourceTag.mkString("|")})"""
    val sinkTagRegex   = raw"""(${config.sinkTag.mkString("|")})"""

    val basicFlows = Iterator(collectBasicFlows(atom, sourceTagRegex, sinkTagRegex))

    val defaultFlows = if defaultTagsMode then
      collectDefaultTagFlows(atom, sourceTagRegex).iterator
    else Iterator.empty

    val cryptoFlows = if config.includeCryptoFlows then
      collectCryptoFlows(atom, language).iterator
    else Iterator.empty

    val languageFlows = collectLanguageSpecificFlows(
      atom,
      config,
      language,
      defaultTagsMode,
      sourceTagRegex,
      sinkTagRegex
    )

    basicFlows ++ defaultFlows ++ cryptoFlows ++ languageFlows
  end collectFlowSlices

  private def collectBasicFlows(
    atom: Cpg,
    sourceTagRegex: String,
    sinkTagRegex: String
  ): Iterator[Path] =
    val sourceP     = atom.tag.name(sourceTagRegex).parameter
    val sourceI     = atom.tag.name(sourceTagRegex).identifier
    val identiSinks = atom.tag.name(sinkTagRegex).call.argument.isIdentifier
    val retSinks    = atom.ret.where(_.tag.name(sinkTagRegex))
    identiSinks.reachableByFlows(sourceP, sourceI) ++ retSinks.reachableByFlows(sourceP, sourceI)

  private def collectDefaultTagFlows(atom: Cpg, sourceTagRegex: String): List[Iterator[Path]] =
      List(
        atom.ret.where(_.method.tag.name(sourceTagRegex)).reachableByFlows(
          atom.tag.name(sourceTagRegex).parameter,
          atom.tag.name(sourceTagRegex).identifier
        ),
        atom.tag.name(API_TAG).parameter.reachableByFlows(atom.tag.name(API_TAG).parameter)
      )

  private def collectCryptoFlows(atom: Cpg, language: String): List[Iterator[Path]] =
      if JVM_BASED_LANGUAGES.contains(language) then
        List(atom.tag.name(CRYPTO_GENERATE_TAG).call.reachableByFlows(
          atom.tag.name(CRYPTO_ALGORITHM_TAG).literal
        ))
      else if PYTHON_LANGUAGES.contains(language) then
        List(atom.tag.name(CRYPTO_GENERATE_TAG).call.reachableByFlows(
          atom.tag.name(CRYPTO_ALGORITHM_TAG).call
        ))
      else
        List.empty

  private def collectLanguageSpecificFlows(
    atom: Cpg,
    config: ReachablesConfig,
    language: String,
    defaultTagsMode: Boolean,
    sourceTagRegex: String,
    sinkTagRegex: String
  ): Iterator[Iterator[Path]] =
    val dynamic = collectDynamicLanguageFlows(
      atom,
      config,
      language,
      defaultTagsMode,
      sourceTagRegex,
      sinkTagRegex
    )
    val phpRuby = collectPHPRubyFlows(atom, language, sourceTagRegex)
    val ruby    = collectRubySpecificFlows(atom, language, defaultTagsMode, config)
    val cLang   = collectCLanguageFlows(atom, language, defaultTagsMode, config)

    dynamic ++ phpRuby ++ ruby ++ cLang
  end collectLanguageSpecificFlows

  private def collectDynamicLanguageFlows(
    atom: Cpg,
    config: ReachablesConfig,
    language: String,
    defaultTagsMode: Boolean,
    sourceTagRegex: String,
    sinkTagRegex: String
  ): Iterator[Iterator[Path]] =
    if !DYNAMIC_LANGUAGES.contains(language) then return Iterator.empty

    val dynCallSource          = atom.tag.name(sourceTagRegex).call.argument.isIdentifier
    val dynFrameworkIdentifier = atom.tag.name(FRAMEWORK_TAG).identifier
    val dynFrameworkParameter  = atom.tag.name(FRAMEWORK_TAG).parameter
    val dynSink                = atom.tag.name(sinkTagRegex).call.argument.isIdentifier

    val flow1 = dynSink.reachableByFlows(dynCallSource, dynFrameworkParameter)

    val defaultFlows = if defaultTagsMode then
      val f1 = atom.tag.name(FRAMEWORK_TAG).call.argument.reachableByFlows(
        dynFrameworkParameter,
        atom.tag.name(sourceTagRegex).parameter
      )
      val f2 = if dynFrameworkParameter.isEmpty then
        Iterator(
          dynSink.reachableByFlows(dynCallSource, dynFrameworkIdentifier),
          atom.tag.name(FRAMEWORK_TAG).call.argument.isIdentifier.reachableByFlows(
            atom.tag.name(sourceTagRegex).identifier
          )
        )
      else Iterator.empty
      Iterator(f1) ++ f2
    else Iterator.empty

    val pythonFlows = collectPythonSpecificFlows(atom, language, defaultTagsMode, sinkTagRegex)

    Iterator(flow1) ++ defaultFlows ++ pythonFlows
  end collectDynamicLanguageFlows

  private def collectPythonSpecificFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    sinkTagRegex: String
  ): Iterator[Iterator[Path]] =
    if !PYTHON_LANGUAGES.contains(language) then return Iterator.empty

    val flows = if defaultTagsMode then
      if atom.tag.name(CLI_SOURCE_TAG).identifier.nonEmpty then
        atom.tag.name(PURL_PREFIX).identifier.reachableByFlows(
          atom.tag.name(CLI_SOURCE_TAG).identifier
        )
      else
        atom.tag.name(PURL_PREFIX).identifier.reachableByFlows(
          atom.tag.name(FRAMEWORK_TAG).parameter
        )
    else
      atom.tag.name(PURL_PREFIX).identifier.reachableByFlows(
        atom.tag.name(CLI_SOURCE_TAG).call
      )
    Iterator(flows)
  end collectPythonSpecificFlows

  private def collectPHPRubyFlows(
    atom: Cpg,
    language: String,
    sourceTagRegex: String
  ): Iterator[Iterator[Path]] =
      if PHP_RUBY_LANGUAGES.contains(language) then
        Iterator(atom.tag.name(FRAMEWORK_TAG).parameter.reachableByFlows(
          atom.tag.name(sourceTagRegex).parameter
        ))
      else Iterator.empty

  private def collectRubySpecificFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    config: ReachablesConfig
  ): Iterator[Iterator[Path]] =
      if language == Languages.RUBYSRC && defaultTagsMode then
        Iterator(atom.tag.name(PURL_PREFIX).call.argument.reachableByFlows(
          atom.tag.name(PURL_PREFIX).call.method.repeat(_.filename(
            s"((app|config)${Pattern.quote(JFile.separator)})?(routes|controller(s)?|model(s)?|application).*\\.rb.*"
          ))(using _.maxDepth(config.sliceDepth)).parameter
        ))
      else Iterator.empty

  private def collectCLanguageFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    config: ReachablesConfig
  ): Iterator[Iterator[Path]] =
    if !C_LANGUAGES.contains(language) then return Iterator.empty

    val basic = collectCBasicFlows(atom).iterator
    val defaults =
        if defaultTagsMode then collectCDefaultFlows(atom, config).iterator else Iterator.empty
    basic ++ defaults

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

    val sinkFlows = if atomPossibleSinkTags.nonEmpty then
      List(atom.tag.name(s"(${atomPossibleSinkTags.slice(0, 5).mkString("|")})")
          .identifier.reachableByFlows(atom.tag.name(
            s"($EVENT_TAG|$CLI_SOURCE_TAG|$HTTP_TAG)"
          ).parameter))
    else List.empty

    println("Collecting additional slices with Reverse Reachability.")
    val reverseFlows = List(
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

    sinkFlows ++ reverseFlows
  end collectCDefaultFlows

  private def handleEmptySlices(atom: Cpg, config: ReachablesConfig): Unit =
    println(
      s"No Reachable Flows identified for the given source tags: ${config.sourceTag} and sink tags: ${config.sinkTag}"
    )
    val atomSemanticTags =
        atom.tag.name.filterNot(t => t.startsWith("pkg:") || t.toUpperCase().equals(t)).toSet
    if atomSemanticTags.nonEmpty then
      println(s"List of semantic tags found in the atom file:\n${atomSemanticTags.mkString("\n")}")

  private def resolveTagsAndPurls(
    node: AstNode,
    fallback: Option[AstNode] = None
  ): (String, Set[String]) =
    val nodeTags = node.tag.toList
    val effectiveTags =
        if nodeTags.isEmpty && fallback.isDefined then fallback.get.tag.toList else nodeTags

    val tagStr = if effectiveTags.nonEmpty then
      effectiveTags.map(_.name).filterNot(v => v.toUpperCase == v && v.contains("_")).mkString(", ")
    else ""

    val purls = effectiveTags.map(_.name).filter(_.startsWith("pkg:")).toSet
    (tagStr, purls)

  private def toSlice(path: Path): ReachableFlows =
    val (sliceNodes, purls, _) =
        path.elements.foldLeft((Vector.empty[SliceNode], Set.empty[String], Set.empty[String])) {
            case ((nodes, accPurls, visited), astNode) =>
                val (nodeOpt, nodePurls) = createSliceNode(astNode)
                val fileLoc = s"${astNode.file.name.headOption.getOrElse("")}#${astNode.lineNumber
                        .map(_.intValue()).getOrElse(0)}"

                val shouldAdd = nodeOpt.isDefined && (astNode match
                  case _: Literal | _: Identifier => !visited.contains(fileLoc)
                  case _                          => true
                )

                val newNodes = if shouldAdd then nodes :+ nodeOpt.get else nodes
                (newNodes, accPurls ++ nodePurls, visited + fileLoc)
        }
    ReachableFlows(flows = sliceNodes.toList, purls = purls)

  private def createSliceNode(astNode: AstNode): (Option[SliceNode], Set[String]) =
    val (tags, purls) = resolveTagsAndPurls(astNode)

    val baseNode = SliceNode(
      astNode.id(),
      astNode.label,
      code = astNode.code,
      parentFileName = astNode.file.name.headOption.getOrElse("").replace("<unknown>", ""),
      lineNumber = astNode.lineNumber,
      columnNumber = astNode.columnNumber,
      tags = tags
    )

    astNode match
      case m: MethodParameterIn =>
          val (finalTags, finalPurls) = resolveTagsAndPurls(m, Some(m.method))
          (
            Some(baseNode.copy(
              name = m.name,
              typeFullName = m.typeFullName,
              parentMethodName = m.method.name,
              parentMethodSignature = m.method.signature,
              parentPackageName = m.method.location.packageName,
              parentClassName = m.method.location.className,
              isExternal = m.method.isExternal,
              tags = finalTags
            )),
            finalPurls
          )

      case r: Return =>
          (
            Some(baseNode.copy(
              name = r.argumentName.getOrElse(""),
              parentMethodName = r.method.name,
              parentMethodSignature = r.method.signature,
              parentPackageName = r.method.location.packageName,
              parentClassName = r.method.location.className
            )),
            purls
          )

      case l: Literal =>
          val (finalTags, finalPurls) = if tags.isEmpty && l.inCall.nonEmpty then
            resolveTagsAndPurls(l, Some(l.inCall.head))
          else (tags, purls)

          (
            Some(baseNode.copy(
              name = l.code.replaceAll("""(['"])""", ""),
              code = l.code.replaceAll("""(['"])""", ""),
              typeFullName = l.typeFullName,
              parentMethodName = l.method.name,
              parentMethodSignature = l.method.signature,
              parentPackageName = l.method.location.packageName,
              parentClassName = l.method.location.className,
              tags = finalTags
            )),
            finalPurls
          )

      case i: Identifier =>
          val (finalTags, finalPurls) = if tags.isEmpty && i.inCall.nonEmpty then
            resolveTagsAndPurls(i, Some(i.inCall.head))
          else (tags, purls)

          if i.inCall.nonEmpty then
            (
              Some(baseNode.copy(
                name = i.name,
                code = i.inCall.head.code,
                parentMethodName = i.method.name,
                parentMethodSignature = i.method.signature,
                parentPackageName = i.method.location.packageName,
                parentClassName = i.method.location.className,
                tags = finalTags
              )),
              finalPurls
            )
          else (None, purls)

      case m: Member =>
          (Some(baseNode.copy(name = m.name, parentMethodName = "<not-in-method>")), purls)

      case c: Call =>
          if c.code.startsWith("<operator") || c.methodFullName.startsWith("<operator") then
            (None, purls)
          else
            val resolvedCallee = c.callee(using NoResolve).headOption
            val (finalTags, finalPurls) =
                if tags.isEmpty && resolvedCallee.exists(_.isExternal) && !c.methodFullName
                      .startsWith("new ")
                then
                  resolveTagsAndPurls(c, resolvedCallee)
                else (tags, purls)

            val isExternal =
                resolvedCallee.exists(_.isExternal) && !c.methodFullName.startsWith("new ")

            (
              Some(baseNode.copy(
                name = c.name,
                fullName = resolvedCallee.map(_.fullName).getOrElse(""),
                isExternal = isExternal,
                parentMethodName = c.method.name,
                parentMethodSignature = c.method.signature,
                parentPackageName = c.method.location.packageName,
                parentClassName = c.method.location.className,
                tags = finalTags
              )),
              finalPurls
            )

      case cfg: CfgNode =>
          val (finalTags, finalPurls) = resolveTagsAndPurls(cfg, Some(cfg.method))
          val method                  = cfg.method
          val stmtStr = cfg match
            case _: MethodParameterIn =>
                val paramsPretty =
                    method.parameter.toList.sortBy(_.index).map(_.code).mkString(", ")
                s"${method.name}($paramsPretty)"
            case _ => cfg.statement.repr

          (
            Some(baseNode.copy(
              parentMethodName = method.name,
              code = stmtStr,
              tags = finalTags
            )),
            finalPurls
          )

      case _ => (Some(baseNode), purls)
    end match
  end createSliceNode
end ReachableSlicing
