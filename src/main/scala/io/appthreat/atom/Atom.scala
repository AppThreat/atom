package io.appthreat.atom

import better.files.{File => ScalaFile}
import io.joern.c2cpg.{C2Cpg, Config => CConfig}
import io.joern.javasrc2cpg.{JavaSrc2Cpg, Config => JavaConfig}
import io.joern.jimple2cpg.{Jimple2Cpg, Config => JimpleConfig}
import io.joern.jssrc2cpg.{JsSrc2Cpg, Config => JSConfig}
import io.joern.pysrc2cpg.{Py2CpgOnFileSystem, Py2CpgOnFileSystemConfig => PyConfig}
import io.shiftleft.codepropertygraph.generated.Languages
import scopt.OptionParser

object Atom {
  val DEFAULT_CPG_OUT_FILE       = "cpg.bin"
  val DEFAULT_MAX_DEFS: Int      = 4000
  val MAVEN_JAR_PATH: ScalaFile  = ScalaFile.home / ".m2"
  val GRADLE_JAR_PATH: ScalaFile = ScalaFile.home / ".gradle" / "caches" / "modules-2" / "files-2.1"
  val SBT_JAR_PATH: ScalaFile    = ScalaFile.home / ".ivy2" / "cache"
  val JAR_INFERENCE_PATHS: Set[String] =
    Set(MAVEN_JAR_PATH.pathAsString, GRADLE_JAR_PATH.pathAsString, SBT_JAR_PATH.pathAsString)
  val ANDROID_HOME: String = System.getenv("ANDROID_HOME")
  var ANDROID_JAR_PATH: String = Option(ANDROID_HOME) match {
    case Some(ANDROID_HOME) => {
      val jars = ScalaFile(ANDROID_HOME).glob("**/android.jar")
      if (jars.nonEmpty) {
        jars.next().pathAsString
      } else {
        ""
      }
    }
    case _ => ""
  }

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
      .text("output filename")
      .action((x, c) => c.copy(outputCpgFile = x))
    opt[String]("language")
      .text("source language")
      .action((x, c) => c.copy(language = x))
    note("Misc")
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
    s"CPG created successfully at $absolutePath\n"
  }

  private def run(config: ParserConfig): Either[String, String] =
    for {
      _        <- checkInputPath(config)
      language <- getLanguage(config)
      _        <- generateAtom(config, language)
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
      Left(s"Please specify a language using the --language option.")
    } else {
      Right(config.language)
    }
  }

  def generateForLanguage(language: String, config: ParserConfig): Either[String, String] = {
    language match {
      case Languages.C | Languages.NEWC =>
        Some(
          new C2Cpg()
            .createCpgWithOverlays(
              CConfig(
                inputPath = config.inputPath,
                outputPath = config.outputCpgFile,
                ignoredFilesRegex = ".*(test|docs|examples|samples|mocks).*".r,
                includeComments = false,
                logProblems = false,
                includePathsAutoDiscovery = false
              )
            )
            .get
            .close()
        )
      case "jar" | "jimple" | "android" | "apk" | "dex" =>
        Some(
          new Jimple2Cpg()
            .createCpgWithOverlays(
              JimpleConfig(
                inputPath = config.inputPath,
                outputPath = config.outputCpgFile,
                android = Some(ANDROID_JAR_PATH)
              )
            )
            .get
            .close()
        )
      case Languages.JAVA | Languages.JAVASRC =>
        Some(
          new JavaSrc2Cpg()
            .createCpgWithOverlays(
              JavaConfig(
                inputPath = config.inputPath,
                outputPath = config.outputCpgFile,
                fetchDependencies = true,
                inferenceJarPaths = JAR_INFERENCE_PATHS
              )
            )
            .get
            .close()
        )
      case Languages.JSSRC | Languages.JAVASCRIPT =>
        Some(
          new JsSrc2Cpg()
            .createCpgWithAllOverlays(
              JSConfig(inputPath = config.inputPath, outputPath = config.outputCpgFile, disableDummyTypes = true)
            )
            .get
            .close()
        )
      case Languages.PYTHONSRC | Languages.PYTHON =>
        Some(
          new Py2CpgOnFileSystem()
            .createCpgWithOverlays(
              PyConfig(
                inputDir = ScalaFile(config.inputPath).path,
                outputFile = ScalaFile(config.outputCpgFile).path,
                disableDummyTypes = true
              )
            )
            .get
            .close()
        )
      case _ => None
    }
    Right("Code property graph generation successful")
  }

  private def generateAtom(config: ParserConfig, language: String): Right[Nothing, String] = {
    generateForLanguage(language.toUpperCase, config)
    Right(s"Code property graph generation successful for $language")
  }

  case class ParserConfig(
    inputPath: String = "",
    outputCpgFile: String = DEFAULT_CPG_OUT_FILE,
    enhance: Boolean = false,
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
