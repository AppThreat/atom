package io.appthreat.atom.frontends.clike

import io.joern.c2cpg.Config
import io.joern.x2cpg.X2Cpg.withNewEmptyCpg
import io.joern.x2cpg.X2CpgFrontend
import io.joern.x2cpg.passes.frontend.MetaDataPass
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.Languages

import scala.util.Try

class H2Atom extends X2CpgFrontend[Config] {

  def createCpg(config: Config): Try[Cpg] = {
    withNewEmptyCpg(config.outputPath, config) { (cpg, config) =>
      new MetaDataPass(cpg, Languages.NEWC, config.inputPath).createAndApply()
      new AstLiteCreationPass(cpg, config).createAndApply()
    }
  }

}
