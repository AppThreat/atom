package io.appthreat.atom

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.concurrent.TimeUnit
import scala.util.Try

/** The versions of atom and of the tools it runs: chen, which atom is built on, atom-parsetools,
  * whose astgen, rbastgen, phpastgen and scalasem commands atom finds on the PATH, and parsa, the
  * native type map writer that atom-parsetools 2.x installs as an npm platform package and astgen
  * runs itself. `atom --version` prints them and `--perf-report` records them.
  *
  * atom-parsetools is found the way chen runs it: the `astgen` command on the PATH, followed
  * through its symlink (npm on Linux and macOS) or its `.cmd` shim (npm on Windows) to the package
  * directory, whose package.json names the version. parsa is found in the order astgen looks for
  * it: `ASTGEN_NATIVE_BIN`, beside astgen.js, the platform package nested in or beside
  * atom-parsetools, then the PATH. atom-parsetools 1.x has no parsa.
  */
object ToolVersions:

  final case class Found(version: String, path: Option[Path])

  final case class Versions(
    atom: String,
    chen: String,
    atomParsetools: Option[Found],
    parsa: Option[Found]
  ):
    def lines: Seq[String] =
        Seq(
          s"atom $atom",
          s"chen $chen",
          atomParsetools.fold("atom-parsetools not found (astgen is not on the PATH)")(f =>
              s"atom-parsetools ${f.version}${f.path.fold("")(p => s" ($p)")}"
          ),
          parsa.fold("parsa not found (astgen writes TypeScript 6 type maps)")(f =>
              s"parsa ${f.version}${f.path.fold("")(p => s" ($p)")}"
          )
        )

    /** One NDJSON line for the perf report, in the shape of its `__meta__` record. */
    def perfRecord: String =
      def str(s: String)        = "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
      def opt(f: Option[Found]) = f.fold("null")(x => str(x.version))
      s"""{"stage":"__versions__","phase":"meta","atom":${str(atom)},"chen":${str(chen)},""" +
          s""""atomParsetools":${opt(atomParsetools)},"parsa":${opt(parsa)}}"""
  end Versions

  def current(
    env: Map[String, String] = sys.env,
    os: String = System.getProperty("os.name", ""),
    arch: String = System.getProperty("os.arch", "")
  ): Versions =
    val pkgDir = findAstgen(env, isWindows(os)).flatMap(parsetoolsDir)
    Versions(
      versionOf(Atom.getClass),
      versionOf(classOf[io.appthreat.x2cpg.passes.taggers.ChennaiTagsPass]),
      pkgDir.flatMap(dir =>
          packageVersion(dir.resolve("package.json")).map(v => Found(v, Some(dir)))
      ),
      findParsa(env, pkgDir, os, arch).map(bin =>
          Found(parsaVersion(bin).getOrElse("unknown"), Some(bin))
      )
    )

  /** Appends the versions to the perf report when one is being written and already exists, after
    * PerfReporter has written its `__meta__` header line.
    */
  def appendToPerfReport(versions: => Versions): Unit =
      Option(System.getProperty("chen.perf.report"))
          .orElse(sys.env.get("CHEN_PERF_REPORT"))
          .filter(_.nonEmpty)
          .map(Paths.get(_))
          .filter(Files.isRegularFile(_))
          .foreach { p =>
              Try(
                Files.writeString(
                  p,
                  versions.perfRecord + "\n",
                  StandardCharsets.UTF_8,
                  StandardOpenOption.APPEND
                )
              )
          }

  private def isWindows(os: String): Boolean = os.toLowerCase.startsWith("windows")

  private def versionOf(clazz: Class[?]): String =
      Option(clazz.getPackage)
          .flatMap(p => Option(p.getImplementationVersion))
          .getOrElse("unknown")

  private def pathDirs(env: Map[String, String]): Seq[Path] =
      env.collectFirst { case (k, v) if k.equalsIgnoreCase("PATH") => v }
          .toSeq
          .flatMap(_.split(java.io.File.pathSeparator))
          .filter(_.nonEmpty)
          .flatMap(d => Try(Paths.get(d)).toOption)

  private def onPath(env: Map[String, String], names: Seq[String]): Option[Path] =
      pathDirs(env).iterator
          .flatMap(d => names.iterator.map(d.resolve))
          .find(Files.isRegularFile(_))

  private[atom] def findAstgen(env: Map[String, String], windows: Boolean): Option[Path] =
      onPath(env, if windows then Seq("astgen.cmd", "astgen.exe", "astgen") else Seq("astgen"))

  /** The atom-parsetools package directory for the `astgen` command found on the PATH. */
  private[atom] def parsetoolsDir(astgen: Path): Option[Path] =
    val real   = Try(astgen.toRealPath()).getOrElse(astgen)
    val beside = Option(real.getParent).filter(d => isParsetools(d.resolve("package.json")))
    // npm on Windows writes a shim beside node_modules instead of a symlink
    def shimmed =
        Option(astgen.getParent)
            .map(_.resolve("node_modules").resolve("@appthreat").resolve("atom-parsetools"))
            .filter(d => isParsetools(d.resolve("package.json")))
    beside.orElse(shimmed)

  private def isParsetools(pkgJson: Path): Boolean =
      Files.isRegularFile(pkgJson) &&
          Try(Files.readString(pkgJson)).toOption.exists(
            _.contains("\"@appthreat/atom-parsetools\"")
          )

  private def packageVersion(pkgJson: Path): Option[String] =
      Try(Files.readString(pkgJson)).toOption
          .flatMap(s => io.circe.parser.parse(s).toOption)
          .flatMap(_.hcursor.downField("version").as[String].toOption)

  /** The npm platform package astgen loads parsa from, as astgen names it. */
  private[atom] def parsaPackage(os: String, arch: String): Option[String] =
    val o = os.toLowerCase match
      case s if s.startsWith("windows") => Some("win32")
      case s if s.startsWith("mac")     => Some("darwin")
      case s if s.startsWith("linux")   => Some("linux")
      case _                            => None
    val a = arch.toLowerCase match
      case "amd64" | "x86_64"  => Some("x64")
      case "aarch64" | "arm64" => Some("arm64")
      case _                   => None
    for oo <- o; aa <- a yield s"parsa-$oo-$aa"

  private[atom] def findParsa(
    env: Map[String, String],
    pkgDir: Option[Path],
    os: String,
    arch: String
  ): Option[Path] =
    val bin = if isWindows(os) then "parsa.exe" else "parsa"
    val fromEnv =
        env.get("ASTGEN_NATIVE_BIN").filter(_.nonEmpty).map(Paths.get(_)).filter(
          Files.isRegularFile(_)
        )
    def packaged =
        for
          dir  <- pkgDir
          name <- parsaPackage(os, arch)
          p <- Seq(
            dir.resolve(bin),
            dir.resolve("node_modules").resolve("@appthreat").resolve(name).resolve("bin").resolve(
              bin
            ),
            dir.resolveSibling(name).resolve("bin").resolve(bin)
          ).find(Files.isRegularFile(_))
        yield p
    fromEnv.orElse(packaged).orElse(onPath(env, Seq(bin)))
  end findParsa

  /** `parsa --version` prints "<version> (TypeScript <commit>)". */
  private def parsaVersion(bin: Path): Option[String] =
      Try {
          val proc = new ProcessBuilder(bin.toString, "--version").redirectErrorStream(true).start()
          val out  = new String(proc.getInputStream.readAllBytes(), StandardCharsets.UTF_8).trim
          if proc.waitFor(10, TimeUnit.SECONDS) && proc.exitValue() == 0 && out.nonEmpty then
            Some(out.linesIterator.next())
          else
            proc.destroyForcibly()
            None
      }.toOption.flatten
end ToolVersions
