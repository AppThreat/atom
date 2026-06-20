# Lesson 7: Graph Exportation Scopes and Formats

### Learning Objective

Export either the full CPG or individual method subgraphs into standard serialization formats for visual analysis or machine learning pipelines.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compiling and running Atom.
- **Pre-compiled Atom File**: An existing `app.atom` file.
- **Visualization Tools**: Visualizers like Gephi (GEXF), Cytoscape, or Graphviz (DOT) to inspect the exported structures.
- **Python Utilities (Optional)**: Libraries such as NetworkX or PyTorch Geometric if processing GNN or GraphML files.

### Conceptual Background

To integrate with third-party visualization tools, graph databases, or neural network engines, the atom graph can be exported. Supported formats include DOT, GraphML, GEXF, GraphSON, Neo4j CSV, and GNN (Graph Neural Network input format).

Exportation supports two scopes:

- **whole**: Exports the entire CPG as a single unified graph.
- **methods**: Iterates over all internal methods and exports a separate file for each method subgraph, representing its AST, CFG, and DDG.

The underlying export logic delegates to `overflowdb2` format exporters.

### Real Commands

Export the entire CPG as a GraphML file:

```bash
./atom.sh export -l java -o app.atom --format graphml --scope whole --out export_output/
```

Export individual method subgraphs in DOT format:

```bash
./atom.sh export -l java -o app.atom --format dot --scope methods --out methods_output/
```

### Code Example

The export routing is handled by [runExport](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala) in [GraphCommands.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/GraphCommands.scala):

```scala
def runExport(cpg: Cpg, config: AtomExportConfig): Either[String, String] = {
  val format = config.exportFormat.toLowerCase
  val scope  = config.scope.toLowerCase
  if (!supportedFormats.contains(format)) {
    Left(s"Unsupported export format '$format'")
  } else {
    val outDir = File(config.exportDir).createDirectoryIfNotExists(createParents = true)
    exporterFor(format) match {
      case None => Left(s"No exporter available for format '$format'")
      case Some(exporter) =>
          scope match {
            case "whole"   => exportWhole(cpg, exporter, outDir)
            case "methods" => exportMethods(cpg, exporter, format, outDir)
            case other     => Left(s"Unsupported export scope '$other'")
          }
    }
  }
}
```
