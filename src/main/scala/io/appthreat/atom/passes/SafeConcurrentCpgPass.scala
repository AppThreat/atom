package io.appthreat.atom.passes
import io.shiftleft.passes.{ConcurrentWriterCpgPass, KeyPool}
import io.shiftleft.SerializedCpg
import io.shiftleft.codepropertygraph.Cpg

import scala.annotation.nowarn

object SafeConcurrentCpgPass {
  private val writerQueueCapacity   = 1
  private val producerQueueCapacity = 1
}
abstract class SafeConcurrentCpgPass[T <: AnyRef](
  cpg: Cpg,
  @nowarn outName: String = "",
  keyPool: Option[KeyPool] = None
) extends ConcurrentWriterCpgPass[T](cpg: Cpg, outName: String, keyPool: Option[KeyPool]) {}
