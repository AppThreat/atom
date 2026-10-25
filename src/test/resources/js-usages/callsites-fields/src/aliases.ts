import { Subject, BehaviorSubject } from "rxjs";
import { uniq, chunk } from "lodash-es";

export class Base {
  readonly Subject = Subject;
  mutable = uniq;

  make() {
    return new this.Subject<string>();
  }

  reset() {
    this.mutable = chunk;
    return this.mutable([1]);
  }
}

export class Sub extends Base {
  latest = BehaviorSubject;

  again() {
    const s = new this.Subject<number>();
    return [s, new this.latest(0)];
  }
}
