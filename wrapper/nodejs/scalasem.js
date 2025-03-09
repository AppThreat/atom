#!/usr/bin/env node
// Usage: scalasem $(pwd) slices.json
import { tmpdir } from "node:os";
import { basename, dirname, join, relative } from "node:path";
import { spawnSync } from "node:child_process";
import { detectScala, detectScalac, getAllFiles } from "./utils.mjs";
import process from "node:process";
import {
  existsSync,
  mkdtempSync,
  mkdirSync,
  readFileSync,
  rmSync,
  writeFileSync
} from "node:fs";

function main(argvs) {
  if (!detectScala() && !detectScalac()) {
    console.warn("Scala is not installed!");
    return false;
  }
  let configFiles = getAllFiles(argvs[0], "routes");
  configFiles = configFiles.concat(getAllFiles(argvs[0], ".conf"));
  let tastyFiles = getAllFiles(argvs[0], ".tasty");
  if (!tastyFiles.length) {
    let buildTool = "sbt";
    const millFiles = getAllFiles(argvs[0], "build.mill");
    if (millFiles.length) {
      buildTool = "mill";
    }
    const cwd = process.env.ATOM_CWD || process.cwd();
    let compileCommand =
      process?.env[`${buildTool.toUpperCase()}_COMPILE_COMMAND`] || "compile";
    if (process.env.SCALA_VERSION && buildTool === "sbt") {
      compileCommand = `++${process.env.SCALA_VERSION} ${compileCommand}`;
    } else {
      // Detect crossScalaVersions
      const scalaVersion = findScalaVersion(cwd);
      if (scalaVersion) {
        compileCommand = `++${scalaVersion} ${compileCommand}`;
      }
    }
    console.log(`Executing '${buildTool} ${compileCommand}' in ${argvs[0]}`);
    const result = spawnSync(buildTool, compileCommand.split(" "), {
      encoding: "utf-8",
      cwd,
      stdio: "ignore",
      stderr: "inherit",
      env: process.env,
      timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT
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
  const slicesFile =
    argvs.length > 1 ? argvs[1] : join(argvs[0], "slices.json");
  createSemanticSlices(tastyFiles, configFiles, slicesFile);
}
main(process.argv.slice(2));

function findScalaVersion(cwd) {
  let scalaVersion;
  const buildSbtFile = join(cwd, "build.sbt");
  if (existsSync(buildSbtFile)) {
    const buildData = readFileSync(buildSbtFile, "utf-8");
    for (let line of buildData.split("\n")) {
      if (line.trim().includes("val ") && line.includes("scala")) {
        const match = line.match(/"(3\.[^"]+)"/);
        if (match) {
          return match[1];
        }
      }
      if (line.trim().includes("crossScalaVersions")) {
        const crossVersions = line.split("crossScalaVersions").pop().trim();
        if (crossVersions.includes("3.")) {
          const match = crossVersions.match(/"(3\.[^"]+)"/);
          if (match) {
            return match[1];
          }
        }
      }
    }
  }
  return scalaVersion;
}

function createSemanticSlices(tastyFiles, configFiles, slicesFile) {
  const outDir = mkdtempSync(join(tmpdir(), "scalasem-"));
  const MAX_BUFFER =
    Number.parseInt(process.env.ATOM_MAX_BUFFER) || 100 * 1024 * 1024;
  const cwd = process.env.ATOM_CWD || process.cwd();
  const slices = {};
  slices.config = parseConfigFiles(configFiles);
  for (const af of tastyFiles) {
    const result = spawnSync(
      process.env.SCALAC_CMD || "scalac",
      ["-color:never", "-print-tasty", af],
      {
        encoding: "utf-8",
        cwd,
        env: process.env,
        maxBuffer: MAX_BUFFER,
        timeout: process.env.ATOM_TIMEOUT || process.env.ASTGEN_TIMEOUT
      }
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
        ""
      );
      if (fileOutDir.includes("classes")) {
        fileOutDir = fileOutDir.replace(
          new RegExp("target/scala-(.)*/classes"),
          ""
        );
      }
      mkdirSync(fileOutDir, { recursive: true });
      const astFile = join(
        fileOutDir,
        basename(af).replace(".tasty", ".scala.ast")
      );
      const scalaFile = join(
        scalaDir,
        basename(af).replace(".tasty", ".scala")
      );
      writeFileSync(astFile, Buffer.from(result.stdout).toString());
      const usages = parseTasty(astFile);
      slices[usages.sourceFile || scalaFile] = usages;
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
      } entries.`
    );
  }
  if (outDir?.startsWith(tmpdir())) {
    rmSync(outDir, { recursive: true });
  }
}

function parseTasty(tastyAstFile) {
  const astData = readFileSync(tastyAstFile, "utf-8");
  let namesMode = false;
  let treesMode = false;
  let sourcePathsMode = false;
  const literals = new Set();
  const usedTypes = new Set();
  const tags = new Set();
  let sourceFile;
  for (let line of astData.split("\n")) {
    line = line.replace("\r", "").trim();
    if (!line.length || line.startsWith("---")) {
      continue;
    }
    if (line.startsWith("Names ") || line.startsWith("Names:")) {
      namesMode = true;
    }
    if (line.startsWith("Trees ") || line.startsWith("Trees:")) {
      namesMode = false;
      treesMode = true;
    }
    if (line.startsWith("Positions ") || line.startsWith("positions:")) {
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
            tags.add("framework");
          }
          if (
            sig.startsWith("play.api.data.Form") ||
            sig.startsWith("play.api.mvc.Request") ||
            sig.startsWith("play.twirl.api")
          ) {
            tags.add("framework-input");
          }
          if (
            sig.startsWith("play.twirl.api.Html") ||
            sig.startsWith("play.api.mvc.Result") ||
            sig.startsWith("play.api.mvc.Action")
          ) {
            tags.add("framework-output");
          }
          if (
            sig.startsWith("play.api.routing.") ||
            sig.startsWith("play.core.routing") ||
            sig.startsWith("router.RoutesPrefix")
          ) {
            tags.add("framework-route");
          }
          if (
            sig.startsWith("slick.sql.") ||
            sig.startsWith("play.db.") ||
            sig.startsWith("slick.jdbc.")
          ) {
            tags.add("database");
          }
        }
      }
    }
    if (line.includes("source paths:")) {
      sourcePathsMode = true;
    }
    if (sourcePathsMode) {
      if (line.includes(" [") && line.endsWith("]")) {
        sourceFile = line.split(" [").pop().replace(/]/g, "");
        sourcePathsMode = false;
      } else if (line.includes(".scala") && line.includes(": ")) {
        sourceFile = line.split(": ").pop().trim();
        sourcePathsMode = false;
      }
    }
    if (!namesMode && !treesMode && !sourcePathsMode) {
      continue;
    }
  }
  if (sourceFile?.includes("target")) {
    tags.add("generated");
  }
  return {
    sourceFile,
    tags: Array.from(tags).sort(),
    usedTypes: Array.from(usedTypes).sort(),
    literals: Array.from(literals)
  };
}

function parseConfigFiles(configFiles) {
  const configMetadata = { routes: [] };
  for (const aconfig of configFiles) {
    if (aconfig.endsWith("routes")) {
      const routes = parseRoutes(aconfig);
      if (routes?.length) {
        for (const aroute of routes) {
          let duplicate = false;
          for (const exisRoute of configMetadata.routes) {
            if (
              exisRoute.method === aroute.method &&
              exisRoute.pattern === aroute.pattern
            ) {
              if (
                exisRoute.controllerMethod &&
                exisRoute.controllerMethod === aroute.controllerMethod
              ) {
                duplicate = true;
                continue;
              }
            }
          }
          if (!duplicate) {
            configMetadata["routes"].push(aroute);
          }
        }
      }
    }
  }
  if (configMetadata.routes.length) {
    console.log("Found", configMetadata.routes.length, "routes.");
  }
  return configMetadata;
}

function parseRoutes(routesFile) {
  const routes = [];
  const routesData = readFileSync(routesFile, "utf-8");
  for (let aline of routesData.split("\n")) {
    aline = aline.replace("\r", "").trim();
    if (aline.startsWith("#") || aline.startsWith("+")) {
      continue;
    }
    const tmpA = aline.split(/\s+/);
    if (tmpA.length < 2) {
      continue;
    }
    // Ignore static assets
    if (["/webjars"].includes(tmpA[1])) {
      continue;
    }
    if (
      [
        "GET",
        "PATCH",
        "POST",
        "OPTIONS",
        "HEAD",
        "DELETE",
        "PUT",
        "->"
      ].includes(tmpA[0].toUpperCase())
    ) {
      let controllerMethod = tmpA.length > 2 ? tmpA[2] : undefined;
      if (controllerMethod.includes("(")) {
        controllerMethod = controllerMethod.split("(")[0];
      }
      // Exclude webjars
      if (controllerMethod.startsWith("webjars.")) {
        continue;
      }
      // Handle wildcards
      if (tmpA[0] === "->") {
        // We now need to parse a method called "routes" in the controllerMethod to identify the list of http methods
        // Let's keep things simple for now
        for (const m of ["GET", "PATCH", "POST", "DELETE", "PUT"]) {
          routes.push({
            method: m,
            pattern: tmpA[1],
            controllerMethod
          });
        }
      } else {
        routes.push({
          method: tmpA[0],
          pattern: tmpA[1],
          controllerMethod
        });
      }
    }
  }
  return routes;
}
