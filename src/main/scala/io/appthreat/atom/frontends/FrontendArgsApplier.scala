package io.appthreat.atom.frontends

import io.appthreat.c2cpg.Config as CConfig
import io.appthreat.javasrc2cpg.Config as JavaConfig
import io.appthreat.jimple2cpg.Config as JimpleConfig
import io.appthreat.jssrc2cpg.Config as JSConfig
import io.appthreat.php2atom.Config as PhpConfig
import io.appthreat.pysrc2cpg.Py2CpgOnFileSystemConfig as PyConfig
import io.appthreat.ruby2atom.Config as RubyConfig
import io.appthreat.x2cpg.ValidationMode
import io.appthreat.x2cpg.X2CpgConfig
import io.appthreat.x2cpg.passes.frontend.TypeRecoveryParserConfig

import java.nio.file.Paths
import scala.util.Try

/** Bridges the flat `frontendArgs` map (key=value pairs supplied via `--frontend-args` or the
  * first-class CLI flags) and the strongly-typed per-frontend `Config` builders exposed by the
  * analysis core.
  *
  * Each frontend is constructed in-process, so every `applyX` simply walks the relevant keys and
  * invokes the matching `withX(...)` builder. Keys that are absent from the map leave the existing
  * configuration untouched, which keeps the surface forward-compatible: a key added here does not
  * break a configuration file authored against an older release.
  *
  * Precedence for any single key is: explicit value (CLI) over file over built-in default. Because
  * the file layer feeds the same map, the applier never has to know where a value originated.
  */
object FrontendArgsApplier:

  /** Reads a comma-separated value as a trimmed, de-duplicated set of non-empty entries. */
  def csv(args: Map[String, String], key: String): Set[String] =
      args.get(key).map(_.split(",").map(_.trim).filter(_.nonEmpty).toSet).getOrElse(Set.empty)

  /** Reads a boolean, falling back to `default` when the key is absent or unparseable. */
  def bool(args: Map[String, String], key: String, default: Boolean): Boolean =
      args.get(key).map(v => Try(v.toBoolean).getOrElse(default)).getOrElse(default)

  /** Reads an integer, falling back to `default` when the key is absent or unparseable. */
  def int(args: Map[String, String], key: String, default: Int): Int =
      args.get(key).map(v => Try(v.toInt).getOrElse(default)).getOrElse(default)

  /** Reads a string, falling back to `default` when the key is absent. */
  def str(args: Map[String, String], key: String, default: String): String =
      args.getOrElse(key, default)

  /** Reads a non-empty optional string. Useful for `Option[String]` config fields where an empty
    * value should not overwrite an existing `None`.
    */
  def strOpt(args: Map[String, String], key: String): Option[String] =
      args.get(key).map(_.trim).filter(_.nonEmpty)

  /** Applies the keys shared by every frontend (the `X2CpgConfig` surface): path/file exclusions
    * and early schema validation. These mutate the config in place because the underlying fields
    * are inherited mutable state.
    */
  def applyUniversal[R <: X2CpgConfig[R]](c: R, args: Map[String, String]): R =
    val excludes = csv(args, "exclude").toSeq.map(c.createPathForIgnore)
    if excludes.nonEmpty then c.ignoredFiles = c.ignoredFiles ++ excludes
    strOpt(args, "exclude-regex").foreach(r => c.ignoredFilesRegex = r.r)
    if bool(args, "enable-early-schema-checking", default = false) then
      c.withSchemaValidation(ValidationMode.Enabled)
    c

  /** Applies the type-recovery knobs (`no-dummyTypes`, `type-prop-iterations`) shared by the
    * frontends that mix in [[TypeRecoveryParserConfig]].
    */
  def applyTypeRecovery[R <: X2CpgConfig[R] & TypeRecoveryParserConfig[R]](
    c: R,
    args: Map[String, String]
  ): R =
    var r = c
    r = r.withDisableDummyTypes(bool(args, "no-dummyTypes", r.disableDummyTypes))
    args.get("type-prop-iterations").foreach(_ =>
        r = r.withTypePropagationIterations(int(
          args,
          "type-prop-iterations",
          r.typePropagationIterations
        ))
    )
    r

  // The apply methods below run every copy-based `withX(...)` builder FIRST and defer the in-place
  // mutations (`applyUniversal`/`applyTypeRecovery`) to the end. The frontend `Config` case classes
  // re-initialise inherited trait state on `copy`, so anything set in place before a copy would be
  // silently lost. Executing the in-place setters last guarantees they survive.

  /** C / C++ frontend. Note: `enable-ast-cache` honours the user value rather than being forced on,
    * so a project can disable the on-disk AST cache for a single run.
    */
  def applyC(c: CConfig, args: Map[String, String]): CConfig =
    var r = c
    r = r.withDefines(r.defines ++ csv(args, "defines"))
    r = r.withIncludePaths(r.includePaths ++ csv(args, "includes") ++ csv(args, "include-paths"))
    r = r.withIncludeFiles(r.includeFiles ++ csv(args, "include-files"))
    r = r.withMacroFiles(r.macroFiles ++ csv(args, "macro-files"))
    r = r.withCppStandard(str(args, "cpp-standard", r.cppStandard))
    r = r.withIncludeComments(bool(args, "include-comments", r.includeComments))
    r = r.withLogProblems(bool(args, "log-problems", r.logProblems))
    r = r.withLogPreprocessor(bool(args, "log-preprocessor", r.logPreprocessor))
    r = r.withPrintIfDefsOnly(bool(args, "print-ifdef-only", r.printIfDefsOnly))
    r = r.withIncludePathsAutoDiscovery(bool(
      args,
      "include-auto-discovery",
      r.includePathsAutoDiscovery
    ))
    r = r.withFunctionBodies(bool(args, "function-bodies", r.includeFunctionBodies))
    r = r.withImageLocations(bool(args, "with-image-locations", r.includeImageLocations))
    r = r.withProjectIndexes(bool(args, "with-project-index", r.useProjectIndex))
    r = r.withParseInactiveCode(bool(args, "parse-inactive-code", r.parseInactiveCode))
    r = r.withIncludeTrivialExpressions(bool(
      args,
      "include-trivial-expressions",
      r.includeTrivialExpressions
    ))
    r = r.withAstCache(bool(args, "enable-ast-cache", r.enableAstCache))
    r = r.withCacheDir(str(args, "ast-cache-dir", Paths.get(r.inputPath, ".chen").toString))
    r = r.withOnlyAstCache(bool(args, "only-ast-cache", r.onlyAstCache))
    applyUniversal(r, args)
    r
  end applyC

  /** Java source frontend. */
  def applyJava(c: JavaConfig, args: Map[String, String]): JavaConfig =
    var r = c
    r = r.withInferenceJarPaths(r.inferenceJarPaths ++ csv(args, "inference-jar-paths"))
    r = r.withFetchDependencies(bool(args, "fetch-dependencies", r.fetchDependencies))
    r = r.withEnableTypeRecovery(bool(args, "enable-type-recovery", r.enableTypeRecovery))
    r = r.withShowEnv(bool(args, "show-env", r.showEnv))
    r = r.withSkipTypeInfPass(bool(args, "skip-type-inf-pass", r.skipTypeInfPass))
    r = r.withDumpJavaparserAsts(bool(args, "dump-javaparser-asts", r.dumpJavaparserAsts))
    strOpt(args, "delombok-java-home").foreach(v => r = r.withDelombokJavaHome(v))
    strOpt(args, "delombok-mode").foreach(v => r = r.withDelombokMode(v))
    strOpt(args, "jdk-path").foreach(v => r = r.withJdkPath(v))
    applyUniversal(r, args)
    applyTypeRecovery(r, args)
    r

  /** JavaScript / TypeScript / Flow frontend. */
  def applyJs(c: JSConfig, args: Map[String, String]): JSConfig =
    var r = c
    r = r.withTsTypes(bool(args, "ts-types", r.tsTypes))
    r = r.withFlow(bool(args, "flow", r.flow))
    strOpt(args, "astgen-out").foreach(v => r = r.withAstGenOutDir(v))
    applyUniversal(r, args)
    applyTypeRecovery(r, args)
    r

  /** JVM bytecode / Android / Scala frontend. */
  def applyJimple(c: JimpleConfig, args: Map[String, String]): JimpleConfig =
    var r = c
    r = r.withDepth(int(args, "depth", r.depth))
    r = r.withFullResolver(bool(args, "full-resolver", r.fullResolver))
    r = r.withRecurse(bool(args, "recurse", r.recurse))
    r = r.withOnlyClasses(bool(args, "only-classes", r.onlyClasses))
    r = r.withDynamicDirs(r.dynamicDirs ++ csv(args, "dynamic-dirs").toSeq)
    r = r.withDynamicPkgs(r.dynamicPkgs ++ csv(args, "dynamic-pkgs").toSeq)
    strOpt(args, "android").foreach(v => r = r.withAndroid(v))
    strOpt(args, "scala-sdk").foreach(v => r = r.withScalaSdk(v))
    applyUniversal(r, args)
    r

  /** Python frontend. */
  def applyPython(c: PyConfig, args: Map[String, String]): PyConfig =
    var r = c
    r = r.withIgnoreVenvDir(bool(args, "ignore-venv-dir", r.ignoreVenvDir))
    r = r.withIgnoreDirNames(r.ignoreDirNames ++ csv(args, "ignore-dir-names").toSeq)
    r = r.withIgnorePaths(r.ignorePaths ++ csv(args, "ignore-paths").toSeq.map(p => Paths.get(p)))
    strOpt(args, "venv-dir").foreach(v => r = r.withVenvDir(Paths.get(v)))
    strOpt(args, "requirements-txt").foreach(v => r = r.withRequirementsTxt(v))
    applyUniversal(r, args)
    applyTypeRecovery(r, args)
    r

  /** PHP frontend. */
  def applyPhp(c: PhpConfig, args: Map[String, String]): PhpConfig =
    var r = c
    r = r.withAstCache(bool(args, "enable-ast-cache", r.enableAstCache))
    r = r.withCacheDir(str(args, "ast-cache-dir", Paths.get(r.inputPath, ".chen").toString))
    strOpt(args, "php-ini").foreach(v => r = r.withPhpIni(v))
    strOpt(args, "php-parser-bin").foreach(v => r = r.withPhpParserBin(v))
    applyUniversal(r, args)
    applyTypeRecovery(r, args)
    r

  /** Ruby frontend. */
  def applyRuby(c: RubyConfig, args: Map[String, String]): RubyConfig =
    var r = c
    if bool(args, "disable-type-stubs", default = false) then r = r.withTypeStubs(false)
    applyUniversal(r, args)
    applyTypeRecovery(r, args)
    r

  /** Description of a single tunable, used to generate the `--frontend-args-keys` reference. */
  final case class KeyDoc(
    name: String,
    typ: String,
    default: String,
    description: String,
    languages: Seq[String]
  )

  /** Language groups referenced by [[allKeys]]. Defined first so the `allKeys` initializer sees
    * non-null values (Scala evaluates object members top to bottom).
    */
  private val Universal: Seq[String] = Seq(
    "c",
    "cpp",
    "h",
    "java",
    "jar",
    "jimple",
    "android",
    "apk",
    "dex",
    "scala",
    "sbt",
    "js",
    "ts",
    "flow",
    "python",
    "py",
    "php",
    "ruby",
    "rb"
  )
  private val CLike: Seq[String] = Seq("c", "cpp", "h")
  private val TypeRecovery: Seq[String] =
      Seq("java", "js", "ts", "flow", "python", "py", "php", "ruby", "rb")

  /** The canonical list of tunables, grouped by the language(s) they apply to. Used both as the
    * source of truth for `--frontend-args-keys` and as living documentation of the surface.
    */
  val allKeys: Seq[KeyDoc] = Seq(
    KeyDoc("exclude", "csv", "", "Paths to exclude, relative to the input or absolute.", Universal),
    KeyDoc("exclude-regex", "string", "", "Regex of file paths to exclude.", Universal),
    KeyDoc(
      "enable-early-schema-checking",
      "bool",
      "false",
      "Validate the schema during AST creation.",
      Universal
    ),
    KeyDoc(
      "no-dummyTypes",
      "bool",
      "false",
      "Disable placeholder dummy types during type propagation.",
      TypeRecovery
    ),
    KeyDoc(
      "type-prop-iterations",
      "int",
      "2",
      "Maximum type-propagation iterations.",
      TypeRecovery
    ),
    KeyDoc("defines", "csv", "", "Preprocessor defines, e.g. `DEBUG`.", CLike),
    KeyDoc("includes", "csv", "", "Header include paths (alias: `include-paths`).", CLike),
    KeyDoc("include-paths", "csv", "", "Header include paths (alias of `includes`).", CLike),
    KeyDoc("include-files", "csv", "", "Specific header files to include.", CLike),
    KeyDoc("macro-files", "csv", "", "Macro definition files.", CLike),
    KeyDoc("cpp-standard", "string", "", "C++ standard, e.g. `c++17`, `c++20`.", CLike),
    KeyDoc("include-comments", "bool", "false", "Embed comments into the graph.", CLike),
    KeyDoc("log-problems", "bool", "false", "Log every parse problem.", CLike),
    KeyDoc("log-preprocessor", "bool", "false", "Log every preprocessor statement.", CLike),
    KeyDoc("print-ifdef-only", "bool", "false", "Print preprocessor conditionals and exit.", CLike),
    KeyDoc("include-auto-discovery", "bool", "false", "Auto-discover system header paths.", CLike),
    KeyDoc("function-bodies", "bool", "true", "Parse function and method bodies.", CLike),
    KeyDoc(
      "with-image-locations",
      "bool",
      "false",
      "Record image/macro-expansion locations.",
      CLike
    ),
    KeyDoc("with-project-index", "bool", "false", "Use an existing Eclipse project index.", CLike),
    KeyDoc("parse-inactive-code", "bool", "false", "Parse `#if 0` branches.", CLike),
    KeyDoc("include-trivial-expressions", "bool", "false", "Emit trivial expressions.", CLike),
    KeyDoc(
      "enable-ast-cache",
      "bool",
      "true",
      "Persist the parsed AST to disk for reuse.",
      CLike ++ Seq("php")
    ),
    KeyDoc(
      "ast-cache-dir",
      "string",
      "<input>/.chen",
      "Directory for the AST cache.",
      CLike ++ Seq("php")
    ),
    KeyDoc("only-ast-cache", "bool", "false", "Build the AST cache then skip CPG creation.", CLike),
    KeyDoc("inference-jar-paths", "csv", "", "Extra jars used for type information.", Seq("java")),
    KeyDoc(
      "fetch-dependencies",
      "bool",
      "false",
      "Fetch dependency jars for type info.",
      Seq("java")
    ),
    KeyDoc(
      "delombok-java-home",
      "string",
      "",
      "Java home used to run Delombok (17 recommended).",
      Seq("java")
    ),
    KeyDoc(
      "delombok-mode",
      "enum",
      "types-only",
      "Delombok strategy: no-delombok|default|types-only|run-delombok.",
      Seq("java")
    ),
    KeyDoc("enable-type-recovery", "bool", "false", "Enable generic type recovery.", Seq("java")),
    KeyDoc("jdk-path", "string", "", "JDK used to resolve builtin Java types.", Seq("java")),
    KeyDoc("show-env", "bool", "false", "Print the resolved environment and exit.", Seq("java")),
    KeyDoc("skip-type-inf-pass", "bool", "false", "Skip the type-inference pass.", Seq("java")),
    KeyDoc("dump-javaparser-asts", "bool", "false", "Dump JavaParser ASTs and exit.", Seq("java")),
    KeyDoc(
      "ts-types",
      "bool",
      "true",
      "Resolve types from TypeScript declarations.",
      Seq("js", "ts")
    ),
    KeyDoc("flow", "bool", "false", "Enable Flow mode.", Seq("js")),
    KeyDoc("astgen-out", "string", "", "Permanent directory for astgen output.", Seq("js", "ts")),
    KeyDoc("depth", "int", "1", "Recursive jar unpacking depth.", Seq("jimple", "scala")),
    KeyDoc(
      "full-resolver",
      "bool",
      "false",
      "Whole-program, transitive call resolution.",
      Seq("jimple", "scala")
    ),
    KeyDoc("recurse", "bool", "false", "Recursively unpack nested jars.", Seq("jimple", "scala")),
    KeyDoc("only-classes", "bool", "false", "Only include `.class` files.", Seq("jimple", "scala")),
    KeyDoc(
      "dynamic-dirs",
      "csv",
      "",
      "Dirs whose classes may be loaded dynamically.",
      Seq("jimple")
    ),
    KeyDoc(
      "dynamic-pkgs",
      "csv",
      "",
      "Packages whose classes may be loaded dynamically.",
      Seq("jimple")
    ),
    KeyDoc("android", "string", "", "Path to `android.jar` for APK analysis.", Seq("jimple")),
    KeyDoc("scala-sdk", "string", "", "Scala library jar for scala-built jars.", Seq("scala")),
    KeyDoc("venv-dir", "string", ".venv", "Virtual-environment directory.", Seq("python")),
    KeyDoc(
      "ignore-venv-dir",
      "bool",
      "true",
      "Whether to ignore the venv directory.",
      Seq("python")
    ),
    KeyDoc("ignore-paths", "csv", "", "Paths to ignore from analysis.", Seq("python")),
    KeyDoc("ignore-dir-names", "csv", "", "Directory name fragments to exclude.", Seq("python")),
    KeyDoc(
      "requirements-txt",
      "string",
      "requirements.txt",
      "Requirements file name.",
      Seq("python")
    ),
    KeyDoc("php-ini", "string", "", "php.ini path for the PHP parser.", Seq("php")),
    KeyDoc("php-parser-bin", "string", "", "Path to the php-parser.phar binary.", Seq("php")),
    KeyDoc(
      "disable-type-stubs",
      "bool",
      "false",
      "Disable type-stub based type recovery.",
      Seq("ruby")
    )
  )

  /** Returns the keys relevant to a given atom `-l` value, formatted for display. */
  def keysForLanguage(language: String): Seq[KeyDoc] =
    val normalized = language.trim.toLowerCase
    allKeys.filter(_.languages.contains(normalized))

  /** Renders the supported keys for a language as a human-readable table. */
  def renderKeys(language: String): String =
    val rows = keysForLanguage(language)
    if rows.isEmpty then s"No frontend-args keys documented for language '$language'."
    else
      val header = f"${"key"}%-28s ${"type"}%-6s ${"default"}%-16s description"
      val sep    = "-" * 80
      val body = rows.map { k =>
          f"${k.name}%-28s ${k.typ}%-6s ${k.default}%-16s ${k.description}"
      }
      (Seq(header, sep) ++ body).mkString("\n")
end FrontendArgsApplier
