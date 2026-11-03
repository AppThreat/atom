#ifndef LIB_H
#define LIB_H
#define LIB_VERSION 3
#define LIB_MAX(a, b) ((a) > (b) ? (a) : (b))
#define LIB_WRAP(x) lib_func(x)
#define LIB_INNER(x) ((x) + 1)
#define LIB_OUTER(x) LIB_INNER(x) * 2
extern int lib_global;
enum lib_errno { LIB_OK = 0, LIB_BAD = 1 };
struct lib_struct { int field_a; int field_b; };
int lib_func(int x);
int same_name(void);
#endif
