package io.appthreat.atom

import io.circe.Json
import io.circe.syntax.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The grouping of memory-safety findings by template instance, on hand-rendered findings: no front
  * end runs.
  */
class MemorySafetyGroupingUnitTests extends AnyWordSpec with Matchers:

  private def finding(confidence: String, line: Int = 7, flow: String = "p"): Json =
      Json.obj(
        "rule"       -> "MS-NULL-001".asJson,
        "file"       -> "src/a.cpp".asJson,
        "line"       -> line.asJson,
        "column"     -> 3.asJson,
        "confidence" -> confidence.asJson,
        "message"    -> "p: null dereference".asJson,
        "flow"       -> List(Json.obj("code" -> flow.asJson)).asJson
      )

  private def instance(args: String) =
      Some(MemorySafetyCommands.TemplateInstance("Box.get", 3, 1, args))

  /** Groups findings that carry no extra value, the way the command groups them. */
  private def group(rendered: List[(Json, Option[MemorySafetyCommands.TemplateInstance])]) =
      MemorySafetyCommands.groupTemplateInstances(rendered.map((j, i) => (j, i, ()))).map(_._1)

  private def instancesOf(json: Json): Option[List[String]] =
      json.hcursor.get[List[String]]("instances").toOption

  private def confidenceOf(json: Json): String =
      json.hcursor.get[String]("confidence").getOrElse("")

  "the finding grouping" should {

      "print a finding every instance has once, naming each instance" in {
          val grouped = group(
            List(finding("high") -> instance("int"), finding("high") -> instance("char"))
          )
          grouped.map(instancesOf) shouldBe List(Some(List("char", "int")))
      }

      "keep findings that differ in confidence apart" in {
          val grouped = group(
            List(finding("medium") -> instance("int"), finding("high") -> instance("char"))
          )
          grouped.map(g => (confidenceOf(g), instancesOf(g))) shouldBe List(
            ("medium", Some(List("int"))),
            ("high", Some(List("char")))
          )
      }

      "keep findings whose flows differ apart" in {
          val grouped = group(
            List(
              finding("high", flow = "p") -> instance("int"),
              finding("high", flow = "q") -> instance("char")
            )
          )
          grouped should have size 2
      }

      "name no instance for an instance without arguments" in {
          val grouped = group(
            List(finding("high") -> instance(""), finding("high") -> instance("int"))
          )
          grouped.map(instancesOf) shouldBe List(Some(List("int")))
      }

      "keep the extra value of the first finding of a group" in {
          val grouped = MemorySafetyCommands.groupTemplateInstances(
            List(
              (finding("high"), instance("int"), "first"),
              (finding("high"), instance("char"), "second")
            )
          )
          grouped.map(_._2) shouldBe List("first")
      }

      "print findings outside a template as rendered, each on its own" in {
          val plain = finding("high")
          val grouped =
              group(List(plain -> None, plain -> None))
          grouped shouldBe List(plain, plain)
      }
  }

  "the finding fingerprint" should {

      def inputs(function: String = "copy", path: String = "src/a.c") =
          MemorySafetyCommands.FingerprintInputs("MS-BOUND-002", function, path, "memcpy")

      def sha256(text: String): String =
          java.security.MessageDigest.getInstance("SHA-256")
              .digest(text.getBytes("UTF-8"))
              .map(b => f"${b & 0xff}%02x")
              .mkString

      def fingerprintOf(json: Json): String =
          json.hcursor.get[String]("fingerprint").getOrElse("")

      "hash rule, function, path, apis and the occurrence among findings sharing them" in {
          val fingerprinted = MemorySafetyCommands.withFingerprints(
            List(
              finding("high", line = 7)  -> inputs(),
              finding("high", line = 9)  -> inputs(),
              finding("high", line = 12) -> inputs(function = "other")
            )
          )
          fingerprinted.map(fingerprintOf) shouldBe List(
            sha256("MS-BOUND-002|copy|src/a.c|memcpy|0"),
            sha256("MS-BOUND-002|copy|src/a.c|memcpy|1"),
            sha256("MS-BOUND-002|other|src/a.c|memcpy|0")
          )
      }

      "place the fingerprint right after the flow" in {
          val fingerprinted =
              MemorySafetyCommands.withFingerprints(List(finding("high") -> inputs()))
          fingerprinted.head.asObject.map(_.keys.toList) shouldBe Some(
            List(
              "rule",
              "file",
              "line",
              "column",
              "confidence",
              "message",
              "flow",
              "fingerprint"
            )
          )
      }
  }
end MemorySafetyGroupingUnitTests
