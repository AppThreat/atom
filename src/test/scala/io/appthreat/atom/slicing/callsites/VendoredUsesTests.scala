package io.appthreat.atom.slicing.callsites

import io.appthreat.x2cpg.passes.ExternalPathsPass
import io.shiftleft.codepropertygraph.generated.{Cpg, EdgeTypes}
import io.shiftleft.codepropertygraph.generated.nodes.{NewCall, NewMethod, NewTag}
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.BatchedUpdate

/** The records `VendoredUses` makes from the graph's CALL edges. A call can be linked to several
  * vendored methods (a dynamic dispatch the linkers resolve to every override), and which edge the
  * graph lists first is the linkers' business: the record must not depend on it.
  *
  * The graph is built by hand because no fixture's linkers tie one call to two vendored methods
  * deterministically: a project function `main` calls `dispatch`, which `third_party/lib/a.c` and
  * `third_party/lib/b.c` both define under the names `third_party.A.go` and `third_party.B.go` (the
  * tag the external-paths pass leaves on the methods it marks).
  */
class VendoredUsesTests extends AnyWordSpec with Matchers:

  private val vendored = VendoredCode("third_party", Nil)

  /** The records of the graph in which the call's edges are added in `edges` order. */
  private def recordsWith(methodFullName: String, edges: List[String]): List[CallSiteRecord] =
    val cpg     = Cpg.empty
    val builder = new BatchedUpdate.DiffGraphBuilder
    val project =
        NewMethod().name("main").fullName("main").filename("src/main.c").isExternal(false)
    def library(owner: String, file: String) =
        NewMethod().name("go").fullName(s"third_party.$owner.go").filename(file).isExternal(true)
    val libs = Map(
      "A" -> library("A", "third_party/lib/a.c"),
      "B" -> library("B", "third_party/lib/b.c")
    )
    val call = NewCall().name("dispatch").methodFullName(methodFullName).code("dispatch()")
        .lineNumber(5).columnNumber(3)
    val tag = NewTag().name(ExternalPathsPass.TagName).value("")
    builder.addNode(project)
    builder.addNode(tag)
    libs.values.foreach { lib =>
      builder.addNode(lib)
      builder.addEdge(lib, tag, EdgeTypes.TAGGED_BY)
    }
    builder.addNode(call)
    edges.foreach(owner => builder.addEdge(call, libs(owner), EdgeTypes.CALL))
    BatchedUpdate.applyDiff(cpg.graph, builder)
    val main = cpg.method.nameExact("main").head
    try new VendoredUses(cpg, vendored, _ => Some(main)).records()
    finally cpg.close()
  end recordsWith

  "the uses of vendored code" should {

      "name the smaller method of two the call is linked to, whichever edge comes first" in {
          val ab = recordsWith("Base.go", List("A", "B"))
          val ba = recordsWith("Base.go", List("B", "A"))
          ab.map(_.resolvedMethod) shouldBe List("third_party.A.go")
          ba shouldBe ab
          ab.map(r => (r.file, r.line, r.module, r.path, r.kind, r.via)) shouldBe
              List(("src/main.c", Integer.valueOf(5), "third_party", "go", "call", "binding"))
      }

      "name the method the call names before any other it is linked to" in {
          val ab = recordsWith("third_party.B.go", List("A", "B"))
          val ba = recordsWith("third_party.B.go", List("B", "A"))
          ab.map(_.resolvedMethod) shouldBe List("third_party.B.go")
          ba shouldBe ab
      }
  }
end VendoredUsesTests
