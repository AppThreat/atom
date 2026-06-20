# Lesson 5: Backward Data-Flow Slicing

### Learning Objective

Execute backwards intraprocedural data-flow slicing from external boundaries to identify source inputs influencing dangerous sinks.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compiling and running Atom.
- **Pre-compiled Atom File**: An existing `app.atom` file.
- **Tuning Parameters**: A configured definition limit for reaching definitions, typically set via `--max-num-def 2000`.

### Conceptual Background

Intraprocedural data-flow slicing tracks variable state backwards using Reaching Definitions and Data Dependence Graph (DDG) edges. Sinks are selected based on external API boundaries (methods defined outside the compiled project). Slicing starts at the arguments of these sinks and traverses the `ddgIn` edges backwards.

The [DataFlowSlicing](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/DataFlowSlicing.scala) class manages this traversal up to a configured `--slice-depth`.

### Real Commands

Generate backward data-flow slices for a Java project up to a depth of 5, targeting only sinks matching the regex `.*exec.*`:

```bash
./atom.sh data-flow -l java -o app.atom --slice-outfile dataflow.json --slice-depth 5 --sink-filter ".*exec.*" /path/to/java/project
```

### Code Example

Refer to the [calculateDataFlowSlice](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/DataFlowSlicing.scala) method in [DataFlowSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/DataFlowSlicing.scala):

```scala
def calculateDataFlowSlice(atom: Cpg, config: DataFlowConfig): Option[DataFlowSlice] = {
  language = atom.metaData.language.headOption
  excludeOperatorCalls.set(config.excludeOperatorCalls)

  val dataFlowSlice = (config.fileFilter match {
    case Some(fileRegex) => atom.call.where(_.file.name(fileRegex))
    case None            => atom.call
  })
  .where(c => c.callee.isExternal)
  .flatMap {
      // Excludes operator calls and internal lambdas based on settings
      case c if excludeOperatorCalls.get() && c.name.startsWith("<operator") => None
      case c => Some(c)
  }
  .map(c => exec.submit(new TrackDataFlowTask(config, c)))
  .flatMap(TimedGet)
  .reduceOption { (a, b) => DataFlowSlice(a.nodes ++ b.nodes, a.edges ++ b.edges) }

  nodeCache.clear()
  dataFlowSlice
}
```
