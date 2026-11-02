package io.appthreat.atom.slicing.callsites

import io.appthreat.x2cpg.passes.taggers.BomFiles
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.NewConfigFile
import io.shiftleft.semanticcpg.language.*
import overflowdb.BatchedUpdate

import scala.util.Try

/** The boundary configuration of one bytecode atom (atom#326): what the input declares as its own,
  * computed once and carried by the atom as a config file, with the rest of what the slice reads
  * from the input: the classes' real file names (atom#327), the synthetic line tables (atom#328),
  * the bundled Maven artifacts (atom#329) and the JVM bootstrap handles (atom#327).
  *
  * The boundary comes, in this order, from an explicit `--app-prefix` (repeatable, it replaces the
  * rest), what the input names itself (the APK manifest's `package` plus the packages of manifest
  * components that share its first two segments; the jar manifest's `Main-Class`,
  * `Automatic-Module-Name` or `Bundle-SymbolicName`; every class of a class directory), the BOM's
  * root component when one is given, and, for a jar that named nothing, the package prefixes that
  * hold at least 10 % of its classes. Code under a sibling package outside the boundary is a
  * dependency, by design.
  */
object JvmAppBoundary:

  val BoundaryFileName = "callsites-app-boundary.json"

  private val log = org.slf4j.LoggerFactory.getLogger(getClass)

  /** What the slice reads, and what the config file stores. */
  final case class BoundaryData(
    inputPath: String,
    appPrefixes: List[String],
    appAll: Boolean,
    boundarySource: String,
    classFiles: List[(String, String)],
    sourceFileGuesses: Int,
    syntheticLineTables: Int,
    methodsWithLineTables: Int,
    lineQuality: String,
    artifacts: List[(String, (String, String, String))],
    indySites: List[(String, List[JvmIndySite])]
  ):
    def boundary: AppBoundary = AppBoundary(appPrefixes, appAll, boundarySource)

  private given io.circe.Encoder[JvmMethodHandle] =
      io.circe.generic.semiauto.deriveEncoder[JvmMethodHandle]

  private given io.circe.Decoder[JvmMethodHandle] =
      io.circe.generic.semiauto.deriveDecoder[JvmMethodHandle]

  private given io.circe.Encoder[JvmIndySite] =
      io.circe.generic.semiauto.deriveEncoder[JvmIndySite]

  private given io.circe.Decoder[JvmIndySite] =
      io.circe.generic.semiauto.deriveDecoder[JvmIndySite]

  private given io.circe.Encoder[BoundaryData] =
      io.circe.generic.semiauto.deriveEncoder[BoundaryData]

  private given io.circe.Decoder[BoundaryData] =
      io.circe.generic.semiauto.deriveDecoder[BoundaryData]

  /** The input the atom was built from, as its metadata carries it. */
  def inputOf(atom: Cpg): JvmBytecodeInput =
      JvmBytecodeInput(atom.metaData.root.headOption.getOrElse(""))

  /** The boundary configuration for the atom: the config file it carries when it has one (an atom a
    * previous run stored), else what the input declares, computed once and stored for the runs that
    * reuse the atom. `--app-prefix` replaces a stored boundary and recomputes what depends on it,
    * in memory: the stored file keeps the input's own boundary, which a later run without the flag
    * must still see.
    */
  def boundaryOf(
    atom: Cpg,
    input: JvmBytecodeInput,
    appPrefixes: Option[Seq[String]]
  ): BoundaryData =
      appPrefixes match
        case Some(prefixes) if prefixes.nonEmpty =>
            val data = compute(atom, input)
            data.copy(
              appPrefixes = prefixes.toList,
              appAll = false,
              boundarySource = "app-prefix",
              syntheticLineTables = lineTablesUnder(
                data.inputPath,
                AppBoundary(prefixes.toList, appAll = false, "app-prefix")
              ).synthetic
            )
        case _ =>
            readStored(atom).getOrElse {
                val data = compute(atom, input)
                store(atom, data)
                data
            }
  end boundaryOf

  private given io.circe.Encoder[LineTables] = io.circe.Encoder.instance { t =>
    import io.circe.syntax.*
    io.circe.Json.obj(
      "synthetic"     -> t.synthetic.asJson,
      "withPositions" -> t.withPositions.asJson,
      "quality"       -> (if t.syntheticQuality then "synthetic" else "source").asJson
    )
  }

  private given io.circe.Decoder[LineTables] = io.circe.Decoder.instance { c =>
      for
        s <- c.downField("synthetic").as[Int]
        p <- c.downField("withPositions").as[Int]
      yield LineTables(s, p)
  }

  /** What the input declares: the boundary, the real file names, the synthetic line tables over the
    * boundary's classes, the Maven artifacts and the bootstrap handles.
    */
  private def compute(atom: Cpg, input: JvmBytecodeInput): BoundaryData =
    val named    = input.namedBoundary
    val fallback = input.fallbackBoundary
    val bomRoot  = bomRootComponent(atom)
    val boundary =
        if !named.isEmpty then named
        else if bomRoot.nonEmpty then AppBoundary(List(bomRoot), appAll = false, "bom-root")
        else fallback
    val tables = input.lineTablesFor(boundary)
    val classFiles = input.classes.toList.sortBy((n, _) => n).map((name, info) =>
        name -> info.sourceFile.getOrElse("")
    )
    val artifacts = input.artifacts.toList.sortBy((k, _) => k)
    val indySites = input.bootstraps.toList.sortBy((k, _) => k)
    BoundaryData(
      inputPath = input.path.toString,
      appPrefixes = boundary.prefixes,
      appAll = boundary.appAll,
      boundarySource = boundary.source,
      classFiles = classFiles,
      sourceFileGuesses = input.classes.values.count(_.sourceGuessed),
      syntheticLineTables = tables.synthetic,
      methodsWithLineTables = tables.withPositions,
      lineQuality = if tables.syntheticQuality then "synthetic" else "source",
      artifacts = artifacts,
      indySites = indySites
    )
  end compute

  /** The line tables recomputed under a replaced boundary. */
  private def lineTablesUnder(inputPath: String, boundary: AppBoundary): LineTables =
      JvmBytecodeInput(inputPath).lineTablesFor(boundary)

  /** The BOM's root component name, when the atom carries a BOM whose metadata names one. */
  private def bomRootComponent(atom: Cpg): String =
      atom.configFile.l
          .filter(f => BomFiles.isBom(f.name))
          .sortBy(_.name)
          .headOption
          .flatMap { bom =>
              io.circe.parser.parse(bom.content).toOption.flatMap { json =>
                  json.hcursor.downField("metadata").downField("component").downField("name")
                      .as[String].toOption
              }
          }
          .filter(name => name.contains('.'))
          .getOrElse("")

  private def readStored(atom: Cpg): Option[BoundaryData] =
      atom.configFile.name(BoundaryFileName).content.headOption.flatMap { content =>
          io.circe.parser.decode[BoundaryData](content).toOption
      }

  private def store(atom: Cpg, data: BoundaryData): Unit =
      Try {
          import io.circe.syntax.*
          val diff = new BatchedUpdate.DiffGraphBuilder
          // a previous run of this slice may have written one; keep exactly the current boundary
          atom.configFile.name(BoundaryFileName).foreach(diff.removeNode)
          diff.addNode(NewConfigFile().name(BoundaryFileName).content(data.asJson.spaces2))
          BatchedUpdate.applyDiff(atom.graph, diff)
      }.failed.foreach(e => log.warn(s"callsites: could not store the app boundary: $e"))
end JvmAppBoundary
