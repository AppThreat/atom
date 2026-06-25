#!/usr/bin/env node

import { platform as _platform } from "node:os";
import { dirname, join, delimiter } from "node:path";
import { readFileSync, realpathSync } from "node:fs";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { locateAtomBinary } from "./resolve.js";

const isWin = _platform() === "win32";
const dirName = dirname(fileURLToPath(import.meta.url));
const selfPJson = JSON.parse(readFileSync(join(dirName, "package.json"), "utf8"));
export const ATOM_VERSION = selfPJson.version;

export const JVM_ARGS = "-XX:MinRAMPercentage=30 -XX:MaxRAMPercentage=90";
export const JAVA_OPTS =
  process.env?.ATOM_JVM_ARGS || `${process.env?.JAVA_OPTS || ""} ${JVM_ARGS}`;
export const APP_MAIN_CLASS = "io.appthreat.atom.Atom";

export let LOG4J_CONFIG;
export let ATOM_HOME;
export let APP_LIB_DIR;
export let PHP_PARSER_BIN;
export let APP_CLASSPATH;

const provider = locateAtomBinary();

if (provider) {
  if (provider.kind === "native") {
    ATOM_HOME = dirname(dirname(provider.binPath));
    PHP_PARSER_BIN = join(ATOM_HOME, "bin", "php-parse");
  } else {
    ATOM_HOME = provider.pluginsDir;
    APP_LIB_DIR = join(ATOM_HOME, "lib");
    PHP_PARSER_BIN = join(ATOM_HOME, "bin", "php-parse");
    LOG4J_CONFIG = join(ATOM_HOME, "log4j2.xml");
    APP_CLASSPATH = join(
      APP_LIB_DIR,
      `io.appthreat.atom-${ATOM_VERSION}-classpath.jar`
    );
  }
}

function detectJava() {
  const result = spawnSync(process.env.JAVA_CMD || "java", ["-version"], {
    encoding: "utf-8"
  });
  return result.status === 0 && !result.error;
}

export const executeAtom = (atomArgs) => {
  if (!provider) {
    console.error("Error: The '@appthreat/atom' package was not installed correctly or is unsupported on this platform.");
    console.error("Please verify your installation and make sure optional dependencies are not blocked.");
    process.exit(1);
  }

  const cwd = process.env.ATOM_CWD || process.cwd();
  const timeout = process.env.ATOM_TIMEOUT ? parseInt(process.env.ATOM_TIMEOUT, 10) : undefined;

  if (provider.kind === "native") {
    const env = {
      ...process.env,
      ATOM_HOME,
      PHP_PARSER_BIN
    };
    const result = spawnSync(provider.binPath, atomArgs, {
      encoding: "utf-8",
      env,
      cwd,
      stdio: "inherit",
      timeout
    });
    process.exit(result.status !== null ? result.status : 1);
  } else {
    if (!detectJava()) {
      const atomJavaHome = process.env.ATOM_JAVA_HOME || process.env.JAVA_HOME;
      if (atomJavaHome) {
        process.env.PATH =
          join(atomJavaHome, "bin") + delimiter + process.env.PATH + delimiter;
      } else {
        console.warn(
          "A Java JDK is not installed or can't be found. Please install JDK version 21 or higher before running atom."
        );
        process.exit(1);
      }
    }

    let JAVACMD = "java";
    if (process.env.JAVA_HOME) {
      JAVACMD = join(process.env.JAVA_HOME, "bin", "java" + (isWin ? ".exe" : ""));
    }

    const atomLibs = [APP_CLASSPATH];
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

    const result = spawnSync(JAVACMD, args, {
      encoding: "utf-8",
      env,
      cwd,
      stdio: "inherit",
      timeout
    });
    process.exit(result.status !== null ? result.status : 1);
  }
};

let isMain = false;
if (process.argv[1]) {
  try {
    const mainPath = realpathSync(process.argv[1]);
    const thisPath = realpathSync(fileURLToPath(import.meta.url));
    const parentIndex = realpathSync(join(dirName, "../../index.js"));
    isMain = mainPath === thisPath || mainPath === parentIndex;
  } catch (e) {
    // Ignore realpathSync errors
  }
}

if (isMain) {
  const argv = process.argv.slice(2);
  executeAtom(argv);
}
