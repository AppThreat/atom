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
  * an empty vocabulary). Every data file of the chen jars atom ships must be covered.
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

  /** Directories of code and metadata, whose files are not data a pass loads. */
  private val CodeDirectories = Set("META-INF", "io", "org", "com", "scala")

  /** The data files of an artifact: at its root, and in its data directories
    * (`predefined-macros/`).
    */
  private def dataFiles(cls: Class[?]): List[String] =
    val location = Paths.get(cls.getProtectionDomain.getCodeSource.getLocation.toURI)
    def isData(name: String): Boolean =
        DataExtensions.exists(name.endsWith) && !CodeDirectories.contains(name.takeWhile(_ != '/'))
    if location.toString.endsWith(".jar") then
      Using.resource(ZipFile(location.toFile)) { zip =>
          zip.entries.asScala.map(_.getName).filter(isData).toList
      }
    else
      val root = File(location)
      root.listRecursively.filter(_.isRegularFile).map(f => root.relativize(f).toString)
          .filter(isData).toList

  private def covered(name: String): Boolean =
      globs.exists(g => FileSystems.getDefault.getPathMatcher(s"glob:$g").matches(Paths.get(name)))

  test("the resource config parses and lists the chen vocabularies"):
    (globs should contain).allOf("component-tags.json", "memory-apis.json", "trackers.json")

  /** The service registrations of an artifact (`META-INF/services/<interface>`). */
  private def serviceFiles(cls: Class[?]): List[String] =
    val location = Paths.get(cls.getProtectionDomain.getCodeSource.getLocation.toURI)
    def isService(name: String): Boolean =
        name.startsWith("META-INF/services/") && !name.endsWith("/")
    if location.toString.endsWith(".jar") then
      Using.resource(ZipFile(location.toFile)) { zip =>
          zip.entries.asScala.map(_.getName).filter(isService).toList
      }
    else
      val root = File(location)
      root.listRecursively.filter(_.isRegularFile).map(f => root.relativize(f).toString)
          .filter(isService).toList

  test("every service a bundled chen jar registers is in the native resource config"):
    val files = chenArtifacts.flatMap(serviceFiles).distinct
    // CDT's plugin finds its bundle through this service outside an OSGi runtime
    files should contain("META-INF/services/org.osgi.framework.connect.FrameworkUtilHelper")
    files.filterNot(covered) shouldBe empty

  /** `module` globs of the config: (module, glob). */
  private val moduleGlobs: List[(String, String)] =
      parse(configFile.contentAsString).toOption
          .flatMap(_.hcursor.downField("globs").as[List[io.circe.Json]].toOption)
          .getOrElse(Nil)
          .flatMap { g =>
              for
                module <- g.hcursor.downField("module").as[String].toOption
                glob   <- g.hcursor.downField("glob").as[String].toOption
              yield (module, glob)
          }

  test("the running JDK's ICU normalisation data is in the native resource config"):
    // java.text.Normalizer loads nfc.nrm (NFD, Unicode skeletons) and nfkc.nrm (PEP 3131 identifier
    // normalisation in pysrc2cpg) from a directory named after the JDK's ICU version: icudt72b
    // (JDK 21), icudt74b (23), icudt76b (24, 25), icudata (26). A native image without them
    // normalises nothing.
    val jrt      = FileSystems.getFileSystem(java.net.URI.create("jrt:/"))
    val javaBase = jrt.getPath("/modules/java.base")
    val data     = javaBase.resolve("jdk/internal/icu/impl/data")
    val files = Using.resource(java.nio.file.Files.walk(data)) { s =>
        s.iterator.asScala.map(p => javaBase.relativize(p).toString)
            .filter(n => n.endsWith("/nfc.nrm") || n.endsWith("/nfkc.nrm")).toList
    }
    files.map(_.split('/').last).toSet shouldBe Set("nfc.nrm", "nfkc.nrm")
    for file <- files do
      withClue(file) {
          moduleGlobs.exists { (module, glob) =>
              module == "java.base" &&
              FileSystems.getDefault.getPathMatcher(s"glob:$glob").matches(Paths.get(file))
          } shouldBe true
      }

  test("every data file of a bundled chen jar is in the native resource config"):
    val files = chenArtifacts.flatMap(dataFiles).distinct
    files should contain("memory-apis.json")
    files should contain("predefined-macros/gcc-linux-x86_64.txt")
    files should contain("unicode/confusables.txt")
    files.filterNot(covered) shouldBe empty
end NativeImageResourcesTests
