#!/usr/bin/env node

import { freemem, platform as _platform } from "node:os";
import { dirname, join, delimiter } from "node:path";
import { readFileSync } from "node:fs";

import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { detectJava } from "./utils.mjs";

const isWin = _platform() === "win32";
let url = import.meta.url;
if (!url.startsWith("file://")) {
  url = new URL(`file://${import.meta.url}`).toString();
}
const dirName = import.meta ? dirname(fileURLToPath(url)) : __dirname;
const selfPJson = JSON.parse(readFileSync(join(dirName, "package.json")));
const _version = selfPJson.version;
export const LOG4J_CONFIG = join(dirName, "plugins", "log4j2.xml");
export const ATOM_HOME = join(dirName, "plugins");
export const APP_LIB_DIR = join(ATOM_HOME, "lib");
export const PHP_PARSER_BIN = join(ATOM_HOME, "bin", "php-parse");
const freeMemoryGB = Math.max(Math.floor(freemem() / 1024 / 1024 / 1024), 4);
export const JVM_ARGS = "-XX:+UseG1GC -XX:+UseStringDeduplication";
export const JAVA_OPTS = `${
  process.env.JAVA_OPTS || ""
} -Xmx${freeMemoryGB}G ${JVM_ARGS}`;
export const APP_MAIN_CLASS = "io.appthreat.atom.Atom";
export const ATOM_VERSION = _version;
export const APP_CLASSPATH = join(
  APP_LIB_DIR,
  `io.appthreat.atom-${ATOM_VERSION}-classpath.jar`
);
let JAVACMD = "java";
if (process.env.JAVA_HOME) {
  JAVACMD = join(process.env.JAVA_HOME, "bin", "java" + (isWin ? ".exe" : ""));
}

const atomLibs = [APP_CLASSPATH];
const argv = process.argv.slice(2);

export const executeAtom = (atomArgs) => {
  if (!detectJava()) {
    // If we couldn't detect java but there is a JAVA_HOME defined then
    // try fixing the PATH manually. Usually required for windows users
    if (process.env.JAVA_HOME) {
      process.env.PATH =
        process.env.PATH +
        delimiter +
        join(process.env.JAVA_HOME, "bin") +
        delimiter;
    } else {
      console.warn(
        "A Java JDK is not installed or can't be found. Please install JDK version 17 or higher before running atom."
      );
      return false;
    }
  }
  let args = JAVA_OPTS.trim()
    .split(" ")
    .concat([
      "-cp",
      atomLibs.join(delimiter),
      `-Dlog4j2.configurationFile=${LOG4J_CONFIG}`,
      APP_MAIN_CLASS,
      ...atomArgs
    ]);
  const env = {
    ...process.env,
    ATOM_HOME,
    PHP_PARSER_BIN
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
