---
sidebar_position: 13
title: Program Slicing
---

# Introduction

Program slicing is a technique to extract parts of a program based on a criterion. Atom (powered by chen library) is a
static opinionated data flow slicer optimized for application and dependency analysis use cases with up to 100K LOC.

## Design principles

- **Precise** - With static analysis, atom can generate precise slices with verifiable location information from the
  application source code.
- **Non-deterministic** - The slicing operation is optimized for constant-time generation performance and therefore
  non-deterministic. Repeated runs could yield slightly varying results depending on code complexity.
- **Secure** - It is not possible to reverse-engineer and obtain the application source code from the atom slices alone.

All slices produce machine-readable json output that can be parsed using atom [proto specification](specification/atomproto).

## Types of slicing

### Usages slice

Usage slices can help answer two key questions about the usages of external libraries.

1. **HOW?** Are the libraries used as-is or via custom alias or derived type?
2. **WHERE?** File and line number locations of the definitions, imports, usage, calls etc.

The mind map below offers an overview.

![Slicing](../static/img/lib-usage.png)

#### How to parse?

1. Parse the usages json.
2. Iterate over the `objectSlices` array. For each slice store its fileName and lineNumber.

For C and C++, each `#include` also appears in `objectSlices`, with no usages: `fullName` is the
header as written, `fileName` the file that includes it, `resolvedPath` the file the include
resolved to (absent when it did not resolve), `isSystem` `true` for a system include (`<...>`), and
`importedSymbols` the functions the including file calls that the header declares (with function
bodies parsed: not in header mode). The resolved file says which package provides the header, which
its name alone often cannot, and the symbols which of its API the code uses.

```json
{
  "code": "#include <zlib.h>",
  "fullName": "zlib.h",
  "signature": "zlib.h",
  "fileName": "src/compress.c",
  "lineNumber": 3,
  "columnNumber": 1,
  "resolvedPath": "/usr/include/zlib.h",
  "isSystem": true,
  "importedSymbols": ["deflate", "deflateInit_"],
  "usages": []
}
```

When the source holds Unicode that hides what the code does, a `sourceIntegrity` list follows the
other two (it is absent otherwise):

- `unicode-confusable`: a name that looks like another name of its file (the UTS #39 skeletons are
  equal and at least one name is not ASCII), with `detail` naming the look-alikes; or a name the file
  spells differently from what it means, with `detail` the spelling - Python's `ｅｖａｌ(x)` calls
  `eval`, as JavaScript's and Java's `\u0065val(x)` do.
- `unicode-bidi-control`: bidirectional formatting characters in a string or comment, `detail` the
  code points (CVE-2021-42574).
- `unicode-hidden-code`: a Java Unicode escape that ends a line, a comment or a literal, so that what
  reads as a comment is compiled (`// \u000a run();` runs `run()`). `name` is the method, `detail`
  the escapes.

```json
"sourceIntegrity": [
  {"kind": "unicode-confusable", "fileName": "auth.c", "lineNumber": 12, "name": "isAdmіn", "detail": "isAdmin"},
  {"kind": "unicode-bidi-control", "fileName": "auth.c", "lineNumber": 30, "name": "comment", "detail": "U+202E,U+2066"},
  {"kind": "unicode-hidden-code", "fileName": "Auth.java", "lineNumber": 14, "name": "check", "detail": "\\u000a"}
]
```

```json
{
  "objectSlices": [
    {
      "code": "",
      "fullName": "com.example.vulnspring.WebController.jwt:java.lang.String(javax.servlet.http.HttpSession,org.springframework.ui.Model)",
      "signature": "java.lang.String(javax.servlet.http.HttpSession,org.springframework.ui.Model)",
      "fileName": "src/main/java/com/example/vulnspring/WebController.java",
      "lineNumber": 274,
      "columnNumber": 2,
      "usages": [
        {
          "targetObj": {
            "name": "username",
            "typeFullName": "java.lang.String",
            "lineNumber": 276,
            "columnNumber": 3,
            "label": "LOCAL"
          },

```

3. Iterate over the `usages` array. The attributes `typeFullName` (found in `targetObj`
   and `definedBy`) and `resolvedMethod` (`invokedCalls` and `argToCalls`) under each category are of interest.

4. Iterate over the `userDefinedTypes` array. Note the `fileName` and `lineNumber` for each type. For each `field`,
   the attribute `typeFullName` indicates the aliased field. For each `procedure`, the `paramTypes` array lists the
   custom type from index 1 onward.

```json
"userDefinedTypes": [
  {
    "name": "com.example.vulnspring.WebController",
    "fields": [
    {
      "name": "jdbcTemplate",
      "typeFullName": "org.springframework.jdbc.core.JdbcTemplate",
      "lineNumber" : 42,
      "columnNumber": 15,
      "label": "LOCAL"
    },
    {
      "name": "logger",
      "typeFullName": "org.slf4j.Logger",
      "lineNumber": 44,
      "columnNumber": 30,
      "label" : "LOCAL"
    }
    ],
    "procedures": [
    {
      "callName": "home",
      "resolvedMethod": "com.example.vulnspring.WebController.home:java.lang.String(org.springframework.ui.Model,javax.servlet.http.HttpSession)",
      "paramTypes" : [
        "com.example.vulnspring.WebController",
        "org.springframework.ui.Model",
        "javax.servlet.http.HttpSession"
      ],
    "returnType": "java.lang.String",
    "lineNumber": 46,
    "columnNumber": 2
    },
```

### Data flow slice

Data flow slices represent the data-dependency information computed statically from the source code using Reverse-Reachability Algorithm. The full list of `nodes` and `edges` from the
Data Dependency Graph (DDG) is also made available for custom visualization and traversal purposes. Up to 50 reachable
paths are precomputed and made available via the `paths` attribute in the json by the atom cli tool for convenience.

![Data Flows](../static/img/data-flows.png)

#### When to use?

It is quite common for organizations to have common libraries and modules in separate repositories, jar files, and other packages. These modules would use external libraries as sinks and might lack any entrypoints (sources). Data flow slicing could work well in theses scenarios where the entrypoints (sources) cannot be identified. The brute-force nature means data-flow slicing would often take significant amount of time compared to usages or reachables slicing.

#### How to parse?

1. Parse the data flow json.
2. Iterate over the `graph.nodes` array and create a Map for each node with the id as the key and the node as the value.
3. Iterate over the `paths` array. For each id, look up the node from the map object created in step 2.
4. Filter any operator calls where the name starts with `<operator`. Note that operator calls could start with
   either `<operator>` or `<operators>` (with an `s`) due to a known unresolved bug.
5. All `CALL` nodes with `isExternal=true` indicates external method calls. The `fullName` property is interesting for
   such external calls along with all the `parent*` attributes such as `parentFileName`, `parentMethodName` etc.
6. Nodes with the label `METHOD_PARAMETER_IN` are method parameters. These could be user-provided input depending on the
   framework and filename. For instance, method parameters in a controller or service class usually takes input from the
   users or another service.

#### Mapping data flow slices to CycloneDX 1.5

The information in a data-flow slice can be used as component evidence in a CycloneDX 1.5 document.

![Dataflow CDX mapping](../static/img/dataflow-cdx-mapping.jpg)

| Data Flow Slice Attribute | CycloneDX Attribute | Comments                                                |
| ------------------------- | ------------------- | ------------------------------------------------------- |
| parentPackageName         | package             | Will be based on the filename for Javascript/Typescript |
| parentClassName           | module              | Will be based on the filename for Javascript/Typescript |
| parentMethodName          | function            |                                                         |
| parentMethodSignature     | parameters          | Could be customized to ignore return types              |
| lineNumber                | line                | Could be unavailable for certain projects               |
| columnNumber              | column              | Could be unavailable for certain projects               |
| parentFileName            | fullFilename        |                                                         |

### Reachables slice

Reachables represent data flows that can originate from an entrypoint (source) and reach an external sink. These potentially represent the paths an adversary could take to reach and exploit a known vulnerability in a third-party library and hence the terms "reachable flows" or "Forward-Reachability". With atom, reachable slices can be generated for every language atom reads: Java and JVM bytecode, JavaScript and TypeScript, Python, PHP, Ruby, and C and C++.

A necessary pre-requisite is the project's Software Bill-of-Materials (SBOM): the purls on reachable flows are the ones it gives the packages, and atom makes none up. Pass it with `--bom-file` (a BOM or VDR) or `--bom-dir` (every `*bom*.json`, `*.cdx.json` and `*.vdr.json` under a directory); the two are mutually exclusive, and a BOM given this way replaces any in the source directory. Without either, a `bom.json` or `*.cdx.json` in the directory containing the source code is used. At present, only the SBOM generated by [CycloneDX Generator](https://github.com/CycloneDX/cdxgen) have the precision and depth required for computing reachables.

```bash
cdxgen -t js -o /tmp/reports/sbom-js.cdx.json /path/to/project
atom reachables -l js --bom-file /tmp/reports/sbom-js.cdx.json -o app.atom -s reachables.json /path/to/project
```

#### How to parse?

1. Parse the reachables json files. Multiple json files could be generated for large projects.
2. Iterate over the array. Each item in this array is an object containing flows (Reachable data-flows) and purls (List of [Package URLs](https://github.com/package-url/purl-spec))
3. Each item in the flows array is of type `node` similar to the nodes array in the data flow slice above.
4. Each item in the purls array is of type string

```json
[
  {
    "flows": [
      {
        "id": 44,
        "label": "METHOD_PARAMETER_IN",
        "name": "this",
        "fullName": "",
        "signature": "",
        "isExternal": false,
        "code": "this",
        "typeFullName": "com.example.SpringKafkaDemo.config.KafkaConsumerConfig",
        "parentMethodName": "consumerFactory",
        "parentMethodSignature": "org.springframework.kafka.core.ConsumerFactory()",
        "parentFileName": "src/main/java/com/example/SpringKafkaDemo/config/KafkaConsumerConfig.java",
        "parentPackageName": "com.example.SpringKafkaDemo.config",
        "parentClassName": "com.example.SpringKafkaDemo.config.KafkaConsumerConfig",
        "lineNumber": 36,
        "columnNumber": null,
        "tags": "framework-input"
      }
    ],
    "purls": [
      "pkg:maven/org.springframework.kafka/spring-kafka@2.8.11?type=jar"
    ]
  }
]
```

## Forwarded-header tags

Three tag pairs mark request values a client can choose through the `Forwarded` and
`X-Forwarded-*` headers. They ride on CALL nodes, appear in the `tags` field of slice records the
way `framework-input` does, and are named for what the code does, not for what a deployment is:

| tag | value | on |
| --- | --- | --- |
| `proxy-derived` | `client-ip`, `host`, `scheme` | a call that returns the client IP, host or scheme as the framework resolves it (`request.getRemoteAddr()`, `req.ip`, `request.remote_addr`, `$request->getClientIp()`, `request.remote_ip`) |
| `trusted-proxy-config` | the framework (`express`, `koa`, `fastify`, `spring`, `jetty`, `reactor-netty`, `werkzeug`, `django`, `symfony`, `laravel`, `rails`) | the call or assignment that turns on forwarded-header handling (`app.set('trust proxy', ...)`, `ProxyFix(...)`, `Request::setTrustedProxies(...)`) |
| `forwarded-header-read` | the header name, lower-cased, with the CGI spelling decoded (`HTTP_X_FORWARDED_FOR` gives `x-forwarded-for`) | a direct read of a forwarded header by name, where the name is a literal |

A fourth pair, `trusted-proxy-headers` (`forwarded`, `x-forwarded`, `all`), rides beside
`trusted-proxy-config` where a literal in the configuration names the header family it trusts.

What these tags do not mean: a `proxy-derived` value is only spoofable behind a reverse proxy
that passes the headers through to the application, and a `trusted-proxy-config` call only says
the application turns the handling on, not that the proxies in front of it are configured
correctly. Static analysis cannot see the proxy, so the tags record where the code reads these
values and leave the deployment question to the reader.

They are deliberately not among the default source tags, so they add no flows to a default
reachables run (records of a default run do show them in their `tags` field). Ask for flows that
start at them explicitly:

```bash
atom reachables -l js --source-tag proxy-derived -o app.atom -s reachables.json /path/to/project
```

## Generate slices

Use the atom cli to generate slices.

### Create a reachables slice for a java project.

```shell
cdxgen -t java --deep -o bom.json .
atom reachables -o app.atom --slice-outfile df.json -l java .
```

### Create a data flow slice for a java project.

```shell
atom data-flow -o app.atom --slice-outfile df.json -l java .
```

### Create a usages slice for a java project.

```shell
atom usages -o app.atom --slice-outfile usages.json -l java .
```

## Develop a custom slicer

Planned for 2.0.0 release
