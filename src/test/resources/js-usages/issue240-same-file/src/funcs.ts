import { signal, computed } from "@angular/core";

export function a() {
  return signal(1);
}

export function b() {
  const s = signal(2);
  return computed(() => s() * 2);
}

export function c() {
  return signal(3);
}
