package io.appthreat

import better.files.File
import io.appthreat.atom.Atom.*
import io.appthreat.atom.slicing.*
import io.circe.{Encoder, Json}

package object atom:

  trait AtomConfig extends BaseConfig:
    this.inputPath = File(".")
    this.outputSliceFile = File(DEFAULT_SLICE_OUT_FILE)
    var outputAtomFile: File = File(DEFAULT_ATOM_OUT_FILE)
    var language: String     = ""
    var dataDeps: Boolean    = false
    var removeAtom: Boolean  = false
    var maxNumDef: Int       = DEFAULT_MAX_DEFS
    // Flux data-flow engine and mini-graph fragment caching are the defaults; `--legacy-dataflow`
    // opts back into the classic engine (and disables fragment caching). Method flow summaries are
    // part of the Flux bundle: when the Flux engine is on, summaries are built and the reachables
    // query engine uses them to prune provably empty cross-call work (so `useSummaries` follows
    // `useFluxEngine` and has no separate flag).
    var useFluxEngine: Boolean            = true
    var cacheFragments: Boolean           = true
    var exportAtom: Boolean               = false
    var reuseAtom: Boolean                = false
    var exportDir: String                 = DEFAULT_EXPORT_DIR
    var exportFormat: String              = DEFAULT_EXPORT_FORMAT
    var frontendArgs: Map[String, String] = Map.empty
    // Optional config file (JSON) for the verbose, repeatable parameters of the graph-level
    // commands. CLI flags always take precedence over values read from this file.
    var configFile: Option[File] = None
    // Optional validators/sanitisers config (the chennai.json schema). Calls to the declared
    // methods are tagged as sanitisers so reachable flows passing through them can be dropped.
    var validationConfigFile: Option[File] = None

    def withOutputAtomFile(x: File): AtomConfig =
      this.outputAtomFile = x
      this

    def withLanguage(x: String): AtomConfig =
      this.language = x
      this

    def withDataDependencies(x: Boolean): AtomConfig =
      this.dataDeps = x
      this

    def withRemoveAtom(x: Boolean): AtomConfig =
      this.removeAtom = x
      this

    def withExportAtom(x: Boolean): AtomConfig =
      this.exportAtom = x
      this

    def withReuseAtom(x: Boolean): AtomConfig =
      this.reuseAtom = x
      this

    def withExportDir(x: String): AtomConfig =
      this.exportDir = x
      this

    def withExportFormat(x: String): AtomConfig =
      this.exportFormat = x
      this

    def withMaxNumDef(x: Int): AtomConfig =
      this.maxNumDef = x
      this

    def withUseFluxEngine(x: Boolean): AtomConfig =
      this.useFluxEngine = x
      this

    def withCacheFragments(x: Boolean): AtomConfig =
      this.cacheFragments = x
      this

    def withFrontendArgs(args: Map[String, String]): AtomConfig =
      this.frontendArgs = args
      this

    def withConfigFile(x: Option[File]): AtomConfig =
      this.configFile = x
      this

    def withValidationConfigFile(x: Option[File]): AtomConfig =
      this.validationConfigFile = x
      this

  end AtomConfig

  case class DefaultAtomConfig() extends AtomConfig

  case class AtomParseDepsConfig() extends AtomConfig

  case class AtomDataFlowConfig(
    sinkPatternFilter: Option[String] = None,
    excludeOperatorCalls: Boolean = true,
    mustEndAtExternalMethod: Boolean = true,
    sliceDepth: Int = DEFAULT_SLICE_DEPTH
  ) extends AtomConfig

  case class AtomUsagesConfig(
    minNumCalls: Int = 1,
    excludeOperatorCalls: Boolean = true,
    includeMethodSource: Boolean = false,
    extractEndpoints: Boolean = false
  ) extends AtomConfig

  case class AtomReachablesConfig(
    sourceTag: Seq[String] = DEFAULT_SOURCE_TAGS,
    sinkTag: Seq[String] = DEFAULT_SINK_TAGS,
    sliceDepth: Int = DEFAULT_SLICE_DEPTH,
    includeCryptoFlows: Boolean = false,
    profile: String = "generic"
  ) extends AtomConfig

  /** Export the whole atom, or a per-method subgraph of it, to one of the supported graph formats.
    * `scope` is either "whole" or "methods". The output format is taken from `exportFormat` and the
    * destination directory from `exportDir`.
    */
  case class AtomExportConfig() extends AtomConfig:
    var scope: String = "whole"

    def withScope(x: String): AtomExportConfig =
      this.scope = x
      this

  /** Run a graph algorithm over the atom and write the result as JSON. `algoType` selects the
    * algorithm (scc, toposort, dominators, paths, centrality, lowest-common-ancestors,
    * dependency-sequencer, union-find, heap-walker, context-sensitive-paths). The path-finding and
    * traversing algorithms use `sourceSelector`/`targetSelector` (regular expressions matched
    * against method full names) and an optional `maxDepth`.
    */
  case class AtomAlgorithmsConfig() extends AtomConfig:
    var algoType: String               = "centrality"
    var sourceSelector: Option[String] = None
    var targetSelector: Option[String] = None
    var maxDepth: Int                  = -1

    def withAlgoType(x: String): AtomAlgorithmsConfig =
      this.algoType = x
      this

    def withSourceSelector(x: Option[String]): AtomAlgorithmsConfig =
      this.sourceSelector = x
      this

    def withTargetSelector(x: Option[String]): AtomAlgorithmsConfig =
      this.targetSelector = x
      this

    def withMaxDepth(x: Int): AtomAlgorithmsConfig =
      this.maxDepth = x
      this
  end AtomAlgorithmsConfig

  import io.appthreat.atom.slicing.*
  import io.circe.generic.auto.*
  import io.circe.syntax.EncoderOps

  implicit val encodeDataFlowSlice: Encoder[AtomDataFlowSlice] = Encoder.instance {
      case AtomDataFlowSlice(dataFlowSlice, paths) =>
          Json.obj("graph" -> dataFlowSlice.asJson, "paths" -> paths.asJson)
  }

  case class AtomDataFlowSlice(graph: DataFlowSlice, paths: Set[List[Long]] = Set.empty):

    def toJson: String = this.asJson.noSpaces
end atom
