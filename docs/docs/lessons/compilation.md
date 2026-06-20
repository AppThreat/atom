# Lesson 1: Compiling Source Code to the Atom (CPG) Representation

### Learning Objective

Understand how language frontends defined in the [chen repository](https://github.com/AppThreat/chen) ingest source code and compile it into an OverflowDB-backed Code Property Graph (CPG) stored as an atom file.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit (OpenJDK, Temurin, or GraalVM).
- **SBT (Scala Build Tool) 1.10+**: Used to compile the atom command-line utility.
- **Node.js 20+**: Required for JavaScript and TypeScript parsing.
- **NPM package @appthreat/atom-parsetools**: Must be installed globally (`npm install -g @appthreat/atom-parsetools`).
- **Python 3.10+**: Required for python AST generation dependencies.
- **C++ Compiler**: `clang++` or `g++` along with `make` (if parsing C/C++ projects).
- **Local clone of Atom**: Clone the [atom repository](https://github.com/AppThreat/atom) and compile it using the command `sbt stage`.

### Conceptual Background

The atom engine coordinates code parsing by invoking language-specific frontends from the `chen` repository. These frontends process raw code (or bytecode) and emit a graph representation using the `cpg2` schema definition. The persistent graph is stored using `overflowdb2`, which manages disk overflow when handling large program graphs.

The compilation process is managed by [Atom.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala). The core orchestration function is [createNewAtom](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala), which matches the target input language and calls the respective frontend:

- **C/C++**: Handled by the `c2cpg` frontend.
- **Java Source**: Handled by the `javasrc2cpg` frontend.
- **Java Bytecode/Android APK**: Handled by the `jimple2cpg` frontend.
- **JavaScript/TypeScript**: Handled by the `jssrc2cpg` frontend.
- **Python**: Handled by the `pysrc2cpg` frontend.
- **PHP**: Handled by the `php2atom` frontend.
- **Ruby**: Handled by the `ruby2atom` frontend.

During this compilation phase, AST (Abstract Syntax Tree) nodes are generated, local Control Flow Graph (CFG) edges are established, and type hints are resolved.

### Real Commands

Compile a Python project to produce an `app.atom` graph:

```bash
./atom.sh -l python -o app.atom /path/to/python/project
```

Compile a C++ project passing custom definitions and include paths:

```bash
./atom.sh -l cpp -o app.atom --frontend-args "defines=DEBUG,includes=/usr/local/include" /path/to/cpp/project
```

### Code Example

The logic to map input files to the compiler frontends is defined in the [createNewAtom](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala) method:

```scala
private def createNewAtom(
  language: String,
  config: AtomConfig,
  outputAtomFile: String
): Try[Cpg] =
  language match {
    case Languages.C | Languages.NEWC | "CPP" | "C++" =>
        createC2Cpg(config, outputAtomFile)
    case Languages.JAVA | Languages.JAVASRC =>
        createJavaSrc2Cpg(config, outputAtomFile)
    case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" =>
        createJsSrc2Cpg(config, outputAtomFile)
    case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
        createPythonCpg(config, outputAtomFile)
    // Additional language match cases handled dynamically
  }
```
