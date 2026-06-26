# Assemble the local @appthreat/atom JAR packages from a freshly built distribution.
#
# Prerequisite: run `sbt stage createDistribution` from the repo root first so that
# target/atom.zip exists. Native-binary packages are assembled separately in the
# release workflow (see scripts/assemble.mjs).
$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

if (-Not (Test-Path ..\..\target\atom.zip)) {
    Write-Error "Build the atom project using 'sbt stage createDistribution' before running this script"
    exit 1
}

# Unzip the distribution (jars + launchers) into a staging plugins\ directory.
Remove-Item -Recurse -Force plugins -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path plugins | Out-Null
Expand-Archive -Path ..\..\target\atom.zip -DestinationPath plugins -Force

# Generate package.json/LICENSE/README for every sub-package and stage parent metadata.
node scripts/assemble.mjs

# Copy the unpacked distribution into the universal JAR package and the JAR-fallback
# platform packages.
node scripts/assemble.mjs @appthreat/atom-jar plugins
foreach ($pkg in @("@appthreat/atom-darwin-amd64", "@appthreat/atom-windows-arm64", "@appthreat/atom-linux-arm64-musl")) {
    node scripts/assemble.mjs $pkg plugins
}

Write-Host "Assembled JAR packages. Native packages are assembled separately from release artifacts."
