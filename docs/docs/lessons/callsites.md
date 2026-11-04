# Lesson 9: Call-Site Slicing (`callsites`)

### Learning Objective

Write one record per use of an imported module, class or header, at the position of the use
itself, for every language atom reads: JavaScript and TypeScript, Python, Java source, PHP, Ruby,
JVM bytecode (jar, class directory, dex, APK) and C and C++. Each record says what was used, how
it was used and how atom found out.

### Pre-requisites

- **JDK 23+** and a built `atom`, plus the tool the language's frontend needs: `astgen` 2.x for
  JavaScript, TypeScript and Flow, `php` for PHP, `rbastgen` 2.x for Ruby. The Python, Java,
  bytecode and C frontends run inside atom. C and C++ can use the EDG frontend through `edga`
  (`--c-frontend edg`, see [C and C++ frontends](../c-frontends.md)).
- A project in one of those languages, or an atom built from it (`--reuse-atom`).
- For the examples below, a checkout of this repository: they run on the fixtures under
  `src/test/resources`. Copy a fixture before you run on it, so the frontend writes nothing into
  the repository.

### Conceptual Background

[`usages`](usages.md) is object-shaped: an entry per declaration, with the calls made on it and the
calls it is passed to. `callsites` is use-shaped: one `CallSiteRecord` per place where code outside
the project is used, so that a record can become an SBOM `evidence.occurrences` entry without
interpretation. The slice is `{"callsites": [ ... ]}`. A source writes a record only when the
frontend gave it a position; the console reports the uses it had to skip
(`Call sites: skipped N node(s) that failed and M node(s) with no method`).

Every record has these fields:

| field             | meaning                                                                                       |
|-------------------|-----------------------------------------------------------------------------------------------|
| `file`            | file of the use, relative to the input (for bytecode, the source file the class names)         |
| `line`, `col`     | position of the use itself; `col` is `-1` where the frontend has no column                      |
| `method`          | the enclosing method's full name, empty where the use sits outside any method                  |
| `module`          | what the use belongs to: an npm package, a Python module, a Java class, a PHP class, a Ruby constant, a C header |
| `path`            | what is used inside the module: an export, a member, a method, a macro, a field (empty for a use of the module itself) |
| `kind`            | what the use is (per language below)                                                          |
| `via`             | how the module was found: `binding`, `type`, `name` or `macro`                                |
| `resolvedMethod`  | the callee's full name when the frontend resolved one, else empty; its spelling is the frontend's, so do not parse it |
| `builtin`         | `true` for the language's standard library (see "Builtin" below)                              |
| `declaringModule` | the class that declares a called method when it differs from the receiver's static type; Java source only |
| `purl`            | the package URL of the distribution the module belongs to, when atom knows one; left out while empty |

`via` is the resolution story of the record:

- `binding`: the name resolves, scope-correctly, to an import, a static member or a declaration the
  frontend linked (an imported binding in JavaScript and Python, a class in Java, a header in C).
- `type`: the call is attributed through the static type of its receiver (`res.status()` on a typed
  express response, an instance method on a Java object, a member of a C struct).
- `name`: the frontend links no node to the use, so the name is matched against the file's imports
  (JavaScript decorators, heritage, tagged templates and JSX tags; Python decorators, base classes
  and annotations).
- `macro`: a C or C++ macro of a header outside the project, used at its invocation site.

A record is unique by file, line, column and module (C and JVM bytecode also by path where two
members share a site); when one site carries several records the most specific kind wins, in the order `call`,
`new`, `jsx`, `tag`, `decorator`, `arg`, `extends` or `implements`, `decltype`, `ref`, `import`,
and the result is sorted by file, line, column, module, path and kind
([`CallSiteRecord.scala`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/callsites/CallSiteRecord.scala)).
The slice is the same bytes for the same input, however the frontend numbered its nodes.

#### Builtin

`builtin` is decided by where the thing resolved, never by its name:

- **Python**: the top-level package is in the committed standard library list.
- **Java source**: the class's package is an exported JDK package (`java.*`, `javax.xml` and so on;
  `javax.servlet` is not).
- **PHP**: the class or function is in the list generated from a PHP binary.
- **Ruby**: the longest listed prefix of the constant is a core or standard library class.
- **JVM bytecode**: the owner is a JDK or Android platform package, a compiler support class
  (`kotlin.jvm.internal.`, `scala.runtime.`, lambda and synthetic classes) or one of a small set of
  Kotlin annotations.
- **C and C++**: the header resolved into an SDK or sysroot, a compiler's own include directory, the
  C++ standard library, or the system include directory with a name in the libc header list. A
  header under the input root or under the build directories of the compilation database is the
  project's own and gives no record.
- **JavaScript and TypeScript**: `builtin` is never set. A Node built-in such as `fs` is a record
  with `module` `fs` and `builtin` false, and no purl.

#### Purl

A purl is set only when it is unambiguous; otherwise the field is absent.

- **JavaScript, TypeScript**: from a BOM in the atom (`--bom-file` or `--bom-dir`), one purl per
  npm package; none for Node built-ins, path aliases, packages with several versions or packages
  the BOM lacks.
- **Python**: from the `ImportedModules` property of a BOM component, or from the index of
  `python-deps`; a module claimed by two purls stays unmapped.
- **Java source**: the Maven artifact whose jar holds the class, from the jars the frontend
  recorded in `java-classpath.json` and the `META-INF/maven` metadata in them.
- **PHP**: Composer's `installed.json` and class maps: `pkg:composer/<name>@<version>`.
- **Ruby**: `Gemfile.lock` and the gem sources found through Bundler's path: `pkg:gem/<name>@<version>`.
- **JVM bytecode**: the artifact that holds the class in the local Gradle or Maven cache, or the
  `META-INF` metadata of the input; nothing is downloaded.
- **C and C++**: only for a header that a BOM component's location names (below).

### JavaScript and TypeScript

```bash
atom callsites -l javascript -o app.atom -s callsites.json /path/to/project
atom callsites -l javascript --reuse-atom -o app.atom -s callsites.json /path/to/project
```

Kinds: `call`, `new`, `jsx`, `arg`, `ref`, `decorator`, `extends`, `implements`, `tag`, `import`.
The module is a bare specifier (`lodash`, `@angular/common/http`, `node:fs`); relative specifiers are
the project's own code unless they lead into vendored code. The sources are the four passes of
[`JsCallSiteSource`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/callsites/JsCallSiteSource.scala):

1. Identifiers that resolve, through closure captures and the frontend's import lowering, to an
   imported binding (`via` `binding`). A local assigned more than once has no origin. A class field
   written once with such a binding is an alias, so its reads are uses, in Angular templates too.
2. Calls whose resolved name starts with a module the project imports (`via` `type`). Calls whose
   member is one of sixteen string and array methods (`includes`, `replace`, `split`, and so on)
   are dropped, because type recovery puts module prefixes on plain strings.
3. Decorators, heritage clauses, tagged templates and JSX tags (`via` `name`).
4. Imports that bind nothing (`via` `binding`, kind `import`): the import runs the module, so
   `import 'y'`, a bare `require('y')` statement and a re-export (`export * from 'y'`) are one
   record each at the statement. An import that binds a name gives a record only where the name is
   used.

<!-- command: javascript -->
```bash
cp -R src/test/resources/js-usages/callsites /tmp/ex-javascript
atom callsites --cache none -l javascript -o /tmp/ex-javascript.atom -s /tmp/ex-javascript.json /tmp/ex-javascript
```

<!-- output: javascript -->
```json
{"file":"src/app.js","line":12,"col":21,"method":"src/app.js::program:list","module":"@aws-sdk/client-s3","path":"S3Client","kind":"new","via":"binding","resolvedMethod":"","builtin":false,"declaringModule":""}
{"file":"src/app.js","line":13,"col":21,"method":"src/app.js::program:list","module":"jsonwebtoken","path":"sign","kind":"call","via":"binding","resolvedMethod":"jsonwebtoken:jwt:sign","builtin":false,"declaringModule":""}
{"file":"src/app.js","line":16,"col":-1,"method":"src/app.js::program","module":"sequelize","path":"Model","kind":"extends","via":"name","resolvedMethod":"sequelize.js::program:Model","builtin":false,"declaringModule":""}
{"file":"src/ui.jsx","line":1,"col":0,"method":"src/ui.jsx::program","module":"core-js/stable","path":"","kind":"import","via":"binding","resolvedMethod":"","builtin":false,"declaringModule":""}
{"file":"src/ui.jsx","line":4,"col":25,"method":"src/ui.jsx::program:App","module":"antd","path":"Button","kind":"jsx","via":"name","resolvedMethod":"","builtin":false,"declaringModule":""}
```

### Python

Kinds: `call`, `arg`, `ref`, `decorator`, `extends`, `decltype`. A name bound by an `import` to a
module that is not part of the project is a use wherever it is read: called, passed, referenced
(including in `except` clauses), or used through a typed receiver (`via` `type`). Base classes
and annotations are `extends` and `decltype` records with `via` `name`. A decorator is a
`decorator` record with `via` `name` when its base is the imported name, and with `via` `type` when
the base is read through the type of an identifier. Relative imports and
imports the frontend resolved to a project file are project code and give no record. A local that
is assigned more than once gets no origin.

<!-- command: python -->
```bash
cp -R src/test/resources/py-usages/callsites /tmp/ex-python
atom callsites --cache none -l python -o /tmp/ex-python.atom -s /tmp/ex-python.json /tmp/ex-python
```

<!-- output: python -->
```json
{"file":"exceptions.py","line":6,"col":16,"method":"exceptions.guarded","module":"requests","path":"get","kind":"call","via":"binding","resolvedMethod":"requests.get","builtin":false,"declaringModule":""}
{"file":"heritage.py","line":4,"col":-1,"method":"heritage","module":"click","path":"Command","kind":"extends","via":"name","resolvedMethod":"click.Command","builtin":false,"declaringModule":""}
{"file":"imports.py","line":7,"col":7,"method":"imports","module":"os","path":"getcwd","kind":"call","via":"binding","resolvedMethod":"os.getcwd","builtin":true,"declaringModule":""}
{"file":"imports.py","line":9,"col":7,"method":"imports","module":"numpy","path":"array","kind":"call","via":"binding","resolvedMethod":"numpy.array","builtin":false,"declaringModule":""}
```

### Java source

Kinds: `call`, `new`, `annotation`, `extends`, `implements`, `ref`, `methodref` and `decltype`. A record is a reference whose class is not declared in the project: calls and
constructors, annotations, base types, static fields, enum constants and class literals, method
references, and the types of parameters, fields, locals, return values, casts, `instanceof` and
`throws`. An instance call is `via` `type` and takes the receiver's static type as `module`; when
the method is declared by another class, `declaringModule` names it. Static calls, constructors
and every other kind are `via` `binding`. Lombok-generated members count. The purls come from the
jars the frontend recorded in `java-classpath.json`.

<!-- command: java -->
```bash
cp -R src/test/resources/java-callsites/src /tmp/ex-java
atom callsites --cache none -l java -o /tmp/ex-java.atom -s /tmp/ex-java.json /tmp/ex-java
```

<!-- output: java -->
```json
{"file":"proj/Calls.java","line":9,"col":9,"method":"proj.Calls.call:void()","module":"web.Security","path":"login","kind":"call","via":"type","resolvedMethod":"web.Security.login:<unresolvedSignature>(1)","builtin":false,"declaringModule":""}
{"file":"proj/Annotations.java","line":13,"col":15,"method":"proj.Annotations.handle:void(java.lang.String)","module":"web.RequestParam","path":"","kind":"annotation","via":"binding","resolvedMethod":"web.RequestParam","builtin":false,"declaringModule":""}
{"file":"proj/Annotations.java","line":8,"col":3,"method":"proj.Annotations.toString:java.lang.String()","module":"java.lang.Override","path":"","kind":"annotation","via":"binding","resolvedMethod":"java.lang.Override","builtin":true,"declaringModule":""}
{"file":"proj/DeclTypes.java","line":11,"col":31,"method":"proj.DeclTypes.load:java.util.List(java.util.Map)","module":"java.util.List","path":"of","kind":"call","via":"binding","resolvedMethod":"java.util.List.of:java.util.List(java.lang.Object)","builtin":true,"declaringModule":""}
```

### PHP

Kinds: `call`, `new`, `ref`, `attribute`, `decltype` and `extends` (which also covers `implements`
and trait `use`). A use is a reference to a class or function that the project does not declare:
`Foo::bar()`, `new Foo`, `Foo::class`, `instanceof`, `catch`, attributes, and the declared types of
parameters, returns, properties and constants. Static and namespaced function calls are
`binding`; instance calls are `type`. `self`, `static`, `parent`, language constructs lowered to
calls (`echo`, `isset`, `include`) and the project's own classes and functions give nothing. The
`vendor/` directory of Composer is dependency code and is not parsed. Composer's metadata decides
what is external and gives the purl; without it, every class the project does not declare counts
as external.

<!-- command: php -->
```bash
cp -R src/test/resources/php-callsites /tmp/ex-php
atom callsites --cache none -l php -o /tmp/ex-php.atom -s /tmp/ex-php.json /tmp/ex-php
```

<!-- output: php -->
```json
{"file":"src/Shapes.php","line":15,"col":21,"method":"src/Shapes.php:<global>","module":"Acme\\Widgets\\Widget","path":"","kind":"extends","via":"binding","resolvedMethod":"Acme\\Widgets\\Widget","builtin":false,"declaringModule":"","purl":"pkg:composer/acme/widgets@1.2.0"}
{"file":"src/Shapes.php","line":15,"col":39,"method":"src/Shapes.php:<global>","module":"Countable","path":"","kind":"extends","via":"binding","resolvedMethod":"Countable","builtin":true,"declaringModule":""}
{"file":"src/Shapes.php","line":14,"col":2,"method":"src/Shapes.php:<global>","module":"Acme\\Widgets\\Mark","path":"","kind":"attribute","via":"binding","resolvedMethod":"Acme\\Widgets\\Mark","builtin":false,"declaringModule":"","purl":"pkg:composer/acme/widgets@1.2.0"}
```

### Ruby

Kinds: `call`, `new`, `ref`, `extends`. Every constant reference to a class or module that is not
the project's own is a use: a superclass or a `include`, `extend` or `prepend` is `extends`;
`Const.method` is a `call` and `Const.new` a `new`; `Const[...]` is a `call` with path `[]`; any
other mention is a `ref`. The written constant is resolved against the enclosing namespaces: the
first candidate that core Ruby or an installed gem defines wins. A local bound once to
`Foo.new`, where `Foo` is a core class, gives `via` `type` records for the calls on it. Gem classes are never parsed, so
typed calls on gem objects give no record.

<!-- command: ruby -->
```bash
cp -R src/test/resources/ruby-callsites /tmp/ex-ruby
atom callsites --cache none -l ruby -o /tmp/ex-ruby.atom -s /tmp/ex-ruby.json /tmp/ex-ruby
```

<!-- output: ruby -->
```json
{"file":"lib/shapes.rb","line":7,"col":19,"method":"lib/shapes.rb:<main>","module":"Faraday::Middleware","path":"","kind":"extends","via":"binding","resolvedMethod":"Faraday.Middleware","builtin":false,"declaringModule":"","purl":"pkg:gem/faraday@2.9.0"}
{"file":"lib/reopened.rb","line":9,"col":17,"method":"lib/reopened.rb:<main>","module":"ActiveRecord::Migration","path":"[]","kind":"call","via":"binding","resolvedMethod":"ActiveRecord.Migration","builtin":false,"declaringModule":""}
{"file":"lib/reopened.rb","line":17,"col":4,"method":"lib/reopened.rb:<main>.Slots.current","module":"Thread","path":"current","kind":"call","via":"binding","resolvedMethod":"Thread","builtin":true,"declaringModule":""}
```

### JVM bytecode

Inputs: a jar, a class directory, a `.dex` file or an Android APK (`-l jar`, `-l dex`, `-l apk`;
`-l android` and `-l jimple` work as well). Kinds: `call`, `new`, `ref`; `ref` is a field read or
write, a cast, an `instanceof`, a class literal, a `super(...)` call or a method handle. `via` is
`type` for a virtual, interface or special call and `binding` otherwise. A record is one
instruction of an application class that touches a class outside the application, and `col` is
always `-1`. Declaration types and annotations are not instructions and are not recorded.

What counts as the application is the **boundary**, chosen in this order: `--app-prefix` (repeat
it), the input's own names (an APK's manifest package, a jar's `Main-Class`, `Automatic-Module-Name` or
`Bundle-SymbolicName`, every class of a class directory), the BOM's root component group, then the
package prefixes that hold at least a tenth of the jar's classes. A bare `.dex` has no name to read, so
it needs `--app-prefix`. Sibling packages outside the boundary are dependencies. The slice has a
`metadata` object beside the records: the boundary prefixes and where they came from
(`boundarySource`), and how many line tables the compiler synthesised (`lineQuality`).

`--android-sdk` and `--jdk-path` give the frontend the platform classes. The jar below is built from
the fixture sources:

<!-- command: jar -->
```bash
javac -d /tmp/ex-jar-classes $(find src/test/resources/jvm-callsites/src -name '*.java')
cp -R src/test/resources/jvm-callsites/meta/* /tmp/ex-jar-classes/
printf 'Manifest-Version: 1.0\nAutomatic-Module-Name: me.app\n' > /tmp/ex-jar-manifest.txt
jar cfm /tmp/ex-jar.jar /tmp/ex-jar-manifest.txt -C /tmp/ex-jar-classes .
atom callsites --cache none -l jar --jdk-path $JAVA_HOME -o /tmp/ex-jar.atom -s /tmp/ex-jar.json /tmp/ex-jar.jar
```

<!-- output: jar -->
```json
{"file":"me/app/App.java","line":11,"col":-1,"method":"me.app.App.<init>:void()","module":"me.lib.Lib","path":"<init>","kind":"new","via":"binding","resolvedMethod":"me.lib.Lib.<init>:void(java.lang.String)","builtin":false,"declaringModule":"","purl":"pkg:maven/me.lib/lib-lib@0.1.0"}
{"file":"me/app/App.java","line":14,"col":-1,"method":"me.app.App.viaLib:java.lang.String()","module":"me.lib.Lib","path":"label","kind":"call","via":"type","resolvedMethod":"me.lib.Lib.label:java.lang.String()","builtin":false,"declaringModule":"","purl":"pkg:maven/me.lib/lib-lib@0.1.0"}
{"file":"me/app/App.java","line":18,"col":-1,"method":"me.app.App.viaStatic:java.lang.String()","module":"me.lib.Lib","path":"util","kind":"call","via":"binding","resolvedMethod":"me.lib.Lib.util:int(int)","builtin":false,"declaringModule":"","purl":"pkg:maven/me.lib/lib-lib@0.1.0"}
{"appPrefixes":["me.app"],"appAll":false,"boundarySource":"jar-manifest","syntheticLineTables":0,"methodsWithLineTables":0,"lineQuality":"source","sourceFileGuesses":0}
```

The committed dex fixture has no manifest, so the boundary is the prefix you give:

<!-- command: dex -->
```bash
cp src/test/resources/jvm-callsites/dex/classes.dex /tmp/ex-dex.dex
atom callsites --cache none -l dex --jdk-path $JAVA_HOME --app-prefix dexy.app -o /tmp/ex-dex.atom -s /tmp/ex-dex.json /tmp/ex-dex.dex
```

<!-- output: dex -->
```json
{"file":"dexy/app/Dexy.java","line":3,"col":-1,"method":"dexy.app.Dexy.<init>:void()","module":"java.lang.Object","path":"<init>","kind":"ref","via":"binding","resolvedMethod":"java.lang.Object.<init>:void()","builtin":true,"declaringModule":""}
{"file":"dexy/app/Dexy.java","line":6,"col":-1,"method":"dexy.app.Dexy.check:boolean(java.lang.String)","module":"java.lang.String","path":"isEmpty","kind":"call","via":"type","resolvedMethod":"java.lang.String.isEmpty:boolean()","builtin":true,"declaringModule":""}
{"appPrefixes":["dexy.app"],"appAll":false,"boundarySource":"app-prefix","syntheticLineTables":0,"methodsWithLineTables":0,"lineQuality":"source","sourceFileGuesses":0}
```

No APK is committed as a fixture, so there is no runnable APK example; the command has the same
shape with `-l apk --android-sdk $ANDROID_HOME/platforms/android-35/android.jar app.apk`. A
manifest gives the boundary, and `callsites-app-boundary.json` in the atom keeps what was found.

### C and C++

Kinds: `call`, `new` (a constructor call or `new T`), `ref` (a global, an enumerator, a function
named as a value), `macro` and `type` (a member read through a header's struct or class, with the
member as `path`). A record is a reference from project code to something that a header outside the
project declares or defines. The `module` is the header as the include root spells it
(`openssl/ssl.h`). `via` is `type` for a member call, `macro` for macros and `binding` otherwise.
Macros are recorded at their invocation, a macro nested in another at the outermost invocation, and
a library call inside a project macro's body at the macro's definition; nothing a library macro's
body expands to is a separate use. The project is the input root and the build directories of the
compilation database (`--compile-commands`), kept in `callsites-c-layout.json` in the atom, so a
header's place decides, not its name. Both frontends work (`--c-frontend cdt|edg`); EDG also reads
the macro uses from the IL.

<!-- command: c -->
```bash
cp -R src/test/resources/c-callsites /tmp/ex-c
atom callsites --cache none -l c --include-path /tmp/ex-c/externlib -o /tmp/ex-c.atom -s /tmp/ex-c.json /tmp/ex-c/proj
```

<!-- output: c -->
```json
{"file":"main.c","line":18,"col":11,"method":"use_all","module":"lib.h","path":"lib_func","kind":"call","via":"binding","resolvedMethod":"lib_func","builtin":false,"declaringModule":""}
{"file":"main.c","line":14,"col":3,"method":"use_all","module":"lib.h","path":"field_a","kind":"type","via":"type","resolvedMethod":"<operator>.fieldAccess","builtin":false,"declaringModule":""}
{"file":"main.c","line":9,"col":11,"method":"use_all","module":"lib.h","path":"lib_global","kind":"ref","via":"binding","resolvedMethod":"lib_global","builtin":false,"declaringModule":""}
{"file":"klass.cpp","line":4,"col":12,"method":"use_cpp:int(int)","module":"libhpp.h","path":"LibClass","kind":"new","via":"binding","resolvedMethod":"LibClass","builtin":false,"declaringModule":""}
```

### Vendored code and `--external-paths`

Code that lives in the project but belongs to someone else (`third_party/`, a copied single-header
library) is project code to a source frontend, so a call into it gives no record. Two things mark
it as external, for every language except JVM bytecode:

- `--external-paths <regex>`: files whose path from the input root matches, with `/` as the
  separator and a match that starts at a path segment, are parsed as always but marked external.
  The record's `module` is the vendored directory. `third_party` does not match
  `src/not_third_party`.
- a BOM (`--bom-file` or `--bom-dir`) whose components name their location with the property
  `cdx:vendored:path` or `cdx:cmake:sourceDir`. The record's `module` is the component's name and
  its `purl` the component's. A path that two different purls claim is dropped.

With either, the records inside the vendored files are dropped, and the uses of them from the
project are added: a call whose target is a vendored method (kind `new` for a constructor, else
`call`) and a project class that inherits a vendored class (`extends`), both `via` `binding`. A
pattern that names nothing leaves the slice byte-identical to a run without it. The pattern and the
components are stored in the atom (`callsites-vendored.json`, and `callsites-c-layout.json` for C),
so a later `callsites` run on the atom needs no option.

<!-- command: vendored-javascript -->
```bash
cp -R src/test/resources/vendored/javascript /tmp/ex-vendored-javascript
atom callsites --cache none -l javascript --external-paths third_party -o /tmp/ex-vendored-javascript.atom -s /tmp/ex-vendored-javascript.json /tmp/ex-vendored-javascript
```

<!-- output: vendored-javascript -->
```json
{"file":"main.js","line":6,"col":0,"method":"main.js::program:Child","module":"third_party","path":"Base","kind":"extends","via":"binding","resolvedMethod":"","builtin":false,"declaringModule":""}
{"file":"main.js","line":8,"col":0,"method":"main.js::program","module":"third_party","path":"run","kind":"call","via":"binding","resolvedMethod":"third_party/lib/index.js::program:run","builtin":false,"declaringModule":""}
{"file":"use.js","line":3,"col":14,"method":"use.js::program","module":"third_party","path":"Base","kind":"new","via":"binding","resolvedMethod":"","builtin":false,"declaringModule":""}
```

### Several slices from one run

`usages --also-callsites <file>` writes the `callsites` slice next to the usages slice and builds
and tags the atom once; the callsites file is the one `atom callsites` would write.

### Limitations

- JavaScript: `via` `name` records (JSX above all) depend on a name match within the file, not on
  scope. The Vue, Svelte and Astro positions are not exact (AppThreat/atom#320).
- `type` records inherit the frontend's type recovery: on untyped code a guessed receiver type
  still produces a record. Python precision is held down by typed attribute reads, `super()` into
  external bases and quoted `TYPE_CHECKING` annotations.
- PHP and Ruby: with `--external-paths`, calls on untyped top-level locals into vendored code get
  no record, the same as calls into any other dependency. Ruby's frontend skips `db/migrate` by
  default, so its uses are absent. Python imports of namespace packages (a directory without
  `__init__.py`) are recorded as dependencies, with or without the option.
- Bytecode: R8-renamed code needs a `mapping.txt` (AppThreat/atom#360), calls through lambdas and
  method references are missing (AppThreat/atom#354), and a Dalvik input has no `invokedynamic`
  handles. Several candidate ancestors of an inherited member keep the first one.
- C and C++: a header must resolve to an absolute path to count. CDT's C++ recall is lower than
  EDG's, and CDT misses curl's `bool` macro.
- Side-effect imports (`import "./polyfill"`) are not records yet.
