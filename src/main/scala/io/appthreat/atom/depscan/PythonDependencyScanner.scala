package io.appthreat.atom.depscan

import better.files.File as BFile
import io.joern.dataflowengineoss.language.*
import io.joern.dataflowengineoss.queryengine.{Engine, EngineContext}
import io.joern.x2cpg.X2Cpg
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*
import overflowdb.traversal.*

import java.io.File as JFile
import scala.annotation.tailrec
object PythonDependencyScanner extends XDependencyScanner {

  implicit val engineContext: EngineContext = EngineContext()

  def scan(cpg: Cpg): DependencySlice = DependencySlice(scanSetupPy(cpg) ++ scanImports(cpg))

  private def scanSetupPy(cpg: Cpg): Set[String] = {
    val requirementsPattern = "([\\w_]+)(=>|<=|==|>=|=<).*".r

    def dataSourcesToRequires = (cpg.literal ++ cpg.identifier)
      .where(_.file.name(".*setup.py"))
      .where(_.argumentName("install_requires"))
      .collectAll[CfgNode]
    def setupCall = cpg.call("setup").where(_.file.name(".*setup.py"))

    def findOriginalDeclaration(xs: Traversal[CfgNode]): Iterable[Literal] =
      xs.flatMap {
        case l: Literal =>
          Iterable(l)
        case i: Identifier =>
          findOriginalDeclaration(
            cpg.assignment.where(_.and(_.file.name(".*setup.py"), _.target.isIdentifier.nameExact(i.name))).source
          )
        case c: Call =>
          findOriginalDeclaration(c.argument)
        case _ => Iterable()
      }.collectAll[Literal]
        .toIterable

    findOriginalDeclaration(setupCall.reachableBy(dataSourcesToRequires))
      .map(x => X2Cpg.stripQuotes(x.code))
      .map {
        case requirementsPattern(x, _) => x
        case x                         => x
      }
      .sorted
      .toSet
  }

  private def scanImports(cpg: Cpg): Set[String] = {
    val root = BFile(cpg.metaData.root.headOption.getOrElse(JFile.separator))
    // Get a set of local modules to exclude from imports
    val localModuleNames =
      cpg.file.name
        .filterNot(_ == "N/A")
        .map(x => BFile(x))
        .flatMap(_.parentOption.map(_.name))
        .filterNot(_ == root.name)
        .toSet
    cpg.imports
      .whereNot(_.call.file.name(".*setup.py"))
      .filterNot {
        _.importedEntity match
          case Some(x) if x.startsWith(".") => true
          case Some(x) if x.contains('.')   => localModuleNames.contains(x.split('.').head)
          case Some(x)                      => localModuleNames.contains(x)
          case _                            => true
      }
      .dedup
      .importedEntity
      .flatMap(_.split('.').headOption)
      .toSet
  }

}
