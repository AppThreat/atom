package io.appthreat.atom

import better.files.File
import io.appthreat.atom.Atom.loadFromOdb
import io.appthreat.atom.dataflows.{DataFlowGraph, OssDataFlow, OssDataFlowOptions}
import io.appthreat.atom.parsedeps.{AtomSlice, parseDependencies}
import io.joern.c2cpg.{C2Cpg, Config as CConfig}
import io.joern.dataflowengineoss.slicing.{UsageSlicing, *}
import io.joern.javasrc2cpg.{JavaSrc2Cpg, Config as JavaConfig}
import io.joern.jimple2cpg.{Jimple2Cpg, Config as JimpleConfig}
import io.joern.jssrc2cpg.passes.{ConstClosurePass, JavaScriptInheritanceNamePass}
import io.joern.jssrc2cpg.{JsSrc2Cpg, Config as JSConfig}
import io.joern.pysrc2cpg.{
  DynamicTypeHintFullNamePass,
  Py2CpgOnFileSystem,
  PythonInheritanceNamePass,
  PythonTypeHintCallLinker,
  PythonTypeRecoveryPass,
  ImportsPass as PythonImportsPass,
  Py2CpgOnFileSystemConfig as PyConfig
}
import io.joern.x2cpg.passes.base.AstLinkerPass
import io.joern.x2cpg.passes.callgraph.NaiveCallLinker
import io.joern.x2cpg.passes.frontend.XTypeRecoveryConfig
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.semanticcpg.layers.LayerCreatorContext
import scopt.OptionParser

import scala.language.postfixOps
import scala.util.{Failure, Properties, Success, Using}

object Atom {

  val DEFAULT_ATOM_OUT_FILE: String = if (Properties.isWin) "app.atom" else "app.⚛"
  val DEFAULT_SLICE_OUT_FILE        = "slices.json"
  val DEFAULT_SLICE_DEPTH           = 3
  val DEFAULT_MAX_DEFS: Int         = 2000
  private val MAVEN_JAR_PATH: File  = File.home / ".m2"
  private val GRADLE_JAR_PATH: File = File.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  private val SBT_JAR_PATH: File    = File.home / ".ivy2" / "cache"
  private val JAR_INFERENCE_PATHS: Set[String] =
    Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)
  private val ANDROID_JAR_PATH: Option[String] = Option(System.getenv("ANDROID_HOME")).flatMap(androidHome =>
    File(androidHome).glob("**/android.jar").map(_.pathAsString).toSeq.headOption
  )

  def main(args: Array[String]): Unit = {
    run(args) match {
      case Right(msg) =>
        println(msg)
      case Left(errMsg) =>
        println(s"Failure: $errMsg")
        System.exit(1)
    }
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
    opt[String]("slice-outfile")
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
      .text(s"the name of the source file to generate slices from.")
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
      .text("maximum number of definitions in per-method data flow calculation - defaults to 2000")
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
          .text(s"the max depth to traverse the DDG for the data-flow slice - defaults to 3.")
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
          ),
        opt[Unit]("end-at-external-method")
          .text(s"all slices must end at an external method - defaults to false.")
          .action((_, c) =>
            c match {
              case c: AtomDataFlowConfig => c.copy(mustEndAtExternalMethod = true)
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
        opt[Unit]("exclude-operators")
          .text(s"excludes operator calls in the slices - defaults to true.")
          .action((_, c) =>
            c match {
              case c: AtomUsagesConfig => c.copy(excludeOperatorCalls = true)
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
    help("help").text("display this help message")
  }

  private def run(args: Array[String]): Either[String, String] = {
    val parserArgs = args.toList
    parseConfig(parserArgs) match {
      case Right(config: AtomConfig) => run(config, config.language)
      case Right(_)                  => Left("Invalid configuration generated")
      case Left(err)                 => Left(err)
    }
  }

  def newCpgCreatedString(path: String): String = {
    val absolutePath = File(path).path.toAbsolutePath
    s"Atom created successfully at $absolutePath\n"
  }

  private def run(config: BaseConfig, language: String): Either[String, String] = {
    for {
      _ <- generateAtom(config, language)
      _ <- generateSlice(config)
    } yield newCpgCreatedString(config match
      case config: AtomConfig => config.outputAtomFile.pathAsString
      case _                  => DEFAULT_ATOM_OUT_FILE
    )
  }

  private def generateForLanguage(language: String, config: BaseConfig): Either[String, String] = {
    val outputAtomFile = config match
      case x: AtomConfig => x.outputAtomFile.pathAsString
      case _             => DEFAULT_ATOM_OUT_FILE

    (language match {
      case Languages.C | Languages.NEWC | "CPP" | "C++" =>
        new C2Cpg()
          .createCpgWithOverlays(
            CConfig(includeComments = false, logProblems = false, includePathsAutoDiscovery = false)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withIgnoredFilesRegex(".*(test|docs|examples|samples|mocks).*")
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
            JavaConfig(fetchDependencies = true, inferenceJarPaths = JAR_INFERENCE_PATHS)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
          )
      case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" =>
        new JsSrc2Cpg()
          .createCpgWithOverlays(
            JSConfig(disableDummyTypes = true)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
          )
          .map { cpg =>
            new JavaScriptInheritanceNamePass(cpg).createAndApply()
            new ConstClosurePass(cpg).createAndApply()
            new NaiveCallLinker(cpg).createAndApply()
            cpg
          }
      case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
        new Py2CpgOnFileSystem()
          .createCpgWithOverlays(
            PyConfig(disableDummyTypes = true)
              .withInputPath(config.inputPath.pathAsString)
              .withOutputPath(outputAtomFile)
              .withDefaultIgnoredFilesRegex(List("\\..*".r))
              .withIgnoredFilesRegex(
                ".*(samples|examples|test|tests|unittests|docs|virtualenvs|venv|benchmarks|tutorials).*"
              )
          )
          .map { cpg =>
            new PythonImportsPass(cpg).createAndApply()
            cpg
          }
      case _ => Failure(new RuntimeException(s"No language frontend supported for language '$language'"))
    }) match {
      case Failure(exception) =>
        Left(exception.getMessage)
      case Success(cpg) =>
        config match {
          case x: AtomConfig if x.dataDeps || x.isInstanceOf[AtomDataFlowConfig] =>
            println("Generating data-flow dependencies")
            new OssDataFlow(new OssDataFlowOptions(maxNumberOfDefinitions = x.maxNumDef))
              .run(new LayerCreatorContext(cpg))
          case _ =>
        }
        cpg.close()
        Right("Atom generation successful")
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
  private def loadFromOdb(filename: String): Cpg = {
    val odbConfig = overflowdb.Config.withDefaults().withStorageLocation(filename)
    val config    = CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    io.shiftleft.codepropertygraph.cpgloading.CpgLoader.loadFromOverflowDb(config)
  }

  private def migrateAtomConfigToSliceConfig(x: BaseConfig): BaseConfig =
    (x match {
      case config: AtomDataFlowConfig =>
        DataFlowConfig(config.sinkPatternFilter, config.mustEndAtExternalMethod, config.sliceDepth)
      case config: AtomUsagesConfig =>
        UsagesConfig(config.minNumCalls, config.excludeOperatorCalls, !config.includeMethodSource)
      case _ => x
    }).withInputPath(x.inputPath)
      .withOutputSliceFile(x.outputSliceFile)
      .withFileFilter(x.fileFilter)
      .withMethodNameFilter(x.methodNameFilter)
      .withMethodParamTypeFilter(x.methodParamTypeFilter)
      .withMethodAnnotationFilter(x.methodAnnotationFilter)

  private def generateSlice(config: BaseConfig): Either[String, String] = {
    def sliceCpg(cpg: Cpg): Option[ProgramSlice] =
      config match {
        case x: AtomDataFlowConfig =>
          val dataFlowConfig = migrateAtomConfigToSliceConfig(x)
          DataFlowSlicing.calculateDataFlowSlice(cpg, dataFlowConfig.asInstanceOf[DataFlowConfig])
        case x: AtomUsagesConfig =>
          val usagesConfig = migrateAtomConfigToSliceConfig(x)
          Option(UsageSlicing.calculateUsageSlice(cpg, usagesConfig.asInstanceOf[UsagesConfig]))
        case _ =>
          None
      }

    val outputAtomFile = config match
      case x: AtomConfig => x.outputAtomFile.pathAsString
      case _             => DEFAULT_ATOM_OUT_FILE

    try {
      migrateAtomConfigToSliceConfig(config) match {
        case _: DataFlowConfig =>
          val dataFlowSlice =
            Using.resource(loadFromOdb(outputAtomFile))(sliceCpg).collect { case x: DataFlowSlice => x }
          val atomDataFlowSliceJson =
            dataFlowSlice.map(x => AtomDataFlowSlice(x, DataFlowGraph.buildFromSlice(x).paths).toJson)
          saveSlice(config.outputSliceFile, atomDataFlowSliceJson)
        case _: UsagesConfig =>
          saveSlice(config.outputSliceFile, Using.resource(loadFromOdb(outputAtomFile))(sliceCpg).map(_.toJson))
        case x: AtomParseDepsConfig =>
          Using.resource(loadFromOdb(outputAtomFile))(parseDependencies).map(_.toJson) match {
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

  private def generateAtom(config: BaseConfig, language: String): Either[String, String] =
    generateForLanguage(language.toUpperCase, config)

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
