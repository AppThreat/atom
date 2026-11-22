package io.appthreat.atom

import io.appthreat.x2cpg.passes.taggers.MemorySafetyFindingPass
import io.circe.Json
import io.circe.syntax.EncoderOps

/** The memory-safety findings as SARIF 2.1.0: the rule registry as the driver's rules (CWE and kind
  * as tags, the rule's severity as its default level), one result per finding with its location,
  * and the evidence flow as a code flow. Paths are relative to `%SRCROOT%`, the analysed input.
  */
object MemorySafetySarif:

  private val SchemaUri =
      "https://docs.oasis-open.org/sarif/sarif/v2.1.0/errata01/os/schemas/sarif-schema-2.1.0.json"

  /** The key each result's fingerprint rides under in `partialFingerprints`: one tool, one key,
    * versioned so a future recipe can travel beside this one.
    */
  val FingerprintKey = "atom/memorySafety/v1"

  /** atom's version, from its jar's manifest; None when run from classes. */
  lazy val toolVersion: Option[String] =
      Option(getClass.getPackage).flatMap(p => Option(p.getImplementationVersion))

  /** The SARIF document for `findings` (the JSON objects the json format writes). */
  def document(findings: List[Json], inputPath: String, toolVersion: Option[String]): Json =
    // a result's ruleIndex points into the rules the driver lists: only the registry's
    val usedRules = findings.flatMap(f => stringField(f, "rule")).distinct.sorted
        .flatMap(id => MemorySafetyFindingPass.rules.get(id))
    val rules     = usedRules.map(ruleJson)
    val ruleIndex = usedRules.map(_.id).zipWithIndex.toMap
    Json.obj(
      "$schema" -> SchemaUri.asJson,
      "version" -> "2.1.0".asJson,
      "runs" -> Json.arr(
        Json.obj(
          "tool" -> Json.obj(
            "driver" -> Json.obj(
              (List(
                "name"           -> "atom".asJson,
                "informationUri" -> "https://github.com/AppThreat/atom".asJson
              ) ++ toolVersion.map(v => "version" -> v.asJson).toList ++
                  List("rules" -> rules.asJson))*
            )
          ),
          "originalUriBaseIds" -> Json.obj(
            "%SRCROOT%" -> Json.obj("uri" -> directoryUri(inputPath).asJson)
          ),
          "results" -> findings.map(f => resultJson(f, ruleIndex, inputName(inputPath))).asJson
        )
      )
    )
  end document

  private def ruleJson(rule: MemorySafetyFindingPass.MemorySafetyRule): Json =
      Json.obj(
        "id"                   -> rule.id.asJson,
        "name"                 -> rule.kind.asJson,
        "shortDescription"     -> Json.obj("text" -> s"${rule.cwe} ${rule.kind}".asJson),
        "fullDescription"      -> Json.obj("text" -> rule.message.asJson),
        "help"                 -> Json.obj("text" -> rule.message.asJson),
        "helpUri"              -> cweUri(rule.cwe).asJson,
        "defaultConfiguration" -> Json.obj("level" -> levelOf(rule.severity).asJson),
        "properties" -> Json.obj(
          "tags"             -> List("memory-safety", rule.cwe, rule.kind).asJson,
          "precision"        -> precisionOf(rule.confidence).asJson,
          "problem.severity" -> problemSeverity(rule.severity).asJson
        )
      )

  /** The name the findings' `file` fields put in front of a path under the input, which SARIF
    * expresses through `%SRCROOT%` instead.
    */
  private def inputName(inputPath: String): String =
      java.nio.file.Paths.get(inputPath).toAbsolutePath.normalize.getFileName match
        case null => ""
        case name => name.toString

  /** A finding's file as an artifact path: the input's name in front of a relative path is dropped,
    * because `%SRCROOT%` is the input itself.
    */
  private def underSourceRoot(file: String, inputName: String): String =
      if inputName.nonEmpty && file.startsWith(s"$inputName/") then file.drop(inputName.length + 1)
      else file

  private def resultJson(finding: Json, ruleIndex: Map[String, Int], inputName: String): Json =
    val ruleId     = stringField(finding, "rule").getOrElse("")
    val file       = underSourceRoot(stringField(finding, "file").getOrElse(""), inputName)
    val severity   = stringField(finding, "severity").getOrElse("")
    val confidence = stringField(finding, "confidence").getOrElse("")
    val location   = physicalLocation(file, intField(finding, "line"), intField(finding, "column"))
    // the finding's fingerprint as a partial fingerprint, under this tool's key, so a SARIF
    // reader matches results across runs the way the JSON format's consumers do
    val partialFingerprints = stringField(finding, "fingerprint").filter(_.nonEmpty).map { fp =>
        "partialFingerprints" -> Json.obj(FingerprintKey -> fp.asJson)
    }.toList
    val flow = finding.hcursor.downField("flow").as[List[Json]].getOrElse(Nil).map { step =>
      val text    = stringField(step, "code").getOrElse("")
      val role    = stringField(step, "role").getOrElse("")
      val message = Json.obj("text" -> (if role.isEmpty then text else s"$role: $text").asJson)
      // a fact the rule read has no position of its own: only its text
      val stepLocation = stringField(step, "file").filter(_.nonEmpty) match
        case Some(stepFile) =>
            physicalLocation(stepFile, intField(step, "line"), None).deepMerge(
              Json.obj("message" -> message)
            )
        case None => Json.obj("message" -> message)
      Json.obj("location" -> stepLocation)
    }
    val base = List(
      "ruleId"    -> ruleId.asJson,
      "ruleIndex" -> ruleIndex.getOrElse(ruleId, -1).asJson,
      "level"     -> levelOf(severity).asJson,
      "message"   -> Json.obj("text" -> stringField(finding, "message").getOrElse(ruleId).asJson),
      "locations" -> Json.arr(location)
    ) ++ partialFingerprints :+ ("properties" -> Json.obj(properties(finding)*))
    val withFlow =
        if flow.isEmpty then base
        else
          base :+ ("codeFlows" -> Json.arr(Json.obj("threadFlows" -> Json.arr(
            Json.obj("locations" -> flow.asJson)
          ))))
    Json.obj(withFlow*)
  end resultJson

  /** A result's properties: the confidence, the CWE and the kind, and for a finding the template's
    * instances share, the arguments of every instantiation that has it.
    */
  private def properties(finding: Json): List[(String, Json)] =
      List(
        "confidence" -> stringField(finding, "confidence").asJson,
        "cwe"        -> stringField(finding, "cwe").getOrElse("").asJson,
        "kind"       -> stringField(finding, "kind").getOrElse("").asJson
      ) ++ finding.hcursor.downField("instances").as[List[String]].toOption
          .filter(_.nonEmpty)
          .map(instances => "instances" -> instances.asJson)
          .toList

  private def physicalLocation(file: String, line: Option[Int], column: Option[Int]): Json =
    val region =
        List(
          line.filter(_ > 0).map(l => "startLine" -> l.asJson),
          column.filter(_ > 0).map(c => "startColumn" -> c.asJson)
        ).flatten // a file outside the analysed directory (a system header) keeps its absolute URI
    val artifact =
        if java.nio.file.Paths.get(file).isAbsolute then
          Json.obj("uri"    -> java.nio.file.Paths.get(file).normalize.toUri.toString.asJson)
        else Json.obj("uri" -> relativeUri(file).asJson, "uriBaseId" -> "%SRCROOT%".asJson)
    Json.obj(
      "physicalLocation" -> Json.obj(
        (List("artifactLocation" -> artifact) ++
            (if region.isEmpty then Nil else List("region" -> Json.obj(region*))))*
      )
    )

  /** A path as a relative URI reference: forward slashes, each segment percent-encoded. */
  private def relativeUri(path: String): String =
      path.replace('\\', '/').split('/').filter(_.nonEmpty).map(segment =>
          java.net.URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
      ).mkString("/")

  private def directoryUri(path: String): String =
    val uri = java.nio.file.Paths.get(path).toAbsolutePath.normalize.toUri.toString
    if uri.endsWith("/") then uri else s"$uri/"

  private def cweUri(cwe: String): String =
      cwe.stripPrefix("CWE-").toIntOption
          .map(n => s"https://cwe.mitre.org/data/definitions/$n.html")
          .getOrElse("https://cwe.mitre.org/")

  private def levelOf(severity: String): String = severity.toLowerCase match
    case "critical" | "high" => "error"
    case "medium"            => "warning"
    case _                   => "note"

  private def problemSeverity(severity: String): String = severity.toLowerCase match
    case "critical" | "high" => "error"
    case "medium"            => "warning"
    case _                   => "recommendation"

  private def precisionOf(confidence: String): String = confidence.toLowerCase match
    case "high"   => "high"
    case "medium" => "medium"
    case _        => "low"

  private def stringField(json: Json, name: String): Option[String] =
      json.hcursor.downField(name).as[String].toOption

  private def intField(json: Json, name: String): Option[Int] =
      json.hcursor.downField(name).as[Int].toOption
end MemorySafetySarif
