# Atom (⚛)

```shell
 █████╗ ████████╗ ██████╗ ███╗   ███╗
██╔══██╗╚══██╔══╝██╔═══██╗████╗ ████║
███████║   ██║   ██║   ██║██╔████╔██║
██╔══██║   ██║   ██║   ██║██║╚██╔╝██║
██║  ██║   ██║   ╚██████╔╝██║ ╚═╝ ██║
╚═╝  ╚═╝   ╚═╝    ╚═════╝ ╚═╝     ╚═╝
```

## Installation

Download the pre-built distribution from the [releases](https://github.com/AppThreat/atom/releases)

```bash
curl -LO https://github.com/AppThreat/atom/releases/latest/download/atom.zip
unzip -q atom.zip -d /opt/
ln -s /opt/atom-1.0.0/bin/atom /usr/local/bin/atom
export ATOM_HOME=/opt/atom-1.0.0
```

## Usage

```shell
Usage: atom [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛
  -l, --language <value>   source language
Misc
  -s, --slice              export intra-procedural slices as json
  --slice-outfile <value>  slice output filename
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice (for `DataFlow` mode) - defaults to 3
  -m, --mode <value>       the kind of slicing to perform - defaults to `DataFlow`. Options: [DataFlow, Usages]
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation
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

```shell
sbt scalafmt
sbt stage
```
