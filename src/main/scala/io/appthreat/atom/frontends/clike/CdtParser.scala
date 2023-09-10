package io.appthreat.atom.frontends.clike

import better.files.File
import io.appthreat.atom.frontends.clike.CdtParser.ParseResult
import io.joern.c2cpg.Config
import io.joern.c2cpg.parser.{CustomFileContentProvider, FileDefaults, HeaderFileFinder, ParserConfig}
import io.shiftleft.utils.IOUtils
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit
import org.eclipse.cdt.core.model.ILanguage
import org.eclipse.cdt.core.parser.{DefaultLogService, FileContent, ScannerInfo}
import org.eclipse.cdt.internal.core.index.EmptyCIndex

import java.nio.file.{NoSuchFileException, Path}
import java.util
import java.util.concurrent.{Callable, ExecutorService, Executors, Future, TimeUnit}
import scala.jdk.CollectionConverters.*

object CdtParser {

  private case class ParseResult(translationUnit: Option[IASTTranslationUnit], failure: Option[Throwable] = None)

  def readFileAsFileContent(path: Path): FileContent = {
    val lines = IOUtils.readLinesInFile(path).mkString("\n").toArray
    FileContent.create(path.toString, true, lines)
  }

}

class CdtParser(config: Config) {

  import io.joern.c2cpg.parser.CdtParser.*
  val exec: ExecutorService    = Executors.newCachedThreadPool()
  private val headerFileFinder = new HeaderFileFinder(config.inputPath)
  private val parserConfig     = ParserConfig.fromConfig(config)
  private val definedSymbols   = parserConfig.definedSymbols.asJava
  private val includePaths     = parserConfig.userIncludePaths
  private val log              = new DefaultLogService

  // enables various options to speed up parsing
  private val opts: Int =
    ILanguage.OPTION_SKIP_FUNCTION_BODIES | ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS | ILanguage.OPTION_NO_IMAGE_LOCATIONS

  private def createParseLanguage(file: Path): ILanguage = {
    if (FileDefaults.isCPPFile(file.toString)) {
      GPPLanguage.getDefault
    } else {
      GCCLanguage.getDefault
    }
  }

  private def createScannerInfo(file: Path): ScannerInfo = {
    if !FileDefaults.isHeaderFile(file.toString) then
      val additionalIncludes =
        if (FileDefaults.isCPPFile(file.toString)) parserConfig.systemIncludePathsCPP
        else parserConfig.systemIncludePathsC
      new ScannerInfo(definedSymbols, (includePaths ++ additionalIncludes ++ "-").map(_.toString).toArray)
    else null
  }

  private class ParseTask(file: Path) extends Callable[ParseResult] {

    def call(): ParseResult = {
      println(file)
      val realPath = File(file)
      if (realPath.isRegularFile) { // handling potentially broken symlinks
        try {
          val fileContent         = readFileAsFileContent(realPath.path)
          val fileContentProvider = new CustomFileContentProvider(headerFileFinder)
          val lang                = createParseLanguage(realPath.path)
          val scannerInfo         = createScannerInfo(realPath.path)
          val translationUnit =
            lang.getASTTranslationUnit(fileContent, scannerInfo, fileContentProvider, EmptyCIndex.INSTANCE, opts, log)
          ParseResult(Option(translationUnit))
        } catch {
          case u: UnsupportedClassVersionError =>
            System.exit(1)
            ParseResult(None, failure = Option(u)) // return value to make the compiler happy
          case e: Throwable =>
            ParseResult(None, failure = Option(e))
        }
      } else {
        ParseResult(
          None,
          failure = Option(new NoSuchFileException(s"File '$realPath' does not exist. Check for broken symlinks!"))
        )
      }
    }
  }

  def parse(file: Path): Option[IASTTranslationUnit] = {
    val callables = new util.ArrayList[Callable[ParseResult]]();
    callables.add(new ParseTask(file));
    val futures = exec.invokeAll(callables, 10, TimeUnit.SECONDS)
    if futures.isEmpty then None
    else
      val parseResult = futures.get(0)
      parseResult match {
        case ParseResult(Some(t), _) =>
          Option(t)
        case ParseResult(_, _) =>
          None
      }
  }

}
