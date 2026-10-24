import createDebug from "debug";
const d1 = createDebug("a:1");
const d2 = createDebug("a:2");
export function one() {
  d1("one");
  d1("one again");
  return createDebug("a:3");
}
export function two() {
  d2("two");
  return createDebug("a:4");
}
export const three = () => createDebug("a:5");
export class K {
  m() {
    return createDebug("a:6");
  }
  static s() {
    return createDebug("a:7");
  }
}
