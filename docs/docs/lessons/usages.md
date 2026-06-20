# Lesson 4: Object and Method Usage Slicing

### Learning Objective

Extract variable, parameter, and object allocation usage traces (`usages` command) to profile how sensitive objects are processed.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compiling and running Atom.
- **Pre-compiled Atom File**: An existing `app.atom` file.
- **Python 3.10+ with atom-tools**: Required for OpenAPI specification generation (`pip install atom-tools`).
- **Node.js 20+**: For running frontend parsing wrappers.

### Conceptual Background

Object usage slicing focuses on tracking individual variables from their creation to their last utilization within a method scope. This is useful for identifying the usage patterns of third-party library objects, cryptographic contexts, or HTTP request objects.

The [UsageSlicing](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/UsageSlicing.scala) engine checks all variable declarations and collects:

- **definedBy**: The node representing where the target object was defined (e.g. local variable assignment, method call, or parameter).
- **invokedCalls**: The list of method calls invoked on the object itself.
- **argToCalls**: The list of calls where the object was passed as an argument, along with its parameter index position.

### Real Commands

Generate usage slices for a JavaScript project, including source code snippets, and filter for variables used in at least two calls:

```bash
./atom.sh usages -l js -o app.atom --slice-outfile usages.json --min-num-calls 2 --include-source /path/to/project
```

Extract usages and convert HTTP route objects to an OpenAPI specification file:

```bash
./atom.sh usages -l js -o app.atom --extract-endpoints --slice-outfile usages.json /path/to/node/project
```

### Code Example

The usage slicing entry point is [calculateUsageSlice](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/UsageSlicing.scala) in [UsageSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/UsageSlicing.scala):

```scala
def calculateUsageSlice(atom: Cpg, config: UsagesConfig): ProgramSlice = {
  val declarations = getDeclarations(atom, config)
  val typeMap      = atom.typeDecl.map(f => (f.name, f.fullName)).toMap
  val slices       = computeSlices(atom, declarations, typeMap, config)
  val language     = atom.metaData.language.headOption
  val userDefTypes = userDefinedTypes(atom)

  createProgramUsageSlice(atom, language, slices, userDefTypes, typeMap)
}
```
