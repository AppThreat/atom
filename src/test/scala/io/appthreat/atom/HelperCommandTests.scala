package io.appthreat.atom

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.attribute.PosixFilePermissions

/** [[HelperCommand]] starts `atom-tools` and `scalasem` with an argument vector, so shell syntax in
  * a scanned path or file name stays part of one argument and is never run.
  */
class HelperCommandTests extends AnyFunSuite with Matchers:

  private val shellSyntax = Seq(
    "usages$(touch dollar-marker).json",
    "x';touch quote-marker;'",
    "a;touch semicolon-marker",
    "`touch backtick-marker`",
    "b && touch and-marker"
  )

  test("a POSIX command is the program name followed by the arguments unchanged"):
    HelperCommand.command("atom-tools", shellSyntax, Map("PATH" -> "/usr/bin"), "Linux") shouldBe
        Right("atom-tools" +: shellSyntax)

  test("arguments with shell syntax reach the program as they are and run nothing"):
    assume(!scala.util.Properties.isWin, "needs a POSIX shell script as the stand-in program")
    File.usingTemporaryDirectory("atom-helper") { dir =>
      val recorded = dir / "args.txt"
      val program  = dir / "record-args"
      program.write(
        s"""#!/bin/sh
             |for arg in "$$@"; do printf '%s\\n' "$$arg"; done > '${recorded.pathAsString}'
             |""".stripMargin
      )
      java.nio.file.Files.setPosixFilePermissions(
        program.path,
        PosixFilePermissions.fromString("rwx------")
      )
      val result = HelperCommand.run(program.pathAsString, shellSyntax, dir.pathAsString)
      result.isSuccess shouldBe true
      recorded.lines.toSeq shouldBe shellSyntax
      dir.list.map(_.name).filter(_.endsWith("-marker")).toSeq shouldBe empty
    }

  test("atom-tools convert gets one argument per value"):
    Atom.atomToolsConvertArgs(
      "/scan/usages$(id).json",
      Some("/scan/x y-semantics.slices.json"),
      "scala",
      "openapi3.1.0",
      "/work/scala-openapi.json"
    ) shouldBe Seq(
      "convert",
      "-i",
      "/scan/usages$(id).json",
      "-e",
      "/scan/x y-semantics.slices.json",
      "-t",
      "scala",
      "-f",
      "openapi3.1.0",
      "-o",
      "/work/scala-openapi.json"
    )
    Atom.atomToolsConvertArgs("s.json", None, "js", "openapi3.0.1", "o.json") shouldBe
        Seq("convert", "-i", "s.json", "-t", "js", "-f", "openapi3.0.1", "-o", "o.json")

  test("on Windows a program is found on the PATH with its PATHEXT extension"):
    File.usingTemporaryDirectory("atom-helper") { dir =>
      val bin    = (dir / "bin").createDirectories()
      val script = (bin / "scalasem.cmd").write("@echo off")
      val env    = Map("Path" -> s"${dir / "missing"};${bin}", "PATHEXT" -> ".com;.exe;.cmd")
      HelperCommand.resolve("scalasem", env, "Windows 11") shouldBe script.pathAsString
      HelperCommand.resolve("atom-tools", env, "Windows 11") shouldBe "atom-tools"
      HelperCommand.resolve("scalasem", env, "Linux") shouldBe "scalasem"
    }

  test("on Windows a batch script is not started with characters cmd.exe interprets"):
    File.usingTemporaryDirectory("atom-helper") { dir =>
      val bin    = (dir / "bin").createDirectories()
      val script = (bin / "scalasem.cmd").write("@echo off")
      val exe    = (bin / "atom-tools.exe").write("")
      val env    = Map("PATH" -> bin.pathAsString, "PATHEXT" -> ".exe;.cmd")
      Seq("C:\\scan\\%USERPROFILE%", "C:\\scan\\a&b", "C:\\scan\\\"x\"", "C:\\a|b").foreach {
          arg =>
              HelperCommand.command("scalasem", Seq("C:\\scan", arg), env, "Windows 11").isLeft shouldBe
                  true
      }
      HelperCommand.command(
        "scalasem",
        Seq("C:\\scan dir", "C:\\out\\s.json"),
        env,
        "Windows 11"
      ) shouldBe
          Right(Seq(script.pathAsString, "C:\\scan dir", "C:\\out\\s.json"))
      HelperCommand.command("atom-tools", Seq("C:\\scan\\a&b"), env, "Windows 11") shouldBe
          Right(Seq(exe.pathAsString, "C:\\scan\\a&b"))
    }
end HelperCommandTests
