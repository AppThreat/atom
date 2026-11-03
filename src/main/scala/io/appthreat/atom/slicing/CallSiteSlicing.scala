package io.appthreat.atom.slicing

import io.appthreat.atom.slicing.callsites.CallSiteSource
import io.shiftleft.codepropertygraph.Cpg

/** The entry point of the callsites slice. The per-language sources live in
  * [[io.appthreat.atom.slicing.callsites]]; the language is read off the atom's metadata and a
  * language without a source gets an empty slice.
  */
object CallSiteSlicing:

  def calculateCallSitesSlice(
    atom: Cpg,
    appPrefixes: Option[Seq[String]] = None,
    externalPaths: Option[String] = None
  ): ProgramCallSites =
    val source = CallSiteSource.forAtom(atom, appPrefixes, externalPaths)
    source.calculate().copy(metadata = source.metadata)
