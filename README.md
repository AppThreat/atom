# atom (⚛)

<img src="./docs/static/img/Atom-logo.png" width="200" height="auto" />

atom is a novel intermediate representation for applications and a standalone tool powered by the [chen](https://github.com/AppThreat/chen) library. The intermediate representation (a network with nodes and links) is optimized for operations typically used for application analytics and machine learning, including [slicing](./specification/docs/slices.md) and vectoring.

Our vision is to make atom useful for many use cases such as:

- **Supply-chain analysis:** Generate evidence of external library usage including the flow of data from sources to sinks. atom is used by [OWASP cdxgen](https://github.com/CycloneDX/cdxgen) to improve the precision and comprehensiveness of the generated CycloneDX document.
- **Vulnerability analysis:** Describe vulnerabilities with evidence of affected symbols, call paths, and data-flows. Enable variant and [reachability analysis](https://github.com/AppThreat/atom/blob/main/specification/docs/slices.md#reachables-slice) at scale.
- **Exploit prediction:** Predict exploits using precise representations of vulnerabilities, libraries, and applications.
- **Threat-model and attack vectors generation:** Generate precise threat models and attack vectors for applications at scale.
- **Application context detection:** Generate context useful for summarization and risk-profile generation (e.g. services, endpoints, and data attributes).
- **Mind-maps for applications:** Automate summarization of large and complex applications as a developer tool.

and more.

[![SBOM](https://img.shields.io/badge/SBOM-with_%E2%9D%A4%EF%B8%8F_by_cdxgen-FF753D)](https://github.com/cdxgen/cdxgen)
![npm](https://img.shields.io/npm/dw/@appthreat/atom)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/AppThreat/atom/total?color=FF753D)

## Languages supported

- C/C++
- H (C/C++ Header and pre-processed .i files alone)
- Java (Requires compilation)
- Jar
- Android APK and split bundles (.apkm, .apks, .xapk). Requires Android SDK. Set the environment variable `ANDROID_HOME` or use the container image.
- JavaScript
- Flow
- TypeScript
- Python (Supports 3.x to 3.14)
- PHP (Requires PHP >= 7.4. Supports PHP 7.0 to 8.4 with limited support for PHP 5.x)
- Ruby (Requires Ruby 4.0.x. Supports Ruby 1.8 - 4.0.x syntax)
- Scala (WIP)

## Installation

atom comprises a scala core with a Node.js wrapper module. It is currently distributed as standalone binaries and as a npm package.

## atom native-image

atom is available as a native image built using graalvm community edition.

```shell
curl -LO https://github.com/AppThreat/atom/releases/latest/download/atom-amd64
chmod +x atom-amd64
./atom-amd64 --help
```

On Windows

```pwsh
curl -LO https://github.com/AppThreat/atom/releases/latest/download/atom.exe
.\atom.exe --help
```

NOTE: Commands such as astgen, rbastgen, phpastgen, etc. are not bundled into this native image. Install the npm package `@appthreat/atom-parsetools` to get these commands.

```shell
npm install -g @appthreat/atom-parsetools
which astgen
which phpastgen
```

## npm-based installation

```shell
npm install -g @appthreat/atom @appthreat/atom-parsetools --ignore-scripts
atom --help
```

Install cdxgen npm package to generate a Software Bill-of-Materials (SBOM) which is required for reachables slicing.

```shell
npm install -g @cyclonedx/cdxgen --omit=optional --ignore-scripts
```

## container usage

```shell
docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom --help
# podman run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom --help
```

Example for java project.

```shell
docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom -l java -o /app/app.atom /app
# podman run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom -l java -o /app/app.atom /app
```

## CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|reachables|export|algorithms] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  -s, --slice-outfile <value>
                           export intra-procedural slices as json
  -l, --language <value>   source language
  --frontend-args <value>  Advanced frontend configuration (key=value). E.g. --frontend-args defines=DEBUG,cpp-standard=c++17
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --remove-atom            do not persist the atom file - defaults to `false`
  --reuse-atom             reuse existing atom file - defaults to `false`
  -x, --export-atom        export the atom file with data-dependencies to graphml - defaults to `false`
  --export-dir <value>     export directory. Default: atom-exports
  --file-filter <value>    the name of the source file to generate slices from. Uses regex.
  --method-name-filter <value>
                           filters in slices that go through specific methods by names. Uses regex.
  --method-parameter-filter <value>
                           filters in slices that go through methods with specific types on the method parameters. Uses regex.
  --method-annotation-filter <value>
                           filters in slices that go through methods with specific annotations on the methods. Uses regex.
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation - defaults to 2000
  --legacy-dataflow        use the classic data-flow engine and disable mini-graph fragment caching and method flow summaries. By default atom uses the faster, lower-allocation Flux engine with fragment caching and summary-guided pruning enabled.
  --validation-config <value>  path to a JSON file declaring validators/sanitisers (chennai.json schema). Reachable flows passing through a declared sanitiser are dropped for its categories.
Command: parsedeps
Extract dependencies from the build file and imports
Command: data-flow [options]
Extract backward data-flow slices
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice - defaults to 7.
  --sink-filter <value>    filters on the sink's `code` property. Uses regex.
Command: usages [options]
Extract local variable and parameter usages
  --min-num-calls <value>  the minimum number of calls required for a usage slice - defaults to 1.
  --include-source         includes method source code in the slices - defaults to false.
  --extract-endpoints      extract http endpoints and convert to openapi format using atom-tools - defaults to false.
Command: reachables [options]
Extract reachable data-flow slices based on automated framework tags
  --source-tag <value>     source tag - defaults to framework-input. Comma-separated values allowed.
  --sink-tag <value>       sink tag - defaults to framework-output. Comma-separated values allowed.
  --include-crypto         includes crypto library flows - defaults to false.
  --profile <value>        reduce false positives with a flow-filtering profile: appsec, generic. Defaults to generic (no extra filtering).
Command: export [options]
Export the atom to a graph format (dot, graphml, gexf, graphson, neo4jcsv, gnn)
  --format <value>         export format: dot, graphml, gexf, graphson, neo4jcsv or gnn
  --scope <value>          export scope: whole or methods. Default: whole
  --out <value>            output directory. Default: atom-exports
Command: algorithms [options]
Run a graph algorithm over the atom and write the result as JSON
  --type <value>           algorithm: scc, toposort, dominators, paths or centrality
  --source <value>         source method full-name pattern for the paths algorithm. Uses regex.
  --target <value>         target method full-name pattern for the paths algorithm. Uses regex.
  --max-depth <value>      maximum path depth for the paths algorithm
  --config <value>         path to a JSON config file for the export and algorithms commands
  --help                   display this help message
```

## Export and algorithms commands

The `export` and `algorithms` commands work on an `.atom` file. If the file given with `-o` already
exists it is reused; otherwise atom builds it from the input first, so there is no separate build
step to remember.

`export` writes the graph in one of several formats. Use `--scope whole` for the entire atom or
`--scope methods` to write one file per method (every format except neo4jcsv supports the per-method
scope). The `gnn` format writes parallel id, label and edge arrays as JSON for machine-learning
pipelines.

```
atom export -l python --format graphml --scope whole -o app.atom --out exports app.py
atom export -l python --format gnn --scope methods -o app.atom --out gnn-out app.py
```

`algorithms` runs a graph algorithm and writes the result to the slice output file as JSON:

- `centrality` ranks methods in the call graph by PageRank and in-degree.
- `scc` reports strongly connected components, which flags recursion in the call graph.
- `toposort` returns a callee-before-caller ordering of methods. Recursive methods are grouped into
  stages (each flagged as recursive) so the ordering works even when the call graph has cycles.
- `dominators` writes the control-flow dominator tree of each method.
- `paths` finds paths between a source and target method selected by regular expression.

```
atom algorithms -l python --type centrality -o app.atom -s centrality.json app.py
atom algorithms -l python --type paths --source '.*main$' --target '.*helper$' -o app.atom -s paths.json app.py
```

Verbose or repeatable parameters can be supplied with `--config <file>`, a JSON file. Command-line
flags override values from the file. For example, an algorithms config file:

```json
{
  "type": "paths",
  "source": ".*main$",
  "target": ".*helper$",
  "maxDepth": 20,
  "out": "paths.json"
}
```

## Data-flow engine

atom computes data dependencies with the **Flux** engine (_Flow-Lattice Update eXecutor_) by default
— a low-allocation, reaching-definitions solver that produces the same `REACHING_DEF` edges as the classic engine while
using far less memory and GC time on large (e.g. bundled/transpiled JavaScript) methods. Per-file
mini-graph (fragment) AST caching is also enabled by default, so unchanged files are restored from a
`.chen` cache instead of being re-parsed.

Pass `--legacy-dataflow` to fall back to the classic engine and disable fragment caching (useful for
A/B comparisons or troubleshooting). Caching can also be controlled independently via the
`-Dchen.cache.disabled=true` system property.

### Storage compression (`-Dodb.storage.compression`)

On large codebases the graph overflows the heap and unused nodes are spilled to disk; the spill/save
path runs on a single thread, so the compressor it uses can dominate generation and slicing time.
Choose it with a system property (no rebuild needed):

```
atom ... -Dodb.storage.compression=none|lzf|deflate
```

- `deflate` (**default**) — slowest but produces the smallest `.atom`; the default because atom file size matters most on large codebases.
- `lzf` — fast compression; best for large graphs that overflow the heap, where DEFLATE dominates generation/slicing time.
- `none` — no compression; fastest spill, largest `.atom`.

The compressor is recorded per chunk, so atoms written with any mode remain readable under any other
(an `lzf` atom loads fine under the `deflate` default). Giving the JVM more heap (`-Xmx`) also
reduces spilling — the cheapest win is to not overflow at all.

## Method flow summaries

The `reachables` command builds a context-independent flow summary for each method before running the
backward query. A summary records which parameters of a method reach its return value or its output
parameters. The reachables engine uses these summaries to skip cross-call work that provably carries
no taint, for example exploring an argument that the callee never writes, or descending into a callee
whose return value can carry no taint. The pruning only removes empty work, so the reported flows are
identical to a run without summaries.

Method flow summaries are part of the default Flux bundle — there is no separate flag. They are
enabled whenever the Flux engine is used (the default) and turned off only by `--legacy-dataflow`,
which also reverts to the classic data-flow engine.

Summaries are persisted in two ways: as CPG-native `flow-summary` tags on each `METHOD` node (so they
serialize with the atom and are reused for free when the atom is reused/cached), and as a
`flowsummary-<fingerprint>.json` sidecar next to the atom for runs that do not carry the tags. The
fingerprint changes when any method body changes. The summary caches, like the other caches, can be
turned off with `-Dchen.cache.disabled=true`.

```shell
atom reachables -o app.atom -s reachables.json -l java .
```

## Validators and sanitisers

Reachable flows that pass through a genuine validation or sanitisation routine are usually not real
findings. You can declare such routines in a JSON file and pass it with `--validation-config`. atom
tags every call to a declared method as a sanitiser and drops reachable flows that pass through one,
scoped to the sink categories the sanitiser covers.

The file uses the same schema as `chennai.json`. `sanitizers` and `validators` are treated
identically; `categories` is optional and, when omitted, the sanitiser covers every flow:

```json
{
  "sanitizers": [
    {
      "name": "owasp-encode",
      "methods": ["org\\.owasp\\.encoder\\.Encode\\..*"],
      "categories": ["http"]
    },
    {
      "name": "sql-escape",
      "methods": [".*escapeSql.*"],
      "categories": ["sql"]
    }
  ],
  "validators": [{ "name": "bean-validation", "methods": [".*\\.validate"] }]
}
```

`methods` entries are matched against each call's method full name (treated as a regular expression
when they contain regex characters, otherwise as an exact match). `categories` are matched against
the tags on a flow's sink, so an HTML encoder declared for `http` does not suppress an SQL-injection
flow.

```shell
atom reachables --validation-config validators.json -o app.atom -s reachables.json -l java .
```

The declarations can also be embedded in the project's `chennai.json` instead of passed on the
command line. For programmatic use, the dataflow engine exposes `passesThrough` /
`doesNotPassThrough` on a flow iterator, taking a simple node predicate.

## Reachability profiles

The default reachables run is deliberately broad: it reports every flow from a tagged source to a
tagged sink. For a focused review you can apply a profile with `--profile`, which post-filters the
flows in a generic, tag-driven way that works across all languages.

```shell
atom reachables --profile appsec -o app.atom -s reachables.json -l python .
```

Profiles do two things:

- **Neutraliser barriers** — a flow that passes through a call (or a call to a method) carrying a
  neutraliser tag is dropped. This builds on the validator/sanitiser handling above and adds
  declassification barriers such as ORM reads.
- **Source restriction** — a profile may narrow the source tags considered. An explicit
  `--source-tag` always overrides this.

| Profile             | Behaviour                                                                                                                                                                                                                                                                      |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `generic` (default) | No additional filtering.                                                                                                                                                                                                                                                       |
| `appsec`            | Web/API review. Drops flows neutralised by `validation`, `sanitization`, `sanitizer`, `encode` or `db-read` (ORM reads), and restricts sources to web-facing inputs (`framework-input`, `framework-route`, `framework`, `service-ingress`) — i.e. excludes CLI/driver sources. |

The `db-read` barrier is what removes the bulk of object-identity false positives: when a request only
supplies the primary key to an ORM accessor (e.g. `get_object_or_404`), the value read back is stored
data, not live request input, so flows that merely pass through such a read are pruned.

Profiles are extensible. A profile is a `ReachabilityProfile` (name, neutraliser tags, optional source
override) registered in `io.appthreat.atom.slicing` — add an entry to the registry to define your own.

## Sample Invocations

### Generate an atom

```shell
# Compile java project
atom -o app.atom -l java .
```

```shell
atom -o app.atom -l jar <jar file>
```

```shell
export ANDROID_HOME=<path to android sdk>
atom -o app.atom -l apk <apk file>
```

Split bundles are also supported. A `.apkm`, `.apks`, or `.xapk` file is unpacked automatically and
the apks that carry dalvik bytecode are analysed.

```shell
export ANDROID_HOME=<path to android sdk>
atom -o app.atom -l apk <apkm file>
```

When slicing an Android apk, atom runs the chen Android tagger passes over the code property graph.
These passes attach semantic tags to the flows in a reachable slice, including personally
identifiable information such as `pii-email` and `pii-device-id`, regulated data such as `pci-dss`,
`gdpr`, and `phi-medical`, secrets, third party trackers tagged as `tracker`, and network direction
tagged as `service-egress`, `service-ingress`, and `on-device-ai`. atom-tools consumes these tags to
attribute reachable services and to enrich the CycloneDX SBOM produced by blint.

### Create reachables slice for a java project.

```shell
cd <path to repo>
cdxgen -t java --deep -o bom.json .
atom reachables -o app.atom -s reachables.json -l java .
```

Pass the argument `--reuse-atom` to slice based on an existing atom file.

```shell
atom reachables --reuse-atom -o app.atom -s reachables.json -l java .
```

Example with container-based invocation.

```shell
docker run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom reachables -l java -o /app/app.atom -s /app/reachables.slices.json /app
# podman run --rm -v /tmp:/tmp -v $HOME:$HOME -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom reachables -l java -o /app/app.atom -s /app/reachables.slices.json /app
```

### Create usages slice for a java project.

```shell
atom usages -o app.atom --slice-outfile usages.json -l java .
```

Example for a Ruby 4 project with container-based invocation.

```shell
docker run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

Pass the argument `--platform=linux/amd64`, if you face issues with Java or Ruby builds on arm64 architecture.

```shell
docker run --rm --platform=linux/amd64 -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm --platform=linux/amd64 -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

For Ruby 4, there is an alpine-based version available.

```shell
docker run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

Ruby 3 variant is also available for alpine.

```shell
docker run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby3 atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby3 atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

### Create data-flow slice for a java project.

```shell
atom data-flow -o app.atom --slice-outfile df.json -l java .
```

Learn more about [slices](./specification/docs/slices.md) or view some [samples](https://github.com/AppThreat/atom-samples)

### Extract HTTP endpoints in openapi format using atom-tools

atom can automatically invoke [atom-tools](https://github.com/AppThreat/atom-tools) `convert` command to extract http endpoints from the usages slices. Pass the argument `--extract-endpoints` to enable this feature.

```shell
pip install atom-tools
atom usages --extract-endpoints -o app.atom --slice-outfile usages.json -l java .
```

A file called `openapi.json` would be created with the endpoints information. Use the environment variable `ATOM_TOOLS_OPENAPI_FILENAME` to customize the filename.

```shell
ATOM_TOOLS_OPENAPI_FILENAME=openapi.json atom usages --extract-endpoints -o app.atom --slice-outfile usages.json -l ruby .
```

Container-based invocation:

```shell
docker run --rm -v /tmp:/tmp -e ATOM_TOOLS_OPENAPI_FILENAME=openapi.json -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -e ATOM_TOOLS_OPENAPI_FILENAME=openapi.json -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

### Export atom to graphml or dot format

It is possible to export each method along with data dependencies in an atom to graphml or dot format. Simply pass `--export` to enable this feature.

```shell
atom -o app.atom -l java --export-atom --export-dir <export dir> <path to application>
```

The resulting graphml files could be imported into [Neo4j](https://neo4j.com/labs/apoc/4.1/import/graphml/) or NetworkX for further analysis. Use the argument `--export-format` for dot format.

```shell
atom -o app.atom -l java --export-atom --export-format dot --export-dir <export dir> <path to application>
```

In dot format, individual representations such as ast, cdg, and cfg would also get exported.

To also compute and include data-dependency graph (DDG) information in the exported files, pass `--with-data-deps`

```shell
atom -o app.atom -l java --export-atom --export-dir <export dir> --with-data-deps <path to application>
```

## Environment variables

| Variable                                | Description                                                                                                                                                |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CHEN_IGNORE_DIRS**                    | Comma-separated list of directories to ignore for every language frontend.                                                                                 |
| **CHEN_IGNORE_TEST_DIRS**               | Set to true to ignore common test directories (`test`, `tests`, `mocks`) for every language frontend.                                                      |
| **CHEN_C_IGNORE_DIRS**                  | Comma-separated list of additional directories to ignore for the C/C++ and header frontends.                                                               |
| **CHEN_CPP_IGNORE_DIRS**                | Comma-separated list of additional directories to ignore for the C++ frontend.                                                                             |
| **CHEN_JAVA_IGNORE_DIRS**               | Comma-separated list of additional directories to ignore for the Java source frontend.                                                                     |
| **CHEN_JIMPLE_IGNORE_DIRS**             | Comma-separated list of directories to ignore for the Jimple/JAR/Android/APK/DEX frontend.                                                                 |
| **CHEN_SCALA_IGNORE_DIRS**              | Comma-separated list of directories to ignore for the Scala frontend.                                                                                      |
| **CHEN_JAVASCRIPT_IGNORE_DIRS**         | Comma-separated list of directories to ignore for the JavaScript, TypeScript, and Flow frontend.                                                           |
| **CHEN_JS_IGNORE_DIRS**                 | Alias for JavaScript, TypeScript, and Flow ignored directories.                                                                                            |
| **CHEN_TYPESCRIPT_IGNORE_DIRS**         | Alias for TypeScript and Flow ignored directories.                                                                                                         |
| **CHEN_PYTHON_IGNORE_DIRS**             | Comma-separated list of directories to ignore for Python. If unset, Atom uses Python's default ignored directories.                                        |
| **CHEN_PHP_IGNORE_DIRS**                | Comma-separated list of additional directories to ignore for the PHP frontend.                                                                             |
| **CHEN_RUBY_IGNORE_DIRS**               | Comma-separated list of additional directories to ignore for the Ruby frontend.                                                                            |
| **CHEN_DELOMBOK_MODE**                  | Delombok mode for the Java frontend (`no-delombok`, `default`, `types-only`, `run-delombok`).                                                              |
| **CHEN_INCLUDE_PATH**                   | Include directories for the C frontend. Separate paths with `:` or `;`.                                                                                    |
| **CHEN_ASTGEN_OUT**                     | Existing astgen output directory. Improves performance for JavaScript, TypeScript, and Flow during repeated invocations by reusing existing AST json data. |
| **ATOM_TOOLS_OPENAPI_FORMAT**           | OpenAPI format for atom-tools. Default: `openapi3.1.0`; alternative: `openapi3.0.1`.                                                                       |
| **ATOM_TOOLS_WORK_DIR**                 | Working directory for atom-tools. Defaults to atom input path.                                                                                             |
| **ATOM_SCALASEM_WORK_DIR**              | Working directory for scalasem. Defaults to atom input path.                                                                                               |
| **ATOM_SCALASEM_SLICES_FILE**           | Slices file name. Defaults to `semantics.slices.json`.                                                                                                     |
| **ATOM_JVM_ARGS**                       | Overrides the JVM arguments, including heap memory values, constructed by the atom Node.js wrapper.                                                        |
| **ATOM_JAVA_HOME**                      | Java 21 or above to be used by atom.                                                                                                                       |
| **PHP_CMD**                             | Overrides the PHP command used by the PHP frontend.                                                                                                        |
| **PHP_PARSER_BIN**                      | Overrides the php-parse command used by the PHP frontend.                                                                                                  |
| **SCALA_CMD**                           | Overrides the scala command.                                                                                                                               |
| **SCALAC_CMD**                          | Overrides the scalac command used by the scala frontend.                                                                                                   |
| **ASTGEN_IGNORE_DIRS**                  | Comma-separated list of directories to ignore by the JavaScript astgen pre-processor command.                                                              |
| **ASTGEN_IGNORE_FILE_PATTERN**          | File pattern to ignore by the JavaScript astgen pre-processor command.                                                                                     |
| **ASTGEN_INCLUDE_NODE_MODULES_BUNDLES** | Also include source code from node_modules directory. Makes the flows more complete at the cost of increased memory use.                                   |
| **JAVA_CMD**                            | Overrides the java command.                                                                                                                                |
| **RUBY_CMD**                            | Overrides the Ruby command.                                                                                                                                |

## atom Specification

The intermediate representation used by atom is available under the same open-source license (MIT). The specification is available in [protobuf](./specification/atom.proto), [markdown](./specification/docs/spec.md), and [html](./specification/docs/spec.html) formats.

The current specification version is 1.0.0

## Generating atom files

atom files (app.⚛ or app.atom) are zip files with serialized protobuf data. atom cli is the preferred approach to generate these files. It is possible to author a generator tool from scratch using the [proto specification](./specification/atom.proto). We offer a sample in [Python](./specification/samples/python-atomgen/README.md) for interested users. We also offer proto bindings in additional languages which can be found [here](./specification/bindings/).

Example code snippet for generating an atom in python.

```python
# Create a method fullname property
methodFullName = atom.CpgStructNodeProperty(
    name=atom.NodePropertyName.FULL_NAME, value=atom.PropertyValue("main")
)

# Create a method node with the fullname property
method = atom.CpgStructNode(
    key=1, type=atom.NodeType.METHOD, property=[methodFullName]
)

# Create an atom with a single node
atom_struct = atom.CpgStruct(node=[method])

# Create an atom (app.atom) by serializing this data into a zip file
with ZipFile("app.atom", "w") as zip_file:
    zip_file.writestr("cpg.proto", bytes(atom_struct))
```

## License

MIT

## Developing / Contributing

Install Java 21
Node.js > 21

```shell
sbt clean stage scalafmt test createDistribution
cd wrapper/nodejs
bash build.sh && sudo npm install -g .
```

## Using atom with chennai

[chennai](https://github.com/AppThreat/chen) is the recommended query interface for working with atom.

```shell
chennai> importAtom("/home/almalinux/work/sandbox/apollo/app.atom")
```

## atom tools

Checkout [atom-tools](https://github.com/AppThreat/atom-tools) for some project ideas involving atom slices.

## devenv setup

Install devenv by following the official [instructions](https://devenv.sh/getting-started/).

```shell
devenv shell
```

Language-specific profile:

```shell
# Ruby environment
devenv --option config.profile:string ruby shell

# php environment
devenv --option config.profile:string php shell
```

## Advanced Configuration

For complex projects, specifically those written in C or C++, you may need to pass granular configuration options to the underlying language frontend. You can achieve this using the `--frontend-args` flag.

This flag accepts a comma-separated list of key-value pairs in the format `key=value`.

### Usage

```bash
--frontend-args key1=value1,key2=value2,key3=value3
```

### Supported Arguments (C/C++)

The following arguments are supported when `--language` is set to `c`, `cpp`, or `c++`.

| Key                    | Type    | Description                                                                     | Example                       |
| :--------------------- | :------ | :------------------------------------------------------------------------------ | :---------------------------- |
| `defines`              | List    | Comma-separated preprocessor definitions.                                       | `defines=DEBUG,VERSION=2`     |
| `includes`             | List    | Additional header include paths.                                                | `includes=/opt/local/include` |
| `cpp-standard`         | String  | The C++ standard version to use.                                                | `cpp-standard=c++17`          |
| `function-bodies`      | Boolean | Whether to extract function bodies.                                             | `function-bodies=false`       |
| `parse-inactive-code`  | Boolean | Parse code within disabled preprocessor blocks (e.g., inside `#if 0`).          | `parse-inactive-code=true`    |
| `with-image-locations` | Boolean | Create image locations (explains how a name made it into the translation unit). | `with-image-locations=true`   |
| `enable-ast-cache`     | Boolean | Cache parsed ASTs to disk to speed up subsequent runs on unchanged files.       | `enable-ast-cache=true`       |
| `ast-cache-dir`        | String  | Directory to store cached AST files. Defaults to `ast_out` in input directory.  | `ast-cache-dir=/tmp/cache`    |
| `only-ast-cache`       | Boolean | Only generate AST cache files and exit. Useful for large projects to avoid OOM. | `only-ast-cache=true`         |

> **Note:** Boolean values must be passed as the strings `true` or `false`.

### Examples

**1. Setting C++ Standard and Defines**
Generate an atom for a C++ project using C++17.

```bash
java -jar atom.jar \
  --language c++ \
  --frontend-args cpp-standard=c++17 \
  ./my-cpp-project
```

**2. Handling Custom Include Paths**
If your project relies on headers located outside the source tree:

```bash
java -jar atom.jar \
  --language c \
  --frontend-args includes=/usr/local/include,/opt/mylib/include \
  ./src
```

**3. Parsing Inactive Code**
To include code hidden behind preprocessor directives (like `#ifdef WINDOWS` when running on Linux):

```bash
java -jar atom.jar \
  --language c \
  --frontend-args parse-inactive-code=true \
  ./src
```

**4. Large Projects: Two-Stage Generation (Memory Optimization)**
For very large C/C++ codebases, generating the full graph in one pass might consume too much memory. You can split the process into two stages using the AST cache.

_Stage 1: Generate AST Cache Only_
This parses files one by one and saves their ASTs to disk (`./src/ast_out` by default), keeping memory usage low.

```bash
java -jar atom.jar \
  --language c \
  --frontend-args only-ast-cache=true,ast-cache-dir=/tmp/cache \
  ./src
```

_Stage 2: Generate Atom from Cache_
Run the command again with caching enabled. It will load the pre-computed ASTs from disk, significantly speeding up graph creation.

```bash
java -jar atom.jar \
  --language c \
  --frontend-args enable-ast-cache=true,ast-cache-dir=/tmp/cache \
  ./src
```

## Troubleshooting

### atom file is incomplete for large JS/TS projects

astgen might require a generous heap of memory for large JavaScript projects, especially flow projects. Use the environment variable `NODE_OPTIONS` to increase the memory available.

```bash
export NODE_OPTIONS="--expose-gc --max-old-space-size=16288"
```

For large projects such as React 19, astgen requires over 80 GB of heap memory! Use the environment variable `CHEN_ASTGEN_OUT` to make atom and chen, reuse any existing directory containing astgen generated json and typemap files.

To improve the accuracy further, include source code from the `node_modules` directory by setting `ASTGEN_INCLUDE_NODE_MODULES_BUNDLES`.

```bash
export ASTGEN_INCLUDE_NODE_MODULES_BUNDLES=true
export ASTGEN_IGNORE_DIRS=""
```

## Enterprise support

Enterprise support including custom language development and integration services is available via AppThreat Ltd.

## Sponsors

YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.
YourKit is the creator of <a href="https://www.yourkit.com/java/profiler/">YourKit Java Profiler</a>, <a href="https://www.yourkit.com/dotnet-profiler/">YourKit .NET Profiler</a>, and <a href="https://www.yourkit.com/youmonitor/">YourKit YouMonitor</a>.

![YourKit logo](./specification/yklogo.png)
