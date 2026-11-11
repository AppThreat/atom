package io.appthreat.atom

import io.appthreat.x2cpg.parse.ParseFailureRecord
import io.circe.Json

import scala.util.{Failure, Success, Try}

/** The per-file parse report (`--parse-report`, chen#255): one JSON document naming every file the
  * frontends left out or only partly read, with the atom version, the language, the input root and
  * the counts per outcome and per frontend. The records come sorted by file from the collector the
  * frontends fed while the run went on; this object only renders and writes them.
  *
  * The JSON is built field by field with no derivation, so the native image needs no reflection
  * configuration for it: everything the encoder touches is a string or an int.
  */
object ParseReport:

  /** The document for the given run. */
  def document(
    atomVersion: String,
    language: String,
    inputRoot: String,
    records: List[ParseFailureRecord]
  ): Json =
      Json.obj(
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
  end document

  private def countsOf(values: List[String]): Json =
    val sorted = values.groupBy(identity).view.mapValues(_.size).toList.sortBy(_._1)
    Json.obj(sorted.map((key, count) => key -> Json.fromInt(count))*)

  /** Write `document` to `path`, creating parents. Nothing else is touched: the caller decides when
    * a run deserves a report at all.
    */
  def write(path: String, document: Json): Try[Unit] =
      Try {
          val file = better.files.File(path)
          file.parent.createDirectoryIfNotExists(createParents = true)
          file.write(document.noSpaces)
      }

end ParseReport
