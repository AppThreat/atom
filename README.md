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

```shell
npm install @appthreat/atom
# sudo npm install -g @appthreat/atom
```

## Usage

```shell
Usage: atom [parsedeps] [options] [input]

  input                    source file or directory
  -o, --output <value>     output filename. Default app.⚛
  -l, --language <value>   source language
Command: parsedeps

Misc
  -s, --slice              export intra-procedural slices as json
  --slice-outfile <value>  slice output filename
  --slice-depth <value>    the max depth to traverse the DDG for the data-flow slice (for `DataFlow` mode) - defaults to 3
  -m, --mode <value>       the kind of slicing to perform - defaults to `DataFlow`. Options: [DataFlow, Usages]
  --max-num-def <value>    maximum number of definitions in per-method data flow calculation. Default 2000
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
