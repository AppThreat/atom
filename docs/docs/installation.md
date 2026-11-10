---
sidebar_position: 2
title: Installation
---

# Installing Atom

## Installation

Atom is a Scala application built on chen, with a Node.js wrapper. It is distributed as an npm package, as
[native images](native.md) and as [container images](container.md). The npm package and the JVM distribution
need Java 23 or later.

```shell
npm install @appthreat/atom @appthreat/atom-parsetools
# sudo npm install -g @appthreat/atom @appthreat/atom-parsetools
```

atom-parsetools 2.x also installs `parsa`, the binary astgen uses to write JavaScript and TypeScript
type maps with TypeScript 7's checker, as an npm package for the host's platform. Nothing needs to be
set; with `--omit=optional`, or with atom-parsetools 1.x, astgen uses TypeScript 6 instead.
`atom --version` prints the versions of atom, chen, atom-parsetools and parsa that a run will use.

Install cdxgen to generate a CycloneDX SBOM. Reachables and callsites take package URLs from it
(`--bom-file`, or a BOM in the project directory); JavaScript reachables also work without one.

```shell
npm install -g @cdxgen/cdxgen --omit=optional
```
