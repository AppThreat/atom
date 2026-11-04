package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{CallSitesMetadata, ProgramCallSites}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** `CallSiteRecord.dedupe` and the JSON a slice is written as. The slice must give the same bytes
  * for the same input, so the record that wins a group may not depend on the order the group came
  * in (the order a source walks the graph in follows node ids, which the streaming passes assign in
  * completion order), and the fields are written in one order.
  *
  * The records are the ones the sources write for the fixtures: `base` is the `@Get` decorator of
  * `js-usages/decorators` (src/cats.ts line 14), whose METHOD and MEMBER annotations differ in the
  * enclosing method, as the two node orders of the slice showed. Each tie test takes two records
  * that differ only in the field the winner used to ignore, and runs them in both orders.
  */
class CallSiteRecordTests extends AnyWordSpec with Matchers:

  private val base = CallSiteRecord(
    "src/cats.ts",
    Integer.valueOf(14),
    Integer.valueOf(2),
    "src/cats.ts::program:CatsController:findOne",
    "@nestjs/common",
    "Get",
    "decorator",
    "name",
    "Get"
  )

  /** `dedupe` of `a` and `b` in both orders: the same single record each time. */
  private def winner(a: CallSiteRecord, b: CallSiteRecord, byMember: Boolean = false) =
    val ab = CallSiteRecord.dedupe(List(a, b), byMember)
    val ba = CallSiteRecord.dedupe(List(b, a), byMember)
    withClue(s"in the order ${a.method}/${b.method}, then the reverse: ")(ab shouldBe ba)
    ab should have size 1
    ab.head

  "CallSiteRecord.dedupe" should {

      "keep the same record of two that differ only in the enclosing method" in {
          // the METHOD annotation names the decorated method, the MEMBER annotation the scope
          // around the class; the smaller method wins, in either order
          val member = base.copy(method = "src/cats.ts::program")
          winner(base, member) shouldBe member
      }

      "prefer the record with no enclosing method to one with a method" in {
          // text a macro definition spells sits in no method (CCallSiteSource.record)
          val spelled = base.copy(method = "")
          winner(base, spelled) shouldBe spelled
      }

      "keep the same record of two kinds with one rank" in {
          // `extends` and `implements` rank alike (CallSiteRecord.kindRank); the kind decides
          val extendsRecord    = base.copy(kind = "extends", resolvedMethod = "events:EventEmitter")
          val implementsRecord = extendsRecord.copy(kind = "implements")
          winner(extendsRecord, implementsRecord) shouldBe extendsRecord
      }

      "keep the same record of two kinds that have no rank" in {
          val macroRecord = base.copy(kind = "macro")
          val otherRecord = base.copy(kind = "attribute")
          winner(macroRecord, otherRecord) shouldBe otherRecord
      }

      "let a use of a binding beat an import record of the same site" in {
          // an import that binds nothing is its own site (decision 0002, Q1), the whole
          // statement, so it meets no other record at one position; if one ever does (a use
          // written on the import's line), the use is the more specific kind and wins, in either
          // order (atom#367)
          val importRecord = base.copy(kind = "import", path = "", resolvedMethod = "")
          val use          = base.copy(kind = "ref", path = "", resolvedMethod = "")
          winner(importRecord, use) shouldBe use
      }

      "keep the same record of two that differ only in the declaring module" in {
          val plain = base.copy(kind = "call", via = "type", resolvedMethod = "java.util.Map.get")
          val declared = plain.copy(declaringModule = "java.util.Map")
          winner(plain, declared) shouldBe plain
      }

      "keep the same record of two that differ only in the purl" in {
          val bare  = base.copy(kind = "new", via = "binding", resolvedMethod = "express")
          val named = bare.copy(purl = "pkg:npm/express@4.19.2")
          winner(bare, named) shouldBe bare
      }

      "keep the same record of two that differ only in builtin" in {
          val project = base.copy(kind = "call", via = "binding", resolvedMethod = "os.getcwd")
          val builtin = project.copy(builtin = true)
          winner(project, builtin) shouldBe project
      }

      "keep the same record of two that differ only in the method, one site per member" in {
          // bytecode: one line and no column, a site per member
          val inLambda = base.copy(method = "me.app.App.lambda$run$0:void()", col = -1)
          val inRun    = inLambda.copy(method = "me.app.App.run:void()")
          winner(inLambda, inRun, byMember = true) shouldBe inLambda
      }

      "give the same winner for every order of a group that ties on everything the old rule read" in {
          val group = List(
            base,
            base.copy(method = "src/cats.ts::program"),
            base.copy(method = ""),
            base.copy(kind = "decorator", declaringModule = "x"),
            base.copy(purl = "pkg:npm/%40nestjs/common@10.0.0"),
            base.copy(builtin = true)
          )
          val expected = CallSiteRecord.dedupe(group)
          expected should have size 1
          group.permutations.foreach(order => CallSiteRecord.dedupe(order) shouldBe expected)
      }

      "still prefer the more specific kind, whatever the later fields say" in {
          val call = base.copy(kind = "call", method = "z", purl = "pkg:npm/a@1")
          val ref  = base.copy(kind = "ref", method = "", purl = "")
          winner(call, ref) shouldBe call
      }

      "still prefer the smaller path, then via, then resolved method" in {
          val a = base.copy(path = "Get", via = "type", resolvedMethod = "b")
          winner(a, a.copy(path = "Post")) shouldBe a
          winner(a, a.copy(via = "name")) shouldBe a.copy(via = "name")
          winner(a, a.copy(resolvedMethod = "a")) shouldBe a.copy(resolvedMethod = "a")
      }

      "order the winners by position, one per site" in {
          val later   = base.copy(line = Integer.valueOf(15))
          val earlier = base.copy(line = Integer.valueOf(9), module = "typeorm")
          CallSiteRecord.dedupe(List(later, earlier, base)) shouldBe List(earlier, base, later)
      }
  }

  "the callsites JSON" should {

      "write a record's fields in one order and leave an empty purl out" in {
          ProgramCallSites(List(base)).toJson shouldBe
              """{"callsites":[{"file":"src/cats.ts","line":14,"col":2,""" +
              """"method":"src/cats.ts::program:CatsController:findOne",""" +
              """"module":"@nestjs/common","path":"Get","kind":"decorator","via":"name",""" +
              """"resolvedMethod":"Get","builtin":false,"declaringModule":""}]}"""
          ProgramCallSites(List(base.copy(purl = "pkg:npm/%40nestjs/common@10.0.0"))).toJson should
              include(""""declaringModule":"","purl":"pkg:npm/%40nestjs/common@10.0.0"}""")
      }

      "write the bytecode metadata after the records, its fields in one order" in {
          val metadata = CallSitesMetadata(List("me.app"), false, "jar-manifest", 0, 0, "source", 1)
          ProgramCallSites(Nil, Some(metadata)).toJson shouldBe
              """{"callsites":[],"metadata":{"appPrefixes":["me.app"],"appAll":false,""" +
              """"boundarySource":"jar-manifest","syntheticLineTables":0,""" +
              """"methodsWithLineTables":0,"lineQuality":"source","sourceFileGuesses":1}}"""
      }
  }
end CallSiteRecordTests
