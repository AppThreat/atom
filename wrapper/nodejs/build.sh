#!/usr/bin/env bash
set -e
rm -rf plugins/bin plugins/lib
if [ -e "../../target/atom.zip" ]; then
    cp -rf ../../target/atom.zip plugins/
    unzip -q plugins/atom.zip -d plugins
    rm plugins/atom.zip
else
    echo "Build the atom project using 'sbt createDistribution' before running this script"
fi

bash packages/atom-parsetools/build.sh

npm ci
