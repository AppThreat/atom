#!/usr/bin/env node

import { join, dirname } from "path";
import yargs from "yargs";
import { hideBin } from "yargs/helpers";
import { parse } from "@babel/parser";
import { parse as parseHermes } from "hermes-parser";
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
    ["flow", { all: true, enums: true }]
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
    "flow"
  ]
};

const shouldIncludeNodeModulesBundles =
  process.env?.ASTGEN_INCLUDE_NODE_MODULES_BUNDLES === "true" ||
  (process.env?.ASTGEN_IGNORE_DIRS &&
    process.env.ASTGEN_IGNORE_DIRS.length > 0 &&
    !process.env.ASTGEN_IGNORE_DIRS.toLowerCase().includes("node_modules"));

/**
 * Return paths to all (j|tsx?) files.
 * Optionally includes specific bundled files from node_modules if:
 * 1. ASTGEN_INCLUDE_NODE_MODULES_BUNDLES is "true", OR
 * 2. ASTGEN_IGNORE_DIRS is non-empty and doesn't include "node_modules"
 */
const getAllSrcJSAndTSFiles = (src) => {
  const filePattern = "\\.(js|jsx|cjs|mjs|ts|tsx|vue|svelte|xsjs|xsjslib|ejs)$";
  const bundledNodeModulesPattern =
    "node_modules[\\\\/].*[\\\\/](?:.*\\.)?(bundle|dist|index|min|app)\\.(js|cjs|mjs)$";

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
  // Step 2: Combine both lists
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
  if (file.endsWith(".ejs")) {
    return toEjsAst(file);
  }
  return codeToJsAst(file, readFileSync(file, "utf-8"), projectType);
};

/**
 * Convert a single JS/TS code snippet to AST
 */
const codeToJsAst = (file, code, projectType) => {
  const isJs = /\.(js|jsx|cjs|mjs)$/.test(file);
  if (isJs && projectType === "flow") {
    try {
      return parseHermes(code, {
        sourceType: "unambiguous",
        babel: true,
        allowReturnOutsideFunction: true,
        flow: "all",
        sourceFilename: file,
        tokens: true
      });
    } catch (err) {
      // Ignore
    }
  }
  // If user explicitly said 'flow', we try Babel-Flow first, then Babel-Standard.
  // Otherwise, we try Babel-Standard (TS/ESNext) first, then Babel-Flow and finally hermes.
  let primaryBabelOptions = babelParserOptions;
  let secondaryBabelOptions = babelFlowParserOptions;
  if (projectType === "flow") {
    primaryBabelOptions = babelFlowParserOptions;
    secondaryBabelOptions = babelParserOptions;
  }
  try {
    return parse(code, primaryBabelOptions);
  } catch (errPrimary) {
    try {
      return parse(code, secondaryBabelOptions);
    } catch (errSecondary) {
      try {
        return parse(code, babelSafeParserOptions);
      } catch (errSafe) {
        try {
          return parse(code, babelSafeFlowParserOptions);
        } catch (errSafeFlow) {
          return parseHermes(code, {
            sourceType: "unambiguous",
            babel: true,
            allowReturnOutsideFunction: true,
            flow: "all",
            sourceFilename: file,
            tokens: true
          });
        }
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
  return codeToJsAst(file, cleanedCode, "ts");
};

/**
 * Convert a single EJS file to AST.
 */
const toEjsAst = (file) => {
  const originalCode = readFileSync(file, "utf-8");
  let arr = originalCode.split("");
  const scriptRegex = /(<script>)([\s\S]*?)(<\/script>)/gi;
  let match;

  while ((match = scriptRegex.exec(originalCode)) !== null) {
    const openStart = match.index;
    const openLen = match[1].length;
    arr[openStart] = "<";
    arr[openStart + 1] = "%";
    for (let i = 2; i < openLen; i++) arr[openStart + i] = " ";
    const closeStart = match.index + match[0].length - match[3].length;
    const closeLen = match[3].length;
    arr[closeStart] = "%";
    arr[closeStart + 1] = ">";
    for (let i = 2; i < closeLen; i++) arr[closeStart + i] = " ";
    const content = match[2];
    const contentOffset = openStart + openLen;
    const innerRegex = /(<%[=\-_#]?)([\s\S]*?)([-_#]?%>)/g;
    let innerMatch;
    while ((innerMatch = innerRegex.exec(content)) !== null) {
      const innerAbsStart = contentOffset + innerMatch.index;
      if (innerMatch[1] === "<%" && innerMatch[3] === "-%>") {
        for (let k = 0; k < innerMatch[0].length; k++)
          arr[innerAbsStart + k] = " ";
      } else {
        for (let k = 0; k < innerMatch[1].length; k++)
          arr[innerAbsStart + k] = " ";
        const endDelimStart =
          innerAbsStart + innerMatch[0].length - innerMatch[3].length;
        for (let k = 0; k < innerMatch[3].length; k++)
          arr[endDelimStart + k] = " ";
      }
    }
  }
  const codeWithoutScriptTag = arr.join("");
  const out = new Array(codeWithoutScriptTag.length).fill(" ");
  for (let i = 0; i < codeWithoutScriptTag.length; i++) {
    const c = codeWithoutScriptTag[i];
    if (c === "\n" || c === "\r") out[i] = c;
  }

  const tagRegex = /(<%[=\-_#]?)([\s\S]*?)([-_#]?%>)/g;
  let tagMatch;
  while ((tagMatch = tagRegex.exec(codeWithoutScriptTag)) !== null) {
    const [fullMatch, openTag, content, closeTag] = tagMatch;
    if (openTag === "<%#" || content.trim().startsWith("include ")) continue;
    const startIndex = tagMatch.index + openTag.length;
    const endIndex = tagMatch.index + fullMatch.length - closeTag.length;
    for (let k = startIndex; k < endIndex; k++) {
      out[k] = codeWithoutScriptTag[k];
    }
    const trimmed = content.trim();
    const needsSemi =
      trimmed.length > 0 &&
      !trimmed.endsWith("{") &&
      !trimmed.endsWith("}") &&
      !trimmed.endsWith(";");

    if (needsSemi) {
      out[endIndex] = ";";
    }
  }

  return codeToJsAst(file, out.join(""), "ts");
};

function createTsc(srcFiles) {
  try {
    const program = tsc.createProgram(srcFiles, {
      target: tsc.ScriptTarget.ES2022,
      module: tsc.ModuleKind.CommonJS,
      moduleResolution: tsc.ModuleResolutionKind.Node10,
      allowImportingTsExtensions: false,
      allowArbitraryExtensions: false,
      allowSyntheticDefaultImports: true,
      allowUmdGlobalAccess: true,
      allowJs: true,
      checkJs: true,
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
      lib: ["lib.es2022.d.ts", "lib.dom.d.ts"]
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
          let typeObj = typeChecker.getTypeAtLocation(node);
          if (
            typeObj.isLiteral() ||
            typeObj.flags & tsc.TypeFlags.BooleanLiteral
          ) {
            try {
              typeObj = typeChecker.getBaseTypeOfLiteralType(typeObj);
            } catch (e) {
              // ignore
            }
          }
          typeStr = safeTypeWithContextToString(typeObj, node);
        }
        if (
          typeStr &&
          ![
            "any",
            "unknown",
            "any[]",
            "unknown[]",
            "error",
            "/*unresolved*/ any"
          ].includes(typeStr)
        ) {
          seenTypes.set(node.getStart(), typeStr);
        }
      } catch (err) {
        // Silently fail on type resolution errors
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
      const projectFiles = !shouldIncludeNodeModulesBundles
        ? srcFiles.filter((file) => !file.includes("node_modules"))
        : srcFiles;
      ts = createTsc(projectFiles);
    }
    for (const chunk of chunks) {
      await Promise.all(chunk.map((file) => processFile(file, options, ts)));
      if (typeof globalThis.gc === "function") {
        try {
          globalThis.gc();
        } catch (e) {
          // ignore
        }
      } else if (typeof Bun !== "undefined" && typeof Bun.gc === "function") {
        try {
          Bun.gc(true);
        } catch (e) {
          // ignore
        }
      }
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
        console.warn("Process file", file, ":", err.message);
      }
    }
  } catch (err) {
    console.error("Failure:", file, err?.message);
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
  writeFileSync(
    outAstFile,
    JSON.stringify(data, getCircularReplacer(), undefined)
  );
  console.log("Converted AST for", relativePath, "to", outAstFile);
};

const writeTypesFile = (file, seenTypes, options) => {
  const relativePath = file.replace(new RegExp("^" + options.src + "/"), "");
  const outTypeFile = join(options.output, relativePath + ".typemap");
  mkdirSync(dirname(outTypeFile), { recursive: true });
  writeFileSync(
    outTypeFile,
    JSON.stringify(Object.fromEntries(seenTypes), undefined, undefined)
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
