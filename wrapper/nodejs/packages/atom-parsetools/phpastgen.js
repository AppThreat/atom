#!/usr/bin/env node

import { existsSync } from "node:fs";
import { dirname, join } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { detectPhp } from "@appthreat/atom-common";

let url = import.meta.url;
if (!url.startsWith("file://")) {
  url = new URL(`file://${import.meta.url}`).toString();
}
const dirName = import.meta ? dirname(fileURLToPath(url)) : __dirname;
export const PLUGINS_HOME = join(dirName, "plugins");
export const PARENT_NODE_PLUGINS_HOME = join(
  dirName,
  "..",
  "..",
  "node_modules",
  "@appthreat",
  "atom-parsetools",
  "plugins"
);
let PHP_PARSER_BIN =
  process.env.PHP_PARSER_BIN || join(PLUGINS_HOME, "bin", "php-parse");
if (
  !existsSync(PHP_PARSER_BIN) &&
  existsSync(join(PARENT_NODE_PLUGINS_HOME, "bin", "php-parse"))
) {
  PHP_PARSER_BIN = join(PARENT_NODE_PLUGINS_HOME, "bin", "php-parse");
}
function main(argvs) {
  if (!detectPhp()) {
    console.warn("PHP is not installed!");
    return false;
  }
  const cwd = process.env.ATOM_CWD || process.cwd();
  argvs.splice(0, 1, PHP_PARSER_BIN);
  spawnSync(process.env.PHP_CMD || "php", argvs, {
    encoding: "utf-8",
    cwd,
    stdio: "inherit",
    stderr: "inherit",
    env: process.env,
    timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT
  });
}
main(process.argv.slice(2));
