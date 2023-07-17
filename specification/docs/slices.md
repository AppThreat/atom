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

#### How to use?

1. Parse the usage json
2. Iterate the `objectSlices` array and for each slice store its fileName and lineNumber.

```json
{
  "objectSlices" : [
    {
      "code" : "",
      "fullName" : "com.example.vulnspring.WebController.jwt:java.lang.String(javax.servlet.http.HttpSession,org.springframework.ui.Model)",
      "signature" : "java.lang.String(javax.servlet.http.HttpSession,org.springframework.ui.Model)",
      "fileName" : "src/main/java/com/example/vulnspring/WebController.java",
      "lineNumber" : 274,
      "columnNumber" : 2,
      "usages" : [
        {
          "targetObj" : {
            "name" : "username",
            "typeFullName" : "java.lang.String",
            "lineNumber" : 276,
            "columnNumber" : 3,
            "label" : "LOCAL"
          },

```

3. Now iterate the `usages` array. Under the each category the attributes `typeFullName` (Found in `targetObj` and `definedBy`) and `resolvedMethod` (`invokedCalls` and `argToCalls`) are interesting.

4. Now iterate the `userDefinedTypes` array. For each type, note down the `fileName` and `lineNumber`. For each `field`, the attribute `typeFullName` indicates the aliased field. For each `procedure`, the `paramTypes` array would list the custom type from index 1 onwards.

```json
"userDefinedTypes" : [
{
    "name" : "com.example.vulnspring.WebController",
    "fields" : [
    {
        "name" : "jdbcTemplate",
        "typeFullName" : "org.springframework.jdbc.core.JdbcTemplate",
        "lineNumber" : 42,
        "columnNumber" : 15,
        "label" : "LOCAL"
    },
    {
        "name" : "logger",
        "typeFullName" : "org.slf4j.Logger",
        "lineNumber" : 44,
        "columnNumber" : 30,
        "label" : "LOCAL"
    }
    ],
    "procedures" : [
    {
        "callName" : "home",
        "resolvedMethod" : "com.example.vulnspring.WebController.home:java.lang.String(org.springframework.ui.Model,javax.servlet.http.HttpSession)",
        "paramTypes" : [
        "com.example.vulnspring.WebController",
        "org.springframework.ui.Model",
        "javax.servlet.http.HttpSession"
        ],
        "returnType" : "java.lang.String",
        "lineNumber" : 46,
        "columnNumber" : 2
    },
```

### Data Flow slice

Data Flow slice represents the data-dependency information computed statically from the source code. Upto 100 reachable paths are precomputed and made available as `paths` attribute in the json. The full list of `nodes` and `edges` from the Data Dependency Graph (DDG) is also made available for custom visualization and traversal purposes.

![Data Flow slice](./Data%20Flows.png)

#### How to use?

1. Parse the Data Flow json
2. Iterate the `graph.nodes` array and create a Map for each node with id as the key and the node as the value.
3. Iterate the `paths` array. For each id, lookup the node from the map object created in step 2.
4. Filter any operator calls where the name starts with `<operator`. Note that operator calls could start with either `<operator>` or `<operators>` (with an `s`) due to a known unresolved bug.
5. All `CALL` nodes with `isExternal=true` indicates external method calls. The `fullName` property is interesting for such external calls along with all the `parent*` attributes such as `parentFileName`, `parentMethodName` etc.
6. Nodes with the label `METHOD_PARAMETER_IN` are method parameters. These could be user-provided input depending on the framework on filename. For instance, method parameters in a controller or service class usually takes input from the users or another service.

#### Mapping between data flow slice and CycloneDX 1.5

The information in a data-flow slice can be used as component evidence in a CycloneDX 1.5 document.

![Data Flow Slice and CycloneDX mapping](./dataflow-cdx-mapping.jpg)

| Data Flow Slice Attribute | CycloneDX attribute | Comments                                                |
| ------------------------- | ------------------- | ------------------------------------------------------- |
| parentPackageName         | package             | Will be based on the filename for Javascript/Typescript |
| parentClassName           | module              | Will be based on the filename for Javascript/Typescript |
| parentMethodName          | function            |                                                         |
| parentMethodSignature     | parameters          | Could be customized to ignore return types              |
| lineNumber                | line                | Could be unavailable for certain projects               |
| columnNumber              | column              | Could be unavailable for certain projects               |
| parentFileName            | fullFilename        |                                                         |

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
