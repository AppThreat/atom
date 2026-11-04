package io.appthreat.atom.slicing.callsites

/** One record per external use site of an imported module: where and how it is used.
  *
  * Every record carries the position of the use itself (the identifier, the member name, the
  * decorator), never a position borrowed from another call. `kind` says what the use is (call, new,
  * jsx, arg, ref, decorator, extends, implements, decltype, tag, import); `via` says how the module
  * was resolved:
  *   - `binding`: the identifier resolves, scope-correctly through captures, to an imported binding
  *     of its file;
  *   - `type`: the callee's resolved method full name starts with a module specifier this project
  *     imports (a typed receiver, `res.status()` on an express response);
  *   - `name`: the frontend links no identifier to the use (decorators, JSX tags, tagged templates,
  *     heritage clauses), so the name is matched against the file's import bindings.
  *
  * `builtin` marks uses of the language's standard library, and `declaringModule` carries the
  * module of the class that declares the callee when it differs from the receiver's static type
  * (decision 0001, P2). `purl` carries the distribution the module maps to when atom knows one (a
  * BOM's ImportedModules or the python-deps index); it is left out of the JSON while empty. These
  * are new in v4; atom and cdxgen change together, so there is no schema versioning.
  */
final case class CallSiteRecord(
  file: String,
  line: Integer,
  col: Integer,
  method: String,
  module: String,
  path: String,
  kind: String,
  via: String,
  resolvedMethod: String,
  builtin: Boolean = false,
  declaringModule: String = "",
  purl: String = ""
)

object CallSiteRecord:

  implicit val encoder: io.circe.Encoder[CallSiteRecord] =
      io.circe.generic.semiauto.deriveEncoder[CallSiteRecord].mapJsonObject { json =>
          // an absent purl says "no mapping", not "the empty purl"
          if json("purl").exists(_.isNull) || json("purl").exists(_.asString.exists(_.isEmpty))
          then json.remove("purl")
          else json
      }

  /** The most specific kind wins when one use site (a position and a module) carries several
    * records: the base of `jwt.sign(...)` reaches the slice both as the chain's base and as the
    * call's receiver argument, and the frontend can duplicate an identifier node outright.
    */
  private val kindRank = Map(
    "call"       -> 0,
    "new"        -> 1,
    "jsx"        -> 2,
    "tag"        -> 3,
    "decorator"  -> 4,
    "arg"        -> 5,
    "extends"    -> 6,
    "implements" -> 6,
    "decltype"   -> 7,
    "ref"        -> 8,
    // an import that binds nothing is its own site, the whole statement; it competes with no
    // other kind at one position, so it ranks last
    "import" -> 9
  )

  /** The order in which records of one use site compete: the most specific kind first, then the
    * path, `via` and the resolved method. Two different records can still tie on those (the same
    * kind rank with two kinds, or two enclosing methods), so every other field follows, which makes
    * the order total: the winner of a group is the same record whatever order the group came in,
    * and the order a source walks the graph in (node ids, completion order) cannot reach the
    * output. The rest is a rule, not a preference: an empty `method` (text no method owns) before a
    * named one, then the names in order; `false` before `true`; an empty `declaringModule` and
    * `purl` before a filled one.
    */
  private def winnerKey(r: CallSiteRecord): (
    Int,
    String,
    String,
    String,
    String,
    String,
    Boolean,
    String,
    String
  ) =
      (
        kindRank.getOrElse(r.kind, Int.MaxValue),
        r.path,
        r.via,
        r.resolvedMethod,
        r.kind,
        r.method,
        r.builtin,
        r.declaringModule,
        r.purl
      )

  /** `r` with `/` as the separator of its file, on every platform. The Java and PHP frontends write
    * a file with the platform's separator, and PHP repeats it at the start of `method`, so a slice
    * made on Windows would otherwise name the same file differently from one made elsewhere, and a
    * consumer merging locations from several tools would count one place twice
    * (AppThreat/atom#373). Only the file and the file prefix of `method` change: the rest of a PHP
    * method name, `module` and `path` keep the backslashes of PHP namespaces.
    */
  def withPortableFile(r: CallSiteRecord): CallSiteRecord =
      if r.file.indexOf('\\') < 0 then r
      else
        val file = r.file.replace('\\', '/')
        val method =
            if r.method.startsWith(r.file) then file + r.method.substring(r.file.length)
            else r.method
        r.copy(file = file, method = method)

  /** One record per use site: for each (file, line, col, module) the most specific kind wins (see
    * [[winnerKey]] for ties), and the order is positional, through the module, path and kind of the
    * record, which no two winners share. The order never involves node ids, which vary between runs
    * (callsites plan, determinism rule 3).
    *
    * `byMember` adds the path to the site, for sources without columns: bytecode has one line per
    * statement and no column, so `sb.append(x).toString()` is two uses of one class at one
    * position, which the column would otherwise tell apart.
    */
  def dedupe(records: Iterable[CallSiteRecord], byMember: Boolean = false): List[CallSiteRecord] =
      records
          .groupBy(r => (r.file, r.line, r.col, r.module, if byMember then r.path else ""))
          .map(_._2.minBy(winnerKey))
          .toList
          .sortBy(r => (r.file, r.line.intValue, r.col.intValue, r.module, r.path, r.kind))
end CallSiteRecord

/** The (module, export path) a use resolves to, shared by the language sources. */
private[callsites] final case class Origin(module: String, path: List[String])
