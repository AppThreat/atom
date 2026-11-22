package io.appthreat.atom.slicing

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.Files
import scala.util.{Failure, Success, Try}

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

  test("a symbolic link at the old predictable temporary name is never written through"):
    File.usingTemporaryDirectory("atom-slice-files") { dir =>
      val outside = dir / "notes.txt"
      outside.write("operator data")
      val target = dir / "python-usages.slices.json"
      // The checkout, not the run, decides what sits beside the name atom writes to: the
      // temporary used to be `.<name>.<pid>.tmp`, a name a checkout could commit as a link
      val oldTemp = dir / s".${target.name}.${ProcessHandle.current().pid()}.tmp"
      Try(Files.createSymbolicLink(oldTemp.path, outside.path)) match
        case Failure(e) =>
            cancel(s"this platform refuses to create a symbolic link: ${e.getMessage}")
        case Success(_) =>
            SliceFiles.writeAtomically(target)(_.writeText("""{"objectSlices":[]}"""))
            outside.contentAsString shouldBe "operator data"
            target.isRegularFile shouldBe true
            target.contentAsString shouldBe """{"objectSlices":[]}"""
    }

  test("a slices file gets the permissions of a file created in place"):
    File.usingTemporaryDirectory("atom-slice-perms") { dir =>
      assume(Files.getFileStore(dir.path).supportsFileAttributeView("posix"), "POSIX only")
      val reference = (dir / "reference.json").createFile()
      val target    = dir / "python-usages.slices.json"
      SliceFiles.writeAtomically(target)(_.writeText("[]"))
      Files.getPosixFilePermissions(target.path) shouldBe
          Files.getPosixFilePermissions(reference.path)
    }
end SliceFilesTests
