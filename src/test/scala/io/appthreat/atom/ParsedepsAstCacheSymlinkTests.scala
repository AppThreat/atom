package io.appthreat.atom

import better.files.File
import io.appthreat.x2cpg.passes.frontend.CacheControl
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Files
import scala.util.{Failure, Success, Try}

/** End to end reproducer for AppThreat/atom#422: the AST cache atom keeps in `<input>/.chen` must
  * not follow a symbolic link committed by the scanned checkout.
  *
  * A repository's author learns the cache-entry names with a private run on an identical copy (the
  * entry name is a hash of the parsed source, not of the checkout path), then ships a link at each
  * entry's temporary name pointing at a file outside the tree. This test replays exactly that: a
  * first run into a scratch copy reveals the names, links go into the victim copy at those names
  * plus `.tmp`, and a second run over the victim must complete and leave the links' target
  * untouched.
  */
class ParsedepsAstCacheSymlinkTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: File = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
      // Other suites in this forked JVM pass `--cache none`, which leaves the cache disabled for
      // every later run in the same JVM; this reproducer needs the default `.chen` cache back.
      System.clearProperty("chen.cache.disabled")
      System.clearProperty("chen.cache.disabled.ast")
      CacheControl.enableAll()
      workspace = File.newTemporaryDirectory("atomAstCacheSymlink")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def writeProject(dir: File): Unit =
      dir.createDirectories()
      (dir / "app.py").write("import os\nprint(os.getcwd())\n")

  private def runParsedeps(project: File, atomFile: File, slicesFile: File): Unit =
      val result = Atom.run(
        Array(
          "parsedeps",
          "-l",
          "python",
          "-o",
          atomFile.pathAsString,
          "-s",
          slicesFile.pathAsString,
          project.pathAsString
        )
      )
      withClue(s"parsedeps did not complete: $result"):
          result.isRight shouldBe true

  "parsedeps over a checkout with linked cache-entry temporaries" should {

      "leave the link target untouched, complete the run, and store real cache entries" in {
          // The author's private run: identical sources, so the same cache-entry names.
          val attacker = workspace / "attacker"
          writeProject(attacker / "project")
          runParsedeps(attacker / "project", attacker / "app.atom", attacker / "slices.json")

          val entries = (attacker / "project" / ".chen").children.toSeq
              .map(_.name)
              .filter(name => name.endsWith(".ast") || name.endsWith(".frag"))
          withClue("the private run stored no cache entries whose names could be learned"):
              entries should not be empty

          // The repository as shipped: a link where the run writes each entry's temporary file,
          // pointing at a file outside the analysed tree.
          val victim = workspace / "victim"
          writeProject(victim / "project")
          val notes = workspace / "home" / "notes.txt"
          notes.parent.createDirectories()
          notes.write("operator data")
          val victimChen = victim / "project" / ".chen"
          victimChen.createDirectories()
          entries.foreach { entry =>
              Try(Files.createSymbolicLink((victimChen / s"$entry.tmp").path, notes.path)) match
                case Failure(e) =>
                    cancel(s"this platform refuses to create a symbolic link: ${e.getMessage}")
                case Success(_) => ()
          }

          // Anyone scanning the shipped repository.
          runParsedeps(
            victim / "project",
            workspace / "out" / "app.atom",
            workspace / "out" / "slices.json"
          )

          notes.contentAsString shouldBe "operator data"
          entries.foreach { entry =>
              val stored = victimChen / entry
              withClue(s"cache entry $entry"):
                  stored.isSymbolicLink shouldBe false
                  stored.isRegularFile shouldBe true
          }
          // parsedeps builds the atom and removes it again by design (`withRemoveAtom`); the
          // slices file it leaves behind is the run's completed output.
          val slices = workspace / "out" / "slices.json"
          slices.exists shouldBe true
          ujson.read(slices.contentAsString).obj.get("modules").isDefined shouldBe true
      }
  }
end ParsedepsAstCacheSymlinkTests
