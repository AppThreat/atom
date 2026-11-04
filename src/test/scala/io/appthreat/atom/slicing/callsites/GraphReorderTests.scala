package io.appthreat.atom.slicing.callsites

import better.files.File as BFile
import io.appthreat.atom.Atom
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.semanticcpg.language.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The test tool the determinism suite stands on: a copy of an atom whose nodes were created in
  * another order must be the same graph, and must differ from the original in what a slice that
  * depends on creation order would read (the node ids and the order of the nodes of a label).
  * Without that the suite would pass for any slice.
  */
class GraphReorderTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomGraphReorder")
    val project = workspace / "proj"
    BFile(getClass.getResource("/py-usages/callsites").toURI).copyTo(project)
    val result = Atom.run(
      Array(
        "callsites",
        "--cache",
        "none",
        "-l",
        "python",
        "-o",
        (workspace / "py.atom").pathAsString,
        "-s",
        (workspace / "py.json").pathAsString,
        project.pathAsString
      )
    )
    withClue("the build of the fixture: ")(result.isRight shouldBe true)

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def load() = CpgLoader.loadFromOverflowDb(
    CpgLoaderConfig().withOverflowConfig(
      overflowdb.Config.withDefaults().withStorageLocation((workspace / "py.atom").pathAsString)
    ).doNotCreateIndexesOnLoad
  )

  "GraphReorder" should {

      "copy every node and edge, with the same properties" in {
          val original = load()
          val reversed = GraphReorder.reversed(original)
          val shuffled = GraphReorder.shuffled(original, 7L)
          try
            List(reversed, shuffled).foreach { copy =>
              copy.graph.nodeCount() shouldBe original.graph.nodeCount()
              copy.graph.edgeCount() shouldBe original.graph.edgeCount()
              copy.graph.nodeCountByLabel() shouldBe original.graph.nodeCountByLabel()
              copy.graph.edgeCountByLabel() shouldBe original.graph.edgeCountByLabel()
              copy.method.fullName.l.sorted shouldBe original.method.fullName.l.sorted
              copy.call.code.l.sorted shouldBe original.call.code.l.sorted
              copy.tag.map(t => (t.name, t.value)).l.sorted shouldBe
                  original.tag.map(t => (t.name, t.value)).l.sorted
            }
          finally
            reversed.close()
            shuffled.close()
            original.close()
      }

      "create the parts in the opposite order and give the nodes other ids" in {
          val original = load()
          val reversed = GraphReorder.reversed(original)
          try
            val parts = GraphReorder.parts(original)
            parts.filter(_.endsWith(".py")) should have size 7
            GraphReorder.parts(reversed) shouldBe parts.reverse
            val ids = (cpg: io.shiftleft.codepropertygraph.generated.Cpg) =>
                cpg.method.map(m => m.fullName -> m.id()).l.toMap
            val before = ids(original)
            val after  = ids(reversed)
            after.keySet shouldBe before.keySet
            before.keys.count(name => after(name) != before(name)) should be > before.size / 2
          finally
            reversed.close()
            original.close()
      }

      "keep the nodes of one file together and in their order" in {
          val original = load()
          val reversed = GraphReorder.reversed(original)
          try
            def callsOf(cpg: io.shiftleft.codepropertygraph.generated.Cpg, file: String) =
                cpg.call.filter(_.method.filename == file).sortBy(_.id()).code.l
            callsOf(reversed, "imports.py") shouldBe callsOf(original, "imports.py")
          finally
            reversed.close()
            original.close()
      }

      "give the same shuffle for the same seed and another for another seed" in {
          val original = load()
          val a        = GraphReorder.shuffled(original, 3L)
          val b        = GraphReorder.shuffled(original, 3L)
          val c        = GraphReorder.shuffled(original, 4L)
          try
            a.call.sortBy(_.id()).code.l shouldBe b.call.sortBy(_.id()).code.l
            a.call.sortBy(_.id()).code.l should not be c.call.sortBy(_.id()).code.l
          finally
            List(a, b, c, original).foreach(_.close())
      }
  }
end GraphReorderTests
