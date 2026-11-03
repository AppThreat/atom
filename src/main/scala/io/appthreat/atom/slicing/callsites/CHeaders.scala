package io.appthreat.atom.slicing.callsites

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.NewConfigFile
import io.shiftleft.semanticcpg.language.*
import overflowdb.BatchedUpdate

import java.nio.file.Paths
import scala.util.Try

/** Where a C or C++ project's own files live, beyond the input root: the build directories of its
  * compilation database, which hold the headers the build generates (CMake's `curl_config.h`), and
  * the `--external-paths` pattern that marks vendored code (atom#346). Stored in the atom when it
  * is built, so a later `callsites` run on the same atom sees what the build saw.
  */
final case class CProjectLayout(buildDirs: List[String], externalPaths: String)

object CProjectLayout:

  val FileName = "callsites-c-layout.json"

  private val log = org.slf4j.LoggerFactory.getLogger(getClass)

  private given io.circe.Encoder[CProjectLayout] =
      io.circe.generic.semiauto.deriveEncoder[CProjectLayout]

  private given io.circe.Decoder[CProjectLayout] =
      io.circe.generic.semiauto.deriveDecoder[CProjectLayout]

  /** The build directories of a compilation database: each entry's `directory`, outside the input
    * root (the root is the project already).
    */
  def buildDirsOf(compileCommands: String, root: String): List[String] =
      Try(better.files.File(compileCommands).contentAsString).toOption
          .flatMap(io.circe.parser.parse(_).toOption)
          .flatMap(_.asArray)
          .map(_.flatMap(_.hcursor.downField("directory").as[String].toOption).toList)
          .getOrElse(Nil)
          .map(d => Try(Paths.get(d).toRealPath().toString).getOrElse(d))
          .distinct
          .filterNot(d => CHeaders.under(d, Seq(root)))
          .sorted

  def read(atom: Cpg): Option[CProjectLayout] =
      atom.configFile.name(FileName).content.headOption.flatMap(c =>
          io.circe.parser.decode[CProjectLayout](c).toOption
      )

  def store(atom: Cpg, layout: CProjectLayout): Unit =
      Try {
          import io.circe.syntax.*
          val diff = new BatchedUpdate.DiffGraphBuilder
          atom.configFile.name(FileName).foreach(diff.removeNode)
          diff.addNode(NewConfigFile().name(FileName).content(layout.asJson.spaces2))
          BatchedUpdate.applyDiff(atom.graph, diff)
      }.failed.foreach(e => log.warn(s"callsites: could not store the C project layout: $e"))
end CProjectLayout

/** What a C or C++ header is to the project that includes it, decided from where it resolved, never
  * from its name: the project's own (under the input root or a build directory), the platform's
  * (`builtin`: an SDK or sysroot, the compiler's own headers, the C++ standard library, the C
  * library under `/usr/include`), or a library's. `module` is the header as its include root spells
  * it (`openssl/ssl.h`, `sys/types.h`), as the oracle writes it.
  */
object CHeaders:

  /** The C library's headers relative to `/usr/include` on Linux, generated from glibc's and musl's
    * packages (the file says how).
    */
  private val libcHeaders: Set[String] =
      Try {
          val stream = getClass.getResourceAsStream("/callsites/c-libc-headers.txt")
          try scala.io.Source.fromInputStream(stream, "UTF-8").getLines().toList
          finally stream.close()
      }.getOrElse(Nil).map(_.trim).filter(l => l.nonEmpty && !l.startsWith("#")).toSet

  /** `x86_64-linux-gnu/`, `aarch64-linux-musl/`: the multiarch directory Debian puts some of the C
    * library's headers in.
    */
  private val Multiarch = "^[a-z0-9_]+-linux-[a-z0-9]+/".r

  /** `path` (either separator) is `root` or under it. */
  def under(path: String, roots: Seq[String]): Boolean =
    val p = slashes(path)
    roots.map(slashes).exists(r => r.nonEmpty && (p == r || p.startsWith(r.stripSuffix("/") + "/")))

  def slashes(path: String): String = path.replace('\\', '/')

  /** The module and builtin flag of a header outside the project, from its absolute path. */
  def external(header: String): (String, Boolean) =
    val p = slashes(header)
    def after(marker: String): Option[String] =
        p.indexOf(marker) match
          case -1 => None
          case at => Some(p.substring(at + marker.length)).filter(_.nonEmpty)
    def baseName: String = p.substring(p.lastIndexOf('/') + 1)
    val sdk              = p.contains("/SDKs/") || p.contains("/sysroot/")
    if sdk then
      // everything a platform SDK or a cross sysroot carries is the platform's
      val module = after(".framework/")
          .orElse(after("/usr/include/c++/v1/"))
          .orElse(after("/usr/include/"))
          .getOrElse(baseName)
      module -> true
    else if p.contains("/lib/clang/") || p.contains("/lib/gcc/") then
      // the compiler's own headers (stddef.h, limits.h, arm_neon.h)
      after("/include/").map(_.split("/include/").last).getOrElse(baseName) -> true
    else if p.contains("/include/c++/") then
      // libc++ (`c++/v1/`) or libstdc++ (`c++/<version>/`)
      after("/include/c++/").map(_.split('/').drop(1).mkString("/")).getOrElse(baseName) -> true
    else if p.startsWith("/usr/include/") then
      val relative = Multiarch.replaceFirstIn(p.stripPrefix("/usr/include/"), "")
      relative -> libcHeaders.contains(relative)
    else after("/include/").getOrElse(baseName) -> false
  end external
end CHeaders
