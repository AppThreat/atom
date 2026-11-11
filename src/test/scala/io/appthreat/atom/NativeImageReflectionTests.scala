package io.appthreat.atom

import better.files.File
import io.circe.Json
import io.circe.parser.parse
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.nio.file.Paths
import java.util.zip.ZipFile
import scala.jdk.CollectionConverters.*
import scala.util.Using

/** A native image reflects only on what its reachability metadata names. A frontend that looks a
  * class or a getter up by name and finds it missing throws in the native binary alone, and the
  * frontend then drops the whole file it was converting: the JVM build keeps the file, the native
  * one writes no slice for it. Every name the frontends look up this way must be registered.
  */
class NativeImageReflectionTests extends AnyFunSuite with Matchers:

  private val metadataDir = File("src/main/resources/META-INF/native-image")

  private def jsonOf(file: File): Json =
      parse(file.contentAsString).fold(e => fail(s"$file is not valid JSON: $e"), identity)

  /** The reflection entries of both metadata files, by type name. native-image reads the legacy
    * `reflect-config.json` (`name`) next to `reachability-metadata.json` (`type`), and some lookups
    * are registered only in the legacy file.
    */
  private val entries: Map[String, List[Json]] =
    val current = jsonOf(metadataDir / "reachability-metadata.json").hcursor
        .downField("reflection").as[List[Json]].fold(e => fail(e.toString), identity)
        .flatMap(e => e.hcursor.downField("type").as[String].toOption.map(_ -> e))
    val legacy = jsonOf(metadataDir / "reflect-config.json").as[List[Json]]
        .fold(e => fail(e.toString), identity)
        .flatMap(e => e.hcursor.downField("name").as[String].toOption.map(_ -> e))
    (current ++ legacy).groupMap(_._1)(_._2)

  /** The classes of the jar (or directory) that holds `cls`, as binary names. */
  private def classNames(cls: Class[?]): List[String] =
    val location = Paths.get(cls.getProtectionDomain.getCodeSource.getLocation.toURI)
    val files =
        if location.toString.endsWith(".jar") then
          Using.resource(ZipFile(location.toFile)) { zip =>
              zip.entries.asScala.map(_.getName).toList
          }
        else
          // relativize gives the platform's separator; a zip entry name always uses '/'
          val root = File(location)
          root.listRecursively.filter(_.isRegularFile)
              .map(f => root.path.relativize(f.path).iterator.asScala.mkString("/")).toList
    files.filter(_.endsWith(".class")).map(_.stripSuffix(".class").replace('/', '.'))

  private def flag(entry: Json, name: String): Boolean =
      entry.hcursor.downField(name).as[Boolean].toOption.contains(true)

  private def names(entry: Json, member: String): List[String] =
      entry.hcursor.downField(member).as[List[Json]].toOption.getOrElse(Nil)
          .flatMap(_.hcursor.downField("name").as[String].toOption)

  private def registersField(typeName: String, field: String): Boolean =
      entries.getOrElse(typeName, Nil).exists(e =>
          names(e, "fields").contains(field) || flag(e, "allDeclaredFields") ||
              flag(e, "allPublicFields")
      )

  private def registersMethod(typeName: String, method: String): Boolean =
      entries.getOrElse(typeName, Nil).exists(e =>
          names(e, "methods").contains(method) || flag(e, "allDeclaredMethods") ||
              flag(e, "allPublicMethods")
      )

  /** `Class.getMethod` finds inherited public methods as well, so only `allPublicMethods` covers
    * every getter a node class answers to. The legacy `queryAllPublicMethods` lets the method be
    * found but not invoked.
    */
  private def registersPublicMethods(typeName: String): Boolean =
      entries.getOrElse(typeName, Nil).exists(flag(_, "allPublicMethods"))

  test("every Babel node of jssrc2cpg is registered with its MODULE$ field"):
    // BabelAst.fromString turns each astgen node type into its object with Class.forName and a
    // read of MODULE$, so an unregistered node type (a dotted type name, a type alias, a tuple
    // type) loses the file it occurs in
    val nodes = classNames(io.appthreat.jssrc2cpg.Main.getClass)
        .filter(_.matches("""io\.appthreat\.jssrc2cpg\.parser\.BabelAst\$\w+\$"""))
    nodes should contain("io.appthreat.jssrc2cpg.parser.BabelAst$TSQualifiedName$")
    nodes.filterNot(registersField(_, "MODULE$")) shouldBe empty

  test("every JavaParser AST node is registered with its public methods"):
    // A change to a parsed compilation unit (the members javasrc2cpg adds for Lombok) is replayed
    // by the lexical-preserving printer, which reads each property of the changed nodes through
    // its getter (ObservableProperty.getRawValue)
    val nodeClass = classOf[com.github.javaparser.ast.Node]
    val loader    = nodeClass.getClassLoader
    val nodes = classNames(nodeClass)
        .filter(n => n.startsWith("com.github.javaparser.ast.") && !n.contains('$'))
        .filter(n => nodeClass.isAssignableFrom(Class.forName(n, false, loader)))
    nodes should contain("com.github.javaparser.ast.body.ClassOrInterfaceDeclaration")
    nodes.filterNot(registersPublicMethods) shouldBe empty

  test("the other members chen looks up by name are registered"):
    // UnicodeSkeleton (x2cpg) reaches java.text.Normalizer through Class.forName, getField and
    // getMethod. CdtLogging (c2cpg) writes the private Plugin.bundle field so that CDT's plugin has
    // a bundle outside OSGi. Both are registered only in the legacy reflect-config.json.
    registersMethod("java.text.Normalizer", "normalize") shouldBe true
    registersField("java.text.Normalizer$Form", "NFD") shouldBe true
    registersField("org.eclipse.core.runtime.Plugin", "bundle") shouldBe true
    // the members still exist under these names
    val form = Class.forName("java.text.Normalizer$Form")
    noException should be thrownBy form.getField("NFD")
    noException should be thrownBy
        Class.forName("java.text.Normalizer").getMethod("normalize", classOf[CharSequence], form)
    noException should be thrownBy
        classOf[org.eclipse.core.runtime.Plugin].getDeclaredField("bundle")
end NativeImageReflectionTests
