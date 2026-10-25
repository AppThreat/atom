# Lesson 9: External Call-Site Slicing (`callsites`)

### Learning Objective

Extract one record per external use site of an imported module — every call, construction,
JSX render, argument pass, plain reference, decorator, heritage clause and tagged template —
each at the position of the use itself, with the module and the export path it resolves to.

### Pre-requisites

- **JDK 23+** and a built `atom`.
- **A JavaScript/TypeScript project** (or a pre-built `app.atom`). The mode is JS/TS only; other
  languages produce an empty slice.

### Conceptual Background

Where `usages` is object-shaped (an entry per declaration, its calls and the calls it is passed
to), [`CallSiteSlicing`](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/CallSiteSlicing.scala)
is use-shaped: one `CallSiteRecord` per place an import is used, ready to become SBOM
`evidence.occurrences` without interpretation. Each record carries:

| field   | meaning                                                                    |
|---------|----------------------------------------------------------------------------|
| `file`  | file of the use                                                             |
| `line`, `col` | position of the use itself (the identifier, the member name, the decorator); never a position borrowed from another call |
| `method` | enclosing method's full name                                               |
| `module` | imported module specifier (`node:fs`, `@babel/parser`, `@angular/common/http`) |
| `path`  | export path within the module (`readFile`, `Parser.on`, `EditorView.dispatch`) |
| `kind`  | `call`, `new`, `jsx`, `arg`, `ref`, `decorator`, `extends`, `tag`           |
| `via`   | how the module was resolved: `binding`, `type` or `name` (below)            |
| `mfn`   | the resolved callee's method full name, when a call is involved             |

`via` is the resolution story of the record:

- **`binding`** — the identifier resolves, scope-correctly through closure captures, to an
  imported binding of its file (`[DeclarationRefs]`). A binding's origin is traced through the
  assignments the frontend lowers imports to, including destructured dynamic imports
  (`const { S3Client } = await import("@aws-sdk/client-s3")` → `new S3Client()`). A local that
  is reassigned anywhere gets no origin: it may hold something else at a use.
- **`type`** — the call is not covered by a binding, but its resolved name starts with a module
  the project imports (`res.status()` on a typed express response → `express:Response:status`).
  Records whose callee member is a primitive string/array method (`includes`, `replace`, ...)
  are dropped: type recovery puts module prefixes on plain strings (`someString.includes` →
  `node:path:path:includes`), measured as false positives on cdxgen and juice-shop.
- **`name`** — the frontend links no identifier to the use, so the name is matched against the
  file's import bindings: decorators (`@Entity()`), heritage clauses (`class Bus extends
  EventEmitter`), tagged templates (`` gql`...` ``) and JSX tags. JSX stays name-based until the
  frontend links tags to their bindings (chen T9); a name match is exact within the file, so a
  shadowing local of the same name is only misattributed when the file imports the shadowed
  module too.

### Real Commands

```bash
# one record per external use site, JS/TS
./atom.sh callsites -l js -o app.atom --slice-outfile callsites.json /path/to/project

# on an existing atom (tags still run; nothing is rebuilt)
./atom.sh callsites -l js --reuse-atom -o app.atom --slice-outfile callsites.json /path/to/project
```

The output is `{"callsites": [ ... ]}`.

### Code Example

```json
{"file":"src/dyn.ts","line":3,"col":21,"method":"src/dyn.ts::program:s3list",
 "module":"@aws-sdk/client-s3","path":"S3Client","kind":"new","via":"binding",
 "mfn":"<unknownFullName>"}
```

The three passes in [CallSiteSlicing.scala](https://github.com/AppThreat/atom/blob/main/src/main/scala/io/appthreat/atom/slicing/CallSiteSlicing.scala):

```scala
// A: identifiers that resolve to an imported binding (kind by how they take part in a call)
atom.identifier.filterNot(_.name.startsWith("_tmp")).foreach { id => ... }

// B: calls whose resolved name carries a module the project imports (typed receivers)
atom.call.filterNot(c => c.name.startsWith("<operator>") || coveredCalls.contains(c.id()))...

// C: decorators, heritage clauses, tagged templates and JSX tags, by name
recs ++= nameResolvedRecords(atom, js)
```

### Limitations

- JS/TS only. Python, Ruby and PHP import shapes differ; the binding → origin → use idea
  carries over, but must be measured before generalising.
- `via=name` records (JSX above all) depend on name matching within the file, not on scope
  resolution.
- `via=type` records inherit type recovery's guesses on untyped code; the primitive-member
  filter removes the measured false-positive class, and the file's own bindings win when a
  resolved name carries a module the file never imports.
