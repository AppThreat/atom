package io.appthreat.atom.slicing.callsites

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.charset.StandardCharsets.UTF_8

/** The readers behind the bytecode source's index: the artifact map that gives records their
  * `purl`, and the class and dex readers on bytes they cannot parse.
  */
class JvmBytecodeInputTests extends AnyWordSpec with Matchers:

  private def entry(name: String, text: String): (String, Array[Byte]) =
      name -> text.getBytes(UTF_8)

  private def pom(group: String, artifact: String, version: String): String =
      s"groupId=$group\nartifactId=$artifact\nversion=$version\n"

  "the artifact map" should {

      "take a .version file's version from its content" in {
          ArtifactMap.fromMetaEntries(
            List(entry("META-INF/androidx.core_core.version", "1.9.0\n")),
            Set("androidx.core.app")
          ) shouldBe Map("androidx.core" -> ("androidx.core", "core", "1.9.0"))
      }

      "drop a package that two artifacts claim" in {
          ArtifactMap.fromMetaEntries(
            List(
              entry("META-INF/androidx.lifecycle_lifecycle-runtime.version", "2.6.1"),
              entry("META-INF/androidx.lifecycle_lifecycle-viewmodel.version", "2.6.1")
            ),
            Set("androidx.lifecycle")
          ) shouldBe Map.empty
      }

      "count one artifact named by both its .version file and its pom.properties once" in {
          ArtifactMap.fromMetaEntries(
            List(
              entry("META-INF/com.example_lib.version", "1.0"),
              entry(
                "META-INF/maven/com.example/lib/pom.properties",
                pom("com.example", "lib", "1.0")
              )
            ),
            Set("com.example.lib")
          ) shouldBe Map("com.example" -> ("com.example", "lib", "1.0"))
      }

      "claim the dotted artifactId when the groupId has no classes" in {
          ArtifactMap.fromMetaEntries(
            List(entry(
              "META-INF/maven/com.github.shyiko/ktlint-core/pom.properties",
              pom("com.github.shyiko", "ktlint-core", "0.3.0")
            )),
            Set("ktlint.core")
          ) shouldBe Map("ktlint.core" -> ("com.github.shyiko", "ktlint-core", "0.3.0"))
      }
  }

  "the readers" should {

      "give nothing for bytes that are no class file" in {
          val junk = Array[Byte](-54, -2, -70, -66, 0, 0, 0, 52, 0, 9)
          ClassFileReader.classInfos(junk) shouldBe Nil
          ClassFileReader.lineTables(junk, _ => true) shouldBe LineTables.empty
          ClassFileReader.bootstraps(junk) shouldBe Map.empty
      }

      "give nothing for bytes that are no dex" in {
          val junk = "dex\n035\u0000".getBytes(UTF_8)
          DexReader.classInfos(junk) shouldBe Nil
          DexReader.lineTables(junk, _ => true) shouldBe LineTables.empty
      }
  }
end JvmBytecodeInputTests
