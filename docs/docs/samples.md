---
sidebar_position: 6
title: Sample Invocations
---

# Sample Invocations

### Generate an atom

```shell
# Compile java project
atom -o app.atom -l java .
```

```shell
atom -o app.atom -l jar <jar file>
```

```shell
export ANDROID_HOME=<path to android sdk>
atom -o app.atom -l apk <apk file>
```

### Create reachables slice for a java project.

```shell
cd <path to repo>
cdxgen -t java --deep -o bom.json .
atom reachables -o app.atom -s reachables.json -l java .
```

### Create data-flow slice for a java project.

```shell
atom data-flow -o app.atom --slice-outfile df.json -l java .
```

### Create usages slice for a java project.

```shell
atom usages -o app.atom --slice-outfile usages.json -l java .
```

Learn more about [slices](./specification/docs/slices.md) or view some [samples](https://github.com/AppThreat/atom-samples)

### Extract HTTP endpoints in openapi format using atom-tools

Atom can automatically invoke [atom-tools](https://github.com/AppThreat/atom-tools) `convert` command to extract http endpoints from the usages slices. Pass the argument `--extract-endpoints` to enable this feature.

```shell
pip install atom-tools
atom usages --extract-endpoints -o app.atom --slice-outfile usages.json -l java .
```

A file called `openapi.generated.json` would be created with the endpoints information.

### Export atom to graphml or dot format

It is possible to export each method along with data dependencies in an atom to graphml or dot format. Simply pass `--export` to enable this feature.

```shell
atom -o app.atom -l java --export-atom --export-dir <export dir> <path to application>
```

The resulting graphml files could be imported into [Neo4j](https://neo4j.com/labs/apoc/4.1/import/graphml/) or NetworkX for further analysis. Use the argument `--export-format` for dot format.

```shell
atom -o app.atom -l java --export-atom --export-format dot --export-dir <export dir> <path to application>
```

In dot format, individual representations such as ast, cdg, and cfg would also get exported.

To also compute and include data-dependency graph (DDG) information in the exported files, pass `--with-data-deps`

```shell
atom -o app.atom -l java --export-atom --export-dir <export dir> --with-data-deps <path to application>
```
