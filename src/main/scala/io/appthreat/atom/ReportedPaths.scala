package io.appthreat.atom

import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.util.Try

/** Decides where a file named in a report sits relative to the analysed input, and how its path is
  * printed.
  *
  * A C/C++ frontend names a FILE relative to the input when the file is inside it, and by its
  * absolute path otherwise (a system header, a header from another checkout). A report line must
  * carry one form or the other, never the input's name glued onto an absolute path.
  *
  * Paths are handled as text so that the answer does not depend on the host: `C:\Work\a.h`,
  * `c:/work/a.h`, `\\server\share\a.h` and `/usr/include/a.h` are absolute everywhere, and drive
  * letters and shares compare without regard to case.
  */
private[atom] object ReportedPaths:

  /** Where a file is relative to the analysed input. */
  enum Location:
    /** Inside the input; `relative` is the path under it. */
    case Inside(relative: String)

    /** Outside the input; `absolute` is the file's absolute path. */
    case Outside(absolute: String)

  /** A path split into its kind of root and its normalised segments. `ups` counts the leading `..`
    * segments of a relative path that cannot be resolved without a base.
    */
  private final case class Parsed(
    prefix: String,
    absolute: Boolean,
    segments: Vector[String],
    ups: Int
  ):
    def caseInsensitive: Boolean = prefix.nonEmpty

    def render: String =
      val body = segments.mkString("/")
      if !absolute then body
      else if body.isEmpty then (if prefix.isEmpty then "/" else s"$prefix/")
      else s"$prefix/$body"

  private val DrivePrefix = "^([A-Za-z]:)(?:/|$)".r
  private val UncPrefix   = "^(//[^/]+/[^/]+)(?:/|$)".r

  private def parse(raw: String): Parsed =
    val text = raw.replace('\\', '/')
    val (prefix, absolute, rest) =
        UncPrefix.findPrefixMatchOf(text).map(m =>
            (m.group(1).toLowerCase, true, text.drop(m.end(1)))
        )
            .orElse(
              DrivePrefix.findPrefixMatchOf(text).map(m =>
                  (m.group(1).toLowerCase, true, text.drop(m.end(1)))
              )
            )
            .getOrElse(if text.startsWith("/") then ("", true, text) else ("", false, text))
    val segments = mutable.ArrayBuffer.empty[String]
    var ups      = 0
    rest.split('/').foreach {
        case "" | "." => ()
        case ".." =>
            if segments.nonEmpty then segments.remove(segments.size - 1)
            else if !absolute then ups += 1
        case segment => segments += segment
    }
    Parsed(prefix, absolute, segments.toVector, ups)
  end parse

  /** The analysed input as the classifier sees it: its absolute path and, when it exists, its real
    * path. A single input file stands for the directory it sits in, so the headers next to it stay
    * project code.
    */
  final class Root private (private val forms: List[Parsed]):

    private val realFiles = mutable.HashMap.empty[String, Option[Parsed]]

    private def under(root: Parsed, file: Parsed): Option[Vector[String]] =
      val same                     = root.absolute == file.absolute && root.prefix == file.prefix
      val n                        = root.segments.size
      def eq(a: String, b: String) = if root.caseInsensitive then a.equalsIgnoreCase(b) else a == b
      if same && file.segments.size >= n &&
        root.segments.indices.forall(i => eq(root.segments(i), file.segments(i)))
      then Some(file.segments.drop(n))
      else None

    private def realOf(name: String): Option[Parsed] =
        realFiles.getOrElseUpdate(
          name,
          Try(Paths.get(name)).toOption.filter(Files.exists(_))
              .flatMap(p => Try(p.toRealPath()).toOption).map(p => parse(p.toString))
        )

    /** The file's location: relative names are inside the input unless they climb out of it. */
    def locate(fileName: String): Location =
        if fileName.isEmpty then Location.Inside(fileName)
        else
          val parsed = parse(fileName)
          if !parsed.absolute && parsed.ups == 0 then
            // the frontend relativised this name against the input; keep its spelling, with `/` as
            // the separator on every host
            Location.Inside(fileName.replace('\\', '/'))
          else
            val absolute =
                if parsed.absolute then parsed
                else
                  // a name climbing out of the input resolves against the input's own path
                  val base = forms.head
                  val kept = base.segments.dropRight(parsed.ups)
                  base.copy(segments = kept ++ parsed.segments)
            val candidates = absolute :: realOf(absolute.render).toList
            val relative = candidates.iterator.flatMap(c => forms.iterator.flatMap(under(_, c)))
                .nextOption()
            relative match
              case Some(segments) => Location.Inside(segments.mkString("/"))
              case None =>
                  Location.Outside(if parsed.absolute then fileName else absolute.render)
    end locate
  end Root

  object Root:
    /** The root for an input path: its absolute form, and its real form when that differs. */
    def apply(input: Path): Root =
      val directory =
          if Files.isRegularFile(input) then
            Option(input.toAbsolutePath.normalize.getParent).getOrElse(input.toAbsolutePath)
          else input.toAbsolutePath.normalize
      val absolute = parse(directory.toString)
      val real     = Try(directory.toRealPath()).toOption.map(p => parse(p.toString))
      new Root((absolute :: real.filter(_ != absolute).toList))

    /** A root from a path written as text, for callers that have no file on disk. */
    def fromText(path: String): Root = new Root(List(parse(path)))

  /** The path a report prints for a file: the input's name in front of a path inside the input (so
    * consumers that match on the project's top directory keep working), and an absolute path for a
    * file outside it.
    */
  def printed(location: Location, inputName: String): String = location match
    case Location.Inside(relative) =>
        if inputName.isEmpty || inputName == "." || relative.isEmpty then relative
        else s"$inputName/$relative"
    case Location.Outside(absolute) => absolute
end ReportedPaths
