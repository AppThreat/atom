package io.appthreat.atom

import better.files.File
import io.appthreat.x2cpg.passes.frontend.CacheControl
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.Files
import scala.util.{Failure, Success, Try}

/** End to end reproducer for AppThreat/atom#423: the method-flow-summary sidecar that a
  * `reachables` run writes beside the slices file must not follow a symbolic link committed by the
  * scanned checkout.
  *
  * A repository's author learns the sidecar's name with a private run on an identical copy (the
  * name is a fingerprint of the graph, not of the checkout path), then ships a link at that name
  * plus `.tmp` pointing at a file outside the tree. The slices are written beside the checkout,
  * which is what lets the checkout decide the names in that directory. The near-empty project (a
  * lone requirements.txt) is what makes the sidecar appear at all: the summary store only computes
  * and writes it when the atom carries no embedded flow-summary tags, and a project with source
  * files gets those tags during enhancement.
  */
class ReachablesFlowSummarySymlinkTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: File = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    // Other suites in this forked JVM pass `--cache none` or `--cache no-summary`, which leaves
    // the caches disabled for every later run in the same JVM; this reproducer needs the flow
    // summary sidecar written.
    System.clearProperty("chen.cache.disabled")
    System.clearProperty("chen.cache.disabled.summary")
    CacheControl.enableAll()
    workspace = File.newTemporaryDirectory("atomFlowSummarySymlink")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def runReachables(dir: File): Unit =
    val result = Atom.run(
      Array(
        "reachables",
        "-l",
        "python",
        "-o",
        (dir / "app.atom").pathAsString,
        "-s",
        (dir / "reach.json").pathAsString,
        dir.pathAsString
      )
    )
    withClue(s"reachables did not complete: $result"):
      result.isRight shouldBe true

  "reachables with a linked flow-summary temporary beside the slices" should {

      "leave the link target untouched and still complete the run" in {
          // The author's private run: an identical copy, so the same summary fingerprint and the
          // same sidecar name.
          val attacker = workspace / "attacker"
          attacker.createDirectories()
          (attacker / "requirements.txt").write("requests==2.0\n")
          runReachables(attacker)

          val sidecars = attacker.children.toSeq
              .map(_.name)
              .filter(_.matches("flowsummary-.*\\.json"))
          withClue("the private run wrote no flow-summary sidecar whose name could be learned"):
            sidecars should not be empty

          // The repository as shipped: a link where the run writes the sidecar's temporary file,
          // pointing at a file outside the analysed tree.
          val victim = workspace / "victim"
          victim.createDirectories()
          (victim / "requirements.txt").write("requests==2.0\n")
          val notes = workspace / "home" / "notes.txt"
          notes.parent.createDirectories()
          notes.write("operator data, do not lose")
          Try(Files.createSymbolicLink((victim / s"${sidecars.head}.tmp").path, notes.path)) match
            case Failure(e) =>
                cancel(s"this platform refuses to create a symbolic link: ${e.getMessage}")
            case Success(_) => ()

          // Anyone scanning the shipped repository, with the slices written beside it.
          runReachables(victim)

          notes.contentAsString shouldBe "operator data, do not lose"
          // The run wrote its own sidecar at the final name, as a regular file rather than
          // through the link that sat at the temporary name.
          val sidecar = victim / sidecars.head
          withClue(s"flow-summary sidecar $sidecar"):
            sidecar.isSymbolicLink shouldBe false
            sidecar.isRegularFile shouldBe true
          val slices = victim / "reach.json"
          slices.exists shouldBe true
          noException should be thrownBy ujson.read(slices.contentAsString)
      }
  }
end ReachablesFlowSummarySymlinkTests
