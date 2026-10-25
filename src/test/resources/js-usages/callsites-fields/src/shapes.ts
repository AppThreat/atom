import dayjs from "dayjs";
import { debounce, throttle, chunk, uniq } from "lodash-es";
import { inspect } from "node:util";

export class Shapes {
  static dayjs = dayjs;
  #debounce = debounce;
  private renamed = throttle;
  "chunk" = chunk;
  [inspect.custom] = () => "Shapes";
  createdAt = dayjs();
  onScroll = () => debounce(() => 1, 10);

  later() {
    return dayjs().add(1, "day");
  }

  static parse(text: string) {
    return Shapes.dayjs(text);
  }

  wait() {
    return [this.#debounce(() => 3, 1), this.renamed(() => 4, 1), this.chunk([1, 2], 1)];
  }

  flush() {
    return debounce(() => 2, 5);
  }

  pairs(xs: number[]) {
    return chunk(uniq(xs), 2);
  }
}
