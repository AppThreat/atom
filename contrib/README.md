# Introduction

Atom works better under Java 21 with virtual threads. Use the provided patch for Java 21.

## Steps

```shell
git apply --ignore-space-change --ignore-whitespace contrib/java21.patch
sbt clean stage createDistribution
```

## Consideration

Atom performs well under Oracle GraalVM 21. When using this version, consider the terms of the [license](https://www.oracle.com/downloads/licenses/graal-free-license.html)

If in doubt, use the image `ghcr.io/appthreat/atom` which is equally performant and uses the GraalVM Community Edition.
