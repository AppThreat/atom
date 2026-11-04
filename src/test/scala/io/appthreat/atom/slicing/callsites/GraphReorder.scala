package io.appthreat.atom.slicing.callsites

import io.shiftleft.codepropertygraph.generated.Cpg
import overflowdb.Node

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/** A copy of an atom's graph as another build of the same input could have made it.
  *
  * The streaming passes of the frontends (one part per file, and one per config file) apply their
  * diffs in the order the parts finish, so two builds of one input give the same graph with its
  * nodes created, numbered and linked in a different order. A slice that depends on that order (an
  * `.id()`, the first of several matches, a tie in `minBy`, a map built from several nodes) differs
  * between builds, and building a fixture twice shows it only when the threads happen to finish
  * differently. This copy makes the difference certain: the nodes of each part (a file, a config
  * file) keep their order and their relative links, the parts come in the opposite order, and every
  * node gets the id of its new position. Edges are added in the order of their source node's new
  * position, so a node's out edges keep their order and its in edges come in the order of the parts
  * that made them.
  *
  * Nodes that belong to no file (the metadata, the type nodes, the stubs) come first, in their
  * original order.
  */
object GraphReorder:

  /** The part a node was made in: the file of its method or type declaration, found through the AST
    * parents, or the file or config file node itself. A tag belongs to the part of what it tags.
    */
  private def partOf(n: Node, cache: mutable.HashMap[Long, String]): String =
      cache.get(n.id()) match
        case Some(part) => part
        case None =>
            cache.update(n.id(), "") // a cycle in the AST would otherwise never end
            val part = compute(n, cache)
            cache.update(n.id(), part)
            part

  private def compute(n: Node, cache: mutable.HashMap[Long, String]): String =
    def firstIn(edge: String): Option[Node] = n.in(edge).asScala.nextOption()
    val properties                          = n.propertiesMap()
    n.label match
      case "CONFIG_FILE" => s"config:${properties.get("NAME")}"
      case "FILE"        => s"file:${properties.get("NAME")}"
      case "TAG"         => firstIn("TAGGED_BY").map(partOf(_, cache)).getOrElse("")
      case _ =>
          Option(properties.get("FILENAME")) match
            case Some(filename) => s"file:$filename"
            case None           => firstIn("AST").map(partOf(_, cache)).getOrElse("")

  /** The parts of a graph (files, config files) in the order their first node was created. */
  def parts(src: Cpg): Vector[String] =
    val cache = mutable.HashMap.empty[Long, String]
    src.graph.nodes().asScala.toVector.sortBy(_.id()).map(partOf(_, cache)).filter(_.nonEmpty)
        .distinct

  /** `src` with the parts of the build in the opposite order. */
  def reversed(src: Cpg): Cpg = reordered(src, parts => parts.reverse)

  /** `src` with the parts of the build in the order `order` gives. */
  def reordered(src: Cpg, order: Vector[String] => Vector[String]): Cpg =
    val cache = mutable.HashMap.empty[Long, String]
    val nodes = src.graph.nodes().asScala.toVector.sortBy(_.id())
    val byPart =
        nodes.foldLeft(mutable.LinkedHashMap.empty[String, mutable.ArrayBuffer[Node]]) {
            (acc, n) =>
              acc.getOrElseUpdate(partOf(n, cache), mutable.ArrayBuffer.empty) += n
              acc
        }
    val partNames = byPart.keys.filter(_.nonEmpty).toVector
    val sequence = byPart.getOrElse("", mutable.ArrayBuffer.empty).toVector ++
        order(partNames).flatMap(byPart(_))
    copyInOrder(src, sequence)

  /** `src` with every node created at a random position (the same for the same `seed`), so that not
    * even the order of the nodes of one file survives. A slice that gives the same bytes for every
    * seed depends on no creation order at all.
    */
  def shuffled(src: Cpg, seed: Long): Cpg =
      copyInOrder(src, new scala.util.Random(seed).shuffle(src.graph.nodes().asScala.toVector))

  // The copy needs the ids and the edge order it chooses, which a diff graph assigns itself; the
  // direct node API that lets it choose is the deprecated one.
  @scala.annotation.nowarn("cat=deprecation")
  private def copyInOrder(src: Cpg, sequence: Vector[Node]): Cpg =
    val copy = Cpg.empty
    val ids  = mutable.HashMap.empty[Long, Node]
    sequence.zipWithIndex.foreach { (n, position) =>
      val made = copy.graph.addNode(position + 1L, n.label, keyValues(n.propertiesMap())*)
      ids.update(n.id(), made)
    }
    sequence.foreach { n =>
      val from = ids(n.id())
      n.outE().asScala.foreach { e =>
          from.addEdgeSilent(e.label(), ids(e.inNode().id()), keyValues(e.propertiesMap())*)
      }
    }
    copy

  private def keyValues(properties: java.util.Map[String, AnyRef]): Seq[AnyRef] =
      properties.asScala.toSeq.flatMap((k, v) => Seq[AnyRef](k, v))
end GraphReorder
