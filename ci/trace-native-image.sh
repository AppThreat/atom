#!/usr/bin/env bash

# Trace the atom executable using Graal native-image-agent to produce an optimized native binary
#
# bash ci/trace-native-image.sh java <file path>
# bash ci/trace-native-image.sh js <file path>
# bash ci/trace-native-image.sh py <file path>
# bash ci/trace-native-image.sh c <file path>

# sdk use java 21.0.2-graalce

# sbt clean stage
./atom.sh -J-XX:MinRAMPercentage=30 -J-XX:MaxRAMPercentage=90 -J-agentlib:native-image-agent=config-merge-dir=src/main/resources/META-INF/native-image reachables -l $1 -o /tmp/app.atom -s /tmp/reachables.slices.json $2
rm /tmp/app.atom /tmp/reachables.slices.json

# bash ci/native-image.sh
