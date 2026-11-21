package io.appthreat.atom

import io.appthreat.x2cpg.parse.{ParseFailureRecord, ParseUnitRecord, UnitSettingsReporter}
import io.circe.Json

import scala.util.{Failure, Success, Try}

/** The per-file parse report (`--parse-report`, chen#255): one JSON document naming every file the
  * frontends left out or only partly read, and, when the frontends collected them, how every
  * translation unit was set up for parsing (its `units` section). The document carries the atom
  * version, the language, the input root and the counts per outcome and per frontend. The records
  * come sorted by file from the collectors the frontends fed while the run went on; this object
  * only renders and writes them.
  *
  * The JSON is built field by field with no derivation, so the native image needs no reflection
  * configuration for it: everything the encoder touches is a string or an int.
  */
object ParseReport:

  /** The document for the given run. The units section is rendered from `units`, what the frontends
    * collected for the run; a run that collected none renders no units section at all.
    */
  def document(
    atomVersion: String,
    language: String,
    inputRoot: String,
    records: List[ParseFailureRecord],
    units: List[ParseUnitRecord]
  ): Json =
    val fields = List(
      "atomVersion" -> Json.fromString(atomVersion),
      "language"    -> Json.fromString(language),
      "inputRoot"   -> Json.fromString(inputRoot),
      "total"       -> Json.fromInt(records.size),
      "counts" -> Json.obj(
        "byOutcome"  -> countsOf(records.map(_.outcome)),
        "byFrontend" -> countsOf(records.map(_.frontend))
      ),
      "records" -> Json.arr(records.map(record =>
          Json.obj(
            "frontend" -> Json.fromString(record.frontend),
            "file"     -> Json.fromString(record.file),
            "outcome"  -> Json.fromString(record.outcome),
            "stage"    -> Json.fromString(record.stage),
            "message"  -> Json.fromString(record.message)
          )
      )*)
    )
    Json.obj((if units.isEmpty then fields else fields :+ ("units" -> unitsSection(units)))*)
  end document

  /** The `units` section: one record per translation unit a frontend parsed, with the settings it
    * was parsed under, where its predefined macros came from, and the exact command line the EDG
    * export ran. A driver the driver table does not know is recorded as unknown, and a file the
    * compilation database names more than once carries every command it has, with the one that was
    * used named among them.
    */
  private def unitsSection(units: List[ParseUnitRecord]): Json =
      Json.obj(
        "total" -> Json.fromInt(units.size),
        "counts" -> Json.obj(
          "byFrontend"    -> countsOf(units.map(_.frontend)),
          "byMacroSource" -> countsOf(units.map(_.macroSource))
        ),
        "records" -> Json.arr(units.map(unitRecord)*)
      )

  private def unitRecord(unit: ParseUnitRecord): Json =
    val fields = List(
      "frontend"           -> Json.fromString(unit.frontend),
      "file"               -> Json.fromString(unit.file),
      "driver"             -> Json.fromString(unit.driver),
      "entry"              -> Json.fromString(unit.entry),
      "macroSource"        -> Json.fromString(unit.macroSource),
      "language"           -> Json.fromString(unit.language),
      "standard"           -> Json.fromString(unit.standard),
      "macroFingerprint"   -> Json.fromString(unit.macroFingerprint),
      "includeDirectories" -> stringsOf(unit.includeDirectories),
      "forcedIncludes"     -> stringsOf(unit.forcedIncludes),
      "ownedHeaders"       -> stringsOf(unit.ownedHeaders)
    ) ++
        (if unit.databaseCommands.isEmpty then Nil
         else
           Seq(
             "databaseCommands" -> Json.arr(unit.databaseCommands.map(command =>
                 Json.obj(
                   "directory" -> Json.fromString(command.directory),
                   "arguments" -> stringsOf(command.arguments)
                 )
             )*),
             // the command the settings came from, by its position in databaseCommands
             "usedDatabaseCommand" -> Json.fromInt(unit.usedDatabaseCommand.getOrElse(0))
           )
        ) ++
        (if unit.edgaCommandLine.isEmpty then Nil
         else Seq("edgaCommandLine" -> stringsOf(unit.edgaCommandLine)))
    Json.obj(fields*)
  end unitRecord

  private def stringsOf(values: Seq[String]): Json =
      Json.arr(values.map(Json.fromString)*)

  private def countsOf(values: List[String]): Json =
    val sorted = values.groupBy(identity).view.mapValues(_.size).toList.sortBy(_._1)
    Json.obj(sorted.map((key, count) => key -> Json.fromInt(count))*)

  /** Write `document` to `path`, creating parents. The unit settings the frontends collected are
    * dropped here: the document already holds what the run collected, so a later run in the same
    * process starts from empty.
    */
  def write(path: String, document: Json): Try[Unit] =
    UnitSettingsReporter.reset()
    Try {
        val file = better.files.File(path)
        file.parent.createDirectoryIfNotExists(createParents = true)
        file.write(document.noSpaces)
    }

end ParseReport
