package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** `--bom-file` and `--bom-dir`: the BOM a reachables run takes purls from, given from outside the
  * project. A given BOM replaces any the project directory holds, so a stale `bom.json` there
  * contributes nothing. The JavaScript frontend needs the external `astgen` generator, so the suite
  * cancels (not fails) without one, like [[JsCallSitesTests]].
  */
class BomOptionsTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit = workspace = BFile.newTemporaryDirectory("atomBomOptions")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private lazy val astgenUsable: Boolean =
      Try(scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  private def requireAstgen(): Unit =
      if !astgenUsable then
        cancel(
          "astgen unavailable or too old; JavaScript fixture needs the astgen from @appthreat/atom-parsetools on PATH"
        )

  private def bom(purls: String*): String =
      purls.map(p => s"""{ "type": "library", "purl": "$p" }""")
          .mkString("""{ "bomFormat": "CycloneDX", "components": [""", ", ", "] }")

  /** An express app whose request reaches `axios.get`, with a stale `bom.json` of its own. */
  private def project(name: String): BFile =
    val dir = (workspace / name).createDirectories()
    (dir / "server.ts").overwrite(
      """import express from 'express';
        |import axios from 'axios';
        |const app = express();
        |app.get('/proxy', async (req, res) => {
        |  const r = await axios.get(req.query.url as string);
        |  res.send(r.data);
        |});
        |app.listen(3000);
        |""".stripMargin
    )
    (dir / "package.json").overwrite(
      """{ "name": "p", "dependencies": { "express": "^4.21.0", "axios": "^1.7.0" } }"""
    )
    (dir / "bom.json").overwrite(bom("pkg:npm/axios@0.0.1-stale", "pkg:npm/express@0.0.1-stale"))
    dir

  private def reachables(dir: BFile, bomArgs: String*): Either[String, Set[String]] =
    val slices = workspace / s"${dir.name}-reachables.json"
    Atom.run(
      (Seq(
        "reachables",
        "--cache",
        "none",
        "-l",
        "js",
        "-o",
        (workspace / s"${dir.name}.atom")
            .pathAsString,
        "-s",
        slices.pathAsString
      ) ++ bomArgs :+ dir.pathAsString).toArray
    ).map(_ =>
        ujson.read(slices.contentAsString).arr.flatMap(f =>
            f.obj.get("purls").toSeq.flatMap(_.arr.map(_.str))
        ).toSet
    )
  end reachables

  "atom reachables" should {

      "take purls from the BOM given with --bom-file, not the project's own" in {
          requireAstgen()
          val dir = project("bom-file")
          val sbom = (workspace / "reports" / "sbom-js.cdx.json").createIfNotExists(createParents =
              true
          ).overwrite(bom("pkg:npm/axios@1.7.9", "pkg:npm/express@4.21.2"))
          val purls = reachables(dir, "--bom-file", sbom.pathAsString)
          purls.isRight shouldBe true
          purls.toOption.get should contain("pkg:npm/axios@1.7.9")
          purls.toOption.get.filter(_.contains("stale")) shouldBe empty
      }

      "take purls from every BOM and VDR under --bom-dir" in {
          requireAstgen()
          val dir     = project("bom-dir")
          val reports = (workspace / "dir-reports").createDirectories()
          (reports / "depscan-js.vdr.json").overwrite(bom("pkg:npm/axios@1.7.9"))
          (reports / "depscan-risk-js.json").overwrite(bom("pkg:npm/axios@9.9.9-not-a-bom"))
          val purls = reachables(dir, "--bom-dir", reports.pathAsString)
          purls.isRight shouldBe true
          purls.toOption.get should contain("pkg:npm/axios@1.7.9")
          purls.toOption.get.filter(p => p.contains("stale") || p.contains("not-a-bom")) shouldBe
              empty
      }

      "refuse --bom-file with --bom-dir, as depscan does" in {
          val dir  = project("both")
          val file = (workspace / "both.cdx.json").overwrite(bom("pkg:npm/axios@1.7.9"))
          val result =
              reachables(dir, "--bom-file", file.pathAsString, "--bom-dir", workspace.pathAsString)
          result.left.toOption.get should include("mutually exclusive")
      }

      "refuse a --bom-dir that holds no BOM" in {
          val dir   = project("empty-dir")
          val empty = (workspace / "no-boms").createDirectories()
          reachables(dir, "--bom-dir", empty.pathAsString).left.toOption.get should include(
            "No BOM"
          )
      }
  }
end BomOptionsTests
