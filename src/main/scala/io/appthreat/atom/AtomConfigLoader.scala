package io.appthreat.atom

import better.files.File
import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigValue}
import io.appthreat.atom.Atom.{DEFAULT_EXPORT_DIR, DEFAULT_EXPORT_FORMAT, DEFAULT_SLICE_OUT_FILE}

import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

/** Loads frontend and graph-command configuration from a HOCON (or JSON) file.
  *
  * A config file is optional. When present it is merged into the parsed configuration using a
  * single, predictable precedence: anything supplied on the command line (either via
  * `--frontend-args` or a first-class flag) wins over the file, which in turn wins over the
  * built-in defaults. This keeps the file declarative and repeatable while leaving room for a
  * one-off override.
  *
  * Discovery order (first match wins):
  *   1. an explicit `--config <path>`; 2. `atom.conf` or `atom.json` at the root of the analysed
  *      input; 3. `.atom/config.conf` (or `atom.conf`/`atom.json`) under that same root; 4. the
  *      path in the `ATOM_CONFIG_FILE` environment variable; 5. `~/.config/atom/config.conf` for
  *      user-wide defaults.
  *
  * The `[frontend]` section holds universal knobs (e.g. `exclude`, `no-dummy-types`) and an
  * optional `[frontend.<language>]` sub-section for language-specific knobs. Both are flattened
  * into the same `frontendArgs` map consumed by the applier, so the file is just another source of
  * keys. Graph-command keys (`format`, `scope`, `exportDir`, `type`, `source`, `target`,
  * `maxDepth`, `out`) are read from the top level so that the existing flat-JSON fixtures continue
  * to work unchanged.
  */
object AtomConfigLoader:

  private val InputConfigNames  = Seq("atom.conf", "atom.json")
  private val DotDirConfigNames = Seq("config.conf", "atom.conf", "atom.json")

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
            val global = File.home / ".config" / "atom" / "config.conf"
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
              Try(ConfigFactory.parseFile(file.toJava).resolve()) match
                case Failure(_: ConfigException) | Failure(_) =>
                    Left(s"Could not parse config file ${file.pathAsString}")
                case Success(hocon) =>
                    mergeFrontendArgs(config, hocon)
                    config match
                      case e: AtomExportConfig     => applyExport(e, hocon)
                      case a: AtomAlgorithmsConfig => applyAlgorithms(a, hocon)
                      case _                       => ()
                    Right(config)

  /** Flattens the `[frontend]` (universal) and `[frontend.<language>]` sections into a flat
    * key=value map. Lists become CSV so they flow into the same channel as `--frontend-args`.
    */
  def frontendArgsFromConfig(hocon: Config, language: String): Map[String, String] =
    val normalized = language.trim.toLowerCase
    val universal  = collectLeaves(hocon, "frontend")
    val perLang =
        if normalized.nonEmpty then collectLeaves(hocon, s"frontend.$normalized") else Map.empty
    universal ++ perLang

  private def mergeFrontendArgs(config: AtomConfig, hocon: Config): Unit =
    val fromFile = frontendArgsFromConfig(hocon, config.language)
    // CLI-provided keys (already in config.frontendArgs) take precedence over the file.
    config.withFrontendArgs(fromFile ++ config.frontendArgs)

  private def collectLeaves(hocon: Config, path: String): Map[String, String] =
      if !hocon.hasPathOrNull(path) then Map.empty
      else
        hocon.getConfig(path).root().entrySet().asScala.flatMap { entry =>
          val key = entry.getKey
          unwrap(entry.getValue).map(value => key -> value)
        }.toMap

  /** Renders a config value as the string representation used by `frontendArgs`. Sub-objects are
    * skipped (they are language sections, not leaf knobs).
    */
  private def unwrap(value: ConfigValue): Option[String] =
      value.unwrapped() match
        case _: java.util.Map[?, ?] => None
        case null                   => None
        case b: java.lang.Boolean   => Some(if b then "true" else "false")
        case n: Number              => Some(String.valueOf(n))
        case s: String              => Some(s)
        case list: java.util.List[?] =>
            import scala.jdk.CollectionConverters.*
            val rendered = list.asScala.iterator.map {
                case b: java.lang.Boolean => if b then "true" else "false"
                case other                => String.valueOf(other)
            }.mkString(",")
            Some(rendered)
        case other => Some(String.valueOf(other))

  private def applyExport(config: AtomExportConfig, hocon: Config): Unit =
    if config.exportFormat == DEFAULT_EXPORT_FORMAT then
      strOpt(hocon, "format").foreach(config.withExportFormat)
    if config.scope == "whole" then
      strOpt(hocon, "scope").foreach(config.withScope)
    if config.exportDir == DEFAULT_EXPORT_DIR then
      strOpt(hocon, "exportDir").foreach(config.withExportDir)

  private def applyAlgorithms(config: AtomAlgorithmsConfig, hocon: Config): Unit =
    if config.algoType == "centrality" then
      strOpt(hocon, "type").foreach(config.withAlgoType)
    if config.sourceSelector.isEmpty then
      strOpt(hocon, "source").foreach(s => config.withSourceSelector(Some(s)))
    if config.targetSelector.isEmpty then
      strOpt(hocon, "target").foreach(s => config.withTargetSelector(Some(s)))
    if config.maxDepth == -1 && hocon.hasPath("maxDepth") then
      Try(hocon.getInt("maxDepth")).foreach(config.withMaxDepth)
    if config.outputSliceFile.name == DEFAULT_SLICE_OUT_FILE then
      strOpt(hocon, "out").foreach(out => config.withOutputSliceFile(File(out)))

  private def strOpt(hocon: Config, key: String): Option[String] =
      if hocon.hasPath(key) then Try(hocon.getString(key)).toOption.map(_.trim).filter(_.nonEmpty)
      else None
end AtomConfigLoader
