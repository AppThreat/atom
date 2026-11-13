package io.appthreat.atom

import better.files.File
import io.appthreat.atom.frontends.FrontendArgsApplier
import io.circe.parser
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** Unit tests for [[AtomConfigLoader]]: JSON discovery, frontend-arg merging and the CLI-over-file
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

  /** As [[configAt]], with `--project-config` so the files in `input` are read. */
  private def projectConfigAt(input: File, language: String): DefaultAtomConfig =
    val c = configAt(input, language)
    c.withProjectConfig(true)
    c

  test("no config file is a no-op"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val config = configAt(dir, "java")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs shouldBe Map.empty
    }

  test("an explicit missing config file is reported as an error"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val missing = dir / "nope.json"
      val config  = configAt(dir, "java")
      config.withConfigFile(Some(missing))
      AtomConfigLoader(config).isLeft shouldBe true
    }

  test("atom.json in the input root is read with --project-config"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write(
        """{
            |  "frontend": {
            |    "no-dummyTypes": true,
            |    "java": {
            |      "delombok-mode": "run-delombok"
            |    }
            |  }
            |}""".stripMargin
      )
      val config = projectConfigAt(dir, "java")
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
      (dir / "atom.json").write(
        """{ "frontend": { "no-dummyTypes": true, "type-prop-iterations": 9 } }""".stripMargin
      )
      val config = projectConfigAt(dir, "python")
      config.withFrontendArg("no-dummyTypes", "false")
      AtomConfigLoader(config) shouldBe Right(config)
      // CLI value preserved.
      config.frontendArgs("no-dummyTypes") shouldBe "false"
      // File-only key still flows through.
      config.frontendArgs("type-prop-iterations") shouldBe "9"
    }

  test("arrays in the file are flattened to CSV frontend args"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write(
        """{ "frontend": { "exclude": ["target/", "node_modules/"] } }""".stripMargin
      )
      val config = projectConfigAt(dir, "js")
      AtomConfigLoader(config) shouldBe Right(config)
      val exclude = config.frontendArgs("exclude").split(",").map(_.trim).toSet
      exclude shouldBe Set("target/", "node_modules/")
    }

  test("universal and per-language sections both apply, per-language wins on conflict"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write(
        """{
            |  "frontend": {
            |    "type-prop-iterations": 2,
            |    "python": { "type-prop-iterations": 4 }
            |  }
            |}""".stripMargin
      )
      val config = projectConfigAt(dir, "python")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("type-prop-iterations") shouldBe "4"
    }

  test("the file under .atom/ is read with --project-config when no root file exists"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      val dotAtom = dir / ".atom"
      dotAtom.createDirectories()
      (dotAtom / "config.json").write(
        """{ "frontend": { "no-dummyTypes": true } }""".stripMargin
      )
      val config = projectConfigAt(dir, "ruby")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("no-dummyTypes") shouldBe "true"
    }

  test("frontendArgsFromConfig is usable directly without a full config"):
    val json = parser.parse(
      """{
          |  "frontend": {
          |    "exclude": "build",
          |    "java": { "jdk-path": "/opt/jdk" }
          |  }
          |}""".stripMargin
    ).toOption.get
    val args = AtomConfigLoader.frontendArgsFromConfig(json, "java")
    args("exclude") shouldBe "build"
    args("jdk-path") shouldBe "/opt/jdk"

  test("merged file args flow through the applier end to end"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write(
        """{ "frontend": { "cpp-standard": "c++20", "defines": ["A", "B"] } }""".stripMargin
      )
      val config = projectConfigAt(dir, "cpp")
      AtomConfigLoader(config) shouldBe Right(config)
      val cConfig = io.appthreat.c2cpg.Config().withInputPath(dir.pathAsString)
      val applied = FrontendArgsApplier.applyC(cConfig, config.frontendArgs)
      applied.cppStandard shouldBe "c++20"
      applied.defines shouldBe Set("A", "B")
    }

  test("files in the input directory are not read without --project-config"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write(
        """{ "frontend": { "js": { "astgen-out": "out; touch marker" } } }"""
      )
      val dotAtom = (dir / ".atom").createDirectories()
      (dotAtom / "config.json").write("""{ "frontend": { "exclude-regex": "x';touch y;'" } }""")
      (dotAtom / "atom.json").write("""{ "frontend": { "fetch-dependencies": true } }""")
      val config = configAt(dir, "js")
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs shouldBe Map.empty
      AtomConfigLoader.discover(
        dir.pathAsString,
        None,
        env = Map.empty,
        home = dir / "home"
      ) shouldBe None
    }

  test("an explicit config wins over a project file, even with --project-config"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write("""{ "frontend": { "astgen-out": "from-project" } }""")
      val explicit = dir / "explicit.json"
      explicit.write("{}")
      val config = projectConfigAt(dir, "js")
      config.withConfigFile(Some(explicit))
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs shouldBe Map.empty
    }

  test("an explicit config of {} or an empty file sets nothing"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
        Seq("{}", "", "  \n").zipWithIndex.foreach { case (content, i) =>
            val file = dir / s"empty-$i.json"
            file.write(content)
            val config = configAt(dir, "java")
            config.withConfigFile(Some(file))
            AtomConfigLoader(config) shouldBe Right(config)
            config.frontendArgs shouldBe Map.empty
        }
    }

  test("ATOM_CONFIG_FILE and the user config are still found without --project-config"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      (dir / "atom.json").write("{}")
      val fromEnv = dir / "env.json"
      fromEnv.write("{}")
      val home   = dir / "home"
      val global = (home / ".config" / "atom").createDirectories() / "config.json"
      global.write("{}")
      AtomConfigLoader.discover(
        dir.pathAsString,
        None,
        env = Map("ATOM_CONFIG_FILE" -> fromEnv.pathAsString),
        home = home
      ) shouldBe Some(fromEnv)
      AtomConfigLoader.discover(dir.pathAsString, None, env = Map.empty, home = home) shouldBe
          Some(global)
      AtomConfigLoader.discover(
        dir.pathAsString,
        None,
        includeProject = true,
        env = Map.empty,
        home = home
      ) shouldBe Some(dir / "atom.json")
    }

  test("--project-config is parsed from the command line"):
    File.usingTemporaryDirectory("atom-loader") { dir =>
      Atom.parseConfig(List("-l", "js", dir.pathAsString)) match
        case Right(c: AtomConfig) => c.projectConfig shouldBe false
        case other                => fail(s"Unexpected config result: $other")
      Atom.parseConfig(List("-l", "js", "--project-config", dir.pathAsString)) match
        case Right(c: AtomConfig) => c.projectConfig shouldBe true
        case other                => fail(s"Unexpected config result: $other")
    }
end AtomConfigLoaderTests
