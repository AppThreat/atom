package io.appthreat.atom.frontends

import better.files.File
import io.appthreat.atom.frontends.CdtParser.ParseResult
import io.joern.c2cpg.Config
import io.joern.c2cpg.parser.{CustomFileContentProvider, FileDefaults, HeaderFileFinder, ParserConfig}
import io.shiftleft.utils.IOUtils
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage
import org.eclipse.cdt.core.dom.ast.{IASTPreprocessorStatement, IASTTranslationUnit}
import org.eclipse.cdt.core.model.ILanguage
import org.eclipse.cdt.core.parser.{DefaultLogService, ScannerInfo}
import org.eclipse.cdt.core.parser.FileContent
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor
import org.slf4j.LoggerFactory

import java.nio.file.{NoSuchFileException, Path}
import scala.jdk.CollectionConverters.*

object CdtParser {

  private case class ParseResult(translationUnit: Option[IASTTranslationUnit], failure: Option[Throwable] = None)

  def readFileAsFileContent(path: Path): FileContent = {
    val lines = IOUtils.readLinesInFile(path).mkString("\n").toArray
    FileContent.create(path.toString, true, lines)
  }

}

class CdtParser(config: Config) {

  import io.joern.c2cpg.parser.CdtParser._

  private val headerFileFinder = new HeaderFileFinder(config.inputPath)
  private val parserConfig     = ParserConfig.fromConfig(config)
  private val definedSymbols   = parserConfig.definedSymbols.asJava
  private val includePaths     = parserConfig.userIncludePaths
  private val log              = new DefaultLogService

  // enables parsing of code behind disabled preprocessor defines:
  private val opts: Int = ILanguage.OPTION_PARSE_INACTIVE_CODE

  private def createParseLanguage(file: Path): ILanguage = {
    if (FileDefaults.isCPPFile(file.toString)) {
      GPPLanguage.getDefault
    } else {
      GCCLanguage.getDefault
    }
  }

  private def createScannerInfo(file: Path): ScannerInfo = {
    val additionalIncludes =
      if (FileDefaults.isCPPFile(file.toString)) parserConfig.systemIncludePathsCPP
      else parserConfig.systemIncludePathsC
    new ScannerInfo(definedSymbols, (includePaths ++ additionalIncludes).map(_.toString).toArray)
  }

  private def parseInternal(file: Path): ParseResult = {
    val realPath = File(file)
    if (realPath.isRegularFile) { // handling potentially broken symlinks
      try {
        val fileContent         = readFileAsFileContent(realPath.path)
        val fileContentProvider = new CustomFileContentProvider(headerFileFinder)
        val lang                = createParseLanguage(realPath.path)
        val scannerInfo         = createScannerInfo(realPath.path)
        val translationUnit = lang.getASTTranslationUnit(fileContent, scannerInfo, fileContentProvider, null, opts, log)
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

  def parse(file: Path): Option[IASTTranslationUnit] = {
    val parseResult = parseInternal(file)
    parseResult match {
      case ParseResult(Some(t), _) =>
        Option(t)
      case ParseResult(_, _) =>
        None
    }
  }

}
