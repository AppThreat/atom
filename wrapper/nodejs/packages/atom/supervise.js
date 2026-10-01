import { constants as osConstants } from "node:os";
import { spawn } from "node:child_process";

const isWin = process.platform === "win32";

// Exit status when ATOM_TIMEOUT stops atom. It is the status coreutils `timeout` uses, so a
// caller can tell "ran out of time" apart from "the analysis failed".
export const TIMEOUT_EXIT_CODE = 124;

// How often the dispatcher checks that the process named by ATOM_PARENT_PID is still there.
const PARENT_POLL_MS = 1000;
// Signals the dispatcher relays to the runtime instead of dying from them itself.
const FORWARDED_SIGNALS = isWin
  ? ["SIGINT", "SIGTERM", "SIGBREAK"]
  : ["SIGINT", "SIGTERM", "SIGHUP", "SIGQUIT"];
// Runtime options a native image takes in front of its own arguments (cdxgen passes
// -XX:MaxHeapSize this way). The jar provider has to hand them to java instead.
const RUNTIME_OPTION_PREFIXES = ["-XX:", "-Xmx", "-Xms", "-Xmn", "-Xss"];

export const parsePositiveInt = (value) => {
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : undefined;
};

/**
 * Split the leading runtime options (-XX:..., -Xmx...) off the atom arguments.
 *
 * @param {string[]} atomArgs Arguments given to the dispatcher
 * @returns {{runtimeOptions: string[], programArgs: string[]}}
 */
export function splitRuntimeOptions(atomArgs) {
  let i = 0;
  while (
    i < atomArgs.length &&
    RUNTIME_OPTION_PREFIXES.some((prefix) =>
      String(atomArgs[i]).startsWith(prefix)
    )
  ) {
    i++;
  }
  return {
    runtimeOptions: atomArgs.slice(0, i),
    programArgs: atomArgs.slice(i)
  };
}

/**
 * True while `pid` names a live process. EPERM means it exists but belongs to someone else.
 */
function isProcessAlive(pid) {
  try {
    process.kill(pid, 0);
    return true;
  } catch (e) {
    return e.code === "EPERM";
  }
}

const defaultProbe = { isProcessAlive, ppid: () => process.ppid, isWin };

/**
 * Watch the supervisor this dispatcher must not outlive, named by ATOM_PARENT_PID.
 *
 * cdxgen sets ATOM_PARENT_PID to its own pid. If cdxgen is killed outright while atom runs, the
 * dispatcher would otherwise keep waiting on the runtime, and the runtime would keep running for
 * nobody.
 *
 * @param {Object} [env] Environment to read ATOM_PARENT_PID from
 * @param {{isProcessAlive: (pid: number) => boolean, ppid: () => number, isWin: boolean}} [probe]
 *   Process probes, replaceable in tests
 * @returns {{pid: number, isGone: () => boolean}|undefined} undefined when no supervisor is named
 */
export function supervisorWatch(env = process.env, probe = defaultProbe) {
  const pid = parsePositiveInt(env.ATOM_PARENT_PID);
  if (!pid) {
    return undefined;
  }
  const initialParent = probe.ppid();
  return {
    pid,
    // Gone when the named process has exited, or when our own parent has. On POSIX an orphan is
    // adopted, so its parent pid changes, which also catches a supervisor that died but is still
    // a zombie nobody has reaped. Windows does not reparent, so there the parent is probed too.
    isGone: () =>
      !probe.isProcessAlive(pid) ||
      (probe.isWin
        ? initialParent !== pid && !probe.isProcessAlive(initialParent)
        : probe.ppid() !== initialParent)
  };
}

/**
 * Run the atom runtime (the native binary, or java) as a supervised child, then exit with its
 * status. The dispatcher only relays, so it must never leave the runtime behind:
 *
 * - signals sent to the dispatcher are forwarded, and it keeps waiting until the runtime exits;
 *   a second signal kills the runtime;
 * - ATOM_TIMEOUT (milliseconds) stops the runtime (SIGTERM, then SIGKILL after
 *   ATOM_KILL_GRACE_MS, default 10 s) and exits with TIMEOUT_EXIT_CODE;
 * - when the supervisor named by ATOM_PARENT_PID goes away, the runtime is stopped the same way;
 * - the runtime receives ATOM_PARENT_PID set to the dispatcher's pid, so it exits on its own when
 *   the dispatcher is killed outright with a signal that cannot be forwarded (SIGKILL).
 *
 * @param {string} command Runtime executable
 * @param {string[]} args Runtime arguments
 * @param {Object} env Runtime environment
 * @param {string} cwd Working directory
 */
export function superviseRuntime(command, args, env, cwd) {
  const timeoutMs = parsePositiveInt(process.env.ATOM_TIMEOUT);
  const killGraceMs = parsePositiveInt(process.env.ATOM_KILL_GRACE_MS) || 10000;
  const supervisor = supervisorWatch();
  if (supervisor?.isGone()) {
    console.error(
      `atom: supervising process ${supervisor.pid} is gone; not starting.`
    );
    process.exit(1);
  }
  const child = spawn(command, args, {
    env: { ...env, ATOM_PARENT_PID: String(process.pid) },
    cwd,
    stdio: "inherit",
    windowsHide: true
  });
  let stopReason;
  let killTimer;
  const timers = [];
  const stop = (reason) => {
    if (stopReason) {
      return;
    }
    stopReason = reason;
    child.kill("SIGTERM");
    killTimer = setTimeout(() => child.kill("SIGKILL"), killGraceMs);
  };
  // The first signal is relayed so the runtime can stop cleanly; a second one means the caller
  // has stopped waiting for that, and the runtime is killed.
  let signalsReceived = 0;
  const forwarders = FORWARDED_SIGNALS.map((signal) => {
    const forward = () => {
      signalsReceived++;
      child.kill(signalsReceived > 1 ? "SIGKILL" : signal);
    };
    process.on(signal, forward);
    return [signal, forward];
  });
  if (timeoutMs) {
    timers.push(setTimeout(() => stop("timeout"), timeoutMs));
  }
  if (supervisor) {
    timers.push(
      setInterval(() => {
        if (supervisor.isGone()) {
          stop("supervisor");
        }
      }, PARENT_POLL_MS)
    );
  }
  const cleanup = () => {
    timers.forEach((t) => clearTimeout(t));
    if (killTimer) {
      clearTimeout(killTimer);
    }
    forwarders.forEach(([signal, forward]) => process.off(signal, forward));
  };
  child.on("error", (err) => {
    cleanup();
    console.error(`atom: unable to start ${command}: ${err.message}`);
    process.exit(1);
  });
  child.on("exit", (code, signal) => {
    cleanup();
    if (stopReason === "timeout") {
      console.error(
        `atom: stopped after exceeding ATOM_TIMEOUT (${timeoutMs} ms).`
      );
      process.exit(TIMEOUT_EXIT_CODE);
    }
    if (stopReason === "supervisor") {
      console.error(
        `atom: stopped because supervising process ${supervisor.pid} is gone.`
      );
    }
    if (code !== null) {
      process.exit(code);
    }
    // The runtime died from a signal: report it the way a shell does.
    process.exit(128 + (osConstants.signals[signal] || 0));
  });
}
