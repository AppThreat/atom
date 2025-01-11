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

function main(argvs) {
  if (!detectRuby()) {
    console.warn("Ruby is not installed!");
    return false;
  }
  const cwd = process.env.ATOM_CWD || process.cwd();
  argvs.splice(
    0,
    0,
    process.env.RUBY_ASTGEN_BIN || join(PLUGINS_HOME, "bin", "ruby_ast_gen")
  );
  const env = {
    ...process.env
  };
  if (
    process.env.ATOM_RUBY_HOME &&
    existsSync(join(process.env.ATOM_RUBY_HOME, "bin"))
  ) {
    const rubyBinDir = join(process.env.ATOM_RUBY_HOME, "bin");
    if (!env.PATH.includes(rubyBinDir)) {
      env.PATH = `${rubyBinDir}${delimiter}${env.PATH}`;
    }
  }
  spawnSync(process.env.RUBY_CMD || "ruby", argvs, {
    encoding: "utf-8",
    cwd,
    stdio: "inherit",
    stderr: "inherit",
    env,
    timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT
  });
}
main(process.argv.slice(2));
