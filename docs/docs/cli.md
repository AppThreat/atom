---
sidebar_position: 5
title: CLI Usage
---

# CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|reachables|memory-safety|export|algorithms] [options] [input]

  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  -s, --slice-outfile <value>
                           export intra-procedural slices as json
  -l, --language <value>   source language
  --frontend-args <value>  Advanced frontend configuration: comma-separated key=value pairs, where a list value keeps its own commas. Repeatable. E.g. --frontend-args defines=DEBUG,NDEBUG,only-ast-cache=true
  --frontend-args-keys     Print the supported --frontend-args keys for the selected language (-l) and exit.
  --exclude <value>        Comma-separated files/folders to exclude (relative to input or absolute).
  --exclude-regex <value>  Regex of file paths to exclude during analysis.
  --no-ast-cache           Disable the on-disk AST cache for this run (default: enabled).
  --cache-dir <value>      Directory for the AST cache (default: <input>/.chen).
  --schema-check           Enable early schema validation during AST creation.
  --no-dummy-types         Disable placeholder dummy types during type propagation.
  --type-prop-iterations <value>
                           Maximum iterations of type propagation (applies to supported frontends).
  --cache <value>          Cache mode: all (default) | none | no-ast | no-cpg | no-astgen | no-summary.
  --perf-report <value>    Opt-in per-stage performance report: pass a file path to have atom append NDJSON lines (wall ms, applying-thread CPU ms / allocated MB) for every frontend, pass, dataflow and slicing stage. Separate analysis from verification timing with it.
  --cpp-standard <value>   C/C++ standard, e.g. c++17, c++20. (C/C++ only)
  --define <value>         Define a preprocessor name. Repeatable. (C/C++ only)
  --auto-defines           Run a macro census first and define the build-option macros (CONFIG_*, ENABLE_*, template-declared) that hide #if code. Opt-in: it changes what is analysed. (C/C++ only)
  --suggest-defines <file>
                           Write the macro census to <file>.json and a reviewable --macro-files header to <file>.h, then exit; with --auto-defines, write it and continue. (C/C++ only)
  --include-path <value>   Header include path. Repeatable. (C/C++ only)
  --compile-commands <file|dir>
                           Parse the translation units of a JSON compilation database (compile_commands.json, or a directory holding one) with their own flags. (C/C++ only)
  --compile-commands-only  With --compile-commands, parse only the database's translation units. (C/C++ only)
  --c-frontend <cdt|edg|edg-fallback>
                           The C/C++ frontend: cdt (default, the Eclipse CDT parser), edg (the EDG front end, through the edga exporter), or edg-fallback (edg, with cdt for the files edga cannot export). (C/C++ only)
  --edga-path <file>       The edga binary for the edg frontends (default: EDGA_PATH, then the PATH). (C/C++ only)
  --delombok-mode <value>  Delombok strategy: no-delombok|default|types-only|run-delombok. (Java only)
  --jdk-path <value>       JDK used to resolve builtin Java types. (Java, JVM bytecode and Scala)
  --fetch-deps             Fetch dependency jars for extra type information. (Java only)
  --ts-types <value>       Resolve types from TypeScript declarations (default: true). (JS/TS only)
  --flow                   Enable Flow mode. (JS only)
  --venv-dir <value>       Virtual-environment directory (default: .venv). (Python only)
  --ignore-paths <value>   Comma-separated paths to ignore from analysis. (Python only)
  --android-sdk <value>    Path to android.jar for APK analysis. (Jimple/Android only)
  --solver-depth <value>   Recursive jar unpacking depth (default: 1). (Jimple/Scala only)
  --full-resolver          Enable whole-program, transitive call resolution. (Jimple/Scala only)
  --php-ini <value>        php.ini path for the PHP parser. (PHP only)
  --disable-type-stubs     Disable type-stub based type recovery. (Ruby only)
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --remove-atom            do not persist the atom file - defaults to `false`
  -x, --export-atom        export the atom file with data-dependencies to graphml - defaults to `false`
  --reuse-atom             reuse existing atom file - defaults to `false`
  --export-dir <value>     export directory. Default: atom-exports
  --export-format <value>  export format graphml or dot. Default: graphml
  --config <value>         path to a JSON config file for the export and algorithms commands
  --memory-api-config <value>
                           path to a JSON file (memory-apis.json schema) merged over the built-in memory-API inventory by API name, declaring in-house wrappers or platform argument roles. (C/C++ only)
  --validation-config <value>
                           path to a JSON file declaring validators/sanitisers (chennai.json schema). Reachable flows passing through a declared sanitiser are dropped for its categories.
  --file-filter <value>    the name of the source file to generate slices from. Uses regex.
  --method-name-filter <value>
                           filters in slices that go through specific methods by names. Uses regex.
  --method-parameter-filter <value>
                           filters in slices that go through methods with specific types on the method parameters. Uses regex.
  --method-annotation-filter <value>
                           filters in slices that go through methods with specific annotations on the methods. Uses regex.
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation - defaults to 2000
  --legacy-dataflow        use the classic data-flow engine and disable mini-graph fragment caching. By default atom uses the faster, lower-allocation Flux engine with fragment caching enabled.
  input                    source file or directory
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
  --slice-depth <value>    the max depth to traverse the DDG during reverse reachability - defaults to 7.
  --include-crypto         includes crypto library flows - defaults to false.
  --profile <value>        reduce false positives with a flow-filtering profile: appsec, generic. Defaults to generic (no extra filtering).
Command: memory-safety [options]
Run the memory-safety overlay and write findings (rule, cwe, kind, confidence, flow) as JSON or SARIF
  --min-confidence <value>
                           drop findings below this confidence: high, medium or low. Defaults to keeping all.
  --format <value>         output format: json or sarif (SARIF 2.1.0). Default: json.
Command: export [options]
Export the atom to a graph format (dot, graphml, gexf, graphson, neo4jcsv, gnn)
  --format <value>         export format: dot, graphml, gexf, graphson, neo4jcsv or gnn
  --scope <value>          export scope: whole or methods. Default: whole
  --out <value>            output directory. Default: atom-exports
Command: algorithms [options]
Run a graph algorithm over the atom and write the result as JSON
  --type <value>           algorithm: scc, toposort, dominators, paths, centrality, lowest-common-ancestors, dependency-sequencer, union-find, heap-walker, or context-sensitive-paths
  --source <value>         source method full-name pattern for the paths algorithm. Uses regex.
  --target <value>         target method full-name pattern for the paths algorithm. Uses regex.
  --max-depth <value>      maximum path depth for the paths algorithm
  --help                   display this help message
```

## Memory-safety findings as SARIF

`memory-safety --format sarif` writes the findings as a SARIF 2.1.0 log, for code-scanning tools and IDEs that read SARIF:

```bash
atom memory-safety -l c --format sarif -s findings.sarif /path/to/project
```

- each rule that has a finding is listed under `runs[0].tool.driver.rules`, with its CWE as a tag and `helpUri`, its severity as the default `level`, and its confidence as `precision`;
- each finding is a result with its rule, its level, and its location. A path inside the project is relative to `%SRCROOT%` (the analysed directory), and a path outside it (a system header) is an absolute `file:` URI;
- the evidence the rule read becomes the result's `codeFlows`, one location per fact.

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
| **CHEN_INCLUDE_PATH**                   | Include directories for the C frontend. Separate paths with `:` or `;` (only `;` on Windows).                                                              |
| **CHEN_ASTGEN_OUT**                     | Existing astgen output directory. Improves performance for JavaScript, TypeScript, and Flow during repeated invocations by reusing existing AST json data. |
| **ATOM_TOOLS_OPENAPI_FORMAT**           | OpenAPI format for atom-tools. Default: `openapi3.1.0`; alternative: `openapi3.0.1`.                                                                       |
| **ATOM_TOOLS_WORK_DIR**                 | Working directory for atom-tools. Defaults to atom input path.                                                                                             |
| **ATOM_SCALASEM_WORK_DIR**              | Working directory for scalasem. Defaults to atom input path.                                                                                               |
| **ATOM_SCALASEM_REUSE**                 | Set to `true` to reuse an existing scalasem version 2 report of the same project instead of running scalasem again.                                        |
| **ATOM_SCALASEM_SLICES_FILE**           | scalasem report path; a relative path resolves against the scalasem work directory. When unset, the report is written beside the `-s` slices file, else beside the `-o` atom file, else into the input path. |
| **ATOM_JVM_ARGS**                       | Overrides the JVM arguments, including heap memory values, constructed by the atom Node.js wrapper.                                                        |
| **ATOM_JAVA_HOME**                      | Java 21 or above to be used by atom.                                                                                                                       |
| **ATOM_TIMEOUT**                        | Maximum run time in milliseconds, enforced by the atom Node.js wrapper: atom is stopped with SIGTERM, then SIGKILL, and the wrapper exits with status 124. |
| **ATOM_KILL_GRACE_MS**                  | How long the wrapper waits after SIGTERM before it kills atom with SIGKILL. Default: `10000`.                                                              |
| **ATOM_PARENT_PID**                     | Supervising process id; cdxgen sets its own. The wrapper stops atom when it exits, and passes its own id on, so atom exits if the wrapper is killed.       |
| **PHP_CMD**                             | Overrides the PHP command used by the PHP frontend.                                                                                                        |
| **PHP_PARSER_BIN**                      | Overrides the php-parse command used by the PHP frontend.                                                                                                  |
| **SCALA_CMD**                           | Overrides the scala command.                                                                                                                               |
| **SCALAC_CMD**                          | Overrides the scalac command used by the scala frontend.                                                                                                   |
| **ASTGEN_IGNORE_DIRS**                  | Comma-separated list of directories to ignore by the JavaScript astgen pre-processor command.                                                              |
| **ASTGEN_IGNORE_FILE_PATTERN**          | File pattern to ignore by the JavaScript astgen pre-processor command.                                                                                     |
| **ASTGEN_INCLUDE_NODE_MODULES_BUNDLES** | Also include source code from node_modules directory. Makes the flows more complete at the cost of increased memory use.                                   |
| **JAVA_CMD**                            | Overrides the java command.                                                                                                                                |
| **RUBY_CMD**                            | Overrides the Ruby command used by the `rbastgen` wrapper.                                                                                                 |
| **ATOM_RUBY_HOME**                      | Ruby installation directory for the `rbastgen` wrapper, when Ruby is not on `PATH`.                                                                        |
| **RUBY_ASTGEN_BIN**                     | Path to the `ruby_ast_gen` script that the `rbastgen` wrapper runs. The simplest way to test a generator build.                                            |
| **RBASTGEN_PATH**                       | Path to the `rbastgen` executable itself, overriding the one on `PATH`; the `rbastgen.path` system property takes precedence.                              |

## Advanced Configuration

For complex projects you may need to pass granular configuration options to the underlying language
frontend. There are three complementary ways to do this, in increasing order of declarativeness:

1. **`--frontend-args`** — a comma-separated list of `key=value` pairs, applied to every frontend.
2. **First-class flags** — a curated set of named options (e.g. `--cpp-standard`, `--delombok-mode`)
   that are easier to discover via `--help` and tab-completion.
3. **A configuration file** — a JSON `atom.json` checked into the project root for repeatable,
   team-wide settings.

All three feed the same channel, so the precedence is simple: a value supplied on the command line
always wins over the file, which wins over the built-in default.

### Discovering keys: `--frontend-args-keys`

The set of supported keys differs per language. To list the keys (and their types/defaults) for the
selected language, pass `--frontend-args-keys`:

```bash
atom -l java --frontend-args-keys .
```

### `--frontend-args`

This flag accepts a comma-separated list of key-value pairs in the format `key=value`, and may be
given more than once. A list value keeps its own commas (`includes=/a,/b`). Keys not relevant to the
selected language are ignored.

```bash
--frontend-args key1=value1,key2=value2,key3=value3
```

### Supported Arguments (C/C++)

The following arguments are supported when `--language` is `c`, `cpp` or `c++`, and by the
header-only modes `h`, `hpp` and `i`. `atom -l c --frontend-args-keys` lists every key with its
default.

| Key                      | Type    | Description                                                                               | Example                           |
| :----------------------- | :------ | :---------------------------------------------------------------------------------------- | :-------------------------------- |
| `defines`                | List    | Preprocessor definitions.                                                                 | `defines=DEBUG,VERSION=2`         |
| `includes`               | List    | Additional header include paths (alias: `include-paths`).                                 | `includes=/opt/a/include,/opt/b`  |
| `include-files`          | List    | Header files to include in every translation unit.                                        | `include-files=config.h`          |
| `macro-files`            | List    | Files whose macro definitions apply to every translation unit.                            | `macro-files=build/defs.h`        |
| `cpp-standard`           | String  | The C++ standard version to use.                                                          | `cpp-standard=c++17`              |
| `compile-commands`       | String  | A JSON compilation database (or a directory holding `compile_commands.json`): each translation unit is parsed with its own flags, and only its units and the project's headers are parsed. | `compile-commands=build`          |
| `compile-commands-only`  | Boolean | With `compile-commands`, parse only the database's translation units.                     | `compile-commands-only=true`      |
| `auto-defines`           | Boolean | Run the macro census and define the build-option macros it finds before parsing.          | `auto-defines=true`               |
| `macro-census`           | String  | Write the macro census to `<file>.json` and `<file>.h`.                                   | `macro-census=/tmp/census`        |
| `include-auto-discovery` | Boolean | Ask `gcc` and `clang` for the system include paths and guess the project's include dirs.  | `include-auto-discovery=true`     |
| `function-bodies`        | Boolean | Parse function bodies (default `true`).                                                   | `function-bodies=false`           |
| `parse-inactive-code`    | Boolean | Parse code within disabled preprocessor blocks (e.g., inside `#if 0`).                    | `parse-inactive-code=true`        |
| `with-image-locations`   | Boolean | Create image locations (explains how a name made it into the translation unit).           | `with-image-locations=true`       |
| `enable-ast-cache`       | Boolean | Cache parsed ASTs to disk to speed up later runs on unchanged files (default `true`).     | `enable-ast-cache=false`          |
| `ast-cache-dir`          | String  | Directory to store cached AST files. Defaults to `.chen` in the input directory.          | `ast-cache-dir=/tmp/cache`        |
| `only-ast-cache`         | Boolean | Only generate AST cache files and exit. Useful for large projects to avoid OOM.           | `only-ast-cache=true`             |

> **Note:** Boolean values must be passed as the strings `true` or `false`. A list value keeps its
> own commas: `--frontend-args includes=/a,/b,cpp-standard=c++17` sets two include paths and the
> standard.

### Supported Arguments (Python)

The following arguments are supported when `--language` is set to `py` or `python`. The
`python-deps` family controls how installed dependencies (from the virtual environment) enter the
graph - from none at all to the whole dependency tree with method bodies:

| Key                  | Type    | Description                                                                                                                                                                                                                                                     | Example                             |
| :------------------- | :------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | :---------------------------------- |
| `venv-dir`           | String  | Virtual-environment directory. Defaults to `.venv`.                                                                                                                                                                                                             | `venv-dir=/opt/venv`                |
| `ignore-paths`       | List    | Paths to ignore from analysis.                                                                                                                                                                                                                                  | `ignore-paths=build,dist`           |
| `requirements-txt`   | String  | Requirements file name.                                                                                                                                                                                                                                         | `requirements-txt=requirements.txt` |
| `strict-parse`       | Boolean | Fail the run when any statement fails to parse (errors are always summarized).                                                                                                                                                                                  | `strict-parse=true`                 |
| `python-deps`        | Enum    | Dependency treatment: `none` (default) \| `stubs` (signature-only, external) \| `summaries` (stubs + flow summaries) \| `full` (whole dependency tree with bodies, external for attribution, explorable by the engine; opt-in, trades build cost for accuracy). | `python-deps=summaries`             |
| `python-deps-rounds` | Int     | Transitive import-closure rounds for `python-deps=stubs\|summaries` only (`full` ingests everything, unbounded). Default 2 - the imported distributions' own modules; raise to 3-4 for deeper chains.                                                           | `python-deps-rounds=3`              |
| `typeshed-dir`       | String  | Typeshed checkout (with a `stdlib/` subtree) for `python-deps` stubs/summaries/full (`full` ingests stdlib signatures). Falls back to `$CHEN_TYPESHED_DIR`.                                                                                                     | `typeshed-dir=/opt/typeshed`        |

> **Note:** With `python-deps=full`, reachables stays scoped to the project's own code: a flow
> whose source lives in library code is a fact about the library, not a finding about the analyzed
> project, so it is explored but not reported. A flow that merely traverses library code (project
> source -> library -> project sink) survives.

### Examples

**1. Setting C++ Standard and Defines**
Generate an atom for a C++ project using C++17.

```bash
atom -l c++ -o app.atom \
  --frontend-args cpp-standard=c++17,defines=NDEBUG,VERSION=2 \
  ./my-cpp-project
```

**2. Handling Custom Include Paths**
If your project relies on headers located outside the source tree:

```bash
atom -l c -o app.atom \
  --frontend-args includes=/usr/local/include,/opt/mylib/include \
  ./src
```

**3. Parsing Inactive Code**
To include code hidden behind preprocessor directives (like `#ifdef WINDOWS` when running on Linux):

```bash
atom -l c -o app.atom --frontend-args parse-inactive-code=true ./src
```

**4. Large Projects: Two-Stage Generation (Memory Optimization)**
For very large C/C++ codebases, generating the full graph in one pass might consume too much memory. You can split the process into two stages using the AST cache.

_Stage 1: Generate AST Cache Only_
This parses files one by one and saves their ASTs to disk (`./src/.chen` by default), keeping memory usage low.

```bash
atom -l c -o app.atom --frontend-args only-ast-cache=true,ast-cache-dir=/tmp/cache ./src
```

_Stage 2: Generate Atom from Cache_
Run the command again with caching enabled. It will load the pre-computed ASTs from disk, significantly speeding up graph creation.

```bash
atom -l c -o app.atom --frontend-args enable-ast-cache=true,ast-cache-dir=/tmp/cache ./src
```

---

## First-class frontend flags

The most common frontend knobs are exposed as real CLI flags so they show up in `--help` and accept
typed values. They populate the same channel as `--frontend-args`, so a flag is just a friendlier
spelling of the equivalent key.

### Universal (every language)

| Flag                       | Type   | Description                                                                         |
| :------------------------- | :----- | :---------------------------------------------------------------------------------- |
| `--exclude <csv>`          | csv    | Files/folders to exclude (relative to input or absolute).                           |
| `--exclude-regex <re>`     | string | Regex of file paths to exclude.                                                     |
| `--no-ast-cache`           | flag   | Disable the on-disk AST cache for this run.                                         |
| `--cache-dir <dir>`        | string | Directory for the AST cache (default: `<input>/.chen`).                             |
| `--schema-check`           | flag   | Enable early schema validation during AST creation.                                 |
| `--no-dummy-types`         | flag   | Disable placeholder dummy types during type propagation.                            |
| `--type-prop-iterations N` | int    | Maximum type-propagation iterations.                                                |
| `--cache <mode>`           | enum   | Cache mode: `all` \| `none` \| `no-ast` \| `no-cpg` \| `no-astgen` \| `no-summary`. |

### Per-language

| Flag                   | Languages      | Description                                                   |
| :--------------------- | :------------- | :------------------------------------------------------------ |
| `--cpp-standard <std>` | C/C++          | C++ standard, e.g. `c++17`, `c++20`.                          |
| `--define NAME`        | C/C++ (repeat) | Preprocessor define.                                          |
| `--include-path <dir>` | C/C++ (repeat) | Header include path.                                          |
| `--delombok-mode <m>`  | Java           | `no-delombok` \| `default` \| `types-only` \| `run-delombok`. |
| `--jdk-path <path>`    | Java, JVM      | JDK used to resolve builtin Java types (also jar/scala/apk).  |
| `--fetch-deps`         | Java           | Fetch dependency jars for type information.                   |
| `--ts-types <bool>`    | JS/TS          | Resolve types from TypeScript declarations (default: true).   |
| `--flow`               | JS             | Enable Flow mode.                                             |
| `--venv-dir <dir>`     | Python         | Virtual-environment directory (default: `.venv`).             |
| `--ignore-paths <csv>` | Python         | Paths to ignore from analysis.                                |
| `--android-sdk <path>` | Jimple/Android | Path to `android.jar` for APK analysis.                       |
| `--solver-depth N`     | Jimple/Scala   | Recursive jar unpacking depth (default: 1).                   |
| `--full-resolver`      | Jimple/Scala   | Whole-program, transitive call resolution.                    |
| `--php-ini <path>`     | PHP            | php.ini path for the PHP parser.                              |
| `--disable-type-stubs` | Ruby           | Disable type-stub based type recovery.                        |

Example combining flags with a config file:

```bash
atom -l java --delombok-mode run-delombok --jdk-path /opt/jdk17 ./my-app
```

## Configuration file

For repeatable, project-level settings, drop a JSON file next to the source tree. atom
discovers it automatically — no flag required. Discovery order (first match wins):

1. an explicit `--config <path>`;
2. `atom.json` at the root of the analysed input;
3. `.atom/config.json` (or `atom.json`) under that root;
4. the path in the `ATOM_CONFIG_FILE` environment variable;
5. `~/.config/atom/config.json` for user-wide defaults.

The `frontend` object holds universal knobs and an optional `frontend.<language>` sub-object
for language-specific knobs. Both are flattened into `--frontend-args`, so the file is just another
source. Command-line flags always override the file.

```json
{
  "frontend": {
    "exclude": ["target/", "node_modules/"],
    "no-dummy-types": false,
    "type-prop-iterations": 2,
    "java": {
      "delombok-mode": "run-delombok",
      "jdk-path": "/opt/jdk17"
    },
    "python": {
      "venv-dir": ".venv",
      "ignore-paths": ["build/", "dist/"]
    },
    "c": {
      "cpp-standard": "c++17",
      "defines": ["DEBUG"]
    }
  }
}
```

The same file can also carry graph-command keys (`type`, `source`, `target`, `maxDepth`, `out` for
`algorithms`; `format`, `scope`, `exportDir` for `export`) at the top level, so existing flat-JSON
config files continue to work unchanged.

---

## Tips & Tricks

### c/++ monorepos:

Given a large monorepo of C/C++ source code (such as mongodb), atom and chen cannot reliably determine the base directory to use for all of them. These base directories are crucial and are often set by the build tools such as CMake, Ninja, etc., to successfully compile the project.

A trick we used recently is to first run atom in `only-ast-cache` mode from the parent directories of src, include, and source.

```shell
find . -type d \( -name "src" -o -name "source" -o -name "include" \) -print0 | \
xargs -0 -n1 dirname | \
sort -u -r | \
while read -r parent; do
    echo "Processing: $parent"
    atom -l c -o app.atom --frontend-args enable-ast-cache=true,ast-cache-dir=/tmp/mongo-ast-cache,only-ast-cache=true "$parent"
done
```

Re-running atom with the cache led to fewer time-out errors.

```shell
atom --with-data-deps -l c -o app.atom --frontend-args enable-ast-cache=true,ast-cache-dir=/tmp/mongo-ast-cache .
```
