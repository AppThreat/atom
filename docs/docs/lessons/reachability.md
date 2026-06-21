# Lesson 6: Interprocedural Reachability Slicing (`reachables`)

### Learning Objective

Compute interprocedural paths from tagged untrusted-input sources to sensitive sinks, accelerated by method flow summaries and the Flux engine, with sanitizer boundaries removed.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A pre-compiled `app.atom`** whose taggers have run (Lesson 3) — reachables anchors on tags.
- **Optional**: a validator/sanitizer config (`--validation-config chennai.json`).

### Conceptual Background

Reachable slicing runs interprocedural taint analysis between **source-tagged** nodes (e.g. `framework-input`) and **sink-tagged** nodes (e.g. `framework-output`, or a category like `sql`). It explores the DDG backwards from sinks across call boundaries. Three strategies keep it scalable:

- **Flux engine** (`useFluxEngine`, default on) — the low-allocation reaching-def engine, reducing GC pressure on large graphs.
- **Flow summaries** (on by default as part of the Flux bundle) — precomputed per-method `MethodFlowSummary` facts. If a method cannot propagate taint from a parameter to its return/output, or its return value can carry no taint, the engine prunes traversal into its body. There is no separate flag; `--legacy-dataflow` disables both the Flux engine and summaries.
- **Sanitization** — paths passing through nodes tagged `sanitizer` are dropped from the final result.

Configuration is `ReachablesConfig`:

```scala
case class ReachablesConfig(
  sourceTag: Seq[String],            // --source-tag (default "framework-input")
  sinkTag: Seq[String],              // --sink-tag (default "framework-output")
  sliceDepth: Int,                   // --slice-depth
  includeCryptoFlows: Boolean,       // --include-crypto
  useSummaries: Boolean = true       // follows the Flux bundle (no separate flag)
) extends BaseConfig
```

### Real Commands

Reachable paths from `framework-input` to `sql` sinks in a Python project (summaries are built and used automatically):

```bash
./atom.sh reachables -l python -o app.atom \
  --source-tag "framework-input" --sink-tag "sql" \
  --slice-outfile reachables.json /path/to/python/project
```

### Code Example

The real logic in [ReachableSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/ReachableSlicing.scala) `calculateReachableSliceAndPersist`:

```scala
def calculateReachableSliceAndPersist(
  atom: Cpg,
  config: ReachablesConfig,
  outputBasePath: String,
  chunkSize: Int = DEFAULT_CHUNK_SIZE
): Unit =
  val summaries =
    if config.useSummaries then
      // Prefer the `flow-summary` tags embedded in the atom during enhancement; fall back to the
      // JSON sidecar when the atom carries none.
      val tagged = FlowSummaryTags.fromCpg(atom)
      if tagged.nonEmpty then tagged
      else
        val cacheDir = Option(new JFile(outputBasePath).getAbsoluteFile.getParent).getOrElse(".")
        FlowSummaryComputer.loadOrCompute(atom, cacheDir, semantics)
    else Map.empty

  context = EngineContext(
    semantics,
    EngineConfig(
      useSummaries = config.useSummaries,
      summaries    = summaries
    )
  )

  val flowIterator = collectFlowSlices(atom, config, language)
    .iterator.flatten
    .filterNot(isSanitized(_, sanitizerCalls))   // drop paths through sanitizers
    .filter(isUniqueFlow(_, seenSignatures, seenEndpoints))
    .map(toSlice)

  persistChunks(flowIterator, outputBasePath, chunkSize)  // chunked JSON output
```

`FlowSummaryComputer.loadOrCompute` reads cached summaries when present, otherwise computes them callee-before-caller over call-graph SCCs. Each emitted `ReachableFlows` carries the ordered `flows` plus the `purls` of packages the flow passes through, enabling SBOM/reachability correlation.
