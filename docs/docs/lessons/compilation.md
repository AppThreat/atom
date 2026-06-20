# Lesson 1: Compiling Source Code to the Atom (CPG) Representation

### Learning Objective

Understand how `atom` drives the language frontends from the [chen repository](https://github.com/AppThreat/chen) to ingest source code (or bytecode) and compile it into an `overflowdb2`-backed Code Property Graph (CPG) persisted as an atom file.

### Pre-requisites

- **JDK 23+**: OpenJDK, Temurin, or GraalVM.
- **SBT 1.10+**: Used to build atom (`sbt stage`).
- **Node.js 20+** and **@appthreat/atom-parsetools** (`npm install -g @appthreat/atom-parsetools`): provides the `astgen`/`rbastgen` binaries for JS/TS/Ruby.
- **C/C++ toolchain** (`clang++`/`g++`, `make`): only when parsing C/C++.
- **Local clone of [atom](https://github.com/AppThreat/atom)** built with `sbt stage` — produces `atom.sh`.

> Python does **not** need an external AST generator: `pysrc2cpg` parses in-process. See the chen lessons for frontend internals.

### Conceptual Background

`atom` is a thin orchestration layer over the chen frontends. It selects a frontend by language, builds the structural graph (AST + local CFG), runs the frontend's overlay and post-processing passes (type recovery, call linking), and writes the result as an atom file using the `cpg2` schema and `overflowdb2` storage.

The orchestration lives in [Atom.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala). `generateForLanguage` normalises the `-l` value and dispatches to `createNewAtom`, which matches the language to a frontend:

| `-l` values                                             | Frontend       | Notes                           |
| ------------------------------------------------------- | -------------- | ------------------------------- |
| `c`, `cpp`, `c++`, `newc` (+ `h`, `hpp`, `i`)           | `c2cpg`        | header-only inputs use `C2Atom` |
| `java`, `javasrc`                                       | `javasrc2cpg`  | delombok + dependency fetch     |
| `jar`, `jimple`, `android`, `apk`, `dex`                | `jimple2cpg`   | Soot Jimple IR                  |
| `scala`, `tasty`, `sbt`                                 | (jimple-based) |                                 |
| `jssrc`, `javascript`, `js`, `ts`, `typescript`, `flow` | `jssrc2cpg`    | astgen                          |
| `python`, `py`                                          | `pysrc2cpg`    | native parser                   |
| `php`                                                   | `php2atom`     | requires local `php`            |
| `ruby`, `rb`, `jruby`                                   | `ruby2atom`    | rbastgen                        |

After the frontend builds the CPG, `atom` enhances it with the data-flow overlay and the semantic taggers (Lesson 3) and, depending on the subcommand, slices/exports it (Lessons 4–8).

### Real Commands

Compile a Python project to `app.atom`:

```bash
./atom.sh -l python -o app.atom /path/to/python/project
```

Compile a C++ project with custom defines and include paths via `--frontend-args` (comma-separated `key=value`):

```bash
./atom.sh -l cpp -o app.atom \
  --frontend-args "defines=DEBUG,includes=/usr/local/include" \
  /path/to/cpp/project
```

Reuse an already-generated atom (skip frontend parsing) for a slicing run:

```bash
./atom.sh data-flow -l java -o app.atom --reuse-atom --slice-outfile dataflow.json /path/to/project
```

### Code Example

The language-to-frontend mapping in [Atom.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala) `createNewAtom`:

```scala
private def createNewAtom(
  language: String,
  config: AtomConfig,
  outputAtomFile: String
): Try[Cpg] =
  language match
    case "H" | "HPP" | "I" =>
        createC2Atom(config, outputAtomFile)
    case Languages.C | Languages.NEWC | "CPP" | "C++" =>
        createC2Cpg(config, outputAtomFile)
    case "JAR" | "JIMPLE" | "ANDROID" | "APK" | "DEX" =>
        createJimple2Cpg(config, outputAtomFile)
    case Languages.JAVA | Languages.JAVASRC =>
        createJavaSrc2Cpg(config, outputAtomFile)
    case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" | "FLOW" =>
        createJsSrc2Cpg(config, outputAtomFile)
    case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
        createPythonCpg(config, outputAtomFile)
    case Languages.PHP =>
        createPhpCpg(config, outputAtomFile)
    case Languages.RUBYSRC | "RUBY" | "RB" | "JRUBY" =>
        createRubyCpg(config, outputAtomFile)
    // ...
```

Each `createXxxCpg` helper sets sensible defaults (e.g. `fetchDependencies=true` and `delombokMode=types-only` for Java) and calls the frontend's `createCpgWithOverlays`. See the chen lessons for each frontend's config and pass pipeline.
