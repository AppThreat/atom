---
sidebar_position: 10
title: Development
---

# Developing / Contributing

atom builds with sbt 2 and JDK 23 or later (CI uses JDK 25), and needs Node.js for the parsers and
the npm wrapper. chen, cpg2 and overflowdb2 come from GitHub Packages, so the build needs a
`GITHUB_TOKEN` that can read packages.

```shell
export GITHUB_TOKEN=$(gh auth token)
sbt "clean; stage; scalafmt; testFull; createDistribution"
cd wrapper/nodejs
bash build.sh && sudo npm install -g .
```
