package io.appthreat.atom.slicing.callsites

import io.appthreat.x2cpg.passes.ExternalPathMatcher
import io.appthreat.x2cpg.passes.taggers.{BomComponentLocations, BomFiles, BomLocatedComponent}
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.NewConfigFile
import io.shiftleft.semanticcpg.language.*
import overflowdb.BatchedUpdate

import java.nio.file.Paths
import scala.util.Try

/** What a vendored path is to the project: the module a use of it names and, when a BOM component
  * owns the path, that component's purl.
  */
final case class Vendored(module: String, purl: String)

/** The code the project carries from someone else (decision 0001, P5), from the two sources atom
  * knows: the `--external-paths` pattern (matched by [[ExternalPathMatcher]], the one meaning every
  * frontend shares) and the directories a BOM gives its components ([[BomComponentLocations]]).
  *
  * A path under a component's directory is that component's, with its purl and its name as module;
  * the deepest component wins. A path only the pattern names has the matched directory as module
  * and no purl. Decided when the atom is built and stored in it (`callsites-vendored.json`), so a
  * later `callsites` run sees what the build marked, and so that the input need not be on disk
  * again.
  */
final class VendoredCode(
  val externalPaths: String,
  val components: Seq[BomLocatedComponent]
):
  private val matcher = ExternalPathMatcher(externalPaths)

  // deepest directory first: a component inside another component's directory owns its files
  private val owners = components.sortBy(c => (-c.path.count(_ == '/'), c.path))

  def isEmpty: Boolean = matcher.isEmpty && components.isEmpty

  /** The vendored code a file (relative to the input root) belongs to. */
  def of(file: String): Option[Vendored] =
    val path = ExternalPathMatcher.normalize(file)
    owners.find(c => path == c.path || path.startsWith(c.path + "/"))
        .map(c => Vendored(c.module, c.purl))
        .orElse(matcher.vendoredPathOf(path).map(Vendored(_, "")))

  def isVendored(file: String): Boolean = of(file).isDefined
end VendoredCode

object VendoredCode:

  val FileName = "callsites-vendored.json"

  private val log = org.slf4j.LoggerFactory.getLogger(getClass)

  private given io.circe.Encoder[BomLocatedComponent] =
      io.circe.generic.semiauto.deriveEncoder[BomLocatedComponent]

  private given io.circe.Decoder[BomLocatedComponent] =
      io.circe.generic.semiauto.deriveDecoder[BomLocatedComponent]

  private final case class Stored(externalPaths: String, components: List[BomLocatedComponent])

  private given io.circe.Encoder[Stored] = io.circe.generic.semiauto.deriveEncoder[Stored]
  private given io.circe.Decoder[Stored] = io.circe.generic.semiauto.deriveDecoder[Stored]

  /** The vendored code of an atom: what the build stored, else (an atom built before this, or
    * without BOM or pattern) the pattern given to this run and the BOMs the atom carries, read now
    * against the input root. `externalPaths` of this run replaces the stored pattern.
    */
  def forAtom(atom: Cpg, externalPaths: Option[String] = None): VendoredCode =
    val stored = atom.configFile.name(FileName).content.headOption
        .flatMap(c => io.circe.parser.decode[Stored](c).toOption)
    val pattern = externalPaths.filter(_.nonEmpty)
        .orElse(stored.map(_.externalPaths).filter(_.nonEmpty))
        .orElse(CProjectLayout.read(atom).map(_.externalPaths).filter(_.nonEmpty))
        .getOrElse("")
    val components = stored.map(_.components).getOrElse(locate(atom))
    VendoredCode(pattern, components)

  /** The located components of the BOMs the atom carries, against its input root. */
  def locate(atom: Cpg): List[BomLocatedComponent] =
    val boms = BomFiles.contents(atom)
    if boms.isEmpty then Nil
    else
      atom.metaData.root.headOption.flatMap(r => Try(Paths.get(r)).toOption) match
        case Some(root) => BomComponentLocations.read(boms, root).toList
        case None       => Nil

  /** Decided at build time: the pattern of this run and the BOMs' component locations. Marks the
    * located components' files external (the pattern's were marked by the frontend) and stores the
    * decision; stores nothing when there is neither, so such an atom is the one main builds.
    */
  def storeAndMark(atom: Cpg, externalPaths: String): VendoredCode =
    val components = locate(atom)
    val vendored   = VendoredCode(externalPaths, components)
    if !vendored.isEmpty then
      // The pattern was applied by the frontends that finish their graph in `createCpg`; the
      // BOM's locations, and the pattern for JavaScript, whose post-processing runs after it
      // (see JsSrc2Cpg.createCpgWithAllOverlays), are marked here. Only internal nodes change, so
      // running it again after a frontend's own marking is a no-op.
      new io.appthreat.x2cpg.passes.ExternalPathsPass(atom, vendored.isVendored).createAndApply()
      Try {
          import io.circe.syntax.*
          val diff = new BatchedUpdate.DiffGraphBuilder
          atom.configFile.name(FileName).foreach(diff.removeNode)
          diff.addNode(
            NewConfigFile().name(FileName).content(
              Stored(externalPaths, components.toList).asJson.spaces2
            )
          )
          BatchedUpdate.applyDiff(atom.graph, diff)
      }.failed.foreach(e => log.warn(s"callsites: could not store the vendored code: $e"))
    vendored
  end storeAndMark
end VendoredCode
