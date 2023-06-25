import { join, basename } from "node:path";
import { readdirSync, statSync } from "node:fs";

const IGNORE_DIRS = [
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
  "build"
];

const IGNORE_FILE_PATTERN = new RegExp(
  "(conf|test|spec|\\.d)\\.(js|ts|jsx|tsx)$",
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
