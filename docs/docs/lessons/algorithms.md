# Lesson 8: Graph Algorithmic Analysis (`algorithms`)

### Learning Objective

Apply graph-theoretic algorithms — strongly connected components, topological sort, dominators, PageRank centrality, and call paths — directly over the compiled atom.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A pre-compiled `app.atom`**.
- **`jq`** (optional) to query the JSON output.

### Conceptual Background

The `algorithms` command runs algorithms over the **call graph** (method nodes linked by `CALL` edges) or, for dominators, over each method's CFG. Routing is in [`runAlgorithms`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala) and the supported set is `scc`, `toposort`, `dominators`, `paths`, `centrality`:

- **scc** — strongly connected components: clusters of mutually recursive methods.
- **toposort** — callee-before-caller ordering; recursive components are collapsed into single nodes to form an acyclic condensation before sorting. This is the same ordering the flow-summary computer uses (see chen Lesson 15).
- **dominators** — per-method immediate-dominator tree over `CFG` out-edges (`node.dominatorTree(...)`).
- **paths** — call paths between the first method matching `--source` and the first matching `--target`, up to `--max-depth` (default 10).
- **centrality** — PageRank (`nodes.pageRank(callees)`) plus in-degree centrality, ranking methods by influence.

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
