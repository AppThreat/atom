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

![npm](https://img.shields.io/npm/dw/@appthreat/atom)

## Languages supported

- C/C++
- H (C/C++ Header and pre-processed .i files alone)
- Java (Requires compilation)
- Jar
- Android APK (Requires Android SDK. Set the environment variable `ANDROID_HOME` or use the container image.)
- JavaScript
- Flow
- TypeScript
- Python (Supports 3.x to 3.13)
- PHP (Requires PHP >= 7.4. Supports PHP 7.0 to 8.4 with limited support for PHP 5.x)
- Ruby (Requires Ruby 3.4.7. Supports Ruby 1.8 - 3.4.x syntax)
- Scala (WIP)

## Installation

atom comprises a scala core with a Node.js wrapper module. It is currently distributed as a npm package.

```shell
npm install -g @appthreat/atom
atom --help
```

Install cdxgen npm package to generate a Software Bill-of-Materials (SBOM) which is required for reachables slicing.

```shell
npm install -g @cyclonedx/cdxgen --omit=optional
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

## atom native-image (Advanced users only)

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

## CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|reachables] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  -s, --slice-outfile <value>
                           export intra-procedural slices as json
  -l, --language <value>   source language
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
  --help                   display this help message
```

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

Example for a Ruby project with container-based invocation.

```shell
docker run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

Pass the argument `--platform=linux/amd64`, if you face issues with Java or Ruby builds on arm64 architecture.

```shell
docker run --rm --platform=linux/amd64 -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm --platform=linux/amd64 -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom atom usages -l ruby -o /app/app.atom -s /app/usages.slices.json /app
```

For Ruby, there is an alpine-based version available.

```shell
docker run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
# podman run --rm -v /tmp:/tmp -v $(pwd):/app:rw -t ghcr.io/appthreat/atom-alpine-ruby atom usages --extract-endpoints -l ruby -o /app/app.atom -s /app/usages.slices.json /app
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

| Variable                                 | Description                                                                                                                                                |
| ---------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CHEN_IGNORE_TEST_DIRS**                | Set to true to ignore `test` directories. Only supported for Python for now.                                                                               |
| **CHEN_PYTHON_IGNORE_DIRS**              | Comma-separated list of directories to ignore for Python.                                                                                                  |
| **CHEN_DELOMBOK_MODE**                   | Delombok mode for the Java frontend (`no-delombok`, `default`, `types-only`, `run-delombok`).                                                              |
| **CHEN_INCLUDE_PATH**                    | Include directories for the C frontend. Separate paths with `:` or `;`.                                                                                    |
| **CHEN_ASTGEN_OUT**                      | Existing astgen output directory. Improves performance for JavaScript, TypeScript, and Flow during repeated invocations by reusing existing AST json data. |
| **ATOM_TOOLS_OPENAPI_FORMAT**            | OpenAPI format for atom-tools. Default: `openapi3.1.0`; alternative: `openapi3.0.1`.                                                                       |
| **ATOM_TOOLS_WORK_DIR**                  | Working directory for atom-tools. Defaults to atom input path.                                                                                             |
| **ATOM_SCALASEM_WORK_DIR**               | Working directory for scalasem. Defaults to atom input path.                                                                                               |
| **ATOM_SCALASEM_SLICES_FILE**            | Slices file name. Defaults to `semantics.slices.json`.                                                                                                     |
| **ATOM_JVM_ARGS**                        | Overrides the JVM arguments, including heap memory values, constructed by the atom Node.js wrapper.                                                        |
| **ATOM_JAVA_HOME**                       | Java 21 or above to be used by atom.                                                                                                                       |
| **PHP_CMD**                              | Overrides the PHP command used by the PHP frontend.                                                                                                        |
| **PHP_PARSER_BIN**                       | Overrides the php-parse command used by the PHP frontend.                                                                                                  |
| **SCALA_CMD**                            | Overrides the scala command.                                                                                                                               |
| **SCALAC_CMD**                           | Overrides the scalac command used by the scala frontend.                                                                                                   |
| **ASTGEN_IGNORE_DIRS**                   | Comma-separated list of directories to ignore by the JavaScript astgen pre-processor command.                                                              |
| **ASTGEN_IGNORE_FILE_PATTERN**           | File pattern to ignore by the JavaScript astgen pre-processor command.                                                                                     |
| **ASTGEN_INCLUDE_NODE_MODULES_BUNDLES ** | Also include source code from node_modules directory. Makes the flows more complete at the cost of increased memory use.                                   |
| **JAVA_CMD**                             | Overrides the java command.                                                                                                                                |
| **RUBY_CMD**                             | Overrides the Ruby command.                                                                                                                                |

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

## Troubleshooting

### atom file is incomplete for large projects

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
