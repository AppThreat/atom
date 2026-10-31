package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.ProgramCallSites
import io.shiftleft.codepropertygraph.Cpg

/** The Python source of the callsites slice. Placeholder while the core split lands: the adapter
  * arrives with the Python port (atom#323).
  */
final class PythonCallSiteSource(atom: Cpg) extends BaseCallSiteSource(atom, "py"):

  def calculate(): ProgramCallSites = ProgramCallSites(Nil)
end PythonCallSiteSource
