#!/usr/bin/env bash
set -e
rm -rf plugins/bin plugins/lib
if [ -e "../../target/atom.zip" ]; then
    cp -rf ../../target/atom.zip plugins/
    unzip -q plugins/atom.zip -d plugins
    rm plugins/atom.zip
else
    echo "Build the atom project using 'sbt createDistribution' before running this script."
fi
if [ -e "../../*.cdx.json" ]; then
  cp "../../*.cdx.json" plugins/
else
  echo "Generate an SBOM with cdxgen before running this script for inclusion."
fi
npm ci
