package io.appthreat

import better.files.File
import io.appthreat.atom.Atom.*
import io.appthreat.atom.slicing.*
import io.circe.{Encoder, Json}

package object atom:

    trait AtomConfig extends BaseConfig:
        this.inputPath = File(".")
        this.outputSliceFile = File(DEFAULT_SLICE_OUT_FILE)
        var outputAtomFile: File = File(DEFAULT_ATOM_OUT_FILE)
        var language: String     = ""
        var dataDeps: Boolean    = false
        var removeAtom: Boolean  = false
        var maxNumDef: Int       = DEFAULT_MAX_DEFS
        var exportAtom: Boolean  = false
        var exportDir: String    = DEFAULT_EXPORT_DIR
        var exportFormat: String = DEFAULT_EXPORT_FORMAT

        def withOutputAtomFile(x: File): AtomConfig =
            this.outputAtomFile = x
            this

        def withLanguage(x: String): AtomConfig =
            this.language = x
            this

        def withDataDependencies(x: Boolean): AtomConfig =
            this.dataDeps = x
            this

        def withRemoveAtom(x: Boolean): AtomConfig =
            this.removeAtom = x
            this

        def withExportAtom(x: Boolean): AtomConfig =
            this.exportAtom = x
            this

        def withExportDir(x: String): AtomConfig =
            this.exportDir = x
            this

        def withExportFormat(x: String): AtomConfig =
            this.exportFormat = x
            this

        def withMaxNumDef(x: Int): AtomConfig =
            this.maxNumDef = x
            this
    end AtomConfig

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

    case class AtomReachablesConfig(
      sourceTag: String = FRAMEWORK_INPUT_TAG,
      sinkTag: String = FRAMEWORK_OUTPUT_TAG,
      sliceDepth: Int = DEFAULT_SLICE_DEPTH,
      includeCryptoFlows: Boolean = false
    ) extends AtomConfig

    import io.appthreat.atom.slicing.*
    import io.circe.generic.auto.*
    import io.circe.syntax.EncoderOps

    implicit val encodeDataFlowSlice: Encoder[AtomDataFlowSlice] = Encoder.instance {
        case AtomDataFlowSlice(dataFlowSlice, paths) =>
            Json.obj("graph" -> dataFlowSlice.asJson, "paths" -> paths.asJson)
    }

    case class AtomDataFlowSlice(graph: DataFlowSlice, paths: Set[List[Long]] = Set.empty):

        def toJson: String = this.asJson.noSpaces
end atom
