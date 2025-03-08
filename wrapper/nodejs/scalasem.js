#!/usr/bin/env node

import { basename, dirname, join, relative } from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { detectScala, detectScalac, getAllFiles } from "./utils.mjs";
import process from "node:process";
import { mkdirSync, readFileSync, rmSync, writeFileSync } from "node:fs";

let url = import.meta.url;
if (!url.startsWith("file://")) {
  url = new URL(`file://${import.meta.url}`).toString();
}
const dirName = import.meta ? dirname(fileURLToPath(url)) : __dirname;

function main(argvs) {
  if (!detectScala()) {
    console.warn("Scala is not installed!");
    return false;
  }
  if (!detectScalac()) {
    console.warn("Scalac is not installed!");
    return false;
  }
  let tastyFiles = getAllFiles(argvs[0], ".tasty");
  if (!tastyFiles.length) {
    let buildTool = "sbt";
    const millFiles = getAllFiles(argvs[0], "build.mill");
    if (millFiles.length) {
      buildTool = "mill";
    }
    const cwd = process.env.ATOM_CWD || process.cwd();
    const compileCommand =
      process?.env[`${buildTool.toUpperCase()}_COMPILE_COMMAND`] || "compile";
    console.log(`Executing '${buildTool} ${compileCommand}' in ${argvs[0]}`);
    const result = spawnSync(buildTool, compileCommand.split(" "), {
      encoding: "utf-8",
      cwd,
      stdio: "ignore",
      stderr: "inherit",
      env: process.env,
      timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT,
    });
    if (result.error || result.status !== 0) {
      if (result.stderr) {
        console.log(result.stderr);
      }
      return false;
    }
    tastyFiles = getAllFiles(argvs[0], ".tasty");
    console.log(`Obtained ${tastyFiles.length} IR files after compilation.`);
  }
  const outDir = argvs.length > 1 ? argvs[1] : join(argvs[0], "ir_out");
  dumpTastyFiles(
    tastyFiles,
    outDir,
    argvs.length > 2 ? join(outDir, argvs[2]) : join(outDir, "slices.json"),
  );
}
main(process.argv.slice(2));

function dumpTastyFiles(tastyFiles, outDir, slicesFile) {
  const MAX_BUFFER =
    Number.parseInt(process.env.ATOM_MAX_BUFFER) || 100 * 1024 * 1024;
  const cwd = process.env.ATOM_CWD || process.cwd();
  const slices = {};
  for (const af of tastyFiles) {
    const result = spawnSync(
      process.env.SCALAC_CMD || "scalac",
      ["-color:never", "-print-tasty", af],
      {
        encoding: "utf-8",
        cwd,
        env: process.env,
        maxBuffer: MAX_BUFFER,
        timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT,
      },
    );
    if (result.error || result.status !== 0) {
      if (result.stderr) {
        console.log(result.stderr);
      }
    }
    if (result.stdout) {
      let fileOutDir = join(outDir, relative(cwd, dirname(af)));
      const scalaDir = relative(cwd, dirname(af)).replace(
        new RegExp("target/scala-(.)*/classes"),
        "",
      );
      if (fileOutDir.includes("classes")) {
        fileOutDir = fileOutDir.replace(
          new RegExp("target/scala-(.)*/classes"),
          "",
        );
      }
      mkdirSync(fileOutDir, { recursive: true });
      const astFile = join(
        fileOutDir,
        basename(af).replace(".tasty", ".scala.ast"),
      );
      const scalaFile = join(
        scalaDir,
        basename(af).replace(".tasty", ".scala"),
      );
      writeFileSync(astFile, Buffer.from(result.stdout).toString());
      const usages = parseTasty(astFile);
      slices[scalaFile] = usages;
      rmSync(astFile);
    }
  }
  const slicesJson = JSON.stringify(slices, null, null);
  writeFileSync(slicesFile, slicesJson);
  if (!Object.keys(slices).length) {
    console.log("Empty slices file created.");
  } else {
    console.log(
      `Slices file ${slicesFile} created successfully with ${
        Object.keys(slices).length
      } entries.`,
    );
  }
}

function parseTasty(tastyAstFile) {
  const astData = readFileSync(tastyAstFile, "utf-8");
  let namesMode = false;
  let treesMode = false;
  const literals = new Set();
  const usedTypes = new Set();
  const tags = [];
  for (let line of astData.split("\n")) {
    line = line.replace("\r", "");
    if (!line.length || line.startsWith("---")) {
      continue;
    }
    if (line.startsWith("Names ")) {
      namesMode = true;
    }
    if (line.startsWith("Trees ")) {
      namesMode = false;
      treesMode = true;
    }
    if (line.startsWith("Positions ")) {
      namesMode = false;
      treesMode = false;
    }
    if (namesMode) {
      // 3: api
      if (line.includes(": ")) {
        const literal = line.split(": ").pop().trim();
        if (literal.length > 1) {
          literals.add(literal);
        }
      }
    }
    if (treesMode && line.includes(" Signature(")) {
      // 139:         SELECTin(12) 38 [<init>[Signed Signature(List(play.api.mvc.MessagesControllerComponents),play.api.mvc.MessagesAbstractController) @<init>]]
      const signatureTypes = line
        .split(" Signature(")
        .pop()
        .split(") ")[0]
        .replaceAll("List(", "")
        .replaceAll(")", "")
        .split(",");
      for (let sig of signatureTypes) {
        sig = sig.trim();
        if (
          sig.length > 3 &&
          !sig.startsWith("scala.") &&
          !sig.startsWith("java.") &&
          !sig.startsWith("javax.inject.")
        ) {
          usedTypes.add(sig);
          if (sig.startsWith("play.api.")) {
            tags.push("framework");
          }
          if (
            sig.startsWith("play.api.data.Form") ||
            sig.startsWith("play.api.mvc.Request") ||
            sig.startsWith("play.twirl.api")
          ) {
            tags.push("framework-input");
          }
          if (
            sig.startsWith("play.twirl.api.Html") ||
            sig.startsWith("play.api.mvc.Result") ||
            sig.startsWith("play.api.mvc.Action")
          ) {
            tags.push("framework-output");
          }
          if (
            sig.startsWith("play.api.routing.") ||
            sig.startsWith("play.core.routing") ||
            sig.startsWith("router.RoutesPrefix")
          ) {
            tags.push("framework-route");
          }
          if (
            sig.startsWith("slick.sql.") ||
            sig.startsWith("play.db.") ||
            sig.startsWith("slick.jdbc.")
          ) {
            tags.push("database");
          }
        }
      }
    }
    if (!namesMode && !treesMode) {
      continue;
    }
  }
  return {
    tags,
    usedTypes: Array.from(usedTypes),
    literals: Array.from(literals),
  };
}
