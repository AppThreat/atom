package io.appthreat.atom

import better.files.File
import io.appthreat.atom.dataflows.{DataFlowGraph, OssDataFlow, OssDataFlowOptions}
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
import io.appthreat.x2cpg.passes.taggers.{CdxPass, ChennaiTagsPass, EasyTagsPass}
import io.appthreat.x2cpg.utils.ExternalCommand
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.layers.LayerCreatorContext
import scopt.OptionParser

import java.util.Locale
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object Atom:

  val DEFAULT_ATOM_OUT_FILE: String = "app.atom"
  val DEFAULT_SLICE_OUT_FILE        = "slices.json"
  val DEFAULT_SLICE_DEPTH           = 7
  val DEFAULT_MAX_DEFS: Int         = 2000
  val FRAMEWORK_INPUT_TAG: String   = "framework-input"
  val FRAMEWORK_OUTPUT_TAG: String  = "framework-output"
  val DEFAULT_SOURCE_TAGS: Seq[String] =
      Seq(FRAMEWORK_INPUT_TAG, "framework-route", "cli-source", "driver-source")
  val DEFAULT_SINK_TAGS: Seq[String] =
      Seq(
        FRAMEWORK_OUTPUT_TAG,
        "library-call",
        "cloud",
        "rpc",
        "http",
        "cron",
        "mail"
      )
  private val COMMON_IGNORE_REGEX = ".*(docs|example|samples|mocks|Documentation|demos).*"
  // Identify directories to ignore for python
  private val defaultPythonIgnoreDirs = Seq(
    "samples",
    "docs",
    "virtualenvs",
    "venv",
    "benchmarks",
    "tutorials",
    "noxfile"
  )
  private val testIgnoreDirs = Seq("test", "tests", "mocks")
  private val basePythonIgnoreDirs: Seq[String] = Option(System.getenv("CHEN_PYTHON_IGNORE_DIRS"))
      .map(_.split(',').map(_.trim).filter(_.nonEmpty).toSeq)
      .getOrElse(defaultPythonIgnoreDirs)
  private val includeTestDirs: Boolean =
      Option(System.getenv("CHEN_IGNORE_TEST_DIRS"))
          .exists(_.trim.equalsIgnoreCase("true"))
  private val pyIgnoreDirs: Seq[String] =
      if includeTestDirs then basePythonIgnoreDirs ++ testIgnoreDirs
      else basePythonIgnoreDirs
  // This regex will be eventually passed to chen
  private val pythonIgnoredFilesRegex: String = ".*(" + pyIgnoreDirs.mkString("|") + ").*"

  val DEFAULT_EXPORT_DIR: String = "atom-exports"
  // Possible values: graphml, dot
  val DEFAULT_EXPORT_FORMAT: String = "graphml"
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
          "Advanced frontend configuration (key=value). E.g. --frontend-args defines=DEBUG,enable-ast-cache=true,only-ast-cache=true"
        )
        .action((x, c) =>
            c match
              case config: AtomConfig => config.withFrontendArgs(x)
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
              )
        )
    help("help").text("display this help message")

  private def extractArgSet(config: AtomConfig, key: String): Set[String] =
      config.frontendArgs.get(key)
          .map(_.split(",").map(_.trim).filter(_.nonEmpty).toSet)
          .getOrElse(Set.empty)

  private def extractArgString(config: AtomConfig, key: String, default: String = ""): String =
      config.frontendArgs.getOrElse(key, default)

  private def extractArgBoolean(config: AtomConfig, key: String, default: Boolean): Boolean =
      config.frontendArgs.get(key)
          .map(v => Try(v.toBoolean).getOrElse(default))
          .getOrElse(default)

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

  private def run(args: Array[String]): Either[String, String] =
    val parserArgs = args.toList
    parseConfig(parserArgs) match
      case Right(config: AtomConfig) => run(config, config.language)
      case Right(_)                  => Left("Invalid configuration generated")
      case Left(err)                 => Left(err)

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
    new ChennaiTagsPass(ag).createAndApply()
    val slice = calculateUsagesSlice(ag, config)
    saveSlice(config.outputSliceFile, slice.map(_.toJson))
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
                .createFileIfNotExists()
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
            ReachablesConfig(
              config.sourceTag,
              config.sinkTag,
              config.sliceDepth,
              config.includeCryptoFlows
            )
        case _ => x
      ).withInputPath(x.inputPath)
          .withOutputSliceFile(x.outputSliceFile)
          .withFileFilter(x.fileFilter)
          .withMethodNameFilter(x.methodNameFilter)
          .withMethodParamTypeFilter(x.methodParamTypeFilter)
          .withMethodAnnotationFilter(x.methodAnnotationFilter)

  private def loadFromOdb(filename: String): Try[Cpg] =
    val odbConfig = overflowdb.Config.withoutOverflow().withStorageLocation(filename)
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
    val outputAtomFile = config.outputAtomFile.pathAsString
    val onlyAstCache   = extractArgBoolean(config, "only-ast-cache", default = false)

    getOrCreateAtom(language, config, outputAtomFile) match
      case Failure(exception) =>
          Left(exception.getStackTrace.take(20).mkString("\n"))
      case Success(ag) =>
          if onlyAstCache then
            closeCpg(ag)
            Try(File(outputAtomFile).delete(true))
            Right("AST cache generated successfully. Skipped CPG enhancement and slicing.")
          else
            for
              _ <- enhanceCpg(config, ag)
              _ <- generateSlice(config, ag)
              _ <- closeCpg(ag)
            yield "Atom generation successful"

  private def getOrCreateAtom(
    language: String,
    config: AtomConfig,
    outputAtomFile: String
  ): Try[Cpg] =
      config match
        case x: AtomConfig if shouldReuseExistingAtom(x, outputAtomFile) =>
            loadExistingAtom(outputAtomFile)
        case _ =>
            createNewAtom(language, config, outputAtomFile)

  private def shouldReuseExistingAtom(config: AtomConfig, outputAtomFile: String): Boolean =
      (config.isInstanceOf[AtomUsagesConfig] || config.exportAtom || config.reuseAtom) &&
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
    val defines       = extractArgSet(config, "defines")
    val extraIncludes = extractArgSet(config, "includes") ++ extractArgSet(config, "include-paths")
    val cppStandard   = extractArgString(config, "cpp-standard")
    val onlyAstCache  = extractArgBoolean(config, "only-ast-cache", default = false)
    val enableAstCache =
        extractArgBoolean(config, "enable-ast-cache", default = false) || onlyAstCache
    val defaultCacheDir = (config.inputPath / "ast_out").pathAsString
    val cacheDir        = extractArgString(config, "ast-cache-dir", default = defaultCacheDir)
    val baseConfig = CConfig(
      includeComments = false,
      logProblems = false,
      includePathsAutoDiscovery = false
    )
        .withLogPreprocessor(false)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFunctionBodies(false)
        .withIgnoredFilesRegex(".*(docs|example|samples|mocks|Documentation|demos).*")
        .withParseInactiveCode(false)
        .withImageLocations(false)
        .withIncludeTrivialExpressions(false)
        .withAstCache(enableAstCache)
        .withCacheDir(cacheDir)
        .withOnlyAstCache(onlyAstCache)

    val finalConfig = baseConfig
        .withDefines(defines)
        .withCppStandard(cppStandard)
        .withIncludePaths(C2ATOM_INCLUDE_PATH ++ extraIncludes)
    new C2Atom().createCpg(finalConfig)
  end createC2Atom

  private def createC2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    val defines        = extractArgSet(config, "defines")
    val extraIncludes  = extractArgSet(config, "includes") ++ extractArgSet(config, "include-paths")
    val cppStandard    = extractArgString(config, "cpp-standard")
    val functionBodies = extractArgBoolean(config, "function-bodies", default = true)
    val parseInactive  = extractArgBoolean(config, "parse-inactive-code", default = false)
    val imageLocations = extractArgBoolean(config, "with-image-locations", default = false)
    val includeComments = extractArgBoolean(config, "include-comments", default = false)
    val includeTrivialExpressions =
        extractArgBoolean(config, "include-trivial-expressions", default = false)
    val onlyAstCache = extractArgBoolean(config, "only-ast-cache", default = false)
    val enableAstCache =
        extractArgBoolean(config, "enable-ast-cache", default = false) || onlyAstCache
    val defaultCacheDir = (config.inputPath / "ast_out").pathAsString
    val cacheDir        = extractArgString(config, "ast-cache-dir", default = defaultCacheDir)
    val baseConfig = CConfig(
      includeComments = includeComments,
      logProblems = false,
      includePathsAutoDiscovery = true
    )
        .withLogPreprocessor(false)
        .withInputPath(config.inputPath.pathAsString)
        .withOutputPath(outputAtomFile)
        .withFunctionBodies(functionBodies)
        .withIgnoredFilesRegex(".*(docs|example|samples|mocks|Documentation|demos).*")
        .withParseInactiveCode(parseInactive)
        .withImageLocations(imageLocations)
        .withIncludeTrivialExpressions(includeTrivialExpressions)
        .withAstCache(enableAstCache)
        .withCacheDir(cacheDir)
        .withOnlyAstCache(onlyAstCache)

    val finalConfig = baseConfig
        .withDefines(defines)
        .withCppStandard(cppStandard)
        .withIncludePaths(C2ATOM_INCLUDE_PATH ++ extraIncludes)
    new C2Cpg().createCpgWithOverlays(finalConfig)
  end createC2Cpg

  private def createJimple2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
      new Jimple2Cpg().createCpgWithOverlays(
        JimpleConfig(android = androidJarPath, fullResolver = true)
            .withRecurse(true)
            .withDepth(10)
            .withInputPath(config.inputPath.pathAsString)
            .withOutputPath(outputAtomFile)
            .withFullResolver(true)
      )

  private def createJavaSrc2Cpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
      new JavaSrc2Cpg().createCpgWithOverlays(
        JavaConfig(
          fetchDependencies = true,
          inferenceJarPaths = JAR_INFERENCE_PATHS,
          enableTypeRecovery = true,
          delombokMode = Some(DEFAULT_DELOMBOK_MODE)
        )
            .withInputPath(config.inputPath.pathAsString)
            .withIgnoredFilesRegex(".*(target|build)/(generated|intermediates|outputs|tmp).*")
            .withOutputPath(outputAtomFile)
      )

  private def createScalaCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
    handleScalaSemantics(config)
    new Jimple2Cpg().createCpgWithOverlays(
      JimpleConfig(scalaSdk = Option(System.getProperty("java.class.path")))
          .withInputPath(config.inputPath.pathAsString)
          .withOutputPath(outputAtomFile)
          .withFullResolver(true)
          .withOnlyClasses(true)
          .withDepth(1)
          .withRecurse(true)
    )

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
    val finalConfig = sys.env.get("CHEN_ASTGEN_OUT") match
      case Some(dir) => initialConfig.withAstGenOutDir(dir)
      case None      => initialConfig
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
      new Py2CpgOnFileSystem()
          .createCpgWithOverlays(
            PyConfig()
                .withDisableDummyTypes(true)
                .withTypePropagationIterations(TYPE_PROPAGATION_ITERATIONS)
                .withInputPath(config.inputPath.pathAsString)
                .withOutputPath(outputAtomFile)
                .withDefaultIgnoredFilesRegex(List("\\..*".r))
                .withIgnoredFilesRegex(pythonIgnoredFilesRegex)
          )
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

  private def createPhpCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
      new Php2Atom().createCpgWithOverlays(
        PhpConfig()
            .withDisableDummyTypes(true)
            .withInputPath(config.inputPath.pathAsString)
            .withOutputPath(outputAtomFile)
            .withDefaultIgnoredFilesRegex(List("\\..*".r))
            .withIgnoredFilesRegex(".*(samples|examples|docs).*")
      ).map { ag =>
        new PhpSetKnownTypesPass(ag).createAndApply()
        ag
      }

  private def createRubyCpg(config: AtomConfig, outputAtomFile: String): Try[Cpg] =
      new Ruby2Atom().createCpgWithOverlays(
        RubyConfig()
            .withInputPath(config.inputPath.pathAsString)
            .withOutputPath(outputAtomFile)
            .withIgnoredFilesRegex(".*(docs|vendor|spec).*")
      ).map { ag =>
          ag
      }

  private def enhanceCpg(config: AtomConfig, atom: Cpg): Either[String, Unit] =
      config match
        case x: AtomConfig if needsDataFlowEnhancement(x) =>
            println("Generating data-flow dependencies from atom. Please wait ...")
            try
              new OssDataFlow(new OssDataFlowOptions(maxNumberOfDefinitions = x.maxNumDef))
                  .run(new LayerCreatorContext(atom))
              new CdxPass(atom).createAndApply()
              new EasyTagsPass(atom).createAndApply()
              new ChennaiTagsPass(atom).createAndApply()
              Right(())
            catch
              case npe: NullPointerException
                  if npe.getMessage != null &&
                      npe.getMessage.contains("AdjacentNodes") =>
                  Left(s"CPG appears to be corrupted with broken references. " +
                      s"Try removing the atom file and regenerating it. Error: ${npe.getMessage}")
              case ex: Exception =>
                  Left(
                    s"Failed to enhance CPG: ${ex.getMessage} ${ex.getStackTrace.take(40).mkString("\n")}"
                  )
        case _ =>
            new EasyTagsPass(atom).createAndApply()
            Right(())

  private def needsDataFlowEnhancement(config: AtomConfig): Boolean =
      !config.reuseAtom && (config.dataDeps ||
          config.isInstanceOf[AtomDataFlowConfig] ||
          config.isInstanceOf[AtomReachablesConfig])

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
