import test from "node:test";
import assert from "node:assert";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { resolveAtomProvider, locateAtomBinary } from "../packages/atom/resolve.js";

const RESOLVE_SRC = fileURLToPath(new URL("../packages/atom/resolve.js", import.meta.url));

test("resolveAtomProvider mappings", () => {
  assert.deepEqual(resolveAtomProvider({ platform: "win32", arch: "x64" }), {
    preferredPkg: "@appthreat/atom-windows-amd64",
    kind: "native",
    platform: "win32",
    arch: "x64",
    libc: undefined
  });

  assert.deepEqual(resolveAtomProvider({ platform: "win32", arch: "arm64" }), {
    preferredPkg: "@appthreat/atom-windows-arm64",
    kind: "jar",
    platform: "win32",
    arch: "arm64",
    libc: undefined
  });

  assert.deepEqual(resolveAtomProvider({ platform: "darwin", arch: "arm64" }), {
    preferredPkg: "@appthreat/atom-darwin-arm64",
    kind: "native",
    platform: "darwin",
    arch: "arm64",
    libc: undefined
  });

  assert.deepEqual(resolveAtomProvider({ platform: "darwin", arch: "x64" }), {
    preferredPkg: "@appthreat/atom-darwin-amd64",
    kind: "jar",
    platform: "darwin",
    arch: "x64",
    libc: undefined
  });

  assert.deepEqual(resolveAtomProvider({ platform: "linux", arch: "x64", libc: "glibc" }), {
    preferredPkg: "@appthreat/atom-linux-amd64",
    kind: "native",
    platform: "linux",
    arch: "x64",
    libc: "glibc"
  });

  assert.deepEqual(resolveAtomProvider({ platform: "linux", arch: "arm64", libc: "glibc" }), {
    preferredPkg: "@appthreat/atom-linux-arm64",
    kind: "native",
    platform: "linux",
    arch: "arm64",
    libc: "glibc"
  });

  assert.deepEqual(resolveAtomProvider({ platform: "linux", arch: "x64", libc: "musl" }), {
    preferredPkg: "@appthreat/atom-linux-amd64-musl",
    kind: "native",
    platform: "linux",
    arch: "x64",
    libc: "musl"
  });

  assert.deepEqual(resolveAtomProvider({ platform: "linux", arch: "arm64", libc: "musl" }), {
    preferredPkg: "@appthreat/atom-linux-arm64-musl",
    kind: "jar",
    platform: "linux",
    arch: "arm64",
    libc: "musl"
  });

  assert.deepEqual(resolveAtomProvider({ platform: "freebsd", arch: "x64" }), {
    preferredPkg: "@appthreat/atom-jar",
    kind: "jar",
    platform: "freebsd",
    arch: "x64",
    libc: undefined
  });
});

test("locateAtomBinary resolution", () => {
  // Returns null when no platform package is installed (e.g. a bare dev checkout),
  // otherwise resolves to the installed native binary or the jar fallback.
  const binaryInfo = locateAtomBinary();
  if (binaryInfo === null) {
    return;
  }
  if (binaryInfo.kind === "native") {
    assert.ok(binaryInfo.binPath);
    assert.ok(binaryInfo.pkg);
  } else {
    assert.ok(binaryInfo.pluginsDir);
    assert.ok(binaryInfo.pkg === "@appthreat/atom-jar");
  }
});

// Verify that the __dirname-based locator (no require.resolve) finds sibling
// sub-packages across the install layouts produced by npm, pnpm and global
// installs. Each case stamps a copy of resolve.js into a fabricated tree and
// imports it from there so its SELF_DIR reflects that layout.
test("locateAtomBinary across package-manager layouts", async () => {
  const tmp = fs.mkdtempSync(path.join(os.tmpdir(), "atom-layout-"));
  const linux = { platform: "linux", arch: "x64", libc: "glibc" };

  const placeParent = (atomDir) => {
    fs.mkdirSync(atomDir, { recursive: true });
    fs.copyFileSync(RESOLVE_SRC, path.join(atomDir, "resolve.js"));
    fs.writeFileSync(path.join(atomDir, "package.json"), '{"version":"3.0.0"}\n');
  };
  const locate = async (atomDir, opts) => {
    const mod = await import(
      `file://${path.join(atomDir, "resolve.js")}?case=${Math.random()}`
    );
    return mod.locateAtomBinary(opts);
  };

  try {
    // npm flat: root/node_modules/@appthreat/{atom,atom-jar}
    const nm = path.join(tmp, "flat", "node_modules", "@appthreat");
    placeParent(path.join(nm, "atom"));
    fs.mkdirSync(path.join(nm, "atom-jar", "plugins"), { recursive: true });
    let r = await locate(path.join(nm, "atom"), linux);
    assert.equal(r?.kind, "jar");
    assert.equal(r?.pkg, "@appthreat/atom-jar");

    // A native sibling must take precedence over the jar fallback.
    fs.mkdirSync(path.join(nm, "atom-linux-amd64", "bin"), { recursive: true });
    fs.writeFileSync(path.join(nm, "atom-linux-amd64", "bin", "atom"), "#!/bin/sh\n");
    r = await locate(path.join(nm, "atom"), linux);
    assert.equal(r?.kind, "native");
    assert.equal(r?.pkg, "@appthreat/atom-linux-amd64");

    // pnpm private store: atom-jar nested under atom's own node_modules.
    const pnpmAtom = path.join(
      tmp, "pnpm", "node_modules", ".pnpm",
      "@appthreat+atom@3.0.0", "node_modules", "@appthreat", "atom"
    );
    placeParent(pnpmAtom);
    fs.mkdirSync(
      path.join(pnpmAtom, "node_modules", "@appthreat", "atom-jar", "plugins"),
      { recursive: true }
    );
    r = await locate(pnpmAtom, linux);
    assert.equal(r?.kind, "jar");

    // pnpm virtual store: atom-jar has its own .pnpm entry beside atom's.
    const vAtom = path.join(
      tmp, "pnpm2", "node_modules", ".pnpm",
      "@appthreat+atom@3.0.0", "node_modules", "@appthreat", "atom"
    );
    placeParent(vAtom);
    fs.mkdirSync(
      path.join(
        tmp, "pnpm2", "node_modules", ".pnpm",
        "@appthreat+atom-jar@3.0.0", "node_modules", "@appthreat", "atom-jar", "plugins"
      ),
      { recursive: true }
    );
    r = await locate(vAtom, linux);
    assert.equal(r?.kind, "jar");

    // global install: prefix/lib/node_modules/@appthreat/{atom,atom-jar}
    const g = path.join(tmp, "usr", "local", "lib", "node_modules", "@appthreat");
    placeParent(path.join(g, "atom"));
    fs.mkdirSync(path.join(g, "atom-jar", "plugins"), { recursive: true });
    r = await locate(path.join(g, "atom"), linux);
    assert.equal(r?.kind, "jar");

    // nothing installed alongside the parent -> null
    const bare = path.join(tmp, "bare", "node_modules", "@appthreat", "atom");
    placeParent(bare);
    r = await locate(bare, linux);
    assert.equal(r, null);
  } finally {
    fs.rmSync(tmp, { recursive: true, force: true });
  }
});
