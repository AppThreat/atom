package io.appthreat.atom

import better.files.File
import io.appthreat.atom.Atom.{DEFAULT_EXPORT_DIR, DEFAULT_EXPORT_FORMAT, DEFAULT_SLICE_OUT_FILE}
import io.circe.{Json, parser}

/** Reads the optional JSON config file for the graph-level commands. Command-line flags always take
  * precedence: a value from the file is only applied when the matching field is still at its
  * default, so anything passed explicitly on the command line wins.
  *
  * Export keys: `format`, `scope`, `exportDir`. Algorithm keys: `type`, `source`, `target`,
  * `maxDepth`, `out`.
  */
object AtomConfigFile:

  def apply(config: AtomConfig): Either[String, AtomConfig] =
      config.configFile match
        case None => Right(config)
        case Some(file) =>
            if !file.exists then Left(s"Config file does not exist: ${file.pathAsString}")
            else
              parser.parse(file.contentAsString) match
                case Left(err) =>
                    Left(s"Could not parse config file ${file.pathAsString}: ${err.message}")
                case Right(json) =>
                    config match
                      case e: AtomExportConfig     => Right(applyExport(e, json))
                      case a: AtomAlgorithmsConfig => Right(applyAlgorithms(a, json))
                      case other                   => Right(other)

  private def applyExport(config: AtomExportConfig, json: Json): AtomExportConfig =
    val cursor = json.hcursor
    if config.exportFormat == DEFAULT_EXPORT_FORMAT then
      stringField(cursor, "format").foreach(config.withExportFormat)
    if config.scope == "whole" then
      stringField(cursor, "scope").foreach(config.withScope)
    if config.exportDir == DEFAULT_EXPORT_DIR then
      stringField(cursor, "exportDir").foreach(config.withExportDir)
    config

  private def applyAlgorithms(config: AtomAlgorithmsConfig, json: Json): AtomAlgorithmsConfig =
    val cursor = json.hcursor
    if config.algoType == "centrality" then
      stringField(cursor, "type").foreach(config.withAlgoType)
    if config.sourceSelector.isEmpty then
      stringField(cursor, "source").foreach(s => config.withSourceSelector(Some(s)))
    if config.targetSelector.isEmpty then
      stringField(cursor, "target").foreach(t => config.withTargetSelector(Some(t)))
    if config.maxDepth == -1 then
      cursor.get[Int]("maxDepth").toOption.foreach(config.withMaxDepth)
    if config.outputSliceFile.name == DEFAULT_SLICE_OUT_FILE then
      stringField(cursor, "out").foreach(out => config.withOutputSliceFile(File(out)))
    config

  private def stringField(cursor: io.circe.HCursor, key: String): Option[String] =
      cursor.get[String](key).toOption.map(_.trim).filter(_.nonEmpty)
end AtomConfigFile
