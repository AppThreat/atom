package io.appthreat.atom

import better.files.File
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** The help block in the README and the CLI page is the real `atom --help` text: a new option or a
  * changed description has to be copied into both, or this fails.
  */
class CliDocsTests extends AnyFunSuite with Matchers:

  private def normalise(text: String): List[String] =
      text.linesIterator.map(_.stripTrailing()).toList.reverse.dropWhile(_.isEmpty).reverse

  /** The fenced block that starts with the usage line. */
  private def documentedHelp(file: File): List[String] =
    val lines = file.lines.toList
    val start = lines.indexWhere(_.startsWith("Usage: atom "))
    start should be >= 0
    lines.drop(start).takeWhile(!_.startsWith("```")).map(_.stripTrailing())

  Seq("README.md", "docs/docs/cli.md").foreach { path =>
      test(s"$path shows the current --help text"):
        documentedHelp(File(path)) shouldBe normalise(Atom.usage)
  }
