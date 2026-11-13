package io.appthreat.atom.passes

import io.appthreat.atom.slicing.JsNames
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.shiftleft.codepropertygraph.generated.nodes.{Call, Method, NewTag}
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language.*

/** Sink tags on the calls project code makes into npm packages, for the graphs jssrc2cpg builds
  * with `node_modules` left out.
  *
  * The 3.2.1 release parsed the dependency code itself and reachables saw its sinks; the anchored
  * EasyTagsPass arms kept the artifact flows out (atom#402) but nothing replaced the boundary: a
  * call into a package the project imports resolves to an external stub (`axios:get`,
  * `express:get`), and no JS arm tags one, so a project whose flows all cross the boundary comes
  * out without the dependency ones. This pass tags those calls the way [[PhpDependencySinkTagger]]
  * tags the Composer boundary for PHP. The callee name is matched against the module specifiers the
  * project actually imports, by [[JsNames.split]], the same rule chen's `PackageAttribution`
  * applies when it attributes purls to resolved names; a match means the callee belongs to an
  * imported package, whose code this graph does not carry. A call into a known HTTP request client
  * becomes an `http-client` sink instead of `library-call`, so a value that decides a request's
  * target stays in the one sink category reachables uses to tell a chosen URL from an API walked
  * with a previous response's data. Node builtins are no package: chen names a sink reached through
  * one (`node:fs`, `file-io`) itself. A call EasyTagsPass already made a sink (a response call, a
  * database call, an SDK verb, a rendered template) keeps its own vocabulary and is left alone, and
  * so is a call whose callee the graph declares - a workspace package's code parsed with the
  * project is the project's own.
  *
  * The tag goes on the call node, where reachables' sink queries look for it, and the SBOM's purl
  * for the package follows later: chen's `PackageAttribution` reads `library-call` and
  * `http-client` sinks and layers `pkg:npm/...` tags on them from the project's BOM, so one
  * provenance owns package identity. Without an SBOM the sinks still exist and carry no purl, like
  * every other JS tag. A call resolved through a project class that inherits a package class's
  * member (`ChallengeModel.bulkCreate()`) is named `<file>::program:<Class>:<member>` and resolved
  * by the callsites slice's private binding resolver, which a pass cannot reach; those stay unsunk,
  * as do the unresolved callees (`helper.load`) and relative-specifier shapes no module matches.
  */
class JsDependencySinkTagger(atom: Cpg) extends CpgPass(atom):

  override def run(dstGraph: DiffGraphBuilder): Unit =
      if JsNames.isJs(atom.metaData.language.headOption) then
        val js = JsNames(atom)
        // one lookup: the calls the earlier tag passes already made sinks
        val alreadySunk = atom.tag.name(JsDependencySinkTagger.ExistingSinkTags.mkString("|"))
            ._taggedByIn.collect { case c: Call => c }.id.l.toSet
        def tag(node: Call, name: String): Unit =
          val created = NewTag().name(name)
          dstGraph.addNode(created)
          dstGraph.addEdge(node, created, EdgeTypes.TAGGED_BY)
        atom.call
            .filterNot(c => c.name.startsWith("<operator>") || alreadySunk.contains(c.id()))
            .foreach { c =>
                js.split(c.methodFullName).foreach { (module, _) =>
                    if !JsNames.isNodeBuiltinSpecifier(module) &&
                      c._callOut.collectAll[Method].l.forall(_.isExternal)
                    then
                      tag(c, JsDependencySinkTagger.moduleSinkTag(module))
                }
            }
end JsDependencySinkTagger

object JsDependencySinkTagger:
  val LibraryCallTag = "library-call"

  val HttpClientTag = "http-client"

  /** The tags that make a JS call a sink already: what the EasyTagsPass arms put on response calls,
    * database calls, SDK verbs, rendered templates and (behind an SBOM) request clients.
    */
  private val ExistingSinkTags = Set(
    LibraryCallTag,
    HttpClientTag,
    "framework-output",
    "xss",
    "sql",
    "file-io",
    "shell-exec",
    "code-execution"
  )

  /** The npm packages that send HTTP requests, the list EasyTagsPass keeps for the JavaScript
    * package managers.
    */
  val HttpClientModules: Set[String] = Set(
    "got",
    "axios",
    "undici",
    "ky",
    "node-fetch",
    "cross-fetch",
    "superagent",
    "needle",
    "isomorphic-fetch",
    "unfetch",
    "wretch",
    "request",
    "cacheable-lookup",
    "cacheable-request",
    "http2-wrapper",
    "responselike"
  )

  private def moduleSinkTag(module: String): String =
      if HttpClientModules.contains(module) then HttpClientTag else LibraryCallTag
end JsDependencySinkTagger
