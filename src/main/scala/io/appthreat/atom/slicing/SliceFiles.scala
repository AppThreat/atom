package io.appthreat.atom.slicing

import better.files.File
import io.appthreat.x2cpg.utils.AtomicWrites

/** Writing slices files so that a reader never sees half of one. */
object SliceFiles:

  /** Write `target` through a temporary sibling that is renamed into place once complete.
    *
    * A run stopped while writing (a timeout, a kill) otherwise leaves a truncated JSON file at the
    * final path, which consumers take for a finished slice and then fail to parse. The rename is
    * atomic where the filesystem supports it, and a plain replace where it does not.
    *
    * Slice outputs are written beside the scanned tree, whose checkout decides the names in that
    * directory before the run does. The temporary sibling and the rename are therefore chen's
    * [[AtomicWrites.replace]], whose temporary name no checkout can predict: a symbolic link
    * committed at that name, or at the final one, is replaced by the rename rather than written
    * through, so the file a link points at outside the tree keeps its bytes.
    */
  def writeAtomically(target: File)(write: File => Unit): Unit =
    Option(target.parent).foreach(_.createDirectoryIfNotExists(createParents = true))
    AtomicWrites.replace(target.path)(temp => write(File(temp)))
