package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets

/** Python 3.15 sources end to end through `atom parsedeps`, `usages` and `reachables`: PEP 810 lazy
  * imports, PEP 798 unpacking comprehensions, nested format-spec fields, PEP 263 source encodings
  * and identifiers CPython NFKC-normalises (PEP 3131). Each project file is valid CPython 3.15.
  */
class Python315WorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  // lazy: the slices below are computed when their tests run, after registration
  private lazy val workspace: BFile = BFile.newTemporaryDirectory("atomPy315")

  override def afterAll(): Unit =
      workspace.delete(swallowIOExceptions = true)

  // eval in fullwidth letters (U+FF45 U+FF56 U+FF41 U+FF4C): CPython calls eval
  private val EvalFullwidth = "\uff45\uff56\uff41\uff4c"

  private def writeProject(dir: BFile): Unit =
    dir.createDirectories()
    (dir / "app.py").write(
      """import subprocess
        |lazy import json
        |lazy from yaml import safe_load
        |__lazy_modules__ = ["requests"]
        |import requests
        |from flask import request
        |
        |def flatten():
        |    name = request.args["name"]
        |    parts = [*p for p in [[name], ["-v"]]]
        |    return subprocess.getoutput(parts[0])
        |
        |def padded():
        |    width = request.args["width"]
        |    return subprocess.getoutput(f"{'ls':>{width}}")
        |
        |def merged():
        |    opts = {**o for o in [request.args]}
        |    return subprocess.getoutput(opts["cmd"])
        |""".stripMargin
    )
    (dir / "obfuscated.py").write(
      s"""from flask import request
         |
         |def run():
         |    return $EvalFullwidth(request.args["q"])
         |""".stripMargin
    )
    // cafe with an e-acute (0xE9) in Latin-1, as its coding cookie declares
    (dir / "legacy.py").writeByteArray(
      "# -*- coding: latin-1 -*-\ndef caf\u00e9():\n    return json_loads\n"
          .getBytes(StandardCharsets.ISO_8859_1)
    )
  end writeProject

  private def run(command: String, name: String): BFile =
    val projectDir = workspace / name
    writeProject(projectDir)
    val sliceFile = workspace / s"$name-$command.json"
    val args = Seq(
      command,
      "--cache",
      "none",
      "-l",
      "python",
      "-o",
      (workspace / s"$name-$command.atom").pathAsString,
      "-s",
      sliceFile.pathAsString,
      projectDir.pathAsString
    )
    Atom.run(args.toArray).isRight shouldBe true
    sliceFile

  "parsedeps" should {
      "list lazily imported modules like any other import" in {
          val modules = ujson.read(run("parsedeps", "deps").contentAsString)("modules").arr
              .map(_("name").str).toSet
          (modules should contain).allOf("json", "yaml", "requests", "flask", "subprocess")
      }
  }

  "usages" should {
      lazy val slice = ujson.read(run("usages", "usages").contentAsString)

      "report a name spelled with NFKC-folded characters as a source-integrity finding" in {
          val findings = slice.obj.get("sourceIntegrity").map(_.arr.toSeq).getOrElse(Nil)
          findings.map(f =>
              (f("kind").str, f("fileName").str, f("name").str, f("detail").str)
          ) should
              contain(("unicode-confusable", "obfuscated.py", "eval", EvalFullwidth))
      }

      "decode a source file by its coding cookie" in {
          slice.toString should include("caf\u00e9")
      }
  }

  "reachables" should {
      lazy val flows =
        val first = run("reachables", "reach")
        workspace.glob(s"${first.nameWithoutExtension}*.json").toSeq.sortBy(_.name)
            .flatMap(f => ujson.read(f.contentAsString).arr.toSeq)

      def methodsOf(entry: ujson.Value): Set[String] =
          entry.obj.get("flows").map(_.arr.toSeq).getOrElse(Nil)
              .flatMap(_.obj.get("parentMethodName").map(_.str)).toSet

      "carry request data through unpacking comprehensions and nested format specs" in {
          val reached = flows.flatMap(methodsOf).toSet
          (reached should contain).allOf("flatten", "padded", "merged")
      }

      "reach eval when the file spells it in fullwidth letters" in {
          flows.flatMap(methodsOf) should contain("run")
      }
  }
end Python315WorkflowTests
