#!/usr/bin/env bash
# Smoke test a native atom binary on the JVM bytecode languages (#271).
#
# The native image has no jrt:/ file system, so the jar, jimple and scala frontends read the JDK
# classes from an installed JDK's lib/modules. This builds a small jar and class directory (and a
# Scala 3 one when scalac is on PATH), then checks, for each language, that atom exits 0 and writes
# a usages slice that resolves JDK calls. It also checks JDK discovery through PATH alone and the
# no-JDK fallback.
#
# usage: bash ci/native-smoke.sh <atom binary>
#   JAVA_HOME  the JDK for javac and for atom to read (required)

set -euo pipefail

ATOM_BIN="${1:?usage: bash ci/native-smoke.sh <atom binary>}"
ATOM_BIN="$(cd "$(dirname "$ATOM_BIN")" && pwd)/$(basename "$ATOM_BIN")"
: "${JAVA_HOME:?JAVA_HOME must point at a JDK}"
JAVAC="$JAVA_HOME/bin/javac"
JAR="$JAVA_HOME/bin/jar"

WORK="$(mktemp -d "${TMPDIR:-/tmp}/atom-native-smoke.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
failures=0

mkdir -p "$WORK/src/demo" "$WORK/classes" "$WORK/jarin"
cat > "$WORK/src/demo/Main.java" <<'EOF'
package demo;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    static byte[] digest(String input) throws Exception {
        return MessageDigest.getInstance("SHA-256").digest(input.getBytes());
    }

    static Connection open(String url) throws Exception {
        return DriverManager.getConnection(url);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(digest(String.join(" ", args)).length);
    }
}
EOF
"$JAVAC" -g -d "$WORK/classes" "$WORK/src/demo/Main.java"
"$JAR" cf "$WORK/jarin/app.jar" -C "$WORK/classes" .

# The reproducer from #271, when a Scala 3 compiler is available.
SCALA_INPUT="$WORK/classes"
if command -v scalac > /dev/null 2>&1; then
  mkdir -p "$WORK/scala"
  cat > "$WORK/scala/Main.scala" <<'EOF'
import java.security.MessageDigest

object Main:
  def main(args: Array[String]): Unit =
    println(MessageDigest.getInstance("SHA-256").digest(args.mkString.getBytes).length)
EOF
  if scalac -d "$WORK/scala" "$WORK/scala/Main.scala" > "$WORK/scalac.log" 2>&1; then
    SCALA_INPUT="$WORK/scala"
  else
    echo "scalac failed; testing -l scala on the Java classes instead"
    cat "$WORK/scalac.log"
  fi
fi

# check <name> <language> <input> <expected call> [env words...] [-- atom args...]
#   Runs `atom usages` behind the env words (e.g. env -u JAVA_HOME) with the extra atom args, then
#   checks the exit status, the slice and the expected resolved JDK call.
check() {
  local name="$1" lang="$2" input="$3" expected="$4"
  shift 4
  local prefix=() args=()
  while [ $# -gt 0 ] && [ "$1" != "--" ]; do prefix+=("$1"); shift; done
  [ $# -gt 0 ] && shift && args=("$@")
  local out="$WORK/out/$name"
  mkdir -p "$out"
  local rc=0
  "${prefix[@]}" "$ATOM_BIN" usages -l "$lang" ${args[@]+"${args[@]}"} -o "$out/app.atom" \
    -s "$out/usages.json" "$input" > "$out/log.txt" 2>&1 || rc=$?
  if [ "$rc" -ne 0 ] || [ ! -s "$out/usages.json" ]; then
    echo "FAIL $name: exit $rc, slice $( [ -s "$out/usages.json" ] && echo written || echo missing )"
    grep -v '^WARNING' "$out/log.txt" | head -20
    failures=$((failures + 1))
  elif ! grep -q "$expected" "$out/usages.json"; then
    echo "FAIL $name: the slice does not resolve $expected"
    failures=$((failures + 1))
  else
    echo "ok   $name"
  fi
}

DIGEST='java.security.MessageDigest.getInstance:java.security.MessageDigest(java.lang.String)'
check jar jar "$WORK/jarin" "$DIGEST" env
check jimple jimple "$WORK/classes" "$DIGEST" env
check scala scala "$SCALA_INPUT" "$DIGEST" env
check jdk-path jar "$WORK/jarin" "$DIGEST" env -u JAVA_HOME -u ATOM_JAVA_HOME -u JDK_HOME \
  -- --jdk-path "$JAVA_HOME"
check path-only jimple "$WORK/classes" 'java.sql.DriverManager.getConnection' \
  env -u JAVA_HOME -u ATOM_JAVA_HOME -u JDK_HOME PATH="$JAVA_HOME/bin:/usr/bin:/bin"

# Python identifiers are NFKC-normalised as CPython does (PEP 3131), which needs the JDK's ICU
# nfkc.nrm in the image: eval spelled in fullwidth letters (U+FF45 U+FF56 U+FF41 U+FF4C, written
# as UTF-8 bytes so this script stays ASCII) calls eval, and the usages slice reports the spelling
# as a source-integrity finding.
mkdir -p "$WORK/py"
EVAL_FULLWIDTH="$(printf '\357\275\205\357\275\226\357\275\201\357\275\214')"
printf 'import os\n\ndef run(cmd):\n    return %s(cmd)\n' "$EVAL_FULLWIDTH" > "$WORK/py/app.py"
check python python "$WORK/py" "\"name\":\"eval\",\"detail\":\"$EVAL_FULLWIDTH\"" env

# Without any JDK, atom still builds the atom, with phantom JDK types, and says so.
mkdir -p "$WORK/out/no-jdk"
rc=0
"$ATOM_BIN" -Dchen.jimple.jdk-classes=none usages -l jar -o "$WORK/out/no-jdk/app.atom" \
  -s "$WORK/out/no-jdk/usages.json" "$WORK/jarin" > "$WORK/out/no-jdk/log.txt" 2>&1 || rc=$?
if [ "$rc" -eq 0 ] && [ -s "$WORK/out/no-jdk/usages.json" ] &&
  grep -q "no JDK found to resolve the JDK types" "$WORK/out/no-jdk/log.txt"; then
  echo "ok   no-jdk"
else
  echo "FAIL no-jdk: exit $rc"
  grep -v '^WARNING' "$WORK/out/no-jdk/log.txt" | head -20
  failures=$((failures + 1))
fi

if [ "$failures" -ne 0 ]; then
  echo "$failures native smoke test(s) failed"
  exit 1
fi
echo "native smoke tests passed"
