package io.appthreat.atom

import better.files.File
import io.appthreat.x2cpg.Defines.TemplateArgsTag
import io.appthreat.x2cpg.Defines.TemplateTag
import io.appthreat.atom.slicing.callsites.VendoredCode
import io.circe.syntax.EncoderOps
import io.circe.{Json, JsonObject}
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.{Languages, PropertyNames}
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

/** Implementation for the `memory-safety` command: render the overlay's findings as JSON for
  * scoring harnesses, CI gates and other tools.
  *
  * The findings themselves are tags on the graph - `ms-finding` valued with the rule id, written by
  * chen's MemorySafetyFindingPass during enhancement - so this is purely a renderer: rule metadata
  * (CWE, kind, severity, confidence, message) comes from the pass's rule registry, and the flow is
  * the evidence chain read back from the tags on the offending node plus its REACHING_DEF
  * definitions. No detector logic lives here, which is what keeps the command and the tags from
  * drifting apart.
  *
  * Output contract (tools/score.py reads file/line/cwe/kind/confidence; flow is for humans, and
  * becomes the code flow of `--format sarif`, see [[MemorySafetySarif]]; `function`, `purl` and
  * `fingerprint` identify the finding for tools that report it beside advisories, and are
  * documented with the published schema, specification/schemas/MemorySafetyFinding.json):
  *
  * {{{
  * {"rule": "MS-BOUND-002", "cwe": "CWE-787", "kind": "unbounded-copy",
  *  "file": "c/cwe415_double_free.c", "line": 11, "column": 5,
  *  "severity": "high", "confidence": "medium",
  *  "message": "...", "function": "cwe415_double_free", "purl": "pkg:generic/vendlib@1.2.3",
  *  "flow": [{"file": "...", "line": 12, "code": "...", "role": "..."}],
  *  "fingerprint": "sha256 hex", "atom": "/path/to/atom"}
  * }}}
  */
object MemorySafetyCommands:

  private val confidenceOrder = Map("high" -> 3, "medium" -> 2, "low" -> 1)

  /** The languages the C frontends produce. Only on these graphs does the overlay actually tag, so
    * only on these does a missing overlay marker mean the findings cannot be there (see
    * [[runMemorySafety]]); every other language legitimately reports zero findings.
    */
  private val OverlayLanguages = Set(Languages.C, Languages.NEWC, "CPP", "C++")

  /** Render every finding and write it to `config.outputSliceFile`. */
  def runMemorySafety(
    cpg: Cpg,
    config: AtomMemorySafetyConfig,
    atomFile: File
  ): Either[String, String] =
    // Reject rather than silently ignore: a caller that asked for `--format sarif` and got JSON
    // named `.sarif`, or that misspelled a confidence and got every finding, has no way to tell.
    val format = config.format.toLowerCase
    if format != "json" && format != "sarif" then
      return Left(s"unsupported --format `$format`; expected json or sarif")
    val requested = config.minConfidence.toLowerCase
    if requested.nonEmpty && !confidenceOrder.contains(requested) then
      return Left(
        s"unknown --min-confidence `$requested`; expected ${confidenceOrder.keys.toList.sorted.mkString(", ")}"
      )
    val minConfidence = confidenceOrder.getOrElse(requested, 0)
    val inputRoot     = config.inputPath.name
    val root          = ReportedPaths.Root(config.inputPath.path)
    // the vendored-code attribution a finding's `purl` comes from, decided once per run: what the
    // build stored, else this run's --external-paths pattern and the atom's BOMs, the same lookup
    // the callsites records use
    val vendored = VendoredCode.forAtom(cpg, config.frontendArgs.get("external-paths"))

    // The findings are tags only the overlay writes, and a graph without them is exactly what a
    // finding-free program looks like - so when the marker Atom writes alongside the overlay is
    // missing on a C/C++ graph, refuse instead of rendering an empty (exit 0) report. This guards
    // `--reuse-atom` against an atom built by another command, and atoms built by atom 4.0.x,
    // where every dataflow-enhanced command embedded the overlay but nothing recorded it.
    val language = cpg.metaData.language.headOption.getOrElse("")
    if OverlayLanguages.contains(language) &&
      cpg.configFile.name(Atom.MemorySafetyOverlayFileName).isEmpty
    then
      return Left(
        s"The atom at ${atomFile.pathAsString} does not carry memory-safety overlay tags: " +
            "it was built by a command other than `memory-safety`, or by an atom build that " +
            "predated overlay marking. Rebuild the atom by running the memory-safety command " +
            "on the source, and pass --reuse-atom only to an atom that command built."
      )

    val seen = mutable.LinkedHashSet.empty[(Long, String)]
    val rendered = cpg.tag
        .name(MemorySafetyFindingPass.TagFinding)
        .l
        .flatMap { tag =>
          val ruleId = tag.value
          // a finding may sit on an expression (a length argument, a freed pointer) or on the
          // exit node itself - a bare `return;` has no expression child (leaks).
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
                // a finding-level confidence override, scoped to its own rule: one
                // node can carry several findings, and one rule's hypothesis tier must not demote
                // another rule's finding on the same node
                (node, rule, MemorySafetyFindingPass.confidenceOf(node, rule))
            }
        }
        .filter { (_, rule, confidence) =>
            confidenceOrder.getOrElse(confidence.toLowerCase, 0) >= minConfidence
        }
        .toList
        .map { (node, rule, confidence) => (node, rule, confidence, root.locate(filenameOf(node))) }
        // the reported location decides: a finding in a file outside the input (a system header)
        // is code the user cannot change, whatever flow led to it, and a finding in project code
        // stays even when its flow passes through such a header
        .filter { (_, _, _, location) =>
            config.includeSystemHeaders || location.isInstanceOf[ReportedPaths.Location.Inside]
        }
        .sortBy { (node, _, _, location) =>
            (
              ReportedPaths.printed(location, inputRoot),
              node.lineNumber.map(_.toInt).getOrElse(0): Int
            )
        }
        .map { (node, rule, confidence, location) =>
            (
              render(atomFile, inputRoot, root, vendored, confidence)(node, rule, location),
              instanceOf(node)
            )
        }

    val findings = groupTemplateInstances(rendered)

    val outFile = config.outputSliceFile.createFileIfNotExists(createParents = true)
    val document =
        if format == "sarif" then
          MemorySafetySarif.document(
            findings,
            config.inputPath.pathAsString,
            MemorySafetySarif.toolVersion
          )
        else findings.asJson
    outFile.write(document.noSpaces)
    println(
      s"Memory-safety analysis complete. ${findings.size} findings written to ${outFile.pathAsString}"
    )
    Right("Memory-safety findings generated successfully")
  end runMemorySafety

  /** The template instance a finding belongs to, when its METHOD is the instance of a template: the
    * template's full name and the template declaration's position, which every instance of one
    * template carries, and the arguments this instance was written with.
    */
  private[atom] final case class TemplateInstance(
    template: String,
    line: Int,
    column: Int,
    args: String
  )

  /** The method a finding's node belongs to, reached per node type: there is no `.method` step on
    * AstNode, and an exit-node finding is a METHOD_RETURN rather than an Expression.
    */
  private def methodOf(node: AstNode): Option[Method] = node match
      case e: Expression   => Some(e.method)
      case m: MethodReturn => Some(m.method)
      case _               => None

  /** The instance a finding sits in: its METHOD's `template` tag names the template's METHOD, and
    * every instance of one template carries the same tag value and the same position. A finding
    * outside an instance METHOD has none.
    */
  private def instanceOf(node: AstNode): Option[TemplateInstance] =
    for
      m        <- methodOf(node)
      template <- m.tag.nameExact(TemplateTag).value.headOption
      args = m.tag.nameExact(TemplateArgsTag).value.headOption.getOrElse("")
    yield TemplateInstance(
      template,
      m.lineNumber.map(_.toInt).getOrElse(0),
      m.columnNumber.map(_.toInt).getOrElse(0),
      args
    )

  /** The instances of one template print their shared findings once: the finding every
    * instantiation has is one entry with an `instances` list naming each instance's
    * `template-args`, and a finding only some instances have lists only those. Findings are the
    * same finding when their METHODs share the template's tag and position and the rendered entries
    * are equal in every compared field (rule, place, confidence, message, flow), so the entry
    * printed is any one of them. The names an instance calls its own - `function`, `purl` and
    * `fingerprint`, which key on the instance method's full name - are not compared, so instances
    * still merge; the entry printed carries them from the entry kept. An instance that carries no
    * `template-args` adds no name to the list. A finding outside an instance METHOD is printed as
    * rendered, each on its own.
    */
  private[atom] def groupTemplateInstances(
    rendered: List[(Json, Option[TemplateInstance])]
  ): List[Json] =
    def compared(json: Json): Json =
        json.asObject
            .map(o => o.remove("function").remove("purl").remove("fingerprint").asJson)
            .getOrElse(json)
    // what a group keeps: its first entry as rendered (the new fields included - they were
    // dropped from the comparison key only) and the instance names it collects
    final case class Group(entry: Json, args: mutable.ArrayBuffer[String])
    val groups =
        mutable.LinkedHashMap.empty[Either[((String, Int, Int), Json), Int], Group]
    rendered.zipWithIndex.foreach { case ((json, instance), occurrence) =>
        // a finding outside an instance is its own group, keyed by its occurrence: two findings
        // that render identically still print both
        val key = instance match
          case Some(i) => Left(((i.template, i.line, i.column), compared(json)))
          case None    => Right(occurrence)
        val group = groups.getOrElseUpdate(key, Group(json, mutable.ArrayBuffer.empty))
        instance.foreach(i => group.args += i.args)
    }
    groups.toList.map {
        case (Left(_), group) =>
            group.entry.deepMerge(
              Json.obj("instances" -> group.args.filter(_.nonEmpty).distinct.sorted.asJson)
            )
        case (Right(_), group) => group.entry
    }
  end groupTemplateInstances

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
    * node's own tags, its memory operation, and the definitions that produced the value, plus the
    * names a report keys the finding by: the enclosing function, the vendored component's purl
    * when the finding sits in one, and the fingerprint.
    *
    * The file path is rooted at the input directory's name: c2cpg records FILE names relative to
    * the analysed input, and consumers of this JSON (a scorer matching on `c/`/`cpp/` prefixes, a
    * script re-rooting paths into a checkout) match on paths that carry that root.
    */
  private def render(
    atomFile: File,
    inputRoot: String,
    root: ReportedPaths.Root,
    vendored: VendoredCode,
    confidence: String
  )(
    node: AstNode,
    rule: MemorySafetyFindingPass.MemorySafetyRule,
    location: ReportedPaths.Location
  ): Json =
    val file   = ReportedPaths.printed(location, inputRoot)
    val line   = node.lineNumber.map(_.toInt).getOrElse(0)
    val column = node.columnNumber.map(_.toInt).getOrElse(0)
    // the enclosing method's full name, resolved the way `instanceOf` resolves the method, and
    // empty when the finding sits outside any method (as a defensive default; every finding node
    // is an Expression or a MethodReturn)
    val function = methodOf(node).map(_.fullName).getOrElse("")
    // the component a BOM located on the finding's file, exactly the attribution a callsites
    // record carries: a path only the --external-paths pattern names is vendored code without a
    // purl, so the field is omitted, never written empty
    val purl = vendored.of(filenameOf(node)).map(_.purl).filter(_.nonEmpty).getOrElse("")

    JsonObject(
      (
        List(
          "rule"       -> rule.id.asJson,
          "cwe"        -> rule.cwe.asJson,
          "kind"       -> rule.kind.asJson,
          "file"       -> file.asJson,
          "line"       -> line.asJson,
          "column"     -> column.asJson,
          "severity"   -> rule.severity.asJson,
          "confidence" -> confidence.asJson,
          "message" -> s"${node.code}: ${rule.message}${debugGuardNote(node)}${reentrantCallNote(node)}"
              .asJson,
          "function" -> function.asJson
        ) ++ Option.when(purl.nonEmpty)("purl" -> purl.asJson) ++ List(
          "flow"       -> flowOf(node, root).asJson,
          "fingerprint" -> fingerprintOf(rule.id, function, file, apisOf(node)).asJson,
          "atom"       -> atomFile.pathAsString.asJson
        )
      )*
    ).asJson
  end render

  /** The ordered distinct names of the finding's memory-operation calls: the finding node's own
    * name when it is itself a Call, plus the call it is an argument of (the same call the flow
    * renders with the `memory-operation` role). The names come from the graph nodes, never from
    * the rendered `code` text, so what feeds the fingerprint is what the graph says was called.
    */
  private def apisOf(node: AstNode): String =
    val own = node match
      case c: Call => c.name :: Nil
      case _       => Nil
    val parent = node._astIn.collectFirst { case c: Call => c }.map(_.name).toList
    (own ++ parent).distinct.mkString(",")

  /** The finding's fingerprint: SHA-256, as lowercase hex, over the UTF-8 bytes of the string
    * `rule|function|file|apis` - the rule id, the rendered `function` and `file` values, and the
    * `apis` of [[apisOf]] joined with "," (empty when there are none). The line and column are
    * deliberately out of the recipe, so an edit that moves the finding within its function keeps
    * the fingerprint a report matches it by. The recipe is published with the output's schema
    * (specification/schemas/MemorySafetyFinding.json) and must stay the same there.
    */
  private def fingerprintOf(rule: String, function: String, file: String, apis: String): String =
    val digest = java.security.MessageDigest.getInstance("SHA-256")
        .digest(s"$rule|$function|$file|$apis".getBytes("UTF-8"))
    digest.map(b => f"${b & 0xff}%02x").mkString

  /** The note a finding carries when the index's only lower bound was an assert's check: the author
    * wrote a guard that a release build compiles out, which is worth saying in the message the
    * finding already shows.
    */
  private def debugGuardNote(node: AstNode): String =
      if node.tag.name(MemorySafetyFindingPass.TagDebugGuard).nonEmpty then
        ". The only lower bound on this index is an assert, which a release build compiles out."
      else ""

  /** The call that can run Python code between an MS-INVAL-002 read and its use, named in the
    * message because it is the line a reader has to look at.
    */
  private def reentrantCallNote(node: AstNode): String =
      node.tag.nameExact(MemorySafetyFindingPass.TagReentrantCall).value.headOption match
        case Some(v) =>
            val at   = v.lastIndexOf('@')
            val code = if at >= 0 then v.substring(0, at) else v
            val line = if at >= 0 then v.substring(at + 1) else ""
            s". The call in between is `$code`" + (if line.nonEmpty then s" at line $line."
                                                   else ".")
        case None => ""

  /** The file of a flow row: a path under the input stays relative to it, without the input's name,
    * and a file outside the input prints as its absolute path.
    */
  private def flowFile(n: AstNode, root: ReportedPaths.Root): String =
      root.locate(n.file.name.headOption.getOrElse("")) match
        case ReportedPaths.Location.Inside(relative)  => relative
        case ReportedPaths.Location.Outside(absolute) => absolute

  /** The evidence chain: the offending argument, the memory operation it bounds, the origin and
    * guard facts the rule read, and the definitions that produced the value (the REACHING_DEF walk
    * backwards - where `df` would be paying for a solve this does not need).
    */
  private def flowOf(node: AstNode, root: ReportedPaths.Root): List[Json] =
    val entries = mutable.ListBuffer.empty[JsonObject]
    def entry(n: AstNode, role: String): Unit =
        entries += JsonObject(
          "file" -> flowFile(n, root).asJson,
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
    // a guard tag is valued `<bounding node id>:<code>`; the id differs between runs, so the
    // report names the bound by its code alone
    def boundCode(value: String): String = value.split(":", 2) match
      case Array(id, code) if id.nonEmpty && id.forall(_.isDigit) => code
      case _                                                      => value
    tags.filter(_.name == GuardPass.TagBelow)
        .foreach(t => textEntry(s"bounded-below by ${boundCode(t.value)}", "guard"))
    tags.filter(_.name == GuardPass.TagAbove)
        .foreach(t => textEntry(s"bounded-above by ${boundCode(t.value)}", "guard"))

    // the destination's extent, for the size-parameter rule
    node._astIn.collectFirst { case c: Call => c }.foreach { call =>
        call.argument.l
            .filter(a => a.tag.name(MemoryApiPass.TagDst).l.nonEmpty)
            .flatMap(_.tag.name(ExtentPass.TagExtent).value.l)
            // an `alloc:<node id>` extent names its allocation by a per-run id: show the
            // allocation's code instead
            .map(v =>
                v.split(":", 2) match
                  case Array("alloc", id) if id.nonEmpty && id.forall(_.isDigit) =>
                      scala.util.Try(call.graph.node(id.toLong)).toOption.flatMap(Option(_))
                          .map(n => s"alloc:${n.property("CODE")}").getOrElse("alloc")
                  case _ => v
            )
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
                // the edge order follows node ids, which differ between runs: order the
                // definitions by where they are written instead
                n._reachingDefIn.collectAll[StoredNode].l.filterNot(d => visited.contains(d.id))
                    .sortBy(d =>
                        (
                          d.property(PropertyNames.LINE_NUMBER, 0),
                          d.property(PropertyNames.COLUMN_NUMBER, 0),
                          String.valueOf(d.property("CODE"))
                        )
                    )
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
