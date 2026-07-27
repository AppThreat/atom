---
sidebar_position: 5
title: CLI Usage
---

# CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|reachables] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  -s, --slice-outfile <value>
                           export intra-procedural slices as json
  -l, --language <value>   source language
  --frontend-args <value>  Advanced frontend configuration (key=value). E.g. --frontend-args defines=DEBUG,cpp-standard=c++17
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --remove-atom            do not persist the atom file - defaults to `false`
  -x, --export-atom        export the atom file with data-dependencies to graphml - defaults to `false`
  --reuse-atom             reuse existing atom file - defaults to `false`
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
  --source-tag <value>     source tag - defaults to framework-input.
  --sink-tag <value>       sink tag - defaults to framework-output.
  --include-crypto         includes crypto library flows - defaults to false.
  --help                   display this help message
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

## Advanced Configuration

For complex projects you may need to pass granular configuration options to the underlying language
frontend. There are three complementary ways to do this, in increasing order of declarativeness:

1. **`--frontend-args`** — a comma-separated list of `key=value` pairs, applied to every frontend.
2. **First-class flags** — a curated set of named options (e.g. `--cpp-standard`, `--delombok-mode`)
   that are easier to discover via `--help` and tab-completion.
3. **A configuration file** — a HOCON `atom.conf` checked into the project root for repeatable,
   team-wide settings.

All three feed the same channel, so the precedence is simple: a value supplied on the command line
always wins over the file, which wins over the built-in default.

### Discovering keys: `--frontend-args-keys`

The set of supported keys differs per language. To list the keys (and their types/defaults) for the
selected language, pass `--frontend-args-keys`:

```bash
java -jar atom.jar -l java --frontend-args-keys
```

### `--frontend-args`

This flag accepts a comma-separated list of key-value pairs in the format `key=value`. Keys not
relevant to the selected language are ignored.

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
  --input ./my-cpp-project \
  --frontend-args cpp-standard=c++17
```

**2. Handling Custom Include Paths**
If your project relies on headers located outside the source tree:

```bash
java -jar atom.jar \
  --language c \
  --input ./src \
  --frontend-args includes=/usr/local/include,/opt/mylib/include
```

**3. Parsing Inactive Code**
To include code hidden behind preprocessor directives (like `#ifdef WINDOWS` when running on Linux):

```bash
java -jar atom.jar \
  --language c \
  --input ./src \
  --frontend-args parse-inactive-code=true
```

**4. Large Projects: Two-Stage Generation (Memory Optimization)**
For very large C/C++ codebases, generating the full graph in one pass might consume too much memory. You can split the process into two stages using the AST cache.

_Stage 1: Generate AST Cache Only_
This parses files one by one and saves their ASTs to disk (`./src/ast_out` by default), keeping memory usage low.

```bash
java -jar atom.jar \
  --language c \
  --input ./src \
  --frontend-args only-ast-cache=true,ast-cache-dir=/tmp/cache
```

_Stage 2: Generate Atom from Cache_
Run the command again with caching enabled. It will load the pre-computed ASTs from disk, significantly speeding up graph creation.

```bash
java -jar atom.jar \
  --language c \
  --input ./src \
  --frontend-args enable-ast-cache=true,ast-cache-dir=/tmp/cache
```

---

## First-class frontend flags

The most common frontend knobs are exposed as real CLI flags so they show up in `--help` and accept
typed values. They populate the same channel as `--frontend-args`, so a flag is just a friendlier
spelling of the equivalent key.

### Universal (every language)

| Flag                       | Type   | Description                                              |
| :------------------------- | :----- | :------------------------------------------------------- |
| `--exclude <csv>`          | csv    | Files/folders to exclude (relative to input or absolute).|
| `--exclude-regex <re>`     | string | Regex of file paths to exclude.                          |
| `--no-ast-cache`           | flag   | Disable the on-disk AST cache for this run.              |
| `--cache-dir <dir>`        | string | Directory for the AST cache (default: `<input>/.chen`).  |
| `--schema-check`           | flag   | Enable early schema validation during AST creation.      |
| `--no-dummy-types`         | flag   | Disable placeholder dummy types during type propagation. |
| `--type-prop-iterations N` | int    | Maximum type-propagation iterations.                     |
| `--cache <mode>`           | enum   | Cache mode: `all` \| `none` \| `no-ast` \| `no-cpg` \| `no-astgen` \| `no-summary`. |

### Per-language

| Flag                         | Languages       | Description                                            |
| :--------------------------- | :-------------- | :----------------------------------------------------- |
| `--cpp-standard <std>`       | C/C++           | C++ standard, e.g. `c++17`, `c++20`.                   |
| `--define NAME`              | C/C++ (repeat)  | Preprocessor define.                                   |
| `--include-path <dir>`       | C/C++ (repeat)  | Header include path.                                   |
| `--delombok-mode <m>`        | Java            | `no-delombok` \| `default` \| `types-only` \| `run-delombok`. |
| `--jdk-path <path>`          | Java            | JDK used to resolve builtin Java types.                |
| `--fetch-deps`               | Java            | Fetch dependency jars for type information.            |
| `--ts-types <bool>`          | JS/TS           | Resolve types from TypeScript declarations (default: true). |
| `--flow`                     | JS              | Enable Flow mode.                                      |
| `--venv-dir <dir>`           | Python          | Virtual-environment directory (default: `.venv`).      |
| `--ignore-paths <csv>`       | Python          | Paths to ignore from analysis.                         |
| `--android-sdk <path>`       | Jimple/Android  | Path to `android.jar` for APK analysis.                |
| `--solver-depth N`           | Jimple/Scala    | Recursive jar unpacking depth (default: 1).            |
| `--full-resolver`            | Jimple/Scala    | Whole-program, transitive call resolution.             |
| `--php-ini <path>`           | PHP             | php.ini path for the PHP parser.                       |
| `--disable-type-stubs`       | Ruby            | Disable type-stub based type recovery.                 |

Example combining flags with a config file:

```bash
java -jar atom.jar -l java --delombok-mode run-delombok --jdk-path /opt/jdk17 ./my-app
```

## Configuration file

For repeatable, project-level settings, drop a HOCON (or JSON) file next to the source tree. atom
discovers it automatically — no flag required. Discovery order (first match wins):

1. an explicit `--config <path>`;
2. `atom.conf` or `atom.json` at the root of the analysed input;
3. `.atom/config.conf` (or `atom.conf`/`atom.json`) under that root;
4. the path in the `ATOM_CONFIG_FILE` environment variable;
5. `~/.config/atom/config.conf` for user-wide defaults.

The `[frontend]` section holds universal knobs and an optional `[frontend.<language>]` sub-section
for language-specific knobs. Both are flattened into `--frontend-args`, so the file is just another
source. Command-line flags always override the file.

```hocon
# atom.conf
frontend {
  exclude          = ["target/", "node_modules/"]
  no-dummy-types   = false
  type-prop-iterations = 2

  java {
    delombok-mode = "run-delombok"
    jdk-path      = "/opt/jdk17"
  }
  python {
    venv-dir       = ".venv"
    ignore-paths   = ["build/", "dist/"]
  }
  c {
    cpp-standard = "c++17"
    defines      = ["DEBUG"]
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
    ~/work/AppThreat/atom/atom.sh -l c -o foo.atom --frontend-args enable-ast-cache=true,ast-cache-dir=/home/appthreat/sandbox/mongo/ast_out,only-ast-cache=true $parent
done
```

Re-running atom with the cache led to fewer time-out errors.

```
~/work/AppThreat/atom/atom.sh --with-data-deps -l c -o foo.atom --frontend-args enable-ast-cache=true,ast-cache-dir=/home/appthreat/sandbox/mongo/ast_out $parent
```
