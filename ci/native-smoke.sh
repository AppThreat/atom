#!/usr/bin/env bash
# Smoke test a native atom binary on the JVM bytecode languages (#271).
#
# The native image has no jrt:/ file system, so the jar, jimple and scala frontends read the JDK
# classes from an installed JDK's lib/modules. This builds a small jar and class directory (and a
# Scala 3 one when scalac is on PATH), then checks, for each language, that atom exits 0 and writes
# a usages slice that resolves JDK calls. It also checks JDK discovery through PATH alone and the
# no-JDK fallback, and that the Python, Java (Lombok), JavaScript and TypeScript source
# frontends keep files whose AST classes they look up by name.
#
# usage: bash ci/native-smoke.sh <atom binary>
#   JAVA_HOME  the JDK for javac and for atom to read (required)
#   astgen     on PATH for the TypeScript and reachables checks; without it the checks fail under
#              CI and are skipped elsewhere
#   phpastgen  on PATH for the PHP reachables check (a 2.x generator from
#              @appthreat/atom-parsetools); without it the check fails under CI and is skipped
#              elsewhere

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

# reach <name> <language> <input> [env words...] [-- atom args...]
#   Runs `atom reachables` behind the env words with the extra atom args, then checks the exit
#   status and that the slice reports at least one flow group. The image losing source files, or a
#   sink tagger it cannot run, shows up here as an empty slice with exit 0 (#395), which the
#   usages checks above cannot see.
reach() {
  local name="$1" lang="$2" input="$3"
  shift 3
  local prefix=() args=()
  while [ $# -gt 0 ] && [ "$1" != "--" ]; do prefix+=("$1"); shift; done
  [ $# -gt 0 ] && shift && args=("$@")
  local out="$WORK/out/$name"
  mkdir -p "$out"
  local rc=0
  "${prefix[@]}" "$ATOM_BIN" reachables -l "$lang" ${args[@]+"${args[@]}"} -o "$out/app.atom" \
    -s "$out/reachables.json" "$input" > "$out/log.txt" 2>&1 || rc=$?
  if [ "$rc" -ne 0 ] || [ ! -s "$out/reachables.json" ] ||
    ! grep -qF '"flows":[{"' "$out/reachables.json"; then
    echo "FAIL $name: exit $rc, flows $( [ -s "$out/reachables.json" ] && echo none || echo missing )"
    grep -v '^WARNING' "$out/log.txt" | head -20
    failures=$((failures + 1))
  else
    echo "ok   $name"
  fi
}

DIGEST='java.security.MessageDigest.getInstance:java.security.MessageDigest(java.lang.String)'
check jar jar "$WORK/jarin" "$DIGEST" env
check jimple jimple "$WORK/classes" "$DIGEST" env
check scala scala "$SCALA_INPUT" "$DIGEST" env
# JDK discovery through --jdk-path alone: the environment loses JAVA_HOME, ATOM_JAVA_HOME and
# JDK_HOME, and the JDK bin goes on PATH only so the launcher script can start, since a macOS
# java on PATH can be the /usr/bin stub, which does not start without JAVA_HOME.
check jdk-path jar "$WORK/jarin" "$DIGEST" env -u JAVA_HOME -u ATOM_JAVA_HOME -u JDK_HOME \
  PATH="$JAVA_HOME/bin:$PATH" -- --jdk-path "$JAVA_HOME"
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

# The source frontends look AST classes and getters up by name, and a lookup the image has no
# metadata for loses the whole file without an error. A class with Lombok's @Getter is replayed
# through the JavaParser getters, so the generated getter shows only when the file survives.
mkdir -p "$WORK/lombok/src/demo"
cat > "$WORK/lombok/src/demo/Account.java" <<'EOF'
package demo;

import lombok.Getter;

@Getter
public class Account {
    private String owner;

    public void run(String cmd) throws Exception {
        Runtime.getRuntime().exec(cmd);
    }
}
EOF
check lombok java "$WORK/lombok" 'demo.Account.getOwner:java.lang.String()' env

# --parse-report: the file the frontend cannot parse is named in the report, with its outcome and
# the parser's reason, while its good twin stays out of the report and in the graph (chen#255).
mkdir -p "$WORK/pr"
cat > "$WORK/pr/Main.java" <<'EOF'
import java.security.MessageDigest;

public class Main {
    public static void main(String[] args) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
    }
}
EOF
printf 'class Broken {{{\n' > "$WORK/pr/Broken.java"
check parse-report java "$WORK/pr" "$DIGEST" env -- --parse-report "$WORK/out/parse-report/parse.json"
if [ -s "$WORK/out/parse-report/parse.json" ] &&
  grep -q '"file":"Broken.java"' "$WORK/out/parse-report/parse.json" &&
  grep -q '"outcome":"unparsed"' "$WORK/out/parse-report/parse.json" &&
  ! grep -q '"file":"Main.java"' "$WORK/out/parse-report/parse.json"; then
  echo "ok   parse-report-report"
else
  echo "FAIL parse-report-report: the report does not name exactly Broken.java"
  cat "$WORK/out/parse-report/parse.json" 2>/dev/null | head -5 || true
  failures=$((failures + 1))
fi

# Each astgen node type becomes its Babel object by name: a type alias, a namespace, a dotted
# type reference and a non-null assertion before a member access each lost this file before.
# The JavaScript frontend needs astgen (npm install -g @appthreat/atom-parsetools).
mkdir -p "$WORK/ts"
cat > "$WORK/ts/app.ts" <<'EOF'
import express from "express";
import { exec } from "child_process";

type Cmd = string;
namespace Shell {
  export const prefix: Cmd = "sh";
}

export function run(req: express.Request, res: express.Response) {
  const cmd: Cmd = req.params!.name;
  exec(`${Shell.prefix} ${cmd}`);
  res.send("ok");
}
EOF
if command -v astgen > /dev/null 2>&1; then
  check typescript ts "$WORK/ts" '"resolvedMethod":"child_process:exec"' env
elif [ -n "${CI:-}" ]; then
  echo "FAIL typescript: astgen is not on PATH"
  failures=$((failures + 1))
else
  echo "skip typescript: astgen is not on PATH"
fi

# Every astgen node type becomes its Babel object by name, and a file whose lookup fails is
# dropped whole: the Directive and DirectiveLiteral nodes of a "use strict" directive had no
# metadata at 4.0.0, so every JavaScript file carrying the directive lost its calls from the
# slice (#396). A require call from the file resolves the module only while the file survives.
mkdir -p "$WORK/js"
cat > "$WORK/js/app.js" <<'EOF'
"use strict";

const { exec } = require("child_process");

// Run cmd through the shell and report whether it succeeded.
function run(cmd) {
  exec(cmd, { shell: "/bin/sh" });
  return true;
}

module.exports = run;
EOF
if command -v astgen > /dev/null 2>&1; then
  check javascript js "$WORK/js" '"resolvedMethod":"child_process"' env
elif [ -n "${CI:-}" ]; then
  echo "FAIL javascript: astgen is not on PATH"
  failures=$((failures + 1))
else
  echo "skip javascript: astgen is not on PATH"
fi


# The same Babel nodes the front end looks up by name must survive into the reachables output: the
# released 4.0.0 image kept 21 of 98 source files of the express checkout from #395 and reported no
# flows with exit 0. A request parameter reaching a response send through a second source file
# fails both ways - files dropped and taggers lost - so the slice has to hold a flow group.
mkdir -p "$WORK/js/src/routes"
cat > "$WORK/js/src/app.js" <<'EOF'
const express = require("express");
const userRoutes = require("./routes/user");
const app = express();

app.use("/users", userRoutes);
app.listen(3000);
EOF
cat > "$WORK/js/src/routes/user.js" <<'EOF'
const express = require("express");
const router = express.Router();

function shout(value) {
  return value + "!";
}

router.get("/:name", function (req, res) {
  const name = req.query.name;
  res.send(shout(name));
});

module.exports = router;
EOF
if command -v astgen > /dev/null 2>&1; then
  reach js-reachables js "$WORK/js" env
elif [ -n "${CI:-}" ]; then
  echo "FAIL js-reachables: astgen is not on PATH"
  failures=$((failures + 1))
else
  echo "skip js-reachables: astgen is not on PATH"
fi

# A Composer project whose dependency calls are recorded at the boundary: `vendor/` holds no code,
# only the composer metadata the graph stores as config files, and the reachables sinks for the
# calls into the packages are written by atom's own taggers from that metadata - circe parsing
# included - with the package URL on the flow. A request parameter reaches a Guzzle call.
mkdir -p "$WORK/php/src" "$WORK/php/vendor/composer"
cat > "$WORK/php/composer.json" <<'EOF'
{
  "name": "synthetic/basic",
  "require": {"guzzlehttp/guzzle": "^7.0"},
  "autoload": {"psr-4": {"App\\": "src/"}}
}
EOF
cat > "$WORK/php/vendor/composer/installed.json" <<'EOF'
{
    "packages": [
        {
            "name": "guzzlehttp/guzzle",
            "version": "7.16.1",
            "install-path": "../guzzlehttp/guzzle"
        }
    ],
    "dev": false
}
EOF
cat > "$WORK/php/vendor/composer/autoload_psr4.php" <<'EOF'
<?php

// autoload_psr4.php @generated by Composer

$vendorDir = dirname(__DIR__);
$baseDir = dirname($vendorDir);

return array(
    'App\\' => array($baseDir . '/src'),
    'GuzzleHttp\\' => array($vendorDir . '/guzzlehttp/guzzle/src'),
);
EOF
cat > "$WORK/php/src/Handler.php" <<'EOF'
<?php

namespace App;

use GuzzleHttp\Client;

class Handler
{
    private Client $client;

    public function __construct(Client $client)
    {
        $this->client = $client;
    }

    public function handle(Request $request): void
    {
        $path = $request->get('path');
        $this->client->request('POST', $path);
    }
}
EOF
cat > "$WORK/php/src/Request.php" <<'EOF'
<?php

namespace App;

class Request
{
    public function get(string $key): ?string
    {
        return $_GET[$key] ?? null;
    }
}
EOF
# `|| true`: under pipefail a missing or failing phpastgen would abort the script here instead of
# reaching the skip/fail branch below.
gen_major="$(phpastgen --parser-info 2> /dev/null |
  sed -n 's/.*Generator version: \([0-9]*\).*/\1/p' | head -1 || true)"
if command -v phpastgen > /dev/null 2>&1 && [ -n "$gen_major" ] && [ "$gen_major" -ge 2 ]; then
  reach php-reachables php "$WORK/php" env
  if grep -q 'pkg:composer/guzzlehttp/guzzle@7.16.1' "$WORK/out/php-reachables/reachables.json"; then
    echo "ok   php-reachables-purl"
  else
    echo "FAIL php-reachables-purl: the Guzzle flow carries no Composer package URL"
    head -2 "$WORK/out/php-reachables/reachables.json"
    failures=$((failures + 1))
  fi
elif [ -n "${CI:-}" ]; then
  echo "FAIL php-reachables: a 2.x phpastgen is not on PATH"
  failures=$((failures + 1))
else
  echo "skip php-reachables: a 2.x phpastgen is not on PATH"
fi

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
