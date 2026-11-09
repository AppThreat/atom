package io.appthreat.atom

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.{Files, Path}
import java.nio.file.attribute.PosixFilePermissions
import scala.util.Properties

/** atom-parsetools and parsa are found in the npm layouts astgen is installed in. */
class ToolVersionsTests extends AnyFunSuite with Matchers:

  private def withDir[T](body: Path => T): T =
    val dir = Files.createTempDirectory("atom-versions-")
    try body(dir)
    finally
        Files.walk(dir).sorted(java.util.Comparator.reverseOrder()).forEach(p =>
            Files.deleteIfExists(p)
        )

  private def write(p: Path, text: String): Path =
    Files.createDirectories(p.getParent)
    Files.writeString(p, text)

  private def parsetools(dir: Path, version: String): Path =
    write(
      dir.resolve("package.json"),
      s"""{"name":"@appthreat/atom-parsetools","version":"$version"}"""
    )
    write(dir.resolve("astgen.js"), "")
    dir

  /** A stand-in parsa that prints what the real one does. */
  private def fakeParsa(bin: Path, version: String): Path =
    write(bin, s"#!/bin/sh\necho '$version (TypeScript 622d4f7)'\n")
    Files.setPosixFilePermissions(bin, PosixFilePermissions.fromString("rwxr-xr-x"))
    bin

  private def env(path: Path*): Map[String, String] =
      Map("PATH" -> path.mkString(java.io.File.pathSeparator))

  test("a global npm install on Linux: astgen links into the package, parsa is nested in it"):
    assume(!Properties.isWin)
    withDir { root =>
      val pkg = parsetools(root.resolve("lib/node_modules/@appthreat/atom-parsetools"), "2.0.0")
      val bin = Files.createDirectories(root.resolve("bin"))
      Files.createSymbolicLink(bin.resolve("astgen"), pkg.resolve("astgen.js"))
      val parsa =
          fakeParsa(pkg.resolve("node_modules/@appthreat/parsa-linux-x64/bin/parsa"), "2.0.0")
      val v = ToolVersions.current(env(bin), "Linux", "amd64")
      v.atomParsetools.map(_.version) shouldBe Some("2.0.0")
      v.parsa.map(_.version) shouldBe Some("2.0.0 (TypeScript 622d4f7)")
      v.parsa.flatMap(_.path).map(_.toRealPath()) shouldBe Some(parsa.toRealPath())
    }

  test("a local install: parsa is hoisted beside atom-parsetools"):
    assume(!Properties.isWin)
    withDir { root =>
      val nm  = root.resolve("node_modules")
      val pkg = parsetools(nm.resolve("@appthreat/atom-parsetools"), "2.0.0-rc.1")
      val bin = Files.createDirectories(nm.resolve(".bin"))
      Files.createSymbolicLink(bin.resolve("astgen"), pkg.resolve("astgen.js"))
      fakeParsa(nm.resolve("@appthreat/parsa-darwin-arm64/bin/parsa"), "2.0.0-rc.1")
      val v = ToolVersions.current(env(bin), "Mac OS X", "aarch64")
      v.parsa.map(_.version) shouldBe Some("2.0.0-rc.1 (TypeScript 622d4f7)")
    }

  test("atom-parsetools 1.x has no parsa, and ASTGEN_NATIVE_BIN names one outside the package"):
    assume(!Properties.isWin)
    withDir { root =>
      val pkg = parsetools(root.resolve("pt"), "1.10.6")
      val bin = Files.createDirectories(root.resolve("bin"))
      Files.createSymbolicLink(bin.resolve("astgen"), pkg.resolve("astgen.js"))
      val v = ToolVersions.current(env(bin), "Linux", "aarch64")
      v.atomParsetools.map(_.version) shouldBe Some("1.10.6")
      v.parsa shouldBe None
      v.lines.last should startWith("parsa not found")
      val own = fakeParsa(root.resolve("own/parsa"), "dev")
      ToolVersions.current(env(bin) + ("ASTGEN_NATIVE_BIN" -> own.toString), "Linux", "aarch64")
          .parsa.flatMap(_.path) shouldBe Some(own)
    }

  test("npm on Windows: a .cmd shim beside node_modules"):
    withDir { root =>
      val pkg  = parsetools(root.resolve("node_modules/@appthreat/atom-parsetools"), "2.0.0")
      val shim = write(root.resolve("astgen.cmd"), "@node astgen.js %*")
      ToolVersions.findAstgen(env(root), windows = true) shouldBe Some(shim)
      ToolVersions.parsetoolsDir(shim) shouldBe Some(pkg)
      val exe = write(pkg.resolve("node_modules/@appthreat/parsa-win32-arm64/bin/parsa.exe"), "")
      ToolVersions.findParsa(Map.empty, Some(pkg), "Windows 11", "aarch64") shouldBe Some(exe)
    }

  test("no astgen on the PATH"):
    withDir { root =>
      val v = ToolVersions.current(env(root), "Linux", "amd64")
      v.atomParsetools shouldBe None
      v.lines(2) should startWith("atom-parsetools not found")
    }

  test("platform package names match astgen's"):
    ToolVersions.parsaPackage("Linux", "amd64") shouldBe Some("parsa-linux-x64")
    ToolVersions.parsaPackage("Mac OS X", "aarch64") shouldBe Some("parsa-darwin-arm64")
    ToolVersions.parsaPackage("Windows 11", "amd64") shouldBe Some("parsa-win32-x64")
    ToolVersions.parsaPackage("Linux", "riscv64") shouldBe None

  test("the perf report gets one versions record after its header"):
    withDir { root =>
      val report =
          write(root.resolve("perf.ndjson"), """{"stage":"__meta__","phase":"meta"}""" + "\n")
      val v = ToolVersions.Versions("5.0.0", "4.0.0", Some(ToolVersions.Found("2.0.0", None)), None)
      System.setProperty("chen.perf.report", report.toString)
      try ToolVersions.appendToPerfReport(v)
      finally System.clearProperty("chen.perf.report")
      val lines = Files.readAllLines(report)
      lines.size shouldBe 2
      lines.get(1) shouldBe
          """{"stage":"__versions__","phase":"meta","atom":"5.0.0","chen":"4.0.0","atomParsetools":"2.0.0","parsa":null}"""
    }

  test("atom --version prints the four versions without needing an input"):
    Atom.run(Array("--version")) shouldBe Right("Displayed versions")
end ToolVersionsTests
