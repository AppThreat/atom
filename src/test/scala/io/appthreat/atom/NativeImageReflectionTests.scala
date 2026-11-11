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

  private val metadataFile =
      File("src/main/resources/META-INF/native-image/reachability-metadata.json")

  private val entries: Map[String, Json] =
      parse(metadataFile.contentAsString).toOption
          .flatMap(_.hcursor.downField("reflection").as[List[Json]].toOption)
          .getOrElse(Nil)
          .flatMap(e => e.hcursor.downField("type").as[String].toOption.map(_ -> e))
          .toMap

  /** The classes of the jar (or directory) that holds `cls`, as binary names. */
  private def classNames(cls: Class[?]): List[String] =
    val location = Paths.get(cls.getProtectionDomain.getCodeSource.getLocation.toURI)
    val files =
        if location.toString.endsWith(".jar") then
          Using.resource(ZipFile(location.toFile)) { zip =>
              zip.entries.asScala.map(_.getName).toList
          }
        else
          val root = File(location)
          root.listRecursively.filter(_.isRegularFile).map(f => root.relativize(f).toString)
              .toList
    files.filter(_.endsWith(".class")).map(_.stripSuffix(".class").replace('/', '.'))

  private def registersField(entry: Json, field: String): Boolean =
      entry.hcursor.downField("fields").as[List[Json]].toOption.getOrElse(Nil)
          .exists(_.hcursor.downField("name").as[String].toOption.contains(field))

  private def registersPublicMethods(entry: Json): Boolean =
      entry.hcursor.downField("allPublicMethods").as[Boolean].toOption.contains(true)

  test("every Babel node of jssrc2cpg is registered with its MODULE$ field"):
    // BabelAst.fromString turns each astgen node type into its object with Class.forName and a
    // read of MODULE$, so an unregistered node type (a dotted type name, a type alias, a tuple
    // type) loses the file it occurs in
    val nodes = classNames(io.appthreat.jssrc2cpg.Main.getClass)
        .filter(_.matches("""io\.appthreat\.jssrc2cpg\.parser\.BabelAst\$\w+\$"""))
    nodes should contain("io.appthreat.jssrc2cpg.parser.BabelAst$TSQualifiedName$")
    nodes.filterNot(n => entries.get(n).exists(registersField(_, "MODULE$"))) shouldBe empty

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
    nodes.filterNot(n => entries.get(n).exists(registersPublicMethods)) shouldBe empty
end NativeImageReflectionTests
