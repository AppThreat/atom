#!/usr/bin/env node

import { join, dirname } from "path";
import yargs from "yargs";
import { hideBin } from "yargs/helpers";
import { parse } from "@babel/parser";
import tsc from "typescript";
import {
  readFileSync,
  mkdirSync,
  writeFileSync,
  accessSync,
  constants,
  existsSync
} from "fs";
import { getAllFiles } from "@appthreat/atom-common";

const ASTGEN_VERSION = "4.0.0";

const babelParserOptions = {
  sourceType: "unambiguous",
  allowImportExportEverywhere: true,
  allowAwaitOutsideFunction: true,
  allowNewTargetOutsideFunction: true,
  allowReturnOutsideFunction: true,
  allowSuperOutsideMethod: true,
  allowUndeclaredExports: true,
  errorRecovery: true,
  plugins: [
    "optionalChaining",
    "classProperties",
    "decorators-legacy",
    "exportDefaultFrom",
    "doExpressions",
    "numericSeparator",
    "dynamicImport",
    "jsx",
    "typescript"
  ]
};

const babelFlowParserOptions = {
  sourceType: "unambiguous",
  allowImportExportEverywhere: true,
  allowAwaitOutsideFunction: true,
  allowNewTargetOutsideFunction: true,
  allowReturnOutsideFunction: true,
  allowSuperOutsideMethod: true,
  allowUndeclaredExports: true,
  errorRecovery: true,
  plugins: [
    "optionalChaining",
    "classProperties",
    "decorators-legacy",
    "exportDefaultFrom",
    "doExpressions",
    "numericSeparator",
    "dynamicImport",
    "jsx",
    "flow"
  ]
};

const babelSafeParserOptions = {
  sourceType: "module",
  allowImportExportEverywhere: true,
  allowAwaitOutsideFunction: true,
  allowReturnOutsideFunction: true,
  errorRecovery: true,
  plugins: [
    "optionalChaining",
    "classProperties",
    "decorators-legacy",
    "exportDefaultFrom",
    "doExpressions",
    "numericSeparator",
    "dynamicImport",
    "typescript"
  ]
};

const babelSafeFlowParserOptions = {
  sourceType: "module",
  allowImportExportEverywhere: true,
  allowAwaitOutsideFunction: true,
  allowReturnOutsideFunction: true,
  errorRecovery: true,
  plugins: [
    "optionalChaining",
    "classProperties",
    "decorators-legacy",
    "exportDefaultFrom",
    "doExpressions",
    "numericSeparator",
    "dynamicImport",
    "typescript"
  ]
};

/**
 * Return paths to all (j|tsx?) files.
 * Optionally includes specific bundled files from node_modules if:
 * 1. ASTGEN_INCLUDE_NODE_MODULES_BUNDLES is "true", OR
 * 2. ASTGEN_IGNORE_DIRS is non-empty and doesn't include "node_modules"
 */
const getAllSrcJSAndTSFiles = (src) => {
  const filePattern = "\\.(js|jsx|cjs|mjs|ts|tsx|vue|svelte|xsjs|xsjslib|ejs)$";
  const bundledNodeModulesPattern =
    "node_modules/.*\\.(bundle|dist|index|min|app)\\.(js|cjs|mjs)$";

  // Step 1: Collect all JS/TS files EXCLUDING node_modules
  const allFilesPromise = Promise.resolve(
    getAllFiles(
      src,
      undefined,
      undefined,
      undefined,
      new RegExp(filePattern),
      true // ignore node_modules
    )
  );

  // Step 2: Check if we should include node_modules bundles
  const shouldIncludeNodeModulesBundles =
    process.env?.ASTGEN_INCLUDE_NODE_MODULES_BUNDLES === "true" ||
    (process.env?.ASTGEN_IGNORE_DIRS &&
      process.env.ASTGEN_IGNORE_DIRS.length > 0 &&
      !process.env.ASTGEN_IGNORE_DIRS.toLowerCase().includes("node_modules"));

  let bundledFilesPromise = Promise.resolve([]);
  if (shouldIncludeNodeModulesBundles) {
    bundledFilesPromise = Promise.resolve(
      getAllFiles(
        src,
        undefined,
        undefined,
        undefined,
        new RegExp(bundledNodeModulesPattern),
        false // DO NOT ignore node_modules
      )
    );
  }

  // Step 3: Combine both lists
  return Promise.all([allFilesPromise, bundledFilesPromise]).then(
    ([allFiles, bundledFiles]) => [...allFiles, ...bundledFiles]
  );
};

/**
 * Convert a single JS/TS file to AST
 */
const fileToJsAst = (file, projectType) => {
  if (file.endsWith(".vue") || file.endsWith(".svelte")) {
    return toVueAst(file);
  }
  return codeToJsAst(readFileSync(file, "utf-8"), projectType);
};

/**
 * Convert a single JS/TS code snippet to AST
 */
const codeToJsAst = (code, projectType) => {
  const optionsToUse =
    projectType === "flow" ? babelFlowParserOptions : babelParserOptions;
  try {
    return parse(code, optionsToUse);
  } catch {
    try {
      return parse(code, babelSafeParserOptions);
    } catch (errFlow) {
      try {
        return parse(code, babelFlowParserOptions);
      } catch (errFlow) {
        return parse(code, babelSafeFlowParserOptions);
      }
    }
  }
};

const vueCleaningRegex = /<\/*script.*>|<style[\s\S]*style>|<\/*br>/gi;
const vueTemplateRegex = /(<template.*>)([\s\S]*)(<\/template>)/gi;
const vueCommentRegex = /<!--[\s\S]*?-->/gi;
const vueBindRegex = /(:\[)([\s\S]*?)(\])/gi;
const vuePropRegex = /\s([.:@])([a-zA-Z]*?=)/gi;
const vueOpenImgTag = /(<img)((?!>)[\s\S]+?)( [^/]>)/gi;

const TSC_FLAGS =
  tsc.TypeFormatFlags.NoTruncation |
  tsc.TypeFormatFlags.InTypeAlias |
  tsc.TypeFormatFlags.WriteArrayAsGenericType |
  tsc.TypeFormatFlags.GenerateNamesForShadowedTypeParams |
  tsc.TypeFormatFlags.WriteTypeArgumentsOfSignature |
  tsc.TypeFormatFlags.UseFullyQualifiedType |
  tsc.TypeFormatFlags.NoTypeReduction;

/**
 * Convert a single vue file to AST
 */
const toVueAst = (file) => {
  const code = readFileSync(file, "utf-8");
  const cleanedCode = code
    .replace(vueCommentRegex, function (match) {
      return match.replaceAll(/\S/g, " ");
    })
    .replace(vueCleaningRegex, function (match) {
      return match.replaceAll(/\S/g, " ").substring(1) + ";";
    })
    .replace(vueBindRegex, function (match, grA, grB, grC) {
      return grA.replaceAll(/\S/g, " ") + grB + grC.replaceAll(/\S/g, " ");
    })
    .replace(vuePropRegex, function (match, grA, grB) {
      return " " + grA.replace(/[.:@]/g, " ") + grB.replaceAll(".", "-");
    })
    .replace(vueOpenImgTag, function (match, grA, grB, grC) {
      return grA + grB + grC.replace(" >", "/>");
    })
    .replace(vueTemplateRegex, function (match, grA, grB, grC) {
      return grA + grB.replaceAll("{{", "{ ").replaceAll("}}", " }") + grC;
    });
  return codeToJsAst(cleanedCode);
};

function createTsc(srcFiles) {
  try {
    const program = tsc.createProgram(srcFiles, {
      target: tsc.ScriptTarget.ES2020,
      module: tsc.ModuleKind.CommonJS,
      allowImportingTsExtensions: false,
      allowArbitraryExtensions: false,
      allowSyntheticDefaultImports: true,
      allowUmdGlobalAccess: true,
      allowJs: true,
      allowUnreachableCode: true,
      allowUnusedLabels: true,
      alwaysStrict: false,
      emitDecoratorMetadata: true,
      exactOptionalPropertyTypes: true,
      experimentalDecorators: false,
      ignoreDeprecations: true,
      noStrictGenericChecks: true,
      noUncheckedIndexedAccess: false,
      noPropertyAccessFromIndexSignature: false,
      removeComments: true
    });
    const typeChecker = program.getTypeChecker();
    const seenTypes = new Map();

    const safeTypeToString = (node) => {
      try {
        return typeChecker.typeToString(node, TSC_FLAGS);
      } catch (err) {
        return "any";
      }
    };

    const safeTypeWithContextToString = (node, context) => {
      try {
        return typeChecker.typeToString(node, context, TSC_FLAGS);
      } catch (err) {
        return "any";
      }
    };
    const addType = (node) => {
      // STRUCTURAL/CONTAINER NODES
      // These nodes define structure (imports, exports, blocks, declarations)
      if (
        node.kind === tsc.SyntaxKind.SourceFile ||
        node.kind === tsc.SyntaxKind.Block ||
        node.kind === tsc.SyntaxKind.ImportDeclaration ||
        node.kind === tsc.SyntaxKind.ImportClause ||
        node.kind === tsc.SyntaxKind.NamedImports ||
        node.kind === tsc.SyntaxKind.NamespaceImport ||
        node.kind === tsc.SyntaxKind.ExportDeclaration ||
        node.kind === tsc.SyntaxKind.NamedExports ||
        node.kind === tsc.SyntaxKind.TypeAliasDeclaration ||
        node.kind === tsc.SyntaxKind.InterfaceDeclaration ||
        node.kind === tsc.SyntaxKind.ModuleDeclaration
      ) {
        tsc.forEachChild(node, addType);
        return;
      }

      let typeStr;

      try {
        // WRAPPER NODES
        if (
          (tsc.SyntaxKind.SatisfiesExpression &&
            node.kind === tsc.SyntaxKind.SatisfiesExpression) ||
          node.kind === tsc.SyntaxKind.AsExpression ||
          node.kind === tsc.SyntaxKind.TypeAssertionExpression
        ) {
          typeStr = safeTypeWithContextToString(
            typeChecker.getTypeAtLocation(node.expression),
            node.expression
          );
        }
        // FUNCTION/METHOD SIGNATURES
        else if (
          tsc.isFunctionLike(node) ||
          tsc.isMethodDeclaration(node) ||
          tsc.isGetAccessor(node) ||
          tsc.isSetAccessor(node) ||
          tsc.isCallSignatureDeclaration(node) ||
          tsc.isConstructSignatureDeclaration(node)
        ) {
          const signature = typeChecker.getSignatureFromDeclaration(node);
          if (signature) {
            const returnType = typeChecker.getReturnTypeOfSignature(signature);
            typeStr = safeTypeToString(returnType);
          } else {
            const funcType = typeChecker.getTypeAtLocation(node);
            const callSignatures = typeChecker.getSignaturesOfType(
              funcType,
              tsc.SignatureKind.Call
            );
            if (callSignatures.length > 0) {
              typeStr = safeTypeToString(callSignatures[0].getReturnType());
            }
          }
        }
        // STANDARD EXPRESSIONS & IDENTIFIERS
        else {
          const typeObj = typeChecker.getTypeAtLocation(node);
          typeStr = safeTypeWithContextToString(typeObj, node);
        }
        if (
          typeStr &&
          !["any", "unknown", "any[]", "unknown[]", "error"].includes(typeStr)
        ) {
          seenTypes.set(node.getStart(), typeStr);
        }
      } catch (err) {
        /*
        console.warn(
          `Failed to resolve type for kind: ${tsc.SyntaxKind[node.kind]}`,
          err.message
        );
        */
      }
      tsc.forEachChild(node, addType);
    };

    return {
      program: program,
      typeChecker: typeChecker,
      addType: addType,
      seenTypes: seenTypes
    };
  } catch (err) {
    // console.warn("Retrieving types", err.message);
    return undefined;
  }
}

/**
 * Generate AST for JavaScript or TypeScript
 */
const createJSAst = async (options) => {
  try {
    const promiseMap = await getAllSrcJSAndTSFiles(options.src);
    const srcFiles = promiseMap.flatMap((d) => d);
    const CONCURRENCY_LIMIT = 10;
    const chunks = [];
    for (let i = 0; i < srcFiles.length; i += CONCURRENCY_LIMIT) {
      chunks.push(srcFiles.slice(i, i + CONCURRENCY_LIMIT));
    }
    let ts;
    if (options.tsTypes) {
      const projectFiles = srcFiles.filter(
        (file) => !file.includes("node_modules")
      );
      ts = createTsc(projectFiles);
    }
    for (const chunk of chunks) {
      await Promise.all(chunk.map((file) => processFile(file, options, ts)));
    }
  } catch (err) {
    console.error(err);
  }
};

const processFile = (file, options, ts) => {
  try {
    const ast = fileToJsAst(file, options.type);
    writeAstFile(file, ast, options);
    if (ts) {
      try {
        const tsAst = ts.program.getSourceFile(file);
        if (tsAst) {
          tsc.forEachChild(tsAst, ts.addType);
          writeTypesFile(file, ts.seenTypes, options);
          ts.seenTypes.clear();
        }
      } catch (err) {
        // console.warn("Process file", file, ":", err.message);
      }
    }
  } catch (err) {
    console.error(file, err.message);
  }
};

/**
 * Generate AST for .vue files
 */
const createVueAst = async (options) => {
  const srcFiles = await getAllFiles(options.src, ".vue");
  for (const file of srcFiles) {
    try {
      const ast = toVueAst(file);
      if (ast) {
        writeAstFile(file, ast, options);
      }
    } catch (err) {
      console.error(file, err.message);
    }
  }
};

/**
 * Deal with cyclic reference in json
 */
const getCircularReplacer = () => {
  const seen = new WeakSet();
  return (key, value) => {
    if (typeof value === "object" && value !== null) {
      if (seen.has(value)) {
        return;
      }
      seen.add(value);
    }
    return value;
  };
};

/**
 * Write AST data to a json file
 */
const writeAstFile = (file, ast, options) => {
  const relativePath = file.replace(new RegExp("^" + options.src + "/"), "");
  const outAstFile = join(options.output, relativePath + ".json");
  const data = {
    fullName: file,
    relativeName: relativePath,
    ast: ast
  };
  mkdirSync(dirname(outAstFile), { recursive: true });
  writeFileSync(outAstFile, JSON.stringify(data, getCircularReplacer(), "  "));
  console.log("Converted AST for", relativePath, "to", outAstFile);
};

const writeTypesFile = (file, seenTypes, options) => {
  const relativePath = file.replace(new RegExp("^" + options.src + "/"), "");
  const outTypeFile = join(options.output, relativePath + ".typemap");
  mkdirSync(dirname(outTypeFile), { recursive: true });
  writeFileSync(
    outTypeFile,
    JSON.stringify(Object.fromEntries(seenTypes), undefined, "  ")
  );
  console.log("Converted types for", relativePath, "to", outTypeFile);
};

const createXAst = async (options) => {
  const src_dir = options.src;
  try {
    accessSync(src_dir, constants.R_OK);
  } catch (err) {
    console.error(src_dir, "is invalid");
    process.exit(1);
  }
  if (
    existsSync(join(src_dir, "package.json")) ||
    existsSync(join(src_dir, "rush.json"))
  ) {
    return await createJSAst(options);
  }
  console.error(src_dir, "unknown project type");
  process.exit(1);
};

/**
 * Method to start the ast generation process
 *
 * @args options CLI arguments
 */
const start = async (options) => {
  let { type } = options;
  if (!type) {
    type = "";
  }
  type = type.toLowerCase();
  switch (type) {
    case "nodejs":
    case "js":
    case "javascript":
    case "typescript":
    case "ts":
    case "flow":
      return await createJSAst(options);
    case "vue":
      return await createVueAst(options);
    default:
      return await createXAst(options);
  }
};

async function main(argvs) {
  const args = yargs(hideBin(argvs))
    .option("src", {
      alias: "i",
      default: ".",
      description: "Source directory"
    })
    .option("output", {
      alias: "o",
      default: "ast_out",
      description: "Output directory for generated AST json files"
    })
    .option("type", {
      alias: "t",
      description: "Project type. Default auto-detect"
    })
    .option("recurse", {
      alias: "r",
      default: true,
      type: "boolean",
      description: "Recurse mode suitable for mono-repos"
    })
    .option("tsTypes", {
      default: true,
      type: "boolean",
      description: "Generate type mappings using the Typescript Compiler API"
    })
    .version(ASTGEN_VERSION)
    .help("h").argv;

  if (args.version) {
    console.log(ASTGEN_VERSION);
    process.exit(0);
  }

  try {
    if (args.output === "ast_out") {
      args.output = join(args.src, args.output);
    }
    await start(args);
  } catch (e) {
    console.error(e);
    process.exit(1);
  }
}

main(process.argv);
