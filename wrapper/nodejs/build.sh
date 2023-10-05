#!/usr/bin/env bash
rm -rf plugins/bin plugins/lib
if [ -e "../../target/atom.zip" ]; then
    cp -rf ../../target/atom.zip plugins/
    unzip -q plugins/atom.zip -d plugins
    rm plugins/atom.zip
else
    echo "Build the atom project using 'sbt createDistribution' before running this script"
fi
npm install
