package io.appthreat.atom

import better.files.File
import io.appthreat.atom.dataflows.{DataFlowGraph, OssDataFlow, OssDataFlowOptions}
import io.appthreat.atom.frontends.FrontendArgsApplier
import io.appthreat.atom.frontends.clike.C2Atom
import io.appthreat.atom.parsedeps.parseDependencies
import io.appthreat.atom.passes.TypeHintPass
import io.appthreat.atom.slicing.*
import io.appthreat.c2cpg.{C2Cpg, Config as CConfig}
import io.appthreat.javasrc2cpg.{JavaSrc2Cpg, Config as JavaConfig}
import io.appthreat.jimple2cpg.{Jimple2Cpg, Config as JimpleConfig}
import io.appthreat.jssrc2cpg.passes.{
    ConstClosurePass,
    ImportResolverPass,
    JavaScriptInheritanceNamePass,
    JavaScriptTypeRecoveryPass
}
import io.appthreat.jssrc2cpg.{JsSrc2Cpg, Config as JSConfig}
import io.appthreat.php2atom.passes.PhpSetKnownTypesPass
import io.appthreat.php2atom.{Php2Atom, Config as PhpConfig}
import io.appthreat.pysrc2cpg.{
    DynamicTypeHintFullNamePass,
    Py2CpgOnFileSystem,
    PythonInheritanceNamePass,
    PythonTypeHintCallLinker,
    PythonTypeRecoveryPass,
    ImportResolverPass as PyImportResolverPass,
    ImportsPass as PythonImportsPass,
    Py2CpgOnFileSystemConfig as PyConfig
}
import io.appthreat.ruby2atom.{Ruby2Atom, Config as RubyConfig}
import io.appthreat.x2cpg.passes.base.AstLinkerPass
import io.appthreat.x2cpg.passes.frontend.XTypeRecoveryConfig
import io.appthreat.x2cpg.passes.taggers.{
    AndroidServicesTagsPass,
    CdxPass,
    ChennaiTagsPass,
    EasyTagsPass,
    PiiTagsPass,
    TrackersTagsPass
}
import io.appthreat.x2cpg.utils.ExternalCommand
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.layers.LayerCreatorContext
import scopt.OptionParser

import java.util.Locale
import java.util.regex.Pattern
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Atom:

  val DEFAULT_ATOM_OUT_FILE: String = "app.atom"
  val DEFAULT_SLICE_OUT_FILE        = "slices.json"
  val DEFAULT_SLICE_DEPTH           = 7
  val DEFAULT_MAX_DEFS: Int         = 2000
  val FRAMEWORK_INPUT_TAG: String   = "framework-input"
  val FRAMEWORK_OUTPUT_TAG: String  = "framework-output"
  // java, jar and jimple frontends (jimple covers Android apk/dex output)
  val JVM_FRONTENDS: Set[String] =
      Set(Languages.JAVA, Languages.JAVASRC, "JAR", "JIMPLE", "ANDROID", "APK", "DEX")
  val DEFAULT_SOURCE_TAGS: Seq[String] =
      Seq(
        FRAMEWORK_INPUT_TAG,
        "framework-route",
        "cli-source",
        "driver-source",
        "framework",
        "event",
        // sensitive data residing on the device (JVM/Android taggers)
        "sensitive-data",
        "pii",
        // remote content fetched onto the device (download / read response body)
        "service-ingress"
      )
  val DEFAULT_SINK_TAGS: Seq[String] =
      Seq(
        FRAMEWORK_OUTPUT_TAG,
        "library-call",
        "cloud",
        "rpc",
        "http",
        "http-client",
        "network",
        "file-io",
        "sql",
        "code-execution",
        "reflection",
        "concurrent",
        "serialization",
        "unsafe-deserialization",
        "regex",
        "cron",
        "mail",
        "framework",
        "api",
        "pkg.*",
        // device-data egress sinks (JVM/Android taggers)
        "service-egress",
        // sensitive data reaching a local/on-device AI model is privacy relevant too
        "on-device-ai",
        "tracker",
        "adware"
      )
  private val COMMON_IGNORE_REGEX = ".*(docs|example|samples|mocks|Documentation|demos).*"
  private val JAVA_IGNORE_REGEX   = ".*(target|build)/(generated|intermediates|outputs|tmp).*"
  private val PHP_IGNORE_REGEX    = ".*(samples|examples|docs).*"
  private val RUBY_IGNORE_REGEX   = ".*(docs|vendor|spec).*"

  private val COMMON_IGNORE_DIRS_ENV = "CHEN_IGNORE_DIRS"
  // Identify directories to ignore for python
  private val defaultPythonIgnoreDirs = Seq(
    "samples",
    "docs",
    "virtualenvs",
    "venv",
    "benchmarks",
    "tutorials"
  )
  private val defaultPythonIgnorePathFragments = Seq("noxfile")
  private val testIgnoreDirs                   = Seq("test", "tests", "mocks")
  private val ignoreTestDirs: Boolean =
      Option(System.getenv("CHEN_IGNORE_TEST_DIRS"))
          .exists(_.trim.equalsIgnoreCase("true"))
  private val pythonIgnoreDirsEnv                   = "CHEN_PYTHON_IGNORE_DIRS"
  private val pythonSpecificIgnoreDirs: Seq[String] = envSpecificIgnoreDirs(pythonIgnoreDirsEnv)
  private val basePythonIgnoreDirs: Seq[String] =
    val pythonBaseIgnoreDirs = pythonSpecificIgnoreDirs match
      case Seq() => defaultPythonIgnoreDirs
      case dirs  => dirs
    (pythonBaseIgnoreDirs ++ commonIgnoreDirs).distinct
  private val pyIgnoreDirs: Seq[String] = appendTestIgnoreDirs(basePythonIgnoreDirs)
  private val pythonIgnorePathFragments: Seq[String] = pythonSpecificIgnoreDirs match
    case Seq() => defaultPythonIgnorePathFragments
    case _     => Seq.empty
  // This regex will be eventually passed to chen
  private val pythonIgnoredFilesRegex: String = combineIgnoredFilesRegex(
    ignoredDirectoriesRegex(pyIgnoreDirs),
    ignoredPathFragmentsRegex(pythonIgnorePathFragments)
  )

  private def parseIgnoreDirs(value: String): Seq[String] =
      value.split(',').map(_.trim).filter(_.nonEmpty).toSeq

  private def commonIgnoreDirs: Seq[String] =
      envSpecificIgnoreDirs(COMMON_IGNORE_DIRS_ENV)

  private def envIgnoreDirs(envVars: String*): Seq[String] =
      (commonIgnoreDirs ++ envSpecificIgnoreDirs(envVars*)).distinct

  private def envSpecificIgnoreDirs(envVars: String*): Seq[String] =
      envVars.flatMap(envVar => Option(System.getenv(envVar)).toSeq.flatMap(parseIgnoreDirs))
          .distinct

  private def appendTestIgnoreDirs(ignoreDirs: Seq[String]): Seq[String] =
      if ignoreTestDirs then (ignoreDirs ++ testIgnoreDirs).distinct
      else ignoreDirs.distinct

  private def ignoredPathFragmentsRegex(ignoreDirs: Seq[String]): Option[String] =
    val escapedPathFragments = ignoreDirs.filter(_.nonEmpty).map(Pattern.quote)
    Option.when(escapedPathFragments.nonEmpty)(s".*(?:${escapedPathFragments.mkString("|")}).*")

  private def ignoredDirectoriesRegex(ignoreDirs: Seq[String]): Option[String] =
    val directoryPatterns = ignoreDirs.map(toDirectoryRegex).filter(_.nonEmpty)
    Option.when(directoryPatterns.nonEmpty) {
        s"(?:^|.*[/\\\\])(?:${directoryPatterns.mkString("|")})(?:[/\\\\].*|$$)"
    }

  private def toDirectoryRegex(ignoreDir: String): String =
      ignoreDir
          .replace('\\', '/')
          .stripPrefix("./")
          .stripPrefix("/")
          .stripSuffix("/")
          .split("/+")
          .filter(_.nonEmpty)
          .map(Pattern.quote)
          .mkString("[/\\\\]")

  private def ignoredFilesRegexFromEnv(envVars: String*): Option[String] =
      ignoredDirectoriesRegex(appendTestIgnoreDirs(envIgnoreDirs(envVars*)))

  private def ignoredFilesRegex(defaultRegex: String, envVars: String*): String =
      combineIgnoredFilesRegex(Some(defaultRegex), ignoredFilesRegexFromEnv(envVars*))

  private def combineIgnoredFilesRegex(regexes: Option[String]*): String =
      regexes.flatten.filter(_.nonEmpty).map(regex => s"(?:$regex)").mkString("|")

  val DEFAULT_EXPORT_DIR: String = "atom-exports"
  // Possible values: graphml, dot
  val DEFAULT_EXPORT_FORMAT: String = "graphml"

  /** Accepted values for the `--cache` flag. Each maps to a `chen.cache.*` JVM system property
    * consumed by the analysis core's cache controller at runtime.
    */
  val CacheModes: Set[String] =
      Set("all", "none", "no-ast", "no-cpg", "no-astgen", "no-summary")

  /** Translates a friendly `--cache <mode>` value into the JVM system properties consumed by the
    * analysis core. These must be set before the frontend runs, so they are applied during option
    * parsing rather than threaded through `frontendArgs`.
    */
  def applyCacheMode(mode: String): Unit = mode match
    case "all"        => // default caching; nothing to disable
    case "none"       => System.setProperty("chen.cache.disabled", "true")
    case "no-ast"     => System.setProperty("chen.cache.disabled.ast", "true")
    case "no-cpg"     => System.setProperty("chen.cache.disabled.cpg", "true")
    case "no-astgen"  => System.setProperty("chen.cache.disabled.astgen", "true")
    case "no-summary" => System.setProperty("chen.cache.disabled.summary", "true")
    case _            =>
  // Possible values: no-delombok, default, types-only, run-delombok
  private val DEFAULT_DELOMBOK_MODE: String =
      sys.env.getOrElse("CHEN_DELOMBOK_MODE", "types-only")
  private val TYPE_PROPAGATION_ITERATIONS = 1
  private val MAVEN_JAR_PATH: File        = File.home / ".m2" / "repository"
  private val GRADLE_JAR_PATH: File = File.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  private val SBT_JAR_PATH: File    = File.home / ".ivy2" / "cache"
  private val JAR_INFERENCE_PATHS: Set[String] =
      Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)

  private def findAndroidJar(dirs: Seq[File]): Option[String] =
      dirs
          .filter(_.isDirectory)
          .flatMap(_.glob("**/android.jar"))
          .map(_.pathAsString)
          .headOption

  private lazy val androidJarPath: Option[String] =
    val envDir    = Option(System.getenv("ANDROID_HOME")).map(File(_))
    val macSdkDir = File.home / "Library" / "Android" / "sdk"
    findAndroidJar(Seq(envDir, Some(macSdkDir)).flatten)
  private val CHEN_INCLUDE_PATH = sys.env.getOrElse("CHEN_INCLUDE_PATH", "")
  // Custom include paths for c/c++
  private val C2ATOM_INCLUDE_PATH =
      if CHEN_INCLUDE_PATH.nonEmpty && File(
          CHEN_INCLUDE_PATH
        ).isDirectory
      then CHEN_INCLUDE_PATH.split(java.io.File.pathSeparator).toSet
      else
        Set.empty

  private val optionParser: OptionParser[BaseConfig] = new scopt.OptionParser[BaseConfig]("atom"):
    opt[String]('o', "output")
        .text("output filename. Default app.⚛ or app.atom in windows")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withOutputAtomFile(File(x))
              case _                  => c
        )
    opt[String]('s', "slice-outfile")
        .text("export intra-procedural slices as json")
        .action((x, c) => c.withOutputSliceFile(File(x)))
    opt[String]('l', "language")
        .text("source language")
        .required()
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withLanguage(x)
              case _                  => c
        )
        .validate(x =>
            if x.isBlank then failure(s"Please specify a language using the --language option.")
            else success
        )
    opt[Map[String, String]]("frontend-args")
        .text(
          "Advanced frontend configuration (key=value). E.g. --frontend-args defines=DEBUG,only-ast-cache=true"
        )
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArgs(x)
              case _                  => c
        )
    opt[Unit]("frontend-args-keys")
        .text(
          "Print the supported --frontend-args keys for the selected language (-l) and exit."
        )
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArgsKeys(true)
              case _                  => c
        )
    // --- First-class universal frontend flags (populate the same frontendArgs channel) ---
    opt[String]("exclude")
        .text("Comma-separated files/folders to exclude (relative to input or absolute).")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("exclude", x)
              case _                  => c
        )
    opt[String]("exclude-regex")
        .text("Regex of file paths to exclude during analysis.")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("exclude-regex", x)
              case _                  => c
        )
    opt[Unit]("no-ast-cache")
        .text("Disable the on-disk AST cache for this run (default: enabled).")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("enable-ast-cache", "false")
              case _                  => c
        )
    opt[String]("cache-dir")
        .text("Directory for the AST cache (default: <input>/.chen).")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("ast-cache-dir", x)
              case _                  => c
        )
    opt[Unit]("schema-check")
        .text("Enable early schema validation during AST creation.")
        .action((_, c) =>
            c match
              case config: AtomConfig =>
                  config.withFrontendArg("enable-early-schema-checking", "true")
              case _ => c
        )
    opt[Unit]("no-dummy-types")
        .text("Disable placeholder dummy types during type propagation.")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("no-dummyTypes", "true")
              case _                  => c
        )
    opt[Int]("type-prop-iterations")
        .text("Maximum iterations of type propagation (applies to supported frontends).")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("type-prop-iterations", x.toString)
              case _                  => c
        )
    opt[String]("cache")
        .text(
          "Cache mode: all (default) | none | no-ast | no-cpg | no-astgen | no-summary."
        )
        .action((x, c) =>
            c match
              case config: AtomConfig =>
                  applyCacheMode(x.trim.toLowerCase)
                  c
              case _ => c
        )
        .validate(x =>
          val mode = x.trim.toLowerCase
          if CacheModes.contains(mode) then success
          else failure(s"Unknown cache mode '$x'. Use: ${CacheModes.mkString(", ")}.")
        )
    // --- Per-language frontend flags (applied only when -l matches) ---
    opt[String]("cpp-standard")
        .text("C/C++ standard, e.g. c++17, c++20. (C/C++ only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("cpp-standard", x)
              case _                  => c
        )
    opt[String]("define")
        .unbounded()
        .text("Define a preprocessor name. Repeatable. (C/C++ only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withAppendedFrontendArg("defines", x)
              case _                  => c
        )
    opt[String]("include-path")
        .unbounded()
        .text("Header include path. Repeatable. (C/C++ only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withAppendedFrontendArg("includes", x)
              case _                  => c
        )
    opt[String]("delombok-mode")
        .text("Delombok strategy: no-delombok|default|types-only|run-delombok. (Java only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("delombok-mode", x)
              case _                  => c
        )
    opt[String]("jdk-path")
        .text("JDK used to resolve builtin Java types. (Java only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("jdk-path", x)
              case _                  => c
        )
    opt[Unit]("fetch-deps")
        .text("Fetch dependency jars for extra type information. (Java only)")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("fetch-dependencies", "true")
              case _                  => c
        )
    opt[Boolean]("ts-types")
        .text("Resolve types from TypeScript declarations (default: true). (JS/TS only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("ts-types", x.toString)
              case _                  => c
        )
    opt[Unit]("flow")
        .text("Enable Flow mode. (JS only)")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("flow", "true")
              case _                  => c
        )
    opt[String]("venv-dir")
        .text("Virtual-environment directory (default: .venv). (Python only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("venv-dir", x)
              case _                  => c
        )
    opt[String]("ignore-paths")
        .text("Comma-separated paths to ignore from analysis. (Python only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("ignore-paths", x)
              case _                  => c
        )
    opt[String]("android-sdk")
        .text("Path to android.jar for APK analysis. (Jimple/Android only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("android", x)
              case _                  => c
        )
    opt[Int]("solver-depth")
        .text("Recursive jar unpacking depth (default: 1). (Jimple/Scala only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("depth", x.toString)
              case _                  => c
        )
    opt[Unit]("full-resolver")
        .text("Enable whole-program, transitive call resolution. (Jimple/Scala only)")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("full-resolver", "true")
              case _                  => c
        )
    opt[String]("php-ini")
        .text("php.ini path for the PHP parser. (PHP only)")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("php-ini", x)
              case _                  => c
        )
    opt[Unit]("disable-type-stubs")
        .text("Disable type-stub based type recovery. (Ruby only)")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withFrontendArg("disable-type-stubs", "true")
              case _                  => c
        )
    opt[Unit]("with-data-deps")
        .text("generate the atom with data-dependencies - defaults to `false`")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withDataDependencies(true)
              case _                  => c
        )
    opt[Unit]("remove-atom")
        .text("do not persist the atom file - defaults to `false`")
        .action((_, c) =>
            c match
              case config: AtomConfig => config.withRemoveAtom(true)
              case _                  => c
        )
    opt[Unit]('x', "export-atom")
        .text("export the atom file with data-dependencies to graphml - defaults to `false`")
        .action((_, c) =>
            c match
              case config: AtomConfig =>
                  config.withExportAtom(true)
              case _ => c
        )
    opt[Unit]("reuse-atom")
        .text("reuse existing atom file - defaults to `false`")
        .action((_, c) =>
            c match
              case config: AtomConfig =>
                  config.withReuseAtom(true)
              case _ => c
        )
    opt[String]("export-dir")
        .text(s"export directory. Default: $DEFAULT_EXPORT_DIR")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withExportDir(x)
              case _                  => c
        )
    opt[String]("export-format")
        .text(s"export format graphml or dot. Default: $DEFAULT_EXPORT_FORMAT")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withExportFormat(x)
              case _                  => c
        )
    opt[String]("config")
        .text("path to a JSON config file for the export and algorithms commands")
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withConfigFile(Option(File(x)))
              case _                  => c
        )
    opt[String]("validation-config")
        .text(
          "path to a JSON file declaring validators/sanitisers (chennai.json schema). Reachable " +
              "flows passing through a declared sanitiser are dropped for its categories."
        )
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withValidationConfigFile(Option(File(x)))
              case _                  => c
        )
    opt[String]("file-filter")
        .text(s"the name of the source file to generate slices from. Uses regex.")
        .action((x, c) => c.withFileFilter(Option(x)))
    opt[String]("method-name-filter")
        .text(s"filters in slices that go through specific methods by names. Uses regex.")
        .action((x, c) => c.withMethodNameFilter(Option(x)))
    opt[String]("method-parameter-filter")
        .text(
          s"filters in slices that go through methods with specific types on the method parameters. Uses regex."
        )
        .action((x, c) => c.withMethodParamTypeFilter(Option(x)))
    opt[String]("method-annotation-filter")
        .text(
          s"filters in slices that go through methods with specific annotations on the methods. Uses regex."
        )
        .action((x, c) => c.withMethodAnnotationFilter(Option(x)))
    opt[Int]("max-num-def")
        .text(
          s"maximum number of definitions in per-method data flow calculation - defaults to $DEFAULT_MAX_DEFS"
        )
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withMaxNumDef(x)
              case _                  => c
        )
        .validate(x =>
            if x <= 0 then failure("`max-num-def` must be an integer larger than 0")
            else success
        )
    opt[Unit]("legacy-dataflow")
        .text(
          "use the classic data-flow engine and disable mini-graph fragment caching. By default " +
              "atom uses the faster, lower-allocation Flux engine with fragment caching enabled."
        )
        .action((_, c) =>
            c match
              case config: AtomConfig =>
                  config.withUseFluxEngine(false).withCacheFragments(false)
              case _ => c
        )
    arg[String]("input")
        .optional()
        .text("source file or directory")
        .action((x, c) => c.withInputPath(File(x)))
        .validate { x =>
            if x == "" then failure("Input path required")
            else if !File(x).exists then
              failure(s"Input path does not exist at `\"$x\"`, exiting.")
            else success
        }
    cmd("parsedeps")
        .text("Extract dependencies from the build file and imports")
        .action((_, *) => AtomParseDepsConfig().withRemoveAtom(true))
    cmd("data-flow")
        .text("Extract backward data-flow slices")
        .action((_, _) => AtomDataFlowConfig().withDataDependencies(true))
        .children(
          opt[Int]("slice-depth")
              .text(
                s"the max depth to traverse the DDG for the data-flow slice - defaults to $DEFAULT_SLICE_DEPTH."
              )
              .action((x, c) =>
                  c match
                    case c: AtomDataFlowConfig => c.copy(sliceDepth = x)
                    case _                     => c
              ),
          opt[String]("sink-filter")
              .text(s"filters on the sink's `code` property. Uses regex.")
              .action((x, c) =>
                  c match
                    case c: AtomDataFlowConfig => c.copy(sinkPatternFilter = Option(x))
                    case _                     => c
              )
        )
    cmd("usages")
        .text("Extract local variable and parameter usages")
        .action((_, *) => AtomUsagesConfig())
        .children(
          opt[Int]("min-num-calls")
              .text(s"the minimum number of calls required for a usage slice - defaults to 1.")
              .action((x, c) =>
                  c match
                    case c: AtomUsagesConfig => c.copy(minNumCalls = x)
                    case _                   => c
              ),
          opt[Unit]("include-source")
              .text(s"includes method source code in the slices - defaults to false.")
              .action((_, c) =>
                  c match
                    case c: AtomUsagesConfig => c.copy(includeMethodSource = true)
                    case _                   => c
              ),
          opt[Unit]("extract-endpoints")
              .text(
                s"extract http endpoints and convert to openapi format using atom-tools - defaults to false."
              )
              .action((_, c) =>
                  c match
                    case c: AtomUsagesConfig => c.copy(extractEndpoints = true)
                    case _                   => c
              )
        )
    cmd("reachables")
        .text("Extract reachable data-flow slices based on automated framework tags")
        .action((_, *) => AtomReachablesConfig().withDataDependencies(true))
        .children(
          opt[String]("source-tag")
              .text(s"source tag - defaults to framework-input. Comma-separated values allowed.")
              .action((x, c) =>
                  c match
                    case c: AtomReachablesConfig => c.copy(sourceTag =
                            x.split("[,|]").map(_.trim).filter(_.nonEmpty).toIndexedSeq
                        )
                    case _ => c
              ),
          opt[String]("sink-tag")
              .text(s"sink tag - defaults to framework-output. Comma-separated values allowed.")
              .action((x, c) =>
                  c match
                    case c: AtomReachablesConfig => c.copy(sinkTag =
                            x.split("[,|]").map(_.trim).filter(_.nonEmpty).toIndexedSeq
                        )
                    case _ => c
              ),
          opt[Int]("slice-depth")
              .text(
                s"the max depth to traverse the DDG during reverse reachability - defaults to $DEFAULT_SLICE_DEPTH."
              )
              .action((x, c) =>
                  c match
                    case c: AtomReachablesConfig => c.copy(sliceDepth = x)
                    case _                       => c
              ),
          opt[Unit]("include-crypto")
              .text(s"includes crypto library flows - defaults to false.")
              .action((_, c) =>
                  c match
                    case c: AtomReachablesConfig => c.copy(includeCryptoFlows = true)
                    case _                       => c
              ),
          opt[String]("profile")
              .text(
                s"reduce false positives with a flow-filtering profile: ${ReachabilityProfile.names.mkString(", ")}. Defaults to generic (no extra filtering)."
              )
              .action((x, c) =>
                  c match
                    case c: AtomReachablesConfig => c.copy(profile = x)
                    case _                       => c
              )
        )
    cmd("export")
        .text("Export the atom to a graph format (dot, graphml, gexf, graphson, neo4jcsv, gnn)")
        .action((_, _) => AtomExportConfig().withDataDependencies(true))
        .children(
          opt[String]("format")
              .text("export format: dot, graphml, gexf, graphson, neo4jcsv or gnn")
              .action((x, c) =>
                  c match
                    case c: AtomExportConfig => c.withExportFormat(x).asInstanceOf[AtomExportConfig]
                    case _                   => c
              ),
          opt[String]("scope")
              .text("export scope: whole or methods. Default: whole")
              .action((x, c) =>
                  c match
                    case c: AtomExportConfig => c.withScope(x)
                    case _                   => c
              ),
          opt[String]("out")
              .text(s"output directory. Default: $DEFAULT_EXPORT_DIR")
              .action((x, c) =>
                  c match
                    case c: AtomExportConfig => c.withExportDir(x).asInstanceOf[AtomExportConfig]
                    case _                   => c
              )
        )
    cmd("algorithms")
        .text("Run a graph algorithm over the atom and write the result as JSON")
        .action((_, _) => AtomAlgorithmsConfig().withDataDependencies(true))
        .children(
          opt[String]("type")
              .text(
                "algorithm: scc, toposort, dominators, paths, centrality, lowest-common-ancestors, dependency-sequencer, union-find, heap-walker, or context-sensitive-paths"
              )
              .action((x, c) =>
                  c match
                    case c: AtomAlgorithmsConfig => c.withAlgoType(x)
                    case _                       => c
              ),
          opt[String]("source")
              .text("source method full-name pattern for the paths algorithm. Uses regex.")
              .action((x, c) =>
                  c match
                    case c: AtomAlgorithmsConfig => c.withSourceSelector(Option(x))
                    case _                       => c
              ),
          opt[String]("target")
              .text("target method full-name pattern for the paths algorithm. Uses regex.")
              .action((x, c) =>
                  c match
                    case c: AtomAlgorithmsConfig => c.withTargetSelector(Option(x))
                    case _                       => c
              ),
          opt[Int]("max-depth")
              .text("maximum path depth for the paths algorithm")
              .action((x, c) =>
                  c match
                    case c: AtomAlgorithmsConfig => c.withMaxDepth(x)
                    case _                       => c
              )
        )
    help("help").text("display this help message")

  def main(args: Array[String]): Unit =
      run(args) match
        case Right(_) =>
        case Left(errMsg) =>
            if errMsg == null then
              println("Unexpected error")
            else if errMsg.nonEmpty && errMsg.contains(
                "storage metadata does not contain version number"
              )
            then
              println(
                "Existing app.atom appears to be corrupted. Please remove and re-run this command."
              )
            else
              println(s"Failure: $errMsg")
            System.exit(1)

  private[atom] def run(args: Array[String]): Either[String, String] =
    val parserArgs = args.toList
    parseConfig(parserArgs) match
      case Right(config: AtomConfig) =>
          AtomConfigLoader(config) match
            case Right(loaded) =>
                if loaded.frontendArgsKeys then
                  println(FrontendArgsApplier.renderKeys(loaded.language))
                  Right("Displayed frontend-args keys")
                else run(loaded, loaded.language)
            case Left(err) => Left(err)
      case Right(_)  => Left("Invalid configuration generated")
      case Left(err) => Left(err)

  private def run(config: AtomConfig, language: String): Either[String, String] =
      for
        _ <- generateAtom(config, language)
      yield newAtomCreatedString(config)

  private def newAtomCreatedString(config: AtomConfig): String =
    val absolutePath = config.outputAtomFile.path.toAbsolutePath
    if config.removeAtom then
      config.outputAtomFile.delete(true)
      ""
    else
      s"Atom created successfully at $absolutePath\n"

  private def generateSlice(config: AtomConfig, ag: Cpg): Either[String, String] =
      try
        migrateAtomConfigToSliceConfig(config) match
          case e: AtomExportConfig =>
              runGraphExport(e, ag)
          case a: AtomAlgorithmsConfig =>
              runGraphAlgorithms(a, ag)
          case x: AtomConfig if config.exportAtom =>
              exportAtom(config, ag, x)
          case _: DataFlowConfig =>
              generateDataFlowSlice(config, ag)
          case u: UsagesConfig =>
              generateUsagesSlice(config, ag, u)
          case _: ReachablesConfig =>
              generateReachablesSlice(config, ag)
          case x: AtomParseDepsConfig =>
              generateParseDepsSlice(config, ag, x)
          case _ =>
              Right("No slice generation required")
        end match
      catch
        case err: Throwable =>
            Left(s"${err.toString}\n${err.getStackTrace.take(20).mkString("\n")}")

  private def runGraphExport(config: AtomExportConfig, ag: Cpg): Either[String, String] =
      GraphCommands.runExport(ag, config)

  private def runGraphAlgorithms(config: AtomAlgorithmsConfig, ag: Cpg): Either[String, String] =
      GraphCommands.runAlgorithms(ag, config)

  private def exportAtom(
    config: AtomConfig,
    ag: Cpg,
    exportConfig: AtomConfig
  ): Either[String, String] =
    println(s"Exporting the atom to the directory ${exportConfig.exportDir}")
    exportConfig.exportFormat match
      case "graphml" =>
          exportToGraphML(ag, exportConfig.exportDir)
      case _ =>
          exportToDot(ag, exportConfig.exportDir)
    Right("Atom exported successfully")

  private def exportToGraphML(ag: Cpg, exportDir: String): Unit =
      ag.method.internal
          .gml(exportDir)

  private def exportToDot(ag: Cpg, exportDir: String): Unit =
    val methods = ag.method.internal
    methods.dot(exportDir)
    methods.exportAllRepr(exportDir)

  private def generateDataFlowSlice(config: AtomConfig, ag: Cpg): Either[String, String] =
    println("Slicing the atom for data-flow. This might take a while ...")
    val dataFlowSlice = calculateDataFlowSlice(ag, config).collect { case x: DataFlowSlice => x }
    val atomDataFlowSliceJson = dataFlowSlice.map { slice =>
        AtomDataFlowSlice(slice, DataFlowGraph.buildFromSlice(slice).paths).toJson
    }
    saveSlice(config.outputSliceFile, atomDataFlowSliceJson)
    Right("Data-flow slice generated successfully")

  private def calculateDataFlowSlice(cpg: Cpg, config: BaseConfig): Option[ProgramSlice] =
      config match
        case x: AtomDataFlowConfig =>
            val dataFlowConfig = migrateAtomConfigToSliceConfig(x).asInstanceOf[DataFlowConfig]
            new DataFlowSlicing().calculateDataFlowSlice(cpg, dataFlowConfig)
        case _ => None

  private def generateUsagesSlice(
    config: AtomConfig,
    ag: Cpg,
    usagesConfig: UsagesConfig
  ): Either[String, String] =
    println("Slicing the atom for usages. This might take a few minutes ...")
    runChennaiTags(config, ag)
    val slice = calculateUsagesSlice(ag, config)
    slice.foreach { s =>
      val outFile = config.outputSliceFile.createFileIfNotExists(createParents = true)
      s.toJsonFile(outFile)
      println(s"Slices have been successfully written to ${outFile.pathAsString}")
    }
    handleEndpointExtraction(config, usagesConfig)
    Right("Usages slice generated successfully")

  private def calculateUsagesSlice(cpg: Cpg, config: BaseConfig): Option[ProgramSlice] =
      config match
        case x: AtomUsagesConfig =>
            val usagesConfig = migrateAtomConfigToSliceConfig(x).asInstanceOf[UsagesConfig]
            Option(UsageSlicing.calculateUsageSlice(cpg, usagesConfig))
        case _ => None

  private def handleEndpointExtraction(config: AtomConfig, usagesConfig: UsagesConfig): Unit =
      if usagesConfig.extractEndpoints then
        val openapiFileName = sys.env.getOrElse(
          "ATOM_TOOLS_OPENAPI_FILENAME",
          s"${config.language}-openapi.json"
        )
        val openapiFormat = sys.env.getOrElse("ATOM_TOOLS_OPENAPI_FORMAT", "openapi3.1.0")
        val atomToolsWorkDir =
            sys.env.getOrElse("ATOM_TOOLS_WORK_DIR", config.inputPath.pathAsString)
        val semanticsSlices = File(atomToolsWorkDir).glob(
          "*semantics.slices.json",
          includePath = true,
          maxDepth = 1
        )
        val extraArgs = if semanticsSlices.nonEmpty then
          s" -e ${semanticsSlices.head.pathAsString}"
        else ""

        println(
          s"atom-tools convert -i ${config.outputSliceFile}$extraArgs -t ${config
                  .language} -f $openapiFormat -o $atomToolsWorkDir${java.io.File.separator}$openapiFileName"
        )

        ExternalCommand.run(
          s"atom-tools convert -i ${config.outputSliceFile}$extraArgs -t ${config
                  .language} -f $openapiFormat -o $atomToolsWorkDir${java.io.File.separator}$openapiFileName",
          atomToolsWorkDir
        ) match
          case Success(_) =>
              println(s"$openapiFileName created successfully.")
          case Failure(exception) =>
              println(
                s"Failed to run atom-tools. Use the atom container image or perform 'pip install atom-tools' and re-run this command. Exception: ${exception.getMessage}"
              )

  private def generateReachablesSlice(config: AtomConfig, ag: Cpg): Either[String, String] =
    println("Slicing the atom for reachables. This might take a few minutes ...")
    try
      migrateAtomConfigToSliceConfig(config) match
        case reachablesConfig: ReachablesConfig =>
            val baseOutputPath =
                if config.outputSliceFile.name.endsWith(".json") then
                  config.outputSliceFile.pathAsString.stripSuffix(".json")
                else
                  config.outputSliceFile.pathAsString

            val chunkSize = 1000
            ReachableSlicing.calculateReachableSliceAndPersist(
              ag,
              reachablesConfig,
              baseOutputPath,
              chunkSize
            )
            Right("Reachables slices generated and persisted successfully to multiple files.")
        case _ =>
            Left("Invalid configuration for reachables slicing.")
    catch
      case ex: Exception =>
          Left(s"Failed to generate or persist reachables slices: ${ex.getMessage}")
    end try
  end generateReachablesSlice

  private def generateParseDepsSlice(
    config: AtomConfig,
    ag: Cpg,
    parseDepsConfig: AtomParseDepsConfig
  ): Either[String, String] =
      parseDependencies(ag).map(_.toJson) match
        case Left(err) =>
            Left(err)
        case Right(slice) =>
            saveSlice(parseDepsConfig.outputSliceFile, Option(slice))
            Right("Parse dependencies slice generated successfully")

  private def saveSlice(outFile: File, programSlice: Option[String]): Unit =
      programSlice.foreach { slice =>
        val finalOutputPath =
            File(outFile.pathAsString)
                .createFileIfNotExists(createParents = true)
                .write(slice)
                .pathAsString
        println(s"Slices have been successfully written to $finalOutputPath")
      }

  private def migrateAtomConfigToSliceConfig(x: BaseConfig): BaseConfig =
      (x match
        case config: AtomDataFlowConfig =>
            DataFlowConfig(
              config.sinkPatternFilter,
              config.excludeOperatorCalls,
              config.mustEndAtExternalMethod,
              config.sliceDepth
            )
        case config: AtomUsagesConfig =>
            UsagesConfig(
              config.minNumCalls,
              config.excludeOperatorCalls,
              !config.includeMethodSource,
              config.extractEndpoints
            )
        case config: AtomReachablesConfig =>
            val profile = ReachabilityProfile.byName(config.profile).getOrElse {
                if config.profile.trim.nonEmpty &&
                  !config.profile.equalsIgnoreCase(ReachabilityProfile.Generic.name)
                then
                  System.err.println(
                    s"Unknown reachables profile '${config.profile}'. Using 'generic'. Available: ${ReachabilityProfile.names.mkString(", ")}"
                  )
                ReachabilityProfile.Generic
            }
            // A profile may restrict sources (e.g. to web-facing inputs), but an explicit
            // --source-tag always wins.
            val effectiveSourceTag =
                profile.sourceTagsOverride match
                  case Some(tags) if config.sourceTag == DEFAULT_SOURCE_TAGS => tags
                  case _                                                     => config.sourceTag
            ReachablesConfig(
              effectiveSourceTag,
              config.sinkTag,
              config.sliceDepth,
              config.includeCryptoFlows,
              // Summaries are part of the Flux bundle (no separate flag): on when Flux is on.
              useSummaries = config.useFluxEngine,
              profile = profile
            )
        case _ => x
      ).withInputPath(x.inputPath)
          .withOutputSliceFile(x.outputSliceFile)
          .withFileFilter(x.fileFilter)
          .withMethodNameFilter(x.methodNameFilter)
          .withMethodParamTypeFilter(x.methodParamTypeFilter)
          .withMethodAnnotationFilter(x.methodAnnotationFilter)

  private def loadFromOdb(filename: String): Try[Cpg] =
    val odbConfig = overflowdb.Config.withDefaults().withStorageLocation(filename)
        .withHeapPercentageThreshold(90)
    val config = CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    try
      Success(io.shiftleft.codepropertygraph.cpgloading.CpgLoader.loadFromOverflowDb(config))
    catch
      case err: Throwable =>
          Failure(err)

  private def generateAtom(config: AtomConfig, language: String): Either[String, String] =
      generateForLanguage(language.toUpperCase(Locale.ROOT), config)

  private def generateForLanguage(language: String, config: AtomConfig): Either[String, String] =
    Option(config.outputAtomFile.parent).foreach(_.createDirectoryIfNotExists(createParents = true))
    Option(config.outputSliceFile.parent).foreach(_.createDirectoryIfNotExists(createParents =
        true
    ))
    val outputAtomFile = config.outputAtomFile.pathAsString
    val onlyAstCache =
        FrontendArgsApplier.bool(config.frontendArgs, "only-ast-cache", default = false)
    // Mini-graph fragment AST caching has its own opt-in (`--cache-fragments`), decoupled from
    // the `--flux` data-flow engine so each improvement can be evaluated independently.
    if config.cacheFragments then
      io.appthreat.x2cpg.passes.frontend.CacheControl.enableFragments()
    // Enable the method-flow-summary cache so summaries persist next to the atom and are reused on
    // an unchanged re-run. Summaries are part of the Flux bundle, so this follows `useFluxEngine`.
    if config.useFluxEngine then
      io.appthreat.x2cpg.passes.frontend.CacheControl.enable(
        io.appthreat.x2cpg.passes.frontend.CacheControl.Summary
      )

    // Reuse-or-build: load the existing atom when one is present and the command allows it,
    // otherwise build a fresh atom from the input. New graph-level commands (export, algorithms)
    // share this path so they never require a manual pre-build step.
    val reusing = shouldReuseExistingAtom(config, outputAtomFile)
    getOrCreateAtom(language, config, outputAtomFile, reusing) match
      case Failure(exception) =>
          Left(exception.getStackTrace.take(20).mkString("\n"))
      case Success(ag) =>
          if onlyAstCache then
            closeCpg(ag)
            Try(File(outputAtomFile).delete(true))
            Right("AST cache generated successfully. Skipped CPG enhancement and slicing.")
          else
            for
              _ <- enhanceCpg(config, ag, reusing)
              _ <- generateSlice(config, ag)
              _ <- closeCpg(ag)
            yield "Atom generation successful"
  end generateForLanguage

  private def getOrCreateAtom(
    language: String,
    config: AtomConfig,
    outputAtomFile: String,
    reusing: Boolean
  ): Try[Cpg] =
      if reusing then loadExistingAtom(outputAtomFile)
      else createNewAtom(language, config, outputAtomFile)

  private def shouldReuseExistingAtom(config: AtomConfig, outputAtomFile: String): Boolean =
      (config.isInstanceOf[AtomUsagesConfig] ||
          config.isInstanceOf[AtomExportConfig] ||
          config.isInstanceOf[AtomAlgorithmsConfig] ||
          config.exportAtom || config.reuseAtom) &&
          File(outputAtomFile).exists

  private def loadExistingAtom(outputAtomFile: String): Try[Cpg] =
      try
        loadFromOdb(outputAtomFile)
      catch
        case _: Throwable =>
            println("Removing the existing atom file since it is corrupted.")
            File(outputAtomFile).delete(true)
            Failure(new RuntimeException("Corrupted atom file removed"))

  private def createNewAtom(
    language: String,
    config: AtomConfig,
    outputAtomFile: String
  ): Try[Cpg] =
    val result = language match
      case "H" | "HPP" | "I" =>
          createC2Atom(config, outputAtomFile)
      case Languages.C | Languages.NEWC | "CPP" | "C++" =>
          createC2Cpg(config, outputAtomFile)
      case "JAR" | "JIMPLE" | "ANDROID" | "APK" | "DEX" =>
          createJimple2Cpg(config, outputAtomFile)
      case Languages.JAVA | Languages.JAVASRC =>
          createJavaSrc2Cpg(config, outputAtomFile)
      case "SCALA" | "TASTY" | "SBT" =>
          createScalaCpg(config, outputAtomFile)
      case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" | "FLOW" =>
          createJsSrc2Cpg(config, outputAtomFile)
      case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
          createPythonCpg(config, outputAtomFile)
      case Languages.PHP =>
          createPhpCpg(config, outputAtomFile)
      case Languages.RUBYSRC | "RUBY" | "RB" | "JRUBY" =>
          createRubyCpg(config, outputAtomFile)
      case _ =>
          Failure(new RuntimeException(s"No language frontend supported for language '$language'"))

    // Clean up output file if creation failed
    result.recoverWith { case _ =>
        File(outputAtomFile).delete(true)
        result
    }
  end createNewAtom

  private def createC2Atom(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val cIgnoreDirEnvVars = Seq("CHEN_C_IGNORE_DIRS")
    val baseConfig = CConfig(
      includeComments = false,
      logProblems = false,
      includePathsAutoDiscovery = false
    )
        .withLogPreprocessor(false)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFunctionBodies(false)
        .withIgnoredFilesRegex(ignoredFilesRegex(COMMON_IGNORE_REGEX, cIgnoreDirEnvVars*))
        .withParseInactiveCode(false)
        .withImageLocations(false)
        .withIncludeTrivialExpressions(false)
        .withIncludePaths(C2ATOM_INCLUDE_PATH)
    val finalConfig = FrontendArgsApplier.applyC(baseConfig, config.frontendArgs)
    new C2Atom().createCpg(finalConfig)

  private def createC2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val cIgnoreDirEnvVars =
        if config.language.equalsIgnoreCase("CPP") || config.language.equalsIgnoreCase("C++") then
          Seq("CHEN_C_IGNORE_DIRS", "CHEN_CPP_IGNORE_DIRS")
        else Seq("CHEN_C_IGNORE_DIRS")
    val baseConfig = CConfig(
      includeComments = false,
      logProblems = false,
      includePathsAutoDiscovery = true
    )
        .withLogPreprocessor(false)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFunctionBodies(true)
        .withIgnoredFilesRegex(ignoredFilesRegex(COMMON_IGNORE_REGEX, cIgnoreDirEnvVars*))
        .withParseInactiveCode(false)
        .withImageLocations(false)
        .withIncludeTrivialExpressions(false)
        .withIncludePaths(C2ATOM_INCLUDE_PATH)
    val finalConfig = FrontendArgsApplier.applyC(baseConfig, config.frontendArgs)
    new C2Cpg().createCpgWithOverlays(finalConfig)
  end createC2Cpg

  private def createJimple2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val baseConfig = JimpleConfig(android = androidJarPath, fullResolver = true)
        .withRecurse(true)
        .withDepth(10)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFullResolver(true)
    val withIgnore = ignoredFilesRegexFromEnv("CHEN_JIMPLE_IGNORE_DIRS") match
      case Some(regex) => baseConfig.withIgnoredFilesRegex(regex)
      case None        => baseConfig
    val finalConfig = FrontendArgsApplier.applyJimple(withIgnore, config.frontendArgs)
    new Jimple2Cpg().createCpgWithOverlays(finalConfig)

  private def createJavaSrc2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val baseConfig = JavaConfig(
      fetchDependencies = true,
      inferenceJarPaths = JAR_INFERENCE_PATHS,
      enableTypeRecovery = true,
      delombokMode = Some(DEFAULT_DELOMBOK_MODE)
    )
        .withInputPath(config.inputPath.pathAsString)
        .withIgnoredFilesRegex(ignoredFilesRegex(JAVA_IGNORE_REGEX, "CHEN_JAVA_IGNORE_DIRS"))
        .withOutputPath(outputAtomFile)
    val finalConfig = FrontendArgsApplier.applyJava(baseConfig, config.frontendArgs)
    new JavaSrc2Cpg().createCpgWithOverlays(finalConfig)

  private def createScalaCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    handleScalaSemantics(config)
    val baseConfig = JimpleConfig(scalaSdk = Option(System.getProperty("java.class.path")))
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFullResolver(true)
        .withIgnoredFilesRegex(ignoredFilesRegex("$^", "CHEN_SCALA_IGNORE_DIRS"))
        .withOnlyClasses(true)
        .withDepth(1)
        .withRecurse(true)
    val finalConfig = FrontendArgsApplier.applyJimple(baseConfig, config.frontendArgs)
    new Jimple2Cpg().createCpgWithOverlays(finalConfig)

  private def handleScalaSemantics(config: AtomConfig): Unit =
    val workDir = sys.env.getOrElse("ATOM_SCALASEM_WORK_DIR", config.inputPath.pathAsString)
    val defaultSemanticSlicesFiles = config.inputPath / "semantics.slices.json"
    var semanticSlicesFile = sys.env.getOrElse(
      "ATOM_SCALASEM_SLICES_FILE",
      defaultSemanticSlicesFiles.pathAsString
    )

    if !semanticSlicesFile.endsWith("semantics.slices.json") then
      semanticSlicesFile = defaultSemanticSlicesFiles.pathAsString

    ExternalCommand.run(s"scalasem $workDir $semanticSlicesFile", workDir) match
      case Success(_) =>
          if File(semanticSlicesFile).exists then
            println(s"Semantic slices file '$semanticSlicesFile' created successfully.")
          else
            println(s"scalasem $workDir $semanticSlicesFile")
            println("scalasem command did not produce the semantic slices file.")
      case Failure(exception) =>
          println(
            s"Failed to run scalasem. Use the atom container image and re-run this command. Exception: ${exception.getMessage}"
          )
  end handleScalaSemantics

  private def createJsSrc2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val initialConfig = JSConfig()
        .withDisableDummyTypes(true)
        .withTypePropagationIterations(TYPE_PROPAGATION_ITERATIONS)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFlow(config.language.equalsIgnoreCase("FLOW"))
    val withAstGen = sys.env.get("CHEN_ASTGEN_OUT") match
      case Some(dir) => initialConfig.withAstGenOutDir(dir)
      case None      => initialConfig
    val jsIgnoreDirEnvVars =
        if config.language.equalsIgnoreCase("TS") || config.language.equalsIgnoreCase(
            "TYPESCRIPT"
          ) ||
          config.language.equalsIgnoreCase("FLOW")
        then
          Seq("CHEN_JAVASCRIPT_IGNORE_DIRS", "CHEN_JS_IGNORE_DIRS", "CHEN_TYPESCRIPT_IGNORE_DIRS")
        else Seq("CHEN_JAVASCRIPT_IGNORE_DIRS", "CHEN_JS_IGNORE_DIRS")
    val withIgnore = ignoredFilesRegexFromEnv(jsIgnoreDirEnvVars*) match
      case Some(regex) => withAstGen.withIgnoredFilesRegex(regex)
      case None        => withAstGen
    val finalConfig = FrontendArgsApplier.applyJs(withIgnore, config.frontendArgs)
    new JsSrc2Cpg()
        .createCpgWithOverlays(finalConfig)
        .map { ag =>
          new JavaScriptInheritanceNamePass(ag).createAndApply()
          new ConstClosurePass(ag).createAndApply()
          new ImportResolverPass(ag).createAndApply()
          new JavaScriptTypeRecoveryPass(ag).createAndApply()
          new TypeHintPass(ag).createAndApply()
          ag
        }
  end createJsSrc2Cpg

  private def createPythonCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val baseConfig = PyConfig()
        .withDisableDummyTypes(true)
        .withTypePropagationIterations(TYPE_PROPAGATION_ITERATIONS)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withDefaultIgnoredFilesRegex(List("\\..*".r))
        .withIgnoredFilesRegex(pythonIgnoredFilesRegex)
    val finalConfig = FrontendArgsApplier.applyPython(baseConfig, config.frontendArgs)
    new Py2CpgOnFileSystem()
        .createCpgWithOverlays(finalConfig)
        .map { ag =>
          new PythonImportsPass(ag).createAndApply()
          new PyImportResolverPass(ag).createAndApply()
          new DynamicTypeHintFullNamePass(ag).createAndApply()
          new PythonInheritanceNamePass(ag).createAndApply()
          new PythonTypeRecoveryPass(
            ag,
            XTypeRecoveryConfig(enabledDummyTypes = false)
          ).createAndApply()
          new PythonTypeHintCallLinker(ag).createAndApply()
          new AstLinkerPass(ag).createAndApply()
          ag
        }
  end createPythonCpg

  private def createPhpCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val baseConfig = PhpConfig()
        .withDisableDummyTypes(true)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withDefaultIgnoredFilesRegex(List("\\..*".r))
        .withIgnoredFilesRegex(ignoredFilesRegex(PHP_IGNORE_REGEX, "CHEN_PHP_IGNORE_DIRS"))
    val finalConfig = FrontendArgsApplier.applyPhp(baseConfig, config.frontendArgs)
    new Php2Atom().createCpgWithOverlays(finalConfig).map { ag =>
      new PhpSetKnownTypesPass(ag).createAndApply()
      ag
    }

  private def createRubyCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val baseConfig = RubyConfig()
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withIgnoredFilesRegex(ignoredFilesRegex(RUBY_IGNORE_REGEX, "CHEN_RUBY_IGNORE_DIRS"))
    val finalConfig = FrontendArgsApplier.applyRuby(baseConfig, config.frontendArgs)
    new Ruby2Atom().createCpgWithOverlays(finalConfig).map { ag =>
        ag
    }

  /** Run the framework/route tagger, feeding it the optional validators/sanitisers config so calls
    * to declared sanitisers are tagged for later flow filtering.
    */
  private def runChennaiTags(config: AtomConfig, atom: Cpg): Unit =
    val externalConfig =
        config.validationConfigFile.filter(_.exists).map(_.contentAsString)
    new ChennaiTagsPass(atom, externalConfig).createAndApply()

  private def enhanceCpg(config: AtomConfig, atom: Cpg, reusing: Boolean): Either[String, Unit] =
      config match
        case x: AtomConfig if needsDataFlowEnhancement(x, reusing) =>
            if x.useFluxEngine then
              println(
                "Generating data-flow dependencies from atom using the Flux engine. Please wait ..."
              )
            else
              println("Generating data-flow dependencies from atom. Please wait ...")
            try
              new OssDataFlow(new OssDataFlowOptions(
                maxNumberOfDefinitions = x.maxNumDef,
                useFluxEngine = x.useFluxEngine
              ))
                  .run(new LayerCreatorContext(atom))
              // Persist per-method flow summaries (CHEN3 §5 / G-5) as CPG-native `flow-summary`
              // tags so they serialize with the atom and the reachables engine can be primed
              // without recomputation. Part of the Flux bundle.
              if x.useFluxEngine then
                val summaries =
                    io.appthreat.dataflowengineoss.queryengine.summaries.FlowSummaryComputer
                        .computeAll(atom, io.appthreat.dataflowengineoss.DefaultSemantics())
                new io.appthreat.dataflowengineoss.queryengine.summaries.FlowSummaryTagsPass(
                  atom,
                  summaries
                ).createAndApply()
              new CdxPass(atom).createAndApply()
              new EasyTagsPass(atom).createAndApply()
              runChennaiTags(x, atom)
              applyJvmTaggers(atom)
              Right(())
            catch
              case npe: NullPointerException
                  if npe.getMessage != null &&
                      npe.getMessage.contains("AdjacentNodes") =>
                  Left(s"CPG appears to be corrupted with broken references. " +
                      s"Try removing the atom file and regenerating it. Error: ${npe.getMessage}")
              case ex: Exception =>
                  // Report the root cause: parallel passes wrap the real failure as a cause, so
                  // surfacing the deepest message is more useful than the generic wrapper.
                  var rootCause: Throwable = ex
                  while rootCause.getCause != null && rootCause.getCause != rootCause do
                    rootCause = rootCause.getCause
                  Left(
                    s"Failed to enhance CPG: ${rootCause.getClass.getName}: ${rootCause.getMessage}"
                  )
            end try
        case _ =>
            new EasyTagsPass(atom).createAndApply()
            applyJvmTaggers(atom)
            Right(())

  /** JVM/Android-focused taggers (java, jar, jimple frontends): PII / sensitive data, known
    * trackers & adware SDKs, and device-data egress to internet-facing services. Each pass guards
    * its own scope, but we additionally gate here so the work is skipped for other languages.
    */
  private def applyJvmTaggers(atom: Cpg): Unit =
    val language = atom.metaData.language.headOption.getOrElse("")
    if JVM_FRONTENDS.contains(language) then
      new PiiTagsPass(atom).createAndApply()
      new TrackersTagsPass(atom).createAndApply()
      new AndroidServicesTagsPass(atom).createAndApply()

  private def needsDataFlowEnhancement(config: AtomConfig, reusing: Boolean): Boolean =
      !reusing && !config.reuseAtom && (config.dataDeps ||
          config.isInstanceOf[AtomDataFlowConfig] ||
          config.isInstanceOf[AtomReachablesConfig] ||
          config.isInstanceOf[AtomExportConfig] ||
          config.isInstanceOf[AtomAlgorithmsConfig])

  private def closeCpg(cpg: Cpg): Either[String, Unit] =
      try
        cpg.close()
        Right(())
      catch
        case err: Throwable =>
            Left(err.getStackTrace.take(20).mkString("\n"))

  private def parseConfig(parserArgs: List[String]): Either[String, BaseConfig] =
      optionParser.parse(
        parserArgs,
        DefaultAtomConfig()
            .withOutputAtomFile(File(DEFAULT_ATOM_OUT_FILE))
            .withOutputSliceFile(File(DEFAULT_SLICE_OUT_FILE))
      ) match
        case Some(config) =>
            Right(config)
        case None =>
            Left("Could not parse command line options")
end Atom
