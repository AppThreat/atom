package io.appthreat.atom

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}
import io.shiftleft.semanticcpg.language.{toMetaDataTraversalExtGen, toNodeTypeStarters, toTraversalSugarExt}

package object parsedeps {

  trait XDependencyParser {
    def parse(cpg: Cpg): DependencySlice
  }

  trait AtomSlice {
    def toJson: String
  }

  implicit val dependencySliceEncoder: Decoder[DependencySlice] =
    (c: HCursor) =>
      for {
        modules <- c.downField("modules").as[List[ModuleWithVersion]]
      } yield {
        DependencySlice(modules)
      }
  implicit val dependencySliceDecoder: Encoder[DependencySlice] =
    Encoder.instance { case DependencySlice(modules) =>
      Json.obj("modules" -> modules.asJson)
    }

  case class DependencySlice(modules: Seq[ModuleWithVersion]) extends AtomSlice {
    override def toJson: String = this.asJson.spaces2
  }

  implicit val moduleWithVersionEncoder: Encoder[ModuleWithVersion] =
    Encoder.forProduct3("name", "version", "versionSpecifiers")(x => (x.name, x.version, x.versionSpecifiers))
  implicit val moduleWithVersionDecoder: Decoder[ModuleWithVersion] =
    Decoder.forProduct3("name", "version", "versionSpecifiers")(ModuleWithVersion.apply)

  case class ModuleWithVersion(name: String, version: String = "", versionSpecifiers: String = "") {

    def versions: Set[String] =
      (if (!version.isBlank) Set(s"==$version") else Set.empty) ++ versionSpecifiers.split(',').filterNot(_.isBlank)

    def merge(x: ModuleWithVersion): ModuleWithVersion = {
      val vs = this.versions ++ x.versions
      vs.find(_.startsWith("==")) match
        case Some(exactVersion) =>
          ModuleWithVersion(name, exactVersion.stripPrefix("=="), (vs diff Set(exactVersion)).mkString(","))
        case None => ModuleWithVersion(name, versionSpecifiers = vs.mkString(","))

    }

  }

  def parseDependencies(cpg: Cpg): Either[String, DependencySlice] = {
    cpg.metaData.language.map(_.toUpperCase).headOption match
      case Some(language) if Set(Languages.PYTHONSRC, Languages.PYTHON, "PY").contains(language) =>
        Right(PythonDependencyParser.parse(cpg))
      case Some(language) => Left(s"'$language' is not yet supported for the `parsedeps` command")
      case _              => Left("Unable to extract atom language")
  }

}
