import { signal } from "@angular/core";

export const label = "second";

export function second() {
  return signal(2);
}
