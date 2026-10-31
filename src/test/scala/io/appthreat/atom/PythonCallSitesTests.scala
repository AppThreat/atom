package io.appthreat.atom

import better.files.File as BFile
import io.shiftleft.semanticcpg.language.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** `atom callsites` over Python fixtures: one record per external use site of an imported module,
  * asserted on (file, line, module, path, kind, via) per case, plus the optional `purl` from a
  * BOM's ImportedModules and the `builtin` flag for the standard library.
  *
  * Each shape has a bad twin (must produce the record) and a good twin (must not): a shadowing
  * parameter or local next to a module-level use, a reassigned import name, a `TYPE_CHECKING`
  * import that only ever appears in annotations. The frontend needs no external tool for Python,
  * so the suite runs wherever atom's tests run.
  */
class PythonCallSitesTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile                             = null
  private var records: List[PythonCallSitesTests.Rec]      = Nil
  private var purlRecords: List[PythonCallSitesTests.Rec]  = Nil

  override protected def beforeAll(): Unit =
    workspace = BFile.newTemporaryDirectory("atomPyCallsites")
    val fixtures = BFile(getClass.getResource("/py-usages/callsites").toURI)
    val purlFixture = BFile(getClass.getResource("/py-usages/purl-bom").toURI)
    records = run(fixtures, workspace / "main", extraArgs = Seq.empty)
    purlRecords = run(purlFixture, workspace / "purl", extraArgs =
        Seq("--bom-file", (purlFixture / "bom.json").pathAsString))

  override protected def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def run(fixture: BFile, projectDir: BFile, extraArgs: Seq[String]): List[PythonCallSitesTests.Rec] =
    fixture.copyTo(projectDir)
    val atomFile  = workspace / s"${projectDir.name}.atom"
    val sliceFile = workspace / s"${projectDir.name}-callsites.json"
    val result = Atom.run(
      ("callsites" :: "--cache" :: "none" :: "-l" :: "python" ::
          "-o" :: atomFile.pathAsString ::
          "-s" :: sliceFile.pathAsString ::
          extraArgs.toList :::
          List(projectDir.pathAsString)).toArray
    )
    withClue(s"atom callsites failed for ${projectDir.name}: "):
        result.isRight shouldBe true
    ujson.read(sliceFile.contentAsString).obj("callsites").arr
        .map(r =>
            PythonCallSitesTests.Rec(
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
        ).toList
  end run

  private def in(file: String): List[PythonCallSitesTests.Rec] =
      records.filter(_.file == file)

  private def tuple(r: PythonCallSitesTests.Rec) =
      (r.line, r.col, r.module, r.path, r.kind, r.via, r.builtin)

  "atom callsites for Python" should {

      "record plain, aliased and from imports at their uses" in {
          // `import os` then os.getcwd(): the use, not the import line
          in("imports.py").filter(_.line == 7) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 7, 7, "os", "getcwd", "call", "binding", builtin = true)
          )
          // `import numpy as np` binds np; np.array([1]) is a use of numpy
          in("imports.py").filter(_.line == 9) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 9, 7, "numpy", "array", "call", "binding", builtin = false)
          )
          // `from flask import Flask, request`: the module is the from-name. The local the
          // constructor assigns to carries no decltype: its type is dataflow, not a declaration
          in("imports.py").filter(_.line == 10) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 10, 7, "flask", "Flask", "call", "binding", builtin = false)
          )
          // `from django.db.models import Q`: the module is the dotted from-name
          in("imports.py").filter(_.line == 12) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 12, 7, "django.db.models", "Q", "call", "binding", builtin = false)
          )
      }

      "follow a plain dotted import and its member chain through the callee lowering" in {
          // `import os.path`: the binding is `os` (chen#244); os.path.join(x) climbs the
          // `tmpN = os.path` callee lowering to one record with the member chain
          in("imports.py").filter(_.line == 8) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 8, 7, "os", "path.join", "call", "binding", builtin = true)
          )
      }

      "respect a parameter that shadows an import (bad twin) next to a module-level use (good twin)" in {
          // def shadow_param(requests): requests.get(...) - the parameter is not the import
          in("scope.py").filter(_.line == 4) shouldBe empty
          // the module-level capture inside nested defs, a lambda and a comprehension
          in("scope.py").filter(_.line == 20) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("scope.py", 20, 16, "datetime", "date", "call", "binding", builtin = true)
          )
          in("scope.py").filter(_.line == 22) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("scope.py", 22, 19, "datetime", "timedelta", "call", "binding", builtin = true)
          )
          in("scope.py").filter(_.line == 23) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("scope.py", 23, 13, "datetime", "timezone.utc", "arg", "binding", builtin = true)
          )
      }

      "attribute nothing to a local that shadows an import (bad twin)" in {
          in("scope.py").filter(r => r.line == 8) shouldBe empty
      }

      "record nothing for a reassigned import name" in {
          // `import json; json = {}`: the name may hold something else at a use
          in("scope.py").filter(r => r.line == 14) shouldBe empty
      }

      "record a use of the standard library with builtin set" in {
          in("imports.py").filter(r => r.module == "os").map(_.builtin) should not contain false
          // a from-import of a stdlib submodule is the standard library too
          in("imports.py").filter(_.line == 14) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("imports.py", 14, 7, "os.path", "basename", "call", "binding", builtin = true)
          )
      }

      "record an except handler class as a use of the module" in {
          in("exceptions.py").filter(_.line == 7) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("exceptions.py", 7, 12, "requests", "ConnectionError", "ref", "binding", builtin = false)
          )
          // a tuple of handler classes: the member chain from the binding names the whole path
          in("exceptions.py").filter(_.line == 9) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("exceptions.py", 9, 13, "urllib3", "exceptions.HTTPError", "ref", "binding", builtin = false)
          )
      }

      "record an except* handler class like an except one" in {
          in("exceptions.py").filter(_.line == 15) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("exceptions.py", 15, 13, "requests", "RequestException", "ref", "binding", builtin = false)
          )
      }

      "keep a guarded fallback import as one origin" in {
          // try: import yaml except ImportError: yaml = None - the name is still the module,
          // and the fallback's own `yaml = None` write is not a use
          in("fallback.py").filter(_.line == 4) shouldBe empty
          in("fallback.py").filter(_.line == 7) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("fallback.py", 7, 12, "yaml", "safe_load", "call", "binding", builtin = false)
          )
      }

      "give a TYPE_CHECKING import only its annotation use" in {
          // `if TYPE_CHECKING:` runs, so the name's own read is a use of typing
          in("fallback.py").filter(_.line == 10) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("fallback.py", 10, 4, "typing", "TYPE_CHECKING", "ref", "binding", builtin = true)
          )
          // the imported Response appears in the quoted annotations only, as decltype
          in("fallback.py").filter(_.line == 13) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("fallback.py", 13, -1, "fastapi", "Response", "decltype", "name", builtin = false)
          )
          in("fallback.py").filter(r => r.module == "fastapi") should have size 1
      }

      "record a decorator through the typed base of its annotation" in {
          in("typed.py").filter(_.line == 6) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("typed.py", 6, 2, "flask", "Flask.route", "decorator", "type", builtin = false)
          )
      }

      "record a typed attribute read via the receiver's static type" in {
          in("typed.py").filter(_.line == 8) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("typed.py", 8, 12, "flask", "request.method", "ref", "binding", builtin = false)
          )
      }

      "record parameter and return annotations as decltype" in {
          // def view(s: Session) -> str
          in("typed.py").filter(_.line == 7) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("typed.py", 7, -1, "sqlalchemy.orm", "Session", "decltype", "name", builtin = false)
          )
          // def typed(r: "Response") -> "Response"
          in("fallback.py").filter(_.line == 13) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("fallback.py", 13, -1, "fastapi", "Response", "decltype", "name", builtin = false)
          )
      }

      "record no decltype for a return type the recovery inferred" in {
          // def make_session(engine): return Session(engine) has no annotation
          in("typed.py").filter(r => r.line == 13 && r.kind == "decltype") shouldBe empty
      }

      "name a generic annotation by its base type" in {
          in("typed.py").filter(r => r.line == 18 && r.kind == "decltype").map(r => (r.module, r.path)) should contain(
            ("typing", "Iterator")
          )
      }

      "put a multi-line signature's return decltype on the annotation's own line" in {
          // def handle(\n    body,\n) -> Response:  the annotation is on the 5th line
          in("multiline.py").filter(r => r.kind == "decltype") should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("multiline.py", 5, -1, "fastapi", "Response", "decltype", "name", builtin = false)
          )
      }

      "record heritage bases checked against the file's imports" in {
          // class Command(click.Command)
          in("heritage.py").filter(_.line == 4) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("heritage.py", 4, -1, "click", "Command", "extends", "name", builtin = false)
          )
          // class Worker(Celery), from celery import Celery
          in("heritage.py").filter(_.line == 7) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("heritage.py", 7, -1, "celery", "Celery", "extends", "name", builtin = false)
          )
      }

      "leave a member inherited from an imported base to the heritage record" in {
          // class Formatter(argparse.HelpFormatter): the receiver of `self.add_usage(...)` is the
          // project class, whose base the class line already records
          in("heritage.py").filter(_.line == 12) should contain theSameElementsAs List(
            PythonCallSitesTests.Rec("heritage.py", 12, -1, "argparse", "HelpFormatter", "extends", "name", builtin = true)
          )
          in("heritage.py").filter(_.line == 14) shouldBe empty
      }

      "map a record's module to its distribution through the BOM's ImportedModules" in {
          val load = purlRecords.filter(_.path == "safe_load")
          load should have size 1
          load.head.purl shouldBe "" // yaml: PyYAML and ruamel-yaml both claim it
          val fetch = purlRecords.filter(_.path == "get")
          fetch should have size 1
          fetch.head.purl shouldBe "pkg:pypi/requests@2.31.0"
          // a submodule maps through its top-level package
          val adapter = purlRecords.filter(_.path == "HTTPAdapter")
          adapter should have size 1
          adapter.head.purl shouldBe "pkg:pypi/requests@2.31.0"
      }
  }
end PythonCallSitesTests

object PythonCallSitesTests:
  final case class Rec(
    file: String,
    line: Int,
    col: Int,
    module: String,
    path: String,
    kind: String,
    via: String,
    builtin: Boolean,
    purl: String = ""
  )
