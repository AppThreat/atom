---
sidebar_position: 8
title: C/C++ frontends
---

# C/C++ frontends

atom builds the graph of C and C++ code with one of two frontends:

- **CDT** (the default): the Eclipse CDT parser, bundled with atom.
- **EDG** (experimental, opt-in): the [EDG C/C++ front end](https://github.com/edgcpp/compiler),
  through the [edga](https://github.com/AppThreat/edga) exporter, a separate binary. EDG is the front end of
  several production compilers: it resolves every name, overload, template and implicit
  operation the way the compiler does.

Both write the same graph shapes, so every command - `usages`, `reachables`, `memory-safety`,
`--with-data-deps` - works with either.

```bash
atom -l c --c-frontend edg-fallback -o app.atom .
```

| Option | Meaning |
|---|---|
| `--c-frontend cdt` | The CDT frontend (default). |
| `--c-frontend edg` | The EDG frontend. A file edga cannot export is left out. |
| `--c-frontend edg-fallback` | The EDG frontend, with CDT for the files edga cannot export. |
| `--edga-path <file>` | The edga binary. Otherwise `EDGA_PATH`, then the `PATH`. |
| `--compile-commands <file\|dir>` | A JSON compilation database (`compile_commands.json`, or a directory holding one), for either frontend. |
| `--c-build-contexts <first\|all>` | With a database that compiles one file more than once: analyse the first entry's build context (`first`, the default) or every distinct one (`all`). |
| `--c-isolate` | Parse each translation unit in a worker process of its own (CDT frontend). |

The same keys work as `--frontend-args c-frontend=edg-fallback,edga-path=/opt/edga`.

## What EDG resolves that CDT may not

- **Template instances.** Each instance is a method or type of its own with its template
  arguments (`max_of:int(int,int)`, `Box<int>`), and calls link to the instance that runs.
- **C++ library types.** The standard library headers of the host (libc++ or libstdc++) are
  parsed in full, so `std::string`, containers, smart pointers and their members have types.
- **Overloaded operators and constructors.** A call to a project's `operator+` or constructor
  links to it; a library operator keeps the shape of the built-in operator it is written with.
- **Lifetimes.** Destructor calls where a scope ends, before a `return`, `break` or `goto`
  that leaves it, and `__attribute__((cleanup(f)))` calls.
- **Facts as tags.** Implicit arithmetic conversions (`implicit-conversion=int->unsigned
  short:narrowing`), virtual calls (`virtual-call`), default arguments (`default-argument`),
  variable length array sizes (`vla-size`), compiler-generated nodes (`compiler-generated`,
  `lifetime-end=<variable>`), constant values (`const-value`), pointer arithmetic
  (`ptr-arith`) and macro expansions (`macro-invocation`, `macro-origin`). The memory-safety
  passes read them where they are present and infer them otherwise.

## Compilation databases

With a compilation database each file is parsed as its build compiles it: its language
standard, defines, include directories and forced includes. The compiler it names (gcc,
clang, MSVC) is asked for its predefined macros and system include directories, and the EDG
front end emulates that compiler and version. Without a database, the host's compiler is
used, and include directories are found from the project's own headers.

A header is parsed as part of the first unit, in path order, that includes it. A header no
unit includes is parsed on its own.

## Build contexts

A compilation database may compile one file more than once, and each entry is one build context
of the file: a way the file is preprocessed and parsed as a program. By default atom analyses the
first entry's context, the one the file has always been parsed with. `--c-build-contexts all`
analyses the file in every distinct context. A function whose body differs between the contexts
keeps one copy per context: the first context's copy keeps its full name and every other copy's
gains `@ctx:` and the first eight hex digits of its context's fingerprint, and the calls inside
the copies are rewritten to reach the copy of their own context. A function whose body is the
same in every context is kept once. Both C frontends support `all`; a context whose parse or
export fails contributes nothing, and the EDG frontend's record of the failure names the build
context it belongs to.

Every FILE node also carries a `build-context` tag. A translation unit's value is a fingerprint
of its compile command, over exactly the parts that change what the unit preprocesses and parses
as. A header's value names the unit whose parse it was read with and that unit's fingerprint, as
`<owner relative path>:<fingerprint>`. The tag is rebuilt on every run, so a run over a warm AST
cache reports the same values a fresh one does.

## Isolated parsing

`--c-isolate` parses each translation unit of the CDT frontend in a worker process of its own,
so a unit that crashes the parser, hangs or exhausts the heap costs its unit, not the run. The
worker's heap is half the run's, between half a gigabyte and four. A unit whose worker exits,
hangs past the parse timeout or runs out of memory is retried once in a fresh worker, and the
reason is written to the parse report (`--parse-report`); a unit that fails both times is left
out of the graph, reported.

## Fallback and the frontend of each file

With `edg-fallback`, the files edga cannot export - a missing header, an error the front end
gives up on - are parsed by CDT into the same graph. Each FILE node has a `frontend` tag,
`edg` or `cdt`. atom prints how many units fell back, with the first reason.

The AST cache (`--no-ast-cache` to turn it off) keeps the edga build in its key: an AST
another edga build exported is not reused.

## Getting edga

The `atom` and `atom-sle` container images include edga 0.4.0, the version atom is tested with.
Elsewhere, download it from the [edga releases](https://github.com/AppThreat/edga/releases) - static
Linux binaries (glibc and musl, amd64 and arm64), macOS arm64, and Windows amd64 and arm64, each
with its SHA-256 - and put it on the `PATH` as `edga` (`edga.exe` on Windows), or point
`EDGA_PATH` or `--edga-path` at it. To build it, see the [edga README](https://github.com/AppThreat/edga): it compiles the EDG
sources unmodified.

## Hosts

- **macOS (libc++).** libc++'s vectorised algorithms use clang vector types the front end
  cannot instantiate; they are left out (`__OPTIMIZE_SIZE__`), as when optimising for size.
  The NEON intrinsics' macros are not passed on.
- **Linux on aarch64, and the musl builds.** These edga builds have no float128 support, so
  glibc's `_Float128` is named as `long double` (the same type on aarch64, the same size on
  x86_64) with GCC 7 and later.
- **Windows and MSVC.** The front end emulates MSVC from a compilation database that names
  `cl.exe`, but this has not been tested; use CDT there.
