#include "libdir/lib.h"
#include "proj.h"
int same_name(void) { return 1; }
static int use_all(int a, int b) {
  int v = LIB_VERSION;
  int m = LIB_MAX(a, b);
  int w = LIB_WRAP(a);
  int d = PROJ_DOUBLE(a);
  int g = lib_global;
  int e = LIB_BAD;
  int p = PROJ_ONE;
  int pg = proj_global;
  struct lib_struct ls;
  ls.field_a = 1;
  struct proj_struct ps;
  ps.field_a = 2;
  int (*fp)(int) = lib_func;
  int c = lib_func(v + m + w + d);
  int h = proj_helper(g + e + p + pg + ls.field_a + ps.field_a + fp(1));
  int n = LIB_OUTER(a);
  int q = LIB_MAX(a, LIB_VERSION);
  int r = PROJ_CALL(a);
  int s = PROJ_DOUBLE(LIB_MAX(a, 1));
  int sn = same_name();
  return h + n + q + r + s + sn;
}
int main(void) { return use_all(1, 2); }
