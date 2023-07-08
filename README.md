# Atom (⚛)

```shell
 █████╗ ████████╗ ██████╗ ███╗   ███╗
██╔══██╗╚══██╔══╝██╔═══██╗████╗ ████║
███████║   ██║   ██║   ██║██╔████╔██║
██╔══██║   ██║   ██║   ██║██║╚██╔╝██║
██║  ██║   ██║   ╚██████╔╝██║ ╚═╝ ██║
╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝     ╚═╝
```

Atom is a novel intermediate representation for applications and a standalone tool that is powered by the [joern](https://joern.io) library. The intermediate representation is optimized for operations such as slicing typically used for application analytics. Our vision is to make atom useful for a number of use cases such as:

- **Supply-chain analysis:** Generate evidence of usages of external libraries including the flow of data from sources to sinks. Atom is used by [OWASP cdxgen](https://github.com/CycloneDX/cdxgen) to improve the precision and comprehensiveness of the generated CycloneDX document.
- **Vulnerability analysis:** Describing vulnerabilities with evidence of affected symbols, call paths, and data-flows. Enable variant and reachability analysis at scale.
- **Exploit prediction:** Predicting exploits using precise representations of vulnerabilities, libraries, and applications.
- **Threat-model and attack vectors generation:** Generating precise threat-models and attack vectors for applications at scale.
- **Application context detection:** Generating context such as services, endpoints, and data attributes useful for summarization and risk-profile generation.
- **Mind-maps for applications:** Automated summarization of large and complex applications as a tool for developers.

and more.

[![release](https://github.com/appthreat/atom/actions/workflows/npm-release.yml/badge.svg)](https://github.com/appthreat/atom/actions/workflows/npm-release.yml)
![npm](https://img.shields.io/npm/dw/@appthreat/atom)
[![Discord](https://img.shields.io/badge/-Discord-lime?style=for-the-badge&logo=discord&logoColor=white&color=black)](https://discord.gg/tmmtjCEHNV)

## Installation

Atom comprises of a core (standalone joern application developed in scala) with a wrapper nodejs module. It is currently distributed as an npm package.

```shell
npm install @appthreat/atom
# sudo npm install -g @appthreat/atom
```

## CLI Usage

```
Usage: atom [parsedeps|data-flow|usages] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛ or app.atom in windows
  --slice-outfile <value>  export intra-procedural slices as json
  -l, --language <value>   source language
  --with-data-deps         generate the atom with data-dependencies - defaults to `false`
  --file-filter <value>    the name of the source file to generate slices from.
  --method-name-filter <value>
                           filters in slices that go through specific methods by names. Uses regex.
  --method-parameter-filter <value>
                           filters in slices that go through methods with specific types on the method parameters. Uses regex.
  --method-annotation-filter <value>
                           filters in slices that go through methods with specific annotations on the methods. Uses regex.
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation - defaults to 2000
Command: parsedeps
Extract dependencies from the build file and imports
Command: data-flow [options]
Extract backward data-flow slices
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice - defaults to 10.
  --sink-filter <value>    filters on the sink's `code` property. Uses regex.
Command: usages [options]
Extract local variable and parameter usages
  --min-num-calls <value>  the minimum number of calls required for a usage slice - defaults to 1.
  --include-source         includes method source code in the slices - defaults to false.
  --help                   display this help message
```

## Sample Invocations

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

### Create data-flow slice for a java project.

```shell
atom data-flow -o app.atom --slice-outfile df.json -l java .
```

### Create usages slice for a java project.

```shell
atom usages -o app.atom --slice-outfile usages.json -l java .
```

## Languages supported

- C/C++ (Requires Java 17 or above)
- Java (Requires compilation)
- Jar
- Android APK (Requires Android SDK. Set the environment variable `ANDROID_HOME`)
- JavaScript
- TypeScript
- Python

## Atom Specification

The intermediate representation used by atom is available under the same open-source license (Apache-2.0). The specification is available in [protobuf](./specification/atom.proto), [markdown](./specification/docs/spec.md), and [html](./specification/docs/spec.html) formats.

The current specification version is 1.0.0

## Generating atom files

Atom files (app.⚛ or app.atom) are zip files with serialized protobuf data. Atom cli is the preferred approach to generate these files. It is possible to author a generator tool from scratch using the [proto specification](./specification/atom.proto). We offer samples in [Python](./specification/samples/python-atomgen/README.md) and [Deno](./specification/samples/deno-atomgen/README.md) for interested users. We also offer proto bindings in additional languages which could be found [here](./specification/bindings/).

Example code snippet for generating an atom in python.

```python
# Create a method fullname property
methodFullName = atom.CpgStructNodeProperty(
    name=atom.NodePropertyName.FULL_NAME, value=atom.PropertyValue("main")
)

# Create a method node with the fullname property
method = atom.CpgStructNode(
    key=1, type=atom.NodeType.METHOD, property=[methodFullName]
)

# Create an atom with a single node
atom_struct = atom.CpgStruct(node=[method])

# Create an atom (app.atom) by serializing this data into a zip file
with ZipFile("app.atom", "w") as zip_file:
    zip_file.writestr("cpg.proto", bytes(atom_struct))
```

## License

Apache-2.0

## Developing / Contributing

Install Java 17 or 19 (Recommended)

```shell
sbt scalafmt
sbt stage
```

## Enterprise support

Enterprise support including custom language development and integration services are available via AppThreat Ltd. Free community support is also available via [discord](https://discord.gg/tmmtjCEHNV).
