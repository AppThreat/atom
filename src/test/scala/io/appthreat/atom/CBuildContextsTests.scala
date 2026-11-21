package io.appthreat.atom

import better.files.File
import io.appthreat.atom.frontends.FrontendArgsApplier
import io.appthreat.c2cpg.Config as CConfig
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** The --c-build-contexts option: what a run makes of a compilation database that compiles one file
  * more than once. `first`, the default, keeps today's analysis of the first entry's build context
  * alone; `all` analyses every distinct context of the file. The value reaches the C frontend's
  * settings under the `c-build-contexts` argument key.
  */
class CBuildContextsTests extends AnyFunSuite with Matchers:

  private def parse(args: String*): AtomConfig =
      Atom.parseConfig(args.toList) match
        case Right(c: AtomConfig) => c
        case other                => fail(s"Unexpected config result: $other")

  private def applied(args: Map[String, String])(check: CConfig => Unit): Unit =
      File.usingTemporaryDirectory("atom-bctx") { dir =>
          check(
            FrontendArgsApplier.applyC(
              CConfig().withInputPath(dir.pathAsString),
              args
            )
          )
      }

  test("the option reaches the frontend arguments under its key"):
    File.usingTemporaryDirectory("atom-bctx") { dir =>
      parse("-l", "c", "--c-build-contexts", "all", dir.pathAsString)
          .frontendArgs
          .get("c-build-contexts") shouldBe Some("all")
      parse("-l", "c", "--c-build-contexts", "first", dir.pathAsString)
          .frontendArgs
          .get("c-build-contexts") shouldBe Some("first")
    }

  test("without the option no build-contexts argument is set"):
    File.usingTemporaryDirectory("atom-bctx") { dir =>
        parse("-l", "c", dir.pathAsString)
            .frontendArgs
            .get("c-build-contexts") shouldBe None
    }

  test("a value other than first or all is refused"):
    File.usingTemporaryDirectory("atom-bctx") { dir =>
      Atom.parseConfig(
        List("-l", "c", "--c-build-contexts", "sometimes", dir.pathAsString)
      ) shouldBe a[Left[_, _]]
      Atom.parseConfig(
        List("-l", "c", "--c-build-contexts", "", dir.pathAsString)
      ) shouldBe a[Left[_, _]]
    }

  test("the argument value reaches the C frontend's settings"):
    applied(Map("c-build-contexts" -> "all"))(c => c.buildContexts shouldBe "all")
    applied(Map.empty)(c => c.buildContexts shouldBe "first")
    applied(Map("c-build-contexts" -> "first"))(c => c.buildContexts shouldBe "first")

  test("the isolate option reaches the frontend arguments under its key"):
    File.usingTemporaryDirectory("atom-bctx") { dir =>
      parse("-l", "c", "--c-isolate", dir.pathAsString)
          .frontendArgs
          .get("c-isolate") shouldBe Some("true")
      parse("-l", "c", dir.pathAsString)
          .frontendArgs
          .get("c-isolate") shouldBe None
    }

  test("the isolate argument reaches the C frontend's settings"):
    applied(Map("c-isolate" -> "true"))(c => c.cIsolate shouldBe true)
    applied(Map.empty)(c => c.cIsolate shouldBe false)
end CBuildContextsTests
