package io.appthreat.atom

import io.appthreat.atom.slicing.JsNames
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/** The module/symbol split of JavaScript callee names, over the name shapes the JS frontend
  * produces (surveyed on the cdxgen repository's atom).
  */
class JsNamesTests extends AnyWordSpec with Matchers:

  private val imported = Seq(
    "@angular/common/http",
    "@cdxgen/cdx-purl",
    "@babel/parser",
    "date-fns/addDays",
    "node:fs",
    "yargs/helpers",
    "lodash",
    "axios",
    "yargs",
    "tar"
  ).sortBy(m => (-m.length, m))

  private def split(name: String) = JsNames.split(imported, name)

  "JsNames.split" should {
      "split node builtins, with or without an import" in {
          split("node:fs:readFile") shouldBe Some("node:fs" -> Some("readFile"))
          split("node:process:process:exit") shouldBe Some("node:process" -> Some("process.exit"))
          split("node:buffer:Buffer:allocUnsafe:subarray") shouldBe
              Some("node:buffer" -> Some("Buffer.allocUnsafe.subarray"))
      }
      "keep scoped packages and subpaths whole" in {
          split("@babel/parser:parse") shouldBe Some("@babel/parser" -> Some("parse"))
          split("@cdxgen/cdx-purl:Purl:toString") shouldBe
              Some("@cdxgen/cdx-purl" -> Some("Purl.toString"))
          split("@angular/common/http:provideHttpClient") shouldBe
              Some("@angular/common/http" -> Some("provideHttpClient"))
          split("date-fns/addDays:addDays") shouldBe Some("date-fns/addDays" -> Some("addDays"))
          split("yargs/helpers:hideBin") shouldBe Some("yargs/helpers" -> Some("hideBin"))
      }
      "prefer the longest imported specifier" in {
          split("yargs:yargs") shouldBe Some("yargs" -> Some("yargs"))
          split("yargs/helpers:hideBin").map(_._1) shouldBe Some("yargs/helpers")
      }
      "name the module itself without a symbol" in {
          split("yargs") shouldBe Some("yargs" -> None)
          split("axios:default.get") shouldBe Some("axios" -> Some("default.get"))
          split("tar:Parser:on") shouldBe Some("tar" -> Some("Parser.on"))
      }
      "give project files, the JS runtime and unknown names no module" in {
          split("lib/cli/index.js::program:createBom") shouldBe None
          split("../lib/core/ui.js:ui:phase") shouldBe None
          split("__whatwg.console:log") shouldBe None
          split("__ecma.Array.factory") shouldBe None
          split("Boolean") shouldBe None
          split("lodashx:flatten") shouldBe None
      }
      "treat placeholders and source text as unresolved" in {
          split("<unknownFullName>") shouldBe None
          split("value .split") shouldBe None
          split("`${a}:${b}`.toLowerCase") shouldBe None
          split("<UserCard v-for=\"user in users\"") shouldBe None
          JsNames.isResolvedName("node:fs:readFile") shouldBe true
          JsNames.isResolvedName("entry.trim") shouldBe true
          JsNames.isResolvedName("ANY") shouldBe false
      }
  }
end JsNamesTests
