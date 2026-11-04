#include "shared.h"
int use_b(int b) { return LIB_MAX(b, 1) + shared_twice(b); }
