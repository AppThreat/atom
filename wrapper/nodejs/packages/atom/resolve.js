import { dirname, join, sep } from "node:path";
import fs from "node:fs";
import { execSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const SELF_DIR = dirname(fileURLToPath(import.meta.url));

const NATIVE_PACKAGES = new Set([
  "@appthreat/atom-linux-amd64",
  "@appthreat/atom-linux-arm64",
  "@appthreat/atom-darwin-arm64",
  "@appthreat/atom-linux-amd64-musl",
  "@appthreat/atom-windows-amd64"
]);

export function getLinuxLibc() {
  if (process.platform !== "linux") {
    return null;
  }
  // Try process.report
  try {
    const report = process.report?.getReport();
    if (typeof report === "object" && report?.header) {
      if (report.header.glibcVersionRuntime) {
        return "glibc";
      }
    }
  } catch (e) {
    // Ignore report errors
  }
  // Check filesystem or ldd
  try {
    if (fs.existsSync("/etc/alpine-release")) {
      return "musl";
    }
  } catch (e) {
    // ignore
  }

  try {
    const lddOutput = execSync("ldd --version", { stdio: ["ignore", "pipe", "ignore"] }).toString();
    if (lddOutput.includes("musl")) {
      return "musl";
    }
  } catch (e) {
    // ignore
  }

  // Default to glibc if Linux
  return "glibc";
}

export function resolveAtomProvider(opts = {}) {
  const platform = opts.platform || process.platform;
  const arch = opts.arch || process.arch;
  let libc = opts.libc;
  if (platform === "linux" && !libc) {
    libc = getLinuxLibc();
  }

  let preferredPkg = null;
  let kind = "jar";

  if (platform === "win32") {
    if (arch === "x64") {
      preferredPkg = "@appthreat/atom-windows-amd64";
      kind = "native";
    } else if (arch === "arm64") {
      preferredPkg = "@appthreat/atom-windows-arm64";
      kind = "jar";
    }
  } else if (platform === "darwin") {
    if (arch === "arm64") {
      preferredPkg = "@appthreat/atom-darwin-arm64";
      kind = "native";
    } else if (arch === "x64") {
      preferredPkg = "@appthreat/atom-darwin-amd64";
      kind = "jar";
    }
  } else if (platform === "linux") {
    if (arch === "x64") {
      if (libc === "musl") {
        preferredPkg = "@appthreat/atom-linux-amd64-musl";
        kind = "native";
      } else {
        preferredPkg = "@appthreat/atom-linux-amd64";
        kind = "native";
      }
    } else if (arch === "arm64") {
      if (libc === "musl") {
        preferredPkg = "@appthreat/atom-linux-arm64-musl";
        kind = "jar";
      } else {
        preferredPkg = "@appthreat/atom-linux-arm64";
        kind = "native";
      }
    }
  }

  if (!preferredPkg) {
    preferredPkg = "@appthreat/atom-jar";
    kind = "jar";
  }

  return {
    preferredPkg,
    kind,
    platform,
    arch,
    libc
  };
}

// Read the parent @appthreat/atom version so we can construct pnpm virtual-store
// directory names (e.g. .pnpm/@appthreat+atom-jar@<version>/...).
function readSelfVersion() {
  try {
    const pj = JSON.parse(fs.readFileSync(join(SELF_DIR, "package.json"), "utf8"));
    return pj.version;
  } catch (e) {
    return undefined;
  }
}

/**
 * Build the list of directories where a sibling @appthreat sub-package may live,
 * derived purely from this module's own location (no require.resolve, which has
 * historically behaved inconsistently across Node versions and package managers).
 *
 * This mirrors the strategy cdxgen uses in lib/helpers/plugins.js: tokenize our
 * own __dirname, then construct candidate paths for the common layouts:
 *   - npm flat node_modules (sub-package is a scoped sibling under node_modules)
 *   - global installs (.../lib/node_modules/@appthreat/<pkg>)
 *   - the parent's own nested node_modules (pnpm symlinks a package's own deps here)
 *   - pnpm virtual store (.../node_modules/.pnpm/@appthreat+<pkg>@<ver>/node_modules/...)
 *
 * @param {string} pkgName e.g. "@appthreat/atom-jar"
 * @returns {string[]} candidate package directories, de-duplicated, in priority order
 */
function candidatePackageDirs(pkgName) {
  const folder = pkgName.split("/")[1]; // e.g. "atom-jar"
  const version = readSelfVersion();
  const parts = SELF_DIR.split(sep);
  const dirs = [];

  // 1) The parent may carry the sub-package in its own nested node_modules
  //    (npm nested installs, and pnpm symlinks a package's deps next to it).
  dirs.push(join(SELF_DIR, "node_modules", "@appthreat", folder));

  // 2) Walk every node_modules segment present in our own path, from the
  //    deepest outward, and look for the scoped sibling under each. Covers npm
  //    flat layout, global installs, and pnpm's per-package private store.
  for (let i = parts.length - 1; i >= 0; i--) {
    if (parts[i] === "node_modules") {
      const root = parts.slice(0, i + 1).join(sep) || sep;
      dirs.push(join(root, "@appthreat", folder));

      // 3) pnpm virtual store hanging off this node_modules root.
      if (version) {
        dirs.push(
          join(root, ".pnpm", `@appthreat+${folder}@${version}`, "node_modules", "@appthreat", folder)
        );
      }
    }
  }

  // 4) When our own dir already lives inside a .pnpm virtual store, the sibling
  //    package has its own .pnpm entry next to ours.
  const pnpmMarker = `${sep}.pnpm${sep}`;
  const pnpmIdx = SELF_DIR.indexOf(pnpmMarker);
  if (pnpmIdx !== -1 && version) {
    const base = SELF_DIR.slice(0, pnpmIdx);
    dirs.push(
      join(base, ".pnpm", `@appthreat+${folder}@${version}`, "node_modules", "@appthreat", folder)
    );
  }

  // De-duplicate while preserving order.
  return [...new Set(dirs)];
}

/**
 * Enumerate every path the locator would inspect for the current runtime, noting
 * which exist. Used both for the optional ATOM_DEBUG trace and for the diagnostic
 * dump the dispatcher prints when no provider can be found.
 *
 * @returns {{selfDir: string, platform: string, arch: string, libc: string|undefined,
 *   preferredPkg: string, attempts: {pkg: string, kind: string, path: string, exists: boolean}[]}}
 */
export function describeAtomSearch(opts = {}) {
  const { preferredPkg, platform, arch, libc } = resolveAtomProvider(opts);
  const packagesToTry = [preferredPkg];
  if (preferredPkg !== "@appthreat/atom-jar") {
    packagesToTry.push("@appthreat/atom-jar");
  }
  const attempts = [];
  for (const pkg of packagesToTry) {
    const isNative = NATIVE_PACKAGES.has(pkg);
    for (const pkgDir of candidatePackageDirs(pkg)) {
      const checkPath = isNative
        ? join(pkgDir, "bin", platform === "win32" ? "atom.exe" : "atom")
        : join(pkgDir, "plugins");
      attempts.push({
        pkg,
        kind: isNative ? "native" : "jar",
        path: checkPath,
        exists: fs.existsSync(checkPath)
      });
    }
  }
  return { selfDir: SELF_DIR, platform, arch, libc, preferredPkg, attempts };
}

export function locateAtomBinary(opts = {}) {
  const debug = !!process.env.ATOM_DEBUG;
  const { preferredPkg, platform } = resolveAtomProvider(opts);

  if (debug) {
    console.error(`[atom] resolver self dir: ${SELF_DIR}`);
    console.error(`[atom] platform=${platform} preferred=${preferredPkg}`);
  }

  // Build a list of package fallbacks to search: the preferred platform package
  // first, then the universal jar package.
  const packagesToTry = [preferredPkg];
  if (preferredPkg !== "@appthreat/atom-jar") {
    packagesToTry.push("@appthreat/atom-jar");
  }

  for (const pkg of packagesToTry) {
    const isNative = NATIVE_PACKAGES.has(pkg);
    for (const pkgDir of candidatePackageDirs(pkg)) {
      if (isNative) {
        const exeName = platform === "win32" ? "atom.exe" : "atom";
        const binaryPath = join(pkgDir, "bin", exeName);
        if (debug) {
          console.error(`[atom] check native ${binaryPath} -> ${fs.existsSync(binaryPath)}`);
        }
        if (fs.existsSync(binaryPath)) {
          return {
            kind: "native",
            pkg,
            binPath: binaryPath,
            pluginsDir: null
          };
        }
      } else {
        const pluginsDir = join(pkgDir, "plugins");
        if (debug) {
          console.error(`[atom] check jar ${pluginsDir} -> ${fs.existsSync(pluginsDir)}`);
        }
        if (fs.existsSync(pluginsDir)) {
          return {
            kind: "jar",
            pkg,
            binPath: null,
            pluginsDir
          };
        }
      }
    }
  }

  return null;
}
