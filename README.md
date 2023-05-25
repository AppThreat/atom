# Atom

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

  input                   source file or directory
  -o, --output <value>    output filename
  -l, --language <value>  source language
Misc
  --help                  display this help message
```

## License

Apache-2.0

## Developing / Contributing

```shell
sbt scalafmt
sbt stage
```
