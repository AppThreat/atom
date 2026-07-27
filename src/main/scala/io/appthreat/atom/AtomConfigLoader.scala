package io.appthreat.atom

import better.files.File
import io.appthreat.atom.Atom.{DEFAULT_EXPORT_DIR, DEFAULT_EXPORT_FORMAT, DEFAULT_SLICE_OUT_FILE}
import io.circe.{Json, parser}

/** Loads frontend and graph-command configuration from a JSON file.
  *
  * A config file is optional. When present it is merged into the parsed configuration using a
  * single, predictable precedence: anything supplied on the command line (either via
  * `--frontend-args` or a first-class flag) wins over the file, which in turn wins over the
  * built-in defaults. This keeps the file declarative and repeatable while leaving room for a
  * one-off override.
  *
  * Discovery order (first match wins):
  *   1. an explicit `--config <path>`; 2. `atom.json` at the root of the analysed input; 3.
  *      `.atom/config.json` (or `atom.json`) under that same root; 4. the path in the
  *      `ATOM_CONFIG_FILE` environment variable; 5. `~/.config/atom/config.json` for user-wide
  *      defaults.
  *
  * The `frontend` object holds universal knobs (e.g. `exclude`, `no-dummy-types`) and an optional
  * `frontend.<language>` sub-object for language-specific knobs. Both are flattened into the same
  * `frontendArgs` map consumed by the applier, so the file is just another source of keys.
  * Graph-command keys (`format`, `scope`, `exportDir`, `type`, `source`, `target`, `maxDepth`,
  * `out`) are read from the top level so that the existing flat-JSON fixtures continue to work
  * unchanged.
  */
object AtomConfigLoader:

  private val InputConfigNames  = Seq("atom.json")
  private val DotDirConfigNames = Seq("config.json", "atom.json")

  /** Resolves the config file to load. An explicit `--config` always wins and is returned even when
    * the file is missing (the caller reports the error); auto-discovery skips absent candidates.
    */
  def discover(inputPath: String, explicit: Option[File]): Option[File] =
      explicit match
        case Some(file) => Some(file)
        case None       => autoDiscover(inputPath)

  private def autoDiscover(inputPath: String): Option[File] =
    val input            = File(inputPath)
    val root             = if input.isDirectory then input else input.parent
    val dotDirCandidates = DotDirConfigNames.map(root / ".atom" / _)
    InputConfigNames.map(root / _).find(_.exists)
        .orElse(dotDirCandidates.find(_.exists))
        .orElse(sys.env.get("ATOM_CONFIG_FILE").filter(_.nonEmpty).map(File(_)).filter(_.exists))
        .orElse {
            val global = File.home / ".config" / "atom" / "config.json"
            if global.exists then Some(global) else None
        }

  /** Merges a discovered or explicit config file into `config`. Returns `Left` only for a genuine
    * failure (missing explicit file, unreadable content); an absent auto-discovered file is a
    * silent no-op.
    */
  def apply(config: AtomConfig): Either[String, AtomConfig] =
      discover(config.inputPath.pathAsString, config.configFile) match
        case None => Right(config)
        case Some(file) =>
            if !file.exists then
              if config.configFile.isDefined then
                Left(s"Config file does not exist: ${file.pathAsString}")
              else Right(config)
            else if file.isDirectory then Left(s"Config path is a directory: ${file.pathAsString}")
            else
              parser.parse(file.contentAsString) match
                case Left(err) =>
                    Left(s"Could not parse config file ${file.pathAsString}: ${err.message}")
                case Right(json) =>
                    mergeFrontendArgs(config, json)
                    config match
                      case e: AtomExportConfig     => applyExport(e, json)
                      case a: AtomAlgorithmsConfig => applyAlgorithms(a, json)
                      case _                       => ()
                    Right(config)

  /** Flattens the `frontend` (universal) and `frontend.<language>` objects into a flat key=value
    * map. Arrays become CSV so they flow into the same channel as `--frontend-args`.
    */
  def frontendArgsFromConfig(json: Json, language: String): Map[String, String] =
    val normalized = language.trim.toLowerCase
    val universal  = collectLeaves(json, "frontend")
    val perLang =
        if normalized.nonEmpty then collectLeaves(json, s"frontend.$normalized") else Map.empty
    universal ++ perLang

  private def mergeFrontendArgs(config: AtomConfig, json: Json): Unit =
    val fromFile = frontendArgsFromConfig(json, config.language)
    // CLI-provided keys (already in config.frontendArgs) take precedence over the file.
    config.withFrontendArgs(fromFile ++ config.frontendArgs)

  private def collectLeaves(json: Json, path: String): Map[String, String] =
      path.split("\\.").foldLeft(Option(json))((acc, segment) =>
          acc.flatMap(_.hcursor.get[Json](segment).toOption)
      )
          .flatMap(_.asObject) match
        case None => Map.empty
        case Some(obj) =>
            obj.toIterable.flatMap { case (key, value) =>
                renderLeaf(value).map(v => key -> v)
            }.toMap

  /** Renders a JSON value as the string representation used by `frontendArgs`. Sub-objects are
    * skipped (they are language sections, not leaf knobs).
    */
  private def renderLeaf(json: Json): Option[String] =
      if json.isObject || json.isNull then None
      else if json.isString then json.asString
      else if json.isBoolean then json.asBoolean.map(b => if b then "true" else "false")
      else if json.isArray then json.asArray.map(_.map(renderScalar).mkString(","))
      else Some(json.noSpaces)

  private def renderScalar(json: Json): String =
      json.asString.getOrElse(
        json.asBoolean.map(b => if b then "true" else "false").getOrElse(json.noSpaces)
      )

  private def applyExport(config: AtomExportConfig, json: Json): Unit =
    if config.exportFormat == DEFAULT_EXPORT_FORMAT then
      strOpt(json, "format").foreach(config.withExportFormat)
    if config.scope == "whole" then
      strOpt(json, "scope").foreach(config.withScope)
    if config.exportDir == DEFAULT_EXPORT_DIR then
      strOpt(json, "exportDir").foreach(config.withExportDir)

  private def applyAlgorithms(config: AtomAlgorithmsConfig, json: Json): Unit =
    if config.algoType == "centrality" then
      strOpt(json, "type").foreach(config.withAlgoType)
    if config.sourceSelector.isEmpty then
      strOpt(json, "source").foreach(s => config.withSourceSelector(Some(s)))
    if config.targetSelector.isEmpty then
      strOpt(json, "target").foreach(s => config.withTargetSelector(Some(s)))
    if config.maxDepth == -1 then
      json.hcursor.get[Int]("maxDepth").toOption.foreach(config.withMaxDepth)
    if config.outputSliceFile.name == DEFAULT_SLICE_OUT_FILE then
      strOpt(json, "out").foreach(out => config.withOutputSliceFile(File(out)))

  private def strOpt(json: Json, key: String): Option[String] =
      json.hcursor.get[String](key).toOption.map(_.trim).filter(_.nonEmpty)
end AtomConfigLoader
