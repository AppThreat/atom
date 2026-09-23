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
    // Reject rather than silently ignore: a caller that asked for `--format sarif` and got JSON
    // named `.sarif`, or that misspelled a confidence and got every finding, has no way to tell.
    val format = config.format.toLowerCase
    if format != "json" then return Left(s"unsupported --format `$format`; only `json` exists")
    val requested = config.minConfidence.toLowerCase
    if requested.nonEmpty && !confidenceOrder.contains(requested) then
      return Left(
        s"unknown --min-confidence `$requested`; expected ${confidenceOrder.keys.toList.sorted.mkString(", ")}"
      )
    val minConfidence = confidenceOrder.getOrElse(requested, 0)
    val inputRoot     = config.inputPath.name

    val seen = mutable.LinkedHashSet.empty[(Long, String)]
    val findings = cpg.tag
        .name(MemorySafetyFindingPass.TagFinding)
        .l
        .flatMap { tag =>
          val ruleId = tag.value
          // a finding may sit on an expression (a length argument, a freed pointer) or on the
          // exit node itself - a bare `return;` has no expression child (part 4, D3 leaks).
          // METHOD_RETURN is the one that has to be named: `Return` and `ControlStructure` are
          // both EXPRESSIONs already, but METHOD_RETURN is only a CFG_NODE, so an
          // `Expression | ControlStructure` match silently dropped every leak reported at the
          // implicit end of a function - which in C is most of them.
          tag._taggedByIn
              .collectFirst { case e: (Expression | MethodReturn) => e }
              .filter(e => seen.add((e.id, ruleId)))
              .map(e => (e, ruleId))
        }
        .flatMap { (node, ruleId) =>
            MemorySafetyFindingPass.rules.get(ruleId).map { rule =>
                // a finding-level confidence override (part 5, E3), scoped to its own rule: one
                // node can carry several findings, and one rule's hypothesis tier must not demote
                // another rule's finding on the same node
                (node, rule, MemorySafetyFindingPass.confidenceOf(node, rule))
            }
        }
        .filter { (_, rule, confidence) =>
            confidenceOrder.getOrElse(confidence.toLowerCase, 0) >= minConfidence
        }
        .toList
        .sortBy { (node, _, _) =>
            (filenameOf(node), node.lineNumber.map(_.toInt).getOrElse(0): Int)
        }
        .map { (node, rule, confidence) =>
            render(cpg, atomFile, inputRoot, confidence)(node, rule)
        }

    val outFile = config.outputSliceFile.createFileIfNotExists(createParents = true)
    outFile.write(findings.asJson.noSpaces)
    println(
      s"Memory-safety analysis complete. ${findings.size} findings written to ${outFile.pathAsString}"
    )
    Right("Memory-safety findings generated successfully")
  end runMemorySafety

  /** The FILE the finding sits in, relative to the analysed input. The file edge is what the path
    * is rooted at; the enclosing method's filename is the fallback, and it has to be reached per
    * node type - there is no `.method` step on AstNode, and an exit-node finding is a METHOD_RETURN
    * rather than an Expression.
    */
  private def filenameOf(node: AstNode): String =
      node.file.name.headOption.getOrElse(
        node match
          case e: Expression   => e.method.filename
          case m: MethodReturn => m.method.filename
          case _               => ""
      )

  /** One finding: rule metadata from the registry, location from the offending node, flow from the
    * node's own tags, its memory operation, and the definitions that produced the value.
    *
    * The file path is rooted at the input directory's name: c2cpg records FILE names relative to
    * the analysed input, and every consumer of this JSON (the corpus scorer's `c/`/`cpp/` prefixes,
    * run_cve.sh's worktree re-rooting) matches on paths that carry that root.
    */
  private def render(cpg: Cpg, atomFile: File, inputRoot: String, confidence: String)(
    node: AstNode,
    rule: MemorySafetyFindingPass.MemorySafetyRule
  ): Json =
    val relative = filenameOf(node)
    val file   = if inputRoot.isEmpty || inputRoot == "." then relative else s"$inputRoot/$relative"
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
      "confidence" -> confidence.asJson,
      "message"    -> s"${node.code}: ${rule.message}".asJson,
      "flow"       -> flowOf(node).asJson,
      "atom"       -> atomFile.pathAsString.asJson
    ).asJson
  end render

  /** The evidence chain: the offending argument, the memory operation it bounds, the origin and
    * guard facts the rule read, and the definitions that produced the value (the REACHING_DEF walk
    * backwards - where `df` would be paying for a solve this does not need).
    */
  private def flowOf(node: AstNode): List[Json] =
    val entries = mutable.ListBuffer.empty[JsonObject]
    def entry(n: AstNode, role: String): Unit =
        entries += JsonObject(
          "file" -> n.file.name.headOption.getOrElse("").asJson,
          "line" -> n.lineNumber.map(_.toInt).getOrElse(0).asJson,
          "code" -> n.code.asJson,
          "role" -> role.asJson
        )
    // The tag-derived evidence has no node of its own: it is the fact a rule read, rendered as
    // text. It must APPEND like `entry` does - a version that merely returns the object silently
    // drops the origin, guard and extent rows the flow exists to show.
    def textEntry(text: String, role: String): Unit =
        entries += JsonObject("code" -> text.asJson, "role" -> role.asJson)

    node match
      case _: Return | _: MethodReturn => entry(node, "exit")
      case _                           => entry(node, "length-argument")
    node._astIn.collectFirst { case c: Call => c }.foreach(c => entry(c, "memory-operation"))

    // Tag.value is a String, not an Option - iterating it yields CHARACTERS, which is how
    // `struct-field` rendered as twelve one-letter origin rows.
    val tags = node.tag.l
    tags.filter(_.name == ValueOriginPass.TagOrigin)
        .foreach(t => textEntry(s"origin: ${t.value}", "origin"))
    tags.filter(_.name == GuardPass.TagBelow)
        .foreach(t => textEntry(s"bounded-below by ${t.value}", "guard"))
    tags.filter(_.name == GuardPass.TagAbove)
        .foreach(t => textEntry(s"bounded-above by ${t.value}", "guard"))

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
          val visited                    = mutable.Set[Long](i.id)
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

  private val MaxFlowEntries = 8
end MemorySafetyCommands
