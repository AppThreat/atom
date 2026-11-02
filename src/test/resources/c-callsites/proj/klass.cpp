#include "libdir/libhpp.h"
#include "proj.h"
int use_cpp(int x) {
  LibClass obj(x);
  ProjSub sub(x);
  LibClass *p = &sub;
  int v = sub.value();
  int w = p->mem;
  LibClass *heap = new LibClass(v);
  return v + w + heap->value() + sub.own;
}
