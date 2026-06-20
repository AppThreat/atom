# Lesson 3: Core CPG Enhancements and Semantic Tagger Passes

### Learning Objective

Understand how `atom` augments a freshly built CPG with the data-flow overlay and a chain of semantic tagger passes (SBOM/CDX, PII, trackers, framework routes, sanitizers/validators) that downstream slicing depends on.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A target project** (the enhancement runs as part of atom generation).
- **Optional**: a validation config (`chennai.json`) declaring custom validators/sanitizers.

### Conceptual Background

A raw AST has no security semantics. Before slicing can find source→sink flows, the CPG must be enriched with:

1. **Reaching-definition (DDG) edges** — produced by the `OssDataFlow` overlay (the data-flow engine; classic or the low-allocation **Flux** engine). Without these, taint cannot propagate.
2. **Semantic tags** — applied by tagger passes defined in [chen's x2cpg](https://github.com/AppThreat/chen/tree/main/platform/frontends/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers).

Enhancement is orchestrated by [`enhanceCpg`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala). The taggers split into two groups:

**Applied for every language** (when data-flow enhancement is needed):

- [CdxPass](https://github.com/AppThreat/chen/blob/main/platform/frontends/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/CdxPass.scala) — maps SBOM/CDX (`bom.json`) components onto matching calls/identifiers via PURLs.
- `EasyTagsPass` — tags common validation/sanitization/auth patterns.
- [ChennaiTagsPass](https://github.com/AppThreat/chen/blob/main/platform/frontends/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/ChennaiTagsPass.scala) (via `runChennaiTags`) — tags framework routes (`framework-route`/`framework-input`/`framework-output`) and the user-declared validators/sanitizers from `--validation-config`.

**Applied only for JVM frontends** (`java`, `jar`, `jimple` — gated by `applyJvmTaggers`):

- [PiiTagsPass](https://github.com/AppThreat/chen/blob/main/platform/frontends/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/PiiTagsPass.scala) — tags PII/PCI/PHI/secrets by value and name regex.
- [TrackersTagsPass](https://github.com/AppThreat/chen/blob/main/platform/frontends/x2cpg/src/main/scala/io/appthreat/x2cpg/passes/taggers/TrackersTagsPass.scala) — tags advertising/analytics SDK calls from `trackers.json`.
- `AndroidServicesTagsPass` — tags Android service ingress/egress.

Tags are attached as `TAGGED_BY` edges (via the `newTagNode(...).store()` API in chen), and the reachables engine later treats them as sources, sinks, or sanitizers.

### Real Commands

Generate a Java atom and enhance it with a custom validation config:

```bash
./atom.sh -l java -o app.atom --validation-config chennai.json /path/to/java/project
```

Force regeneration of data-flow dependencies with the Flux engine:

```bash
./atom.sh -l java -o app.atom --with-data-deps /path/to/java/project
```

### Code Example

The real (abbreviated) `enhanceCpg` in [Atom.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/Atom.scala):

```scala
private def enhanceCpg(config: AtomConfig, atom: Cpg, reusing: Boolean): Either[String, Unit] =
  config match
    case x: AtomConfig if needsDataFlowEnhancement(x, reusing) =>
        try
          new OssDataFlow(new OssDataFlowOptions(
            maxNumberOfDefinitions = x.maxNumDef,
            useFluxEngine = x.useFluxEngine
          )).run(new LayerCreatorContext(atom))
          new CdxPass(atom).createAndApply()
          new EasyTagsPass(atom).createAndApply()
          runChennaiTags(x, atom)
          applyJvmTaggers(atom)   // Pii / Trackers / AndroidServices, JVM frontends only
          Right(())
        catch
          case ex: Exception => Left(s"Failed to enhance CPG: ${ex.getMessage}")
    case _ =>
        // Reused atom: tags only, no data-flow recompute
        new EasyTagsPass(atom).createAndApply()
        applyJvmTaggers(atom)
        Right(())
```

`needsDataFlowEnhancement` returns false when reusing an atom that already has DDG edges, so re-running a slice over an existing atom skips the expensive reaching-definition recompute.
