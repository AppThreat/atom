# Lesson 4: Object and Method Usage Slicing (`usages`)

### Learning Objective

Extract per-object usage traces — how each variable, parameter, or allocation is defined, what methods are invoked on it, and where it is passed as an argument — and optionally derive HTTP endpoints as an OpenAPI spec.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A target project** (or a pre-built `app.atom`).
- **Python with atom-tools** (`pip install atom-tools`) only if post-processing endpoints into OpenAPI.

### Conceptual Background

Usage slicing tracks each object from its definition to its uses within method scope — ideal for profiling how library objects, crypto contexts, or HTTP request objects are handled. [`UsageSlicing`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/UsageSlicing.scala) collects, for each tracked declaration:

- **definedBy** — the `DefComponent` describing where the object originated (local assignment, call result, or parameter).
- **invokedCalls** — methods invoked directly on the object.
- **argToCalls** — calls where the object is passed as an argument (with its parameter index).

Behaviour is tuned by `UsagesConfig`:

```scala
case class UsagesConfig(
  minNumCalls: Int = 1,             // skip objects with fewer than N calls
  excludeOperatorCalls: Boolean = true,
  excludeMethodSource: Boolean = true,  // set false (--include-source) to embed source
  extractEndpoints: Boolean = false     // --extract-endpoints: emit HTTP routes
) extends BaseConfig
```

The result is a `ProgramSlice` serialised to JSON.

### Real Commands

Usage slice for a JS project, embedding source, only objects used in ≥2 calls:

```bash
./atom.sh usages -l js -o app.atom --slice-outfile usages.json --min-num-calls 2 --include-source /path/to/project
```

Extract usages and HTTP route objects for OpenAPI conversion:

```bash
./atom.sh usages -l js -o app.atom --extract-endpoints --slice-outfile usages.json /path/to/node/project
# then, with atom-tools installed:
atom-tools convert -i usages.json -t openapi3.1.0 -o openapi.json
```

### Code Example

The entry point in [UsageSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/UsageSlicing.scala):

```scala
def calculateUsageSlice(atom: Cpg, config: UsagesConfig): ProgramSlice =
  val declarations = getDeclarations(atom, config)
  val typeMap      = atom.typeDecl.map(f => (f.name, f.fullName)).toMap
  val slices       = computeSlices(atom, declarations, typeMap, config)
  val language     = atom.metaData.language.headOption
  val userDefTypes = userDefinedTypes(atom)
  createProgramUsageSlice(atom, language, slices, userDefTypes, typeMap)
```

`computeSlices` walks each declaration, builds `definedBy`, and partitions its uses into `invokedCalls` and `argToCalls` before assembling the per-object usage slice.
