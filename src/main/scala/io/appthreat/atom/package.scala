package io.appthreat

import better.files.File
import io.appthreat.atom.Atom.*
import io.joern.dataflowengineoss.slicing.*

package object atom {

  trait AtomConfig extends BaseConfig {

    this.outputSliceFile = File("slices.json")
    var outputAtomFile: File = File(DEFAULT_ATOM_OUT_FILE)
    var language: String     = ""
    var dataDeps: Boolean    = false
    var maxNumDef: Int       = 2000

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
    mustEndAtExternalMethod: Boolean = false,
    sliceDepth: Int = 3
  ) extends AtomConfig

  case class AtomUsagesConfig(
    minNumCalls: Int = 1,
    excludeOperatorCalls: Boolean = false,
    excludeMethodSource: Boolean = false
  ) extends AtomConfig

}
