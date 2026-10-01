package io.appthreat.atom.slicing

import better.files.File

import java.nio.file.{AtomicMoveNotSupportedException, Files, StandardCopyOption}
import scala.util.control.NonFatal

/** Writing slices files so that a reader never sees half of one. */
object SliceFiles:

  /** Write `target` through a temporary sibling that is renamed into place once complete.
    *
    * A run stopped while writing (a timeout, a kill) otherwise leaves a truncated JSON file at the
    * final path, which consumers take for a finished slice and then fail to parse. The rename is
    * atomic where the filesystem supports it, and a plain replace where it does not.
    */
  def writeAtomically(target: File)(write: File => Unit): Unit =
    Option(target.parent).foreach(_.createDirectoryIfNotExists(createParents = true))
    val temp = target.sibling(s".${target.name}.${ProcessHandle.current().pid()}.tmp")
    try
      write(temp)
      try
        Files.move(
          temp.path,
          target.path,
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE
        )
      catch
        case _: AtomicMoveNotSupportedException =>
            Files.move(temp.path, target.path, StandardCopyOption.REPLACE_EXISTING)
    catch
      case NonFatal(e) =>
          temp.delete(swallowIOExceptions = true)
          throw e
  end writeAtomically
end SliceFiles
