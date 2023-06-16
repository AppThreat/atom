package io.appthreat.atom

import io.circe.{Encoder, Decoder}
import io.circe.syntax.EncoderOps
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}
import io.shiftleft.semanticcpg.language.{toNodeTypeStarters, toMetaDataTraversalExtGen, toTraversalSugarExt}

package object parsedeps {

  trait XDependencyParser {
    def parse(cpg: Cpg): DependencySlice
  }

  trait AtomSlice {
    def toJson: String
  }

  implicit val dependencySliceEncoder: Encoder[DependencySlice] = Encoder.forProduct1("modules")(x => x.modules)
  implicit val dependencySliceDecoder: Decoder[DependencySlice] = Decoder.forProduct1("modules")(DependencySlice.apply)

  case class DependencySlice(modules: Seq[String]) extends AtomSlice {
    override def toJson: String = this.asJson.spaces2
  }

  def parseDependencies(cpg: Cpg): Either[String, DependencySlice] = {
    cpg.metaData.language.map(_.toUpperCase).headOption match
      case Some(language) if Set(Languages.PYTHONSRC, Languages.PYTHON, "PY").contains(language) =>
        Right(PythonDependencyParser.parse(cpg))
      case Some(language) => Left(s"'$language' is not yet supported for the `depscan` command")
      case _              => Left("Unable to extract CPG language")
  }

}
