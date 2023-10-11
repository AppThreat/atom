# Atom (⚛)

Atom is a novel intermediate representation for applications and a standalone tool powered by the [chen](https://github.com/AppThreat/chen) library. The intermediate representation is optimized for operations typically used for application analytics and machine learning, including [slicing](./specification/docs/slices.md) and [vectoring](./specification/docs/vectors.md).

Our vision is to make atom useful for a number of use cases such as:

- **Supply-chain analysis:** Generate evidence of external library usage including the flow of data from sources to sinks. Atom is used by [OWASP cdxgen](https://github.com/CycloneDX/cdxgen) to improve the precision and comprehensiveness of the generated CycloneDX document.
- **Vulnerability analysis:** Describe vulnerabilities with evidence of affected symbols, call paths, and data-flows. Enable variant and reachability analysis at scale.
- **Exploit prediction:** Predict exploits using precise representations of vulnerabilities, libraries, and applications.
- **Threat-model and attack vectors generation:** Generate precise threat-models and attack vectors for applications at scale.
- **Application context detection:** Generate context useful for summarization and risk-profile generation (e.g. services, endpoints, and data attributes).
- **Mind-maps for applications:** Automate summarization of large and complex applications as a developer tool.

and more.

[![release](https://github.com/appthreat/atom/actions/workflows/npm-release.yml/badge.svg)](https://github.com/appthreat/atom/actions/workflows/npm-release.yml)
![npm](https://img.shields.io/npm/dw/@appthreat/atom)
[![Discord](https://img.shields.io/badge/-Discord-lime?style=for-the-badge&logo=discord&logoColor=white&color=black)](https://discord.gg/tmmtjCEHNV)

![Atom logo](./specification/docs/Atom-logo.png)

## Installation

Atom comprises a core (standalone chen application developed in scala) with a nodejs wrapper module. It is currently distributed as an npm package.

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
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice - defaults to 7.
  --sink-filter <value>    filters on the sink's `code` property. Uses regex.
Command: usages [options]
Extract local variable and parameter usages
  --min-num-calls <value>  the minimum number of calls required for a usage slice - defaults to 1.
  --include-source         includes method source code in the slices - defaults to false.
Command: reachables [options]
Extract reachable data-flow slices based on automated framework tags
--source-tag <value>     source tag - defaults to framework-input.
--sink-tag <value>       sink tag - defaults to framework-output.
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

Learn more about [slices](./specification/docs/slices.md) or view some [samples](https://github.com/AppThreat/atom-samples)

## Languages supported

- C/C++ (Requires Java 17 or above)
- H (C/C++ Header files alone)
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

Atom files (app.⚛ or app.atom) are zip files with serialized protobuf data. Atom cli is the preferred approach to generate these files. It is possible to author a generator tool from scratch using the [proto specification](./specification/atom.proto). We offer samples in [Python](./specification/samples/python-atomgen/README.md) and [Deno](./specification/samples/deno-atomgen/README.md) for interested users. We also offer proto bindings in additional languages which can be found [here](./specification/bindings/).

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
sbt clean stage scalafmt test createDistribution
cd wrapper/nodejs
bash build.sh && sudo npm install -g .
```

## Using atom with chennai

[chennai](https://github.com/AppThreat/chen) is the recommended query interface for working with atom.

```shell
chennai> importAtom("/home/almalinux/work/sandbox/apollo/app.atom")
Loading base CPG from: /home/almalinux/workspace/app.atom1/cpg.bin.tmp
res1: Option[Cpg] = Some(value = Cpg (Graph [122094 nodes]))
```

## Using atom with joern

At present, atom files are compatible with joern 2.x. However, atom files have specific overlays and enhancements and therefore must be imported with `enhance=false` as shown:

```shell
joern> importCpg("/home/almalinux/work/sandbox/apollo/app.atom", enhance=false)
Creating project `app.atom1` for CPG at `/home/almalinux/work/sandbox/apollo/app.atom`
Creating working copy of CPG to be safe
Loading base CPG from: /home/almalinux/workspace/app.atom1/cpg.bin.tmp
res1: Option[Cpg] = Some(value = Cpg (Graph [122094 nodes]))
```

## Enterprise support

Enterprise support including custom language development and integration services are available via AppThreat Ltd. Free community support is also available via [discord](https://discord.gg/tmmtjCEHNV).
