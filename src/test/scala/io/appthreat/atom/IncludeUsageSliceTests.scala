package io.appthreat.atom

import io.appthreat.atom.slicing.*
import io.appthreat.c2cpg.testfixtures.CCodeToCpgSuite
import io.circe.syntax.*

/** A C/C++ include's usages slice names the file the include resolved to and whether it is a system
  * include, so an SBOM tool can tell which package provides the header.
  */
class IncludeUsageSliceTests extends CCodeToCpgSuite:

  private val cpg = code(
    """
      |#include "lib/api.h"
      |#include <no_such_system_header.h>
      |int main(void) { return api(); }
      |""".stripMargin,
    "main.c"
  ).moreCode("int api(void);\n", "lib/api.h")

  private lazy val slices =
      UsageSlicing.calculateUsageSlice(cpg, UsagesConfig()).asInstanceOf[ProgramUsageSlice]
          .objectSlices

  private def includeSlice(header: String) =
      slices.find(s => s.fullName == header && s.fileName.endsWith("main.c")).get

  "an include's usages slice" should {
      "name the file the include resolved to" in {
          val api = includeSlice("lib/api.h")
          api.resolvedPath.map(_.replace('\\', '/')).get should endWith("/lib/api.h")
          api.isSystem shouldBe None
      }

      "mark a system include, with no path when it did not resolve" in {
          val system = includeSlice("no_such_system_header.h")
          system.isSystem shouldBe Some(true)
          system.resolvedPath shouldBe None
      }

      "carry the include fields in JSON only on include slices" in {
          val api = includeSlice("lib/api.h").asJson
          api.hcursor.downField("resolvedPath").as[String].isRight shouldBe true
          api.hcursor.downField("isSystem").succeeded shouldBe false
          val method = slices.find(_.fullName == "main").get.asJson
          method.hcursor.downField("resolvedPath").succeeded shouldBe false
          method.hcursor.downField("isSystem").succeeded shouldBe false
          // and read back, include and method slices alike
          api.as[MethodUsageSlice].toOption.flatMap(_.resolvedPath) shouldBe
              includeSlice("lib/api.h").resolvedPath
          method.as[MethodUsageSlice].toOption shouldBe slices.find(_.fullName == "main")
      }
  }
end IncludeUsageSliceTests
