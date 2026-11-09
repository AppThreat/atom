---
sidebar_position: 2
title: Installation
---

# Installing Atom

## Installation

Atom comprises a core (standalone chen application developed in scala) with a nodejs wrapper module. It is currently distributed as an npm package.

```shell
npm install @appthreat/atom @appthreat/atom-parsetools
# sudo npm install -g @appthreat/atom @appthreat/atom-parsetools
```

atom-parsetools 2.x also installs `parsa`, the binary astgen uses to write JavaScript and TypeScript
type maps with TypeScript 7's checker, as an npm package for the host's platform. Nothing needs to be
set; with `--omit=optional`, or with atom-parsetools 1.x, astgen uses TypeScript 6 instead.
`atom --version` prints the versions of atom, chen, atom-parsetools and parsa that a run will use.

Install cdxgen to generate a Software Bill-of-Materials which is required for reachables slicing.

```shell
npm install -g @cdxgen/cdxgen --omit=optional
```
