import test from "node:test";
import assert from "node:assert";
import { spawn } from "node:child_process";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { fileURLToPath } from "node:url";
import {
  TIMEOUT_EXIT_CODE,
  splitRuntimeOptions,
  supervisorWatch
} from "../packages/atom/supervise.js";

const SUPERVISE_SRC = fileURLToPath(new URL("../packages/atom/supervise.js", import.meta.url));
const isWin = process.platform === "win32";

test("splitRuntimeOptions lifts only the leading runtime options", () => {
  assert.deepEqual(splitRuntimeOptions(["-XX:MaxHeapSize=1024", "-Xss4m", "reachables", "-l", "python"]), {
    runtimeOptions: ["-XX:MaxHeapSize=1024", "-Xss4m"],
    programArgs: ["reachables", "-l", "python"]
  });
  assert.deepEqual(splitRuntimeOptions(["usages", "-Xmx2g"]), {
    runtimeOptions: [],
    programArgs: ["usages", "-Xmx2g"]
  });
  assert.deepEqual(splitRuntimeOptions([]), { runtimeOptions: [], programArgs: [] });
});

test("supervisorWatch is off unless ATOM_PARENT_PID names a process", () => {
  const probe = { isProcessAlive: () => true, ppid: () => 10, isWin: false };
  assert.equal(supervisorWatch({}, probe), undefined);
  assert.equal(supervisorWatch({ ATOM_PARENT_PID: "" }, probe), undefined);
  assert.equal(supervisorWatch({ ATOM_PARENT_PID: "abc" }, probe), undefined);
  assert.equal(supervisorWatch({ ATOM_PARENT_PID: "0" }, probe), undefined);
});

test("supervisorWatch detects a dead or abandoning supervisor", () => {
  let ppid = 10;
  const alive = new Set([10, 20]);
  const posix = { isProcessAlive: (pid) => alive.has(pid), ppid: () => ppid, isWin: false };
  const watch = supervisorWatch({ ATOM_PARENT_PID: "10" }, posix);
  assert.equal(watch.pid, 10);
  assert.equal(watch.isGone(), false);
  // Reparented while the supervisor is still a zombie (kill(pid, 0) still succeeds).
  ppid = 1;
  assert.equal(watch.isGone(), true);

  ppid = 10;
  alive.delete(10);
  assert.equal(supervisorWatch({ ATOM_PARENT_PID: "10" }, posix).isGone(), true);

  // Windows does not reparent: an intermediate parent (cmd.exe under shell: true) is probed.
  const winAlive = new Set([10, 30]);
  const win = { isProcessAlive: (pid) => winAlive.has(pid), ppid: () => 30, isWin: true };
  const winWatch = supervisorWatch({ ATOM_PARENT_PID: "10" }, win);
  assert.equal(winWatch.isGone(), false);
  winAlive.delete(30);
  assert.equal(winWatch.isGone(), true);
});

/**
 * Supervise a stand-in runtime (a node process that writes its pid and environment to a file,
 * then waits) through the real superviseRuntime, in a separate dispatcher process.
 */
function startDispatcher(env, runtimeScript) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "atom-supervise-"));
  const info = path.join(dir, "runtime.json");
  const runtime = path.join(dir, "runtime.mjs");
  fs.writeFileSync(
    runtime,
    `import fs from "node:fs";
fs.writeFileSync(${JSON.stringify(info)}, JSON.stringify({ pid: process.pid, parentPid: process.env.ATOM_PARENT_PID }));
${runtimeScript}`
  );
  const dispatcher = path.join(dir, "dispatcher.mjs");
  fs.writeFileSync(
    dispatcher,
    `import { superviseRuntime } from ${JSON.stringify(SUPERVISE_SRC)};
superviseRuntime(process.execPath, [${JSON.stringify(runtime)}], process.env, process.cwd());`
  );
  const child = spawn(process.execPath, [dispatcher], {
    env: { ...process.env, ...env },
    stdio: ["ignore", "ignore", "pipe"]
  });
  let stderr = "";
  child.stderr.on("data", (d) => (stderr += d));
  const exited = new Promise((resolve) =>
    child.on("exit", (code, signal) => resolve({ code, signal, stderr }))
  );
  const runtimeInfo = async () => {
    for (let i = 0; i < 200; i++) {
      if (fs.existsSync(info)) {
        try {
          return JSON.parse(fs.readFileSync(info, "utf8"));
        } catch {
          // still being written
        }
      }
      await new Promise((r) => setTimeout(r, 25));
    }
    throw new Error("the stand-in runtime never started");
  };
  return { child, exited, runtimeInfo, dir };
}

const isAlive = (pid) => {
  try {
    process.kill(pid, 0);
    return true;
  } catch (e) {
    return e.code === "EPERM";
  }
};

const waitForDeath = async (pid) => {
  for (let i = 0; i < 200 && isAlive(pid); i++) {
    await new Promise((r) => setTimeout(r, 25));
  }
  return !isAlive(pid);
};

const WAIT_FOREVER = "setInterval(() => {}, 1000);";

test("the runtime exit status is passed through", async () => {
  const { exited, runtimeInfo } = startDispatcher({}, "process.exit(3);");
  const info = await runtimeInfo();
  assert.equal(info.parentPid !== undefined, true);
  assert.equal((await exited).code, 3);
});

test("the runtime is told the dispatcher is its supervisor", async () => {
  const { child, exited, runtimeInfo } = startDispatcher({ ATOM_PARENT_PID: "" }, "process.exit(0);");
  assert.equal((await runtimeInfo()).parentPid, String(child.pid));
  assert.equal((await exited).code, 0);
});

test("ATOM_TIMEOUT stops the runtime and exits with the timeout status", async () => {
  const { exited, runtimeInfo } = startDispatcher(
    { ATOM_TIMEOUT: "300", ATOM_KILL_GRACE_MS: "2000" },
    WAIT_FOREVER
  );
  const { pid } = await runtimeInfo();
  const result = await exited;
  assert.equal(result.code, TIMEOUT_EXIT_CODE);
  assert.match(result.stderr, /ATOM_TIMEOUT/);
  assert.equal(await waitForDeath(pid), true);
});

test("a runtime that ignores SIGTERM is killed after the grace period", { skip: isWin }, async () => {
  const { exited, runtimeInfo } = startDispatcher(
    { ATOM_TIMEOUT: "300", ATOM_KILL_GRACE_MS: "300" },
    `process.on("SIGTERM", () => {}); ${WAIT_FOREVER}`
  );
  const { pid } = await runtimeInfo();
  assert.equal((await exited).code, TIMEOUT_EXIT_CODE);
  assert.equal(await waitForDeath(pid), true);
});

test("the runtime is stopped when the supervisor named by ATOM_PARENT_PID dies", async () => {
  const supervisor = spawn(process.execPath, ["-e", WAIT_FOREVER], { stdio: "ignore" });
  const { exited, runtimeInfo } = startDispatcher(
    { ATOM_PARENT_PID: String(supervisor.pid) },
    WAIT_FOREVER
  );
  const { pid } = await runtimeInfo();
  supervisor.kill("SIGKILL");
  const result = await exited;
  assert.match(result.stderr, /supervising process/);
  assert.equal(await waitForDeath(pid), true);
});

test("SIGTERM to the dispatcher is forwarded and waited for", { skip: isWin }, async () => {
  const { child, exited, runtimeInfo } = startDispatcher({}, WAIT_FOREVER);
  const { pid } = await runtimeInfo();
  child.kill("SIGTERM");
  const result = await exited;
  // The stand-in runtime dies from the forwarded SIGTERM; the dispatcher reports that as 128 + 15.
  assert.equal(result.code, 143);
  assert.equal(isAlive(pid), false);
});
