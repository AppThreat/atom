#!/usr/bin/env node

import { freemem, platform as _platform } from "node:os";
import { dirname, join, delimiter } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const isWin = _platform() === "win32";
let url = import.meta.url;
if (!url.startsWith("file://")) {
  url = new URL(`file://${import.meta.url}`).toString();
}
const dirName = import.meta ? dirname(fileURLToPath(url)) : __dirname;

const LOG4J_CONFIG = join(dirName, "plugins", "log4j2.xml");
const ATOM_HOME = join(dirName, "plugins", "atom-1.0.0");
const APP_LIB_DIR = join(ATOM_HOME, "lib");
const freeMemoryGB = Math.floor(freemem() / 1024 / 1024 / 1024);
const JVM_ARGS =
  "-XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:+UnlockDiagnosticVMOptions -XX:G1SummarizeRSetStatsPeriod=1";
const JAVA_OPTS = `${process.env.JAVA_OPTS || ""} -Xms${Math.round(
  Math.floor(freeMemoryGB / 2)
)}G -Xmx${freeMemoryGB}G ${JVM_ARGS}`;
const APP_MAIN_CLASS = "io.appthreat.atom.Atom";
let APP_CLASSPATH = join(APP_LIB_DIR, "io.appthreat.atom-1.0.0-classpath.jar");
let JAVACMD = "java";
if (process.env.JAVA_HOME) {
  JAVACMD = join(process.env.JAVA_HOME, "bin", "java" + (isWin ? ".exe" : ""));
}

const atomLibs = [APP_CLASSPATH];
const argv = process.argv.slice(2);

const executeAtom = (atomArgs) => {
  let args = JAVA_OPTS.trim()
    .split(" ")
    .concat([
      "-cp",
      atomLibs.join(delimiter),
      `-Dlog4j.configurationFile=${LOG4J_CONFIG}`,
      APP_MAIN_CLASS,
      ...atomArgs
    ]);
  const env = {
    ...process.env,
    ATOM_HOME
  };
  const cwd = process.env.ATOM_CWD || process.cwd();
  spawnSync(JAVACMD, args, {
    encoding: "utf-8",
    env,
    cwd,
    stdio: "inherit",
    stderr: "inherit",
    timeout: process.env.ATOM_TIMEOUT
  });
};
executeAtom(argv);
