package io.appthreat.atom

import io.shiftleft.codepropertygraph.generated.{Cpg, EdgeTypes}
import io.shiftleft.codepropertygraph.generated.nodes.{NewLiteral, NewMethod}
import io.shiftleft.passes.SimpleCpgPass
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.BatchedUpdate
import io.shiftleft.semanticcpg.language._
import io.shiftleft.semanticcpg.testing.MockCpg
import better.files.{File => F}

class MyTest extends AnyWordSpec with Matchers {

  "Foo" should {

    "description1" in {
      val cpg = MockCpg()
        .withMethod("foo")
        .withCallInMethod("foo", "bar")
        .withCustom { case (diffGraph, cpg) =>
          val literal = NewLiteral().code("abc")
          diffGraph.addNode(literal)
          val block = cpg.method.block.head
          diffGraph.addEdge(block, literal, EdgeTypes.AST)
        }
        .cpg
      cpg.method.block.astChildren.isLiteral.foreach(println)
    }

    "description2" in {
      F.usingTemporaryFile("atom") { file =>
        println(file)
      }
    }
  }

}
