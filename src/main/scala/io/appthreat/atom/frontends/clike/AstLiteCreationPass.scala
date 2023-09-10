package io.appthreat.atom.frontends.clike

import io.appthreat.atom.passes.SafeConcurrentCpgPass
import io.joern.c2cpg.Config
import io.joern.c2cpg.astcreation.AstCreator
import io.joern.c2cpg.parser.FileDefaults
import io.joern.x2cpg.SourceFiles
import io.shiftleft.codepropertygraph.Cpg

import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern
import scala.util.matching.Regex

class AstLiteCreationPass(cpg: Cpg, config: Config) extends SafeConcurrentCpgPass[String](cpg) {

  private val file2OffsetTable: ConcurrentHashMap[String, Array[Int]] = new ConcurrentHashMap()
  private val parser: CdtParser                                       = new CdtParser(config)

  private val EscapedFileSeparator = Pattern.quote(java.io.File.separator)
  private val DefaultIgnoredFolders: List[Regex] = List(
    "\\..*".r,
    s"(.*[$EscapedFileSeparator])?tests?[$EscapedFileSeparator].*".r,
    s"(.*[$EscapedFileSeparator])?CMakeFiles[$EscapedFileSeparator].*".r
  )

  override def generateParts(): Array[String] =
    SourceFiles
      .determine(
        config.inputPath,
        FileDefaults.HEADER_FILE_EXTENSIONS,
        config.withDefaultIgnoredFilesRegex(DefaultIgnoredFolders)
      )
      .sorted(Ordering[String])
      .toArray

  override def runOnPart(diffGraph: DiffGraphBuilder, filename: String): Unit = {
    val path    = Paths.get(filename).toAbsolutePath
    val relPath = SourceFiles.toRelativePath(path.toString, config.inputPath)
    try {
      val parseResult = parser.parse(path)
      parseResult match {
        case Some(translationUnit) =>
          val localDiff =
            new AstCreator(relPath, config, translationUnit, file2OffsetTable)(config.schemaValidation).createAst()
          diffGraph.absorb(localDiff)
        case None =>
      }
    } catch {
      case e: Throwable =>
    }
  }

}
