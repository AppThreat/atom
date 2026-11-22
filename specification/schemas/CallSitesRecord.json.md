# JSON Schema

## Definitions

- <a id="definitions/CallSitesRecord"></a>**`CallSitesRecord`** *(object)*: One record per external use site of an imported module, class, function or header: where the use is, what is used and how the module was found. One entry of the callsites array of the callsites slice. Cannot contain additional properties. Every record carries `file`, `line`, `col`, `method`, `module`, `path`, `kind`, `via`, `resolvedMethod`, `builtin` and `declaringModule`; the others are left out when they have no value.
  - <a id="definitions/CallSitesRecord/properties/file"></a>**`file`** *(string)*: File of the use, relative to the input (for bytecode, the source file the class names).
  - <a id="definitions/CallSitesRecord/properties/line"></a>**`line`** *(integer)*: Line of the use itself.
  - <a id="definitions/CallSitesRecord/properties/col"></a>**`col`** *(integer)*: Column of the use itself, -1 where the frontend has no column.
  - <a id="definitions/CallSitesRecord/properties/method"></a>**`method`** *(string)*: The enclosing method's full name, empty where the use sits outside any method.
  - <a id="definitions/CallSitesRecord/properties/module"></a>**`module`** *(string)*: What the use belongs to: an npm package, a Python module, a Java class, a PHP class, a Ruby constant, a C header.
  - <a id="definitions/CallSitesRecord/properties/path"></a>**`path`** *(string)*: What is used inside the module: an export, a member, a method, a macro, a field (empty for a use of the module itself).
  - <a id="definitions/CallSitesRecord/properties/kind"></a>**`kind`** *(string)*: What the use is: call, new, jsx, arg, ref, decorator, extends, implements, tag, import, decltype, annotation, attribute, methodref, macro or type.
  - <a id="definitions/CallSitesRecord/properties/via"></a>**`via`** *(string)*: How the module was found: binding, type, name or macro.
  - <a id="definitions/CallSitesRecord/properties/resolvedMethod"></a>**`resolvedMethod`** *(string)*: The callee's full name when the frontend resolved one, else empty; its spelling is the frontend's, so do not parse it.
  - <a id="definitions/CallSitesRecord/properties/builtin"></a>**`builtin`** *(boolean)*: True for the language's standard library.
  - <a id="definitions/CallSitesRecord/properties/declaringModule"></a>**`declaringModule`** *(string)*: The class that declares a called method when it differs from the receiver's static type; Java source only.
  - <a id="definitions/CallSitesRecord/properties/purl"></a>**`purl`** *(string)*: The package URL of the distribution the module belongs to, when atom knows one; left out while empty.
  - <a id="definitions/CallSitesRecord/properties/resolvedPath"></a>**`resolvedPath`** *(string)*: C and C++ only: the file the include of the record's header resolved to; left out when the record's own file did not include the header itself.
  - <a id="definitions/CallSitesRecord/properties/isSystem"></a>**`isSystem`** *(boolean)*: C and C++ only: true for a system include (<...>); present only when true.
