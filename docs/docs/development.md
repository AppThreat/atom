---
sidebar_position: 10
title: Development
---

# Developing / Contributing

Install Java 21
Node.js > 21

```shell
sbt clean stage scalafmt test createDistribution
cd wrapper/nodejs
bash build.sh && sudo npm install -g .
```
