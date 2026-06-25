import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import fs from "node:fs";
import { execSync } from "node:child_process";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);

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

export function locateAtomBinary(opts = {}) {
  const { preferredPkg, platform } = resolveAtomProvider(opts);
  
  // Build a list of package fallbacks to search.
  const packagesToTry = [preferredPkg];
  if (preferredPkg !== "@appthreat/atom-jar") {
    packagesToTry.push("@appthreat/atom-jar");
  }

  const dirName = dirname(fileURLToPath(import.meta.url));

  for (const pkg of packagesToTry) {
    try {
      const pkgJsonPath = require.resolve(`${pkg}/package.json`, { paths: [dirName] });
      const pkgDir = dirname(pkgJsonPath);
      const isNative = NATIVE_PACKAGES.has(pkg);
      
      if (isNative) {
        const exeName = platform === "win32" ? "atom.exe" : "atom";
        const binaryPath = join(pkgDir, "bin", exeName);
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
        if (fs.existsSync(pluginsDir)) {
          return {
            kind: "jar",
            pkg,
            binPath: null,
            pluginsDir
          };
        }
      }
    } catch (e) {
      // Package not installed or resolve failed, try next
    }
  }

  return null;
}
