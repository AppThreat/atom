package io.appthreat.atom

import better.files.File
import io.appthreat.atom.dataflows.{DataFlowGraph, OssDataFlow, OssDataFlowOptions}
import io.appthreat.atom.parsedeps.parseDependencies
import io.appthreat.atom.passes.{SafeJSTypeRecoveryPass, TypeHintPass}
import io.appthreat.atom.slicing.*
import io.appthreat.atom.frontends.clike.C2Atom
import io.appthreat.c2cpg.{C2Cpg, Config as CConfig}
import io.appthreat.javasrc2cpg.{JavaSrc2Cpg, Config as JavaConfig}
import io.appthreat.jimple2cpg.{Jimple2Cpg, Config as JimpleConfig}
import io.appthreat.jssrc2cpg.passes.{ConstClosurePass, ImportResolverPass, JavaScriptInheritanceNamePass}
import io.appthreat.jssrc2cpg.{JsSrc2Cpg, Config as JSConfig}
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
import io.appthreat.x2cpg.passes.base.AstLinkerPass
import io.appthreat.x2cpg.passes.frontend.XTypeRecoveryConfig
import io.appthreat.x2cpg.passes.taggers.{CdxPass, ChennaiTagsPass}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.semanticcpg.layers.LayerCreatorContext
import scopt.OptionParser

import scala.language.postfixOps
import scala.util.{Failure, Properties, Success}

object Atom {

  val DEFAULT_ATOM_OUT_FILE: String       = if (Properties.isWin) "app.atom" else "app.⚛"
  val DEFAULT_SLICE_OUT_FILE              = "slices.json"
  val DEFAULT_SLICE_DEPTH                 = 7
  val DEFAULT_MAX_DEFS: Int               = 2000
  val FRAMEWORK_INPUT_TAG: String         = "framework-input"
  val FRAMEWORK_OUTPUT_TAG: String        = "framework-output"
  private val TYPE_PROPAGATION_ITERATIONS = 1
  private val MAVEN_JAR_PATH: File        = File.home / ".m2" / "repository"
  private val GRADLE_JAR_PATH: File       = File.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  private val SBT_JAR_PATH: File          = File.home / ".ivy2" / "cache"
  private val JAR_INFERENCE_PATHS: Set[String] =
    Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)
  private val ANDROID_JAR_PATH: Option[String] = Option(System.getenv("ANDROID_HOME")).flatMap { androidHome =>
    if (File(androidHome).isDirectory) File(androidHome).glob("**/android.jar").map(_.pathAsString).toSeq.headOption
    else None
  }

  // unused since it slows down the cdt parser
  private var C2ATOM_INCLUDE_PATHS: scala.collection.mutable.Set[String] = scala.collection.mutable.Set(
    "/usr/include",
    "/usr/local/include",
    "/usr/lib/gcc/x86_64-linux-gnu",
    "/usr/include/c++/11",
    "/usr/include/c++/11/backward",
    "/usr/lib/gcc/x86_64-redhat-linux",
    "/opt/local/include/postgresql14",
    "/opt/local/include",
    "/usr/include/tcl8.6",
    "/usr/include/tcl8.6/tcl-private/generic",
    "/usr/include/4ti2",
    "/usr/src/AMF",
    "/usr/src/linux",
    "/usr/share",
    "/usr/lib64/R",
    "/usr/lib/R",
    "/opt/ebuilds",
    "/opt/homebrew/include",
    "/home/linuxbrew/.linuxbrew/include"
  )
  Option(System.getenv("C_INCLUDE_PATH")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS ++ ipath.split(java.io.File.pathSeparator)
    None
  }
  Option(System.getenv("%ProgramFiles(x86)%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  Option(System.getenv("%CommonProgramFiles(x86)%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  Option(System.getenv("%ProgramW6432%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  Option(System.getenv("%CommonProgramW6432%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  Option(System.getenv("%ProgramFiles%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  Option(System.getenv("%CommonProgramFiles%")).flatMap { ipath =>
    C2ATOM_INCLUDE_PATHS += ipath
    None
  }
  private val optionParser: OptionParser[BaseConfig] = new scopt.OptionParser[BaseConfig]("atom") {
    arg[String]("input")
      .optional()
      .text("source file or directory")
      .action((x, c) => c.withInputPath(File(x)))
      .validate { x =>
        if (x == "") failure("Input path required")
        else if (!File(x).exists) failure(s"Input path does not exist at `\"$x\"`, exiting.")
        else success
      }
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
      .validate(x => if (x.isBlank) failure(s"Please specify a language using the --language option.") else success)
    opt[Unit]("with-data-deps")
      .text("generate the atom with data-dependencies - defaults to `false`")
      .action((_, c) =>
        c match
          case config: AtomConfig => config.withDataDependencies(true)
          case _                  => c
      )
    opt[String]("file-filter")
      .text(s"the name of the source file to generate slices from. Uses regex.")
      .action((x, c) => c.withFileFilter(Option(x)))
    opt[String]("method-name-filter")
      .text(s"filters in slices that go through specific methods by names. Uses regex.")
      .action((x, c) => c.withMethodNameFilter(Option(x)))
    opt[String]("method-parameter-filter")
      .text(s"filters in slices that go through methods with specific types on the method parameters. Uses regex.")
      .action((x, c) => c.withMethodParamTypeFilter(Option(x)))
    opt[String]("method-annotation-filter")
      .text(s"filters in slices that go through methods with specific annotations on the methods. Uses regex.")
      .action((x, c) => c.withMethodAnnotationFilter(Option(x)))
    opt[Int]("max-num-def")
      .text(s"maximum number of definitions in per-method data flow calculation - defaults to $DEFAULT_MAX_DEFS")
      .action((x, c) =>
        c match
          case config: AtomConfig => config.withMaxNumDef(x)
          case _                  => c
      )
      .validate(x => if (x <= 0) failure("`max-num-def` must be an integer larger than 0") else success)
    cmd("parsedeps")
      .text("Extract dependencies from the build file and imports")
      .action((_, c) => AtomParseDepsConfig())
    cmd("data-flow")
      .text("Extract backward data-flow slices")
      .action((_, _) => AtomDataFlowConfig().withDataDependencies(true))
      .children(
        opt[Int]("slice-depth")
          .text(s"the max depth to traverse the DDG for the data-flow slice - defaults to $DEFAULT_SLICE_DEPTH.")
          .action((x, c) =>
            c match {
              case c: AtomDataFlowConfig => c.copy(sliceDepth = x)
              case _                     => c
            }
          ),
        opt[String]("sink-filter")
          .text(s"filters on the sink's `code` property. Uses regex.")
          .action((x, c) =>
            c match {
              case c: AtomDataFlowConfig => c.copy(sinkPatternFilter = Option(x))
              case _                     => c
            }
          )
      )
    cmd("usages")
      .text("Extract local variable and parameter usages")
      .action((_, c) => AtomUsagesConfig())
      .children(
        opt[Int]("min-num-calls")
          .text(s"the minimum number of calls required for a usage slice - defaults to 1.")
          .action((x, c) =>
            c match {
              case c: AtomUsagesConfig => c.copy(minNumCalls = x)
              case _                   => c
            }
          ),
        opt[Unit]("include-source")
          .text(s"includes method source code in the slices - defaults to false.")
          .action((_, c) =>
            c match {
              case c: AtomUsagesConfig => c.copy(includeMethodSource = true)
              case _                   => c
            }
          )
      )
    cmd("reachables")
      .text("Extract reachable data-flow slices based on automated framework tags")
      .action((_, c) => AtomReachablesConfig().withDataDependencies(true))
      .children(
        opt[String]("source-tag")
          .text(s"source tag - defaults to framework-input.")
          .action((x, c) =>
            c match {
              case c: AtomReachablesConfig => c.copy(sourceTag = x)
              case _                       => c
            }
          ),
        opt[String]("sink-tag")
          .text(s"sink tag - defaults to framework-output.")
          .action((x, c) =>
            c match {
              case c: AtomReachablesConfig => c.copy(sinkTag = x)
              case _                       => c
            }
          )
      )
    help("help").text("display this help message")
  }

  def main(args: Array[String]): Unit = {
    run(args) match {
      case Right(msg) =>
        println(msg)
      case Left(errMsg) =>
        println(s"Failure: $errMsg")
        System.exit(1)
    }
  }

  private def run(args: Array[String]): Either[String, String] = {
    val parserArgs = args.toList
    parseConfig(parserArgs) match {
      case Right(config: AtomConfig) => run(config, config.language)
      case Right(_)                  => Left("Invalid configuration generated")
      case Left(err)                 => Left(err)
    }
  }

  private def run(config: BaseConfig, language: String): Either[String, String] = {
    for {
      _ <- generateAtom(config, language)
    } yield newAtomCreatedString(config match
      case config: AtomConfig => config.outputAtomFile.pathAsString
      case _                  => DEFAULT_ATOM_OUT_FILE
    )
  }

  private def newAtomCreatedString(path: String): String = {
    val absolutePath = File(path).path.toAbsolutePath
    s"Atom created successfully at $absolutePath\n"
  }

  private def generateSlice(config: BaseConfig, cpg: Cpg): Either[String, String] = {
    def sliceCpg(cpg: Cpg): Option[ProgramSlice] =
      config match {
        case x: AtomDataFlowConfig =>
          println("Slicing the atom for data-flow. This might take a while ...")
          val dataFlowConfig = migrateAtomConfigToSliceConfig(x)
          new DataFlowSlicing().calculateDataFlowSlice(cpg, dataFlowConfig.asInstanceOf[DataFlowConfig])
        case x: AtomUsagesConfig =>
          println("Slicing the atom for usages. This might take a few minutes ...")
          new ChennaiTagsPass(cpg).createAndApply()
          val usagesConfig = migrateAtomConfigToSliceConfig(x)
          Option(UsageSlicing.calculateUsageSlice(cpg, usagesConfig.asInstanceOf[UsagesConfig]))
        case x: AtomReachablesConfig =>
          println("Slicing the atom for reachables. This might take a few minutes ...")
          val reachablesConfig = migrateAtomConfigToSliceConfig(x)
          Some(ReachableSlicing.calculateReachableSlice(cpg, reachablesConfig.asInstanceOf[ReachablesConfig]))
        case _ =>
          None
      }

    try {
      migrateAtomConfigToSliceConfig(config) match {
        case _: DataFlowConfig =>
          val dataFlowSlice = sliceCpg(cpg).collect { case x: DataFlowSlice => x }
          val atomDataFlowSliceJson =
            dataFlowSlice.map(x => AtomDataFlowSlice(x, DataFlowGraph.buildFromSlice(x).paths).toJson)
          saveSlice(config.outputSliceFile, atomDataFlowSliceJson)
        case _: UsagesConfig =>
          saveSlice(config.outputSliceFile, sliceCpg(cpg).map(_.toJson))
        case _: ReachablesConfig =>
          saveSlice(config.outputSliceFile, sliceCpg(cpg).map(_.toJsonPretty))
        case x: AtomParseDepsConfig =>
          parseDependencies(cpg).map(_.toJson) match {
            case Left(err)    => return Left(err)
            case Right(slice) => saveSlice(x.outputSliceFile, Option(slice))
          }
        case _ =>
      }
      Right("Atom sliced successfully")
    } catch {
      case err: Throwable if err.getMessage == null => Left(err.getStackTrace.take(7).mkString("\n"))
      case err: Throwable                           => Left(err.getMessage)
    }
  }

  private def saveSlice(outFile: File, programSlice: Option[String]): Unit =
    programSlice.foreach { slice =>
      val finalOutputPath =
        File(outFile.pathAsString)
          .createFileIfNotExists()
          .write(slice)
          .pathAsString
      println(s"Slices have been successfully written to $finalOutputPath")
    }

  /** Load code property graph from overflowDB
    *
    * @param filename
    *   name of the file that stores the CPG
    */
  private def migrateAtomConfigToSliceConfig(x: BaseConfig): BaseConfig =
    (x match {
      case config: AtomDataFlowConfig =>
        DataFlowConfig(
          config.sinkPatternFilter,
          config.excludeOperatorCalls,
          config.mustEndAtExternalMethod,
          config.sliceDepth
        )
      case config: AtomUsagesConfig =>
        UsagesConfig(config.minNumCalls, config.excludeOperatorCalls, !config.includeMethodSource)
      case config: AtomReachablesConfig =>
        ReachablesConfig(config.sourceTag, config.sinkTag)
      case _ => x
    }).withInputPath(x.inputPath)
      .withOutputSliceFile(x.outputSliceFile)
      .withFileFilter(x.fileFilter)
      .withMethodNameFilter(x.methodNameFilter)
      .withMethodParamTypeFilter(x.methodParamTypeFilter)
      .withMethodAnnotationFilter(x.methodAnnotationFilter)

  private def generateAtom(config: BaseConfig, language: String): Either[String, String] =
    generateForLanguage(language.toUpperCase, config)

  private def generateForLanguage(language: String, config: BaseConfig): Either[String, String] = {
    val outputAtomFile = config match
      case x: AtomConfig => x.outputAtomFile.pathAsString
      case _             => DEFAULT_ATOM_OUT_FILE

    (language match {
      case "H" | "HPP" =>
        new C2Atom()
          .createCpg(
            CConfig(includeComments = false, logProblems = false, includePathsAutoDiscovery = true)
              .withLogPreprocessor(false)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withFunctionBodies(false)
              .withIgnoredFilesRegex(".*(test|docs|examples|samples|mocks|Documentation|demos).*")
          )
      case Languages.C | Languages.NEWC | "CPP" | "C++" =>
        new C2Cpg()
          .createCpgWithOverlays(
            CConfig(includeComments = false, logProblems = false, includePathsAutoDiscovery = true)
              .withLogPreprocessor(false)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withFunctionBodies(true)
              .withIgnoredFilesRegex(".*(test|docs|examples|samples|mocks|Documentation|demos).*")
          )
      case "JAR" | "JIMPLE" | "ANDROID" | "APK" | "DEX" =>
        new Jimple2Cpg()
          .createCpgWithOverlays(
            JimpleConfig(android = ANDROID_JAR_PATH)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withFullResolver(true)
          )
      case Languages.JAVA | Languages.JAVASRC =>
        new JavaSrc2Cpg()
          .createCpgWithOverlays(
            JavaConfig(fetchDependencies = true, inferenceJarPaths = JAR_INFERENCE_PATHS, enableTypeRecovery = true)
              .withInputPath(config.inputPath.pathAsString)
              .withDefaultIgnoredFilesRegex(
                List("\\..*".r, ".*build/(generated|intermediates|outputs|tmp).*" r, ".*src/test.*" r)
              )
              .withOutputPath(outputAtomFile)
          )
      case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" =>
        new JsSrc2Cpg()
          .createCpgWithOverlays(
            JSConfig()
              .withDisableDummyTypes(true)
              .withTypePropagationIterations(TYPE_PROPAGATION_ITERATIONS)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
          )
          .map { ag =>
            new JavaScriptInheritanceNamePass(ag).createAndApply()
            new ConstClosurePass(ag).createAndApply()
            new ImportResolverPass(ag).createAndApply()
            new SafeJSTypeRecoveryPass(ag).createAndApply()
            new TypeHintPass(ag).createAndApply()
            ag
          }
      case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
        new Py2CpgOnFileSystem()
          .createCpgWithOverlays(
            PyConfig()
              .withDisableDummyTypes(true)
              .withTypePropagationIterations(TYPE_PROPAGATION_ITERATIONS)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withDefaultIgnoredFilesRegex(List("\\..*".r))
              .withIgnoredFilesRegex(
                ".*(samples|examples|test|tests|unittests|docs|virtualenvs|venv|benchmarks|tutorials|noxfile).*"
              )
          )
          .map { ag =>
            new PythonImportsPass(ag).createAndApply()
            new PyImportResolverPass(ag).createAndApply()
            new DynamicTypeHintFullNamePass(ag).createAndApply()
            new PythonInheritanceNamePass(ag).createAndApply()
            new PythonTypeRecoveryPass(ag, XTypeRecoveryConfig(enabledDummyTypes = false))
              .createAndApply()
            new PythonTypeHintCallLinker(ag).createAndApply()
            new AstLinkerPass(ag).createAndApply()
            ag
          }
      case _ => Failure(new RuntimeException(s"No language frontend supported for language '$language'"))
    }) match {
      case Failure(exception) =>
        Left(exception.getMessage)
      case Success(ag) =>
        config match {
          case x: AtomConfig if x.dataDeps || x.isInstanceOf[AtomDataFlowConfig] =>
            println("Generating data-flow dependencies from atom. Please wait ...")
            // Enhance with the BOM from cdxgen
            new CdxPass(ag).createAndApply()
            new ChennaiTagsPass(ag).createAndApply()
            new OssDataFlow(new OssDataFlowOptions(maxNumberOfDefinitions = x.maxNumDef))
              .run(new LayerCreatorContext(ag))
          case _ =>
        }
        generateSlice(config, ag)
        ag.close()
        Right("Atom generation successful")
    }
  }

  private def parseConfig(parserArgs: List[String]): Either[String, BaseConfig] = {
    optionParser.parse(
      parserArgs,
      DefaultAtomConfig()
        .withOutputAtomFile(File(DEFAULT_ATOM_OUT_FILE))
        .withOutputSliceFile(File(DEFAULT_SLICE_OUT_FILE))
    ) match {
      case Some(config) =>
        Right(config)
      case None =>
        Left("Could not parse command line options")
    }
  }
}
