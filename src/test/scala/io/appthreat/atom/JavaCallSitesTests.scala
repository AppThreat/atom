package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.atom.slicing.callsites.JavaCallSiteSource
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.codepropertygraph.generated.nodes.NewConfigFile
import io.shiftleft.semanticcpg.language.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.BatchedUpdate

/** `atom callsites` over Java fixtures: one record per external use site, asserted on (file, line,
  * module, path, kind, via) per shape, with the `builtin` flag for the JDK and the optional `purl`
  * from the Maven coordinates of the jar a class was resolved from.
  *
  * Each shape has a bad twin (must produce the record) and a good twin (must not): a project class
  * that shadows an external simple name produces none, and neither does the implicit
  * `java.lang.Object` base or the generated Lombok field. The dependency jars are committed stubs
  * under resources (java-callsites/lib), so the suite downloads nothing.
  */
class JavaCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  import JavaCallSitesTests.Rec

  private var workspace: BFile         = scala.compiletime.uninitialized
  private var records: List[Rec]       = Nil
  private var alsoCallsites: List[Rec] = Nil
  private var atomRan: Boolean         = false

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomJavaCallsites")
    val fixture = BFile(getClass.getResource("/java-callsites").toURI)
    val project = workspace / "proj"
    // the sources and the jars only: make/ holds the jars' build script and would shadow them
    (fixture / "src").copyTo(project / "src")
    (fixture / "lib").copyTo(project / "lib")
    val jars     = project / "lib"
    val atomFile = workspace / "proj.atom"
    // One process, two slices (atom#336): the usages run also writes the callsites slice.
    val usagesResult = Atom.run(
      ("usages" :: "--cache" :: "none" :: "-l" :: "java" ::
          "--frontend-args" :: s"inference-jar-paths=${jars.pathAsString}" ::
          "--also-callsites" :: (workspace / "also-callsites.json").pathAsString ::
          "-o" :: atomFile.pathAsString ::
          "-s" :: (workspace / "usages.json").pathAsString ::
          List(project.pathAsString)).toArray
    )
    atomRan = usagesResult.isRight
    if atomRan then
      // The dependency classpath the slice reads for the purl mapping is the frontend's own
      // resolution result; the stub jars stand in for it here the way Maven does for a real
      // project.
      seedDependencyClasspath(atomFile, jars)
      // A second command on the reused atom, whose tagger inputs match the recorded state, must
      // not tag again (atom#337).
      val sliceFile = workspace / "callsites.json"
      val result = Atom.run(
        ("callsites" :: "--cache" :: "none" :: "--reuse-atom" :: "-l" :: "java" ::
            "-o" :: atomFile.pathAsString ::
            "-s" :: sliceFile.pathAsString ::
            List(project.pathAsString)).toArray
      )
      withClue(s"atom callsites failed: "):
        result.isRight shouldBe true
      records = read(sliceFile)
      alsoCallsites = read(workspace / "also-callsites.json")
  end beforeAll

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def seedDependencyClasspath(atomFile: BFile, jars: BFile): Unit =
    val jarList =
        jars.listRecursively.filter(_.extension.contains(".jar")).map(_.pathAsString).toList.sorted
    val contents = ujson.write(ujson.Arr.from(jarList.map(jar => ujson.Str(jar))))
    val odbConfig = overflowdb.Config.withDefaults()
        .withStorageLocation(atomFile.pathAsString)
    val config = CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    val cpg    = CpgLoader.loadFromOverflowDb(config)
    try
      val diff = new BatchedUpdate.DiffGraphBuilder
      diff.addNode(
        NewConfigFile()
            .name(JavaCallSiteSource.DependencyClasspathFileName)
            .content(contents)
      )
      BatchedUpdate.applyDiff(cpg.graph, diff)
    finally cpg.close()

  private def read(sliceFile: BFile): List[Rec] =
      ujson.read(sliceFile.contentAsString).obj("callsites").arr
          .map(r =>
              Rec(
                r("file").str.split('/').last,
                r("line").num.toInt,
                r("col").num.toInt,
                r("module").str,
                r("path").str,
                r("kind").str,
                r("via").str,
                r.obj.get("builtin").map(_.bool).getOrElse(false),
                r.obj.get("purl").map(_.str).getOrElse("")
              )
          )
          .toList

  private def in(file: String): List[Rec] =
    if !atomRan then cancel("the Java fixture run failed")
    records.filter(_.file == file)

  private def expect(file: String, kind: String, expected: List[Rec]): Unit =
      in(file).filter(_.kind == kind).sortBy(r => (r.line, r.col, r.module, r.path)) shouldBe
          expected.sortBy(r => (r.line, r.col, r.module, r.path))

  "atom callsites for Java" should {

      "write the callsites slice from the usages command's single tagging run" in {
          assume(atomRan)
          alsoCallsites.map(r => (r.file, r.line, r.col, r.module, r.path, r.kind, r.via))
              .sortBy(_.toString()) shouldBe
              records.map(r => (r.file, r.line, r.col, r.module, r.path, r.kind, r.via))
                  .sortBy(_.toString())
      }

      "not tag again on a reused atom whose tagger inputs are unchanged" in {
          assume(atomRan)
          // the usages run tagged once and stored the state; the callsites run skipped the pass,
          // so the tags it produced are still there exactly once
          val odbConfig = overflowdb.Config.withDefaults().withStorageLocation(
            (workspace / "proj.atom").pathAsString
          )
          val cpg = CpgLoader.loadFromOverflowDb(
            CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
          )
          try
            cpg.tag.name("framework-input").size.shouldBe(1)
            cpg.configFile.name(Atom.TagStateFileName).size.shouldBe(1)
          finally cpg.close()
      }

      "record imports and qualified uses at their uses" in {
          expect(
            "Imports.java",
            "new",
            List(
              Rec(
                "Imports.java",
                8,
                -1,
                "web.Session",
                "",
                "new",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Imports.java",
            "call",
            List(
              Rec(
                "Imports.java",
                8,
                30,
                "web.Session",
                "header",
                "call",
                "type",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Imports.java",
                12,
                12,
                "web.Security",
                "http",
                "call",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Imports.java",
            "decltype",
            List(
              Rec(
                "Imports.java",
                7,
                -1,
                "web.Session",
                "",
                "decltype",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Imports.java",
                11,
                -1,
                "web.Security",
                "",
                "decltype",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
      }

      "record typed calls on the receiver's static type and chains on external returns" in {
          expect(
            "Calls.java",
            "new",
            List(
              Rec(
                "Calls.java",
                6,
                -1,
                "web.Security",
                "",
                "new",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Calls.java",
            "call",
            List(
              Rec(
                "Calls.java",
                9,
                9,
                "web.Security",
                "login",
                "call",
                "type",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Calls.java",
                9,
                21,
                "web.Session",
                "header",
                "call",
                "type",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Calls.java",
                10,
                14,
                "web.Security",
                "http",
                "call",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Calls.java",
                10,
                21,
                "web.Security",
                "login",
                "call",
                "type",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Calls.java",
            "decltype",
            List(
              Rec(
                "Calls.java",
                6,
                20,
                "web.Security",
                "",
                "decltype",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
      }

      "record declaration types at the type's position" in {
          expect(
            "DeclTypes.java",
            "decltype",
            List(
              Rec(
                "DeclTypes.java",
                8,
                20,
                "web.Security",
                "",
                "decltype",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "DeclTypes.java",
                10,
                -1,
                "java.io.IOException",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec(
                "DeclTypes.java",
                10,
                -1,
                "java.util.List",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec(
                "DeclTypes.java",
                10,
                -1,
                "java.util.Map",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec(
                "DeclTypes.java",
                11,
                -1,
                "java.util.List",
                "",
                "decltype",
                "binding",
                builtin = true
              )
            )
          )
          expect(
            "DeclTypes.java",
            "call",
            List(
              Rec(
                "DeclTypes.java",
                11,
                31,
                "java.util.List",
                "of",
                "call",
                "binding",
                builtin = true
              )
            )
          )
      }

      "record class literals, method references, static fields and enum constants" in {
          expect(
            "Literals.java",
            "ref",
            List(
              Rec(
                "Literals.java",
                7,
                18,
                "web.Security",
                "",
                "ref",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Literals.java",
                9,
                16,
                "web.Security",
                "VERSION",
                "ref",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec(
                "Literals.java",
                10,
                25,
                "web.Security",
                "Method",
                "ref",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              ),
              Rec("Literals.java", 10, 25, "web.Security$Method", "GET", "ref", "binding")
            )
          )
          expect(
            "Literals.java",
            "methodref",
            List(
              Rec(
                "Literals.java",
                8,
                53,
                "web.Security",
                "http",
                "methodref",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Literals.java",
            "decltype",
            List(
              Rec(
                "Literals.java",
                7,
                -1,
                "java.lang.Class",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec(
                "Literals.java",
                8,
                -1,
                "java.util.function.Function",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec(
                "Literals.java",
                9,
                -1,
                "java.lang.String",
                "",
                "decltype",
                "binding",
                builtin = true
              ),
              Rec("Literals.java", 10, -1, "web.Security$Method", "", "decltype", "binding")
            )
          )
      }

      "record annotations at every site, java.lang included" in {
          // the class's own annotation (proj.RestController) is project code, not a use
          expect(
            "Annotations.java",
            "annotation",
            List(
              Rec(
                "Annotations.java",
                8,
                3,
                "java.lang.Override",
                "",
                "annotation",
                "binding",
                builtin = true
              ),
              Rec(
                "Annotations.java",
                13,
                15,
                "web.RequestParam",
                "",
                "annotation",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
      }

      "record extends and implements, and no implicit java.lang.Object" in {
          val heritage = in("Heritage.java").filter(r =>
              r.kind == "extends" || r.kind == "implements"
          ).map(r => (r.line, r.module, r.kind)).sortBy(_._2)
          heritage shouldBe List(
            (6, "web.Handler", "implements"),
            (6, "web.Security", "extends")
          )
          records.exists(_.module == "java.lang.Object") shouldBe false
      }

      "record no use of the project class that shadows an external simple name" in {
          in("Shadow.java").filter(_.module.startsWith("web.")) shouldBe Nil
      }

      "record calls in field initialisers" in {
          expect(
            "Initializers.java",
            "new",
            List(
              Rec(
                "Initializers.java",
                7,
                -1,
                "web.Session",
                "",
                "new",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
          expect(
            "Initializers.java",
            "call",
            List(
              Rec(
                "Initializers.java",
                6,
                51,
                "web.Security",
                "http",
                "call",
                "binding",
                purl = "pkg:maven/com.example/stub-web@1.0.0"
              )
            )
          )
      }

      "record the Lombok-generated logger's calls and no record for the generated field" in {
          in("Lombok.java").filter(_.module.startsWith("org.slf4j")) shouldBe List(
            Rec("Lombok.java", 11, 9, "org.slf4j.Logger", "info", "call", "type")
          )
      }

      "map a class to the artifact of the jar that carries it, and no further" in {
          val purls = in("Purl.java").filter(_.kind == "call")
              .map(r => (r.module, r.purl)).sortBy(_._1)
          purls shouldBe List(
            ("dup.Shared", ""),
            ("shaded.com.example.biz.Util", "pkg:maven/com.example/shaded-biz@2.0.0"),
            ("web.Security", "pkg:maven/com.example/stub-web@1.0.0")
          )
          // dup.Shared is carried by two artifacts: the record stays, its purl stays unmapped,
          // and the JDK's classes carry no purl at all
          records.filter(r => r.builtin && r.purl.nonEmpty) shouldBe Nil
      }
  }
end JavaCallSitesTests

object JavaCallSitesTests:

  /** The fields the assertions need, one per record. */
  private final case class Rec(
    file: String,
    line: Int,
    col: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean = false,
    purl: String = ""
  )
