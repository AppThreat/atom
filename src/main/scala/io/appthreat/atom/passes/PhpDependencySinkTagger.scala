package io.appthreat.atom.passes

import io.appthreat.atom.slicing.callsites.PhpCallSiteSource
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.{EdgeTypes, Languages}
import io.shiftleft.codepropertygraph.generated.nodes.{Call, NewTag}
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language.*

/** Sink tags on the calls project code makes into dependency code, for the graphs php2atom builds
  * with `vendor/` left out.
  *
  * The release 3.2.1 parsed the vendor code itself and reachables saw its sinks; the frontend now
  * records a dependency-boundary use instead (atom#395): every resolved instance call carries
  * php2atom's `callsites-name` tag, whatever class the receiver holds. No pass turned those uses
  * into sinks, so a Composer project whose flows all cross the boundary came out without a single
  * one. This pass applies [[PhpCallSiteSource]]'s own externality rule to the class the call's full
  * name names, and tags the calls whose class the project does not declare as `library-call` sinks,
  * the vocabulary CdxPass uses for JVM calls into a `pkg:maven` component. The package URL goes on
  * the call node as a `pkg:composer/...` tag: it joins the slice's purls and matches the `pkg.*`
  * sink pattern, so a flow can be tied back to a dependency CVE.
  *
  * A call the project can answer itself stays unsunk, and a receiver whose class happens to look
  * like a vendor one in another namespace (`App\Request`, `Local\Monolog\Logger`) is the project's
  * by the same rule the callsites slice reads classes by.
  */
class PhpDependencySinkTagger(atom: Cpg) extends CpgPass(atom):

  override def run(dstGraph: DiffGraphBuilder): Unit =
      if atom.metaData.language.headOption.exists(_ == Languages.PHP) then
        val source = PhpCallSiteSource(atom)
        def tag(node: Call, name: String): Unit =
          val created = NewTag().name(name)
          dstGraph.addNode(created)
          dstGraph.addEdge(node, created, EdgeTypes.TAGGED_BY)
        atom.tag.name(PhpCallSiteSource.NamePositionTag)._taggedByIn.collect { case c: Call => c }
            .foreach { c =>
                source.classOfCallName(c.methodFullName).foreach { cls =>
                    if source.externalClass(cls) then
                      tag(c, PhpDependencySinkTagger.LibraryCallTag)
                      source.purlOf(cls) match
                        case purl if purl.nonEmpty =>
                            tag(c, purl)
                            if source.packageNameOf(cls).exists(
                                PhpDependencySinkTagger.HttpClientPackages.contains
                              )
                            then tag(c, PhpDependencySinkTagger.HttpClientTag)
                        case _ => ()
                }
            }
end PhpDependencySinkTagger

object PhpDependencySinkTagger:
  val LibraryCallTag = "library-call"

  val HttpClientTag = "http-client"

  /** The Composer packages that send HTTP requests, mirroring the list EasyTagsPass keeps for the
    * JavaScript package managers.
    */
  val HttpClientPackages: Set[String] = Set("guzzlehttp/guzzle", "symfony/http-client")
