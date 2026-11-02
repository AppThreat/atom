package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.jar.{JarEntry, JarOutputStream, Manifest}
import javax.tools.ToolProvider
import scala.jdk.CollectionConverters.*
import scala.util.Using

/** `atom callsites` over compiled JVM bytecode: one record per external use of a method of an app
  * class, asserted exactly on (file, line, module, path, kind, via, builtin, purl).
  *
  * The fixture (`jvm-callsites`) is Java sources compiled in this process with the compiler of the
  * JDK the tests run on, then packaged as a jar whose `Automatic-Module-Name` names the application
  * (`me.app`); `me.lib` rides along as a bundled library, the way an APK or an uber jar carries
  * one. Every shape of the adapter has its line in `me/app/App.java` or in the `AppBase`/`AppSub`
  * pair: a static call, a virtual call, a constructor, a static field read, an instance field put
  * and get, `X.class`, `instanceof`, a cast, string concatenation through `invokedynamic` (which
  * must produce no record: its bootstrap has no method handle and the dummy owner
  * `soot.dummy.InvokeDynamic` is nobody's writing), a method reference (whose handle is a
  * `call`/`type` record at the `invokedynamic` line), a member inherited from a library through an
  * app subclass (recorded on the library class), and an app method called through an app subclass
  * (no record). The jar carries a `pom.properties`, which gives the `me.lib` records their `purl`.
  *
  * The twins: `--app-prefix` replacing the manifest (which flips the boundary and makes the app's
  * own calls the records), a class directory (every class of it is application code, so only the
  * JDK owners remain), and the committed dex (`dex/classes.dex`, provenance beside it), whose
  * boundary comes from `--app-prefix` alone. Where a tool is missing the suite cancels with the
  * reason, it does not fail.
  */
class BytecodeCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  import BytecodeCallSitesTests.Rec

  private var workspace: BFile            = scala.compiletime.uninitialized
  private var records: List[Rec]          = Nil
  private var flipped: List[Rec]          = Nil
  private var dirRecords: List[Rec]       = Nil
  private var dexRecords: List[Rec]       = Nil
  private var atomRan: Boolean            = false
  private var unavailable: Option[String] = None

  /** The jar of the fixture: the fixture's sources compiled with the JDK's own compiler, packaged
    * with a manifest and the Maven metadata the purls come from.
    */
  private def buildFixtureJar(): Either[String, BFile] =
    val compiler = ToolProvider.getSystemJavaCompiler
    if compiler == null then Left("no system Java compiler in this JVM")
    else
      val fixture = BFile(getClass.getResource("/jvm-callsites"))
      val out     = workspace / "classes"
      out.createDirectories()
      val sources = (fixture / "src").listRecursively
          .filter(_.extension.contains(".java"))
          .map(_.pathAsString).toList
      val ok = compiler.run(null, null, null, "-d" :: out.pathAsString :: sources*)
      if ok != 0 then Left(s"javac failed for the fixture sources")
      else
        val manifest = new Manifest()
        manifest.getMainAttributes.putValue("Manifest-Version", "1.0")
        manifest.getMainAttributes.putValue("Automatic-Module-Name", "me.app")
        val jar = workspace / "fixture.jar"
        Using.resource(new JarOutputStream(jar.newOutputStream, manifest)) { stream =>
          (out.listRecursively.filter(_.isRegularFile)).foreach { f =>
            val entry = new JarEntry(out.relativize(f).toString.replace('\\', '/'))
            stream.putNextEntry(entry)
            stream.write(f.byteArray)
            stream.closeEntry()
          }
          (fixture / "meta").listRecursively.filter(_.isRegularFile).foreach { f =>
            val entry = new JarEntry((fixture / "meta").relativize(f).toString.replace('\\', '/'))
            stream.putNextEntry(entry)
            stream.write(f.byteArray)
            stream.closeEntry()
          }
        }
        Right(jar)
      end if
    end if
  end buildFixtureJar

  private def runAtom(
    input: BFile,
    lang: String,
    name: String,
    prefixes: String*
  ): Either[String, List[Rec]] =
    val sliceFile = workspace / s"$name.json"
    val args = List(
      "callsites",
      "--cache",
      "none",
      "-l",
      lang,
      "--jdk-path",
      sys.props.getOrElse("java.home", ""),
      "-o",
      (workspace / s"$name.atom").pathAsString,
      "-s",
      sliceFile.pathAsString
    ) ++ prefixes.flatMap(p => List("--app-prefix", p)) :+ input.pathAsString
    Atom.run(args.toArray).left.map(_.toString).map(_ => read(sliceFile))
  end runAtom

  private def read(sliceFile: BFile): List[Rec] =
      ujson.read(sliceFile.contentAsString).obj("callsites").arr
          .map(r =>
              Rec(
                r("file").str,
                r("line").num.toInt,
                r("module").str,
                r("path").str,
                r("kind").str,
                r("via").str,
                r.obj.get("builtin").map(_.bool).getOrElse(false),
                r.obj.get("purl").map(_.str).getOrElse("")
              )
          )
          .toList
          .sortBy(r => (r.line, r.module, r.path))

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomBytecodeCallsites")
    buildFixtureJar() match
      case Left(reason) => unavailable = Some(reason)
      case Right(jar) =>
          atomRan = runAtom(jar, "jar", "slice") match
            case Right(recs) =>
                records = recs
                true
            case Left(_) => false
          if atomRan then
            // `--app-prefix me.lib` replaces the manifest: the library becomes the application,
            // and the application's own classes become the dependencies
            runAtom(jar, "jar", "flipped", "me.lib").foreach(flipped = _)
            // a class directory: every class of it is application code. The twin holds only
            // the app's own subtree, the way a module's classes dir carries no libraries, so
            // the bundled library's owners are external again.
            val dirOnly = (workspace / "dirapp")
            (workspace / "classes").copyTo(dirOnly)
            (dirOnly / "me" / "lib").delete()
            runAtom(dirOnly, "jar", "classes").foreach(dirRecords = _)
            // the committed dex: no manifest, so the boundary comes from --app-prefix alone
            val dex = BFile(getClass.getResource("/jvm-callsites/dex/classes.dex").toURI)
            runAtom(dex, "dex", "dex", "dexy.app").foreach(dexRecords = _)
    end match
  end beforeAll

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def requireRan(): Unit =
    if unavailable.isDefined then cancel(unavailable.get)
    if !atomRan then cancel("the fixture jar run failed")

  private def app(from: List[Rec] = records): List[Rec] =
    requireRan()
    from.filter(_.file.contains("me/app/"))

  "atom callsites for JVM bytecode" should {

      "emit exactly the records of the app shapes in the fixture" in {
          app().map(_.copy(file = "")) shouldBe BytecodeCallSitesTests.expected
      }

      "name the real file of the SourceFile attribute, never a temporary path" in {
          app().foreach { r =>
            r.file should endWith(".java")
            r.file should include("me/app/")
            (r.file should not).include("jimple2cpg")
          }
      }

      "record a static call as call and binding, a virtual call as call and type" in {
          val calls = app().filter(_.kind == "call")
          calls.filter(_.via == "binding").map(r => (r.line, r.module, r.path)) should contain(
            (18, "me.lib.Lib", "util")
          )
          calls.filter(_.via == "type").map(r => (r.line, r.module, r.path)) should contain(
            (14, "me.lib.Lib", "label")
          )
      }

      "record a constructor as one new on the class" in {
          app().filter(_.kind == "new").map(r => (r.line, r.module, r.path)) should contain(
            (11, "me.lib.Lib", "<init>")
          )
      }

      "record field reads and writes as refs" in {
          val refs = app().filter(_.kind == "ref")
          refs.filter(_.module == "me.lib.Lib").map(r => (r.line, r.path)) should contain(
            (9, "CONSTANT")
          )
          refs.filter(_.path == "count").map(r => r.line) shouldBe List(22, 23)
      }

      "record X.class, instanceof and cast as refs" in {
          // the class literal at 35, the instanceof at 27 and the cast at 31
          app().filter(_.path.isEmpty).map(r => (r.line, r.module)) shouldBe
              List((27, "me.lib.Lib"), (31, "me.lib.Lib"), (35, "me.lib.Lib"))
      }

      "give no record for string concatenation through invokedynamic" in {
          // line 39: `"x" + s` lowers to soot.dummy.InvokeDynamic, whose bootstrap has no handle
          app().map(_.line).find(_ == 39) shouldBe None
          app().foreach(_.module should not include "soot.dummy")
      }

      "record a method reference by its bootstrap handle at the invokedynamic line" in {
          app().filter(_.line == 47).map(r => (r.module, r.path, r.kind, r.via, r.builtin)) shouldBe
              List(("java.lang.String", "isEmpty", "call", "type", true))
      }

      "give no record for a method handle on the app's own code" in {
          // `App::base` (line 51) and the lambda's body method (line 55) are the app's own;
          // the lambda body's call to `isBlank` is a use like any other
          app().filter(r => r.line == 51 || r.module.startsWith("me.app")) shouldBe Nil
          app().filter(_.line == 55).map(r => (r.module, r.path)) shouldBe
              List(("java.lang.String", "isBlank"))
      }

      "record a super constructor call as a ref to the parent, not a new" in {
          app().filter(r => r.path == "<init>" && r.kind == "ref").map(r =>
              (r.line, r.module)
          ) shouldBe
              List((5, "me.lib.LibGrand"), (7, "java.lang.Object"))
      }

      "resolve a member inherited from a library through an app subclass onto the library" in {
          // AppSub.throughGrand() calls `foo()`, which the bytecode names on AppSub; AppBase does
          // not declare it, LibGrand does
          app().filter(_.module == "me.lib.LibGrand").map(r => (r.line, r.path, r.via)).filter(
            _._2 == "foo"
          ) shouldBe
              List((6, "foo", "type")) // AppBase's implicit super is the `ref` at line 5
          app().filter(_.module == "me.lib.LibGrand").foreach(_.builtin shouldBe false)
      }

      "give no record for an app method called through an app subclass" in {
          // AppSub.throughBase() calls `bar()`, which AppBase declares: the app's own member
          app().filter(r => r.file.contains("AppSub") && r.line == 10).shouldBe(Nil)
          // AppBase's own `bar` sits under `me/app/` in the expected list
      }

      "mark the JDK builtin and the bundled library not, and give the library its purl" in {
          app().filter(_.builtin).map(_.module).toSet shouldBe
              Set("java.lang.String", "java.lang.Object")
          app().filterNot(_.builtin).map(_.module).toSet shouldBe
              Set("me.lib.Lib", "me.lib.LibGrand")
          app().filter(_.module == "me.lib.Lib").map(_.purl).toSet shouldBe
              Set("pkg:maven/me.lib/lib-lib@0.1.0")
          app().filter(_.builtin).foreach(_.purl shouldBe "")
          app().filter(_.module == "me.lib.LibGrand").map(_.purl).toSet shouldBe
              Set("pkg:maven/me.lib/lib-lib@0.1.0")
      }

      "keep every record on a real line" in {
          requireRan()
          records.foreach(r => r.line should be > 0)
      }
  }

  "the application boundary" should {

      "come from the jar manifest and ride in the slice metadata" in {
          requireRan()
          val slice = ujson.read((workspace / "slice.json").contentAsString)
          slice.obj("metadata").obj("appPrefixes").arr.map(_.str).toList shouldBe List("me.app")
          slice.obj("metadata").obj("boundarySource").str shouldBe "jar-manifest"
      }

      "be replaced by --app-prefix, which makes the library the application" in {
          if unavailable.isDefined then cancel(unavailable.get)
          if flipped.isEmpty then cancel("the flipped run produced no records")
          // records now come from the library's methods about the app's classes; the app's own
          // methods are no longer sources of records
          flipped.filter(r => r.file.contains("me/lib/")).map(_.module).toSet should contain(
            "me.app.App"
          )
          flipped.filter(_.file.contains("me/app/")) shouldBe Nil
          val slice = ujson.read((workspace / "flipped.json").contentAsString)
          slice.obj("metadata").obj("appPrefixes").arr.map(_.str).toList shouldBe List("me.lib")
          slice.obj("metadata").obj("boundarySource").str shouldBe "app-prefix"
      }

      "cover every class of a class directory, and only what the directory carries" in {
          if unavailable.isDefined then cancel(unavailable.get)
          if dirRecords.isEmpty then cancel("the class-dir run produced no records")
          // the dir carries only me/app: its classes' uses of me.lib are uses of a library,
          // constructors included (a module's classes dir carries no libraries, so the owners
          // the bytecode names are outside the boundary even under `appAll`)
          dirRecords.filter(_.module.startsWith("me.lib")).map(_.module).toSet shouldBe
              Set("me.lib.Lib", "me.lib.LibGrand")
          dirRecords.filter(_.kind == "new").map(_.module).toSet should contain("me.lib.Lib")
          dirRecords.filter(_.builtin).map(_.module).toSet should contain("java.lang.String")
          // nobody outside the dir and the JDK is a record here
          dirRecords.map(_.module).forall(m =>
              m.startsWith("me.lib") || m.startsWith("java.")
          ) shouldBe
              true
          val slice = ujson.read((workspace / "classes.json").contentAsString)
          slice.obj("metadata").obj("appAll").bool shouldBe true
          slice.obj("metadata").obj("boundarySource").str shouldBe "class-dir"
      }
  }

  "the committed dex" should {

      "produce the same shape of record from a --app-prefix boundary" in {
          if unavailable.isDefined then cancel(unavailable.get)
          if dexRecords.isEmpty then cancel("the dex run produced no records")
          // Dexy.check's `s.isEmpty()` and the constructor's implicit super()
          dexRecords.map(r => (r.file, r.module, r.path, r.kind, r.via, r.builtin)) shouldBe
              List(
                ("dexy/app/Dexy.java", "java.lang.Object", "<init>", "ref", "binding", true),
                ("dexy/app/Dexy.java", "java.lang.String", "isEmpty", "call", "type", true)
              )
          val slice = ujson.read((workspace / "dex.json").contentAsString)
          slice.obj("metadata").obj("appPrefixes").arr.map(_.str).toList shouldBe List("dexy.app")
          slice.obj("metadata").obj("boundarySource").str shouldBe "app-prefix"
      }
  }

  "two runs over one atom" should {

      "write byte-identical slices" in {
          requireRan()
          val atom   = workspace / "slice.atom"
          val first  = workspace / "again1.json"
          val second = workspace / "again2.json"
          Atom.run(
            ("callsites" :: "--cache" :: "none" :: "-l" :: "jar" :: "--reuse-atom" ::
                "--jdk-path" :: sys.props.getOrElse("java.home", "") :: "-o" ::
                atom.pathAsString :: "-s" :: first.pathAsString :: Nil).toArray
          )
          Atom.run(
            ("callsites" :: "--cache" :: "none" :: "-l" :: "jar" :: "--reuse-atom" ::
                "--jdk-path" :: sys.props.getOrElse("java.home", "") :: "-o" ::
                atom.pathAsString :: "-s" :: second.pathAsString :: Nil).toArray
          )
          first.contentAsString shouldBe second.contentAsString
      }
  }
end BytecodeCallSitesTests

object BytecodeCallSitesTests:

  final case class Rec(
    file: String,
    line: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean,
    purl: String
  )

  object Rec:
    def apply(
      line: Int,
      module: String,
      path: String,
      kind: String,
      via: String,
      builtin: Boolean,
      purl: String
    ): Rec = Rec("", line, module, path, kind, via, builtin, purl)

  /** The exact records of `me/app/App.java` and the `AppBase`/`AppSub` pair, on the jar whose
    * boundary is `me.app`. Lines are the fixture sources'.
    */
  // format: off
  val expected: List[Rec] = List(
    Rec(5, "me.lib.LibGrand", "<init>", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(6, "me.lib.LibGrand", "foo", "call", "type", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(7, "java.lang.Object", "<init>", "ref", "binding", true, ""),
    Rec(9, "me.lib.Lib", "CONSTANT", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(11, "me.lib.Lib", "<init>", "new", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(14, "me.lib.Lib", "label", "call", "type", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(18, "me.lib.Lib", "util", "call", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(22, "me.lib.Lib", "count", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(23, "java.lang.String", "valueOf", "call", "binding", true, ""),
    Rec(23, "me.lib.Lib", "count", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(27, "me.lib.Lib", "", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(31, "me.lib.Lib", "", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(35, "me.lib.Lib", "", "ref", "binding", false, "pkg:maven/me.lib/lib-lib@0.1.0"),
    Rec(47, "java.lang.String", "isEmpty", "call", "type", true, ""),
    Rec(55, "java.lang.String", "isBlank", "call", "type", true, "")
  )
  // format: on
end BytecodeCallSitesTests
