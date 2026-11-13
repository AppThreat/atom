package io.appthreat.atom

import io.appthreat.x2cpg.utils.ExternalCommand

import java.nio.file.{Files, Path, Paths}
import java.util.Locale
import scala.util.{Failure, Try}

/** Starts the helper programs atom hands its results to (`atom-tools`, `scalasem`) with one
  * argument per array element and no shell, so a path or name in an argument is never parsed as
  * shell syntax.
  *
  * On Windows a program name is looked up on the `PATH` with the `PATHEXT` extensions, because npm
  * installs its commands as `.cmd` scripts. Windows always runs such a script through `cmd.exe`,
  * which expands `%` and treats `"`, `^`, `&`, `|`, `<` and `>` specially even inside quotes, so a
  * script is not started with an argument holding any of those characters.
  */
object HelperCommand:

  private val DefaultPathExt = Seq(".COM", ".EXE", ".BAT", ".CMD")

  private val BatchExtensions = Set(".bat", ".cmd")

  private val BatchUnsafeChars = Set('"', '%', '!', '^', '&', '|', '<', '>', '\r', '\n')

  private def isWindows(os: String): Boolean = os.toLowerCase(Locale.ROOT).startsWith("windows")

  private def envValue(env: Map[String, String], key: String): Option[String] =
      env.collectFirst { case (k, v) if k.equalsIgnoreCase(key) => v }

  private def pathDirs(env: Map[String, String], separator: String): Seq[Path] =
      envValue(env, "PATH").toSeq
          .flatMap(_.split(java.util.regex.Pattern.quote(separator)))
          .map(_.trim.stripPrefix("\"").stripSuffix("\""))
          .filter(_.nonEmpty)
          .flatMap(d => Try(Paths.get(d)).toOption)

  /** The program to start for `name`. Outside Windows this is `name` itself, which the process
    * builder looks up on the `PATH`. On Windows it is the first `PATH` entry holding `name` with
    * one of the `PATHEXT` extensions, or `name` when none does.
    */
  private[atom] def resolve(
    name: String,
    env: Map[String, String] = sys.env,
    os: String = System.getProperty("os.name", "")
  ): String =
      if !isWindows(os) || name.contains("/") || name.contains("\\") then name
      else
        val extensions = envValue(env, "PATHEXT")
            .map(_.split(";").toSeq.map(_.trim).filter(_.startsWith(".")))
            .filter(_.nonEmpty)
            .getOrElse(DefaultPathExt)
        val hasExtension = extensions.exists(e =>
            name.toLowerCase(Locale.ROOT).endsWith(e.toLowerCase(Locale.ROOT))
        )
        val candidates = (if hasExtension then Seq(name) else Nil) ++ extensions.map(name + _)
        pathDirs(env, ";").iterator
            .flatMap(d => candidates.iterator.map(d.resolve))
            .find(Files.isRegularFile(_))
            .map(_.toString)
            .getOrElse(name)

  private def isBatchFile(program: String): Boolean =
      BatchExtensions.exists(program.toLowerCase(Locale.ROOT).endsWith)

  /** The command line for `name` and `args`, or the reason it cannot be started safely. */
  private[atom] def command(
    name: String,
    args: Seq[String],
    env: Map[String, String] = sys.env,
    os: String = System.getProperty("os.name", "")
  ): Either[String, Seq[String]] =
    val program = resolve(name, env, os)
    if isWindows(os) && isBatchFile(program) then
      args.find(_.exists(BatchUnsafeChars.contains)) match
        case Some(arg) =>
            Left(
              s"$program is a batch script, and the argument '$arg' holds a character that " +
                  "cmd.exe would interpret. Rename the path or install the program as an executable."
            )
        case None => Right(program +: args)
    else Right(program +: args)

  /** Runs `name` with `args` in `cwd` and returns its standard output. */
  def run(name: String, args: Seq[String], cwd: String): Try[Seq[String]] =
      command(name, args) match
        case Left(reason) => Failure(new IllegalArgumentException(reason))
        case Right(cmd)   => ExternalCommand.runWithResult(cmd, cwd).toTry
end HelperCommand
