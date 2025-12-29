# Atom npm wrapper

Atom is a novel intermediate representation for applications and a standalone tool powered by the [chen](https://github.com/AppThreat/chen) library. The intermediate representation (a network with nodes and links) is optimised for operations typically used for application analytics and machine learning, including [slicing](./specification/docs/slices.md) and vectoring.

This package wraps the atom Java distributable and makes it available via the npm package registry. Ensure Java 21 is available in the PATH.

## Usage

```shell
npm install @appthreat/atom
```

For a broader language support, also install `@appthreat/atom-parsetools`.

```shell
npm install -g @appthreat/atom-parsetools
```

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
- Ruby (Requires Ruby 4.0.x. Supports Ruby 1.8 - 4.0.x syntax)
- Scala (WIP)

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

## Environment variables

| Variable                                | Description                                                                                                                                                |
| --------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **CHEN_IGNORE_TEST_DIRS**               | Set to true to ignore `test` directories. Only supported for Python for now.                                                                               |
| **CHEN_PYTHON_IGNORE_DIRS**             | Comma-separated list of directories to ignore for Python.                                                                                                  |
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

## License

MIT
