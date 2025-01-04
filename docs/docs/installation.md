---
sidebar_position: 2
title: Installation
---

# Installing Atom

## Installation

Atom comprises a core (standalone chen application developed in scala) with a nodejs wrapper module. It is currently distributed as an npm package.

```shell
npm install @appthreat/atom
# sudo npm install -g @appthreat/atom
```

Install cdxgen to generate a Software Bill-of-Materials which is required for reachables slicing.

```shell
npm install -g @cyclonedx/cdxgen --omit=optional
```