package io.appthreat.atom

import better.files.File
import io.circe.parser.parse
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.{FileSystems, Paths}
import java.util.zip.ZipFile
import scala.jdk.CollectionConverters.*
import scala.util.Using

/** A native image only carries the classpath resources its resource config names; a resource a pass
  * loads but the config misses is silently absent from the native binary (the loaders fall back to
  * an empty vocabulary). Every data file at the root of the chen jars atom ships must be covered.
  */
class NativeImageResourcesTests extends AnyFunSuite with Matchers:

  private val configFile =
      File("src/main/resources/META-INF/native-image/resource-config.json")

  private val globs: List[String] =
      parse(configFile.contentAsString).toOption
          .flatMap(_.hcursor.downField("globs").as[List[io.circe.Json]].toOption)
          .getOrElse(Nil)
          .flatMap(_.hcursor.downField("glob").as[String].toOption)

  /** One class from each chen artifact atom bundles; its code source is that artifact's jar. */
  private val chenArtifacts: Seq[Class[?]] = Seq(
    classOf[io.appthreat.x2cpg.passes.taggers.CdxPass],
    io.appthreat.c2cpg.Main.getClass,
    io.appthreat.php2atom.Main.getClass,
    io.appthreat.jssrc2cpg.Main.getClass,
    io.appthreat.pysrc2cpg.Py2CpgOnFileSystem.getClass,
    io.appthreat.javasrc2cpg.Main.getClass,
    io.appthreat.jimple2cpg.Main.getClass,
    io.appthreat.ruby2atom.Main.getClass,
    io.appthreat.dataflowengineoss.DefaultSemantics.getClass
  )

  private val DataExtensions = Seq(".json", ".txt", ".conf", ".properties")

  private def rootDataFiles(cls: Class[?]): List[String] =
    val location = Paths.get(cls.getProtectionDomain.getCodeSource.getLocation.toURI)
    if location.toString.endsWith(".jar") then
      Using.resource(ZipFile(location.toFile)) { zip =>
          zip.entries.asScala.map(_.getName)
              .filter(n => !n.contains("/") && DataExtensions.exists(n.endsWith))
              .toList
      }
    else
      File(location).list.filter(_.isRegularFile).map(_.name)
          .filter(n => DataExtensions.exists(n.endsWith))
          .toList

  private def covered(name: String): Boolean =
      globs.exists(g => FileSystems.getDefault.getPathMatcher(s"glob:$g").matches(Paths.get(name)))

  test("the resource config parses and lists the chen vocabularies"):
    (globs should contain).allOf("component-tags.json", "memory-apis.json", "trackers.json")

  test("every data file at the root of a bundled chen jar is in the native resource config"):
    val files = chenArtifacts.flatMap(rootDataFiles).distinct
    files should contain("memory-apis.json")
    files.filterNot(covered) shouldBe empty
end NativeImageResourcesTests
