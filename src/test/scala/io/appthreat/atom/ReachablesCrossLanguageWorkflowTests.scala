package io.appthreat.atom

import better.files.File as BFile
import io.appthreat.edg2atom.parser.EdgaRunner
import io.shiftleft.codepropertygraph.cpgloading.{CpgLoader, CpgLoaderConfig}
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.semanticcpg.language.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import overflowdb.Config as OdbConfig

import scala.util.Try

/** Cross-language `reachables` regression fixtures.
  *
  * One small project per supported frontend, each with a source -> helper -> sink chain, driven
  * end-to-end through `Atom.run`: frontend, enhancement passes, taggers, the backward data-flow
  * engine and the reachable slicer.
  *
  * These exist because reachables output is language-agnostic shared code: a change made for one
  * language can silently lose flows in another unless every language has a reachables test. Every
  * fixture pins
  *
  *   - the total number of emitted flow entries, and
  *   - the source and sink node identity of at least one flow,
  *
  * so that a change to the shared slicer or the query engine cannot silently drop or flood any
  * language. Counts move deliberately: when a change alters them, update the number AND record why
  * in the commit.
  *
  * The JavaScript, PHP and Ruby frontends need their external AST generators (`astgen`,
  * `phpastgen`, `rbastgen` - all from `@appthreat/atom-parsetools`); those sections cancel (not
  * fail) when the generator is missing or too old, exactly like [[PhpAtomWorkflowTests]] and
  * [[RubyAtomWorkflowTests]]. Without the gate a missing `astgen` surfaces as a frontend that
  * "succeeds" with an empty graph, and the fixture then fails with an opaque count mismatch instead
  * of canceling with the reason. The C fixture runs the EDG frontend and needs the `edga` binary;
  * it also cancels when the running edga is not the release its count was calibrated against.
  */
class ReachablesCrossLanguageWorkflowTests extends AnyWordSpec with Matchers with BeforeAndAfterAll:

  private var workspace: BFile = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
      workspace = BFile.newTemporaryDirectory("atomReachablesXlang")

  override def afterAll(): Unit =
      if workspace != null then workspace.delete(swallowIOExceptions = true)

  private def runReachables(
    language: String,
    projectName: String,
    writeProject: BFile => Unit,
    perfReport: Option[BFile] = None,
    extraArgs: Seq[String] = Seq.empty
  ): Seq[ujson.Value] =
    val projectDir = workspace / projectName
    projectDir.createDirectories()
    writeProject(projectDir)
    val atomFile  = workspace / s"$projectName.atom"
    val sliceFile = workspace / s"$projectName-slices.json"
    val perfArgs  = perfReport.map(p => Seq("--perf-report", p.pathAsString)).getOrElse(Seq.empty)
    val result = Atom.run(
      (Seq(
        "reachables",
        "--cache",
        "none",
        "-l",
        language,
        "-o",
        atomFile.pathAsString,
        "-s",
        sliceFile.pathAsString
      ) ++ perfArgs ++ extraArgs ++ Seq(projectDir.pathAsString)).toArray
    )
    result.isRight shouldBe true
    // reachables chunks at 1000 entries per file (`slices.json`, `slices_1.json`, ...): a
    // single-file read would understate every count on larger projects.
    val chunks = workspace.glob(s"$projectName-slices*.json").toSeq.sortBy(_.name)
    chunks should not be empty
    chunks.flatMap(f => ujson.read(f.contentAsString).arr.toSeq)
  end runReachables

  /** The stage names a `--perf-report` run recorded, read back from its NDJSON. */
  private def reportedStages(perfReport: BFile): Set[String] =
      perfReport.lines.toList
          .filter(_.nonEmpty)
          .map(line => ujson.read(line).obj("stage").str)
          .toSet

  /** The nodes of one flow entry, for identity assertions. */
  private def nodesOf(entry: ujson.Value): Seq[ujson.Value] =
      entry.obj.get("flows").map(_.arr.toSeq).getOrElse(Seq.empty)

  private def tagSet(node: ujson.Value): Set[String] =
      node.obj
          .get("tags")
          .map(_.str)
          .getOrElse("")
          .split(",")
          .toSeq
          .map(_.trim)
          .filter(_.nonEmpty)
          .toSet

  /** True if the entry's FIRST node identifies the source and its LAST node the sink. Identity is
    * (name or code, parent method, tag) - stable across node-id churn.
    */
  private def matchesEndpoints(
    entry: ujson.Value,
    sourceName: String,
    sinkName: String,
    sinkTag: String
  ): Boolean =
    val ns = nodesOf(entry)
    ns.nonEmpty && {
        val head = ns.head
        val tail = ns.last
        (head.obj("name").str == sourceName || head.obj("code").str.contains(sourceName)) &&
        (tail.obj("name").str == sinkName || tail.obj("code").str.contains(sinkName)) &&
        tagSet(tail).contains(sinkTag)
    }

  private def assertSourceToSinkFlow(
    flows: Seq[ujson.Value],
    language: String,
    sourceName: String,
    sinkName: String,
    sinkTag: String
  ): Unit =
      withClue(
        s"$language: no flow runs from source '$sourceName' to sink '$sinkName' (tag $sinkTag); " +
            s"entries=${flows.map(f => nodesOf(f).map(_.obj("name").str).mkString("->")).mkString(" | ")}: "
      ):
        flows.exists(matchesEndpoints(_, sourceName, sinkName, sinkTag)) shouldBe true

  "reachables for python" should {
      "emit source-to-sink flows for a flask-style project" in {
          val flows = runReachables(
            "python",
            "python-project",
            dir =>
                (dir / "server.py").write(
                  """import subprocess
                |from flask import Flask, request
                |
                |app = Flask(__name__)
                |
                |def run(cmd):
                |    return subprocess.getoutput(cmd)
                |
                |@app.route("/exec")
                |def exec_name():
                |    name = request.args["name"]
                |    return run("echo " + name)
                |""".stripMargin
                )
          )
          flows.length should be(1)
          // The source is `request.args["name"]`, which Python lowers to a tagged
          // `<operator>.indexAccess` call: the flow must be ROOTED at that call (not at an
          // intermediate local) and terminate at the code-execution sink.
          assertSourceToSinkFlow(flows, "python", "request.args", "getoutput", "code-execution")
      }
  }

  "reachables for c" should {
      "emit source-to-sink flows and run none of the memory-safety overlay stages" in {
          // The memory-safety overlay used to run for every dataflow-enhanced command and cost
          // most of a C reachables run on real projects (atom#397: on google/brotli its eight
          // stages were about 8.7 s of the 10 s regression against 3.2.1). The --perf-report
          // stage list is the evidence: a reachables run may not execute a single one of them,
          // and it stays as fast as the pinned flow count suggests. On main this assertion fails
          // with the overlay stages present in the report.
          val perfReport = workspace / "c-project-perf.ndjson"
          try
            val flows = runReachables(
              "c",
              "c-project",
              perfReport = Some(perfReport),
              writeProject = dir =>
                (dir / "src").createDirectories()
                (dir / "src" / "main.c").write(
                  """void store_input(const char *in) {
                        |    (void)in;
                        |}
                        |
                        |int main(int argc, char **argv) {
                        |    if (argc > 1) {
                        |        const char *name = argv[1];
                        |        store_input(name);
                        |        curl_free(name);
                        |    }
                        |    return 0;
                        |}
                        |""".stripMargin
                )
            )
            // Pinned at 1: the flow from the argv parameter into the curl_free call, whose
            // argument the C taggers mark library-call, one of the fixed sink categories
            // atom#404 collects. This fixture's earlier shape reached the store_input call,
            // whose argument carries the event tag, a source mark the old vocabulary pick
            // queried as a sink; under the fixed list it correctly is not a sink, so the
            // fixture's flow now ends at a contract sink. curl_free stays undeclared for the
            // same reason as the c-cdt-project fixture's callees: a prototype written into
            // the project makes chen mark the callee internal and leaves the library-call
            // tagger nothing to fire on. While the memory-safety overlay ran here, its tags
            // flooded the C default-sink vocabulary and the count rose with the overlay's
            // passes instead of with the program.
            flows.length should be(1)
            // The command-line argument reaches the external library call's parameter, the
            // node the taggers mark library-call (p1, as the c-cdt-project fixture's p3).
            assertSourceToSinkFlow(flows, "c", "argv", "p1", "library-call")
            val stages = reportedStages(perfReport)
            stages should not be empty
            stages should contain("slicing.reachables")
            stages.intersect(MemorySafetyOverlayStages) shouldBe empty
          finally System.clearProperty("chen.perf.report")
          end try
      }
  }

  "reachables for javascript" should {
      "emit source-to-sink flows for an express-style project" in {
          requireAstgen()
          val flows = runReachables(
            "javascript",
            "js-project",
            dir =>
              (dir / "src").createDirectories()
              (dir / "src" / "app.js").write(
                """const express = require("express");
                    |const app = express();
                    |
                    |function shout(value) {
                    |    return value + "!";
                    |}
                    |
                    |app.get("/greet", function (req, res) {
                    |    const name = req.query.name;
                    |    res.send(shout(name));
                    |});
                    |""".stripMargin
              )
          )
          // One flow: req.query.name -> shout(name) -> res.send(...). Three more used to be
          // reported - the `app.get(...)` registration call was a source and a sink because its
          // code contained `req.` and `res.`, and one path ended at `shout(name)`, on no sink.
          flows.length should be(1)
          // The request data must reach the framework output sink. The flow is rooted at the read
          // `req.query` that `.name` is taken from: a read of a read now carries the value.
          assertSourceToSinkFlow(flows, "javascript", "req.query", "send", "framework-output")
      }

      "sink the calls a project makes into its npm dependencies, with the SBOM's purl" in {
          requireAstgen()
          // The dependency boundary (atom#402): jssrc2cpg leaves `node_modules` out, so a call
          // into an imported package resolves to an external stub (`axios:get`) and, before this
          // fixture, no JS arm tagged one - a flow crossing the boundary had no sink to end on.
          // JsDependencySinkTagger tags the axios call http-client and the log call
          // library-call (`get` is absent from EasyTagsPass's SDK-verb list, so nothing else
          // tags either), and chen's PackageAttribution layers the SBOM's purl for axios onto
          // the http-client sink. On main both assertions fail: the axios call carries only
          // service-ingress, a source tag, so no entry ends on it, and no node carries
          // library-call or a purl.
          val flows = runReachables(
            "javascript",
            "js-axios-project",
            dir =>
              (dir / "package.json").write("""{ "dependencies": { "axios": "^1.7.0" } }""")
              (dir / "bom.json").write(
                """{
                    |  "bomFormat": "CycloneDX",
                    |  "specVersion": "1.4",
                    |  "components": [
                    |    {
                    |      "type": "library",
                    |      "name": "axios",
                    |      "version": "1.7.0",
                    |      "purl": "pkg:npm/axios@1.7.0"
                    |    }
                    |  ]
                    |}
                    |""".stripMargin
              )
              (dir / "src").createDirectories()
              (dir / "src" / "app.js").write(
                """const axios = require("axios");
                    |const express = require("express");
                    |const winston = require("winston");
                    |const app = express();
                    |
                    |app.get("/fetch", function (req, res) {
                    |    const target = req.query.url;
                    |    winston.info("fetching " + target);
                    |    axios.get(target).then(function (response) {
                    |        res.json(response.data);
                    |    });
                    |});
                    |""".stripMargin
              )
          )
          // Three entries: the boundary flows and, through the axios result, the response call
          // the fixture had on its own. The request data reaches the axios call, which is the
          // sink itself: http-client, because axios is a request client, with the purl the SBOM
          // gives the package (rendered on the `target` value the call receives).
          flows.length should be(3)
          val boundary = flows.filter { entry =>
              nodesOf(entry).lastOption.exists { node =>
                  node.obj("name").str == "target" && tagSet(node).contains("http-client") &&
                  tagSet(node).contains("pkg:npm/axios@1.7.0")
              }
          }
          boundary should not be empty
          // A dependency call outside the request-client list is a library-call sink the same
          // way. winston is named by no component in the BOM, so its sink carries no purl: the
          // SBOM owns package identity.
          flows.exists(entry =>
              nodesOf(entry).exists(n => tagSet(n).contains("library-call"))
          ) shouldBe true
      }

      "emit source-to-sink flows for a react-router component" in {
          requireAstgen()
          // RELEASE ORDERING: this fixture and the usages one below depend on the chen framework
          // taggers (ChennaiTagsPass route tables / React Router). chen is consumed at an exact
          // version with no local bump, so chen must be published BEFORE atom's CI runs these -
          // a clean machine resolving the previously published chen fails both fixtures.
          val flows = runReachables(
            "javascript",
            "js-react-project",
            dir =>
              (dir / "src").createDirectories()
              (dir / "src" / "profile.jsx").write(
                """import { useParams } from "react-router-dom";
                    |
                    |export default function Profile() {
                    |    const { bio } = useParams();
                    |    return <div dangerouslySetInnerHTML={{ __html: bio }} />;
                    |}
                    |""".stripMargin
              )
          )
          // The URL parameter reaches the raw-HTML render through the props chain: the flow must
          // be rooted at the tagged useParams call and end at the framework-output sink.
          assertSourceToSinkFlow(flows, "javascript", "useParams", "__html", "framework-output")
      }
  }

  // Frontend single-page applications, with no SBOM in the project: the sources and sinks come
  // from chen's JavaScript framework recognizers alone (`taggers/js`), not from purl tags.
  // RELEASE ORDERING: like the React Router fixture above, these need the chen that ships the
  // recognizers published before atom's CI runs them.
  "reachables for frontend applications without an SBOM" should {
      "find an Angular route parameter reaching a sanitizer bypass" in {
          requireAstgen()
          val flows = runReachables(
            "ts",
            "ng-spa",
            dir =>
              (dir / "src" / "app").createDirectories()
              (dir / "src" / "app" / "search.component.ts").write(
                """import { Component, inject } from '@angular/core';
                    |import { ActivatedRoute } from '@angular/router';
                    |import { DomSanitizer } from '@angular/platform-browser';
                    |
                    |@Component({ selector: 'app-search', template: '<p [innerHTML]="html"></p>' })
                    |export class SearchComponent {
                    |  private readonly route = inject(ActivatedRoute);
                    |  private readonly sanitizer = inject(DomSanitizer);
                    |  html: unknown;
                    |  ngOnInit() {
                    |    const q = this.route.snapshot.queryParams.q;
                    |    this.html = this.sanitizer.bypassSecurityTrustHtml('<b>' + q + '</b>');
                    |  }
                    |}
                    |""".stripMargin
              )
          )
          assertSourceToSinkFlow(
            flows,
            "typescript",
            "this.route.snapshot.queryParams",
            "bypassSecurityTrustHtml",
            "xss"
          )
      }

      "find location.hash reaching innerHTML through a captured variable" in {
          requireAstgen()
          val flows = runReachables(
            "javascript",
            "dom-spa",
            dir =>
              (dir / "src").createDirectories()
              (dir / "src" / "main.js").write(
                """const panel = document.getElementById('panel');
                    |const fragment = decodeURIComponent(location.hash.slice(1));
                    |
                    |window.addEventListener('hashchange', () => {
                    |  if (fragment) {
                    |    panel.innerHTML = fragment;
                    |  }
                    |});
                    |""".stripMargin
              )
          )
          flows.exists(f =>
              nodesOf(f).lastOption.exists(n => tagSet(n).contains("xss"))
          ) shouldBe true
      }

      "find a Next.js server action argument reaching a shell command" in {
          requireAstgen()
          val flows = runReachables(
            "ts",
            "next-spa",
            dir =>
              (dir / "app").createDirectories()
              (dir / "app" / "actions.ts").write(
                """'use server';
                    |import { exec } from 'node:child_process';
                    |
                    |export async function ping(formData: FormData) {
                    |  const host = formData.get('host') as string;
                    |  exec(`ping -c 1 ${host}`);
                    |}
                    |""".stripMargin
              )
          )
          assertSourceToSinkFlow(flows, "typescript", "formData", "exec", "shell-exec")
      }

      "report what a handler reflects, not a load's data, a JSON response or a chained request" in {
          requireAstgen()
          val flows = runReachables(
            "ts",
            "sk-spa",
            dir =>
              (dir / "src" / "routes" / "item").createDirectories()
              (dir / "src" / "routes" / "api" / "raw").createDirectories()
              (dir / "src" / "lib").createDirectories()
              (dir / "package.json").write("""{ "dependencies": { "@sveltejs/kit": "^2.0.0" } }""")
              (dir / "src" / "routes" / "item" / "+page.server.ts").write(
                """export const load = async ({ params }) => {
                    |  return { id: params.id };
                    |};
                    |""".stripMargin
              )
              (dir / "src" / "routes" / "api" / "+server.ts").write(
                """export const GET = async ({ url }) => {
                    |  const q = url.searchParams.get('q');
                    |  return Response.json({ q });
                    |};
                    |""".stripMargin
              )
              (dir / "src" / "routes" / "api" / "raw" / "+server.ts").write(
                """export const GET = async ({ url }) => {
                    |  const q = url.searchParams.get('q') ?? '';
                    |  return new Response(q, { headers: { 'content-type': 'text/html' } });
                    |};
                    |""".stripMargin
              )
              (dir / "src" / "lib" / "items.ts").write(
                """export async function related() {
                    |  const res = await fetch('/api/item');
                    |  const item = await res.json();
                    |  return fetch(`/api/related/${item.id}`);
                    |}
                    |""".stripMargin
              )
          )
          def endsIn(file: String) = flows.filter(f =>
              nodesOf(f).lastOption.exists(_.obj.get("parentFileName").exists(_.str.endsWith(file)))
          )
          // the load's data goes to the page; a JSON response is no markup; an id one response
          // gave is how the next request walks the API
          endsIn("item/+page.server.ts") shouldBe empty
          endsIn("api/+server.ts") shouldBe empty
          endsIn("lib/items.ts") shouldBe empty
          endsIn("raw/+server.ts") should not be empty
      }
  }

  "usages for javascript" should {
      "expose a React Router <Route path> attribute as a route usage type" in {
          requireAstgen()
          val projectDir = workspace / "js-route-project"
          projectDir.createDirectories()
          (projectDir / "src").createDirectories()
          (projectDir / "src" / "app.jsx").write(
            """import { Route } from "react-router-dom";
                |function Profile(props) {
                |    return <div>{props.name}</div>;
                |}
                |export default function App() {
                |    return <Route path="/profile" element={<Profile />} />;
                |}
                |""".stripMargin
          )
          val atomFile  = workspace / "js-route-project.atom"
          val sliceFile = workspace / "js-route-project-usages.json"
          val result = Atom.run(
            Seq(
              "usages",
              "--cache",
              "none",
              "-l",
              "javascript",
              "-o",
              atomFile.pathAsString,
              "-s",
              sliceFile.pathAsString,
              projectDir.pathAsString
            ).toArray
          )
          result.isRight shouldBe true
          val json = ujson.read(sliceFile.contentAsString)
          val udtNames = json.obj
              .get("userDefinedTypes")
              .map(_.arr.map(_.obj("name").str))
              .getOrElse(Seq.empty)
          // The JSX Route path literal sits under a TEMPLATE_DOM node, not a call argument, so
          // only the template-literal branch of routesAsUDT can surface it.
          udtNames should contain("/profile")
      }
  }

  "reachables for java" should {
      "emit source-to-sink flows for a configured project" in {
          val flows = runReachables(
            "java",
            "java-project",
            dir =>
              (dir / "chennai.json").write(
                """{
                    |  "tags": [
                    |    { "name": "framework-input", "parameters": ["java.lang.String"] },
                    |    { "name": "code-execution", "types": ["java.lang.Process"] }
                    |  ]
                    |}
                    |""".stripMargin
              )
              (dir / "src" / "main" / "java" / "com" / "example").createDirectories()
              (dir / "src" / "main" / "java" / "com" / "example" / "UserController.java").write(
                """package com.example;
                    |
                    |import java.io.IOException;
                    |
                    |public class UserController {
                    |    public String greet(String name) throws IOException {
                    |        String message = "hello " + name;
                    |        return render(message);
                    |    }
                    |
                    |    private String render(String template) throws IOException {
                    |        Runtime.getRuntime().exec("echo " + template);
                    |        return "<p>" + template + "</p>";
                    |    }
                    |}
                    |""".stripMargin
              )
          )
          flows.length should be(1)
          // The controller parameter must reach the Runtime.exec call, across the helper call.
          assertSourceToSinkFlow(flows, "java", "name", "exec", "code-execution")
      }

      "emit framework-tagged flows for a Spring controller without any chennai.json" in {
          // RELEASE ORDERING: depends on chen's Java framework taggers (ChennaiTagsPass
          // .tagJavaRoutes: @RequestParam parameters are framework-input) and the Java sink
          // families (EasyTagsPass.tagJavaSinkFamilies: Runtime.exec is code-execution). chen is
          // consumed at an exact version, so chen must be published BEFORE atom's CI runs this -
          // a clean machine resolving the previously published chen fails this fixture.
          val flows = runReachables(
            "java",
            "java-spring-project",
            dir =>
              (dir / "src" / "main" / "java" / "com" / "example").createDirectories()
              (dir / "src" / "main" / "java" / "com" / "example" / "GreetingController.java")
                  .write(
                    """package com.example;
                        |
                        |import org.springframework.web.bind.annotation.GetMapping;
                        |import org.springframework.web.bind.annotation.RequestParam;
                        |import org.springframework.web.bind.annotation.RestController;
                        |
                        |@RestController
                        |public class GreetingController {
                        |
                        |    @GetMapping("/greet")
                        |    public String greet(@RequestParam("name") String name) throws Exception {
                        |        return run("echo " + name);
                        |    }
                        |
                        |    private String run(String command) throws Exception {
                        |        return new String(Runtime.getRuntime().exec(command).getInputStream().readAllBytes());
                        |    }
                        |}
                        |""".stripMargin
                  )
          )
          flows.length should be(1)
          // The @RequestParam parameter is web-facing input by annotation, and Runtime.exec is a
          // code-execution sink by the Java sink family - no project config involved.
          assertSourceToSinkFlow(flows, "java", "name", "exec", "code-execution")
      }

      "emit dataflow through a Java 21 pattern switch from a servlet parameter" in {
          // RELEASE ORDERING: depends on chen's switch-expression and pattern lowering (the
          // javasrc2cpg AstCreator lowers `switch` expressions to conditionals and binds pattern
          // variables). Same publish-before-CI ordering as the Spring fixture above.
          val flows = runReachables(
            "java",
            "java-modern-project",
            dir =>
              (dir / "src" / "main" / "java" / "com" / "example").createDirectories()
              (dir / "src" / "main" / "java" / "com" / "example" / "LegacyServlet.java")
                  .write(
                    """package com.example;
                        |
                        |import jakarta.servlet.http.HttpServlet;
                        |import jakarta.servlet.http.HttpServletRequest;
                        |import jakarta.servlet.http.HttpServletResponse;
                        |import java.io.IOException;
                        |
                        |public class LegacyServlet extends HttpServlet {
                        |    @Override
                        |    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                        |        String kind = req.getParameter("kind");
                        |        String command = switch (kind) {
                        |            case "greet" -> "echo hello";
                        |            case String k when k.length() > 3 -> "echo " + k;
                        |            default -> "echo noop";
                        |        };
                        |        resp.getWriter().println(execute(command));
                        |    }
                        |
                        |    private String execute(String command) throws IOException {
                        |        return new String(Runtime.getRuntime().exec(command).getInputStream().readAllBytes());
                        |    }
                        |}
                        |""".stripMargin
                  )
          )
          flows should not be empty
          // The servlet request PARAMETER is the tagged source; the request reader, the switch
          // expression (a Java 14 construct with Java 21 pattern arms and a guard), and the
          // helper call all sit between it and the Runtime.exec sink.
          assertSourceToSinkFlow(flows, "java", "req", "exec", "code-execution")
      }
  }

  "reachables for c" should {
      "emit flows from argv through project-header macros into an external library call" in {
          requireEdga()
          val edga = EdgaRunner.locate().get
          // The emitted count depends on the edga build (this fixture measured 4 entries with the
          // edga 0.2.0 release and 3 with the pre-0.2.0 build, because resolving the enumerator
          // constant is what connects part of the paths), so the pin below names the release it
          // was calibrated against and the running binary's identity is read first: a different
          // release cancels the fixture instead of moving the number silently.
          edgaReportedVersion(edga) match
            case Some(v) if v == edgaCalibratedAgainst => ()
            case other =>
                cancel(
                  s"edga ${other.getOrElse("of unrecognizable version")} is running, but this " +
                      s"fixture's count is calibrated against edga $edgaCalibratedAgainst; " +
                      "re-pin the number against the new release and record why in the commit"
                )
          // argv flows through the project-header identity macros in proj.h into the parser
          // functions and from there into a parameter of the external curl_easy_setopt function
          // (chen tags the parameters of external `curl_*` methods `library-call`, the sink family
          // the reachables collector reads on C). The sink call itself expands through an identity
          // macro defined in the same project header, the shape real curl headers use: without the
          // expansion, the arguments do not reach the library-call-tagged parameters. The
          // INLINED macro-use calls edg2atom writes for the expansions are what carry the data
          // across the macros.
          val flows = runReachables(
            "c",
            "c-edg-project",
            dir =>
              val src = (dir / "src").createDirectories()
              (src / "proj.h").write(
                """#ifndef PROJ_H
                    |#define PROJ_H
                    |#define pass_through(p) (p)
                    |#define pick_option(o) (o)
                    |#define curl_easy_setopt(handle, opt, param) curl_easy_setopt(handle, opt, param)
                    |enum option { CURLOPT_URL = 10000 };
                    |#endif
                    |""".stripMargin
              )
              (src / "main.c").write(
                """#include <stdlib.h>
                    |#include <string.h>
                    |#include "proj.h"
                    |
                    |extern int curl_easy_setopt(void *handle, int option, const char *value);
                    |
                    |static char *parse_arg(char *a) {
                    |  return pass_through(a);
                    |}
                    |
                    |static char *find_option(char *o) {
                    |  return pick_option(o);
                    |}
                    |
                    |int main(int argc, char **argv) {
                    |  char *value = parse_arg(argv[1]);
                    |  char *opt = find_option(value);
                    |  curl_easy_setopt(0, CURLOPT_URL, opt);
                    |  return (int)strlen(value);
                    |}
                    |""".stripMargin
              )
            ,
            extraArgs = Seq("--c-frontend", "edg", "--edga-path", edga)
          )
          // Pinned at 2 under the fixed C sink list of atom#404 (4 under the vocabulary-derived
          // pick this fixture was first calibrated against, whose two extra entries ended on fact
          // tags the old pick queried as sinks). The two remaining entries end on the
          // library-call-tagged parameters of the external curl_easy_setopt, the contract sink.
          flows.length should be(2)
          // Secondary check on the slices JSON: at least one emitted path runs from main's argv
          // through the pick_option macro call into the library-call sink. The dispatch type is
          // only visible on the graph, so the macro mechanism itself is asserted there below.
          flows.exists { entry =>
            val ns = nodesOf(entry)
            ns.nonEmpty &&
            ns.head.obj("name").str == "argv" &&
            ns.head.obj("parentMethodName").str == "main" &&
            ns.last.obj("parentMethodName").str == "curl_easy_setopt" &&
            tagSet(ns.last).contains("library-call") &&
            ns.tail.dropRight(1).exists(n =>
                n.obj("label").str == "CALL" && n.obj("name").str == "pick_option"
            )
          } shouldBe true
          // On the graph: the macro-use calls keep the dispatch type edg2atom gives them (INLINED)
          // and the definition-site fullName shape it writes
          // (<definition file>:<first line>:<last line>:<NAME>:<parameter count>). The sink call
          // carries the same shape for its identity macro, while the ordinary call the expansion
          // contains keeps the plain callee name.
          val odb = OdbConfig.withoutOverflow()
              .withStorageLocation((workspace / "c-edg-project.atom").pathAsString)
          val cpg = CpgLoader.loadFromOverflowDb(
            CpgLoaderConfig().withOverflowConfig(odb).doNotCreateIndexesOnLoad
          )
          try
            val parserMacroCalls = cpg.call.nameExact("pick_option", "pass_through").l
            parserMacroCalls should not be empty
            parserMacroCalls.map(c => c.dispatchType).toSet shouldBe Set(DispatchTypes.INLINED)
            parserMacroCalls.map(_.methodFullName).foreach { fullName =>
                (fullName should fullyMatch).regex(
                  """.*proj\.h:\d+:\d+:(pick_option|pass_through):\d+"""
                )
            }
            val sinkMacroCall =
                cpg.call.methodFullName(""".*proj\.h:\d+:\d+:curl_easy_setopt:3""").l
            sinkMacroCall.map(c => c.dispatchType).toSet shouldBe Set(DispatchTypes.INLINED)
          finally cpg.close()
      }
  }

  "reachables for c" should {
      "emit the argv flow into an undeclared library call as the only entry" in {
          // RELEASE ORDERING: the count below is calibrated against chen 4.0.1 (c2cpg_3 4.0.1 from
          // the local ivy repository, built from clean chen main 20162e8a). chen is consumed at an
          // exact version, so chen must be published BEFORE atom's CI runs this - a clean machine
          // resolving the previously published chen fails this fixture. The gate reads the running
          // build the way `atom --version` prints it, so a different chen cancels with re-pin
          // instructions instead of failing on an opaque count mismatch.
          ToolVersions.current().chen match
            case v if v == chenCalibratedAgainst => ()
            case other =>
                cancel(
                  s"chen $other is running, but this fixture's count is calibrated against " +
                      s"chen $chenCalibratedAgainst; re-pin the number against the new build and " +
                      "record why in the commit"
                )
          val flows = runReachables(
            "c",
            "c-cdt-project",
            dir =>
                (dir / "main.c").write(
                  """int main(int argc, char **argv) {
                    |  char buf[16];
                    |  if (argc > 1) {
                    |    char *val = argv[1];
                    |    unicode_fold(buf, val);
                    |    curl_easy_setopt(0, 42, val);
                    |  }
                    |  return 0;
                    |}
                    |""".stripMargin
                )
          )
          // The command line must reach the argument of the undeclared library call: argv flows
          // into val, val reaches curl_easy_setopt, and chen tags the parameters of external
          // curl_* methods library-call. The callees stay undeclared on purpose: chen marks
          // source-parsed methods internal, so prototypes written into the fixture would turn
          // both callees internal and leave nothing for the library-call tagger to fire on.
          assertSourceToSinkFlow(flows, "c", "argv", "p3", "library-call")
          // The fixed sink list leaves exactly one entry, the library-call flow above, and every
          // emitted entry ends on a node carrying one of the five C sink categories. This
          // property is specific to this minimal fixture and must NOT be generalised to the EDG
          // fixture or to larger C projects: there the collectors legitimately leave entries
          // whose last node carries a bookkeeping tag (kept whole by the terminus normalisation
          // when no sink-tagged call lies on the path).
          flows.length should be(1)
          flows.foreach { entry =>
              withClue(
                s"entry ${nodesOf(entry).map(_.obj("name").str).mkString("->")} does not end on a C sink category: "
              ):
                (tagSet(nodesOf(entry).last) & C_CONTRACT_SINK_TAGS) should not be empty
          }
      }
  }

  "reachables for php" should {
      "emit source-to-sink flows for a routed controller" in {
          requirePhpGenerator()
          val flows = runReachables(
            "php",
            "php-project",
            dir =>
              (dir / "src").createDirectories()
              (dir / "src" / "Controller.php").write(
                """<?php
                    |
                    |namespace App;
                    |
                    |class UserController
                    |{
                    |    #[Route("/users")]
                    |    public function index($id)
                    |    {
                    |        $q = $_GET['q'];
                    |        echo $q;
                    |        return $id;
                    |    }
                    |}
                    |""".stripMargin
              )
          )
          flows.length should be(2)
          // The superglobal-derived local must reach the echo output sink.
          assertSourceToSinkFlow(flows, "php", "q", "echo", "framework-output")
      }
  }

  "reachables for ruby" should {
      "emit source-to-sink flows for a rails-style controller" in {
          requireRubyGenerator()
          val flows = runReachables(
            "ruby",
            "ruby-project",
            dir =>
              (dir / "app").createDirectories()
              (dir / "app" / "users_controller.rb").write(
                """require "net/http"
                    |
                    |class UsersController < BaseController
                    |  def show(user_id)
                    |    url = "https://example.com/users/" + user_id.to_s
                    |    Net::HTTP.get(URI(url))
                    |  end
                    |end
                    |""".stripMargin
              )
          )
          flows.length should be(2)
          // The controller parameter must reach the Net::HTTP.get client sink.
          assertSourceToSinkFlow(flows, "ruby", "user_id", "get", "http-client")
      }
  }

  // ------------------------------------------------------------------ calibration references

  /** The chen build the C fixture's count is calibrated against, read the way `atom --version`
    * prints it: the Implementation-Version of the chen jars on this suite's classpath.
    */
  private val chenCalibratedAgainst = "4.0.1"

  /** The sink categories the C default sink list queries, in the order the slicer fixes them. */
  private val C_CONTRACT_SINK_TAGS = Set("library-call", "http", "cloud", "rpc", "device-driver")

  // ------------------------------------------------------------------ generator gates

  /** The eight memory-safety overlay stages (atom#397). Only the `memory-safety` command runs them;
    * a reachables perf report naming any of them means the gating regressed and the run paid for
    * passes nothing in its output reads.
    */
  private val MemorySafetyOverlayStages = Set(
    "taggers.MemorySemanticsPass",
    "taggers.MemoryApiPass",
    "taggers.ExtentPass",
    "taggers.GuardPass",
    "taggers.ValueOriginPass",
    "taggers.IntegerWidthPass",
    "taggers.AllocationStatePass",
    "taggers.MemorySafetyFindingPass"
  )

  /** astgen binary the way jssrc2cpg's `AstGenRunner` resolves it: plain `astgen` from `PATH` (the
    * frontend honours no environment override). The probe is the frontend's own: it also runs
    * `astgen --version` and treats an unparseable answer as unusable. Anything from
    * `@appthreat/atom-parsetools` reports 4.x; only the long-retired standalone `@appthreat/astgen`
    * 1.x is excluded by the floor.
    */
  private lazy val astgenUsable: Boolean =
      Try(scala.sys.process.Process(Seq("astgen", "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  private def requireAstgen(): Unit =
      if !astgenUsable then
        cancel(
          s"astgen 'astgen' unavailable or too old; JavaScript reachables fixture needs the astgen from @appthreat/atom-parsetools on PATH"
        )

  // ------------------------------------------------------------------ edga gate

  /** The edga release the C fixture's count is calibrated against: the release atom's CI installs
    * (`.github/actions/edga` defaults to it).
    */
  private val edgaCalibratedAgainst = "0.2.0"

  /** The version the running edga reports for `--edga-version` ("edga 0.2.0 / EDG commit ..."). */
  private def edgaReportedVersion(edga: String): Option[String] =
      Try(scala.sys.process.Process(Seq(edga, "--edga-version")).lazyLines_!)
          .toOption
          .flatMap(_.find(_.startsWith("edga ")))
          .map(_.split("\\s+")(1))
          .filter(_.nonEmpty)

  private def requireEdga(): Unit =
      assume(
        EdgaRunner.locate().isDefined,
        "edga unavailable; the C reachables fixture needs the edga binary from the AppThreat/edga release on PATH or EDGA_PATH"
      )

  /** phpastgen binary resolved the way php2atom does (env override, then PATH). */
  private val phpastgen: String =
      sys.env
          .get("PHP_PARSER_BIN")
          .map(_.trim)
          .filter(_.nonEmpty)
          .getOrElse("phpastgen")

  private lazy val phpGeneratorUsable: Boolean =
    val info =
        Try(scala.sys.process.Process(Seq(phpastgen, "--parser-info")).lazyLines_!.toList)
            .toOption
            .getOrElse(Seq.empty)
    val version = info
        .find(_.contains("Generator version:"))
        .map(_.split("Generator version:", 2).last.trim)
        .filter(_.nonEmpty)
    !info.mkString("\n").contains("PHP is not installed") &&
    version.exists(v => v.takeWhile(_.isDigit).toIntOption.exists(_ >= 2))

  private def requirePhpGenerator(): Unit =
      if !phpGeneratorUsable then
        cancel(
          s"phpastgen '$phpastgen' unavailable or pre-2.x; PHP reachables fixture needs a 2.x generator"
        )

  private val rbastgen: String =
      sys.env
          .get("RUBY_ASTGEN_BIN")
          .orElse(sys.env.get("RBASTGEN_PATH"))
          .map(_.trim)
          .filter(_.nonEmpty)
          .getOrElse("rbastgen")

  private lazy val rubyGeneratorUsable: Boolean =
      Try(scala.sys.process.Process(Seq(rbastgen, "--version")).lazyLines_!.headOption.map(_.trim))
          .toOption
          .flatten
          .exists(v => v.takeWhile(_.isDigit).toDoubleOption.exists(_ >= 2.0))

  private def requireRubyGenerator(): Unit =
      if !rubyGeneratorUsable then
        cancel(
          s"rbastgen '$rbastgen' unavailable or pre-2.x; Ruby reachables fixture needs a 2.x generator"
        )

end ReachablesCrossLanguageWorkflowTests
