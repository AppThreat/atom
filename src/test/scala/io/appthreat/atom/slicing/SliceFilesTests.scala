package io.appthreat.atom.slicing

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** A slices file is either the previous complete one or the new complete one, never a fragment. */
class SliceFilesTests extends AnyFunSuite with Matchers:

  test("a completed write replaces the target and leaves no temporary file"):
    File.usingTemporaryDirectory("atom-slice-files") { dir =>
      val target = dir / "sub" / "python-usages.slices.json"
      SliceFiles.writeAtomically(target)(_.writeText("""{"objectSlices":[]}"""))
      target.contentAsString shouldBe """{"objectSlices":[]}"""
      SliceFiles.writeAtomically(target)(_.writeText("[]"))
      target.contentAsString shouldBe "[]"
      (dir / "sub").list.map(_.name).toList shouldBe List("python-usages.slices.json")
    }

  test("a write that fails part-way leaves the previous file in place"):
    File.usingTemporaryDirectory("atom-slice-files") { dir =>
      val target = dir / "python-reachables.slices.json"
      target.writeText("""[{"flows":[]}]""")
      an[IllegalStateException] should be thrownBy SliceFiles.writeAtomically(target) { temp =>
        temp.writeText("""[{"flo""")
        throw IllegalStateException("stopped while writing")
      }
      target.contentAsString shouldBe """[{"flows":[]}]"""
      dir.list.map(_.name).toList shouldBe List("python-reachables.slices.json")
    }
end SliceFilesTests
