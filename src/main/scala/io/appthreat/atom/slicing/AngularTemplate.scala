package io.appthreat.atom.slicing

import scala.collection.mutable
import scala.util.matching.Regex

/** The bare-name reads in an Angular template's expressions, scanned as text (the frontend lowers
  * only some of an external template's bindings, and none of an inline `template` string).
  *
  * Expressions are those of `{{ e }}`, bound attributes (`[prop]="e"`, `(event)="e"`,
  * `[(model)]="e"`, `*directive="e"`, `bind-x="e"`, `on-x="e"`, quoted or not), the control flow
  * blocks' parameters (`@if (e)`, `@else if (e)`, `@for (x of e; track t)`, `@switch (e)`, `@case
  * (e)`, and only the `when` conditions of `@defer (...)`) and `@let x = e;`.
  *
  * A read is an identifier that is not a member (`a.b`, `a?.b`, `a!.b`), not inside a string
  * literal, not a pipe name (`| date`), not an object key, not a name Angular provides (`$event`,
  * `$index`, ...), not a keyword of a structural directive's or control flow block's microsyntax
  * (`of`, `track`, `index`, ...), and not a name the template declares where that declaration is in
  * scope: a reference (`#box`, `ref-box`) in the whole template; `let x`/`as x` of a structural
  * directive (and `let-x` of an `ng-template`) in its element; `@for (x of ...)` and `@if (...; as
  * x)` in their block; `@let x` from there to the end of its block.
  */
object AngularTemplate:

  private val Interpolation = """\{\{([\s\S]*?)\}\}""".r
  private val Binding =
      ("""(?:^|\s)(\[\(?[^\]=\s]+\)?\]|\([^)=\s]+\)|\*[\w.-]+|bind-[\w.-]+|on-[\w.-]+)""" +
          """\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s"'=<>`]+))""").r
  private val ControlFlow = """@(if|else\s+if|for|switch|case|defer)\s*\(""".r
  private val Let         = """@let\s+([A-Za-z_$][\w$]*)\s*=\s*([^;]*);""".r
  private val Tag         = """<(/?)([A-Za-z][\w.:-]*)((?:[^>"']|"[^"]*"|'[^']*')*?)(/?)>""".r
  private val Read =
      """(?<![\w$.]|\?\.)([A-Za-z_$][\w$]*)((?:!?\??\.[A-Za-z_$][\w$]*)*)(\s*\()?""".r
  private val MicroDeclared = """(?:\blet\s+|\bas\s+)([A-Za-z_$][\w$]*)""".r
  private val Quoted        = """"[^"]*"|'[^']*'""".r

  private val VoidElements = Set(
    "area",
    "base",
    "br",
    "col",
    "embed",
    "hr",
    "img",
    "input",
    "link",
    "meta",
    "source",
    "track",
    "wbr"
  )
  private val Provided =
      Set("$event", "$any", "$implicit", "$index", "$count", "$first", "$last", "$even", "$odd")
  private val Microsyntax =
      Set(
        "let",
        "of",
        "as",
        "track",
        "trackBy",
        "index",
        "count",
        "first",
        "last",
        "even",
        "odd",
        "else",
        "then",
        "when"
      )

  /** An expression: [start, end), and whether it is microsyntax. */
  final case class Span(start: Int, end: Int, micro: Boolean)

  /** A declared name and the [from, to) offsets where it is in scope. */
  private final case class Declaration(name: String, from: Int, to: Int)

  /** The offset just past the `}` closing the block that opens at or after `from`. */
  private def blockEnd(text: String, from: Int): Int =
    val open = text.indexOf('{', from)
    if open < 0 then text.length
    else
      var depth = 0
      var i     = open
      var end   = text.length
      while i < text.length && end == text.length do
        if text(i) == '{' then depth += 1
        else if text(i) == '}' then
          depth -= 1
          if depth == 0 then end = i + 1
        i += 1
      end

  /** The offset of the `)` closing a parenthesis opened just before `from`. */
  private def closingParen(text: String, from: Int): Int =
    var depth = 1
    var i     = from
    while i < text.length && depth > 0 do
      if text(i) == '(' then depth += 1
      else if text(i) == ')' then depth -= 1
      i += 1
    i - 1

  /** Start tags (`<x ...>`, not `</x>`). */
  private def startTags(text: String): Iterator[Regex.Match] =
      Tag.findAllMatchIn(text).filter(_.group(1).isEmpty)

  /** The end of each element (past its closing tag), by the offset of its start tag. */
  private def elementEnds(text: String): Map[Int, Int] =
    val ends  = mutable.HashMap[Int, Int]()
    val stack = mutable.Stack[(String, Int)]()
    Tag.findAllMatchIn(text).foreach { m =>
      val name = m.group(2).toLowerCase
      if m.group(1) == "/" then
        val at = stack.indexWhere(_._1 == name)
        if at >= 0 then
          (0 to at).foreach { _ =>
            val (_, start) = stack.pop()
            ends(start) = m.end
          }
      else if m.group(4) == "/" || VoidElements.contains(name) then ends(m.start) = m.end
      else stack.push((name, m.start))
    }
    stack.foreach((_, start) => ends(start) = text.length)
    ends.toMap

  /** The values of a start tag's bound attributes: (absolute start, end, value, structural). */
  private def boundValues(tag: Regex.Match): Iterator[(Int, Int, String, Boolean)] =
      Binding.findAllMatchIn(tag.group(3)).map { m =>
        val group = (2 to 4).find(g => m.group(g) != null).get
        (
          tag.start(3) + m.start(group),
          tag.start(3) + m.end(group),
          m.group(group),
          m.group(1).startsWith("*")
        )
      }

  /** Blank the contents of string literals (keeping offsets), so their text is never a read. */
  private def withoutStrings(expr: String): String =
    val out         = new StringBuilder(expr)
    var quote: Char = 0
    var i           = 0
    while i < expr.length do
      val c = expr(i)
      if quote != 0 then
        if c == '\\' then
          out.setCharAt(i, ' ')
          if i + 1 < expr.length then out.setCharAt(i + 1, ' ')
          i += 1
        else if c == quote then quote = 0
        else out.setCharAt(i, ' ')
      else if c == '"' || c == '\'' || c == '`' then quote = c
      i += 1
    out.toString

  /** The template's expressions. */
  def expressionSpans(text: String): List[Span] =
    val spans = mutable.ListBuffer[Span]()
    def add(start: Int, end: Int, micro: Boolean = false): Unit =
        if end > start then spans += Span(start, end, micro)
    Interpolation.findAllMatchIn(text).foreach(m => add(m.start(1), m.end(1)))
    startTags(text).flatMap(boundValues).foreach((s, e, _, micro) => add(s, e, micro))
    ControlFlow.findAllMatchIn(text).foreach { m =>
      val end = closingParen(text, m.end)
      if m.group(1) == "defer" then
        // the triggers are not expressions; the `when` conditions are
        """\bwhen\s+([^;]*)""".r.findAllMatchIn(text.substring(m.end, end)).foreach(w =>
            add(m.end + w.start(1), m.end + w.end(1))
        )
      else add(m.end, end, micro = true)
    }
    Let.findAllMatchIn(text).foreach(m => add(m.start(2), m.end(2)))
    spans.toList.sortBy(s => (s.start, s.end))

  private def declarations(text: String): List[Declaration] =
    val decls = mutable.ListBuffer[Declaration]()
    val ends  = elementEnds(text)
    startTags(text).foreach { tag =>
      val element = ends.getOrElse(tag.start, text.length)
      // attribute names only: `href="#top"` declares nothing
      val names =
          Quoted.replaceAllIn(tag.group(3), v => Regex.quoteReplacement(" " * v.matched.length))
      """(?:^|\s)(?:#|ref-)([A-Za-z_$][\w$-]*)""".r.findAllMatchIn(names).foreach(m =>
          decls += Declaration(m.group(1), 0, text.length)
      )
      """(?:^|\s)let-([A-Za-z_$][\w$-]*)""".r.findAllMatchIn(names).foreach(m =>
          decls += Declaration(m.group(1), tag.start, element)
      )
      boundValues(tag).filter(_._4).foreach { (_, _, value, _) =>
          MicroDeclared.findAllMatchIn(value).foreach(d =>
              decls += Declaration(d.group(1), tag.start, element)
          )
      }
    }
    ControlFlow.findAllMatchIn(text).foreach { m =>
      val paren = closingParen(text, m.end)
      val block = blockEnd(text, paren)
      val head  = text.substring(m.end, paren)
      if m.group(1) == "for" then
        """^\s*([A-Za-z_$][\w$]*)\s+of\b""".r.findFirstMatchIn(head).foreach(d =>
            decls += Declaration(d.group(1), m.start, block)
        )
      if m.group(1) == "for" || m.group(1).endsWith("if") then
        MicroDeclared.findAllMatchIn(head).foreach(d =>
            decls += Declaration(d.group(1), m.start, block)
        )
    }
    Let.findAllMatchIn(text).foreach { m =>
      // to the end of the enclosing block: the first unmatched `}` after the declaration
      var depth = 0
      var i     = m.end
      var to    = text.length
      while i < text.length && to == text.length do
        if text(i) == '{' then depth += 1
        else if text(i) == '}' then
          if depth == 0 then to = i else depth -= 1
        i += 1
      decls += Declaration(m.group(1), m.start, to)
    }
    decls.toList
  end declarations

  // an assignment or update of the name just matched (`x = v`, `x += v`, `x++`), not `x == v`
  private val Assigned =
      """(?s)\s*(?:(?:[-+*/%&|^]|\*\*|<<|>>>?|&&|\|\||\?\?)?=(?!=)|\+\+|--).*"""

  /** The names a template's expressions assign (`(click)="open = true"`): template statements write
    * component members.
    */
  def writes(text: String): Set[String] =
      names(text).collect { case (_, name, Nil, _, true) => name }.toSet

  /** (offset, name, member path, called) of every bare-name read. */
  def reads(text: String): List[(Int, String, List[String], Boolean)] =
      names(text).collect {
          case (at, name, members, called, assigned) if !(assigned && members.isEmpty) =>
              (at, name, members, called)
      }

  /** (offset, name, member path, called, assigned) of every bare name. */
  private def names(text: String): List[(Int, String, List[String], Boolean, Boolean)] =
    val declared = declarations(text)
    expressionSpans(text).flatMap { span =>
      val expr = withoutStrings(text.substring(span.start, span.end))
      Read.findAllMatchIn(expr).flatMap { m =>
        val name   = m.group(1)
        val at     = span.start + m.start
        val before = expr.substring(0, m.start)
        val after  = expr.substring(m.end)
        val isPipe = before.matches("(?s).*\\|\\s*") && !before.matches("(?s).*\\|\\|\\s*")
        val isKey  = before.matches("(?s).*[{,]\\s*") && after.matches("(?s)\\s*:.*")
        val isKeyword = span.micro && Microsyntax.contains(name) && m.group(2).isEmpty &&
            m.group(3) == null
        val isDeclared = declared.exists(d => d.name == name && d.from <= at && at < d.to)
        if isDeclared || Provided.contains(name) || isPipe || isKey || isKeyword then None
        else
          val members = m.group(2).split("""!?\??\.""").toList.filter(_.nonEmpty)
          Some((at, name, members, m.group(3) != null, after.matches(Assigned)))
      }.toList
    }
  end names
end AngularTemplate
