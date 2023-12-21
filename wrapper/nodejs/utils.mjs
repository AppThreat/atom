import { join, basename } from "node:path";
import { readdirSync, statSync } from "node:fs";
import { spawnSync } from "node:child_process";

const IGNORE_DIRS = process.env.ASTGEN_IGNORE_DIRS
  ? process.env.ASTGEN_IGNORE_DIRS.split(",")
  : [
      "node_modules",
      "venv",
      "docs",
      "test",
      "tests",
      "e2e",
      "e2e-beta",
      "examples",
      "cypress",
      "jest-cache",
      "eslint-rules",
      "codemods",
      "flow-typed",
      "i18n",
      "vendor",
      "www",
      "dist",
      "build",
      "__tests__"
    ];

const IGNORE_FILE_PATTERN = new RegExp(
  process.env.ASTGEN_IGNORE_FILE_PATTERN ||
    "(conf|config|test|spec|min|three|\\.d)\\.(js|ts|jsx|tsx)$",
  "i"
);

export const getAllFiles = (dir, extn, files, result, regex) => {
  files = files || readdirSync(dir);
  result = result || [];
  regex = regex || new RegExp(`\\${extn}$`);

  for (let i = 0; i < files.length; i++) {
    const file = files[i];
    if (
      file.startsWith(".") ||
      file.startsWith("__") ||
      IGNORE_FILE_PATTERN.test(file)
    ) {
      continue;
    }
    const fileWithDir = join(dir, file);
    if (statSync(fileWithDir).isDirectory()) {
      // Ignore directories
      const dirName = basename(fileWithDir);
      if (
        dirName.startsWith(".") ||
        dirName.startsWith("__") ||
        IGNORE_DIRS.includes(dirName.toLowerCase())
      ) {
        continue;
      }
      try {
        result = getAllFiles(
          fileWithDir,
          extn,
          readdirSync(fileWithDir),
          result,
          regex
        );
      } catch (error) {
        // ignore
      }
    } else {
      if (regex.test(fileWithDir)) {
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
  let result = spawnSync(process.env.JAVA_CMD || "php", ["--version"], {
    encoding: "utf-8"
  });
  if (result.status !== 0 || result.error) {
    return false;
  }
  return true;
};
