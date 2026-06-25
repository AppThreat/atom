import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const rootDir = path.resolve(__dirname, "..");
const packagesDir = path.join(rootDir, "packages");

// Ensure packages dir exists
if (!fs.existsSync(packagesDir)) {
  fs.mkdirSync(packagesDir, { recursive: true });
}

const parentPkgJson = JSON.parse(fs.readFileSync(path.join(packagesDir, "atom", "package.json"), "utf8"));
const version = parentPkgJson.version;

const licenseContent = fs.readFileSync(path.join(rootDir, "LICENSE"), "utf8");

const packagesInfo = [
  {
    name: "@appthreat/atom-jar",
    kind: "jar",
    description: "Universal JAR fallback package for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-linux-amd64",
    kind: "native",
    os: ["linux"],
    cpu: ["x64"],
    libc: ["glibc"],
    description: "Linux x64 (glibc) native binary for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-linux-arm64",
    kind: "native",
    os: ["linux"],
    cpu: ["arm64"],
    libc: ["glibc"],
    description: "Linux arm64 (glibc) native binary for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-linux-amd64-musl",
    kind: "native",
    os: ["linux"],
    cpu: ["x64"],
    libc: ["musl"],
    description: "Linux x64 (musl) native binary for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-darwin-arm64",
    kind: "native",
    os: ["darwin"],
    cpu: ["arm64"],
    description: "Darwin arm64 native binary for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-windows-amd64",
    kind: "native",
    os: ["win32"],
    cpu: ["x64"],
    description: "Windows x64 native binary for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-darwin-amd64",
    kind: "jar",
    os: ["darwin"],
    cpu: ["x64"],
    description: "Darwin x64 JAR fallback package for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-windows-arm64",
    kind: "jar",
    os: ["win32"],
    cpu: ["arm64"],
    description: "Windows arm64 JAR fallback package for @appthreat/atom"
  },
  {
    name: "@appthreat/atom-linux-arm64-musl",
    kind: "jar",
    os: ["linux"],
    cpu: ["arm64"],
    libc: ["musl"],
    description: "Linux arm64 (musl) JAR fallback package for @appthreat/atom"
  }
];

function copyDirSync(src, dest) {
  fs.mkdirSync(dest, { recursive: true });
  const entries = fs.readdirSync(src, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyDirSync(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

// Stage the parent @appthreat/atom package's LICENSE and README so that the
// `files` allowlist in its package.json publishes them.
function stageParentMetadata() {
  const atomDir = path.join(packagesDir, "atom");
  fs.writeFileSync(path.join(atomDir, "LICENSE"), licenseContent, "utf8");
  const parentReadme = path.join(rootDir, "README.md");
  const readmeDest = path.join(atomDir, "README.md");
  if (fs.existsSync(parentReadme)) {
    fs.copyFileSync(parentReadme, readmeDest);
  } else {
    fs.writeFileSync(
      readmeDest,
      "# @appthreat/atom\n\nCreate atom (⚛) representation for your application, packages and libraries.\n",
      "utf8"
    );
  }
}

const targetPkgName = process.argv[2];
const srcPathArg = process.argv[3];

// Always (re)stage parent metadata; harmless and keeps the published parent complete.
stageParentMetadata();

const selectedPkgs = targetPkgName
  ? packagesInfo.filter(p => p.name === targetPkgName)
  : packagesInfo;

if (targetPkgName && selectedPkgs.length === 0) {
  console.error(`Error: Unknown sub-package name "${targetPkgName}"`);
  process.exit(1);
}

if (srcPathArg && !fs.existsSync(srcPathArg)) {
  console.error(`Error: source path "${srcPathArg}" does not exist`);
  process.exit(1);
}

for (const pkg of selectedPkgs) {
  const folderName = pkg.name.split("/")[1];
  const pkgDir = path.join(packagesDir, folderName);
  
  if (!fs.existsSync(pkgDir)) {
    fs.mkdirSync(pkgDir, { recursive: true });
  }

  const subPkgJson = {
    name: pkg.name,
    version: version,
    description: pkg.description,
    type: "module",
    repository: {
      type: "git",
      url: "git+https://github.com/AppThreat/atom.git"
    },
    author: "Team AppThreat <cloud@appthreat.com>",
    license: "MIT",
    bugs: {
      url: "https://github.com/AppThreat/atom/issues"
    },
    homepage: "https://github.com/AppThreat/atom#readme"
  };

  if (pkg.os) subPkgJson.os = pkg.os;
  if (pkg.cpu) subPkgJson.cpu = pkg.cpu;
  if (pkg.libc) subPkgJson.libc = pkg.libc;

  if (pkg.kind === "native") {
    subPkgJson.files = ["bin/"];
  } else {
    // JAR packages ship only the unpacked distribution under plugins/. All
    // launch logic (java detection, classpath, log4j) lives in the parent
    // @appthreat/atom dispatcher, so these packages carry no runtime deps.
    subPkgJson.files = ["plugins/"];
  }

  fs.writeFileSync(path.join(pkgDir, "package.json"), JSON.stringify(subPkgJson, null, 2), "utf8");
  fs.writeFileSync(path.join(pkgDir, "LICENSE"), licenseContent, "utf8");

  const readmeContent = `# ${pkg.name}\n\n${pkg.description}.\n\nThis is an internal package used by \`@appthreat/atom\` and is not intended to be installed directly.\n`;
  fs.writeFileSync(path.join(pkgDir, "README.md"), readmeContent, "utf8");

  if (srcPathArg) {
    if (pkg.kind === "native") {
      const binDir = path.join(pkgDir, "bin");
      if (!fs.existsSync(binDir)) {
        fs.mkdirSync(binDir, { recursive: true });
      }
      const isWinPkg = pkg.os && pkg.os.includes("win32");
      const destBinName = isWinPkg ? "atom.exe" : "atom";
      const destBinPath = path.join(binDir, destBinName);
      
      console.log(`Copying native binary from ${srcPathArg} to ${destBinPath}`);
      fs.copyFileSync(srcPathArg, destBinPath);
      fs.chmodSync(destBinPath, 0o755);
      const copiedSize = fs.statSync(destBinPath).size;
      if (copiedSize === 0) {
        console.error(`Error: copied native binary ${destBinPath} is empty`);
        process.exit(1);
      }
    } else {
      const destPluginsDir = path.join(pkgDir, "plugins");
      if (fs.existsSync(destPluginsDir)) {
        fs.rmSync(destPluginsDir, { recursive: true, force: true });
      }
      console.log(`Copying plugins directory from ${srcPathArg} to ${destPluginsDir}`);
      copyDirSync(srcPathArg, destPluginsDir);
    }
  }
}

console.log("Assembly completed successfully.");
