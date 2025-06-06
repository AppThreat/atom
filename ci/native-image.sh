#!/usr/bin/env bash
# bash ci/native-image.sh

# sdk use java 23.0.2-graalce

echo "About to build the native image. This would take a few minutes ..."
sbt "GraalVMNativeImage / packageBin"

if [ -f "target/graalvm-native-image/atom" ]; then
    chmod +x target/graalvm-native-image/atom
    target/graalvm-native-image/atom --help
    echo "atom native-image was built successfully."
    echo "Mandrel-based builds are released under version 2 of the GNU General Public License with the “Classpath” Exception - https://github.com/graalvm/mandrel/blob/default/LICENSE"
    echo "Using Oracle GraalVM to build this image? Adhere to the terms of the license - https://www.oracle.com/downloads/licenses/graal-free-license.html"
else
    echo "atom native-image was not built correctly. Check if you have Oracle GraalVM 23 installed."
fi
