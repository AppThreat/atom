package io.appthreat.atom

import better.files.{File => ScalaFile}
import io.joern.console.cpgcreation.{CpgGenerator, guessLanguage}
import io.joern.c2cpg.{Config => CConfig, C2Cpg}
import io.joern.javasrc2cpg.{Config => JavaConfig, JavaSrc2Cpg}
import io.joern.jssrc2cpg.{Config => JSConfig, JsSrc2Cpg}
import io.joern.pysrc2cpg.Py2Cpg
import io.joern.joerncli.CpgBasedTool.{newCpgCreatedString, splitArgs}
import io.joern.joerncli.DefaultOverlays
import io.shiftleft.codepropertygraph.generated.Languages

object Atom {
  // Special string used to separate opts from frontend-specific opts
  val ARGS_DELIMITER             = "--frontend-args"
  val DEFAULT_CPG_OUT_FILE       = "cpg.bin"
  var generator: CpgGenerator    = _
  val MAVEN_JAR_PATH: ScalaFile  = ScalaFile.home / ".m2"
  val GRADLE_JAR_PATH: ScalaFile = ScalaFile.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  val SBT_JAR_PATH: ScalaFile    = ScalaFile.home / ".ivy2" / "cache"
  val JAR_INFERENCE_PATHS: Set[String] =
    Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)

  def main(args: Array[String]): Unit = {
    run(args) match {
      case Right(msg) => println(msg)
      case Left(errMsg) =>
        println(s"Failure: $errMsg")
        System.exit(1)
    }
  }

  val optionParser = new scopt.OptionParser[ParserConfig]("atom") {
    arg[String]("input")
      .optional()
      .text("source file or directory")
      .action((x, c) => c.copy(inputPath = x))
    opt[String]('o', "output")
      .text("output filename")
      .action((x, c) => c.copy(outputCpgFile = x))
    opt[String]("language")
      .text("source language")
      .action((x, c) => c.copy(language = x))
    opt[Unit]("overlays")
      .text("apply default overlays")
      .action((_, c) => c.copy(enhance = true))
    note("Misc")
    help("help").text("display this help message")
    note(s"Args specified after the $ARGS_DELIMITER separator will be passed to the front-end verbatim")
  }

  private def run(args: Array[String]): Either[String, String] = {
    val (parserArgs, frontendArgs) = splitArgs(args)
    parseConfig(parserArgs) match {
      case Right(config) =>
        run(config, frontendArgs)
      case Left(err) => Left(err)
    }
  }

  private def run(config: ParserConfig, frontendArgs: List[String] = List.empty): Either[String, String] =
    for {
      _        <- checkInputPath(config)
      language <- getLanguage(config)
      _        <- generateAtom(frontendArgs, config, language)
      _        <- applyDefaultOverlays(config)
    } yield newCpgCreatedString(config.outputCpgFile)
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
      val inputPath = config.inputPath
      guessLanguage(inputPath) match {
        case Some(guess) => Right(guess)
        case None =>
          Left(
            s"Could not guess language from input path $inputPath. Please specify a language using the --language option."
          )
      }
    } else {
      Right(config.language)
    }
  }

  def generateForLanguage(language: String, config: ParserConfig, args: List[String]): Either[String, String] = {
    language match {
      case Languages.C | Languages.NEWC =>
        Some(
          C2Cpg().createCpg(
            CConfig(
              inputPath = config.inputPath,
              outputPath = config.outputCpgFile,
              ignoredFilesRegex = ".*(test|docs|examples|samples|mocks).*".r,
              includeComments = false,
              logProblems = false,
              includePathsAutoDiscovery = false
            )
          )
        )
      case Languages.JAVA | Languages.JAVASRC =>
        Some(
          JavaSrc2Cpg().createCpg(
            JavaConfig(
              inputPath = config.inputPath,
              outputPath = config.outputCpgFile,
              fetchDependencies = true,
              inferenceJarPaths = JAR_INFERENCE_PATHS
            )
          )
        )
      case Languages.JSSRC | Languages.JAVASCRIPT =>
        Some(JsSrc2Cpg().createCpg(JSConfig()))
      case Languages.PYTHONSRC => None
      case Languages.PYTHON    => None
      case Languages.PHP       => None
      case _                   => None
    }
    Right("Code property graph generation successful")
  }

  private def generateAtom(
    frontendArgs: List[String],
    config: ParserConfig,
    language: String
  ): Right[Nothing, String] = {
    generateForLanguage(language.toUpperCase, config, frontendArgs)
    Right(s"Code property graph generation successful for $language")
  }

  private def applyDefaultOverlays(config: ParserConfig): Either[String, String] = {
    try {
      if (config.enhance) {
        println("[+] Applying default overlays")
        val cpg = DefaultOverlays.create(config.outputCpgFile, config.maxNumDef)
        generator.applyPostProcessingPasses(cpg)
        cpg.close()
      } else {}
      Right("Code property graph generation successful")
    } catch {
      case err: Throwable => Left(err.getMessage)
    }
  }

  case class ParserConfig(
    inputPath: String = "",
    outputCpgFile: String = DEFAULT_CPG_OUT_FILE,
    enhance: Boolean = false,
    language: String = "",
    maxNumDef: Int = DefaultOverlays.defaultMaxNumberOfDefinitions
  )

  private def parseConfig(parserArgs: List[String]): Either[String, ParserConfig] = {
    optionParser.parse(parserArgs, ParserConfig()) match {
      case Some(config) => Right(config)
      case None =>
        Left("Could not parse command line options")
    }
  }
}
