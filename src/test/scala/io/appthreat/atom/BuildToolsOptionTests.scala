package io.appthreat.atom

import better.files.File
import io.appthreat.atom.frontends.FrontendArgsApplier
import io.appthreat.javasrc2cpg.Config as JavaConfig
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** `--no-build-tools` keeps the Java source frontend from fetching dependencies with Maven or
  * Gradle and from running delombok, whichever name `-l` uses for it and whatever the frontend
  * arguments ask for.
  */
class BuildToolsOptionTests extends AnyFunSuite with Matchers:

  private def parse(args: String*): AtomConfig =
      Atom.parseConfig(args.toList) match
        case Right(c: AtomConfig) => c
        case other                => fail(s"Unexpected config result: $other")

  test("build tools stay allowed unless --no-build-tools is given"):
    File.usingTemporaryDirectory("atom-build-tools") { dir =>
      parse("-l", "java", dir.pathAsString).buildTools shouldBe true
      parse("-l", "JAVASRC", "--no-build-tools", dir.pathAsString).buildTools shouldBe false
      parse("usages", "-l", "scala", "--no-build-tools", dir.pathAsString).buildTools shouldBe
          false
    }

  test("--no-build-tools turns off the dependency fetch and delombok the arguments ask for"):
    File.usingTemporaryDirectory("atom-build-tools") { dir =>
      val config = parse(
        "-l",
        "JAVASRC",
        "--fetch-deps",
        "--delombok-mode",
        "run-delombok",
        "--no-build-tools",
        dir.pathAsString
      )
      val applied = FrontendArgsApplier.applyJava(
        JavaConfig(fetchDependencies = true, delombokMode = Some("types-only")),
        config.frontendArgs
      )
      applied.fetchDependencies shouldBe true
      applied.delombokMode shouldBe Some("run-delombok")
      val gated = Atom.javaBuildToolsConfig(applied, config.buildTools)
      gated.fetchDependencies shouldBe false
      gated.delombokMode shouldBe Some("no-delombok")
    }

  test("--no-build-tools wins over fetch-dependencies from a config file"):
    File.usingTemporaryDirectory("atom-build-tools") { dir =>
      val file = dir / "atom-config.json"
      file.write("""{ "frontend": { "java": { "fetch-dependencies": true } } }""")
      val config =
          parse("-l", "java", "--config", file.pathAsString, "--no-build-tools", dir.pathAsString)
      AtomConfigLoader(config) shouldBe Right(config)
      config.frontendArgs("fetch-dependencies") shouldBe "true"
      val applied = FrontendArgsApplier.applyJava(JavaConfig(), config.frontendArgs)
      Atom.javaBuildToolsConfig(applied, config.buildTools).fetchDependencies shouldBe false
    }

  test("with build tools allowed the frontend configuration is left as it is"):
    val config = JavaConfig(fetchDependencies = true, delombokMode = Some("types-only"))
    Atom.javaBuildToolsConfig(config, buildTools = true) shouldBe config
end BuildToolsOptionTests
