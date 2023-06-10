package io.appthreat.atom

import io.circe.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*
import io.joern.dataflowengineoss.layers.dataflows.{OssDataFlow, OssDataFlowOptions}
import io.shiftleft.codepropertygraph.generated.{Cpg, Languages}
import io.shiftleft.semanticcpg.language.*
import io.shiftleft.semanticcpg.layers.LayerCreatorContext

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
    if (!cpg.metaData.overlays.toSet.contains(OssDataFlow.overlayName)) {
      println("Data-flow overlay is not detected, applying now")
      new OssDataFlow(new OssDataFlowOptions()).run(new LayerCreatorContext(cpg))
    }
    cpg.metaData.language.map(_.toUpperCase).headOption match
      case Some(language) if Set(Languages.PYTHONSRC, Languages.PYTHON, "PY").contains(language) =>
        Right(PythonDependencyParser.parse(cpg))
      case Some(language) => Left(s"'$language' is not yet supported for the `depscan` command")
      case _              => Left("Unable to extract CPG language")
  }

}
