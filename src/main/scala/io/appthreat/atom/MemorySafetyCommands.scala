package io.appthreat.atom

import better.files.File
import io.circe.syntax.EncoderOps
import io.circe.{Json, JsonObject}
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*
import io.appthreat.x2cpg.passes.taggers.{
    ExtentPass,
    GuardPass,
    MemoryApiPass,
    MemorySafetyFindingPass,
    ValueOriginPass
}

import scala.collection.mutable

/** Implementation for the `memory-safety` command: render the overlay's findings as the JSON the
  * corpus scorer (and anything else) consumes.
  *
  * The findings themselves are tags on the graph - `ms-finding` valued with the rule id, written by
  * chen's MemorySafetyFindingPass during enhancement - so this is purely a renderer: rule metadata
  * (CWE, kind, severity, confidence, message) comes from the pass's rule registry, and the flow is
  * the evidence chain read back from the tags on the offending node plus its REACHING_DEF
  * definitions. No detector logic lives here, which is what keeps the command and the tags from
  * drifting apart.
  *
  * Output contract (tools/score.py reads file/line/cwe/kind/confidence; flow is for humans and
  * SARIF later):
  *
  * {{{
  * {"rule": "MS-BOUND-002", "cwe": "CWE-787", "kind": "unbounded-copy",
  *  "file": "c/cwe415_double_free.c", "line": 11, "column": 5,
  *  "severity": "high", "confidence": "medium",
  *  "message": "...", "flow": [{"file": "...", "line": 12, "code": "...", "role": "..."}]}
  * }}}
  */
object MemorySafetyCommands:

  private val confidenceOrder = Map("high" -> 3, "medium" -> 2, "low" -> 1)

  /** Render every finding and write it to `config.outputSliceFile`. */
  def runMemorySafety(
    cpg: Cpg,
    config: AtomMemorySafetyConfig,
    atomFile: File
  ): Either[String, String] =
    val minConfidence = confidenceOrder.getOrElse(config.minConfidence.toLowerCase, 0)

    val seen = mutable.LinkedHashSet.empty[(Long, String)]
    val findings = cpg.tag
        .name(MemorySafetyFindingPass.TagFinding)
        .l
        .flatMap { tag =>
          val ruleId = tag.value
          tag._taggedByIn
              .collectFirst { case e: Expression => e }
              .filter(e => seen.add((e.id, ruleId)))
              .map(e => (e, ruleId))
        }
        .flatMap { (node, ruleId) =>
            MemorySafetyFindingPass.rules.get(ruleId).map { rule =>
                (node, rule)
            }
        }
        .filter { (_, rule) =>
            confidenceOrder.getOrElse(rule.confidence.toLowerCase, 0) >= minConfidence
        }
        .map { (node, rule) => render(cpg, atomFile)(node, rule) }
        .toList
        .sortBy { f =>
          val obj  = f.asObject.getOrElse(JsonObject.empty)
          val file = obj("file").flatMap(_.asString).getOrElse("")
          val line = obj("line").flatMap(_.asNumber).flatMap(_.toInt).getOrElse(0)
          (file, line)
        }

    val outFile = config.outputSliceFile.createFileIfNotExists(createParents = true)
    outFile.write(findings.asJson.noSpaces)
    println(
      s"Memory-safety analysis complete. ${findings.size} findings written to ${outFile.pathAsString}"
    )
    Right("Memory-safety findings generated successfully")
  end runMemorySafety

  /** One finding: rule metadata from the registry, location from the offending node, flow from the
    * node's own tags, its memory operation, and the definitions that produced the value.
    */
  private def render(cpg: Cpg, atomFile: File)(
    node: Expression,
    rule: MemorySafetyFindingPass.MemorySafetyRule
  ): Json =
    val method = node.method
    val file   = node.file.name.headOption.getOrElse(method.filename)
    val line   = node.lineNumber.map(_.toInt).getOrElse(0)
    val column = node.columnNumber.map(_.toInt).getOrElse(0)

    JsonObject(
      "rule"       -> rule.id.asJson,
      "cwe"        -> rule.cwe.asJson,
      "kind"       -> rule.kind.asJson,
      "file"       -> file.asJson,
      "line"       -> line.asJson,
      "column"     -> column.asJson,
      "severity"   -> rule.severity.asJson,
      "confidence" -> rule.confidence.asJson,
      "message"    -> s"${node.code}: ${rule.message}".asJson,
      "flow"       -> flowOf(node).asJson,
      "atom"       -> atomFile.pathAsString.asJson
    ).asJson
  end render

  /** The evidence chain: the offending argument, the memory operation it bounds, the origin and
    * guard facts the rule read, and the definitions that produced the value (the REACHING_DEF walk
    * backwards - where `df` would be paying for a solve this does not need).
    */
  private def flowOf(node: Expression): List[Json] =
    val entries = mutable.ListBuffer.empty[JsonObject]
    def entry(n: AstNode, role: String): Unit =
        entries += JsonObject(
          "file" -> n.file.name.headOption.getOrElse("").asJson,
          "line" -> n.lineNumber.map(_.toInt).getOrElse(0).asJson,
          "code" -> n.code.asJson,
          "role" -> role.asJson
        )

    entry(node, "length-argument")
    node._astIn.collectFirst { case c: Call => c }.foreach(c => entry(c, "memory-operation"))

    val tags = node.tag.l
    tags.filter(_.name == ValueOriginPass.TagOrigin)
        .foreach(t => t.value.foreach(v => textEntry(s"origin: $v", "origin")))
    tags.filter(_.name == GuardPass.TagBelow)
        .foreach(t => t.value.foreach(v => textEntry(s"bounded-below by $v", "guard")))
    tags.filter(_.name == GuardPass.TagAbove)
        .foreach(t => t.value.foreach(v => textEntry(s"bounded-above by $v", "guard")))

    // the destination's extent, for the size-parameter rule
    node._astIn.collectFirst { case c: Call => c }.foreach { call =>
        call.argument.l
            .filter(a => a.tag.name(MemoryApiPass.TagDst).l.nonEmpty)
            .flatMap(_.tag.name(ExtentPass.TagExtent).value.l)
            .foreach(v => textEntry(s"destination extent $v", "destination"))
    }

    // definitions of the value, nearest first, definition sites only (identifiers in between
    // are the chain, not the evidence)
    node match
      case i: Identifier =>
          var frontier: List[StoredNode] = List(i)
          val visited                    = mutable.Set.empty[Long] + i.id
          var shown                      = 0
          while frontier.nonEmpty && shown < MaxFlowEntries do
            val next = mutable.ListBuffer.empty[StoredNode]
            frontier.foreach { n =>
                n._reachingDefIn.collectAll[StoredNode].l.filterNot(d => visited.contains(d.id))
                    .foreach { d =>
                      visited += d.id
                      d match
                        case c: Call =>
                            if shown < MaxFlowEntries then
                              entry(c, "definition")
                              shown += 1
                            next += d
                        case p: MethodParameterIn =>
                            if shown < MaxFlowEntries then
                              entry(p, "definition")
                              shown += 1
                        case lit: Literal =>
                            if shown < MaxFlowEntries then
                              entry(lit, "definition")
                              shown += 1
                        case other => next += other
                    }
            }
            frontier = next.toList
          end while
      case _ => ()
    end match

    entries.toList.map(_.asJson)
  end flowOf

  private def textEntry(text: String, role: String): Json =
      JsonObject("code" -> text.asJson, "role" -> role.asJson).asJson

  private val MaxFlowEntries = 8
end MemorySafetyCommands
