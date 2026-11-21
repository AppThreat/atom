package io.appthreat.atom

import better.files.File
import io.appthreat.atom.slicing.*
import io.appthreat.c2cpg.testfixtures.DataFlowCodeToCpgSuite
import io.appthreat.edg2atom.parser.EdgaRunner
import io.appthreat.x2cpg.passes.taggers.MemorySafetyFindingPass
import io.circe.Json
import io.circe.parser.parse
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.EdgeTypes
import io.shiftleft.codepropertygraph.generated.nodes.NewTag
import io.shiftleft.codepropertygraph.generated.DiffGraphBuilder
import io.shiftleft.passes.CpgPass
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The memory-safety findings and the usage and reachables slices of a C++ template print once for
  * the whole template, not once per instantiation.
  *
  * The fixture carries the two seeded shapes a template program has: a null dereference every
  * instantiation has (the use of `chain` comes before the only guard the method has), and an
  * out-of-bounds write only one instantiation has (`narrow_storage` writes through a pointer whose
  * element is as wide as `T`, which is four bytes of a one-byte allocation for `int` and a whole
  * byte for `char`). Each seeded shape has its correct look-alike beside it: a guard first, and a
  * buffer as wide as the element.
  *
  * The front ends differ in what they build from the same fixture. The EDG front end writes one
  * METHOD per instantiation, each carrying the `template` and `template-args` tags, so its findings
  * and slice entries group and name the instantiations. The CDT front end writes the generic method
  * alone, so there is nothing to group and its output keeps today's shape.
  */
class TemplateInstanceGroupingTests extends AnyWordSpec with Matchers:

  private val source =
      """#include <stdlib.h>
        |
        |template<typename T>
        |struct Chain
        |{
        |    T value;
        |    Chain<T> *next;
        |};
        |
        |/* The null dereference every instantiation has: the use comes before
        | * the only guard the method has. */
        |template<typename T>
        |void guarded_late(Chain<T> *chain)
        |{
        |    T v = chain->next->value;
        |    if (chain == nullptr)
        |        return;
        |    (void)v;
        |}
        |
        |/* The correct look-alike: the check first, the same walk afterwards. */
        |template<typename T>
        |void guarded_first(Chain<T> *chain)
        |{
        |    if (chain == nullptr)
        |        return;
        |    T v = chain->next->value;
        |    (void)v;
        |}
        |
        |/* The write only one instantiation has: the element is as wide as T,
        | * which is four bytes for int and one byte for char, and the
        | * allocation holds one byte. */
        |template<typename T>
        |T *narrow_storage(void)
        |{
        |    char *raw = (char *)malloc(1);
        |    T *wide = (T *)raw;
        |    wide[0] = T();
        |    return wide;
        |}
        |
        |/* The correct look-alike: the buffer is as wide as the element. */
        |template<typename T>
        |T *wide_enough(void)
        |{
        |    char *raw = (char *)malloc(sizeof(T));
        |    T *fine = (T *)raw;
        |    fine[0] = T();
        |    return fine;
        |}
        |
        |struct Record
        |{
        |    char tag[12];
        |    unsigned len;
        |};
        |
        |void call_everything(void)
        |{
        |    Chain<int> ci;
        |    ci.next = nullptr;
        |    guarded_late(&ci);
        |    guarded_first(&ci);
        |    Chain<char> cc;
        |    cc.next = nullptr;
        |    guarded_late(&cc);
        |    guarded_first(&cc);
        |    int *p = narrow_storage<int>();
        |    free(p);
        |    char *q = narrow_storage<char>();
        |    free(q);
        |    int *r = wide_enough<int>();
        |    free(r);
        |    char *s = wide_enough<char>();
        |    free(s);
        |}
        |""".stripMargin

  /** The non-template look-alike of the whole question: one function, one finding per defect, and
    * nothing that could name instances.
    */
  private val plainSource =
      """#include <stdlib.h>
        |
        |void release_twice(void)
        |{
        |    char *p = malloc(16);
        |    free(p);
        |    free(p);
        |}
        |""".stripMargin

  private def writeSource(dir: File, name: String, text: String): File =
    val src = (dir / "src").createDirectory()
    (src / name).writeText(text)
    src

  private def memorySafety(frontend: String, dir: File, floor: String): List[Json] =
    if frontend == "edg" then assume(EdgaRunner.locate().isDefined, "edga is not installed")
    val findings = dir / s"findings-$frontend.json"
    Atom.run(
      Array(
        "memory-safety",
        "-l",
        "c",
        "--c-frontend",
        frontend,
        "--with-data-deps",
        "--no-ast-cache",
        "--min-confidence",
        floor,
        "-o",
        (dir / s"$frontend.atom").pathAsString,
        "-s",
        findings.pathAsString,
        (dir / "src").pathAsString
      )
    ) match
      case Left(error) => fail(s"the $frontend front end run failed: $error")
      case Right(_) =>
          parse(findings.contentAsString).toOption.flatMap(_.asArray).toList.flatten
    end match
  end memorySafety

  private def usages(frontend: String, dir: File): List[Json] =
    if frontend == "edg" then assume(EdgaRunner.locate().isDefined, "edga is not installed")
    val slices = dir / s"usages-$frontend.json"
    Atom.run(
      Array(
        "usages",
        "-l",
        "c",
        "--c-frontend",
        frontend,
        "--no-ast-cache",
        "-o",
        (dir / s"$frontend-usages.atom").pathAsString,
        "-s",
        slices.pathAsString,
        (dir / "src").pathAsString
      )
    ) match
      case Left(error) => fail(s"the $frontend usages run failed: $error")
      case Right(_) =>
          parse(slices.contentAsString).toOption
              .flatMap(_.hcursor.downField("objectSlices").as[List[Json]].toOption)
              .getOrElse(Nil)
    end match
  end usages

  private def textOf(f: Json, field: String): String =
      f.hcursor.get[String](field).toOption.getOrElse("")

  private def intOf(f: Json, field: String): Int =
      f.hcursor.get[Int](field).toOption.getOrElse(0)

  private def instancesOf(f: Json): Option[List[String]] =
      f.hcursor.get[List[String]]("instances").toOption

  /** The rendered findings as (rule, line, column, confidence, instances), so an assertion speaks
    * about what a scorer reads.
    */
  private def tuplesOf(findings: List[Json])
    : Set[(String, Int, Int, String, Option[List[String]])] =
      findings
          .map(f =>
              (
                textOf(f, "rule"),
                intOf(f, "line"),
                intOf(f, "column"),
                textOf(f, "confidence"),
                instancesOf(f)
              )
          )
          .toSet

  /** The same tuples as [[tuplesOf]], every finding once, in rule, line and column order. */
  private def tupleListOf(findings: List[Json])
    : List[(String, Int, Int, String, Option[List[String]])] =
      findings
          .map(f =>
              (
                textOf(f, "rule"),
                intOf(f, "line"),
                intOf(f, "column"),
                textOf(f, "confidence"),
                instancesOf(f)
              )
          )
          .sortBy(t => (t._1, t._2, t._3))

  "the memory-safety findings of a template program" should {

      "print the write only one instantiation has once, naming that instantiation" in {
          File.usingTemporaryDirectory("atom-tmpl-group") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val findings = memorySafety("edg", dir, "medium")
            // the medium floor keeps exactly the out-of-bounds write of
            // narrow_storage<int>: medium confidence, one entry, the int
            // instantiation alone in the instances list
            findings should have size 1
            val f = findings.head.hcursor
            f.get[String]("rule").toOption shouldBe Some(MemorySafetyFindingPass.RuleIndexWrite)
            f.get[String]("cwe").toOption shouldBe Some("CWE-787")
            f.get[String]("kind").toOption shouldBe Some("oob-index-write")
            f.get[String]("severity").toOption shouldBe Some("high")
            f.get[String]("confidence").toOption shouldBe Some("medium")
            f.get[String]("file").toOption.exists(_.endsWith("template_bugs.cpp")) shouldBe true
            f.get[Int]("line").toOption shouldBe Some(39)
            f.get[Int]("column").toOption shouldBe Some(5)
            f.get[List[String]]("instances").toOption shouldBe Some(List("int"))
          }
      }

      "print the finding every instantiation has once, naming both" in {
          File.usingTemporaryDirectory("atom-tmpl-group") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val findings = memorySafety("edg", dir, "low")
            // the low floor adds the null dereferences (the use before the
            // guard in guarded_late, the unchecked allocations of
            // narrow_storage and wide_enough) and the allocations both return
            // unfreed. Every one of those exists in both instantiations and
            // prints once with both named; only the out-of-bounds write is
            // single-instantiation. The guard-first look-alike contributes
            // nothing, and the wide-enough look-alike carries no write past a
            // bound.
            tuplesOf(findings) shouldBe Set(
              (MemorySafetyFindingPass.RuleNullDeref, 15, 11, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleNullDeref, 16, 9, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleNullDeref, 39, 5, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleLeak, 40, 5, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleNullDeref, 49, 5, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleLeak, 50, 5, "low", Some(List("char", "int"))),
              (MemorySafetyFindingPass.RuleIndexWrite, 39, 5, "medium", Some(List("int")))
            )
            findings.foreach { f =>
                (textOf(f, "file") should endWith("template_bugs.cpp"))
            }
          }
      }

      "keep the CDT output on today's shape, with no instances to name" in {
          File.usingTemporaryDirectory("atom-tmpl-group") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val findings = memorySafety("cdt", dir, "low")
            // the CDT front end builds the generic method alone, so every
            // finding is one entry with no instances list, at the same lines
            // the EDG front end reports them at; the width of `wide` is
            // symbolic in the generic body, so the write stays silent there
            tuplesOf(findings) shouldBe Set(
              (MemorySafetyFindingPass.RuleNullDeref, 15, 11, "low", None),
              (MemorySafetyFindingPass.RuleNullDeref, 16, 9, "low", None),
              (MemorySafetyFindingPass.RuleNullDeref, 39, 5, "low", None),
              (MemorySafetyFindingPass.RuleLeak, 40, 5, "low", None),
              (MemorySafetyFindingPass.RuleNullDeref, 49, 5, "low", None),
              (MemorySafetyFindingPass.RuleLeak, 50, 5, "low", None)
            )
          }
      }

      "write the instances list into SARIF as a result property" in {
          File.usingTemporaryDirectory("atom-tmpl-group") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val out = dir / "findings.sarif"
            Atom.run(
              Array(
                "memory-safety",
                "-l",
                "c",
                "--c-frontend",
                "edg",
                "--with-data-deps",
                "--no-ast-cache",
                "--min-confidence",
                "medium",
                "--format",
                "sarif",
                "-o",
                (dir / "sarif.atom").pathAsString,
                "-s",
                out.pathAsString,
                (dir / "src").pathAsString
              )
            ).isRight shouldBe true
            val sarif = parse(out.contentAsString).toOption.get
            val results = sarif.hcursor.downField("runs").downArray.downField("results")
                .as[List[Json]].toOption.get
            results should have size 1
            results.head.hcursor.downField("properties").get[List[String]]("instances").toOption shouldBe
                Some(List("int"))
            results.head.hcursor.downField("properties").get[String]("cwe").toOption shouldBe
                Some("CWE-787")
          }
      }
  }

  "the memory-safety findings of a program without templates" should {

      "print exactly today's findings, byte for byte on the fields" in {
          Seq("cdt", "edg").foreach { frontend =>
            if frontend == "cdt" then ()
            else assume(EdgaRunner.locate().isDefined, "edga is not installed")
            File.usingTemporaryDirectory("atom-tmpl-plain") { dir =>
              writeSource(dir, "plain.c", plainSource)
              val findings = memorySafety(frontend, dir, "low")
              // a sequence, not a set: two findings folded into one would shorten it
              tupleListOf(findings) shouldBe List(
                (MemorySafetyFindingPass.RuleDoubleFree, 6, 10, "high", None),
                (MemorySafetyFindingPass.RuleDoubleFree, 7, 10, "high", None)
              )
              findings.foreach(f => instancesOf(f) shouldBe None)
            }
          }
      }
  }

  "the usages slice of a template program" should {

      "keep one entry for the instances of one template, naming both" in {
          File.usingTemporaryDirectory("atom-tmpl-usages") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val entries = usages("edg", dir)
            val named   = entries.map(e => (textOf(e, "fullName"), instancesOf(e)))
            val byPrefix = (prefix: String) =>
                named.collect { case (n, i) if n.startsWith(prefix) => (n, i) }
            // the instances of each template are one entry: the first
            // instance's spelling of the shared fields, the instantiations named
            byPrefix("guarded_late") shouldBe List(
              ("guarded_late:void(Chain<char>*)", Some(List("char", "int")))
            )
            byPrefix("narrow_storage") shouldBe List(
              ("narrow_storage:char*()", Some(List("char", "int")))
            )
            byPrefix("wide_enough") shouldBe List(
              ("wide_enough:char*()", Some(List("char", "int")))
            )
            // every other entry keeps today's shape
            byPrefix("call_everything") should have size 1
            byPrefix("call_everything").head._2 shouldBe None
            // the merged entry holds every instance's usages: the return value
            // the template instantiates is typed for int in one entry of the
            // union and for char in the other
            val merged = entries.find(e => textOf(e, "fullName").startsWith("narrow_storage")).get
            val usagesOfMerged =
                merged.hcursor.downField("usages").as[List[Json]].toOption.getOrElse(Nil)
            val types = usagesOfMerged.flatMap { u =>
                List("targetObj", "definedBy").flatMap(f =>
                    u.hcursor.downField(f).get[String]("typeFullName").toOption
                )
            }
            types.exists(_.contains("int")) shouldBe true
            types.exists(_.contains("char")) shouldBe true
          }
      }

      "keep the CDT usages on today's shape, with no instances to name" in {
          File.usingTemporaryDirectory("atom-tmpl-usages") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val entries = usages("cdt", dir)
            entries.filter(e => textOf(e, "fullName").startsWith("guarded_late")) should have size 1
            entries.foreach(e => instancesOf(e) shouldBe None)
          }
      }
  }

  /** The flow lesson the reachables command follows is driven by tags, so the reachables test tags
    * one source and one sink inside the template's instances: the `chain` parameter is the source,
    * the `chain->next` walk the sink.
    */
  private def tagLessonNodes(cpg: Cpg): Unit =
    val pass = new CpgPass(cpg):
      override def run(dst: DiffGraphBuilder): Unit =
        cpg.method.name("guarded_late").parameter.nameExact("chain").l.foreach { p =>
            dst.addEdge(p, NewTag().name("lesson-source").value("chain"), EdgeTypes.TAGGED_BY)
        }
        cpg.method.name("guarded_late")
            .call.nameExact("<operator>.indirectFieldAccess").codeExact("chain->next").l
            .foreach { c =>
                dst.addEdge(c, NewTag().name("lesson-sink").value("next"), EdgeTypes.TAGGED_BY)
            }
    pass.createAndApply()

  private def reachables(frontend: String, dir: File): List[Json] =
    if frontend == "edg" then assume(EdgaRunner.locate().isDefined, "edga is not installed")
    val atomFile = dir / s"$frontend-reach.atom"
    Atom.run(
      Array(
        "memory-safety",
        "-l",
        "c",
        "--c-frontend",
        frontend,
        "--with-data-deps",
        "--no-ast-cache",
        "-o",
        atomFile.pathAsString,
        "-s",
        (dir / s"findings-$frontend-reach.json").pathAsString,
        (dir / "src").pathAsString
      )
    ) match
      case Left(error) => fail(s"the $frontend front end run failed: $error")
      case Right(_) =>
          val odb = overflowdb.Config.withDefaults().withStorageLocation(atomFile.pathAsString)
          val cpg = io.shiftleft.codepropertygraph.cpgloading.CpgLoader.loadFromOverflowDb(
            io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig()
                .withOverflowConfig(odb)
                .doNotCreateIndexesOnLoad
          )
          try
            tagLessonNodes(cpg)
            val base = (dir / s"reach-$frontend").pathAsString
            slicing.ReachableSlicing.calculateReachableSliceAndPersist(
              cpg,
              ReachablesConfig(
                sourceTag = Seq("lesson-source"),
                sinkTag = Seq("lesson-sink"),
                sliceDepth = 5,
                includeCryptoFlows = false
              ),
              outputBasePath = base
            )
            parse(File(s"$base.json").contentAsString).toOption.flatMap(_.asArray).toList.flatten
          finally cpg.close()
    end match
  end reachables

  "the reachables slice of a template program" should {

      "print the flow every instantiation has once, naming both" in {
          File.usingTemporaryDirectory("atom-tmpl-reach") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val entries = reachables("edg", dir)
            // one flow per instantiation reaches the sink; the entries are one
            // per route: the sink the command collects as a call, and the same
            // walk it collects as the identifier read
            entries should have size 2
            entries.foreach { flow =>
              flow.hcursor.get[List[String]]("instances").toOption shouldBe
                  Some(List("char", "int"))
              val nodes = flow.hcursor.downField("flows").as[List[Json]].toOption.get
              // the source parameter, the walk to the sink
              nodes.map(n => textOf(n, "label")) shouldBe
                  List("METHOD_PARAMETER_IN", nodes.last.hcursor.get[String]("label").toOption.get)
              nodes.map(n => textOf(n, "code")) shouldBe
                  List("Chain<T> *chain", "chain->next")
            }
            entries.map(e =>
                e.hcursor.downField("flows").as[List[Json]].toOption.get.last.hcursor
                    .get[String]("label").toOption.get
            ) should contain theSameElementsAs List("CALL", "IDENTIFIER")
          }
      }

      "keep the CDT reachables on today's shape, with no instances to name" in {
          File.usingTemporaryDirectory("atom-tmpl-reach") { dir =>
            writeSource(dir, "template_bugs.cpp", source)
            val entries = reachables("cdt", dir)
            entries should have size 2
            entries.foreach(e => e.hcursor.get[List[String]]("instances").toOption shouldBe None)
          }
      }
  }
end TemplateInstanceGroupingTests
