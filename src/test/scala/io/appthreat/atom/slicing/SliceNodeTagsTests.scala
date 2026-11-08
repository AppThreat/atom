package io.appthreat.atom.slicing

import io.shiftleft.codepropertygraph.generated.{Cpg, EdgeTypes}
import io.shiftleft.codepropertygraph.generated.nodes.{NewCall, NewMethod, NewTag}
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.BatchedUpdate

import java.util.regex.Pattern

/** The tag string a reachables flow node shows for a call to an external library method. The graph
  * is built by hand: `handler` calls the library's `parseObject` twice, once untagged and once
  * tagged `service-egress` by a tagger, and the library method carries the library's purl and a
  * description tag, the way a BOM-driven tagger leaves them (atom#385).
  */
class SliceNodeTagsTests extends AnyWordSpec with Matchers:

  private val purl = "pkg:maven/com.alibaba/fastjson@1.2.24?type=jar"

  /** The tag strings of the untagged and the tagged call, in that order. */
  private def tagStrings(): (String, String) =
    val cpg     = Cpg.empty
    val builder = new BatchedUpdate.DiffGraphBuilder
    val handler = NewMethod().name("handler").fullName("app.Handler.handler").isExternal(false)
    val library = NewMethod().name("parseObject")
        .fullName("com.alibaba.fastjson.JSON.parseObject").isExternal(true)
    def call(line: Int) =
        NewCall().name("parseObject").methodFullName("com.alibaba.fastjson.JSON.parseObject")
            .code("JSON.parseObject(params)").lineNumber(line)
    val untagged = call(23)
    val tagged   = call(24)
    val pkgTag   = NewTag().name(purl).value("")
    val descTag  = NewTag().name("comp-desc:json").value("")
    val ownTag   = NewTag().name("service-egress").value("")
    List(handler, library, untagged, tagged, pkgTag, descTag, ownTag).foreach(builder.addNode)
    builder.addEdge(library, pkgTag, EdgeTypes.TAGGED_BY)
    builder.addEdge(library, descTag, EdgeTypes.TAGGED_BY)
    builder.addEdge(tagged, ownTag, EdgeTypes.TAGGED_BY)
    List(untagged, tagged).foreach { c =>
      builder.addEdge(handler, c, EdgeTypes.CONTAINS)
      builder.addEdge(c, library, EdgeTypes.CALL)
    }
    BatchedUpdate.applyDiff(cpg.graph, builder)
    try
      val pattern = Pattern.compile("service-.*")
      def shown(line: Int) =
          ReachableSlicing.createSliceNode(cpg.call.lineNumber(line).head, pattern)._1.get.tags
      (shown(23), shown(24))
    finally cpg.close()
  end tagStrings

  "a reachables flow node for a call to a library method" should {

      "take all of the callee's tags when the call has none of its own" in {
          val (untagged, _) = tagStrings()
          untagged.split(", ").toSet shouldBe Set(purl, "comp-desc:json")
      }

      "keep its own tags and add the callee's purl when a tagger marked it" in {
          val (_, tagged) = tagStrings()
          tagged shouldBe s"service-egress, $purl"
      }
  }
end SliceNodeTagsTests
