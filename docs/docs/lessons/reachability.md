# Lesson 6: Interprocedural Reachability Slicing

### Learning Objective

Calculate interprocedural paths from untrusted input sources to sensitive APIs, utilizing method flow summaries and sanitization boundaries.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compiling and running Atom.
- **Pre-compiled Atom File**: An existing `app.atom` file.
- **Taint Policy Config**: A custom validator/sanitizer configuration file (e.g. `validation.json`).

### Conceptual Background

Reachable slicing conducts interprocedural taint analysis across method calls. It searches for paths between source nodes (e.g. HTTP parameters, CLI arguments, or database queries) and sink nodes (e.g. file writers, SQL execution statements, or socket streams).

To scale on large codebases, the reachable engine uses three key strategies:

- **Flux Engine**: A compiler-optimized query engine that caches local CFG/DDG fragments to accelerate traversal.
- **Flow Summaries**: Method-level flow summaries computed up front. If a method does not propagate taint from parameter to return value, the engine skips traversing that method body.
- **Sanitization**: Paths passing through methods tagged as `sanitizer` are dropped from the final slice.

### Real Commands

Find reachable paths from `framework-input` to `sql` sinks in a Python project using the Flux engine and flow summaries:

```bash
./atom.sh reachables -l python -o app.atom --source-tag "framework-input" --sink-tag "sql" --summaries --slice-outfile reachables.json /path/to/python/project
```

### Code Example

The reachables slicing logic is implemented in [calculateReachableSliceAndPersist](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/ReachableSlicing.scala) in [ReachableSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/ReachableSlicing.scala):

```scala
def calculateReachableSliceAndPersist(
  atom: Cpg,
  config: ReachablesConfig,
  outputBasePath: String,
  chunkSize: Int = DEFAULT_CHUNK_SIZE
): Unit = {
  val language = atom.metaData.language.head
  val summaries =
      if config.useSummaries then
        val cacheDir = Option(new JFile(outputBasePath).getAbsoluteFile.getParent).getOrElse(".")
        FlowSummaryComputer.loadOrCompute(atom, cacheDir, semantics)
      else Map.empty

  context = EngineContext(
    semantics,
    EngineConfig(
      useFluxEngine = config.useFluxEngine,
      useSummaries = config.useSummaries,
      summaries = summaries
    )
  )

  // Deduplicate paths and filter out sanitized paths
  val flowIterator = collectFlowSlices(atom, config, language)
      .iterator
      .flatten
      .filterNot(isSanitized(_, sanitizerCalls))
      .filter(isUniqueFlow(_, seenSignatures, seenEndpoints))
      .map(toSlice)

  // Persist slices in chunked JSON files
  persistChunks(flowIterator, outputBasePath, chunkSize)
}
```
