# Atom (⚛)

```shell
 █████╗ ████████╗ ██████╗ ███╗   ███╗
██╔══██╗╚══██╔══╝██╔═══██╗████╗ ████║
███████║   ██║   ██║   ██║██╔████╔██║
██╔══██║   ██║   ██║   ██║██║╚██╔╝██║
██║  ██║   ██║   ╚██████╔╝██║ ╚═╝ ██║
╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝     ╚═╝
```

[![release](https://github.com/appthreat/atom/actions/workflows/npm-release.yml/badge.svg)](https://github.com/appthreat/atom/actions/workflows/npm-release.yml)
![npm](https://img.shields.io/npm/dw/@appthreat/atom)
[![Discord](https://img.shields.io/badge/-Discord-lime?style=for-the-badge&logo=discord&logoColor=white&color=black)](https://discord.gg/tmmtjCEHNV)

## Installation

```shell
npm install @appthreat/atom
# sudo npm install -g @appthreat/atom
```

## Usage

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
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice - defaults to 3.
  --sink-filter <value>    filters on the sink's `code` property. Uses regex.
  --end-at-external-method
                           all slices must end at an external method - defaults to false.
Command: usages [options]
Extract local variable and parameter usages
  --min-num-calls <value>  the minimum number of calls required for a usage slice - defaults to 1.
  --exclude-operators      excludes operator calls in the slices - defaults to false.
  --exclude-source         excludes method source code in the slices - defaults to false.
  --help                   display this help message
```

## Languages supported

- C/C++
- Java
- Jar
- Android APK
- JavaScript
- TypeScript
- Python

## License

Apache-2.0

## Developing / Contributing

Install Java 17 or 19

```shell
sbt scalafmt
sbt stage
```
