package io.appthreat.atom

import better.files.File as BFile
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.util.Try

/** Cross-language `reachables` regression fixtures.
  *
  * One small project per supported frontend, each with a source -> helper -> sink chain, driven
  * end-to-end through `Atom.run`: frontend, enhancement passes, taggers, the backward data-flow
  * engine and the reachable slicer.
  *
  * These exist because reachables output is language-agnostic shared code: until now exactly ONE
  * reachables test existed (PHP), which is how a 77% JS flow loss passed review during the Python
  * upgrade. Every fixture pins
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
  * of canceling with the reason.
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
    writeProject: BFile => Unit
  ): Seq[ujson.Value] =
    val projectDir = workspace / projectName
    projectDir.createDirectories()
    writeProject(projectDir)
    val atomFile  = workspace / s"$projectName.atom"
    val sliceFile = workspace / s"$projectName-slices.json"
    val result = Atom.run(
      Seq(
        "reachables",
        "--cache",
        "none",
        "-l",
        language,
        "-o",
        atomFile.pathAsString,
        "-s",
        sliceFile.pathAsString,
        projectDir.pathAsString
      ).toArray
    )
    result.isRight shouldBe true
    // reachables chunks at 1000 entries per file (`slices.json`, `slices_1.json`, ...): a
    // single-file read would understate every count on larger projects.
    val chunks = workspace.glob(s"$projectName-slices*.json").toSeq.sortBy(_.name)
    chunks should not be empty
    chunks.flatMap(f => ujson.read(f.contentAsString).arr.toSeq)
  end runReachables

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
          flows.length should be(4)
          // The request-derived local must reach the framework output sink.
          assertSourceToSinkFlow(flows, "javascript", "name", "send", "framework-output")
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

  // ------------------------------------------------------------------ generator gates

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
