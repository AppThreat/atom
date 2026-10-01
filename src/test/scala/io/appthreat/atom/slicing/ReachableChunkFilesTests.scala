package io.appthreat.atom.slicing

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** A reachables run must not leave the numbered chunks of an earlier, larger run behind. */
class ReachableChunkFilesTests extends AnyFunSuite with Matchers:

  test("only the numbered chunks of the base path are removed"):
    File.usingTemporaryDirectory("atom-chunks") { dir =>
      val names = Seq(
        "python-reachables.slices.json",
        "python-reachables.slices_1.json",
        "python-reachables.slices_12.json",
        "python-reachables.slices_.json",
        "python-reachables.slices_x.json",
        "python-reachables.slices_1.json.bak",
        "js-reachables.slices_1.json"
      )
      names.foreach(n => (dir / n).writeText("[]"))
      ReachableSlicing.removeChunkFiles((dir / "python-reachables.slices").pathAsString)
      dir.list.map(_.name).toSet shouldBe Set(
        "python-reachables.slices.json",
        "python-reachables.slices_.json",
        "python-reachables.slices_x.json",
        "python-reachables.slices_1.json.bak",
        "js-reachables.slices_1.json"
      )
    }

  test("a base path in a missing directory is a no-op"):
    noException should be thrownBy ReachableSlicing.removeChunkFiles(
      "/nonexistent-dir-4419/a.slices"
    )
end ReachableChunkFilesTests
