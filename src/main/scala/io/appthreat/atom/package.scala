package io.appthreat

import better.files.File
import io.appthreat.atom.Atom.*
import io.circe.{Encoder, Json}
import io.appthreat.atom.slicing.*

package object atom {

  trait AtomConfig extends BaseConfig {
    this.inputPath = File(".")
    this.outputSliceFile = File(DEFAULT_SLICE_OUT_FILE)
    var outputAtomFile: File = File(DEFAULT_ATOM_OUT_FILE)
    var language: String     = ""
    var dataDeps: Boolean    = false
    var maxNumDef: Int       = DEFAULT_MAX_DEFS

    def withOutputAtomFile(x: File): AtomConfig = {
      this.outputAtomFile = x
      this
    }

    def withLanguage(x: String): AtomConfig = {
      this.language = x
      this
    }

    def withDataDependencies(x: Boolean): AtomConfig = {
      this.dataDeps = x
      this
    }

    def withMaxNumDef(x: Int): AtomConfig = {
      this.maxNumDef = x
      this
    }
  }

  case class DefaultAtomConfig() extends AtomConfig

  case class AtomParseDepsConfig() extends AtomConfig

  case class AtomDataFlowConfig(
    sinkPatternFilter: Option[String] = None,
    excludeOperatorCalls: Boolean = true,
    mustEndAtExternalMethod: Boolean = true,
    sliceDepth: Int = DEFAULT_SLICE_DEPTH
  ) extends AtomConfig

  case class AtomUsagesConfig(
    minNumCalls: Int = 1,
    excludeOperatorCalls: Boolean = true,
    includeMethodSource: Boolean = false
  ) extends AtomConfig

  import io.appthreat.atom.slicing._
  import io.circe.generic.auto._
  import io.circe.syntax.EncoderOps

  implicit val encodeDataFlowSlice: Encoder[AtomDataFlowSlice] = Encoder.instance {
    case AtomDataFlowSlice(dataFlowSlice, paths) =>
      Json.obj("graph" -> dataFlowSlice.asJson, "paths" -> paths.asJson)
  }

  case class AtomDataFlowSlice(graph: DataFlowSlice, paths: Set[List[Long]] = Set.empty) {

    def toJson: String = this.asJson.toString()

  }

}
