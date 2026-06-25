import test from "node:test";
import assert from "node:assert";
import { resolveAtomProvider, locateAtomBinary } from "../packages/atom/resolve.js";

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
