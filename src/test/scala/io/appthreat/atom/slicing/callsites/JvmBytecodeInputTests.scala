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

  "the local artifact caches" should {

      /** A zip of the given entries, each with empty content unless given. */
      def zipOf(entries: (String, Array[Byte])*): Array[Byte] =
        val out = new java.io.ByteArrayOutputStream()
        val zos = new java.util.zip.ZipOutputStream(out)
        entries.foreach { (name, bytes) =>
          zos.putNextEntry(new java.util.zip.ZipEntry(name))
          zos.write(bytes)
          zos.closeEntry()
        }
        zos.close()
        out.toByteArray

      def classes(names: String*): Array[Byte] =
          zipOf(names.map(n => s"$n.class" -> Array.emptyByteArray)*)

      def write(path: java.nio.file.Path, bytes: Array[Byte]): Unit =
        java.nio.file.Files.createDirectories(path.getParent)
        java.nio.file.Files.write(path, bytes)

      "map each class to the one cached artifact that carries it" in {
          val root   = java.nio.file.Files.createTempDirectory("atomArtifactCache")
          val gradle = root.resolve("files-2.1")
          val maven  = root.resolve("m2")
          // two artifacts of one group, as androidx.core ships core and core-ktx
          write(
            gradle.resolve("androidx.core/core/1.9.0/0a1b/core-1.9.0.aar"),
            zipOf("classes.jar" -> classes(
              "androidx/core/app/NotificationCompat",
              "androidx/core/Shared"
            ))
          )
          write(
            gradle.resolve("androidx.core/core-ktx/1.9.0/2c3d/core-ktx-1.9.0.aar"),
            zipOf(
              "classes.jar" -> classes("androidx/core/content/ContextKt", "androidx/core/Shared"),
              "libs/extra.jar" -> classes("androidx/core/content/Extra$Inner")
            )
          )
          write(
            maven.resolve("com/squareup/okio/okio/3.9.0/okio-3.9.0.jar"),
            classes("okio/Buffer")
          )
          val core = ("androidx.core", "core", "1.9.0")
          val ktx  = ("androidx.core", "core-ktx", "1.9.0")
          val okio = ("com.squareup.okio", "okio", "3.9.0")
          LocalArtifactClasses.classArtifacts(
            List(core, ktx, okio, ("not.cached", "x", "1.0")),
            Set(
              "androidx.core.app.NotificationCompat",
              "androidx.core.content.ContextKt",
              "androidx.core.content.Extra$Inner",
              "androidx.core.Shared",
              "okio.Buffer",
              "me.app.Main"
            ),
            Some(gradle),
            Some(maven)
          ) shouldBe Map(
            "androidx.core.app.NotificationCompat" -> core,
            "androidx.core.content.ContextKt"      -> ktx,
            "androidx.core.content.Extra$Inner"    -> ktx,
            "okio.Buffer"                          -> okio
          )
      }

      "credit a Kotlin Multiplatform library's classes to its Android artifact" in {
          val gradle = java.nio.file.Files.createTempDirectory("atomArtifactCacheKmp")
          // the root publishes an empty aar and points Android builds to sqlite-android
          write(
            gradle.resolve("androidx.sqlite/sqlite/2.6.2/aa/sqlite-2.6.2.aar"),
            zipOf("META-INF/androidx/sqlite/sqlite/LICENSE.txt" -> Array.emptyByteArray)
          )
          write(
            gradle.resolve("androidx.sqlite/sqlite/2.6.2/bb/sqlite-2.6.2.module"),
            """{"variants": [
              |  {"name": "metadataApiElements", "attributes": {"org.jetbrains.kotlin.platform.type": "common"}},
              |  {"name": "iosArm64ApiElements-published", "attributes": {"org.jetbrains.kotlin.platform.type": "native"},
              |   "available-at": {"group": "androidx.sqlite", "module": "sqlite-iosarm64", "version": "2.6.2"}},
              |  {"name": "androidRuntimeElements-published", "attributes": {"org.jetbrains.kotlin.platform.type": "jvm"},
              |   "available-at": {"group": "androidx.sqlite", "module": "sqlite-android", "version": "2.6.2"}}
              |]}""".stripMargin.getBytes(UTF_8)
          )
          write(
            gradle.resolve("androidx.sqlite/sqlite-android/2.6.2/cc/sqlite-android-2.6.2.aar"),
            zipOf("classes.jar" -> classes("androidx/sqlite/db/SupportSQLiteDatabase"))
          )
          LocalArtifactClasses.classArtifacts(
            List(("androidx.sqlite", "sqlite", "2.6.2")),
            Set("androidx.sqlite.db.SupportSQLiteDatabase"),
            Some(gradle),
            None
          ) shouldBe Map(
            "androidx.sqlite.db.SupportSQLiteDatabase" -> (
              "androidx.sqlite",
              "sqlite-android",
              "2.6.2"
            )
          )
      }

      "read nothing for an artifact without a version or outside the caches" in {
          val empty = java.nio.file.Files.createTempDirectory("atomArtifactCacheEmpty")
          LocalArtifactClasses.locate(
            "androidx.core",
            "core",
            "1.9.0",
            Some(empty),
            Some(empty)
          ) shouldBe None
          LocalArtifactClasses.classArtifacts(
            List(("androidx.core", "core", "")),
            Set("androidx.core.Shared"),
            Some(empty),
            Some(empty)
          ) shouldBe Map.empty
      }
  }

  "the jar boundary" should {

      /** A class file of the given internal name, written with ASM. */
      def classBytes(internalName: String): Array[Byte] =
        val cw = new org.objectweb.asm.ClassWriter(0)
        cw.visit(
          org.objectweb.asm.Opcodes.V11,
          org.objectweb.asm.Opcodes.ACC_PUBLIC,
          internalName,
          null,
          "java/lang/Object",
          null
        )
        cw.visitEnd()
        cw.toByteArray

      def jarOf(manifest: Map[String, String], classes: String*): java.nio.file.Path =
        val file = java.nio.file.Files.createTempFile("atomJarBoundary", ".jar")
        val mf   = new java.util.jar.Manifest()
        mf.getMainAttributes.putValue("Manifest-Version", "1.0")
        manifest.foreach((k, v) => mf.getMainAttributes.putValue(k, v))
        val out = new java.util.jar.JarOutputStream(java.nio.file.Files.newOutputStream(file), mf)
        classes.foreach { c =>
          out.putNextEntry(new java.util.jar.JarEntry(s"$c.class"))
          out.write(classBytes(c))
          out.closeEntry()
        }
        out.close()
        file

      "count packages, not classes, for the 10 % fallback" in {
          // 20 classes of one package: each class is 5 %, the package is all of them
          JarPrefixes.of((1 to 20).map(i => s"me/app/C$i.class").toList :+ "Top.class") shouldBe
              List("me.app")
      }

      "name the package of Main-Class, and only names that hold classes" in {
          val jar = jarOf(
            Map("Main-Class" -> "me.app.Main", "Automatic-Module-Name" -> "kotlin.stdlib"),
            "me/app/Main",
            "me/app/Other",
            "me/lib/Lib"
          )
          val boundary = JvmBytecodeInput(jar.toString).namedBoundary
          boundary.prefixes shouldBe List("me.app")
          boundary.isApp("me.app.Other") shouldBe true
          boundary.isApp("me.lib.Lib") shouldBe false
      }
  }

  "the invokedynamic sites" should {

      "name their method in the graph's signature form" in {
          ClassFileReader.graphSignature("(Ljava/util/List;I)[Ljava/lang/String;") shouldBe
              "java.lang.String[](java.util.List,int)"
      }
  }

  "the readers" should {

      "give nothing for bytes that are no class file" in {
          val junk = Array[Byte](-54, -2, -70, -66, 0, 0, 0, 52, 0, 9)
          ClassFileReader.classInfos(junk) shouldBe Nil
          ClassFileReader.bootstraps(junk) shouldBe Map.empty
      }

      "give nothing for bytes that are no dex" in {
          val junk = "dex\n035\u0000".getBytes(UTF_8)
          DexReader.classInfos(junk) shouldBe Nil
          DexReader.lineTables(junk, _ => true) shouldBe LineTables.empty
      }
  }
end JvmBytecodeInputTests
