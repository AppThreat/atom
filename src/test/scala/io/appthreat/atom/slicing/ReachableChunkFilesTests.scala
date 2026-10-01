package io.appthreat.atom.slicing

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** A reachables run must not leave the numbered chunks of an earlier, larger run behind. */
class ReachableChunkFilesTests extends AnyFunSuite with Matchers:

  test("only the numbered chunks and leftover temporary files of the base path are removed"):
    File.usingTemporaryDirectory("atom-chunks") { dir =>
      val names = Seq(
        "python-reachables.slices.json",
        "python-reachables.slices_1.json",
        "python-reachables.slices_12.json",
        "python-reachables.slices_.json",
        "python-reachables.slices_x.json",
        "python-reachables.slices_1.json.bak",
        "js-reachables.slices_1.json",
        ".python-reachables.slices.json.4242.tmp",
        ".python-reachables.slices_3.json.4242.tmp",
        ".js-reachables.slices.json.4242.tmp"
      )
      names.foreach(n => (dir / n).writeText("[]"))
      ReachableSlicing.removeChunkFiles((dir / "python-reachables.slices").pathAsString)
      dir.list.map(_.name).toSet shouldBe Set(
        "python-reachables.slices.json",
        "python-reachables.slices_.json",
        "python-reachables.slices_x.json",
        "python-reachables.slices_1.json.bak",
        "js-reachables.slices_1.json",
        ".js-reachables.slices.json.4242.tmp"
      )
    }

  test("a base path in a missing directory is a no-op"):
    noException should be thrownBy ReachableSlicing.removeChunkFiles(
      "/nonexistent-dir-4419/a.slices"
    )
end ReachableChunkFilesTests
