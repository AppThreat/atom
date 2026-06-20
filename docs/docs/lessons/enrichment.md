# Lesson 3: Core CPG Enhancements and Semantic Tagger Passes

### Learning Objective

Understand how compiler-level typing metadata is enriched using custom semantic tagger passes to tag PII, trackers, and validation functions.

### Pre-requisites

To follow this lesson, ensure the following software is installed on your system:

- **JDK 23+**: Standard Java SE Development Kit.
- **SBT 1.10+**: For compilation and runtime command invocation.
- **Pre-compiled Atom File**: An existing `app.atom` file generated in Lesson 1.
- **Custom Taint Rules**: A validation configuration file (e.g. `chennai.json`) declaring validator or sanitizer functions.

### Conceptual Background

A raw Abstract Syntax Tree (AST) lacks security-specific semantic metadata. To enable data-flow tracking, the CPG must be augmented. When an atom file is created, the system runs the [enhanceCpg](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala) helper method to execute various tagger passes defined in [chen](https://github.com/AppThreat/chen):

- **[CdxPass](https://github.com/AppThreat/chen/blob/main/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/CdxPass.scala)**: Maps dependencies to AST structures.
- **[ChennaiTagsPass](https://github.com/AppThreat/chen/blob/main/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/ChennaiTagsPass.scala)**: Tags framework controllers, HTTP routes, and user-defined sanitizers or validators.
- **[PiiTagsPass](https://github.com/AppThreat/chen/blob/main/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/PiiTagsPass.scala)**: Scans variable names, parameter labels, and structures for PII (Personally Identifiable Information) keywords to tag sensitive sources.
- **[TrackersTagsPass](https://github.com/AppThreat/chen/blob/main/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/TrackersTagsPass.scala)**: Detects and tags third-party advertising, telemetry, and tracking SDK calls.
- **[AndroidServicesTagsPass](https://github.com/AppThreat/chen/blob/main/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/AndroidServicesTagsPass.scala)**: Tags Android framework services and APIs.

### Real Commands

Enhance an existing CPG with semantic annotations using a validation config file `chennai.json`:

```bash
./atom.sh -l java -o app.atom --validation-config chennai.json /path/to/java/project
```

### Code Example

The execution of tagger passes is handled sequentially within the [enhanceCpg](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala) method:

```scala
private def enhanceCpg(config: AtomConfig, atom: Cpg, reusing: Boolean): Either[String, Unit] = {
  // If data flow enhancement is needed
  new OssDataFlow(new OssDataFlowOptions(
    maxNumberOfDefinitions = config.maxNumDef,
    useFluxEngine = config.useFluxEngine
  )).run(new LayerCreatorContext(atom))

  new CdxPass(atom).createAndApply()
  new EasyTagsPass(atom).createAndApply()
  runChennaiTags(config, atom)
  applyJvmTaggers(atom)
  Right(())
}
```
