#!/usr/bin/env node

import { platform as _platform } from "node:os";
import { dirname, join, delimiter } from "node:path";
import { readFileSync } from "node:fs";

import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { detectJava } from "@appthreat/atom-common";

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

export const JVM_ARGS = "-XX:MinRAMPercentage=30 -XX:MaxRAMPercentage=90";
export const JAVA_OPTS =
  process.env?.ATOM_JVM_ARGS || `${process.env?.JAVA_OPTS || ""} ${JVM_ARGS}`;

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
    const atomJavaHome = process.env.ATOM_JAVA_HOME || process.env.JAVA_HOME;
    if (atomJavaHome) {
      process.env.PATH =
        join(atomJavaHome, "bin") + delimiter + process.env.PATH + delimiter;
    } else {
      console.warn(
        "A Java JDK is not installed or can't be found. Please install JDK version 21 or higher before running atom."
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
