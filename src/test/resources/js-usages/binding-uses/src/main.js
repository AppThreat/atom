import { readFileSync } from "node:fs";
import path from "node:path";
import process from "node:process";
import { flatten } from "lodash";
import debugLib from "debug";

const f = flatten;
export function viaAlias(a) {
  return f(a);
}
export function chains() {
  process.stdout.write("x");
  return readFileSync(path.join("a", path.sep));
}
export class K {
  run() {
    const inner = () => readFileSync("y");
    return inner();
  }
}
export function shadow() {
  const debugLib = (x) => x;
  return debugLib("not the import");
}
export const d = debugLib("app");
