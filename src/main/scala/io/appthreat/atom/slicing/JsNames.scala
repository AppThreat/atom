package io.appthreat.atom.slicing

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Operators
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

/** Module and symbol names for JavaScript/TypeScript calls.
  *
  * The JS frontends name an imported callee `<module>:<path>`: `node:fs:readFile`,
  * `@babel/parser:parse`, `tar:Parser:on`, `node:process:process:exit`, `@angular/common/http:x`.
  * The module part can itself contain `:` (`node:fs`) and `/` (scoped packages, subpaths), so it is
  * matched against the module specifiers this project actually imports (string literals of
  * `require`/`import` calls) rather than split at the first separator. `node:` builtins are
  * recognised without an import. Project files (relative specifiers, `::program` names) and the JS
  * runtime (`__ecma.*`, `__whatwg.*`) have no module.
  */
final class JsNames(atom: Cpg):

  /** Bare module specifiers imported anywhere in the project, longest first. */
  private val knownModules: List[String] =
      atom.call.nameExact("require", "import").flatMap(JsNames.literalSpecifier)
          .filter(JsNames.isBareSpecifier).dedup.l.distinct.sortBy(m => (-m.length, m))

  /** (module, symbol) for a resolved callee name; symbol is absent for the module itself. */
  def split(fullName: String): Option[(String, Option[String])] =
      JsNames.split(knownModules, fullName)

  /** (module, symbol path) of an import binding's defining expression: `require("x")`,
    * `require("x").a.b`, `await import("x")`, and the frontend's `_tmp` lowering of them.
    */
  def ofImportExpression(c: Call): Option[(String, Option[String])] =
    def go(e: Expression, depth: Int): Option[(String, List[String])] =
        if depth > 8 then None
        else
          e match
            case call: Call if call.name == "require" || call.name == "import" =>
                JsNames.literalSpecifier(call).map(_ -> Nil)
            case call: Call if call.name == Operators.fieldAccess =>
                val field = call.argument.collectFirst { case f: FieldIdentifier =>
                    f.canonicalName
                }
                call.argument.collectFirst { case b: Expression if b.argumentIndex == 1 => b }
                    .flatMap(go(_, depth + 1)).map((m, p) => (m, p ++ field.toList))
            case call: Call if call.name == "<operator>.await" =>
                call.argument.collectFirst { case b: Expression => b }.flatMap(go(_, depth + 1))
            case _ => None
    go(c, 0).filter((m, _) => JsNames.isBareSpecifier(m))
        .map((m, p) => m -> Option(p.mkString(".")).filter(_.nonEmpty))
end JsNames

object JsNames:

  /** (module, symbol) of `fullName` given the imported module specifiers, longest first. */
  def split(knownModules: Seq[String], fullName: String): Option[(String, Option[String])] =
      if !isResolvedName(fullName) || fullName.contains("::program") then None
      else
        val module = knownModules.find(m => fullName == m || fullName.startsWith(m + ":"))
            .orElse(nodeBuiltin(fullName))
        module.map { m =>
          val rest = fullName.stripPrefix(m).stripPrefix(":")
          m -> Option(rest.replace(':', '.')).filter(_.nonEmpty)
        }

  private val sourceTextChars = "\"'`(){}[]<>=,;".toSet

  /** A callee name the frontend actually resolved: not a placeholder and not source text (the JS
    * frontend falls back to code for some callees, e.g. template literals and JSX).
    */
  def isResolvedName(name: String): Boolean =
      name.nonEmpty && name != "ANY" &&
          !DefComponent.unresolvedCallPattern.matcher(name).matches() &&
          !name.exists(ch => ch.isWhitespace || sourceTextChars.contains(ch))

  def isBareSpecifier(s: String): Boolean =
      s.nonEmpty && !s.startsWith(".") && !s.startsWith("/") && !s.startsWith("#") &&
          !s.contains("://")

  private def nodeBuiltin(fullName: String): Option[String] =
      if fullName.startsWith("node:") then
        fullName.drop(5).takeWhile(_ != ':') match
          case ""   => None
          case name => Some(s"node:$name")
      else None

  def literalSpecifier(c: Call): Option[String] =
      c.argument.collectFirst {
          case l: Literal if l.argumentIndex >= 1 => l.code
      }.map(_.stripPrefix("\"").stripSuffix("\"").stripPrefix("'").stripSuffix("'")
          .stripPrefix("`").stripSuffix("`"))

  def isJs(language: Option[String]): Boolean =
      language.exists(l =>
          l == io.shiftleft.codepropertygraph.generated.Languages.JSSRC ||
              l == io.shiftleft.codepropertygraph.generated.Languages.JAVASCRIPT
      )
end JsNames
