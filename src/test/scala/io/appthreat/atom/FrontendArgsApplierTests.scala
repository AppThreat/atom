package io.appthreat.atom

import io.appthreat.atom.frontends.FrontendArgsApplier
import io.appthreat.c2cpg.Config as CConfig
import io.appthreat.javasrc2cpg.Config as JavaConfig
import io.appthreat.jimple2cpg.Config as JimpleConfig
import io.appthreat.jssrc2cpg.Config as JSConfig
import io.appthreat.php2atom.Config as PhpConfig
import io.appthreat.pysrc2cpg.Py2CpgOnFileSystemConfig as PyConfig
import io.appthreat.ruby2atom.Config as RubyConfig
import io.appthreat.x2cpg.PythonDepsMode
import io.appthreat.x2cpg.passes.frontend.{XTypeRecovery, XTypeRecoveryConfig}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** Pure unit tests for [[FrontendArgsApplier]]. No CPG is built: each frontend `Config` is
  * constructed directly and the applier is exercised in isolation, so the suite is fast and
  * independent of the analysis toolchain.
  */
class FrontendArgsApplierTests extends AnyFunSuite with Matchers:

  private val inputPath = System.getProperty("java.io.tmpdir")

  test("value readers handle absent, present and malformed values"):
    val args = Map("present" -> "true", "num" -> "3", "bad" -> "nope", "csv" -> "a, b ,c")
    FrontendArgsApplier.bool(args, "present", default = false) shouldBe true
    FrontendArgsApplier.bool(args, "absent", default = true) shouldBe true
    FrontendArgsApplier.bool(args, "bad", default = true) shouldBe true
    FrontendArgsApplier.int(args, "num", default = 0) shouldBe 3
    FrontendArgsApplier.int(args, "bad", default = 7) shouldBe 7
    FrontendArgsApplier.str(args, "present", default = "") shouldBe "true"
    FrontendArgsApplier.csv(args, "csv") shouldBe Set("a", "b", "c")
    FrontendArgsApplier.csv(args, "absent") shouldBe Set.empty
    FrontendArgsApplier.strOpt(args, "present") shouldBe Some("true")
    FrontendArgsApplier.strOpt(args, "absent") shouldBe None

  test("applyC honours enable-ast-cache (the previously hardcoded true)"):
    val c = CConfig().withInputPath(inputPath)
    // Default keeps the cache enabled.
    val afterDefault = FrontendArgsApplier.applyC(c, Map.empty)
    afterDefault.enableAstCache shouldBe true
    // Explicitly disabling now actually takes effect.
    val disabled = FrontendArgsApplier.applyC(c, Map("enable-ast-cache" -> "false"))
    disabled.enableAstCache shouldBe false

  test("applyC forwards C-specific knobs and merges include paths"):
    val base = CConfig().withInputPath(inputPath).withIncludePaths(Set("/base/include"))
    val args = Map(
      "defines"          -> "DEBUG,RELEASE",
      "cpp-standard"     -> "c++17",
      "include-paths"    -> "/opt/include",
      "include-files"    -> "common.h",
      "include-comments" -> "true"
    )
    val result = FrontendArgsApplier.applyC(base, args)
    result.defines shouldBe Set("DEBUG", "RELEASE")
    result.cppStandard shouldBe "c++17"
    result.includePaths shouldBe Set("/base/include", "/opt/include")
    result.includeFiles shouldBe Set("common.h")
    result.includeComments shouldBe true

  test("applyC defaults the cache dir to <input>/.chen"):
    val result = FrontendArgsApplier.applyC(
      CConfig().withInputPath(inputPath),
      Map.empty
    )
    result.cacheDir.endsWith(".chen") shouldBe true

  test("applyJava forwards delombok and jdk options"):
    val args = Map(
      "delombok-mode"      -> "run-delombok",
      "jdk-path"           -> "/opt/jdk17",
      "fetch-dependencies" -> "true"
    )
    val result = FrontendArgsApplier.applyJava(JavaConfig().withInputPath(inputPath), args)
    result.delombokMode shouldBe Some("run-delombok")
    result.jdkPath shouldBe Some("/opt/jdk17")
    result.fetchDependencies shouldBe true

  test("applyJava lets no-dummyTypes flow through type recovery"):
    val result =
        FrontendArgsApplier.applyJava(
          JavaConfig().withInputPath(inputPath),
          Map("no-dummyTypes" -> "true")
        )
    result.disableDummyTypes shouldBe true

  test("applyPython preserves the type-recovery defaults the caller set"):
    // The gap that let a real bug live for the lifetime of the flag. The test above proves only
    // that an *argument* reaches the config; what was never tested is that a value the caller
    // set survives the applier when no argument overrides it. It did not: every copy-based
    // `withX` builder re-initialises the inherited `TypeRecoveryParserConfig` vars, so
    // `Atom.createPythonCpg` asking for `withDisableDummyTypes(true)` came back as `false`, and
    // Python graphs shipped full of placeholder types.
    val result = FrontendArgsApplier.applyPython(
      PyConfig()
          .withInputPath(inputPath)
          .withDisableDummyTypes(true)
          .withTypePropagationIterations(2),
      // unrelated keys, so the copy-based builders really do run
      Map("strict-parse" -> "true", "ignore-dir-names" -> "docs")
    )
    result.disableDummyTypes shouldBe true
    result.typePropagationIterations shouldBe 2
    result.strictParse shouldBe true

  test("an explicit type-recovery argument still overrides the caller default"):
    val result = FrontendArgsApplier.applyPython(
      PyConfig()
          .withInputPath(inputPath)
          .withDisableDummyTypes(true)
          .withTypePropagationIterations(2),
      Map("no-dummyTypes" -> "false", "type-prop-iterations" -> "4")
    )
    result.disableDummyTypes shouldBe false
    result.typePropagationIterations shouldBe 4

  test("XTypeRecovery.configFor translates a frontend config into a recovery config"):
    // The other half of the same bug: the knobs stay inert unless the recovery pass is built
    // from them, and `Atom.createPythonCpg` used to restate the config as a literal and discard
    // both.
    val config = PyConfig()
        .withInputPath(inputPath)
        .withDisableDummyTypes(true)
        .withTypePropagationIterations(2)
    XTypeRecovery.configFor(config) shouldBe
        XTypeRecoveryConfig(iterations = 2, enabledDummyTypes = false)

  test("applyJs forwards ts-types and flow"):
    val result = FrontendArgsApplier.applyJs(
      JSConfig().withInputPath(inputPath),
      Map("ts-types" -> "false", "flow" -> "true")
    )
    result.tsTypes shouldBe false
    result.flow shouldBe true

  test("applyJimple forwards depth, full-resolver and android"):
    val args   = Map("depth" -> "5", "full-resolver" -> "true", "android" -> "/opt/android.jar")
    val result = FrontendArgsApplier.applyJimple(JimpleConfig().withInputPath(inputPath), args)
    result.depth shouldBe 5
    result.fullResolver shouldBe true
    result.android shouldBe Some("/opt/android.jar")

  test("applyPython forwards venv-dir and ignore paths"):
    val args   = Map("venv-dir" -> ".venv-2", "ignore-paths" -> "build/, dist/")
    val result = FrontendArgsApplier.applyPython(PyConfig().withInputPath(inputPath), args)
    result.venvDir.toString shouldBe ".venv-2"
    result.ignorePaths.map(_.toString) should contain("build")
    result.ignorePaths.map(_.toString) should contain("dist")

  test("applyPython keeps python-deps=none default and honours the stubs/summaries/full modes"):
    // The default path ships: an absent key must leave today's behaviour untouched.
    val defaultResult = FrontendArgsApplier.applyPython(
      PyConfig().withInputPath(inputPath),
      Map("strict-parse" -> "true") // unrelated key so the copy builders run
    )
    defaultResult.pythonDeps shouldBe PythonDepsMode.Disabled

    val stubs = FrontendArgsApplier.applyPython(
      PyConfig().withInputPath(inputPath),
      Map("python-deps" -> "stubs")
    )
    stubs.pythonDeps shouldBe PythonDepsMode.Stubs
    // stubs mode must NOT silently flip the venv handling - that is `full`.
    stubs.ignoreVenvDir shouldBe true

    val summaries = FrontendArgsApplier.applyPython(
      PyConfig().withInputPath(inputPath),
      Map("python-deps" -> "summaries", "typeshed-dir" -> "/tmp/typeshed")
    )
    summaries.pythonDeps shouldBe PythonDepsMode.Summaries
    summaries.typeshedDir shouldBe Some(java.nio.file.Paths.get("/tmp/typeshed"))

    // `full` is the legacy ignore-venv-dir=false switch
    val full = FrontendArgsApplier.applyPython(
      PyConfig().withInputPath(inputPath),
      Map("python-deps" -> "full")
    )
    full.pythonDeps shouldBe PythonDepsMode.Full

    // garbage falls back to none rather than crashing the run
    val garbage = FrontendArgsApplier.applyPython(
      PyConfig().withInputPath(inputPath),
      Map("python-deps" -> "maybe")
    )
    garbage.pythonDeps shouldBe PythonDepsMode.Disabled

  test("applyPhp honours enable-ast-cache and cache-dir"):
    val result = FrontendArgsApplier.applyPhp(
      PhpConfig().withInputPath(inputPath),
      Map("enable-ast-cache" -> "false", "php-ini" -> "/etc/php.ini")
    )
    result.enableAstCache shouldBe false
    result.phpIni shouldBe Some("/etc/php.ini")

  test("applyRuby honours disable-type-stubs"):
    val disabled =
        FrontendArgsApplier.applyRuby(
          RubyConfig().withInputPath(inputPath),
          Map("disable-type-stubs" -> "true")
        )
    disabled.useTypeStubs shouldBe false
    val kept =
        FrontendArgsApplier.applyRuby(RubyConfig().withInputPath(inputPath), Map.empty)
    kept.useTypeStubs shouldBe true

  test("applyUniversal threads exclude and exclude-regex into ignoredFiles"):
    val c      = CConfig().withInputPath(inputPath)
    val result = FrontendArgsApplier.applyUniversal(c, Map("exclude" -> "vendor,tmp"))
    // createPathForIgnore makes each entry an absolute path under inputPath.
    result.ignoredFiles.exists(_.endsWith("vendor")) shouldBe true
    result.ignoredFiles.exists(_.endsWith("tmp")) shouldBe true
    result.ignoredFiles.size shouldBe 2

  test("keysForLanguage returns keys relevant to a language"):
    val javaKeys = FrontendArgsApplier.keysForLanguage("java").map(_.name)
    javaKeys should contain("delombok-mode")
    javaKeys should contain("exclude")
    javaKeys should not contain ("cpp-standard")

    val cppKeys = FrontendArgsApplier.keysForLanguage("cpp").map(_.name)
    cppKeys should contain("cpp-standard")
    cppKeys should contain("defines")

  test("renderKeys produces a readable header for a known language"):
    val rendered = FrontendArgsApplier.renderKeys("python")
    rendered should include("key")
    rendered should include("venv-dir")
end FrontendArgsApplierTests
