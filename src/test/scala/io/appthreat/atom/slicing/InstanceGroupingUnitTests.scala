package io.appthreat.atom.slicing

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The grouping of reachable flows and usage entries by template instance, on hand-built input: no
  * front end runs, so the rules hold whichever front end built the graph.
  */
class InstanceGroupingUnitTests extends AnyWordSpec with Matchers:

  private def node(id: Long, method: String, line: Int, code: String = "x"): SliceNode =
      SliceNode(
        id,
        "IDENTIFIER",
        code = code,
        parentMethodName = method,
        parentMethodSignature = "int()",
        parentFileName = "a.cpp",
        lineNumber = Some(line),
        columnNumber = Some(1)
      )

  /** Instance methods `A<int>::get` and `A<char>::get` of template `A.get`, `B<int>::get` of
    * template `B.get`, all with the same name and signature, and a plain `Stack::get`.
    */
  private val instances = ReachableSlicing.InstanceMethods(
    byFullName = Map(
      "A<int>.get:int()"  -> (("A.get", 3, 1), "int"),
      "A<char>.get:int()" -> (("A.get", 3, 1), "char"),
      "B<int>.get:int()"  -> (("B.get", 9, 1), "int")
    ),
    parentOf = Map(
      1L -> "A<int>.get:int()",
      2L -> "A<char>.get:int()",
      3L -> "B<int>.get:int()",
      4L -> "Stack.get:int()"
    ).get
  )

  "the reachable flow grouping" should {

      "merge the flows of the instances of one template" in {
          val grouped = ReachableSlicing.groupInstanceFlows(
            Vector(
              ReachableFlows(List(node(1, "get", 4)), Set.empty),
              ReachableFlows(List(node(2, "get", 4)), Set.empty)
            ),
            instances
          )
          grouped.map(_.instances) shouldBe Vector(List("char", "int"))
      }

      "leave a flow in a method that is no instance alone, without instances" in {
          val plain = ReachableFlows(List(node(4, "get", 4)), Set.empty)
          val grouped = ReachableSlicing.groupInstanceFlows(
            Vector(ReachableFlows(List(node(1, "get", 4)), Set.empty), plain),
            instances
          )
          grouped should contain(plain)
          grouped.filter(_.flows.head.id == 4).map(_.instances) shouldBe Vector(Nil)
      }

      "keep two templates whose instances share a name and signature apart" in {
          val grouped = ReachableSlicing.groupInstanceFlows(
            Vector(
              ReachableFlows(List(node(1, "get", 4)), Set.empty),
              ReachableFlows(List(node(3, "get", 4)), Set.empty)
            ),
            instances
          )
          grouped should have size 2
          grouped.map(_.instances) shouldBe Vector(List("int"), List("int"))
      }

      "name no instance for an instance that carries no arguments" in {
          val bare = instances.copy(byFullName =
              instances.byFullName.updated("A<char>.get:int()", (("A.get", 3, 1), ""))
          )
          val grouped = ReachableSlicing.groupInstanceFlows(
            Vector(
              ReachableFlows(List(node(1, "get", 4)), Set.empty),
              ReachableFlows(List(node(2, "get", 4)), Set.empty)
            ),
            bare
          )
          grouped.map(_.instances) shouldBe Vector(List("int"))
      }
  }

  "the usage entry grouping" should {

      final case class M(name: String, group: Option[(String, String, Int)], args: List[String])

      def group(entries: List[(M, List[String])]) =
          UsageSlicing.groupInstanceEntries[M, String, (String, String, Int)](
            entries,
            _.group,
            _.args,
            _.name
          )

      "merge instances of one template in one file and keep other files apart" in {
          val a1      = M("A<int>", Some(("A.get", "a.cpp", 3)), List("int"))
          val a2      = M("A<char>", Some(("A.get", "a.cpp", 3)), List("char"))
          val b       = M("A<long>", Some(("A.get", "b.cpp", 3)), List("long"))
          val grouped = group(List(a1 -> List("u1"), a2 -> List("u2"), b -> List("u3")))
          grouped.map((m, u, i) => (m.name, u, i)) shouldBe List(
            ("A<char>", Set("u1", "u2"), Some(List("char", "int"))),
            ("A<long>", Set("u3"), Some(List("long")))
          )
      }

      "leave entries outside a template as they are and drop empty arguments" in {
          val plain = M("plain", None, Nil)
          val a1    = M("A<int>", Some(("A.get", "a.cpp", 3)), List(""))
          group(List(plain -> List("u"), a1 -> List("v"))).map((m, _, i) => (m.name, i)) shouldBe
              List(("A<int>", None), ("plain", None))
      }
  }
end InstanceGroupingUnitTests
