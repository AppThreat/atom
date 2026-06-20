# Lesson 2: Extracting Dependency Manifests (`parsedeps`)

### Learning Objective

Learn how the `parsedeps` command extracts declared and imported third-party modules from a compiled atom and emits them as a dependency slice for supply-chain analysis.

### Pre-requisites

- **JDK 23+** and a built `atom` (`sbt stage`).
- **A target project** with a resolvable dependency manifest (e.g. `requirements.txt`, `pyproject.toml`).

> **Language support:** at present `parsedeps` is implemented for **Python only**. For other languages the command returns `"'<LANG>' is not yet supported for the \`parsedeps\` command"`. Use `cdxgen`/ the chen frontends'`DEPENDENCY` nodes for other ecosystems.

### Conceptual Background

`parsedeps` reads dependency information that the frontend recorded in the CPG (for Python, the `DependenciesFromRequirementsTxtPass` populates `DEPENDENCY` nodes and the import passes record imported symbols) and unifies it into a single `DependencySlice`.

The entry point is [`parseDependencies`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/parsedeps/package.scala). It dispatches on the atom's language and delegates to a language-specific `XDependencyParser` (currently `PythonDependencyParser`). The output is a `DependencySlice(modules: Seq[ModuleWithVersion])`, where each module carries:

```scala
case class ModuleWithVersion(
  name: String,
  version: String = "",
  versionSpecifiers: String = "",  // e.g. ">=2.0,<3.0"
  importedSymbols: String = ""     // symbols actually imported from the module
)
```

This pairs _declared_ versions with _actually imported_ symbols, so you can spot dependencies that are declared but unused, or imported but undeclared.

### Real Commands

Extract dependencies from a Python project:

```bash
./atom.sh parsedeps -l python -o app.atom --slice-outfile deps.json /path/to/python/project
```

Inspect the result:

```bash
jq '.modules[] | {name, version, importedSymbols}' deps.json
```

### Code Example

The real dispatch logic in [parsedeps/package.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/parsedeps/package.scala):

```scala
def parseDependencies(cpg: Cpg): Either[String, DependencySlice] =
  cpg.metaData.language.map(_.toUpperCase).headOption match
    case Some(language)
        if Set(Languages.PYTHONSRC, Languages.PYTHON, "PY").contains(language) =>
        Right(PythonDependencyParser.parse(cpg))
    case Some(language) =>
        Left(s"'$language' is not yet supported for the `parsedeps` command")
    case _ => Left("Unable to extract atom language")

case class DependencySlice(modules: Seq[ModuleWithVersion]) extends AtomSlice:
  override def toJson: String = this.asJson.noSpaces
```

A `ModuleWithVersion.merge` helper combines duplicate entries (union of version specifiers and imported symbols) so that the same module imported in several files appears once.
