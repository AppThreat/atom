package io.appthreat.atom.slicing

import better.files.File
import io.appthreat.x2cpg.utils.AtomicWrites

import java.nio.file.Files

/** Writing slices files so that a reader never sees half of one. */
object SliceFiles:

  /** Write `target` through a temporary sibling that is renamed into place once complete.
    *
    * A run stopped while writing (a timeout, a kill) otherwise leaves a truncated JSON file at the
    * final path, which consumers take for a finished slice and then fail to parse. The rename is
    * atomic where the filesystem supports it, and a plain replace where it does not.
    *
    * Slice outputs are written beside the scanned tree, whose checkout decides the names in that
    * directory before the run does. The scratch the caller fills and the temporary that is renamed
    * onto `target` therefore both carry names no checkout can predict, and the naming and the
    * rename are chen's [[AtomicWrites.copy]]: a symbolic link committed at any of those names, the
    * final one included, is replaced by the rename rather than written through, so the file a link
    * points at outside the tree keeps its bytes.
    */
  def writeAtomically(target: File)(write: File => Unit): Unit =
    Option(target.parent).foreach(_.createDirectoryIfNotExists(createParents = true))
    val temp = File(Files.createTempFile(target.parent.path, s".${target.name}.", ".tmp"))
    try
      write(temp)
      AtomicWrites.copy(temp.path, target.path)
    finally
      temp.delete(swallowIOExceptions = true)
  end writeAtomically
end SliceFiles
