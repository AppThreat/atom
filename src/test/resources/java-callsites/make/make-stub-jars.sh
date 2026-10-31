#!/usr/bin/env bash
# Rebuilds the four jars in ../lib from the sources in this directory. The jars are
# checked in, so this only matters when a stub changes: stub-web and shaded-biz carry
# their Maven coordinates in META-INF/maven/**/pom.properties, dup-a and dup-b both
# carry dup.Shared so a class two artifacts contain stays unmapped.
set -euo pipefail
cd "$(dirname "$0")"
work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT

classes="$work/classes"
mkdir -p "$classes"
javac -d "$classes" ./*.java

layout() {
    local jar=$1 group=$2 artifact=$3 version=$4 shift_pkg=$5
    local root="$work/pack-$jar"
    local out="$work/out"
    mkdir -p "$root/META-INF/maven/$group/$artifact"
    printf 'groupId=%s\nartifactId=%s\nversion=%s\n' "$group" "$artifact" "$version" \
        > "$root/META-INF/maven/$group/$artifact/pom.properties"
    mkdir -p "$root/classes"
    cp -r "$classes/$shift_pkg" "$root/classes/"
    mkdir -p "$out"
    jar cf "$out/$jar" -C "$root" META-INF -C "$root/classes" .
}

layout stub-web-1.0.0.jar   com.example stub-web   1.0.0 web
layout shaded-biz-2.0.0.jar com.example shaded-biz 2.0.0 shaded
layout dup-a-1.0.0.jar      com.example dup-a      1.0.0 dup
layout dup-b-1.0.0.jar      com.example dup-b      1.0.0 dup

cp "$work"/out/*.jar ../lib/
echo "rebuilt ../lib"
