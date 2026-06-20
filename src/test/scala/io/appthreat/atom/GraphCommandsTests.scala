package io.appthreat.atom

import better.files.File
import io.appthreat.pysrc2cpg.PySrc2CpgFixture

/** Exercises the graph-level export and algorithms commands end to end on a small built CPG. */
class GraphCommandsTests extends PySrc2CpgFixture(withOssDataflow = false):

  private val sampleCode =
      """
          |def helper(x):
          |    return x + 1
          |
          |def main():
          |    y = helper(5)
          |    helper(y)
          |    print(y)
          |
          |main()
          |""".stripMargin

  // Mutually recursive ping/pong so the call graph has a genuine cycle.
  private val recursiveCode =
      """
          |def helper(x):
          |    return x + 1
          |
          |def ping(n):
          |    if n <= 0:
          |        return 0
          |    return pong(n - 1)
          |
          |def pong(n):
          |    return ping(n - 1)
          |
          |def main():
          |    y = helper(5)
          |    ping(y)
          |    print(y)
          |
          |main()
          |""".stripMargin

  "atom export" should {

      "write a whole-graph graphml file" in {
          val cpg = code(sampleCode)
          File.usingTemporaryDirectory("atom-export") { dir =>
            val config = AtomExportConfig()
                .withExportFormat("graphml")
                .asInstanceOf[AtomExportConfig]
                .withScope("whole")
                .withExportDir(dir.pathAsString)
                .asInstanceOf[AtomExportConfig]
            GraphCommands.runExport(cpg, config) shouldBe Right("Atom exported successfully")
            dir.list.exists(_.extension.contains(".xml")) shouldBe true
          }
      }

      "write one file per method for the methods scope" in {
          val cpg = code(sampleCode)
          File.usingTemporaryDirectory("atom-export-methods") { dir =>
            val config = AtomExportConfig()
                .withExportFormat("dot")
                .asInstanceOf[AtomExportConfig]
                .withScope("methods")
                .withExportDir(dir.pathAsString)
                .asInstanceOf[AtomExportConfig]
            GraphCommands.runExport(cpg, config) shouldBe Right("Atom exported successfully")
            dir.list.count(_.extension.contains(".dot")) should be > 1
          }
      }

      "reject an unknown format" in {
          val cpg    = code(sampleCode)
          val config = AtomExportConfig().withExportFormat("bogus").asInstanceOf[AtomExportConfig]
          GraphCommands.runExport(cpg, config).isLeft shouldBe true
      }
  }

  "atom algorithms" should {

      "rank methods by centrality" in {
          val cpg = code(sampleCode)
          File.usingTemporaryDirectory("atom-algo") { dir =>
            val out    = dir / "centrality.json"
            val config = AtomAlgorithmsConfig().withAlgoType("centrality")
            config.withOutputSliceFile(out)
            GraphCommands.runAlgorithms(cpg, config).isRight shouldBe true
            out.contentAsString should include("\"ranking\"")
            out.contentAsString should include("pageRank")
          }
      }

      "report strongly connected components" in {
          val cpg = code(sampleCode)
          File.usingTemporaryDirectory("atom-algo-scc") { dir =>
            val out    = dir / "scc.json"
            val config = AtomAlgorithmsConfig().withAlgoType("scc")
            config.withOutputSliceFile(out)
            GraphCommands.runAlgorithms(cpg, config).isRight shouldBe true
            out.contentAsString should include("componentCount")
          }
      }

      "order methods without erroring on a recursive call graph" in {
          val cpg = code(recursiveCode)
          File.usingTemporaryDirectory("atom-algo-topo") { dir =>
            val out    = dir / "toposort.json"
            val config = AtomAlgorithmsConfig().withAlgoType("toposort")
            config.withOutputSliceFile(out)
            GraphCommands.runAlgorithms(cpg, config).isRight shouldBe true
            val content = out.contentAsString
            content should include("\"order\"")
            // ping/pong form a recursive stage, so at least one stage is flagged recursive.
            content should include("\"recursive\" : true")
          }
      }

      "find paths driven by a committed config file" in {
          val cpg     = code(sampleCode)
          val fixture = File("test/fixtures/algorithms-paths.json")
          File.usingTemporaryDirectory("atom-algo-paths") { dir =>
            val out    = dir / "paths.json"
            val config = AtomAlgorithmsConfig()
            config.withOutputSliceFile(out)
            config.withConfigFile(Some(fixture))
            AtomConfigFile(config) match
              case Right(c: AtomAlgorithmsConfig) =>
                  c.algoType shouldBe "paths"
                  GraphCommands.runAlgorithms(cpg, c).isRight shouldBe true
                  out.contentAsString should include("\"paths\"")
              case other => fail(s"Unexpected config result: $other")
          }
      }

      "reject an unknown algorithm" in {
          val cpg    = code(sampleCode)
          val config = AtomAlgorithmsConfig().withAlgoType("bogus")
          GraphCommands.runAlgorithms(cpg, config).isLeft shouldBe true
      }
  }
end GraphCommandsTests
