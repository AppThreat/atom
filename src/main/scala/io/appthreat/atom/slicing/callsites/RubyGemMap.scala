package io.appthreat.atom.slicing.callsites

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.semanticcpg.language.*

import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try

/** A gem of `Gemfile.lock`. */
private[callsites] final case class RubyGem(name: String, version: String)

/** Which gem defines a constant, when that is certain.
  *
  * `Gemfile.lock` (a config file of the atom) names the gems and their versions. The constants a
  * gem defines come from its installed sources, read from disk only when they are there: Bundler's
  * path (`BUNDLE_PATH` of `.bundle/config`, or of the environment, `vendor/bundle` by default) and
  * `GEM_HOME`. A gem's sources are never parsed: a Ruby file under `lib/` or `app/<kind>/` defines
  * the constant its path spells (`lib/faraday/utils.rb`, `Faraday::Utils`) and every directory
  * above it a namespace, compared by name with case and underscores left out so that `json` is
  * `JSON`. A constant maps to a gem by the longest prefix of its path (to four names) that one gem,
  * and only one, defines. Without installed sources, or when two gems define the prefix, nothing
  * maps: a gem is never chosen by the likeness of its name to the constant's.
  */
final class RubyGemMap(atom: Cpg):

  private def normalised(name: String): String = name.replace('\\', '/')

  private lazy val lockFile =
      atom.configFile.l.find(f => normalised(f.name).split('/').last == "Gemfile.lock")

  /** The gems the lock file lists, in the order it lists them. */
  lazy val gems: List[RubyGem] =
      lockFile.toList.flatMap(f => RubyGemMap.parseLock(f.content))

  private lazy val root: Option[Path] =
      atom.metaData.root.headOption.filter(_.nonEmpty).flatMap(r => Try(Paths.get(r)).toOption)

  /** The directories that hold unpacked gems: `<bundle path>/ruby/<abi>/gems` and `GEM_HOME`'s. */
  private lazy val gemDirectories: List[Path] =
    val bundlePaths = mutable.ListBuffer.empty[Path]
    root.foreach { r =>
      val config = r.resolve(".bundle").resolve("config")
      if Files.isRegularFile(config) then
        Try(Files.readAllLines(config).asScala.toList).getOrElse(Nil).foreach {
            case RubyGemMap.BundlePath(p) => bundlePaths += r.resolve(p)
            case _                        =>
        }
      bundlePaths += r.resolve("vendor").resolve("bundle")
    }
    sys.env.get("BUNDLE_PATH").filter(_.nonEmpty).foreach(p => bundlePaths += Paths.get(p))
    val homes = sys.env.get("GEM_HOME").filter(_.nonEmpty).map(Paths.get(_)).toList
    val fromBundle = bundlePaths.toList.flatMap { p =>
      val abiDirectories =
          Try(Files.list(p.resolve("ruby")).iterator().asScala.toList).getOrElse(Nil)
      abiDirectories.map(_.resolve("gems")) :+ p.resolve("gems")
    }
    (fromBundle ++ homes.map(_.resolve("gems")) ++ homes).filter(Files.isDirectory(_)).distinct

  /** The unpacked directory of a gem: `<name>-<version>`, possibly with a platform after it. */
  private def installedDirectory(gem: RubyGem): Option[Path] =
    val prefix = s"${gem.name}-${gem.version}"
    gemDirectories.iterator.flatMap { dir =>
        Try(Files.list(dir).iterator().asScala.toList).getOrElse(Nil).filter { p =>
          val n = p.getFileName.toString
          Files.isDirectory(p) && (n == prefix || n.startsWith(s"$prefix-"))
        }.sortBy(_.getFileName.toString)
    }.nextOption()

  /** The name a constant segment or a path segment is compared by. */
  private def squash(segment: String): String = segment.filter(c => c != '_').toLowerCase

  /** squashed namespace path -> the gems that define it. */
  private lazy val defined: Map[String, Set[RubyGem]] =
    val index = mutable.Map.empty[String, mutable.Set[RubyGem]]
    gems.foreach { gem =>
        installedDirectory(gem).foreach { dir =>
          val roots = List(dir.resolve("lib")) ++
              Try(Files.list(dir.resolve("app")).iterator().asScala.toList).getOrElse(Nil)
          roots.filter(Files.isDirectory(_)).foreach { base =>
              Try(Files.walk(base)).toOption.foreach { stream =>
                  try
                      stream.iterator().asScala.foreach { file =>
                        val relative = normalised(base.relativize(file).toString)
                        if relative.nonEmpty && (relative.endsWith(".rb") || Files.isDirectory(
                            file
                          ))
                        then
                          val parts = relative.stripSuffix(".rb").split('/').toList
                          parts.indices.foreach { i =>
                            val key = parts.take(i + 1).map(squash).mkString("::")
                            index.getOrElseUpdate(key, mutable.Set.empty) += gem
                          }
                      }
                  finally stream.close()
              }
          }
        }
    }
    index.view.mapValues(_.toSet).toMap
  end defined

  /** Whether some gem with installed sources defines exactly this constant (or namespace). */
  def definesExactly(path: List[String]): Boolean =
      defined.contains(path.map(squash).mkString("::"))

  /** Whether some gem with installed sources defines the constant path or a prefix of it. */
  def defines(path: List[String]): Boolean =
      (math.min(path.size, RubyBuiltins.Depth) to 1 by -1).exists(k =>
          defined.contains(path.take(k).map(squash).mkString("::"))
      )

  /** `pkg:gem/<name>@<version>` of the one gem that defines the longest prefix of the path any gem
    * defines, else empty.
    */
  def purlOf(path: List[String]): String =
    val found = (math.min(path.size, RubyBuiltins.Depth) to 1 by -1).iterator
        .map(k => defined.get(path.take(k).map(squash).mkString("::")))
        .collectFirst { case Some(owners) => owners }
    found.filter(_.size == 1).map(_.head).map(g => s"pkg:gem/${g.name}@${g.version}").getOrElse("")
end RubyGemMap

object RubyGemMap:

  /** `BUNDLE_PATH: "vendor/bundle"` in `.bundle/config`. */
  private val BundlePath = """^\s*BUNDLE_PATH:\s*"?([^"\s]+)"?\s*$""".r

  /** The `    name (version)` lines of a lock file's `GEM`, `PATH` and `GIT` sections. */
  private val Spec = """^    (\S+) \(([^)]+)\)\s*$""".r

  /** The gems of a `Gemfile.lock`: a platform after a hyphen is not part of the version. */
  def parseLock(content: String): List[RubyGem] =
    var inSpecs = false
    content.linesIterator.flatMap { line =>
        if line.nonEmpty && line.head.isUpper then
          inSpecs = false
          None
        else if line.trim == "specs:" then
          inSpecs = true
          None
        else if inSpecs then
          line match
            case Spec(name, version) => Some(RubyGem(name, version.split('-').head))
            case _                   => None
        else None
    }.toList.distinct
end RubyGemMap
