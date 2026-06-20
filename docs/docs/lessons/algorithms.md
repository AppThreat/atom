# Lesson 8: Graph Algorithmic Analysis

### Learning Objective

Apply graph-theoretic algorithms (strongly connected components, topological sorting, dominators, PageRank centrality, and shortest paths) over CPG structures.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compiling and running Atom.
- **Pre-compiled Atom File**: An existing `app.atom` file.
- **JSON Processing Utilities**: Tools like `jq` to analyze and query the output JSON file.

### Conceptual Background

The `algorithms` command applies graph algorithms directly on the compiled atom graph:

- **Strongly Connected Components (SCC)**: Detects clusters of mutually recursive methods in the call graph.
- **Topological Sort**: Computes a callee-before-caller ordering. When recursion exists, it collapses recursive components into single nodes to create an acyclic representation before sorting.
- **Centrality**: Runs PageRank and In-Degree metrics on the call graph to rank critical methods.
- **Dominators**: Computes the immediate dominator tree over the control flow graph of each method.
- **Paths**: Evaluates control/data-flow paths between two target method nodes up to a maximum depth.

The results are written as JSON to the configured output file.

### Real Commands

Analyze the call graph to locate strongly connected components (mutual recursion):

```bash
./atom.sh algorithms -l python -o app.atom --type scc --slice-outfile scc_results.json
```

Rank the methods in a codebase based on PageRank centrality:

```bash
./atom.sh algorithms -l python -o app.atom --type centrality --slice-outfile centrality_ranks.json
```

Trace paths from method `authenticate` to `db_query` up to depth 8:

```bash
./atom.sh algorithms -l python -o app.atom --type paths --source ".*authenticate.*" --target ".*db_query.*" --max-depth 8 --slice-outfile paths_traced.json
```

### Code Example

The execution of the algorithms is routed through [runAlgorithms](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala) in [GraphCommands.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala):

```scala
def runAlgorithms(cpg: Cpg, config: AtomAlgorithmsConfig): Either[String, String] = {
  val algo = config.algoType.toLowerCase
  if (!supportedAlgorithms.contains(algo)) {
    Left(s"Unsupported algorithm '$algo'")
  } else {
    val resultJson = algo match {
      case "scc"        => Right(sccReport(cpg))
      case "toposort"   => toposortReport(cpg)
      case "centrality" => Right(centralityReport(cpg))
      case "dominators" => Right(dominatorsReport(cpg))
      case "paths"      => pathsReport(cpg, config)
    }
    resultJson.map { json =>
      val outFile = File(config.outputSliceFile.pathAsString)
          .createFileIfNotExists(createParents = true)
          .write(json.spaces2)
      s"Algorithm '$algo' result written to ${outFile.pathAsString}"
    }
  }
}
```
