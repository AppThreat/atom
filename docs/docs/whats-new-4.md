---
sidebar_position: 2
title: What is new in atom 4.0
---

# What is new in atom 4.0

atom 4.0 is built on [chen 4.0](https://github.com/AppThreat/chen/blob/main/docs/v4/README.md). It
adds a slice that records every use of a dependency for every language, a memory-safety command for
C and C++, a second C and C++ frontend built on the EDG front end, and makes slicing faster and its
output identical from run to run. This page lists what changed since atom 3.2.1 and what tools that
read atom's output need to know.

## New commands and slices

- **`callsites`** writes one record per use of an imported module, class, function or header, at
  the position of the use: `file`, `line`, `col`, `method`, `module`, `path`, `kind`, `via`,
  `resolvedMethod`, `builtin`, `declaringModule` and, when known, `purl`. It covers JavaScript and
  TypeScript, Python, Java source, PHP, Ruby, JVM bytecode (jars, class directories, DEX and APKs)
  and C and C++. Package URLs come from a CycloneDX BOM, `java-classpath.json`, Composer's
  `installed.json`, `Gemfile.lock` and installed gems, or the local Gradle and Maven caches,
  depending on the language. Records are ordered totally, so the output is byte-identical across
  runs. [Lesson 9](lessons/callsites.md) describes them per language.
- **`usages --also-callsites <file>`** writes the usages and callsites slices from one process,
  with the same output as running the two commands.
- **`memory-safety`** runs chen's memory-safety overlay on C and C++ and writes findings with a rule
  id, CWE, confidence and flow, as JSON or SARIF 2.1.0 (`--format sarif`). `--min-confidence`
  filters them, and `--memory-api-config` declares in-house allocation and copy wrappers.

## C and C++

- **`--c-frontend cdt|edg|edg-fallback`** chooses the parser. `cdt`, the Eclipse CDT parser, is the
  default. `edg` uses the EDG front end through [edga](https://github.com/AppThreat/edga) 0.2.0,
  which resolves overloads, templates, implicit conversions and object lifetimes as a compiler
  does; `edg-fallback` parses with CDT the units edga cannot export. `--edga-path` or `EDGA_PATH`
  name the binary. See [C/C++ frontends](c-frontends.md).
- **`--compile-commands <file|dir>`** parses each translation unit of a compilation database with
  its own flags, and `--compile-commands-only` limits the run to them.
- **`--auto-defines`** and **`--suggest-defines <file>`** run a census of the configuration macros
  a project tests and define, or write out, the ones that hide code.
- The default ignore pattern matches path segments under the input root, so an input directory
  whose name contains an ignored word is no longer skipped, and atom says how many files a pattern
  dropped.

## Vendored code and BOMs

- **`--external-paths <regex>`** marks vendored directories (`third_party/`, a copied
  single-header library) external in every source frontend: their code is parsed, but treated as a
  dependency, and uses of it become call sites.
- **`--bom-file`** and **`--bom-dir`** give atom the project's CycloneDX BOMs. Reachables takes
  package URLs and component tags from them, callsites attributes records to packages, and the
  directories a BOM records as vendored are marked external.
- **`--app-prefix`** sets the application package of a bytecode input, where the manifest does not.

## Languages

- Python 3.15, the PHP 8.5 pipe operator and 8.4 property hooks, Java source read in the encoding
  the build declares and Lombok without delombok, and Ruby constants and calls from outside the
  project.
- JavaScript and TypeScript use atom-parsetools 2.x, which writes type maps with `parsa`, its
  native TypeScript 7 engine, when installed. `--frontend-args astgen-types=ts6` selects TypeScript
  6. atom-parsetools 1.x still works.
- JavaScript reachables find flows without an SBOM for Angular, DOM APIs, Next.js server actions
  and SvelteKit.
- The native images read JDK classes for the bytecode languages from an installed JDK
  (`--jdk-path`, `ATOM_JAVA_HOME`, `JAVA_HOME` and others), so jars and APKs work there too.
- PHP projects skip `vendor/` at any depth as dependency code: usages on symfony-demo went from 74
  seconds and 11 GB to 2 seconds and 0.3 GB.

## Performance and determinism

- Reachables deduplicates flows with an index instead of pairwise comparison: on DefectDojo the
  reachables stage went from 140 seconds and 231 GB allocated to 95 seconds and 72 GB, with
  identical output.
- Tags are computed once per input state, so callsites on a saved atom does not tag again (juice-shop:
  5.2 to 2.7 seconds).
- Usages has no per-slice timeout any more, so its output no longer depends on machine load, and
  six runs on DefectDojo gave identical slices.
- The callsites, usages and memory-safety outputs are byte-identical across runs; callsites is
  tested by building graphs in reversed and shuffled node order.
- Semantics are built once per graph: on libavformat the old per-step rebuild created 4.5 TB of
  garbage in 155 seconds.

## What changes for consumers of atom's output

Tools that read the slices, such as cdxgen, dep-scan and atom-tools, should know about these
changes:

- **Usages, JavaScript and TypeScript:** `resolvedMethod` holds the resolved name, not source text;
  the source text is in the new `code` field, next to `module` and `symbol`. Each decorator is an
  ANNOTATION entry and each heritage clause an EXTENDS entry.
- **Usages, all languages:** there is one entry per external call site, at the call, so counts and
  positions change (cdxgen's usage recall went from 0.713 to 0.887). A method's definition no
  longer takes its name and line from a caller.
- **Usages, C and C++:** `#include` directives appear in `objectSlices` with `resolvedPath`,
  `isSystem` and `importedSymbols`.
- **Usages:** an optional top-level `sourceIntegrity` array reports Unicode look-alike
  identifiers, bidirectional controls and hidden code.
- **Reachables:** the default source tags include `ai-model-artifact` and the default sink tags
  `sandbox-render`. A tagged library call also carries the package URLs of its callee. JavaScript
  flows without a sink and similar noise are dropped. Flows that pass through forwarded headers are
  opt-in: `--source-tag proxy-derived`.
- **Reachables files:** stale `<base>_N.json` chunks are removed before writing, and files are
  written through a temporary sibling, so a reader never sees half a file.
- **Analysis scope:** the C and C++ ignore pattern works on path segments, and PHP `vendor/` is not
  project code. Both change what is analysed.
- **npm wrapper:** atom exits with status 124 when `ATOM_TIMEOUT` stops it and 128 plus the signal
  number when it is killed. `ATOM_PARENT_PID` stops atom when its parent exits, and a second signal
  kills it.

No command or option of atom 3.2.1 was removed.

## Requirements

atom 4.0 needs Java 23 or later (the native images need none), and atom-parsetools 2.x for
JavaScript, TypeScript, PHP and Ruby. The container images include atom-parsetools 2.0.0 and, in
`atom` and `atom-sle`, edga 0.2.0.
