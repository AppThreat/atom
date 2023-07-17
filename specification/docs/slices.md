# Introduction

Program slicing is a technique to extract parts of a program based on a criterion. Atom (powered by joern library) is a static opionionated data-flow slicer that is optimized for application and dependency analysis use cases for upto 100K LOC.

## Design principles

- **Precise** - With static analysis, atom can generate precise slices with verifiable location information from the application source code.
- **Non-deterministic** - The slicing operation is optimized for constant-time generation performance and therefore non-deterministic. Repeated runs could yield slightly varying results depending on code complexity.
- **Secure** - It is not possible to reverse-engineer and obtain the application source code from the atom slices alone.

All slices produce machine-readable json output that can be parsed using atom [proto specification](../atom.proto).

## Types of slicing

### Usages slice

Usage slices can help answer two key questions about the usages of external libraries.

1. **HOW?** Are the libraries used as-is or via custom alias or derived type?
2. **WHERE?** File and line number locations of the definitions, imports, usage, calls etc

The below mind map offers an overview.

![Usages slice](./Library%20Usages.png)

### Data Flow slice

Data Flow slice represents the data-dependency information computed statically from the source code. Upto 100 reachable paths are precomputed and made available as `paths` attribute in the json. The full list of `nodes` and `edges` from the Data Dependency Graph (DDG) is also made available for custom visualization and traversal purposes.

![Data Flow slice](./Data%20Flows.png)

## Generating slices

Use the atom cli to generate slices.

### Create data-flow slice for a java project.

```shell
atom data-flow -o app.atom --slice-outfile df.json -l java .
```

### Create usages slice for a java project.

```shell
atom usages -o app.atom --slice-outfile usages.json -l java .
```

## Developing custom slicer

Coming soon!
