package io.appthreat.atom.slicing

import better.files.File
import io.appthreat.atom.Atom.{DEFAULT_SINK_TAGS, DEFAULT_SOURCE_TAGS, FRAMEWORK_INPUT_TAG}
import io.appthreat.dataflowengineoss.DefaultSemantics
import io.appthreat.dataflowengineoss.language.*
import io.appthreat.dataflowengineoss.queryengine.{EngineConfig, EngineContext}
import io.appthreat.dataflowengineoss.queryengine.summaries.{FlowSummaryComputer, FlowSummaryTags}
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

  implicit val semantics: Semantics = DefaultSemantics()
  // Reassigned per run to carry the method flow summaries computed for this atom (the backward query
  // engine uses them to prune provably empty cross-call work). The reaching-def engine choice (Flux
  // vs classic) is made earlier at enhancement time, not here. Reachable slicing runs one project at
  // a time, so a run-scoped var is safe.
  implicit var context: EngineContext = EngineContext(semantics, EngineConfig())

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

  // JVM/Android privacy egress: sensitive data on the device flowing out to known
  // trackers, adware and internet-facing services (cloud, AI/LLM, social, ...), or
  // into a local/on-device AI model (on-device-ai).
  private val SENSITIVE_SOURCE_TAG = "(sensitive-data|pii|phi-.*|pci-.*)"
  private val EGRESS_SINK_TAG      = "(service-egress|on-device-ai|tracker|adware)"

  // JVM/Android remote ingress: content fetched from a remote service onto the device
  // (download / read response body) reaching a device-side sink (file write, code
  // execution, deserialization, reflection, framework output, sql).
  private val INGRESS_SOURCE_TAG = "service-ingress"
  private val INGRESS_SINK_TAG =
      "(file-io|code-execution|reflection|serialization|sql|framework-output)"

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
    val baseFile = File(outputBasePath)
    Option(baseFile.parent).foreach(_.createDirectoryIfNotExists(createParents = true))
    val language = atom.metaData.language.head
    // Build method flow summaries up front when requested, so the backward query engine can prune
    // cross-call tasks that provably carry no taint. Prefer the `flow-summary` tags already embedded
    // in the atom (written during enhancement, so they survive a reused/cached atom); only fall back
    // to computing/loading from the JSON sidecar when the atom carries no tags.
    val summaries =
        if config.useSummaries then
          val tagged = FlowSummaryTags.fromCpg(atom)
          if tagged.nonEmpty then tagged
          else
            val cacheDir =
                Option(new JFile(outputBasePath).getAbsoluteFile.getParent).getOrElse(".")
            FlowSummaryComputer.loadOrCompute(atom, cacheDir, semantics)
        else Map.empty
    context = EngineContext(
      semantics,
      EngineConfig(
        useSummaries = config.useSummaries,
        summaries = summaries
      )
    )
    // The various collectors overlap (e.g. a default-tag flow may also be a privacy flow), so the
    // same path can be produced more than once. Deduplicate paths by their node-id signature, and
    // also drop paths that are a contiguous sub-sequence of an already-seen (longer) path sharing
    // the same source and sink, which removes near-duplicate partial flows.
    val seenSignatures = scala.collection.mutable.HashSet.empty[String]
    val seenEndpoints  = scala.collection.mutable.HashSet.empty[String]
    // Calls tagged as sanitisers/validators (by ChennaiTagsPass) and the sink categories each
    // covers. A flow that passes through such a call is dropped for the matching categories.
    val sanitizerCalls = collectSanitizerCalls(atom)
    val flowIterator = collectFlowSlices(atom, config, language)
        .iterator
        .flatten
        .filterNot(isSanitized(_, sanitizerCalls))
        .filter(isUniqueFlow(_, seenSignatures, seenEndpoints))
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

    val privacyFlows = collectJvmDataPrivacyFlows(atom, language)
    val ingressFlows = collectJvmRemoteIngressFlows(atom, language)

    basicFlows ++ defaultFlows ++ cryptoFlows ++ languageFlows ++ privacyFlows ++ ingressFlows
  end collectFlowSlices

  /** Collects flows where sensitive/PII data on the device reaches a data-egress sink (trackers,
    * adware, or internet-facing service SDKs). Only runs for JVM/Android frontends, where the
    * [[io.appthreat.x2cpg.passes.taggers.PiiTagsPass]], `TrackersTagsPass` and
    * `AndroidServicesTagsPass` produce the relevant tags.
    */
  private def collectJvmDataPrivacyFlows(
    atom: Cpg,
    language: String
  ): Iterator[Iterator[Path]] =
    if !JVM_BASED_LANGUAGES.contains(language) then return Iterator.empty

    def sensitiveLiterals    = atom.tag.name(SENSITIVE_SOURCE_TAG).literal
    def sensitiveIdentifiers = atom.tag.name(SENSITIVE_SOURCE_TAG).identifier
    def sensitiveParameters  = atom.tag.name(SENSITIVE_SOURCE_TAG).parameter

    def fromSensitive(sinks: Traversal[CfgNode]) =
        sinks.reachableByFlows(sensitiveLiterals, sensitiveIdentifiers, sensitiveParameters)

    Iterator(
      fromSensitive(atom.tag.name(EGRESS_SINK_TAG).call),
      fromSensitive(atom.tag.name(EGRESS_SINK_TAG).call.argument.isIdentifier),
      fromSensitive(atom.tag.name(EGRESS_SINK_TAG).call.argument.isLiteral),
      fromSensitive(atom.tag.name(EGRESS_SINK_TAG).parameter)
    )
  end collectJvmDataPrivacyFlows

  /** Collects ingress flows where remote content fetched onto the device (tagged `service-ingress`
    * by the `AndroidServicesTagsPass` on HTTP/cloud data-receiving calls) reaches a device-side
    * sink: file write, code execution, deserialization, reflection, framework output or SQL. This
    * models remote-content -> device flows (download / fetch), the inverse of the device -> egress
    * privacy flows. Only runs for JVM/Android frontends.
    */
  private def collectJvmRemoteIngressFlows(
    atom: Cpg,
    language: String
  ): Iterator[Iterator[Path]] =
    if !JVM_BASED_LANGUAGES.contains(language) then return Iterator.empty

    def ingressSources = atom.tag.name(INGRESS_SOURCE_TAG).call

    def toDeviceSink(sinks: Traversal[CfgNode]) = sinks.reachableByFlows(ingressSources)

    Iterator(
      toDeviceSink(atom.tag.name(INGRESS_SINK_TAG).call),
      toDeviceSink(atom.tag.name(INGRESS_SINK_TAG).call.argument.isIdentifier),
      toDeviceSink(atom.tag.name(INGRESS_SINK_TAG).parameter)
    )

  /** Returns true if the path is worth keeping. Drops:
    *   - exact duplicates (same node-id sequence), which arise because collectors overlap, and
    *   - same-endpoint, same-length variants (typically SSA/operator noise around an identical
    *     source -> sink flow), keeping the first one seen.
    *
    * Paths of differing length between the same endpoints are retained as distinct flows.
    */
  private def isUniqueFlow(
    path: Path,
    seenSignatures: scala.collection.mutable.HashSet[String],
    seenEndpoints: scala.collection.mutable.HashSet[String]
  ): Boolean =
    val elems = path.elements
    if elems.isEmpty then false
    else
      val signature = elems.map(_.id()).mkString("-")
      if !seenSignatures.add(signature) then false
      else
        val endpointKey = s"${elems.head.id()}->${elems.last.id()}#${elems.size}"
        seenEndpoints.add(endpointKey)

  private val SANITIZER_TAG             = "sanitizer"
  private val SANITIZER_CATEGORY_PREFIX = "sanitizer-"

  /** Call sites tagged as sanitisers/validators, mapped to the sink categories each one covers (an
    * empty set means it covers every category).
    */
  private def collectSanitizerCalls(atom: Cpg): Map[Long, Set[String]] =
      atom.call.where(_.tag.name(SANITIZER_TAG)).map { call =>
        val categories = call.tag.name.l.collect {
            case name if name.startsWith(SANITIZER_CATEGORY_PREFIX) =>
                name.stripPrefix(SANITIZER_CATEGORY_PREFIX)
        }.toSet
        call.id() -> categories
      }.toMap

  /** True if the flow passes through a sanitiser whose categories cover the flow's sink. A flow is
    * sanitised when it touches a sanitiser call (or an argument of one) that either covers every
    * category or shares a category with the flow's sink.
    */
  private def isSanitized(path: Path, sanitizerCalls: Map[Long, Set[String]]): Boolean =
      if sanitizerCalls.isEmpty then false
      else
        val sinkCategories = flowSinkCategories(path)
        path.elements.exists { element =>
            (element.id() :: enclosingCallId(element).toList).exists { id =>
                sanitizerCalls.get(id).exists { categories =>
                    categories.isEmpty || categories.exists(sinkCategories.contains)
                }
            }
        }

  private def enclosingCallId(node: AstNode): Option[Long] =
      node match
        case expr: Expression => expr.inCall.headOption.map(_.id())
        case _                => None

  /** The categories of a flow's sink, taken from the tags on the last path element and its
    * enclosing call.
    */
  private def flowSinkCategories(path: Path): Set[String] =
      path.elements.lastOption.toList.flatMap { sink =>
        val callTags = sink match
          case expr: Expression => expr.inCall.tag.name.l
          case _                => Nil
        sink.tag.name.l ++ callTags
      }.toSet

  private def collectBasicFlows(
    atom: Cpg,
    sourceTagRegex: String,
    sinkTagRegex: String
  ): Iterator[Path] =
    def sourcesP = atom.tag.name(sourceTagRegex).parameter
    def sourcesI = atom.tag.name(sourceTagRegex).identifier
    def sourcesC = atom.tag.name(sourceTagRegex).call

    def flowsFrom(sinks: Traversal[CfgNode]) = sinks.reachableByFlows(sourcesP, sourcesI, sourcesC)

    Iterator(
      flowsFrom(atom.tag.name(sinkTagRegex).call),
      flowsFrom(atom.tag.name(sinkTagRegex).identifier),
      flowsFrom(atom.tag.name(sinkTagRegex).call.argument.isIdentifier),
      flowsFrom(atom.tag.name(sinkTagRegex).parameter),
      flowsFrom(atom.ret.where(_.tag.name(sinkTagRegex)))
    ).flatten

  private def collectDefaultTagFlows(atom: Cpg, sourceTagRegex: String): List[Iterator[Path]] =
      List(
        atom.ret.where(_.method.tag.name(sourceTagRegex)).reachableByFlows(
          atom.tag.name(sourceTagRegex).parameter,
          atom.tag.name(sourceTagRegex).identifier,
          atom.tag.name(sourceTagRegex).call
        ),
        atom.tag.name(API_TAG).parameter.reachableByFlows(
          atom.tag.name(API_TAG).parameter,
          atom.tag.name(API_TAG).identifier
        )
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
    val dynCallAllArgSource    = atom.tag.name(sourceTagRegex).call.argument
    val dynFrameworkIdentifier = atom.tag.name(s"($FRAMEWORK_TAG|$FRAMEWORK_INPUT_TAG)").identifier
    val dynFrameworkParameter  = atom.tag.name(s"($FRAMEWORK_TAG|$FRAMEWORK_INPUT_TAG)").parameter
    val dynSink                = atom.tag.name(sinkTagRegex).call.argument.isIdentifier
    val dynSinkMethodCallIn    = atom.tag.name(sinkTagRegex).method.callIn(using NoResolve)
    val flow1                  = dynSink.reachableByFlows(dynCallSource, dynFrameworkParameter)
    val dynCallInFlows =
        dynSinkMethodCallIn.reachableByFlows(
          dynCallAllArgSource
        )
    val defaultFlows = if defaultTagsMode then
      val f1 =
          atom.tag.name(s"($FRAMEWORK_TAG|$FRAMEWORK_INPUT_TAG)").call.argument.reachableByFlows(
            dynFrameworkParameter,
            atom.tag.name(sourceTagRegex).parameter
          )
      val f2 = if dynFrameworkParameter.isEmpty then
        Iterator(
          dynSink.reachableByFlows(dynCallSource, dynFrameworkIdentifier),
          atom.tag.name(s"($FRAMEWORK_TAG|$FRAMEWORK_INPUT_TAG)").call.argument.isIdentifier.reachableByFlows(
            atom.tag.name(sourceTagRegex).identifier
          )
        )
      else Iterator.empty
      Iterator(f1) ++ f2
    else Iterator.empty

    val pythonFlows = collectPythonSpecificFlows(atom, language, defaultTagsMode, sinkTagRegex)

    Iterator(dynCallInFlows) ++ Iterator(flow1) ++ defaultFlows ++ pythonFlows
  end collectDynamicLanguageFlows

  private def collectPythonSpecificFlows(
    atom: Cpg,
    language: String,
    defaultTagsMode: Boolean,
    sinkTagRegex: String
  ): Iterator[Iterator[Path]] =
      Iterator.empty

  private def collectPHPRubyFlows(
    atom: Cpg,
    language: String,
    sourceTagRegex: String
  ): Iterator[Iterator[Path]] =
      if PHP_RUBY_LANGUAGES.contains(language) then
        Iterator(atom.tag.name(s"($FRAMEWORK_TAG|$FRAMEWORK_INPUT_TAG)").parameter.reachableByFlows(
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
            s"($EVENT_TAG|$CLI_SOURCE_TAG|$HTTP_TAG|$FRAMEWORK_INPUT_TAG)"
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
