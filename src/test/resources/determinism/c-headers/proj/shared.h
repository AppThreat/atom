#ifndef SHARED_H
#define SHARED_H
#include "libdir/lib.h"
#define SHARED_TWICE(x) LIB_MAX((x), (x) * 2)
static inline int shared_twice(int x) {
  return SHARED_TWICE(x) + lib_func(x);
}
#endif
