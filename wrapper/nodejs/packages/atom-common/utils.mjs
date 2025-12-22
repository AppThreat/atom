import { join, basename } from "node:path";
import { readdirSync, statSync } from "node:fs";
import { spawnSync } from "node:child_process";

const IGNORE_DIRS = process.env.ASTGEN_IGNORE_DIRS
  ? process.env.ASTGEN_IGNORE_DIRS.split(",")
  : [
      "venv",
      "docs",
      "e2e",
      "e2e-beta",
      "examples",
      "cypress",
      "jest-cache",
      "eslint-rules",
      "codemods",
      "flow-typed",
      "i18n"
    ];

const IGNORE_FILE_PATTERN = new RegExp(
  process.env.ASTGEN_IGNORE_FILE_PATTERN || "(three|\\.d)\\.(js|ts|jsx|tsx)$",
  "i"
);

export const getAllFiles = (
  dir,
  extn,
  files,
  result,
  regex,
  ignore_node_modules = true
) => {
  files = files || readdirSync(dir);
  result = result || [];
  regex = regex || new RegExp(`\\${extn}$`);

  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    if (
      (ignore_node_modules && file.startsWith(".")) ||
      IGNORE_FILE_PATTERN.test(file)
    ) {
      continue;
    }
    const fileWithDir = join(dir, file);
    if (statSync(fileWithDir).isDirectory()) {
      // Ignore directories
      const dirName = basename(fileWithDir);
      if (
        (ignore_node_modules && dirName.startsWith(".")) ||
        IGNORE_DIRS.includes(dirName.toLowerCase()) ||
        (ignore_node_modules && dirName.toLowerCase() === "node_modules")
      ) {
        continue;
      }
      try {
        result = getAllFiles(
          fileWithDir,
          extn,
          readdirSync(fileWithDir),
          result,
          regex,
          ignore_node_modules
        );
      } catch (error) {
        // ignore
      }
    } else {
      if (
        regex.test(fileWithDir) ||
        (extn &&
          !extn.includes(".") &&
          fileWithDir.toLowerCase().endsWith(extn.toLowerCase()))
      ) {
        result.push(fileWithDir);
      }
    }
  }
  return result;
};

export const detectJava = () => {
  let result = spawnSync(process.env.JAVA_CMD || "java", ["-version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  return true;
};

export const detectPhp = () => {
  let result = spawnSync(process.env.PHP_CMD || "php", ["--version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  return true;
};

export const detectRuby = (versionNeeded) => {
  let result = spawnSync(process.env.RUBY_CMD || "ruby", ["--version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  const stdout = result.stdout;
  if (versionNeeded && stdout) {
    versionNeeded = versionNeeded.replaceAll(".x", ".");
    const cmdOutput = Buffer.from(stdout).toString();
    const versionStr = cmdOutput.trim().replaceAll("\r", "");
    return versionStr.startsWith(`ruby ${versionNeeded}`);
  }
  return true;
};

export const detectScala = () => {
  let result = spawnSync(process.env.SCALA_CMD || "scala", ["--version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  return true;
};

export const detectScalac = () => {
  let result = spawnSync(process.env.SCALAC_CMD || "scalac", ["--version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  return true;
};
