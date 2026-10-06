package io.appthreat.atom

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** Where the scalasem report lands, and when an existing one is reused. The default must keep the
  * report out of the scanned project whenever the run knows an output location, because a build
  * would otherwise treat it as a source file.
  */
class ScalasemSlicesTests extends AnyFunSuite with Matchers:

  private def config(input: File): DefaultAtomConfig =
    val config = DefaultAtomConfig()
    config.withInputPath(input)
    config

  private def withDir(body: File => Unit): Unit =
    val dir = File.newTemporaryDirectory("scalasem-tests-")
    try body(dir)
    finally dir.delete(swallowIOExceptions = true)

  private def parsed(args: String*): AtomConfig =
      Atom.parseConfig(args.toList) match
        case Right(config: AtomConfig) => config
        case other                     => fail(s"unexpected parse result $other")

  test("an absolute env path is used as given"):
    withDir { dir =>
      val target = (dir / "reports" / "my-scala.json").pathAsString
      Atom.scalaSemanticsSlicesFile(config(dir), Some(target)) shouldBe target
    }

  test("a relative env path resolves against the scalasem work directory"):
    withDir { dir =>
        Atom.scalaSemanticsSlicesFile(config(dir), Some("scala.json")) shouldBe
            (dir / "scala.json").pathAsString
    }

  test("the report sits beside the slice file that -s names"):
    withDir { dir =>
      val slice = (dir / "out" / "usages.slices.json").pathAsString
      val config =
          parsed("usages", "-l", "scala", "-s", slice, dir.pathAsString)
      Atom.scalaSemanticsSlicesFile(config, None) shouldBe
          (dir / "out" / "semantics.slices.json").pathAsString
    }

  test("without -s the report sits beside the atom file that -o names"):
    withDir { dir =>
      // -o may name a default-named file in another directory.
      val atomFile = (dir / "out" / "app.atom").pathAsString
      val config =
          parsed("usages", "-l", "scala", "-o", atomFile, dir.pathAsString)
      Atom.scalaSemanticsSlicesFile(config, None) shouldBe
          (dir / "out" / "semantics.slices.json").pathAsString
    }

  test("without an output location the report stays in the input path"):
    withDir { dir =>
      val config = parsed("usages", "-l", "scala", dir.pathAsString)
      Atom.scalaSemanticsSlicesFile(config, None) shouldBe
          (dir / "semantics.slices.json").pathAsString
    }

  test("only a version 2 report of the same project is reused, and only when asked"):
    withDir { dir =>
      val project = (dir / "project").createDirectories()
      val path    = project.pathAsString.replace("\\", "\\\\")
      val report  = dir / "semantics.slices.json"
      report.writeText(
        s"""{"_meta": {"schemaVersion": "scalasem/2", "projectPath": "$path"}}"""
      )
      Atom.isScalaSemanticsSliceReusable(
        report.pathAsString,
        project.pathAsString,
        enabled = false
      ) shouldBe
          false
      Atom.isScalaSemanticsSliceReusable(
        report.pathAsString,
        project.pathAsString,
        enabled = true
      ) shouldBe
          true
      val other = (dir / "other").createDirectories()
      Atom.isScalaSemanticsSliceReusable(
        report.pathAsString,
        other.pathAsString,
        enabled = true
      ) shouldBe
          false
      val version1 = dir / "v1.slices.json"
      version1.writeText("""{"src/App.scala": {"usedTypes": []}, "schemaVersion": "scalasem/2"}""")
      Atom.isScalaSemanticsSliceReusable(
        version1.pathAsString,
        project.pathAsString,
        enabled = true
      ) shouldBe
          false
      Atom.isScalaSemanticsSliceReusable(
        (dir / "missing.json").pathAsString,
        project.pathAsString,
        enabled = true
      ) shouldBe false
    }
end ScalasemSlicesTests
