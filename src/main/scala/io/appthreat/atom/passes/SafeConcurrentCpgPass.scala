package io.appthreat.atom.passes
import io.shiftleft.SerializedCpg
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.passes.{KeyPool, NewStyleCpgPassBase}
import io.shiftleft.utils.ExecutionContextProvider

import java.util.concurrent.LinkedBlockingQueue
import scala.annotation.nowarn
import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

/** SafeConcurrentCpgPass is a modified version of ConcurrentWriterCpgPass
  */
object SafeConcurrentCpgPass:
    private val producerQueueCapacity = Runtime.getRuntime.availableProcessors() / 2
    private val writerQueueCapacity   = Math.max(Math.floor(producerQueueCapacity / 2).toInt, 2)
abstract class SafeConcurrentCpgPass[T <: AnyRef](
  cpg: Cpg,
  @nowarn outName: String = "",
  keyPool: Option[KeyPool] = None
) extends NewStyleCpgPassBase[T]:

    @volatile private var nDiffT: Int = -1

    override def createApplySerializeAndStore(
      serializedCpg: SerializedCpg,
      inverse: Boolean = false,
      prefix: String = ""
    ): Unit =
        import SafeConcurrentCpgPass.producerQueueCapacity
        var nDiff          = 0
        var completedParts = 0
        nDiffT = -1
        init()
        val parts    = generateParts()
        val nParts   = parts.length
        val partIter = parts.iterator
        val completionQueue =
            mutable.ArrayDeque[Future[overflowdb.BatchedUpdate.DiffGraphBuilder]]()
        val writer       = new Writer()
        val writerThread = new Thread(writer)
        writerThread.setName("Writer")
        writerThread.start()
        implicit val ec: ExecutionContext = ExecutionContextProvider.getExecutionContext
        var done                          = false
        try
            while !done || completedParts < nParts do
                if completionQueue.size < producerQueueCapacity && partIter.hasNext then
                    val next = partIter.next()
                    completionQueue.prepend(Future.apply {
                        val builder = new DiffGraphBuilder
                        runOnPart(builder, next.asInstanceOf[T])
                        builder
                    })
                else if completionQueue.nonEmpty then
                    val future = completionQueue.removeLast()
                    val res    = Await.result(future, Duration.Inf).build()
                    nDiff += res.size
                    writer.queue.put(Some(res))
                    completedParts += 1
                else
                    writer.queue.put(None)
                    completedParts += 1
                    done = true
        finally
            try
                writerThread.join()
            finally
                finish()
        end try
    end createApplySerializeAndStore

    private class Writer() extends Runnable:

        val queue =
            new LinkedBlockingQueue[Option[overflowdb.BatchedUpdate.DiffGraph]](
              SafeConcurrentCpgPass.writerQueueCapacity
            )

        override def run(): Unit =
            var terminate  = false
            var index: Int = 0
            nDiffT = 0
            var hadErrors = false
            while !terminate do
                try
                    queue.take() match
                        case None =>
                            terminate = true
                        case Some(diffGraph) =>
                            nDiffT += overflowdb.BatchedUpdate
                                .applyDiff(cpg.graph, diffGraph, keyPool.orNull, null)
                                .transitiveModifications()
                            index += 1
                finally
                    hadErrors = true
    end Writer
end SafeConcurrentCpgPass
