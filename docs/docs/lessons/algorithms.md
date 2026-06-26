# Lesson 8: Graph Algorithmic Analysis (`algorithms`)

### Learning Objective

Apply graph-theoretic algorithms — strongly connected components, topological sort, dominators, PageRank centrality, and call paths — directly over the compiled atom.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A pre-compiled `app.atom`**.
- **`jq`** (optional) to query the JSON output.

### Conceptual Background

The `algorithms` command runs algorithms over the **call graph** (method nodes linked by `CALL` edges) or, for dominators/traversals, over each method's CFG/AST. Routing is in [`runAlgorithms`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala) and the supported set includes:

- **scc** — Strongly Connected Components: clusters of mutually recursive methods. Useful to identify cyclic calls.
- **toposort** — Callee-before-caller ordering; recursive components are collapsed into single nodes to form an acyclic condensation before sorting.
- **dominators** — Per-method immediate-dominator tree over `CFG` out-edges. Used to analyze control dependencies.
- **paths** — Breadth-First search call paths between the first method matching `--source` and the first matching `--target`, up to `--max-depth`.
- **centrality** — PageRank plus in-degree centrality, ranking methods by call frequency and reference weight.
- **lowest-common-ancestors** — Computes lowest common caller method(s) that can reach all methods matching `--source`. Useful for identifying common entry points or routing hubs in security audits.
- **dependency-sequencer** — Partitions components of the call graph DAG into parallel execution stages. Useful to determine concurrent task scheduling/processing orders.
- **union-find** — Disjoint Set Union to cluster methods into completely disconnected/independent subgraphs. Useful to find code boundaries and isolated subsystems.
- **heap-walker** — Traverses the AST of a method matching `--source` in stack-safe Depth-First Search order using heap memory. Useful to prevent stack overflows when inspecting deeply nested syntax trees.
- **context-sensitive-paths** — Uses matching parentheses (call/return site IDs) to compute valid call paths from `--source` to `--target`. Useful to eliminate false positives/unreachable call paths that cross call-sites incorrectly.

Results are written as JSON to `--slice-outfile`.

### Real Commands

Locate mutually recursive method clusters:

```bash
./atom.sh algorithms -l python -o app.atom --type scc --slice-outfile scc_results.json
```

Rank methods by PageRank centrality:

```bash
./atom.sh algorithms -l python -o app.atom --type centrality --slice-outfile centrality_ranks.json
```

Trace paths from `authenticate` to `db_query` up to depth 8:

```bash
./atom.sh algorithms -l python -o app.atom --type paths \
  --source ".*authenticate.*" --target ".*db_query.*" --max-depth 8 \
  --slice-outfile paths_traced.json
```

Find the lowest common caller (LCA) of authentication and billing helper methods:

```bash
./atom.sh algorithms -l python -o app.atom --type lowest-common-ancestors \
  --source ".*auth_helper.*|.*billing_helper.*" --slice-outfile lca.json
```

Sequence dependencies for parallel processing:

```bash
./atom.sh algorithms -l python -o app.atom --type dependency-sequencer \
  --slice-outfile sequencer_stages.json
```

Identify disconnected components of the call graph using Disjoint Set Union:

```bash
./atom.sh algorithms -l python -o app.atom --type union-find \
  --slice-outfile call_components.json
```

Perform stack-safe DFS walk of the `main` method AST:

```bash
./atom.sh algorithms -l python -o app.atom --type heap-walker \
  --source ".*main" --slice-outfile main_ast_walk.json
```

Trace high-precision context-sensitive call paths from `main` to `helper`:

```bash
./atom.sh algorithms -l python -o app.atom --type context-sensitive-paths \
  --source ".*main" --target ".*helper" --max-depth 10 \
  --slice-outfile cs_paths.json
```

A `centrality` result looks like:

```json
{
  "ranking": [
    { "method": "app.router.dispatch:...", "pageRank": 0.18, "inDegree": 42 },
    { "method": "app.auth.verify:...", "pageRank": 0.09, "inDegree": 17 }
  ]
}
```

### Code Example

The real dispatch in [GraphCommands.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala):

```scala
def runAlgorithms(cpg: Cpg, config: AtomAlgorithmsConfig): Either[String, String] =
  val algo = config.algoType.toLowerCase
  if !supportedAlgorithms.contains(algo) then
    Left(s"Unsupported algorithm '$algo'. Supported: ${supportedAlgorithms.toSeq.sorted.mkString(", ")}")
  else
    val resultJson = algo match
      case "scc"        => Right(sccReport(cpg))
      case "toposort"   => toposortReport(cpg)
      case "centrality" => Right(centralityReport(cpg))
      case "dominators" => Right(dominatorsReport(cpg))
      case "paths"      => pathsReport(cpg, config)
    resultJson.map { json =>
      val outFile = File(config.outputSliceFile.pathAsString)
        .createFileIfNotExists(createParents = true)
        .write(json.spaces2)
      s"Algorithm '$algo' result written to ${outFile.pathAsString}"
    }
```

`toposort` and `paths` return `Either` (they can fail, e.g. when a source/target regex matches no method), whereas `scc`, `centrality`, and `dominators` always produce a report.
