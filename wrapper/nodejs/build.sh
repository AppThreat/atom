#!/usr/bin/env bash
# Assemble the local @appthreat/atom JAR packages from a freshly built distribution.
#
# Prerequisite: run `sbt stage createDistribution` from the repo root first so that
# target/atom.zip exists. This script unzips that distribution and stamps the
# universal JAR package (@appthreat/atom-jar) plus the JAR-fallback platform
# packages (darwin-amd64, windows-arm64, linux-arm64-musl).
#
# Native-binary packages are assembled separately in the release workflow using the
# per-arch binaries downloaded as build artifacts (see scripts/assemble.mjs).
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "${SCRIPT_DIR}"

DIST_ZIP="../../target/atom.zip"
PLUGINS_DIR="plugins"

if [ ! -e "${DIST_ZIP}" ]; then
    echo "Build the atom project using 'sbt stage createDistribution' before running this script" >&2
    exit 1
fi

# Unzip the distribution (jars + launchers) into a staging plugins/ directory.
rm -rf "${PLUGINS_DIR}"
mkdir -p "${PLUGINS_DIR}"
unzip -q "${DIST_ZIP}" -d "${PLUGINS_DIR}"

# Generate package.json/LICENSE/README for every sub-package and stage parent metadata.
node scripts/assemble.mjs

# Copy the unpacked distribution into the universal JAR package and the JAR-fallback
# platform packages.
node scripts/assemble.mjs @appthreat/atom-jar "${PLUGINS_DIR}"
for pkg in @appthreat/atom-darwin-amd64 @appthreat/atom-windows-arm64 @appthreat/atom-linux-arm64-musl; do
    node scripts/assemble.mjs "${pkg}" "${PLUGINS_DIR}"
done

echo "Assembled JAR packages. Native packages are assembled separately from release artifacts."
