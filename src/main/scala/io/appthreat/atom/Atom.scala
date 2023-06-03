package io.appthreat.atom

import better.files.{File => ScalaFile}
import io.joern.c2cpg.{C2Cpg, Config => CConfig}
import io.joern.dataflowengineoss.layers.dataflows.{OssDataFlow, OssDataFlowOptions}
import io.joern.dataflowengineoss.slicing.SliceMode._
import io.joern.dataflowengineoss.slicing._
import io.joern.javasrc2cpg.{JavaSrc2Cpg, Config => JavaConfig}
import io.joern.jimple2cpg.{Jimple2Cpg, Config => JimpleConfig}
import io.joern.jssrc2cpg.{JsSrc2Cpg, Config => JSConfig}
import io.joern.jssrc2cpg.passes.{JavaScriptInheritanceNamePass, ConstClosurePass}
import io.joern.pysrc2cpg.{Py2CpgOnFileSystem, Py2CpgOnFileSystemConfig => PyConfig}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.semanticcpg.layers.LayerCreatorContext

import scopt.OptionParser

import scala.language.postfixOps
import scala.util.{Failure, Success, Using}

object Atom {
  private val DEFAULT_ATOM_OUT_FILE      = "app.atom"
  private val DEFAULT_SLICE_OUT_FILE     = "slices.json"
  private val DEFAULT_SLICE_DEPTH        = 3
  private val DEFAULT_MAX_DEFS: Int      = 2000
  private val MAVEN_JAR_PATH: ScalaFile  = ScalaFile.home / ".m2"
  private val GRADLE_JAR_PATH: ScalaFile = ScalaFile.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  private val SBT_JAR_PATH: ScalaFile    = ScalaFile.home / ".ivy2" / "cache"
  private val JAR_INFERENCE_PATHS: Set[String] =
    Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)
  private val ANDROID_JAR_PATH: Option[String] = Option(System.getenv("ANDROID_HOME")).flatMap(androidHome =>
    ScalaFile(androidHome).glob("**/android.jar").map(_.pathAsString).toSeq.headOption
  )

  implicit val sliceModeRead: scopt.Read[SliceModes] =
    scopt.Read.reads(SliceMode withName)

  def main(args: Array[String]): Unit = {
    run(args) match {
      case Right(msg) => println(msg)
      case Left(errMsg) =>
        println(s"Failure: $errMsg")
        System.exit(1)
    }
  }

  val optionParser: OptionParser[ParserConfig] = new scopt.OptionParser[ParserConfig]("atom") {
    arg[String]("input")
      .optional()
      .text("source file or directory")
      .action((x, c) => c.copy(inputPath = x))
    opt[String]('o', "output")
      .text("output filename. Default app.atom")
      .action((x, c) => c.copy(outputAtomFile = x))
    opt[String]('l', "language")
      .text("source language")
      .action((x, c) => c.copy(language = x))
    note("Misc")
    opt[Unit]('s', "slice")
      .text("export intra-procedural slices as json")
      .action((_, c) => c.copy(slice = true))
    opt[String]("slice-outfile")
      .text("slice output filename")
      .action((x, c) => c.copy(outputSliceFile = x))
    opt[Int]("slice-depth")
      .text("the max depth to traverse the DDG for the data-flow slice (for `DataFlow` mode) - defaults to 3")
      .action((x, c) => c.copy(sliceDepth = x))
    opt[SliceModes]('m', "mode")
      .text(s"the kind of slicing to perform - defaults to `DataFlow`. Options: [${SliceMode.values.mkString(", ")}]")
      .action((x, c) => c.copy(sliceMode = x))
    opt[Int]("max-num-def")
      .text("maximum number of definitions in per-method data flow calculation. Default 2000")
      .action((x, c) => c.copy(maxNumDef = x))
    help("help").text("display this help message")
  }

  private def run(args: Array[String]): Either[String, String] = {
    val parserArgs = args.toList
    parseConfig(parserArgs) match {
      case Right(config) =>
        run(config)
      case Left(err) => Left(err)
    }
  }

  def newCpgCreatedString(path: String): String = {
    val absolutePath = ScalaFile(path).path.toAbsolutePath
    s"Atom created successfully at $absolutePath\n"
  }

  private def run(config: ParserConfig): Either[String, String] =
    for {
      _        <- checkInputPath(config)
      language <- getLanguage(config)
      _        <- generateAtom(config, language)
      -        <- generateSlice(config)
    } yield newCpgCreatedString(config.outputAtomFile)
  private def checkInputPath(config: ParserConfig): Either[String, Unit] = {
    if (config.inputPath == "") {
      println(optionParser.usage)
      Left("Input path required")
    } else if (!ScalaFile(config.inputPath).exists) {
      Left("Input path does not exist at `" + config.inputPath + "`, exiting.")
    } else {
      Right(())
    }
  }

  private def getLanguage(config: ParserConfig): Either[String, String] = {
    if (config.language.isEmpty) {
      Left(s"Please specify a language using the --language option.")
    } else {
      Right(config.language)
    }
  }

  private def generateForLanguage(language: String, config: ParserConfig): Either[String, String] = {
    (language match {
      case Languages.C | Languages.NEWC | "CPP" | "C++" =>
        new C2Cpg()
          .createCpgWithOverlays(
            CConfig(
              inputPath = config.inputPath,
              outputPath = config.outputAtomFile,
              ignoredFilesRegex = ".*(test|docs|examples|samples|mocks).*".r,
              includeComments = false,
              logProblems = false,
              includePathsAutoDiscovery = false
            )
          )
          .map(_.close())
      case "JAR" | "JIMPLE" | "ANDROID" | "APK" | "DEX" =>
        new Jimple2Cpg()
          .createCpgWithOverlays(
            JimpleConfig(inputPath = config.inputPath, outputPath = config.outputAtomFile, android = ANDROID_JAR_PATH)
          )
          .map(_.close())
      case Languages.JAVA | Languages.JAVASRC =>
        new JavaSrc2Cpg()
          .createCpgWithOverlays(
            JavaConfig(
              inputPath = config.inputPath,
              outputPath = config.outputAtomFile,
              fetchDependencies = true,
              inferenceJarPaths = JAR_INFERENCE_PATHS
            )
          )
          .map(_.close())
      case Languages.JSSRC | Languages.JAVASCRIPT | "JS" | "TS" | "TYPESCRIPT" =>
        new JsSrc2Cpg()
          .createCpgWithOverlays(
            JSConfig(inputPath = config.inputPath, outputPath = config.outputAtomFile, disableDummyTypes = true)
          )
          .map { cpg =>
            new OssDataFlow(new OssDataFlowOptions(maxNumberOfDefinitions = config.maxNumDef))
              .run(new LayerCreatorContext(cpg))
            new JavaScriptInheritanceNamePass(cpg)
            new ConstClosurePass(cpg)
            cpg
          }
          .map(_.close())
      case Languages.PYTHONSRC | Languages.PYTHON | "PY" =>
        new Py2CpgOnFileSystem()
          .createCpgWithOverlays(
            PyConfig(
              inputDir = ScalaFile(config.inputPath).path,
              outputFile = ScalaFile(config.outputAtomFile).path,
              disableDummyTypes = true
            )
          )
          .map(_.close())
      case _ => Failure(new RuntimeException(s"No language frontend supported for language '$language'"))
    }) match {
      case Failure(exception) =>
        Left(exception.getMessage)
      case Success(_) =>
        Right("Atom generation successful")
    }
  }

  private def saveSlice(outFile: ScalaFile, programSlice: ProgramSlice): Unit = {

    val finalOutputPath =
      ScalaFile(outFile.pathAsString)
        .createFileIfNotExists()
        .write(programSlice.toJson)
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

  private def generateSlice(config: ParserConfig): Either[String, String] = {
    def sliceCpg(cpg: Cpg): ProgramSlice = {
      val sliceConfig = SliceConfig(
        inputPath = ScalaFile(config.inputPath),
        outFile = ScalaFile(config.outputSliceFile),
        sliceMode = config.sliceMode,
        excludeOperatorCalls = true,
        typeRecoveryDummyTypes = false,
        sliceDepth = config.sliceDepth,
        minNumCalls = 1
      )
      config.sliceMode match {
        case DataFlow => DataFlowSlicing.calculateDataFlowSlice(cpg, sliceConfig)
        case Usages   => UsageSlicing.calculateUsageSlice(cpg, sliceConfig)
      }

    }

    try {
      if (config.slice) {
        saveSlice(
          ScalaFile(config.outputSliceFile),
          (
            Using.resource(loadFromOdb(config.outputAtomFile)) { cpg =>
              {
                val slice = sliceCpg(cpg)
                cpg.close()
                slice
              }
            }
          )
        )
      }
      Right("Atom sliced successfully")
    } catch {
      case err: Throwable => Left(err.getMessage)
    }
  }

  private def generateAtom(config: ParserConfig, language: String): Right[Nothing, String] = {
    generateForLanguage(language.toUpperCase, config)
    Right(s"Atom generation successful for $language")
  }

  case class ParserConfig(
    inputPath: String = "",
    outputAtomFile: String = DEFAULT_ATOM_OUT_FILE,
    outputSliceFile: String = DEFAULT_SLICE_OUT_FILE,
    slice: Boolean = false,
    sliceMode: SliceModes = DataFlow,
    sliceDepth: Int = DEFAULT_SLICE_DEPTH,
    language: String = "",
    maxNumDef: Int = DEFAULT_MAX_DEFS
  )

  private def parseConfig(parserArgs: List[String]): Either[String, ParserConfig] = {
    optionParser.parse(parserArgs, ParserConfig()) match {
      case Some(config) => Right(config)
      case None =>
        Left("Could not parse command line options")
    }
  }
}
