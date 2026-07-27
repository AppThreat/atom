package io.appthreat.atom

import better.files.File
import io.appthreat.atom.frontends.FrontendArgsApplier
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** Unit tests for [[AtomConfigLoader]]: HOCON discovery, frontend-arg merging and the CLI-over-file
  * precedence. Each test writes a throwaway config file under a temporary directory.
  */
class AtomConfigLoaderTests extends AnyFunSuite with Matchers:

  /** Builds a `DefaultAtomConfig` rooted at `input` for the given language. The inherited setters
    * return the base type, so they are applied as mutating statements.
    */
  private def configAt(input: File, language: String): DefaultAtomConfig =
    val c = DefaultAtomConfig()
    c.withInputPath(input)
    c.withLanguage(language)
    c

  test("no config file is a no-op"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val config = configAt(dir, "java")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs shouldBe Map.empty
    }

  test("an explicit missing config file is reported as an error"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val missing = dir / "nope.conf"
      val config  = configAt(dir, "java")
      config.withConfigFile(Some(missing))
      AtomConfigLoader(config).isLeft shouldBe true
    }

  test("atom.conf is auto-discovered in the input root"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.conf").write(
        """frontend {
            |  no-dummyTypes = true
            |  java {
            |    delombok-mode = "run-delombok"
            |  }
            |}
            |""".stripMargin
      )
      val config = configAt(dir, "java")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("no-dummyTypes") shouldBe "true"
      config.frontendArgs("delombok-mode") shouldBe "run-delombok"
    }

  test("flat JSON config is parsed for the algorithms command"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val conf = dir / "paths.json"
      conf.write(
        """{"type":"paths","source":".*main$","target":".*helper$","maxDepth":20}""".stripMargin
      )
      val config = AtomAlgorithmsConfig()
      config.withInputPath(dir)
      config.withConfigFile(Some(conf))
      AtomConfigLoader(config) shouldBe Right(config)
      config.algoType shouldBe "paths"
      config.sourceSelector shouldBe Some(".*main$")
      config.targetSelector shouldBe Some(".*helper$")
      config.maxDepth shouldBe 20
    }

  test("CLI-supplied frontend args win over the file"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.conf").write(
        """frontend { no-dummyTypes = true, type-prop-iterations = 9 }""".stripMargin
      )
      val config = configAt(dir, "python")
      config.withFrontendArg("no-dummyTypes", "false")
      AtomConfigLoader(config) shouldBe Right(config)
      // CLI value preserved.
      config.frontendArgs("no-dummyTypes") shouldBe "false"
      // File-only key still flows through.
      config.frontendArgs("type-prop-iterations") shouldBe "9"
    }

  test("lists in HOCON are flattened to CSV frontend args"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.conf").write(
        """frontend { exclude = ["target/", "node_modules/"] }""".stripMargin
      )
      val config = configAt(dir, "js")
      AtomConfigLoader(config) shouldBe Right(config)
      val exclude = config.frontendArgs("exclude").split(",").map(_.trim).toSet
      exclude shouldBe Set("target/", "node_modules/")
    }

  test("universal and per-language sections both apply, per-language wins on conflict"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.conf").write(
        """frontend {
            |  type-prop-iterations = 2
            |  python { type-prop-iterations = 4 }
            |}""".stripMargin
      )
      val config = configAt(dir, "python")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("type-prop-iterations") shouldBe "4"
    }

  test("the discovered file under .atom/ is used when no root file exists"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val dotAtom = dir / ".atom"
      dotAtom.createDirectories()
      (dotAtom / "config.conf").write(
        """frontend { no-dummyTypes = true }""".stripMargin
      )
      val config = configAt(dir, "ruby")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("no-dummyTypes") shouldBe "true"
    }

  test("frontendArgsFromConfig is usable directly without a full config"):
    val hocon = com.typesafe.config.ConfigFactory.parseString(
      """frontend {
          |  exclude = "build"
          |  java { jdk-path = "/opt/jdk" }
          |}""".stripMargin
    )
    val args = AtomConfigLoader.frontendArgsFromConfig(hocon, "java")
    args("exclude") shouldBe "build"
    args("jdk-path") shouldBe "/opt/jdk"

  test("merged file args flow through the applier end to end"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.conf").write(
        """frontend { cpp-standard = "c++20", defines = ["A", "B"] }""".stripMargin
      )
      val config = configAt(dir, "cpp")
      AtomConfigLoader(config) shouldBe Right(config)
      val cConfig = io.appthreat.c2cpg.Config().withInputPath(dir.pathAsString)
      val applied = FrontendArgsApplier.applyC(cConfig, config.frontendArgs)
      applied.cppStandard shouldBe "c++20"
      applied.defines shouldBe Set("A", "B")
    }
end AtomConfigLoaderTests
