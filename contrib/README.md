# Introduction

Atom works better under Java 21 with virtual threads. Use the provided patch for Java 21.

```shell
git apply --ignore-space-change --ignore-whitespace contrib/java21.patch
sbt clean stage createDistribution
```
