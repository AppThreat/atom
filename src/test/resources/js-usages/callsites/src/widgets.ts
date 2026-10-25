import { Subject } from 'rxjs';

export class WithField {
  readonly Subject = Subject;

  make() {
    return new Subject<number>();
  }
}

export class WithoutField {
  make() {
    return new Subject<number>();
  }
}
