#include <extdir/exth.h>
#include "extdir/exth2.h"
#include "wrap.h"
int direct(int x) { return ext_func(x); }
int quoted(int x) { return ext2_func(x); }
int chained(int x) { return other_func(x); }
