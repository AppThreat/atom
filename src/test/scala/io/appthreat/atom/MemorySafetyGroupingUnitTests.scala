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

  private def instancesOf(json: Json): Option[List[String]] =
      json.hcursor.get[List[String]]("instances").toOption

  private def confidenceOf(json: Json): String =
      json.hcursor.get[String]("confidence").getOrElse("")

  "the finding grouping" should {

      "print a finding every instance has once, naming each instance" in {
          val grouped = MemorySafetyCommands.groupTemplateInstances(
            List(finding("high") -> instance("int"), finding("high") -> instance("char"))
          )
          grouped.map(instancesOf) shouldBe List(Some(List("char", "int")))
      }

      "keep findings that differ in confidence apart" in {
          val grouped = MemorySafetyCommands.groupTemplateInstances(
            List(finding("medium") -> instance("int"), finding("high") -> instance("char"))
          )
          grouped.map(g => (confidenceOf(g), instancesOf(g))) shouldBe List(
            ("medium", Some(List("int"))),
            ("high", Some(List("char")))
          )
      }

      "keep findings whose flows differ apart" in {
          val grouped = MemorySafetyCommands.groupTemplateInstances(
            List(
              finding("high", flow = "p") -> instance("int"),
              finding("high", flow = "q") -> instance("char")
            )
          )
          grouped should have size 2
      }

      "name no instance for an instance without arguments" in {
          val grouped = MemorySafetyCommands.groupTemplateInstances(
            List(finding("high") -> instance(""), finding("high") -> instance("int"))
          )
          grouped.map(instancesOf) shouldBe List(Some(List("int")))
      }

      "print findings outside a template as rendered, each on its own" in {
          val plain = finding("high")
          val grouped =
              MemorySafetyCommands.groupTemplateInstances(List(plain -> None, plain -> None))
          grouped shouldBe List(plain, plain)
      }
  }
end MemorySafetyGroupingUnitTests
