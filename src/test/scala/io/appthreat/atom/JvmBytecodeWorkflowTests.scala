package io.appthreat.atom

import better.files.File
import io.appthreat.jimple2cpg.util.JdkClasses
import io.circe.parser.parse
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.nio.file.FileSystemNotFoundException
import java.util.jar.{JarEntry, JarOutputStream}
import javax.tools.ToolProvider
import scala.compiletime.uninitialized
import scala.jdk.CollectionConverters.*
import scala.util.Using

/** Issue #271: the native image cannot read JDK classes through jrt:/, so every JVM bytecode
  * language (jar, jimple, scala, ...) failed in it. The frontend now reads an installed JDK's
  * lib/modules instead. `chen.jimple.jdk-classes=image` forces that path on the JVM, so these tests
  * exercise exactly what the native image runs and compare it with the jrt:/ result.
  */
class JvmBytecodeWorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workDir: File    = uninitialized
  private var classesDir: File = uninitialized
  private var jarDir: File     = uninitialized

  private val sources = Map(
    "demo/Digests.java" ->
        """package demo;
          |
          |import java.nio.charset.StandardCharsets;
          |import java.security.MessageDigest;
          |
          |public class Digests {
          |    public static byte[] sha256(String input) throws Exception {
          |        MessageDigest md = MessageDigest.getInstance("SHA-256");
          |        return md.digest(input.getBytes(StandardCharsets.UTF_8));
          |    }
          |
          |    public static void main(String[] args) throws Exception {
          |        System.out.println(sha256(String.join(" ", args)).length + " bytes");
          |    }
          |}
          |""".stripMargin,
    "demo/Accounts.java" ->
        """package demo;
          |
          |import java.sql.Connection;
          |import java.sql.DriverManager;
          |import java.sql.ResultSet;
          |import java.sql.Statement;
          |
          |public class Accounts {
          |    public String owner(String url, String id) throws Exception {
          |        try (Connection c = DriverManager.getConnection(url);
          |             Statement st = c.createStatement()) {
          |            ResultSet rs = st.executeQuery("select owner from accounts where id = '" + id + "'");
          |            return rs.next() ? rs.getString(1) : null;
          |        }
          |    }
          |}
          |""".stripMargin
  )

  override protected def beforeAll(): Unit =
    super.beforeAll()
    workDir = File.newTemporaryDirectory("atom-jvm-bytecode")
    val src = (workDir / "src").createDirectory()
    sources.foreach { case (path, code) =>
        (src / path).createIfNotExists(createParents = true).writeText(code)
    }
    classesDir = (workDir / "classes").createDirectory()
    val javac = ToolProvider.getSystemJavaCompiler
    assume(javac != null, "a JDK with javac is required")
    val manager = javac.getStandardFileManager(null, null, null)
    val ok = javac.getTask(
      null,
      manager,
      null,
      List("-g", "-d", classesDir.pathAsString).asJava,
      null,
      manager.getJavaFileObjectsFromFiles(
        src.listRecursively.filter(_.extension.contains(".java")).map(_.toJava).toList.asJava
      )
    ).call()
    assert(ok, "fixture compilation failed")
    jarDir = (workDir / "jarin").createDirectory()
    Using.resource(new JarOutputStream((jarDir / "demo.jar").newOutputStream)) { out =>
        classesDir.listRecursively.filter(_.isRegularFile).foreach { f =>
          out.putNextEntry(new JarEntry(classesDir.relativize(f).toString.replace('\\', '/')))
          out.write(f.byteArray)
          out.closeEntry()
        }
    }
  end beforeAll

  /** The JNI fixture, compiled from the same source as `nativemethods/classes.dex`. */
  private lazy val nativeClassesDir: File =
    val dir = (workDir / "native-classes").createDirectory()
    val src = (dir / "demo" / "NativeBridge.java").createIfNotExists(createParents = true)
    src.writeText(
      File(getClass.getResource("/nativemethods/NativeBridge.java.txt").toURI).contentAsString
    )
    val javac   = ToolProvider.getSystemJavaCompiler
    val manager = javac.getStandardFileManager(null, null, null)
    val ok = javac.getTask(
      null,
      manager,
      null,
      List("-g", "-d", dir.pathAsString).asJava,
      null,
      manager.getJavaFileObjects(src.toJava)
    ).call()
    assert(ok, "NativeBridge failed to compile")
    src.delete()
    dir

  override protected def afterAll(): Unit =
    Option(workDir).foreach(_.delete(swallowIOExceptions = true))
    super.afterAll()

  private def withJdkClasses[T](mode: String)(f: => T): T =
    val previous = Option(System.getProperty(JdkClasses.ModeProperty))
    System.setProperty(JdkClasses.ModeProperty, mode)
    try f
    finally
        previous match
          case Some(v) => System.setProperty(JdkClasses.ModeProperty, v)
          case None    => System.clearProperty(JdkClasses.ModeProperty)

  /** Run `atom usages` and return the set of resolved call targets in the slice. */
  private def usagesTargets(mode: String, lang: String, input: File, name: String): Set[String] =
    val atomFile  = workDir / s"$name-$mode.atom"
    val sliceFile = workDir / s"$name-$mode.usages.json"
    val result = withJdkClasses(mode) {
        Atom.run(Array(
          "usages",
          "-l",
          lang,
          "-o",
          atomFile.pathAsString,
          "-s",
          sliceFile.pathAsString,
          input.pathAsString
        ))
    }
    withClue(s"atom usages -l $lang ($mode): ") { result.isRight shouldBe true }
    sliceFile.exists shouldBe true
    val json = parse(sliceFile.contentAsString).toOption.get
    json.findAllByKey("resolvedMethod").flatMap(_.asString).toSet ++
        json.findAllByKey("fullName").flatMap(_.asString).toSet
  end usagesTargets

  "atom with JDK classes from the JDK image (what the native image does)" should {
      for (lang, inputName) <- List(("jar", "jar"), ("jimple", "classes"), ("scala", "classes")) do
        s"build an atom and a usages slice for -l $lang" in {
            val input = if inputName == "jar" then jarDir else classesDir
            val image = usagesTargets("image", lang, input, s"$lang-$inputName")
            val jrt   = usagesTargets("platform", lang, input, s"$lang-$inputName")
            image should not be empty
            image.exists(_.startsWith("java.security.MessageDigest.getInstance")) shouldBe true
            image shouldBe jrt
        }

      "still build an atom when no JDK can be found" in {
          val targets = usagesTargets("none", "jar", jarDir, "jar-none")
          targets.exists(_.startsWith("java.security.MessageDigest.getInstance")) shouldBe true
      }
  }

  "atom on JNI native methods" should {
      // A dex native method has no method source in Soot; it used to become a METHOD without a
      // BLOCK, and the flow summaries of every data-flow command failed on it.
      "build reachables and data-flow slices for a dex with native methods" in {
          val dex = File(getClass.getResource("/nativemethods/classes.dex").toURI)
          val hasAndroidJar = Seq(
            sys.env.get("ANDROID_HOME"),
            Some(s"${sys.props("user.home")}/Library/Android/sdk")
          )
              .flatten.map(File(_)).exists(d => d.isDirectory && d.glob("**/android.jar").hasNext)
          assume(hasAndroidJar, "dex analysis needs an Android SDK (ANDROID_HOME)")
          for command <- List("reachables", "data-flow") do
            val atomFile  = workDir / s"dex-$command.atom"
            val sliceFile = workDir / s"dex-$command.json"
            val result = Atom.run(Array(
              command,
              "-l",
              "dex",
              "-o",
              atomFile.pathAsString,
              "-s",
              sliceFile.pathAsString,
              dex.pathAsString
            ))
            withClue(s"atom $command -l dex: $result") { result.isRight shouldBe true }
      }

      "slice usages of native methods called from class files" in {
          val targets = usagesTargets("image", "jimple", nativeClassesDir, "native-classes")
          targets should contain("demo.NativeBridge.checksum:int(byte[],int)")
      }
  }

  "Atom.describeFailure" should {
      "name the exception class even when it has no message" in {
          val err = new FileSystemNotFoundException("Provider \"jrt\" not installed")
          val out = Atom.describeFailure(err)
          out.linesIterator.next() shouldBe
              "java.nio.file.FileSystemNotFoundException: Provider \"jrt\" not installed"
          Atom.describeFailure(new NullPointerException()).linesIterator.next() shouldBe
              "java.lang.NullPointerException"
          Atom.describeFailure(new RuntimeException("  ")).linesIterator.next() shouldBe
              "java.lang.RuntimeException"
      }

      "list the causes before the stack" in {
          val root  = new FileSystemNotFoundException("Provider \"jrt\" not installed")
          val err   = new RuntimeException("frontend failed", new IllegalStateException(null, root))
          val lines = Atom.describeFailure(err, frames = 3).linesIterator.toList
          lines.take(3) shouldBe List(
            "java.lang.RuntimeException: frontend failed",
            "Caused by: java.lang.IllegalStateException",
            "Caused by: java.nio.file.FileSystemNotFoundException: Provider \"jrt\" not installed"
          )
          lines.drop(3).size shouldBe 3
          lines.drop(3).foreach(_ should startWith("  at "))
      }

      "stop on a cause cycle" in {
          val a = new RuntimeException("a")
          val b = new RuntimeException("b", a)
          a.initCause(b)
          Atom.describeFailure(a, frames = 0).linesIterator.toList shouldBe List(
            "java.lang.RuntimeException: a",
            "Caused by: java.lang.RuntimeException: b"
          )
      }
  }
end JvmBytecodeWorkflowTests
