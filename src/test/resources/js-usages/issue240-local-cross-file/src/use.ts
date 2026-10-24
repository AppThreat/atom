import { make } from "./make";

export function useIt() {
  const a = make(1);
  const b = make(2);
  return [a, b];
}
