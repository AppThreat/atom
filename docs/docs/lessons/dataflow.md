# Lesson 5: Backward Data-Flow Slicing (`data-flow`)

### Learning Objective

Execute backward, intraprocedural data-flow slicing from external API boundaries to discover the source inputs that influence dangerous sinks.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A target project** (or pre-built `app.atom`).
- **Tuning**: the reaching-definition cap, set via `--max-num-def` (default 2000).

### Conceptual Background

Data-flow slicing walks variable state **backwards** over the Data Dependence Graph (DDG / `REACHING_DEF` edges produced during enrichment). Sinks are selected as the arguments of **external** calls (methods defined outside the compiled project) — these are the boundaries where tainted data leaves the program. From each sink the slicer repeats `_.ddgIn` up to `--slice-depth`, collecting the reachable nodes and the edges between them.

[`DataFlowSlicing`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/DataFlowSlicing.scala) runs one `TrackDataFlowTask` per candidate sink on a thread pool. Behaviour is controlled by `DataFlowConfig`:

```scala
case class DataFlowConfig(
  sinkPatternFilter: Option[String] = None,   // --sink-filter regex on the sink code
  mustEndAtExternalMethod: Boolean = true,
  excludeOperatorCalls: Boolean = true,        // drop <operator>.* noise
  sliceDepth: Int = 7,                         // --slice-depth: max DDG hops
  sliceNodesLimit: Int = 200,
  useFluxEngine: Boolean = true                // low-allocation reaching-def engine
) extends BaseConfig
```

The result is a `DataFlowSlice(nodes: Set[SliceNode], edges: Set[SliceEdge])` serialised to JSON.

### Real Commands

Backward data-flow slices for a Java project, depth 5, only sinks matching `.*exec.*`:

```bash
./atom.sh data-flow -l java -o app.atom --slice-outfile dataflow.json \
  --slice-depth 5 --sink-filter ".*exec.*" /path/to/java/project
```

### Code Example

The real traversal in [DataFlowSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/DataFlowSlicing.scala):

```scala
def calculateDataFlowSlice(atom: Cpg, config: DataFlowConfig): Option[DataFlowSlice] =
  language = atom.metaData.language.headOption
  excludeOperatorCalls.set(config.excludeOperatorCalls)

  (config.fileFilter match
    case Some(fileRegex) => atom.call.where(_.file.name(fileRegex))
    case None            => atom.call
  )
    .where(c => c.callee.isExternal)
    .flatMap {
      case c if excludeOperatorCalls.get() && c.name.startsWith("<operator") => None
      case c                                                                 => Some(c)
    }
    .map(c => exec.submit(new TrackDataFlowTask(config, c)))
    .flatMap(TimedGet)
    .reduceOption((a, b) => DataFlowSlice(a.nodes ++ b.nodes, a.edges ++ b.edges))
```

Inside `TrackDataFlowTask`, the backward walk itself is:

```scala
val sliceNodes = sinks.repeat(_.ddgIn)(using _.maxDepth(config.sliceDepth).emit).dedup.l
```

`TimedGet` bounds each task so one pathological method cannot stall the whole slice. Unlike _reachables_ (Lesson 6), this slice is **intraprocedural** — it does not cross method boundaries.
