# Lesson 2: Extracting Dependency and Build Manifests

### Learning Objective

Learn how the `parsedeps` command extracts package identifiers, local modules, and third-party dependencies to build a supply-chain manifest.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compilation and runtime command invocation.
- **Node.js 20+**: Required for dependency graph utilities.
- **NPM package @cyclonedx/cdxgen**: Must be installed globally (`npm install -g @cyclonedx/cdxgen`) to handle complex project build manifests.
- **Target Project**: A project with a valid manifest (e.g. `package.json`, `requirements.txt`, or `build.sbt`).

### Conceptual Background

Auditing software supply chains requires extracting declared dependencies and matching them to actual import statements in the code. The `parsedeps` command resolves package names, versions, and scope classifications.

The dependency parser extracts:

- Third-party dependency coordinate metadata.
- Local file boundaries and module packages.
- AST imports and external call namespaces.

The parser interfaces with packages like `@cyclonedx/cdxgen` to inspect and build precise dependency manifests. The results are unified into a JSON representation of dependencies.

### Real Commands

Execute dependency parsing on a Python project:

```bash
./atom.sh parsedeps -l python -o app.atom --slice-outfile deps.json /path/to/python/project
```

Extract dependencies on a Node.js project:

```bash
./atom.sh parsedeps -l js -o app.atom --slice-outfile deps.json /path/to/node/project
```

### Code Example

The entry point for dependency extraction can be traced in [parseDependencies](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/parsedeps/package.scala):

```scala
def parseDependencies(cpg: Cpg): Either[String, DependencySlice] =
  Try {
    val dependencies = cpg.dependency.map(d =>
      Dependency(d.name, d.version, d.dependencyGroupId)
    ).toList
    val imports = cpg.imports.map(i =>
      Import(i.importedEntity, i.importedAs)
    ).toList
    DependencySlice(dependencies, imports)
  } match {
    case Success(slice) => Right(slice)
    case Failure(exception) => Left(s"Failed to parse dependencies: ${exception.getMessage}")
  }
```
