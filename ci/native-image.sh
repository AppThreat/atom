#!/usr/bin/env bash
# bash ci/native-image.sh

echo "Applying Java 21 patch"
git apply --ignore-space-change --ignore-whitespace contrib/java21.patch
if [ $? != 0 ]; then
    echo "Unable to patch the codebase correctly. Please file a bug report for atom."
fi
echo "About to build the native image. This would take a few minutes ..."
sbt clean stage scalafmt "GraalVMNativeImage / packageBin"

if [ -f "target/graalvm-native-image/atom" ]; then
    chmod +x target/graalvm-native-image/atom
    target/graalvm-native-image/atom --help
    echo "atom native-image was built successfully."
    echo "Using Oracle GraalVM 21? Adhere to the terms of the license - https://www.oracle.com/downloads/licenses/graal-free-license.html"
else
    echo "atom native-image was not built correctly. Check if you have Oracle GraalVM 21 installed."
fi
