package io.appthreat.atom.parsedeps

import better.files.File as ScalaFile
import io.appthreat.atom.dataflows.OssDataFlow
import io.appthreat.dataflowengineoss.language.*
import io.appthreat.dataflowengineoss.queryengine.EngineContext
import io.appthreat.x2cpg.X2Cpg
import io.shiftleft.codepropertygraph.generated.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.{Call, CfgNode, Identifier, Literal}
import io.shiftleft.semanticcpg.language.*

import java.io.File as JFile

object PythonDependencyParser extends XDependencyParser:

  implicit val engineContext: EngineContext = EngineContext()
  private val SETUP_PY_FILE                 = ".*setup.py"
  private val SETUP_REQUIRES_PATTERN        = "(install_requires|extras_require|tests_require)"
  private val PYTHON_REQUIRES_PATTERN       = "python_requires"

  override def parse(cpg: Cpg): DependencySlice = DependencySlice(
    (parseSetupPy(cpg) ++ parseImports(cpg))
        .groupBy(_.name)
        .map { case (_, slices) => slices.reduce((a, b) => a.merge(b)) }
        .toSeq
        .sortBy(_.name),
    declaredPythonRuntime(cpg)
  )

  /** The Python runtime the project declares, when it declares one: `requires-python` in
    * pyproject.toml (or Poetry's `python` dependency), `python_requires` in setup.cfg or setup.py,
    * a `.python-version` file, Heroku's `runtime.txt`, and the base image of a root Dockerfile, in
    * that order; the first that declares wins. The value is reported as written: whether it is a
    * supported release is for the consumer to decide.
    */
  private def declaredPythonRuntime(cpg: Cpg): Option[String] =
    val root = ScalaFile(cpg.metaData.root.headOption.getOrElse(JFile.separator))
    val declared = Seq(
      textOf(root, "pyproject.toml").flatMap(toml =>
          firstMatch(toml, """(?m)^\s*requires-python\s*=\s*["']([^"']+)["']""")
              .orElse(poetryPython(toml))
      ),
      textOf(root, "setup.cfg").flatMap(
        firstMatch(_, """(?m)^\s*python_requires\s*=\s*["']?([^"'\s#]+)""")
      ),
      setupPyPythonRequires(cpg),
      textOf(root, ".python-version").flatMap(
        _.linesIterator.find(_.trim.nonEmpty)
      ).map(_.trim),
      textOf(root, "runtime.txt").flatMap(
        _.linesIterator.find(_.trim.nonEmpty)
      ).map(_.trim.stripPrefix("python-").trim),
      textOf(root, "Dockerfile").flatMap(
        firstMatch(_, """(?im)^\s*FROM\s+(?:--\S+\s+)*(?:\S+/)?python:(\S+)""")
      )
    )
    declared.flatten.headOption
  end declaredPythonRuntime

  private def textOf(root: ScalaFile, name: String): Option[String] =
    val f = root / name
    if f.exists && f.isRegularFile then scala.util.Try(f.contentAsString).toOption else None

  /** `python = "^3.10"` under `[tool.poetry.dependencies]`. */
  private def poetryPython(toml: String): Option[String] =
      firstMatch(toml, """(?s)\[tool\.poetry\.dependencies\](.*?)(?:\n\[|$)""")
          .flatMap(firstMatch(_, """(?m)^\s*python\s*=\s*["']([^"']+)["']"""))

  private def firstMatch(text: String, regex: String): Option[String] =
      regex.r.findFirstMatchIn(text).map(_.group(1))

  /** `python_requires='>=3.9'` in setup.py, read the way the dependency reads are: the setup call's
    * keyword argument, through the graph.
    */
  private def setupPyPythonRequires(cpg: Cpg): Option[String] =
      cpg.call
          .where(_.file.name(SETUP_PY_FILE))
          .name("setup")
          .argument
          .argumentName(PYTHON_REQUIRES_PATTERN)
          .collectAll[Literal]
          .headOption
          .map(l => X2Cpg.stripQuotes(l.code))

  private def parseSetupPy(cpg: Cpg): Set[ModuleWithVersion] =
    val dataFlowEnabled     = cpg.metaData.overlays.contains(OssDataFlow.overlayName)
    val requirementsPattern = """([\[\]/.\w_-]+)\s?((=>|<=|==|>=|=<|<|>|!=|~=).*)""".r

    def dataSourcesToRequires = (cpg.literal ++ cpg.identifier)
        .where(_.file.name(SETUP_PY_FILE))
        .where(_.argumentName(SETUP_REQUIRES_PATTERN))
        .collectAll[CfgNode] ++ cpg.assignment.where(_.file.name(SETUP_PY_FILE)).where(
      _.source.isCall.name("<operator>.listLiteral")
    ).target.isIdentifier.code(".*(libs|requirements)").collectAll[CfgNode]

    def installOrExtraRequires =
        cpg.call.where(_.file.name(SETUP_PY_FILE)).where(_.argumentName(
          SETUP_REQUIRES_PATTERN
        )).argument.collectAll[Literal]

    def setupCall = cpg.call("setup").where(_.file.name(SETUP_PY_FILE))

    def findOriginalDeclaration(xs: Traversal[CfgNode]): Iterable[Literal] =
        xs.flatMap {
            case l: Literal =>
                Iterable(l)
            case i: Identifier =>
                findOriginalDeclaration(
                  cpg.assignment.where(_.and(
                    _.file.name(SETUP_PY_FILE),
                    _.target.isIdentifier.nameExact(i.name)
                  )).source
                )
            case c: Call =>
                findOriginalDeclaration(c.argument)
            case _ => Iterable()
        }.collectAll[Literal]
            .to(Iterable)

    val initialTraversal = if dataFlowEnabled then setupCall.reachableBy(dataSourcesToRequires)
    else dataSourcesToRequires ++ installOrExtraRequires
    findOriginalDeclaration(initialTraversal)
        .map(x => X2Cpg.stripQuotes(x.code))
        .map {

            case requirementsPattern(name, versionSpecifiers, _)
                if versionSpecifiers.contains("==") =>
                val versions     = versionSpecifiers.split(',').toSeq
                val exactVersion = versions.find(_.startsWith("==")).get
                ModuleWithVersion(
                  name,
                  exactVersion.stripPrefix("=="),
                  versions.diff(Seq(exactVersion)).mkString(",")
                )
            case requirementsPattern(name, versionSpecifiers, _) =>
                ModuleWithVersion(name, versionSpecifiers = versionSpecifiers)
            case requirementsPattern(name, version) => ModuleWithVersion(name, version)
            case x                                  => ModuleWithVersion(x)
        }
        .toSet
  end parseSetupPy

  private def parseImports(cpg: Cpg): Set[ModuleWithVersion] =
    val root = ScalaFile(cpg.metaData.root.headOption.getOrElse(JFile.separator)).pathAsString
    // Get a set of local modules to exclude from imports
    // Identify the local modules names based on the presence of __init__.py files
    // Lastly, exclude import names that also match a local filename
    val localModuleNames = cpg.file.name
        .filterNot(_ == "N/A")
        .map(x => ScalaFile(x))
        .map(_.pathAsString)
        .map(_.stripPrefix(s"$root${JFile.separatorChar}").split(
          JFile.separatorChar
        ).head.replaceFirst("\\.py", ""))
        .toSet
        ++ cpg.file.name(".*__init__.py").name.map(_.stripPrefix(
          s"$root${JFile.separatorChar}"
        ).stripSuffix(s"${JFile.separatorChar}__init__.py").split(
          JFile.separatorChar
        ).last).toSet
        ++ cpg.file.nameNot(".*__init__.py").name
            .filterNot(_ == "N/A")
            .map(x => ScalaFile(x))
            .map(_.pathAsString)
            .map(_.stripPrefix(s"$root${JFile.separatorChar}").split(
              JFile.separatorChar
            ).last.replaceFirst("\\.py", ""))
            .toSet
    cpg.imports
        .whereNot(_.call.file.name(SETUP_PY_FILE))
        .filterNot {
            _.importedEntity match
              case Some(x) if x.startsWith(".") => true
              case Some(x) if x.contains('.')   => localModuleNames.contains(x.split('.').head)
              case Some(x)                      => localModuleNames.contains(x)
              case _                            => true
        }
        .dedup
        .importedEntity
        .map(x => ModuleWithVersion(name = x.split('.').head, importedSymbols = x))
        .toSet
  end parseImports
end PythonDependencyParser
