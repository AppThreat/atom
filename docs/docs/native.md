---
sidebar_position: 4
title: Atom Native-Image
---

# Atom Native-Image for Advanced Users

atom is available as native images built with GraalVM Community Edition, each with a `.sha512` file:
`atom-amd64` and `atom-arm64` for Linux, `atom-linux-musl-amd64` for musl, `atom-darwin-arm64` for macOS
on Apple silicon, and `atom.exe` for Windows x86_64.

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

The JVM bytecode languages (`jar`, `jimple`, `android`, `apk`, `dex`, `scala`) read the JDK classes from an
installed JDK 8 or later, found through `--jdk-path`, `ATOM_JAVA_HOME`, `JAVA_HOME`, `JDK_HOME`, the `java`
on `PATH` and common install locations. Without one, the JDK types are left as phantoms and atom says so.

The native images do not bundle astgen, phpastgen or rbastgen. Install `@appthreat/atom-parsetools` for
JavaScript, TypeScript, PHP and Ruby:

```shell
npm install -g @appthreat/atom-parsetools
```
