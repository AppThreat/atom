#!/usr/bin/env bash
# bash ci/native-image.sh
# sdk use java 25-graalce

echo "About to build the native image. This would take a few minutes ..."
sbt "GraalVMNativeImage / packageBin"

if [ -f "target/graalvm-native-image/atom" ]; then
    chmod +x target/graalvm-native-image/atom
    target/graalvm-native-image/atom --help
    echo "atom native-image was built successfully."
    echo "GraalVM CE builds are released under version 2 of the GNU General Public License with the \"Classpath\" Exception (GPLv2+CPE)"
    echo "Using Oracle GraalVM to build this image? Adhere to the terms of the license - https://www.oracle.com/downloads/licenses/graal-free-license.html"
else
    echo "atom native-image was not built correctly. Check if you have GraalVM CE 25 installed."
fi

