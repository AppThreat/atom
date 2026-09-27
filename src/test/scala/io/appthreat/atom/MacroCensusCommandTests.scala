package io.appthreat.atom

import better.files.File as BFile
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.Config as OdbConfig

/** `--suggest-defines` writes the macro census and stops; `--auto-defines` applies it. Both are
  * first-class flags that a later `--frontend-args` must not drop.
  */
class MacroCensusCommandTests extends AnyWordSpec with Matchers:

  private val source =
      """#if CONFIG_FEATURE
        |int feature_fn(int x) {
        |  x += 1;
        |  x += 2;
        |  return x;
        |}
        |#endif
        |int always(int x) { return x; }
        |""".stripMargin

  private def methodNames(atomFile: BFile): List[String] =
    val odbConfig = OdbConfig.withDefaults().withStorageLocation(atomFile.pathAsString)
    val cpg = CpgLoader.loadFromOverflowDb(
      CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    )
    try cpg.method.name.l
    finally cpg.close()

  "--suggest-defines" should:
    "write the census and build no atom, even before a --frontend-args" in:
      BFile.usingTemporaryDirectory("atom-census") { dir =>
        val src = dir / "src"
        src.createDirectory()
        (src / "a.c").write(source)
        val atomFile = dir / "app.atom"
        val base     = dir / "out" / "census"
        val result = Atom.run(
          Array(
            "-l",
            "c",
            "-o",
            atomFile.pathAsString,
            "--suggest-defines",
            base.pathAsString,
            "--frontend-args",
            "enable-ast-cache=false",
            src.pathAsString
          )
        )
        result shouldBe Right("Wrote the macro census")
        (dir / "out" / "census.h").contentAsString should include("#define CONFIG_FEATURE 1")
        atomFile.exists shouldBe false
      }

  "--auto-defines" should:
    "parse the code behind an auto-tier macro" in:
      BFile.usingTemporaryDirectory("atom-census-auto") { dir =>
        val src = dir / "src"
        src.createDirectory()
        (src / "a.c").write(source)
        val plain = dir / "plain.atom"
        val auto  = dir / "auto.atom"
        def build(out: BFile, extra: String*) = Atom.run(
          (Seq("-l", "c", "-o", out.pathAsString, "--frontend-args", "enable-ast-cache=false") ++
              extra ++ Seq(src.pathAsString)).toArray
        )
        build(plain).isRight shouldBe true
        build(auto, "--auto-defines").isRight shouldBe true
        methodNames(plain) should not contain "feature_fn"
        methodNames(auto) should contain("feature_fn")
      }
end MacroCensusCommandTests
