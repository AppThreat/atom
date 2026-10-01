package io.appthreat.atom

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class IncludePathEnvTests extends AnyFunSuite with Matchers:

  test("every directory in a CHEN_INCLUDE_PATH list is used, missing ones are skipped"):
    val first    = File.newTemporaryDirectory("inc-a").deleteOnExit()
    val second   = File.newTemporaryDirectory("inc-b").deleteOnExit()
    val missing  = (first / "does-not-exist").pathAsString
    val expected = Set(first.pathAsString, second.pathAsString)
    Atom.includePathsFrom(
      s"${first.pathAsString}:${second.pathAsString}:$missing",
      windows = false
    ) shouldBe expected
    Atom.includePathsFrom(
      s"${first.pathAsString};${second.pathAsString}",
      windows = false
    ) shouldBe expected
    Atom.includePathsFrom(first.pathAsString, windows = false) shouldBe Set(first.pathAsString)
    Atom.includePathsFrom("", windows = false) shouldBe Set.empty

  test("on Windows only ';' separates entries, so drive letters survive"):
    val dir = File.newTemporaryDirectory("inc-w").deleteOnExit()
    // a single entry that contains ':' is not split
    Atom.includePathsFrom(s"${dir.pathAsString};", windows = true) shouldBe Set(dir.pathAsString)
    Atom.includePathsFrom("C:\\no\\such\\dir;D:\\nor\\this", windows = true) shouldBe Set.empty
end IncludePathEnvTests
