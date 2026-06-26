# Lesson 7: Graph Export Scopes and Formats (`export`)

### Learning Objective

Export the full CPG, or individual method subgraphs, into standard serialization formats for visualization, graph databases, or machine-learning pipelines.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A pre-compiled `app.atom`**.
- **Optional viewers**: Gephi (GEXF), Cytoscape, Graphviz (DOT); NetworkX / PyTorch Geometric for GraphML/GNN.

### Conceptual Background

The atom graph can be serialised for third-party tools. Export delegates to `overflowdb2`'s format exporters via [`runExport`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala).

**Supported formats** (`supportedFormats`):

| `--format` | Exporter           | Use case                |
| ---------- | ------------------ | ----------------------- |
| `dot`      | `DotExporter`      | Graphviz visualisation  |
| `graphml`  | `GraphMLExporter`  | yEd, NetworkX           |
| `gexf`     | `GexfExporter`     | Gephi                   |
| `graphson` | `GraphSONExporter` | TinkerPop/Gremlin       |
| `neo4jcsv` | `Neo4jCsvExporter` | Neo4j bulk import       |
| `gnn`      | `GnnExporter`      | JSON tensors for GNN ML |

**Supported scopes** (`supportedScopes`):

- `whole` — the entire CPG as a single graph.
- `methods` — one file per internal method (its AST, CFG, DDG subgraph).

### Real Commands

Export the whole CPG as GraphML:

```bash
./atom.sh export -l java -o app.atom --format graphml --scope whole --out export_output/
```

Export per-method DOT subgraphs:

```bash
./atom.sh export -l java -o app.atom --format dot --scope methods --out methods_output/
```

### Code Example

The real routing in [GraphCommands.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala):

```scala
def runExport(cpg: Cpg, config: AtomExportConfig): Either[String, String] =
  val format = config.exportFormat.toLowerCase
  val scope  = config.scope.toLowerCase
  if !supportedFormats.contains(format) then
    Left(s"Unsupported export format '$format'. Supported: ${supportedFormats.toSeq.sorted.mkString(", ")}")
  else if !supportedScopes.contains(scope) then
    Left(s"Unsupported export scope '$scope'. Supported: ${supportedScopes.toSeq.sorted.mkString(", ")}")
  else
    val outDir = File(config.exportDir).createDirectoryIfNotExists(createParents = true)
    exporterFor(format) match
      case None           => Left(s"No exporter available for format '$format'")
      case Some(exporter) =>
          scope match
            case "whole"   => exportWhole(cpg, exporter, outDir)
            case "methods" => exportMethods(cpg, exporter, format, outDir)
```

`exporterFor` resolves the format to an `overflowdb.formats.*` exporter (e.g. `overflowdb.formats.gnn.GnnExporter` for `gnn`). For large graphs prefer `--scope methods` with `dot` for legible per-method diagrams, or `neo4jcsv`/`graphson` to load the whole graph into a database for interactive querying.
