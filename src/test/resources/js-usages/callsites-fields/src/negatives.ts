import { of, from, interval, timer, Subject, BehaviorSubject } from "rxjs";

export class Brackets {
  g = of;
  reset() { this["g"] = from; }
  use() { return this.g(1); }
}

export class Nullish {
  k = timer;
  init() { this.k ??= interval; }
  use() { return this.k(1); }
}

export class Destructured {
  h = from;
  load(x) { ({ a: this.h } = x); }
  use() { return this.h([1]); }
}

export class Assigned {
  q = Subject;
  constructor(x) { Object.assign(this, x); }
  make() { return new this.q(); }
}

export class Stat {
  static S = Subject;
  inst() { return this.S; }
  static make() { return new this.S(); }
}

export class Base2 {
  f = Subject;
  make() { return new this.f(); }
}

export class Sub2 extends Base2 {
  constructor() { super(); this.f = BehaviorSubject; }
}
