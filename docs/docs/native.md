---
sidebar_position: 4
title: Atom Native-Image
---

# Atom Native-Image for Advanced Users

atom is available as a native image built using graalvm community edition.

```shell
curl -LO https://github.com/AppThreat/atom/releases/latest/download/atom-amd64
chmod +x atom-amd64
./atom-amd64 --help
```

On Windows

```pwsh
curl -LO https://github.com/AppThreat/atom/releases/latest/download/atom.exe
.\atom.exe --help
```

NOTE: Commands such as cdxgen, astgen, and phpastgen are not bundled into this native image. Therefore, the binary is quite limited in functionality.