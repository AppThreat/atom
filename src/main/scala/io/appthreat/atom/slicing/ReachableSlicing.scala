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

  private val logger = org.slf4j.LoggerFactory.getLogger(getClass)

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
      "(file-io|code-execution|reflection|serialization|unsafe-deserialization|sql|framework-output)"

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
    // Materialise the graph's lazily-loaded state once, single-threaded, before
    // the parallel query engine reads it concurrently. The engine solves on a thread pool, and a
    // graph fresh off disk still has lazily-deserialized adjacency: threads racing to force the
    // same adjacency could transiently fail (a dropped task, a dropped source) or silently miss
    // edges - a different handful of lost paths per run and, measured on one fixture, a
    // 2888-2937 reachables spread from concurrency alone. Walking every node's edges here makes
    // the lazy loads happen exactly once, deterministically; the parallel engine then races
    // nothing. The cost is proportional to edges and is logged (debug) rather than asserted
    // here - it was sub-second on a 28k-node graph, which says nothing useful about an atom two
    // orders of magnitude larger, and the log is what an operator chasing slicing time needs.
    warmGraph(atom)
    // The various collectors overlap (e.g. a default-tag flow may also be a privacy flow), so the
    // same path can be produced more than once; the uniqueness filter below (canonical survivor
    // per endpoint group + exact-signature suppression) removes the duplicates order-independently.
    // Calls tagged as sanitisers/validators (by ChennaiTagsPass) and the sink categories each
    // covers. A flow that passes through such a call is dropped for the matching categories.
    val sanitizerCalls = collectSanitizerCalls(atom)
    // Profile-driven neutraliser barriers (validators, sanitisers, encoders, ORM reads). A flow that
    // passes through any of these node ids is dropped. Empty for the default `generic` profile.
    val neutralizerNodeIds = collectNeutralizerNodeIds(atom, config.profile.neutralizerTags)
    // One pattern matching any tag name this run treats as a source or sink. Rendered paths keep
    // operator calls carrying one of these tags (see createSliceNode): Python lowers sources such
    // as `request.form["n"]` to `<operator>.indexAccess`, so an untagged-operator-only filter
    // would silently unroot exactly those flows.
    val sourceSinkTagPattern = Pattern.compile(
      s"(${(config.sourceTag ++ config.sinkTag).mkString("|")})"
    )
    val sinkTagPattern = Pattern.compile(s"(${config.sinkTag.mkString("|")})")
    // Materialise BEFORE the stateful filters. The collectors and the query
    // engine underneath produce the same SET of paths on a given graph, but their arrival ORDER
    // is not deterministic (the engine's virtual-thread task pool completes in scheduling order,
    // so its result table order varies run to run). A streaming first-wins uniqueness filter
    // then keeps a different representative of each same-endpoint group per run, and the
    // downstream containment dedup absorbs a different number of entries - a measured 2888-2937
    // spread on one fixture from ordering alone. Selecting survivors canonically (minimum
    // id-signature per endpoint group) makes the SET of emitted entries independent of arrival
    // order.
    //
    // What this does NOT do, stated plainly: the emitted ORDER still follows arrival, so an
    // output file is only byte-stable where arrival is. That holds on the small gates
    // (express-sample is byte-identical) and does not on a large python target, where the count
    // is now stable but the entry order need not be. And the canonical choice is not the old
    // choice: java-sec-code keeps its 1195 entries with 2 of them swapped for the other
    // truncation variant of one `XSSFCell -> cell` finding. That is the fix working - the old
    // variant was whichever the scheduler delivered first - but it is a real output change, and
    // a claim of byte-identity there would be false.
    val collectedPaths = collectFlowSlices(atom, config, language)
        .iterator
        .flatten
        .filterNot(isLibrarySourcedFlow)
        .filterNot(isSanitized(_, sanitizerCalls))
        .filterNot(passesThroughNeutralizer(_, neutralizerNodeIds))
        .filterNot(isMetaClassAdapterFlow)
        .filterNot(endsAtNonSink)
        .toVector
    // Each path's endpoint key and signature, computed ONCE. Both are O(path length) string
    // builds over every collected path, and the group fold plus the emit filter each need both -
    // deriving them twice doubled that work on the largest inputs, which is where this whole
    // code path is slow to begin with.
    val keyed: Vector[(Option[String], String, Path)] =
        collectedPaths.map(path => (endpointKeyOf(path), pathSignatureOf(path), path))
    // The canonical (minimum) signature per endpoint group, computed from the whole materialised
    // collection order-independently - which path of a group survives no longer depends on
    // collector or engine scheduling. A mutable map because this runs over every collected path
    // and an immutable `updated` fold rebuilt the map once per entry.
    val canonicalSurvivor = scala.collection.mutable.HashMap.empty[String, String]
    keyed.foreach { case (keyOpt, sig, _) =>
        keyOpt.foreach { key =>
          val current = canonicalSurvivor.get(key)
          if current.isEmpty || sig < current.get then canonicalSurvivor.update(key, sig)
        }
    }
    // Debug hook (CHEN_SLICE_DEBUG=<dir>): the sorted signature multiset of the collected
    // paths - a fingerprint of the ENGINE's output set, independent of ordering and of every
    // filter below it. Diffing this across runs on one fixed atom answers "does the engine
    // itself vary?" without touching anything else.
    Option(System.getenv("CHEN_SLICE_DEBUG")).filter(_.nonEmpty).foreach { dir =>
      // The directory is created: a debug hook that throws because the operator did not
      // pre-create the path is a diagnostic that costs a whole run to learn.
      val target = better.files.File(dir).createDirectoryIfNotExists(createParents = true)
      (target / "collected-sigs.txt").write(keyed.map(_._2).sorted.mkString("\n"))
    }
    val seenSignatures = scala.collection.mutable.HashSet.empty[String]
    val flowIterator = keyed.iterator
        .filter { case (keyOpt, signature, _) =>
            // Keep the canonical representative of each endpoint group, once. `seenSignatures`
            // still earns its place: two collectors can yield the identical path, and both
            // copies carry the group's surviving signature.
            keyOpt.exists(key => canonicalSurvivor.get(key).contains(signature)) &&
            seenSignatures.add(signature)
        }
        .map { case (_, _, path) => toSlice(path, sourceSinkTagPattern) }
        // Backstop: never emit an entry with no evidence. A path whose every element renders to
        // None (untagged operator calls, bare identifiers) carries a purl attribution at best and
        // no flow at all - consumers that count entries would count it as a real finding.
        .filter(_.flows.nonEmpty)

    // Canonicalise before writing: the collectors query overlapping source/sink sets, so one
    // finding routinely arrives as several entries - the path, a truncation of it (ending at an
    // argument or starting at an intermediate), and the same path again from another collector.
    // Deduplicate on the canonical form (see deduplicateFlows) so each finding is emitted once.
    val canonicalFlows = deduplicateFlows(flowIterator.toVector, sinkTagPattern)

    val chunkedIterator = canonicalFlows.grouped(chunkSize).zipWithIndex
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

  /** Force every node's in/out edges (and their endpoints) to materialise, single-threaded. See the
    * call site for why this precedes the parallel query engine.
    */
  private def warmGraph(atom: Cpg): Unit =
    val started   = System.nanoTime()
    var nodeCount = 0L
    // Counted, and the count logged. Two reasons, neither cosmetic: the endpoint reads exist
    // ONLY for their side effect (forcing a lazy deserialization), and a result nothing observes
    // is the shape a compiler is entitled to optimise away - accumulating it makes the work
    // load-bearing. And the cost of this walk is proportional to edges, so an operator chasing
    // slicing time on a large atom needs the number rather than an assurance.
    var edgeCount = 0L
    val nodes     = atom.graph.nodes()
    while nodes.hasNext do
      val node = nodes.next()
      nodeCount += 1
      val outE = node.outE()
      while outE.hasNext do
        val e = outE.next()
        if e.inNode() != null then edgeCount += 1
        e.outNode()
      val inE = node.inE()
      while inE.hasNext do
        val e = inE.next()
        if e.outNode() != null then edgeCount += 1
        e.inNode()
    logger.debug(
      s"warmGraph materialised $nodeCount nodes / $edgeCount edge endpoints in " +
          s"${(System.nanoTime() - started) / 1000000L} ms"
    )
  end warmGraph

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

  /** True for a flow whose SOURCE - its first element - lives in library code. A finding must be
    * rooted in the analyzed project: with dependency bodies in the graph (`--frontend-args
    * python-deps=full`), flows sourced inside the library (flask internals tagged framework-input,
    * urllib3 internals tagged http, ...) exist by the thousand and a reachables run that reports
    * them is unreadable - they are facts about the library, not findings about the project.
    * Exploration still traverses library code freely (a project-sourced flow through the library to
    * any sink survives); only reporting is scoped.
    *
    * A no-op wherever dependency code is not in the graph: sources are tagged nodes of the
    * project's own code there, and those methods are internal.
    */
  private def isLibrarySourcedFlow(path: Path): Boolean =
      path.elements.headOption.exists {
          case m: MethodParameterIn => m.method.isExternal
          case e: Expression        => e.method.isExternal
          case m: Method            => m.isExternal
          case _                    => false
      }

  /** The node-id sequence of a path, rendered as a comparable string. Two paths with the same
    * signature traverse the identical route and are interchangeable for every downstream filter and
    * renderer, which is what makes the signature usable both as the exact-duplicate key and as the
    * canonical survivor tie-break.
    */
  private def pathSignatureOf(path: Path): String =
      path.elements.map(_.id()).mkString("-")

  /** The uniqueness group of a path: same first node, same last node, same length. Historically the
    * first-arrived member of each group was kept; survivor selection is now the group's minimum
    * signature, so which member survives no longer depends on collector or engine scheduling. Paths
    * of differing length between the same endpoints are distinct groups and are both retained as
    * distinct flows. `None` for a path with no elements (never kept).
    */
  private def endpointKeyOf(path: Path): Option[String] =
      path.elements.headOption.zip(path.elements.lastOption).map { (head, last) =>
          s"${head.id()}->${last.id()}#${path.elements.size}"
      }

  /** Reduce the collected entries to one entry per finding, on a canonical form rather than by
    * ranking.
    *
    * The engine is queried once per (sink set, source set) combination, and those sets overlap by
    * design, so a single source -> sink finding typically arrives as several entries that differ
    * only in where they were cut: the path to the sink call, the path ending at one of the sink's
    * arguments, the same path truncated earlier in the source (starting at an intermediate instead
    * of the tagged source), and its continuation through the sink's return into the caller.
    *
    * Two steps, both canonical rather than ranked:
    *
    *   1. Terminus normalisation - a flow TERMINATES at its sink. An entry whose last node is not
    *      itself a sink is a continuation variant; it is cut right after the last sink-tagged CALL
    *      it traverses (calls only - never an argument, which would cut one node too early). An
    *      entry whose terminus IS a sink (e.g. data reaching a framework output after passing a sql
    *      call) is a distinct downstream finding and is left whole.
    *
    * 2. Containment dedup - entries are the SAME finding when one's rendered node sequence is a
    * contiguous subsequence of the other's (or equal): they traverse the identical route, just
    * reported at different truncation points. The kept representative is the maximal sequence of
    * each such group - not a ranking between findings, since there is only one route in the group
    * and the maximal form is the only one that shows both the tagged source and the sink. Distinct
    * routes - sequences not contained in one another - are always both kept.
    *
    * Entries with fewer than two rendered nodes, or whose first and last node are the same, are
    * zero-information (source == sink) and dropped outright.
    *
    * Deterministic regardless of collector/iterator order: candidates are processed longest-first
    * with the id-signature as tie-break, and entries with equal signatures render identically.
    */
  private def deduplicateFlows(
    entries: Vector[ReachableFlows],
    sinkTagPattern: Pattern
  ): Vector[ReachableFlows] =
    val normalised = entries.flatMap(normaliseTerminus(_, sinkTagPattern))
        .filter { entry =>
            entry.flows.lengthCompare(1) > 0 && entry.flows.head.id != entry.flows.last.id
        }
    if normalised.lengthCompare(2) < 0 then return normalised

    // "#id1#id2#" - the delimiters make `contains` a contiguous-node-subsequence test.
    def signature(entry: ReachableFlows): String =
        entry.flows.map(n => s"#${n.id}").mkString + "#"

    val signatures = normalised.map(signature)

    // A container of entry B necessarily contains B's head and terminus nodes, so indexing every
    // entry by every node id lets the containment check only consider entries reachable from B's
    // endpoints - without restricting where in the container the subsequence may sit.
    val entriesByNode = scala.collection.mutable.HashMap.empty[Long, List[Int]]
    normalised.indices.foreach { i =>
        entryNodeIds(normalised(i)).foreach { nodeId =>
            entriesByNode.update(nodeId, i :: entriesByNode.getOrElse(nodeId, Nil))
        }
    }

    val keep  = Array.fill(normalised.size)(false)
    val order = normalised.indices.sortBy(i => (-signatures(i).length, signatures(i)))
    order.foreach { i =>
      val flows        = normalised(i).flows
      val candidateSig = signatures(i)
      val candidates =
          entriesByNode.getOrElse(flows.head.id, Nil) ++
              entriesByNode.getOrElse(flows.last.id, Nil)
      keep(i) = !candidates.exists { j =>
          j != i && keep(j) && (
            signatures(j) == candidateSig ||
                (signatures(j).length > candidateSig.length &&
                    signatures(j).contains(candidateSig))
          )
      }
    }
    normalised.indices.filter(keep(_)).map(normalised).toVector
  end deduplicateFlows

  /** A flow terminates at its sink: when an entry's last node is not itself sink-tagged but the
    * path traverses a sink-tagged call, everything after that call is a continuation into the
    * caller and the entry is cut back to the call. Purls accumulated on the cut nodes stay on the
    * entry - package attribution is metadata, not evidence.
    */
  private def normaliseTerminus(
    entry: ReachableFlows,
    sinkTagPattern: Pattern
  ): Option[ReachableFlows] =
    val flows = entry.flows
    def carriesSinkTag(node: SliceNode): Boolean =
        node.tags.split(",").exists((t: String) => sinkTagPattern.matcher(t.trim).matches)
    if carriesSinkTag(flows.last) then Some(entry)
    else
      val cutAt = flows.lastIndexWhere { n => n.label == "CALL" && carriesSinkTag(n) }
      if cutAt <= 0 then Some(entry)
      else Some(entry.copy(flows = flows.take(cutAt + 1)))

  private def entryNodeIds(entry: ReachableFlows): Set[Long] =
      entry.flows.map(_.id).toSet

  /** Collects the ids of nodes that act as profile neutraliser barriers: calls carrying any of the
    * profile's tags, calls to methods carrying them, and the parameters/identifiers/returns of such
    * methods. Language-agnostic - it relies purely on tags emitted by the taggers.
    */
  private def collectNeutralizerNodeIds(atom: Cpg, tags: Set[String]): Set[Long] =
      if tags.isEmpty then Set.empty
      else
        val tagRegex      = s"(${tags.mkString("|")})"
        val taggedCalls   = atom.tag.name(tagRegex).call.id.l
        val taggedMethods = atom.tag.name(tagRegex).method
        val callsToTagged = taggedMethods.callIn(using NoResolve).id.l
        val taggedParams  = atom.tag.name(tagRegex).parameter.id.l
        val taggedIdents  = atom.tag.name(tagRegex).identifier.id.l
        (taggedCalls ++ callsToTagged ++ taggedParams ++ taggedIdents).toSet

  /** True if the flow passes through a neutraliser barrier (the node itself, or the call enclosing
    * it). Used to drop flows that are validated/sanitised/encoded, or declassified via an ORM read,
    * under a profile such as `appsec`.
    */
  private def passesThroughNeutralizer(path: Path, neutralizerNodeIds: Set[Long]): Boolean =
      neutralizerNodeIds.nonEmpty &&
          path.elements.exists { element =>
              neutralizerNodeIds.contains(element.id()) ||
              enclosingCallId(element).exists(neutralizerNodeIds.contains)
          }

  /** True if the flow originates in a synthetic `<metaClassAdapter>` method. Such flows duplicate
    * the real method's flow (the frontend emits both), so dropping the adapter variant removes the
    * duplicate while keeping the genuine flow.
    */
  // Builtins, iterator/string/type helpers that are never security sinks but leak into flows via
  // the broad framework-output / dynamic call-in tagging. Language-agnostic; matched on the
  // terminal sink's call name. A flow ending in one of these carries no signal.
  private val NON_SINK_NAMES = Set(
    "__next__",
    "__iter__",
    "__len__",
    "__getitem__",
    "next",
    "iter",
    "len",
    "isinstance",
    "issubclass",
    "hasattr",
    "strip",
    "lstrip",
    "rstrip",
    "lower",
    "upper",
    "title",
    "capitalize",
    "split",
    "rsplit",
    "splitlines",
    "join",
    "b64encode",
    "hexdigest",
    "repr",
    "print"
  )

  /** True if the flow terminates at a benign non-sink (an iterator/string/type builtin). The sink
    * is the last path element; we check its call name (or the call enclosing it).
    */
  private def endsAtNonSink(path: Path): Boolean =
      path.elements.lastOption.exists { last =>
        val name = last match
          case c: Call       => c.name
          case e: Expression => e.inCall.headOption.map(_.name).getOrElse("")
          case _             => ""
        NON_SINK_NAMES.contains(name)
      }

  private def isMetaClassAdapterFlow(path: Path): Boolean =
      path.elements.headOption.exists { head =>
        val methodName = head match
          case expr: Expression     => expr.method.name
          case p: MethodParameterIn => p.method.name
          case _                    => ""
        methodName.contains("metaClassAdapter")
      }

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

  private def toSlice(path: Path, sourceSinkTagPattern: Pattern): ReachableFlows =
    val (sliceNodes, purls, _) =
        path.elements.foldLeft((Vector.empty[SliceNode], Set.empty[String], Set.empty[String])) {
            case ((nodes, accPurls, visited), astNode) =>
                val (nodeOpt, nodePurls) = createSliceNode(astNode, sourceSinkTagPattern)
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

  /** True when the node carries a tag matching the run's source or sink tag patterns. Language
    * neutral: an operator call a tagger deliberately marked is by definition a meaningful flow
    * element, even though operator calls are SSA/lowering plumbing as a class.
    */
  private def carriesSourceOrSinkTag(node: AstNode, sourceSinkTagPattern: Pattern): Boolean =
      node.tag.name.exists(sourceSinkTagPattern.matcher(_).matches)

  private def createSliceNode(
    astNode: AstNode,
    sourceSinkTagPattern: Pattern
  ): (Option[SliceNode], Set[String]) =
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
          val isOperatorCall =
              c.code.startsWith("<operator") || c.methodFullName.startsWith("<operator")
          // Operator calls are lowering plumbing and are dropped from rendered paths - UNLESS the
          // taggers marked this one as a source or sink. Python lowers `request.form["n"]` to
          // `<operator>.indexAccess` and tags that call as framework-input; dropping it made every
          // Flask-style flow start at an intermediate node instead of its source.
          if isOperatorCall && !carriesSourceOrSinkTag(c, sourceSinkTagPattern) then
            (None, purls)
          else
            val resolvedCallee = c.callee(using NoResolve).headOption
            val externalCallee =
                resolvedCallee.exists(_.isExternal) && !c.methodFullName.startsWith("new ")
            val (finalTags, resolvedPurls) =
                if tags.isEmpty && externalCallee then
                  resolveTagsAndPurls(c, resolvedCallee)
                else (tags, purls)
            // G9: an external library call may carry its package URL on the resolved callee method
            // even when the call site itself already has other tags. Surface it so reachable flows
            // can be tied back to dependency CVEs.
            val calleePurls =
                if externalCallee then
                  resolvedCallee.toList.flatMap(_.tag.name.l).filter(_.startsWith("pkg:")).toSet
                else Set.empty[String]
            val finalPurls = resolvedPurls ++ calleePurls

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
          end if

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
