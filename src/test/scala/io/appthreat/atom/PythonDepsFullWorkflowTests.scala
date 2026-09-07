package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** `python-deps=full` end to end through `atom reachables`: the dependency tree enters the graph
  * with bodies, and the OUTPUT stays scoped to the project's own code.
  *
  * The venv here is deliberately hand-assembled - it exercises the pipeline, not pip.
  *
  * The scoping rule under test: a flow whose every element lives in dependency code is a fact about
  * the library, not a finding about the analyzed project, and must not be reported - while a flow
  * that merely TRAVERSES the library (project source -> library -> project sink) must survive,
  * because traversal is the entire point of the mode.
  */
class PythonDepsFullWorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomPyDepsFull")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def writeProject(dir: BFile): Unit =
    (dir / "server.py").write(
      """import subprocess
          |from carrier import carry
          |from flask import request
          |
          |def relay():
          |    name = request.args["name"]
          |    return subprocess.getoutput(carry(name))
          |""".stripMargin
    )
    val sp = dir / ".venv/lib/python3.13/site-packages"
    sp.createDirectories()
    // A dependency with a REAL body: `carry` really returns its argument, so a project flow
    // through it survives because the engine descends into these statements.
    (sp / "carrier.py").write(
      """def carry(value):
          |    return value
          |""".stripMargin
    )
    // Package identity for the dependency.
    (sp / "carrier-1.0.0.dist-info").createDirectories()
    (sp / "carrier-1.0.0.dist-info/top_level.txt").write("carrier\n")
    // A library-INTERNAL source-to-sink chain: a flow that never touches the project. With
    // dependency bodies in the graph the collectors see it; the output must not contain it.
    (sp / "internalsink.py").write(
      """import subprocess
          |from flask import request
          |
          |def leak():
          |    name = request.args["name"]
          |    return subprocess.getoutput(name)
          |""".stripMargin
    )
  end writeProject

  private def runReachables(projectName: String, frontendArgs: Seq[String]): Seq[ujson.Value] =
    val projectDir = workspace / projectName
    projectDir.createDirectories()
    writeProject(projectDir)
    val atomFile  = workspace / s"$projectName.atom"
    val sliceFile = workspace / s"$projectName-slices.json"
    val args = Seq(
      "reachables",
      "--cache",
      "none",
      "-l",
      "python",
      "-o",
      atomFile.pathAsString,
      "-s",
      sliceFile.pathAsString
    ) ++ frontendArgs.flatMap(a => Seq("--frontend-args", a)) :+ projectDir.pathAsString
    val result = Atom.run(args.toArray)
    result.isRight shouldBe true
    val chunks = workspace.glob(s"$projectName-slices*.json").toSeq.sortBy(_.name)
    chunks should not be empty
    chunks.flatMap(f => ujson.read(f.contentAsString).arr.toSeq)
  end runReachables

  private def nodesOf(entry: ujson.Value): Seq[ujson.Value] =
      entry.obj.get("flows").map(_.arr.toSeq).getOrElse(Seq.empty)

  private def startsInDependency(entry: ujson.Value): Boolean =
      nodesOf(entry).headOption.exists { n =>
          n.obj.get("parentFileName").map(_.str).getOrElse("").contains("site-packages")
      }

  private def purlsOf(entry: ujson.Value): Set[String] =
      entry.obj.get("purls").map(_.arr.toSeq.map(_.str).toSet).getOrElse(Set.empty)

  "reachables with python-deps=full" should {

      "keep the output scoped to the project's own code" in {
          val flows = runReachables("pydeps-full", Seq("python-deps=full"))
          // The library-internal chain exists in the graph (the engine explores it), but no
          // REPORTED entry is SOURCED inside site-packages: a finding must be rooted in the
          // analyzed project.
          withClue(
            s"entries sourced in dependency code: ${flows.filter(startsInDependency).size}: "
          ):
            flows.filter(startsInDependency) shouldBe empty
          // And scoping did not delete the project's own reporting: at least one entry has a
          // node in the project's server.py.
          flows.exists(e =>
              nodesOf(e).exists(n =>
                  n.obj.get("parentFileName").map(_.str).getOrElse("") == "server.py"
              )
          ) shouldBe true
      }

      "attribute package identity on flows through the dependency" in {
          val flows = runReachables("pydeps-full-purl", Seq("python-deps=full"))
          // A project flow that traverses `carrier` names the distribution and version it
          // passed through - the purl tag the dependency ingestion attached from .dist-info.
          flows.flatMap(purlsOf) should contain("pkg:pypi/carrier@1.0.0")
      }

      "leave the default (python-deps=none) output unchanged in shape" in {
          val flows = runReachables("pydeps-none", Seq.empty)
          // Without dependency ingestion the venv is invisible: no site-packages nodes exist at
          // all, so nothing to scope and nothing to attribute.
          flows.flatMap(purlsOf) should not contain "pkg:pypi/carrier@1.0.0"
          flows.foreach { e =>
              nodesOf(e).foreach { n =>
                  (n.obj.get("parentFileName").map(_.str).getOrElse("") should not).include(
                    "site-packages"
                  )
              }
          }
      }
  }
end PythonDepsFullWorkflowTests
