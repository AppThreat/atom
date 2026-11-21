package io.appthreat.atom

import io.appthreat.atom.ReportedPaths.Location.{Inside, Outside}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ReportedPathsTests extends AnyWordSpec with Matchers:

  private val posix   = ReportedPaths.Root.fromText("/work/nginx/src")
  private val windows = ReportedPaths.Root.fromText("C:\\Work\\nginx\\src")

  "a root on a POSIX path" should {
      "keep a name the frontend relativised inside the input" in {
          posix.locate("core/ngx_string.c") shouldBe Inside("core/ngx_string.c")
      }

      "treat an absolute file under the root as inside, relative to it" in {
          posix.locate("/work/nginx/src/core/a.h") shouldBe Inside("core/a.h")
          posix.locate("/work/nginx/src/./core/../core/a.h") shouldBe Inside("core/a.h")
      }

      "treat an absolute file elsewhere as outside and leave it absolute" in {
          val sdk =
              "/Applications/Xcode.app/Contents/Developer/SDKs/MacOSX.sdk/usr/include/_ctype.h"
          posix.locate(sdk) shouldBe Outside(sdk)
          posix.locate("/usr/include/string.h") shouldBe Outside("/usr/include/string.h")
          posix.locate("/opt/homebrew/include/pcre2.h") shouldBe Outside(
            "/opt/homebrew/include/pcre2.h"
          )
      }

      "not mistake a sibling directory that shares the root's name as a prefix for a child" in {
          posix.locate("/work/nginx/src2/a.h") shouldBe Outside("/work/nginx/src2/a.h")
          posix.locate("../src2/a.h") shouldBe Outside("/work/nginx/src2/a.h")
      }

      "resolve a relative name that climbs out of the root" in {
          posix.locate("../objs/ngx_auto_config.h") shouldBe Outside(
            "/work/nginx/objs/ngx_auto_config.h"
          )
          posix.locate("../src/core/a.h") shouldBe Inside("core/a.h")
      }

      "take an empty name as inside" in {
          posix.locate("") shouldBe Inside("")
      }
  }

  "a root on a Windows path" should {
      "recognise absolute files whatever the separators and drive case" in {
          windows.locate("C:\\Work\\nginx\\src\\core\\a.h") shouldBe Inside("core/a.h")
          windows.locate("c:/work/NGINX/src/core/a.h") shouldBe Inside("core/a.h")
          windows.locate("C:/Work/nginx/src2/a.h") shouldBe Outside("C:/Work/nginx/src2/a.h")
      }

      "treat another drive and a network share as outside" in {
          val msvc = "D:\\VS\\VC\\Tools\\MSVC\\14.40\\include\\string.h"
          windows.locate(msvc) shouldBe Outside(msvc)
          val share = "\\\\build\\sdk\\include\\a.h"
          windows.locate(share) shouldBe Outside(share)
      }

      "write a backslash relative name inside the input with /" in {
          windows.locate("core\\a.h") shouldBe Inside("core/a.h")
      }

      "not take a POSIX absolute path as inside" in {
          windows.locate("/usr/include/string.h") shouldBe Outside("/usr/include/string.h")
      }
  }

  "the printed path" should {
      "put the input's name in front of a path inside it" in {
          ReportedPaths.printed(Inside("core/a.c"), "src") shouldBe "src/core/a.c"
      }

      "leave a path outside the input absolute, with nothing in front" in {
          ReportedPaths.printed(Outside("/usr/include/string.h"), "src") shouldBe
              "/usr/include/string.h"
          ReportedPaths.printed(Outside("C:/VS/include/string.h"), "src") shouldBe
              "C:/VS/include/string.h"
      }

      "print a relative path alone when the input has no name" in {
          ReportedPaths.printed(Inside("core/a.c"), "") shouldBe "core/a.c"
          ReportedPaths.printed(Inside("core/a.c"), ".") shouldBe "core/a.c"
      }
  }
end ReportedPathsTests
