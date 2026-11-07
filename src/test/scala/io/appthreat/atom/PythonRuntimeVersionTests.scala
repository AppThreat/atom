package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.atom.parsedeps.PythonDependencyParser
import io.appthreat.pysrc2cpg.PySrc2CpgFixture
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import java.nio.file.Files

/** The parsedeps output's `pythonVersion` field: the runtime the project declares, read from
  * pyproject.toml, setup.cfg, setup.py, .python-version, runtime.txt and a root Dockerfile, and
  * reported as written.
  *
  * The manifest files are not Python, so the unit fixture (which deletes the project it ingested
  * once the graph is built) can only exercise the setup.py keyword, which the graph itself carries;
  * the file-based sources run through the CLI workflow below.
  */
class PythonRuntimeVersionTests extends PySrc2CpgFixture(withOssDataflow = false) with Matchers:

  "parsedeps pythonVersion (graph-read)" should:

    "read python_requires from setup.py" in:
      val cpg = code(
        """|from setuptools import setup
               |setup(name="demo", python_requires=">=3.8")
               |""".stripMargin,
        "setup.py"
      )
      PythonDependencyParser.parse(cpg).pythonVersion shouldBe Some(">=3.8")

    "leave the field out when nothing declares a runtime" in:
      val cpg = code("""import requests""", "app.py")
      PythonDependencyParser.parse(cpg).pythonVersion shouldBe None
      ujson.read(PythonDependencyParser.parse(cpg).toJson).obj.contains("pythonVersion") shouldBe
          false

    "emit pythonVersion next to modules without changing the modules shape" in:
      val cpg = code(
        """|from setuptools import setup
               |setup(name="demo", python_requires=">=3.8", install_requires=["requests"])
               |""".stripMargin,
        "setup.py"
      )
      val json = ujson.read(PythonDependencyParser.parse(cpg).toJson)
      json("modules").arr.map(_("name").str) should contain("requests")
      json("pythonVersion").str shouldBe ">=3.8"
end PythonRuntimeVersionTests

/** The file-based runtime declarations, through the CLI: the manifests are project files, not graph
  * nodes, so they are read from the analysed project root.
  */
class PythonRuntimeFileTests extends AnyWordSpec with Matchers:

  private lazy val workspace = BFile(Files.createTempDirectory("atom-runtime-tests"))

  private def runParsedeps(name: String, files: (String, String)*): ujson.Value =
    val projectDir = workspace / name
    projectDir.createDirectoryIfNotExists(createParents = true)
    files.foreach { case (fileName, content) =>
        (projectDir / fileName).write(content)
    }
    (projectDir / "app.py").write("import requests\n")
    val sliceFile = workspace / s"$name-parsedeps.json"
    Atom.run(
      Seq(
        "parsedeps",
        "--cache",
        "none",
        "-l",
        "python",
        "-o",
        (workspace / s"$name.atom").pathAsString,
        "-s",
        sliceFile.pathAsString,
        projectDir.pathAsString
      ).toArray
    ).isRight shouldBe true
    ujson.read(sliceFile.contentAsString)
  end runParsedeps

  "parsedeps pythonVersion (project files)" should:

    "read requires-python from pyproject.toml" in:
      runParsedeps(
        "pyproject",
        "pyproject.toml" -> "[project]\nname = \"demo\"\nrequires-python = \">=3.11\"\n"
      )("pythonVersion").str shouldBe ">=3.11"

    "read python_requires from setup.cfg, which wins over setup.py" in:
      runParsedeps(
        "setupcfg",
        "setup.py" -> "from setuptools import setup\nsetup(name=\"d\", python_requires=\">=3.8\")\n",
        "setup.cfg" -> "[options]\npython_requires = >=3.7\n"
      )("pythonVersion").str shouldBe ">=3.7"

    "read .python-version" in:
      runParsedeps("pyversion", ".python-version" -> "3.9.7\n")("pythonVersion").str shouldBe
          "3.9.7"

    "read runtime.txt without its python- prefix" in:
      val declared = runParsedeps("runtimetxt", "runtime.txt" -> "python-3.10.4\n")
      declared("pythonVersion").str shouldBe "3.10.4"

    "read the Dockerfile base image" in:
      runParsedeps(
        "dockerfile",
        "Dockerfile" -> "FROM python:3.12-slim\nWORKDIR /app\n"
      )("pythonVersion").str shouldBe "3.12-slim"

    "read a Dockerfile base image with a platform flag and a registry prefix" in:
      runParsedeps(
        "dockerfile-registry",
        "Dockerfile" -> "FROM --platform=linux/amd64 docker.io/library/python:3.11 AS build\n"
      )("pythonVersion").str shouldBe "3.11"

    "read Poetry's python dependency" in:
      runParsedeps(
        "poetry",
        "pyproject.toml" ->
            "[tool.poetry]\nname = \"demo\"\n\n[tool.poetry.dependencies]\npython = \"^3.10\"\nrequests = \"*\"\n"
      )("pythonVersion").str shouldBe "^3.10"
end PythonRuntimeFileTests
