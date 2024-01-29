#!/usr/bin/env bash
# bash ci/native-image.sh

# sdk use java 21.0.1-graalce

echo "About to build the native image. This would take a few minutes ..."
sbt "GraalVMNativeImage / packageBin"

if [ -f "target/graalvm-native-image/atom" ]; then
    chmod +x target/graalvm-native-image/atom
    target/graalvm-native-image/atom --help
    echo "atom native-image was built successfully."
    echo "Using Oracle GraalVM 21? Adhere to the terms of the license - https://www.oracle.com/downloads/licenses/graal-free-license.html"
else
    echo "atom native-image was not built correctly. Check if you have Oracle GraalVM 21 installed."
fi
