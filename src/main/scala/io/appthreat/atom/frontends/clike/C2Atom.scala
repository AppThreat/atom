package io.appthreat.atom.frontends.clike

import io.appthreat.c2cpg.Config
import io.appthreat.c2cpg.passes.AstCreationPass
import io.appthreat.x2cpg.X2Cpg.withNewEmptyCpg
import io.appthreat.x2cpg.X2CpgFrontend
import io.appthreat.x2cpg.passes.frontend.MetaDataPass
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages

import scala.util.Try

class C2Atom extends X2CpgFrontend[Config]:

  def createCpg(config: Config): Try[Cpg] =
      withNewEmptyCpg(config.outputPath, config) { (atom, config) =>
        new MetaDataPass(atom, Languages.NEWC, config.inputPath).createAndApply()
        new AstCreationPass(atom, config).createAndApply()
      }
