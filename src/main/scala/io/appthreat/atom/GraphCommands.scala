package io.appthreat.atom

import better.files.File
import io.shiftleft.codepropertygraph.CpgAlgorithms.*
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.Method
import io.shiftleft.semanticcpg.language.*
import overflowdb.formats.Exporter
import overflowdb.{Edge, Node}
import io.circe.Json
import io.circe.syntax.EncoderOps
import overflowdb.algorithm.{
    GetParents,
    LowestCommonAncestors,
    DependencySequencer,
    UnionFind,
    HeapWalker,
    ContextSensitivePathFinder
}

import java.nio.file.Paths
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

/** Implementations for the graph-level `export` and `algorithms` commands. Both operate on a built
  * or reused atom and reuse the export formats and algorithms provided by the underlying graph
  * library, so there is a single implementation per format and per algorithm.
  */
object GraphCommands:

  private val supportedFormats =
      Set("dot", "graphml", "gexf", "graphson", "neo4jcsv", "gnn")
  private val supportedScopes = Set("whole", "methods")
  private val supportedAlgorithms = Set(
    "scc",
    "toposort",
    "dominators",
    "paths",
    "centrality",
    "lowest-common-ancestors",
    "dependency-sequencer",
    "union-find",
    "heap-walker",
    "context-sensitive-paths"
  )

  /** Export the atom to one of the supported graph formats. */
  def runExport(cpg: Cpg, config: AtomExportConfig): Either[String, String] =
    val format = config.exportFormat.toLowerCase
    val scope  = config.scope.toLowerCase
    if !supportedFormats.contains(format) then
      Left(
        s"Unsupported export format '$format'. Supported: ${supportedFormats.toSeq.sorted.mkString(", ")}"
      )
    else if !supportedScopes.contains(scope) then
      Left(
        s"Unsupported export scope '$scope'. Supported: ${supportedScopes.toSeq.sorted.mkString(", ")}"
      )
    else
      val outDir = File(config.exportDir).createDirectoryIfNotExists(createParents = true)
      exporterFor(format) match
        case None => Left(s"No exporter available for format '$format'")
        case Some(exporter) =>
            scope match
              case "whole"   => exportWhole(cpg, exporter, outDir)
              case "methods" => exportMethods(cpg, exporter, format, outDir)
              case other     => Left(s"Unsupported export scope '$other'")
  end runExport

  private def exportWhole(cpg: Cpg, exporter: Exporter, outDir: File): Either[String, String] =
    val result = exporter.runExport(cpg.graph, outDir.path)
    println(
      s"Exported ${result.nodeCount} nodes and ${result.edgeCount} edges to ${outDir.pathAsString}"
    )
    result.additionalInfo.foreach(println)
    Right("Atom exported successfully")

  private def exportMethods(
    cpg: Cpg,
    exporter: Exporter,
    format: String,
    outDir: File
  ): Either[String, String] =
      if format == "neo4jcsv" then
        Left(
          "The neo4jcsv format writes one set of files per label and does not support the 'methods' " +
              "scope. Use '--scope whole' for neo4jcsv."
        )
      else
        val ext      = exporter.defaultFileExtension
        var exported = 0
        cpg.method.internal.foreach { method =>
          val nodes    = method.ast.l.map(_.asInstanceOf[Node])
          val edges    = inducedEdges(nodes)
          val fileName = s"${sanitize(method.fullName)}.$ext"
          exporter.runExport(nodes, edges, outDir.path.resolve(fileName))
          exported += 1
        }
        println(s"Exported $exported method subgraphs to ${outDir.pathAsString}")
        Right("Atom exported successfully")
  end exportMethods

  /** Run a graph algorithm and write the result as JSON to the configured output file. */
  def runAlgorithms(cpg: Cpg, config: AtomAlgorithmsConfig): Either[String, String] =
    val algo = config.algoType.toLowerCase
    if !supportedAlgorithms.contains(algo) then
      Left(
        s"Unsupported algorithm '$algo'. Supported: ${supportedAlgorithms.toSeq.sorted.mkString(", ")}"
      )
    else
      val resultJson = algo match
        case "scc"                     => Right(sccReport(cpg))
        case "toposort"                => toposortReport(cpg)
        case "centrality"              => Right(centralityReport(cpg))
        case "dominators"              => Right(dominatorsReport(cpg))
        case "paths"                   => pathsReport(cpg, config)
        case "lowest-common-ancestors" => lowestCommonAncestorsReport(cpg, config)
        case "dependency-sequencer"    => dependencySequencerReport(cpg)
        case "union-find"              => Right(unionFindReport(cpg))
        case "heap-walker"             => heapWalkerReport(cpg, config)
        case "context-sensitive-paths" => contextSensitivePathsReport(cpg, config)
        case other                     => Left(s"Unsupported algorithm '$other'")
      resultJson.map { json =>
        val outFile = File(config.outputSliceFile.pathAsString)
            .createFileIfNotExists(createParents = true)
            .write(json.spaces2)
        s"Algorithm '$algo' result written to ${outFile.pathAsString}"
      }
    end if
  end runAlgorithms

  // The call graph: a method points at the methods it directly calls.
  private def callees(node: Node): Iterator[Node] =
      node match
        case m: Method => m.call.flatMap(_.out("CALL").asScala)
        case _         => Iterator.empty

  private def methodNodes(cpg: Cpg): Seq[Node] =
      cpg.method.l.map(_.asInstanceOf[Node])

  private def methodNamesById(cpg: Cpg): Map[Long, String] =
      cpg.method.l.map(m => m.id() -> m.fullName).toMap

  private def sccReport(cpg: Cpg): Json =
    val names      = methodNamesById(cpg)
    val components = methodNodes(cpg).stronglyConnectedComponents(callees)
    // Components with more than one member (or a self-loop) indicate recursion in the call graph.
    val recursive = components.filter(_.size > 1)
    Json.obj(
      "componentCount"          -> Json.fromInt(components.size),
      "recursiveComponentCount" -> Json.fromInt(recursive.size),
      "recursiveComponents" -> recursive
          .map(c => c.toSeq.flatMap(n => names.get(n.id())).sorted.asJson)
          .asJson
    )

  /** Orders methods callee-before-caller, which is the order a summary-based analysis wants to
    * process them in. Recursion would make a plain topological sort impossible, so recursive
    * methods are first grouped into strongly connected components and the resulting acyclic
    * condensation is ordered instead. Each entry in the result is one stage: a group of methods
    * that can be handled together, flagged as recursive when it holds more than one method or a
    * self-call.
    */
  private def toposortReport(cpg: Cpg): Either[String, Json] =
      Try {
          val names      = methodNamesById(cpg)
          val nodes      = methodNodes(cpg)
          val components = nodes.stronglyConnectedComponents(callees)

          val componentOfNode = scala.collection.mutable.HashMap.empty[Long, Int]
          components.zipWithIndex.foreach { case (component, index) =>
              component.foreach(node => componentOfNode(node.id()) = index)
          }
          val selfRecursive =
              nodes.filter(n => callees(n).exists(_.id() == n.id())).map(_.id()).toSet

          val size       = components.size
          val successors = Array.fill(size)(scala.collection.mutable.Set.empty[Int])
          val inDegree   = Array.fill(size)(0)
          nodes.foreach { node =>
            val from = componentOfNode(node.id())
            callees(node).foreach { callee =>
                componentOfNode.get(callee.id()).foreach { to =>
                    if to != from && !successors(from).contains(to) then
                      successors(from) += to
                      inDegree(to) += 1
                }
            }
          }

          // Kahn's algorithm over the condensation, which is acyclic by construction.
          val queue = scala.collection.mutable.Queue.empty[Int]
          (0 until size).foreach(index => if inDegree(index) == 0 then queue.enqueue(index))
          val callerFirst = scala.collection.mutable.ArrayBuffer.empty[Int]
          while queue.nonEmpty do
            val current = queue.dequeue()
            callerFirst += current
            successors(current).foreach { to =>
              inDegree(to) -= 1
              if inDegree(to) == 0 then queue.enqueue(to)
            }

          val stages = callerFirst.reverse.map { index =>
            val component   = components(index)
            val memberIds   = component.map(_.id())
            val isRecursive = component.size > 1 || memberIds.exists(selfRecursive.contains)
            Json.obj(
              "methods"   -> component.toSeq.flatMap(n => names.get(n.id())).sorted.asJson,
              "recursive" -> Json.fromBoolean(isRecursive)
            )
          }
          val recursiveStageCount = components.count(c =>
              c.size > 1 || c.map(_.id()).exists(selfRecursive.contains)
          )
          Json.obj(
            "order"               -> stages.asJson,
            "recursiveStageCount" -> Json.fromInt(recursiveStageCount)
          )
      } match
        case Success(json) => Right(json)
        case Failure(ex)   => Left(s"Topological sort failed: ${ex.getMessage}")

  private def centralityReport(cpg: Cpg): Json =
    val names    = methodNamesById(cpg)
    val nodes    = methodNodes(cpg)
    val ranks    = nodes.pageRank(callees)
    val inDegree = nodes.inDegreeCentrality(callees)
    val ranked = names.keys.toSeq
        .sortBy(id => -ranks.getOrElse(id, 0.0))
        .map { id =>
            Json.obj(
              "method"   -> names(id).asJson,
              "pageRank" -> Json.fromDoubleOrNull(ranks.getOrElse(id, 0.0)),
              "inDegree" -> Json.fromInt(inDegree.getOrElse(id, 0))
            )
        }
    Json.obj("ranking" -> ranked.asJson)

  private def dominatorsReport(cpg: Cpg): Json =
    val perMethod = cpg.method.internal.l.map { method =>
      val node  = method.asInstanceOf[Node]
      val idoms = node.dominatorTree(n => n.out("CFG").asScala)
      val entries = idoms.toSeq.map { case (nodeId, idomId) =>
          Json.obj("node" -> Json.fromLong(nodeId), "immediateDominator" -> Json.fromLong(idomId))
      }
      Json.obj("method" -> method.fullName.asJson, "dominators" -> entries.asJson)
    }
    Json.obj("methods" -> perMethod.asJson)

  private def pathsReport(cpg: Cpg, config: AtomAlgorithmsConfig): Either[String, Json] =
      (config.sourceSelector, config.targetSelector) match
        case (Some(srcPat), Some(tgtPat)) =>
            val source = cpg.method.fullName(srcPat).headOption
            val target = cpg.method.fullName(tgtPat).headOption
            (source, target) match
              case (Some(s), Some(t)) =>
                  val depth = if config.maxDepth > 0 then config.maxDepth else 10
                  val paths = s.asInstanceOf[Node].pathsTo(t.asInstanceOf[Node], depth)
                  val jsonPaths = paths.map { p =>
                      p.nodes.map { n =>
                          Json.obj(
                            "id"    -> Json.fromLong(n.id()),
                            "label" -> n.label().asJson
                          )
                      }.asJson
                  }
                  Right(
                    Json.obj(
                      "source" -> s.fullName.asJson,
                      "target" -> t.fullName.asJson,
                      "paths"  -> jsonPaths.asJson
                    )
                  )
              case (None, _) => Left(s"No method matched the source selector '$srcPat'")
              case (_, None) => Left(s"No method matched the target selector '$tgtPat'")
            end match
        case _ =>
            Left(
              "The 'paths' algorithm needs a source and target selector. Provide them via the config " +
                  "file (source, target) or rerun with matching method patterns."
            )

  private def lowestCommonAncestorsReport(
    cpg: Cpg,
    config: AtomAlgorithmsConfig
  ): Either[String, Json] =
      config.sourceSelector match
        case Some(srcPat) =>
            val matched = cpg.method.fullName(srcPat).l
            if matched.isEmpty then
              Left(s"No methods matched the source selector '$srcPat'")
            else
              implicit val getParents: GetParents[Node] = new GetParents[Node]:
                override def apply(node: Node): Set[Node] =
                    node match
                      case m: Method =>
                          m.caller(using NoResolve).toSeq.map(_.asInstanceOf[Node]).toSet
                      case _ => Set.empty
              val nodesSet = matched.map(_.asInstanceOf[Node]).toSet
              val lcas     = LowestCommonAncestors(nodesSet)
              Right(
                Json.obj(
                  "matchedMethods" -> matched.map(_.fullName).sorted.asJson,
                  "lowestCommonAncestors" -> lcas.toSeq.map {
                      case m: Method => m.fullName
                      case n         => s"Node(id=${n.id()})"
                  }.sorted.asJson
                )
              )
            end if
        case None =>
            Left(
              "The 'lowest-common-ancestors' algorithm needs a source selector matching the methods to find LCAs for."
            )

  private def dependencySequencerReport(cpg: Cpg): Either[String, Json] =
      Try {
          val names      = methodNamesById(cpg)
          val nodes      = methodNodes(cpg)
          val components = nodes.stronglyConnectedComponents(callees)

          val componentOfNode = scala.collection.mutable.HashMap.empty[Long, Int]
          components.zipWithIndex.foreach { case (component, index) =>
              component.foreach(node => componentOfNode(node.id()) = index)
          }
          val selfRecursive =
              nodes.filter(n => callees(n).exists(_.id() == n.id())).map(_.id()).toSet

          val size       = components.size
          val successors = Array.fill(size)(scala.collection.mutable.Set.empty[Int])
          nodes.foreach { node =>
            val from = componentOfNode(node.id())
            callees(node).foreach { callee =>
                componentOfNode.get(callee.id()).foreach { to =>
                    if to != from && !successors(from).contains(to) then
                      successors(from) += to
                }
            }
          }

          implicit val getParents: GetParents[Int] = (index: Int) => successors(index).toSet

          val stages = DependencySequencer((0 until size).toSet)

          val stagesJson = stages.map { stage =>
              stage.toSeq.sorted.map { index =>
                val component   = components(index)
                val memberIds   = component.map(_.id())
                val isRecursive = component.size > 1 || memberIds.exists(selfRecursive.contains)
                Json.obj(
                  "methods"   -> component.toSeq.flatMap(n => names.get(n.id())).sorted.asJson,
                  "recursive" -> Json.fromBoolean(isRecursive)
                )
              }.asJson
          }

          Json.obj(
            "stages"     -> stagesJson.asJson,
            "stageCount" -> Json.fromInt(stages.size)
          )
      } match
        case Success(json) => Right(json)
        case Failure(ex)   => Left(s"Dependency sequencing failed: ${ex.getMessage}")

  private def unionFindReport(cpg: Cpg): Json =
    val names = methodNamesById(cpg)
    val nodes = methodNodes(cpg)
    val uf    = new UnionFind(nodes.size)
    nodes.foreach(n => uf.makeSet(n.id()))
    nodes.foreach { node =>
        callees(node).foreach { callee =>
            uf.union(node.id(), callee.id())
        }
    }
    val groups = nodes.groupBy(n => uf.find(n.id()))
    val componentsJson = groups.values.toSeq.map { component =>
        component.flatMap(n => names.get(n.id())).sorted.asJson
    }.sortBy(_.hcursor.as[Seq[String]].toOption.flatMap(_.headOption).getOrElse(""))
    Json.obj(
      "componentCount" -> Json.fromInt(groups.size),
      "components"     -> componentsJson.asJson
    )

  private def heapWalkerReport(cpg: Cpg, config: AtomAlgorithmsConfig): Either[String, Json] =
      config.sourceSelector match
        case Some(srcPat) =>
            val source = cpg.method.fullName(srcPat).headOption
            source match
              case Some(s) =>
                  val walker  = HeapWalker.forNode(s.asInstanceOf[Node], "AST")
                  val visited = scala.collection.mutable.ArrayBuffer.empty[Json]
                  while walker.hasNext do
                    val nextNode = walker.next()
                    visited += Json.obj(
                      "id"    -> Json.fromLong(nextNode.id()),
                      "label" -> nextNode.label().asJson,
                      "code"  -> nextNode.property("CODE", "").asJson
                    )
                  Right(
                    Json.obj(
                      "method"  -> s.fullName.asJson,
                      "astWalk" -> visited.asJson
                    )
                  )
              case None => Left(s"No method matched the source selector '$srcPat'")
        case None =>
            Left("The 'heap-walker' algorithm needs a source selector matching the root method.")

  private def contextSensitivePathsReport(
    cpg: Cpg,
    config: AtomAlgorithmsConfig
  ): Either[String, Json] =
      (config.sourceSelector, config.targetSelector) match
        case (Some(srcPat), Some(tgtPat)) =>
            val source = cpg.method.fullName(srcPat).headOption
            val target = cpg.method.fullName(tgtPat).headOption
            (source, target) match
              case (Some(s), Some(t)) =>
                  val depth = if config.maxDepth > 0 then config.maxDepth else 10
                  val getEdges: java.util.function.Function[
                    Node,
                    java.util.Iterator[ContextSensitivePathFinder.ContextEdge]
                  ] = (n: Node) =>
                    val list = new java.util.ArrayList[ContextSensitivePathFinder.ContextEdge]()
                    n match
                      case m: Method =>
                          m.call.foreach { c =>
                              list.add(new ContextSensitivePathFinder.ContextEdge(
                                c.asInstanceOf[Node],
                                ContextSensitivePathFinder.ContextEdge.Type.NEUTRAL,
                                0
                              ))
                          }
                          list.add(new ContextSensitivePathFinder.ContextEdge(
                            m.methodReturn.asInstanceOf[Node],
                            ContextSensitivePathFinder.ContextEdge.Type.NEUTRAL,
                            0
                          ))
                      case c: io.shiftleft.codepropertygraph.generated.nodes.Call =>
                          c.out("CALL").asScala.foreach { callee =>
                              list.add(new ContextSensitivePathFinder.ContextEdge(
                                callee,
                                ContextSensitivePathFinder.ContextEdge.Type.OPEN,
                                c.id()
                              ))
                          }
                          list.add(new ContextSensitivePathFinder.ContextEdge(
                            c.method.asInstanceOf[Node],
                            ContextSensitivePathFinder.ContextEdge.Type.NEUTRAL,
                            0
                          ))
                      case mr: io.shiftleft.codepropertygraph.generated.nodes.MethodReturn =>
                          mr.method.caller(using NoResolve).foreach { callerMethod =>
                              callerMethod.call.filter(c =>
                                  c.out("CALL").asScala.exists(_.id() == mr.method.id())
                              ).foreach { callNode =>
                                  list.add(new ContextSensitivePathFinder.ContextEdge(
                                    callNode.asInstanceOf[Node],
                                    ContextSensitivePathFinder.ContextEdge.Type.CLOSE,
                                    callNode.id()
                                  ))
                              }
                          }
                      case _ =>
                    end match
                    list.iterator()

                  val pathOpt = ContextSensitivePathFinder.findPath(
                    s.asInstanceOf[Node],
                    t.asInstanceOf[Node],
                    getEdges,
                    depth
                  )
                  if pathOpt.isPresent then
                    val pathNodes = pathOpt.get().nodes.asScala
                    val jsonPath = pathNodes.map { n =>
                        Json.obj(
                          "id"    -> Json.fromLong(n.id()),
                          "label" -> n.label().asJson,
                          "name" -> (n match
                            case m: Method => m.fullName.asJson
                            case _         => n.property("NAME", s"Node(id=${n.id()})").asJson
                          )
                        )
                    }.asJson
                    Right(
                      Json.obj(
                        "source" -> s.fullName.asJson,
                        "target" -> t.fullName.asJson,
                        "path"   -> jsonPath
                      )
                    )
                  else
                    Right(
                      Json.obj(
                        "source" -> s.fullName.asJson,
                        "target" -> t.fullName.asJson,
                        "path"   -> Json.arr()
                      )
                    )
                  end if
              case (None, _) => Left(s"No method matched the source selector '$srcPat'")
              case (_, None) => Left(s"No method matched the target selector '$tgtPat'")
            end match
        case _ =>
            Left(
              "The 'context-sensitive-paths' algorithm needs a source and target selector. Provide them via the config " +
                  "file (source, target) or rerun with matching method patterns."
            )

  private def inducedEdges(nodes: Seq[Node]): Seq[Edge] =
    val ids = nodes.map(_.id()).toSet
    nodes.flatMap(_.outE().asScala.filter(e => ids.contains(e.inNode().id())))

  private def sanitize(name: String): String =
    val cleaned = name.replaceAll("[^A-Za-z0-9._-]", "_")
    if cleaned.length > 180 then cleaned.take(180) else cleaned

  private def exporterFor(format: String): Option[Exporter] =
      format match
        case "dot"      => Some(overflowdb.formats.dot.DotExporter)
        case "graphml"  => Some(overflowdb.formats.graphml.GraphMLExporter)
        case "gexf"     => Some(overflowdb.formats.gexf.GexfExporter)
        case "graphson" => Some(overflowdb.formats.graphson.GraphSONExporter)
        case "neo4jcsv" => Some(overflowdb.formats.neo4jcsv.Neo4jCsvExporter)
        case "gnn"      => Some(overflowdb.formats.gnn.GnnExporter)
        case _          => None
end GraphCommands
