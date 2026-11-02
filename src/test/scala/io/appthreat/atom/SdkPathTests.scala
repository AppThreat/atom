package io.appthreat.atom

import better.files.File
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `--android-sdk` takes the `android.jar` file, the platform directory that holds it, or an SDK
  * root with `platforms/<name>/android.jar` (atom#328). Anything else is a clear error, before the
  * frontend runs.
  */
class SdkPathTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private val tmp = File.newTemporaryDirectory("atomSdkPaths")

  private val jar   = tmp / "jar-dir" / "android.jar"
  private val stub  = tmp / "platforms" / "android-35"
  private val other = tmp / "platforms" / "android-34"

  override protected def afterAll(): Unit = tmp.delete(swallowIOExceptions = true)

  private def resolve(path: String) = Atom.resolveAndroidSdk(path)

  "the android-sdk path resolution" should {

      "take the android.jar file as it is" in {
          jar.parent.createDirectories()
          jar.write("")
          resolve(jar.pathAsString) shouldBe Right(jar.pathAsString)
      }

      "take the platform directory that holds android.jar" in {
          stub.createDirectories()
          (stub / "android.jar").write("")
          resolve(stub.pathAsString) shouldBe Right((stub / "android.jar").pathAsString)
      }

      "take an SDK root's newest platform" in {
          other.createDirectories()
          (other / "android.jar").write("")
          resolve(tmp.pathAsString) shouldBe Right((stub / "android.jar").pathAsString)
      }

      "count android-9 below android-35" in {
          val old = tmp / "platforms" / "android-9"
          old.createDirectories()
          (old / "android.jar").write("")
          resolve(tmp.pathAsString) shouldBe Right((stub / "android.jar").pathAsString)
          old.delete(swallowIOExceptions = true)
      }

      "reject a missing path, a directory without android.jar, and a wrong file" in {
          resolve((tmp / "missing" / "android.jar").pathAsString).isLeft shouldBe true
          val empty = tmp / "empty-platform"
          empty.createDirectories()
          resolve(empty.pathAsString).left.toOption.map(_.contains("android.jar")).shouldBe(Some(
            true
          ))
          resolve((tmp / "missing" / "android.jar").pathAsString).left.toOption.map(_.contains(
            "does not exist"
          )) shouldBe Some(true)
      }
  }
end SdkPathTests
