---
sidebar_position: 5
title: CLI Usage
---

# CLI Usage

```
Usage: atom [parsedeps|data-flow|usages|callsites|reachables|memory-safety|export|algorithms] [options] [input]

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
  --parse-report <value>   Opt-in per-file parse report: pass a file path to have atom write one JSON document naming every file the frontends left out or only partly read, with the outcome (unparsed, dropped, partial), the stage and the parser's reason. Separate a broken project from a quietly incomplete one with it. Written even when the run fails.
  --cpp-standard <value>   C/C++ standard, e.g. c++17, c++20. (C/C++ only)
  --define <value>         Define a preprocessor name. Repeatable. (C/C++ only)
  --auto-defines           Run a macro census first and define the build-option macros (CONFIG_*, ENABLE_*, template-declared) that hide #if code. Opt-in: it changes what is analysed. (C/C++ only)
  --suggest-defines <file>
                           Write the macro census to <file>.json and a reviewable --macro-files header to <file>.h, then exit; with --auto-defines, write it and continue. (C/C++ only)
  --include-path <value>   Header include path. Repeatable. (C/C++ only)
  --compile-commands <file|dir>
                           Parse the translation units of a JSON compilation database (compile_commands.json, or a directory holding one) with their own flags. (C/C++ only)
  --compile-commands-only  With --compile-commands, parse only the database's translation units. (C/C++ only)
  --c-build-contexts <first|all>
                           With a compilation database that compiles one file more than once: analyse the first entry's build context (first, the default) or every distinct one (all, a function whose body differs keeps one copy per context). (C/C++ only)
  --c-isolate              Parse each translation unit in a worker process of its own; a unit whose worker exits, hangs or runs out of memory is reported with the reason and retried once in a fresh worker. (C/C++ only)
  --c-frontend <cdt|edg|edg-fallback>
                           The C/C++ frontend: cdt (default, the Eclipse CDT parser), edg (the EDG front end, through the edga exporter), or edg-fallback (edg, with cdt for the files edga cannot export). (C/C++ only)
  --edga-path <file>       The edga binary for the edg frontends (default: EDGA_PATH, then the PATH). (C/C++ only)
  --delombok-mode <value>  Delombok strategy: no-delombok|default|types-only|run-delombok. (Java only)
  --jdk-path <value>       JDK used to resolve builtin Java types. (Java, JVM bytecode and Scala)
  --fetch-deps             Fetch dependency jars for extra type information. (Java only)
  --no-build-tools         Never run the project's build tools: no Maven or Gradle dependency fetch and no delombok for Java sources, and no scalasem (sbt) for Scala. Overrides --fetch-deps, --delombok-mode and the same keys from --frontend-args or a config file.
  --ts-types <value>       Resolve types from TypeScript declarations (default: true). (JS/TS only)
  --flow                   Enable Flow mode. (JS only)
  --venv-dir <value>       Virtual-environment directory (default: .venv). (Python only)
  --ignore-paths <value>   Comma-separated paths to ignore from analysis. (Python only)
  --android-sdk <value>    Path to android.jar, a platform directory holding it, or an SDK directory. (Jimple/Android only)
  --app-prefix <value>     Application package of a bytecode input; repeatable. Replaces the boundary the APK or jar manifest would give, and the jar's package prefixes. (Jimple only)
  --external-paths <value>
                           Regular expression of vendored input paths (third_party/, a copied single-header library); matched files are parsed as always but their methods and types are marked external, and uses of them from the project become callsites records. The match is relative to the input root, with / as separator, and begins at a path segment (third_party does not match src/not_third_party). A BOM given with --bom-file or --bom-dir marks the directories of its components (cdx:vendored:path) the same way. (Not for bytecode inputs.)
  --solver-depth <value>   Recursive jar unpacking depth (default: 1). (Jimple/Scala only)
  --full-resolver          Enable whole-program, transitive call resolution. (Jimple/Scala only)
  --php-ini <value>        php.ini path for the PHP parser. (PHP only)
  --disable-type-stubs     Disable type-stub based type recovery. (Ruby only)
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --remove-atom            do not persist the atom file - defaults to `false`
  -x, --export-atom        export the atom file with data-dependencies to graphml - defaults to `false`
  --reuse-atom             reuse existing atom file - defaults to `false`. The memory-safety command reuses an atom any command built: when the atom carries no memory-safety overlay tags, the overlay runs over the loaded graph in memory and the atom file itself is not changed.
  --export-dir <value>     export directory. Default: atom-exports
  --export-format <value>  export format graphml or dot. Default: graphml
  --config <value>         path to a JSON config file with frontend options and settings for the export and algorithms commands. An empty file sets nothing.
  --project-config         read atom.json, .atom/atom.json or .atom/config.json from the input directory when --config is not given. Off by default, since that file comes from the analysed project.
  --memory-api-config <value>
                           path to a JSON file (memory-apis.json schema) merged over the built-in memory-API inventory by API name, declaring in-house wrappers or platform argument roles. (C/C++ only)
  --validation-config <value>
                           path to a JSON file declaring validators/sanitisers (chennai.json schema). Reachable flows passing through a declared sanitiser are dropped for its categories.
  --bom-file <value>       the project's CycloneDX BOM or VDR, e.g. from cdxgen. Reachables takes package purls and component tags from it; without it or --bom-dir, a BOM in the input directory is used.
  --bom-dir <value>        a directory of CycloneDX BOMs: every *bom*.json, *.cdx.json and *.vdr.json under it is used as with --bom-file.
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
  --also-callsites <value>
                           also write the callsites slice to the given path: the atom is built and tagged once for both slices.
Command: callsites
Extract one record per use of an imported module, class or header, for every language atom reads
Command: reachables [options]
Extract reachable data-flow slices based on automated framework tags
  --source-tag <value>     source tag - defaults to framework-input. Comma-separated values allowed.
  --sink-tag <value>       sink tag - defaults to framework-output. Comma-separated values allowed.
  --slice-depth <value>    the max depth to traverse the DDG during reverse reachability - defaults to 7.
  --include-crypto         includes crypto library flows - defaults to false.
  --profile <value>        reduce false positives with a flow-filtering profile: appsec, generic. Defaults to generic (no extra filtering).
Command: memory-safety [options]
Run the memory-safety overlay and write findings (rule, cwe, kind, confidence, flow) as JSON or SARIF. With --reuse-atom the overlay runs over the loaded graph when the reused atom carries no overlay tags; the atom file itself is not changed.
  --min-confidence <value>
                           drop findings below this confidence: high, medium or low. Defaults to keeping all.
  --format <value>         output format: json or sarif (SARIF 2.1.0). Default: json.
  --include-system-headers
                           keep findings located in a file outside the analysed directory, such as a header from a system include directory. They are dropped by default; kept ones print their absolute path.
  --memory-api-profiles <value>
                           which declared memory-API library profiles the overlay applies: auto selects per translation unit by the headers it includes, none turns profiles off, or a comma list of profile names forces exactly those. Default: auto. (C/C++ only)
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
  --version                print the versions of atom, chen, atom-parsetools and parsa, then exit
  --help                   display this help message
```

## Call-site records

`callsites` writes one record per use of an imported module, class or header, for JavaScript and
TypeScript, Python, Java source, PHP, Ruby, JVM bytecode (jar, class directory, dex, APK) and C and
C++. The records, their `kind` and `via` values and the rules for `builtin` and `purl` are in
[Lesson 9](lessons/callsites.md). A language that has no source writes an empty slice. The options
that change the records are these:

| Option | Languages | Effect on the slice |
| :----- | :-------- | :------------------ |
| `--external-paths <regex>` | every language except JVM bytecode | Marks matching input paths as vendored code; uses of it become records with the vendored directory as module. |
| `--bom-file`, `--bom-dir` | every language | A BOM supplies the purls of JavaScript and Python modules and the locations of vendored components (`cdx:vendored:path`, `cdx:cmake:sourceDir`); for JVM bytecode, its root component's group sets the application boundary instead. |
| `--app-prefix <package>` | JVM bytecode | The application's packages; everything else is a dependency. Needed for a bare `.dex`. |
| `--android-sdk`, `--jdk-path` | JVM bytecode, Java | Where the frontend finds the platform classes. |
| `--compile-commands`, `--include-path`, `--c-frontend` | C and C++ | The project's build directories and header search paths decide which headers are the project's own; `--c-frontend edg` uses `edga`. |

`usages --also-callsites <file>` writes the usages slice and the callsites slice from one run.
The same input gives the same bytes on every run and on every build of the atom.

## Memory-safety findings as SARIF

`memory-safety --format sarif` writes the findings as a SARIF 2.1.0 log, for code-scanning tools and IDEs that read SARIF:

```bash
atom memory-safety -l c --format sarif -s findings.sarif /path/to/project
```

- each rule that has a finding is listed under `runs[0].tool.driver.rules`, with its CWE as a tag and `helpUri`, its severity as the default `level`, and its confidence as `precision`;
- each finding is a result with its rule, its level, and its location. A path inside the project is relative to `%SRCROOT%` (the analysed directory), and a path outside it (a system header) is an absolute `file:` URI;
- the evidence the rule read becomes the result's `codeFlows`, one location per fact.

## The memory-safety command

`memory-safety` builds the atom, runs chen's memory-safety overlay (eight passes that read what
the C and C++ frontends recorded, conclude per-method memory semantics, track each pointer's
state through the control flow, and report) and writes the findings. Every finding names a rule
(`MS-ALLOC-001` double free, `MS-ALLOC-002` use after free, `MS-ALLOC-003` leak, `MS-ALLOC-004`
double close, `MS-ALLOC-005` free of non-heap storage, `MS-ALLOC-006` free of an offset pointer,
`MS-ALLOC-007` mismatched deallocation, beside the `MS-BOUND-*`, `MS-INT-*` and other families),
its CWE, a confidence (`high`, `medium` or `low`) and the flow of facts that produced it.
`--min-confidence medium` keeps the findings whose evidence earned at least `medium`.

A rule carries a default confidence, and where the evidence makes a weaker claim the finding
reports below it: a use after a region reset, after a release that reached zero, a use of a block
a destroy may not have reached, and a block stored into storage that outlives a scope whose
cleanup releases it, all report at `medium`, while the use after a borrowed release reports at
`low`. The leak rule reports at `low`.

Beside the rule, the CWE, the kind, the confidence and the flow, each finding carries the
`function` it sits in, the `purl` of the BOM component that owns its file when a located
component (`cdx:vendored:path`, `cdx:cmake:sourceDir`) owns it, and a `fingerprint`: SHA-256
over the rule, the function, the file relative to the input, the finding's memory-operation
calls and the finding's place among the findings that share those four. The line is not part
of it, so the fingerprint survives edits that move the finding within its function, which is
what lets a report match a finding across builds. In `--format sarif` the
same value rides under `partialFingerprints` as `atom/memorySafety/v1`. The field list and the
exact fingerprint recipe are published as a
[JSON Schema](https://github.com/AppThreat/atom/blob/main/specification/schemas/MemorySafetyFinding.json).

### Reusing an atom

`memory-safety --reuse-atom` works on an atom any command built. An atom that carries the
overlay marker a memory-safety build wrote renders its findings as they are; this run's
`--memory-api-profiles` and `--memory-api-config` are ignored with a warning, because the
build consumed them. An atom without the marker (built by `usages` or `callsites`) gets the
overlay's passes run over the loaded graph, in memory, following the current run's options,
after the data-flow layer the rules read when the atom was built without `--with-data-deps`.
The atom is loaded through a temporary copy beside it, removed when the command ends, so the
atom file on disk is not rewritten and a later reuse of the same file simply runs the passes
again. An atom built by atom 4.0.x carries the overlay's tags without the marker and is
refused; rebuild it with the memory-safety command. Languages other than C and C++ report zero
findings, as before.

### Findings outside the analysed directory

A finding is located in the file of the code it points at. Some of that code is not the project's: a macro or inline function from a system header (`_ctype.h`, `sys/_types/_fd_def.h` in the macOS SDK, a header under `/usr/include`, an MSVC or Homebrew include directory) is found through an include path, and its index or size contract is the platform's to keep. The command decides by the file's path, not by the name of any SDK: a file under the analysed directory is project code, even when an include path also covers it, and a file anywhere else is not.

By default the findings in files outside the analysed directory are dropped. Only the reported location counts: a finding in project code stays when its flow passes through a system header. `--include-system-headers` keeps the others.

Every path in the output is relative to the analysed directory, written with the directory's name in front (`src/core/ngx_string.c`), or absolute (`/usr/include/sys/_types/_fd_def.h`, `C:/Program Files/.../include/string.h`). A path outside the directory is never joined to the directory's name. In `--format sarif`, a path under the directory is relative to `%SRCROOT%` and a path outside it is an absolute `file:` URI. The `file` of each `flow` row follows the same rule, without the directory's name in front of a path under it.

### Findings of template instances

The instances of one template can carry the same finding, and they print it once. The findings
are grouped by the `template` tag and position of the METHOD they sit in: the finding every
instantiation has is one entry, with an `instances` list naming each instance's `template-args`
(`int`, `Str`), and a finding only some instances have lists only those. Two instances' findings
merge only when they render identically apart from the instance (rule, place, function,
confidence, message and flow); findings that differ in any of these stay separate entries. The
`function` of a finding in an instance names the template, not the instance, so the merged
entry has one function and one fingerprint whichever instance it was kept from. An instance without
`template-args` adds no name to the list. A finding outside an instance METHOD is printed as
rendered, with no `instances` list.

### Allocation styles the overlay reads

The overlay knows the allocation styles C code is written in, from three kinds of evidence:
declared attributes (`malloc`, `alloc_size`, `malloc(dealloc)`, `nonnull`, `access`, and the GCC
`cleanup` attribute both frontends read off a declarator, however a macro spells it), bodies
inferred to a bounded fixpoint (allocators, free wrappers, destroy functions, offset families,
region allocators, reference releases), and the declared inventory in `memory-apis.json` with
the library profiles below. Nothing is decided from a function's name; the name-level guess that
remains reports its findings at `low`.

**Scope cleanup.** Both C frontends place a call to a variable's `cleanup` function at every exit
of the variable's scope: where the block ends, before each `return`, and at a `goto`, `break` or
`continue` that leaves the scope, inner scopes first and in reverse declaration order. The call
is `fn(&var)`, tagged `compiler-generated` and `lifetime-end` with the variable's name. A
variable a cleanup call releases never reports a leak; one already freed and not reset that
reaches its cleanup call is a double free, `MS-ALLOC-001` at `high` on the compiler-placed call,
with the hand-written free left out of the report. A method that returns or stores away a
variable the cleanup goes on to release is summarised `returns-freed`, so the caller's use of the
result is `MS-ALLOC-002` at `high` and the store `MS-ALLOC-002` at `medium`.

```c
static void freep(void *pp) { free(*(void **)pp); }

int bad(int n)
{
    __attribute__((cleanup(freep))) char *buf = malloc(n);
    if (!buf) return -1;
    if (n > 4)
    {
        free(buf);
        return n;   /* MS-ALLOC-001, high, at the freep(&buf) call the scope exit places */
    }
    return 0;       /* no leak finding here: the cleanup releases buf */
}

int good(int n)
{
    __attribute__((cleanup(freep))) char *buf = malloc(n);
    if (!buf) return -1;
    if (n > 4)
    {
        free(buf);
        buf = NULL; /* the reset leaves the cleanup call nothing to release */
        return n;
    }
    return 0;
}
```

**Destroy functions.** A method that frees its parameter on a path only the parameter's own NULL
tests control is summarised as freeing it, even when it frees members first, calls callees that
neither free nor allocate, or assigns to fields, provided it returns no value and frees the
parameter itself. A free gated by a flag or a counter yields no summary. A call to the destroy
moves an argument the method tracks, one holding an allocation that method can see (its own
allocation, or a call summarised as allocating its result), to `freed`; a later use of it is
`MS-ALLOC-002` at `high` and a second call is `MS-ALLOC-001` at `high`. An argument the method
does not track, a bare parameter it only passes on, changes no state the rules read.

```c
#include <stdlib.h>
#include <string.h>

struct acct { char *name; int id; };

static void acct_free(struct acct *a)
{
    if (!a) return;
    free(a->name);
    free(a);
}

int bad(void)
{
    struct acct *a = malloc(sizeof *a);
    if (!a) return -1;
    memset(a, 0, sizeof *a);
    acct_free(a);
    return a->id;   /* MS-ALLOC-002, high: a use after the destroy */
}

int good(void)
{
    struct acct *a = malloc(sizeof *a);
    if (!a) return -1;
    memset(a, 0, sizeof *a);
    int id = 0;
    if (a) id = a->id;
    acct_free(a);
    return id;      /* the value is read before the destroy: no finding */
}
```

**Offset and header-prefix allocator families.** A method whose every non-NULL value return is a
known allocation advanced by one constant amount is an allocator of a family named after it
(`offset:<method>`), normalised to bytes where the types allow, and a method that releases its
parameter moved back by the same amount is that family's free. The pair is matched through the
amount and the underlying family, never through names; a wrapper returning an allocation unchanged
stays in the underlying family. Within one method, a release of a pointer that arithmetic moved off
its block's start is `MS-ALLOC-006` at `high`, reported at the move. A pointer moved back from a
parameter's value is how a header-prefixed allocator frees its own block, and a pointer moved back
by exactly what advanced it in the same method is the block's own start; neither is reported.

```c
typedef struct { long size; } hdr_t;

void bad(void)
{
    char *p = malloc(64);
    free(p + sizeof(hdr_t));   /* MS-ALLOC-006, high: a release off the block's start */
}

void good(void *p)
{
    free((hdr_t *)p - 1);      /* the header-prefix free itself: moved back from a
                                  parameter, so no finding */
}
```

**Declared library profiles.** A file that includes a profiled library's header follows that
library's declared memory management, so `PyMem_Malloc` memory released with `free` is a family
mismatch (`MS-ALLOC-007` at `high`) and released with `PyMem_Free` is correct. The profiles and
the option that selects them are the next section.

```c
#include <pymem.h>

void bad(void)
{
    char *p = PyMem_Malloc(16);
    free(p);          /* MS-ALLOC-007, high: PyMem_Malloc memory goes back to PyMem_Free */
}

void good(void)
{
    char *p = PyMem_Malloc(16);
    PyMem_Free(p);    /* no finding */
}
```

**Regions.** A region is a pool, a context or a device: one handle owns every block handed out
from it. A region allocation is inferred from a body that hands out pointer arithmetic over a
field of one parameter with no allocation behind it, or declared by a profile entry's `region`
role (`create`, `allocate`, `reset`, `destroy`). Region memory is never reported as a leak; a
destroy moves every same-region pointer to `freed` and a reset to `maybe-freed`, so a use after
an unconditional destroy is `MS-ALLOC-002` at `high` and a use after a reset is `MS-ALLOC-002`
at `medium`. A heap free of region memory is a wrong deallocation, `MS-ALLOC-005` at `high` where
the allocator declares only the region role and `MS-ALLOC-007` at `high` where the family
comparison decides, so `pfree` of `palloc` memory is correct while `pfree` of `malloc` memory
and `free` of pool memory are mismatches.

```c
#include <apr_pools.h>
#include <string.h>

int bad(apr_pool_t *pool)
{
    char *p = apr_palloc(pool, 32);
    memset(p, 0, 32);
    apr_pool_destroy(pool);
    return p[0];   /* MS-ALLOC-002, high: a use after the pool's destroy */
}

int good(apr_pool_t *pool)
{
    char *p = apr_palloc(pool, 32);
    memset(p, 0, 32);
    int first = p[0];
    apr_pool_clear(pool);  /* the block goes with the reset, the pool stays */
    return first;          /* no finding, and no leak either: the region owns the block */
}
```

**Reference counting.** For each variable the overlay tracks, it counts the references the
method owns: a new allocation owns one, an acquire adds one, a release drops one. The declared
roles are a profile's `release` and `acquire`; the inferred twins conclude from bodies that grow
or shrink a counter field of one parameter, the release freeing the object under the condition
that the count reached zero, directly or through a callback argument (the kernel's
`kref_put(&o->refs, release)` shape). A release that reaches zero frees the object, so a use
after it is `MS-ALLOC-002` at `medium`; a release of unknown ownership leaves the object maybe
freed, and a use after it is `MS-ALLOC-002` at `low`; a second release below zero is
`MS-ALLOC-001` at `medium`.

```c
/* acct_new, acct_unref and acct_ref are the declarations of the example configuration;
   acct_put and acct_get are inferred, concluded from their bodies. */

struct acct { int refs; int id; };

static void acct_put(struct acct *a)
{
    a->refs -= 1;
    if (a->refs == 0) free(a);
}

static void acct_get(struct acct *a)
{
    a->refs += 1;
}

int bad(void)
{
    struct acct *a = acct_new(sizeof(struct acct)); /* the method owns one reference */
    acct_unref(a);  /* the count reaches zero: freed */
    return a->id;   /* MS-ALLOC-002, medium: a use after the zero-reaching release */
}

int worse(void)
{
    struct acct *a = acct_new(sizeof(struct acct));
    acct_unref(a);  /* reaches zero: freed */
    acct_unref(a);  /* MS-ALLOC-001, medium: a second release below zero */
    return 0;
}

int borrowed(struct acct *a)
{
    acct_put(a);    /* a is a parameter: ownership unknown, so the release is the borrowed kind */
    return a->id;   /* MS-ALLOC-002, low */
}

int good(struct acct *a)
{
    acct_get(a);    /* the acquire and its matching release net out: no finding */
    acct_put(a);
    return 0;
}
```

### Library profiles and `--memory-api-profiles`

The built-in inventory declares seven profiles, one per library whose memory management lives in
headers outside the analysed tree. Every profile entry cites the header or documentation page
that states its behaviour.

| Profile | Selected by a translation unit including | Entries declare |
|---|---|---|
| glib | `glib.h`, `gmem.h`, `glib-object.h`, `gio/gio.h`, `gtk/gtk.h` | `g_new0` (heap, the element count is argument 2), `g_free` (heap; `g_free` is `free` on every platform glib supports), `g_steal_pointer` (steal), `g_autoptr_cleanup_generic_gfree` (the cleanup `g_autofree` attaches) |
| linux | `linux/slab.h`, `linux/kref.h`, `linux/gfp.h`, `linux/vmalloc.h`, `linux/device.h` | `kmalloc`, `kzalloc`, `kcalloc`, `kmalloc_array` (the `slab` family, released by `kfree`), `vmalloc` (the `vmalloc` family, released by `vfree`), `kvmalloc`, `kvzalloc` (the `kvmalloc` family), `kvfree` (releases all three families), `devm_kzalloc`, `devm_kcalloc` (region allocations owned by argument 1, the device), `kref_put` (releases the object of argument 1, calling the callback of argument 2 at zero) |
| postgresql | `postgres.h`, `utils/palloc.h`, `utils/memutils.h` | `palloc`, `palloc0` (the `pgmem` family, allocated from the `CurrentMemoryContext` global), `pfree`, `MemoryContextReset` (reset), `MemoryContextDelete` (destroy), `AllocSetContextCreate` (create) |
| apr | `apr_pools.h`, `httpd.h` | `apr_palloc` (the `aprpool` family, region argument 1), `apr_pool_create` (create through the out-pointer argument 1), `apr_pool_clear` (reset), `apr_pool_destroy` (destroy) |
| nginx | `ngx_core.h`, `ngx_palloc.h` | `ngx_alloc`, `ngx_free` (plain heap wrappers), `ngx_palloc` (the `ngxpool` family, region argument 1), `ngx_create_pool` (create), `ngx_destroy_pool` (destroy) |
| cpython | `Python.h`, `object.h`, `pymem.h` | `PyMem_Malloc`, `PyMem_Free` (the `pymem` family), `Py_DECREF` (release of argument 1), `Py_INCREF` (acquire of argument 1) |
| php | `php.h`, `zend.h`, `zend_API.h`, `zend_alloc.h`, `zend_string.h` | `emalloc`, `efree` (the `zendmm` request heap), `zend_string_release` (release of argument 1) |

The builtin descriptors are inventoried the same way: `open`, `openat`, `creat`, `socket`,
`accept`, `accept4`, `dup` and `dup2` allocate the `fd` family, released by `close` and
`closesocket`; `fopen`, `fdopen`, `freopen`, `tmpfile` and `opendir` allocate the `file` family,
released by `fclose` and `closedir`. A second close of either family is `MS-ALLOC-004` at `high`,
and a release of one family's handle with the other family's function (a `close` of a `FILE *`,
an `fclose` of a descriptor) is `MS-ALLOC-007` at `high`.

A translation unit selects a profile when its primary source file includes one of the profile's
headers, compared by basename, whatever path the include spelled. Precedence is fixed: an
inventory entry (built-in or from `--memory-api-config`) wins for the names it declares; a body
in the analysed tree wins over a profile entry of the same name, whatever the body says; a
profile entry stands above the name-level conclusions; and a call the graph resolves to a method
is judged by that method's own body.

`--memory-api-profiles <auto|none|name,name>` selects how units choose: `auto` (the default) by
the includes each unit carries, `none` turns profiles off, and a comma list (compared without
case) forces those profiles onto every file whose calls name their entries, reporting a name no
profile carries on stderr so a typo'd list cannot analyse quietly as `none`. atom prints the
profiles it followed, with a per-profile file count, beside the function-summary counts, for
example `MemoryApiPass: memory-api profiles: cpython (2 files), linux (1 file)`. The profiles and
`--memory-api-config` apply when the atom is built: `--reuse-atom` keeps the ones the reused atom
was built with, and the command says so on stderr when this run asks for others.

### Declaring in-house APIs with `--memory-api-config`

`--memory-api-config` passes a JSON file of the same schema as `memory-apis.json`. Its `apis` array
is merged over the built-in inventory by API name, an external entry replacing the built-in entry
of the same name, and its `profiles` array is merged by profile name. An unreadable document is
reported on stderr and ignored rather than failing the run, and an unreadable entry inside a
readable document is dropped with its own note; every declared-role field is optional, so an
existing configuration file still decodes. Two family names changed when sockets and files were
split by handle type: a `socket` family in an existing file is read as `fd`, and an entry that
declared an integer-descriptor function with the `file` family must now say `fd`, or a `close` of
its result is reported as a mismatch. A `free` may list several families for a release that accepts
blocks of each, as `kvfree` does: `"free": ["slab", "vmalloc", "kvmalloc"]`.

The declared roles an entry can carry beside the long-standing ones are `release` and `acquire`
(the 1-based index of the argument whose reference count the call drops or takes), `retains` (the
argument whose resource the call's result takes over, as `fdopen` takes its descriptor), `steal`
(the pointer-to-pointer argument the call empties and returns), `cleanup` (true for a function that
takes the address of a variable and releases what it points to), and `region` with one of the roles
`create`, `allocate`, `reset` or `destroy`. The vocabulary validates what nothing in the analysed
tree can confirm: a role index must be a number 1 or greater (a quoted number is a string and is
rejected), a `region` allocate names exactly one of `arg` and `context`, a `create` names no
`context`, and every profile entry needs its `source` citation. A rejected entry is dropped with a
note naming it, and the rest of the file still applies. The `release`, `acquire`, `retains` and
`region` roles reach the call sites as tags; a declared `steal` moves the block out of the slot it
is handed the address of, as the body-inferred steal summary does, and a declared `cleanup`
function releases the block its argument points to, which is what the cleanup calls the frontends
place then do.

The file below declares a small in-house library; chen's c2cpg test suite loads this exact
document from its documentation, so the example tracks what the vocabulary accepts.

```json
{
  "version": 1,
  "apis": [
    {
      "name": "acct_new",
      "alloc": "acctmem",
      "len": 1,
      "nullableReturn": true,
      "source": "acct/core.h:41"
    },
    {
      "name": "acct_del",
      "free": "acctmem",
      "source": "acct/core.h:52"
    }
  ],
  "profiles": [
    {
      "name": "acct",
      "headers": ["acct/pool.h"],
      "entries": [
        {
          "name": "acct_pool_new",
          "region": { "role": "create", "arg": 1 },
          "source": "acct/pool.h:23"
        },
        {
          "name": "acct_pool_alloc",
          "region": { "role": "allocate", "arg": 1 },
          "len": 2,
          "nullableReturn": true,
          "source": "acct/pool.h:34"
        },
        {
          "name": "acct_pool_clear",
          "region": { "role": "reset", "arg": 1 },
          "source": "acct/pool.h:45"
        },
        {
          "name": "acct_pool_delete",
          "region": { "role": "destroy", "arg": 1 },
          "source": "acct/pool.h:56"
        },
        {
          "name": "acct_pool_get",
          "region": { "role": "allocate", "context": "acct_current_pool" },
          "len": 1,
          "source": "acct/pool.h:67"
        },
        {
          "name": "acct_unref",
          "release": 1,
          "source": "acct/object.h:70"
        },
        {
          "name": "acct_ref",
          "acquire": 1,
          "source": "acct/object.h:81"
        },
        {
          "name": "acct_take",
          "steal": 1,
          "source": "acct/object.h:92"
        },
        {
          "name": "acct_cleanup",
          "cleanup": true,
          "source": "acct/object.h:103"
        }
      ]
    }
  ]
}
```

### What the memory-safety overlay does not report

The cpython profile declares `Py_DECREF` as a release and `Py_INCREF` as an acquire; `Py_XDECREF`
and `Py_CLEAR` have no declared entries, because their NULL-guard and reset shapes are not
expressible as a plain release role, so releases written with them are not tracked. The selecting
headers are matched against a unit's own `#include` lines, not the headers those include in turn,
so a unit that reaches a library only through a header of its own selects no profile. A missing
reference release is not reported by design: the count model reports the uses after a release and
the second releases below zero, never an omission. A release does not compose through a wrapper: a
function whose body only calls the inferred releaser draws no summary, so the use after the
wrapper-spent reference draws no finding and the block still reads as live at the exit, reporting
the leak the wrapper actually released at `low`.

## The parse report

`--parse-report <file>` has atom write one JSON document naming every file the frontends left out
or only partly read. The report is written even when the run fails, which is what separates a
broken project from a quietly incomplete one.

| Field | What it holds |
|---|---|
| `atomVersion` | The atom that wrote the report. |
| `language` | The `--language` value the run analysed. |
| `inputRoot` | The analysed input. |
| `total` | The number of records. |
| `counts` | The records counted `byOutcome` (`unparsed`, `dropped`, `partial`) and `byFrontend`. |
| `records` | One record per file left out or partly read: `frontend`, `file`, `outcome`, `stage` (`parse`, `ast`) and `message`, the parser's reason. |

When the frontends also collected how every translation unit was set up for parsing, the document
gains a `units` section: a `total`, counts `byFrontend` and `byMacroSource` (`probed`, `table`,
`host`, `unknown`), and one record per unit. A record names `frontend` (`c` or `edg`), `file`,
`driver`, `entry` (`unknown` when the compiler table knows no such driver), `macroSource`,
`language`, `standard`, `macroFingerprint`, `includeDirectories`, `forcedIncludes` and
`ownedHeaders`. A file the compilation database names more than once also carries every command
in `databaseCommands`, with the position of the one that was used in `usedDatabaseCommand`; a
unit the EDG frontend exported carries `edgaCommandLine`, the exact command line that ran. A run
that collects the units section reads no AST cache entry, so every unit is parsed and reaches the
records.

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
| **ATOM_JAVA_HOME**                      | Java 23 or above to be used by atom.                                                                                                                       |
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
3. **A configuration file** — a JSON file passed with `--config`, or an `atom.json` checked into
   the project root and read with `--project-config`, for repeatable, team-wide settings.

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
| `c-build-contexts`       | String  | With a database that compiles one file more than once: `first` (the default) analyses the first entry's build context, `all` every distinct one. | `c-build-contexts=all`            |
| `c-isolate`              | Boolean | Parse each translation unit in a worker process of its own (CDT frontend).                | `c-isolate=true`                  |
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

The JavaScript, TypeScript, Flow and Ruby front ends cache the output of their parser (astgen and
rbastgen) in `<input>/.chen/astgen`, keyed on a fingerprint of the project. With `--cache none`,
`--cache no-astgen` or `--no-ast-cache` no fingerprint is computed and the parser's `--version` is
not run. When the cache is on, the fingerprint reads the project's own files, skips `.git`, `.hg`,
`.svn` and `.chen`, and describes each file under `node_modules` (and each file that
`CHEN_JS_IGNORE_DIRS` and its aliases exclude) by path, size and modification time without reading
it. A reinstall of the same packages therefore changes the key and parses again, which costs time
but never serves a stale parse. Ruby leaves out the files its exclude pattern covers, which
rbastgen never opens.

### Per-language

| Flag                   | Languages      | Description                                                   |
| :--------------------- | :------------- | :------------------------------------------------------------ |
| `--cpp-standard <std>` | C/C++          | C++ standard, e.g. `c++17`, `c++20`.                          |
| `--define NAME`        | C/C++ (repeat) | Preprocessor define.                                          |
| `--include-path <dir>` | C/C++ (repeat) | Header include path.                                          |
| `--compile-commands <file\|dir>` | C/C++ | Parse the units of a compilation database with their own flags. |
| `--compile-commands-only` | C/C++      | With `--compile-commands`, parse only the database's units.   |
| `--c-build-contexts <first\|all>` | C/C++ | With a database that compiles one file more than once: `first` (default) or every distinct context. |
| `--c-isolate`          | C/C++          | Parse each translation unit in a worker process of its own.   |
| `--c-frontend <name>`  | C/C++          | `cdt` (default), `edg` or `edg-fallback`.                     |
| `--edga-path <file>`   | C/C++          | The edga binary for the EDG frontends (default: `EDGA_PATH`, then `PATH`). |
| `--auto-defines`       | C/C++          | Define the build-option macros a macro census finds.          |
| `--suggest-defines <file>` | C/C++      | Write the macro census to `<file>.json` and `<file>.h`.       |
| `--delombok-mode <m>`  | Java           | `no-delombok` \| `default` \| `types-only` \| `run-delombok`. |
| `--jdk-path <path>`    | Java, JVM      | JDK used to resolve builtin Java types (also jar/scala/apk).  |
| `--fetch-deps`         | Java           | Fetch dependency jars for type information.                   |
| `--no-build-tools`     | Java, Scala    | Never run Maven, Gradle, delombok or scalasem (sbt) in the project; overrides the keys above. |
| `--ts-types <bool>`    | JS/TS          | Resolve types from TypeScript declarations (default: true).   |
| `--flow`               | JS             | Enable Flow mode.                                             |
| `--venv-dir <dir>`     | Python         | Virtual-environment directory (default: `.venv`).             |
| `--ignore-paths <csv>` | Python         | Paths to ignore from analysis.                                |
| `--android-sdk <path>` | Jimple/Android | `android.jar`, a platform directory holding it, or an SDK directory. |
| `--app-prefix <pkg>`   | Jimple (repeat) | Application package of a bytecode input.                     |
| `--external-paths <regex>` | Source languages | Mark vendored paths external; uses of them become call sites. |
| `--solver-depth N`     | Jimple/Scala   | Recursive jar unpacking depth (default: 1).                   |
| `--full-resolver`      | Jimple/Scala   | Whole-program, transitive call resolution.                    |
| `--php-ini <path>`     | PHP            | php.ini path for the PHP parser.                              |
| `--disable-type-stubs` | Ruby           | Disable type-stub based type recovery.                        |

Example combining flags with a config file:

```bash
atom -l java --delombok-mode run-delombok --jdk-path /opt/jdk17 ./my-app
```

## Configuration file

For repeatable settings, pass a JSON file with `--config <path>`. Discovery order (first match
wins):

1. an explicit `--config <path>`;
2. with `--project-config` only, `atom.json` at the root of the analysed input, then
   `.atom/config.json` (or `.atom/atom.json`) under that root;
3. the path in the `ATOM_CONFIG_FILE` environment variable;
4. `~/.config/atom/config.json` for user-wide defaults.

atom does not read a config file from the analysed input unless `--project-config` is given,
because that input is often a repository under review, and several frontend options name programs
or paths that atom hands to its helper processes. When such a file is present and not read, atom
says so on stderr. An empty file, like `{}`, sets nothing, so `--config` with an empty file also
keeps the environment variable and the user-wide file out of the run.

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