#!/usr/bin/env node

import { existsSync } from "node:fs";
import { dirname, join, delimiter } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { detectRuby } from "./utils.mjs";

let url = import.meta.url;
if (!url.startsWith("file://")) {
  url = new URL(`file://${import.meta.url}`).toString();
}
const dirName = import.meta ? dirname(fileURLToPath(url)) : __dirname;
export const PLUGINS_HOME = join(dirName, "plugins");
const RUBY_ASTGEN_BIN =
  process.env.RUBY_ASTGEN_BIN || join(PLUGINS_HOME, "bin", "ruby_ast_gen");
// Ruby version needed
const RUBY_VERSION_NEEDED = "3.4.2";
function main(argvs) {
  const cwd = process.env.ATOM_CWD || process.cwd();
  argvs.splice(
    0,
    0,
    process.env.RUBY_ASTGEN_BIN || join(PLUGINS_HOME, "bin", "ruby_ast_gen")
  );
  const env = {
    ...process.env
  };
  let rubyCmd = process.env.RUBY_CMD || "ruby";
  if (
    process.env.ATOM_RUBY_HOME &&
    existsSync(join(process.env.ATOM_RUBY_HOME, "bin"))
  ) {
    const rubyBinDir = join(process.env.ATOM_RUBY_HOME, "bin");
    if (rubyCmd === "ruby") {
      rubyCmd = join(rubyBinDir, "ruby");
    }
    if (!env.PATH.includes(rubyBinDir)) {
      env.PATH = `${rubyBinDir}${delimiter}${env.PATH}`;
    }
  }
  if (rubyCmd === "ruby" && !detectRuby(RUBY_VERSION_NEEDED)) {
    console.warn(
      `Ruby ${RUBY_VERSION_NEEDED} is not installed! Set the environment variable "ATOM_RUBY_HOME" to the Ruby ${RUBY_VERSION_NEEDED} install directory.`
    );
    return false;
  }
  spawnSync(rubyCmd, argvs, {
    encoding: "utf-8",
    cwd,
    stdio: "inherit",
    stderr: "inherit",
    env,
    timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT
  });
}
main(process.argv.slice(2));
