#ifndef PROJ_H
#define PROJ_H
#define PROJ_DOUBLE(x) ((x) * 2)
#define PROJ_CALL(x) lib_func(x)
enum proj_enum { PROJ_ONE = 1 };
extern int proj_global;
struct proj_struct { int field_a; };
int proj_helper(int x);
#ifdef __cplusplus
class ProjClass { public: ProjClass(int v); int value() const; int mem; };
struct ProjSub : LibClass {
  ProjSub(int v);
  int own;
};
#endif
#endif
