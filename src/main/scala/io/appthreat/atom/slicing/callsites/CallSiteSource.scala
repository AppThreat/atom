package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.{DeclarationRefs, JsNames, ProgramCallSites}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.util.Try
import scala.util.control.NonFatal

/** One slice source per language: the dispatch on `metaData.language` picks the implementation, and
  * a language without a source still gets a slice (an empty one).
  */
trait CallSiteSource:
  def calculate(): ProgramCallSites

object CallSiteSource:

  /** The source for `atom`'s language; every language the slice does not cover returns an empty
    * slice.
    */
  def forAtom(atom: Cpg): CallSiteSource =
    val language = atom.metaData.language.headOption
    if JsNames.isJs(language) then JsCallSiteSource(atom)
    else if isPython(language) then PythonCallSiteSource(atom)
    else EmptyCallSiteSource

  private def isPython(language: Option[String]): Boolean =
      language.exists(l => l == Languages.PYTHONSRC || l == Languages.PYTHON)

  /** A language the slice does not cover (yet). */
  object EmptyCallSiteSource extends CallSiteSource:
    def calculate(): ProgramCallSites = ProgramCallSites(Nil)

/** Collects the records of one source run, and keeps one bad node from failing the whole slice: a
  * record whose construction throws is logged once with the node's id and skipped, and the slice is
  * always written (atom#317).
  */
final class RecordCollector(sourceName: String):

  private val logger      = LoggerFactory.getLogger(getClass)
  private val records     = mutable.LinkedHashSet.empty[CallSiteRecord]
  private val failedNodes = mutable.LinkedHashSet.empty[Long]
  private var skipped     = 0

  /** Adds the record built for `node`; an exception in the by-name `record` logs the node once. */
  def add(node: AstNode)(record: => CallSiteRecord): Unit =
      try records += record
      catch
        case NonFatal(e) =>
            if failedNodes.add(node.id()) then
              logger.warn(
                s"callsites[$sourceName]: skipped node ${node.id()} (${node
                        .label}) at line ${nodeLine(node)}: $e"
              )

  /** Adds records whose builders used the safe lookups and cannot throw. */
  def addAll(built: List[CallSiteRecord]): Unit = records ++= built

  /** A use with no enclosing method and no resolvable declaration cannot name a record: counted,
    * skipped.
    */
  def skip(node: AstNode): Unit =
    skipped += 1
    if failedNodes.add(node.id()) then
      logger.debug(
        s"callsites[$sourceName]: no method for node ${node.id()} (${node.label}) at line ${nodeLine(node)}"
      )

  private def nodeLine(node: AstNode): String =
      node.property(io.shiftleft.codepropertygraph.generated.PropertyNames.LINE_NUMBER, "?")

  def result(): ProgramCallSites =
    val failed = failedNodes.size
    if failed > 0 || skipped > 0 then
      println(
        s"Call sites: skipped $failed node(s) that failed and $skipped node(s) with no method"
      )
    ProgramCallSites(CallSiteRecord.dedupe(records))
end RecordCollector

/** The lookups every language source shares. Some frontends leave AST-detached nodes (outline's
  * destructured arrow parameters, PHP property-hook bodies), so no builder may assume a node has a
  * `.method` or a `.file`: the enclosing method is found through the AST parents, with the method
  * of the declaration the node REFs as the fallback, and the file follows the method.
  */
abstract class BaseCallSiteSource(protected val atom: Cpg, sourceName: String)
    extends CallSiteSource:

  protected val declRefs  = DeclarationRefs(atom)
  protected val collector = RecordCollector(sourceName)

  /** The method enclosing `n`, through the AST parents; `astParent` throws on detached nodes, so
    * the edge is read directly (see DeclarationRefs.parentOf).
    */
  protected def enclosingMethodOf(n: AstNode): Option[Method] =
      Iterator
          .iterate(DeclarationRefs.parentOf(n))(_.flatMap(DeclarationRefs.parentOf))
          .takeWhile(_.isDefined).flatten.collectFirst { case m: Method => m }

  /** The enclosing method of a use, falling back to the method of the declaration it REFs: the
    * frontend's lowering can detach an identifier from the AST while it keeps its REF edge.
    */
  protected def methodOf(use: AstNode): Option[Method] =
      enclosingMethodOf(use).orElse(
        use match
          case e: Expression =>
              e._refOut.collectFirst { case d: Declaration => d } match
                case Some(l: Local)             => enclosingMethodOf(l)
                case Some(p: MethodParameterIn) => enclosingMethodOf(p)
                case _                          => None
          case _ => None
      )

  /** The file a node sits in: its method's filename when there is one, else the file traversal,
    * which the detached nodes have no business relying on.
    */
  protected def fileOf(n: AstNode): Option[String] =
      methodOf(n).map(_.filename).orElse(Try(n.file.name.headOption).toOption.flatten)

  protected def line(n: AstNode): Integer = n match
    case e: Expression => e.lineNumber.getOrElse(-1)
    case t: TypeDecl   => t.lineNumber.getOrElse(-1)
    case _             => -1

  protected def col(n: AstNode): Integer = n match
    case e: Expression => e.columnNumber.getOrElse(-1)
    case _             => -1

  /** The callee's full name when the frontend resolved one, else empty (a placeholder, or the
    * source text the JS frontend falls back to for JSX elements).
    */
  protected def resolvedName(fullName: String): String =
      if JsNames.isResolvedName(fullName) then fullName else ""
end BaseCallSiteSource
