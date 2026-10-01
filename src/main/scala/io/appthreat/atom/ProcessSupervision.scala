package io.appthreat.atom

import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*
import scala.util.Try

/** Keeps atom from outliving whoever launched it, and its helper processes from outliving atom.
  *
  * atom normally runs under the npm dispatcher (or directly under cdxgen), which waits for it and
  * relays its exit status. When that launcher is killed outright, nothing reaps atom: it is adopted
  * by init and keeps analysing for nobody, holding a heap sized for the whole machine.
  *
  *   - `ATOM_PARENT_PID` names the supervising process; the dispatcher sets it to itself. A daemon
  *     thread checks once a second, and atom exits once that process is gone or atom has been
  *     re-parented.
  *   - A shutdown hook stops the helper processes atom started (astgen, php-parse, atom-tools,
  *     ...), so that a SIGTERM, a timeout or the watchdog does not leave those behind in turn.
  */
object ProcessSupervision:

  /** The status of a process stopped by SIGTERM, which is what the supervisor would have sent. */
  val SupervisorGoneExitCode = 143

  private val PollMillis = 1000L
  // How long helpers get to exit after SIGTERM before they are killed.
  private val HelperGraceMillis = 2000L
  // If exiting itself hangs (a shutdown hook stuck on an exhausted heap), halt after this long.
  private val HaltGraceMillis = 15000L

  /** Whether the supervisor named by `ATOM_PARENT_PID` is gone. Kept apart from the thread so the
    * decision can be tested.
    *
    * @param pid
    *   the supervisor's pid
    * @param supervisor
    *   its handle, taken at startup. A handle remembers the process start time, so a recycled pid
    *   does not read as the supervisor still being alive.
    * @param initialParent
    *   atom's parent at startup
    * @param currentParent
    *   reads atom's parent now. An orphan is adopted (POSIX) or loses its parent (Windows), so a
    *   change means the launcher has exited even when it was not the supervisor itself.
    */
  final class SupervisorWatch(
    val pid: Long,
    supervisor: Option[ProcessHandle],
    initialParent: Option[Long],
    currentParent: () => Option[Long]
  ):
    def isGone: Boolean = !supervisor.exists(_.isAlive) || currentParent() != initialParent

  /** The watch configured by `ATOM_PARENT_PID`, if any. */
  def supervisorWatch(env: Map[String, String]): Option[SupervisorWatch] =
      env.get("ATOM_PARENT_PID").flatMap(_.trim.toLongOption).filter(_ > 0).map { pid =>
        val currentParent = () => ProcessHandle.current().parent().toScala.map(_.pid())
        SupervisorWatch(pid, ProcessHandle.of(pid).toScala, currentParent(), currentParent)
      }

  /** Install the shutdown hook and, when `ATOM_PARENT_PID` is set, the supervisor watchdog. */
  def install(env: Map[String, String] = sys.env): Unit =
    Runtime.getRuntime.addShutdownHook(Thread(() => stopHelpers(), "atom-stop-helpers"))
    supervisorWatch(env).foreach(startWatchdog)

  private def startWatchdog(watch: SupervisorWatch): Unit =
      startDaemon("atom-supervisor-watch")(watchSupervisor(watch))

  private def watchSupervisor(watch: SupervisorWatch): Unit =
    while !watch.isGone do Thread.sleep(PollMillis)
    System.err.println(s"atom: supervising process ${watch.pid} is gone; stopping.")
    startDaemon("atom-halt")(haltAfterGrace())
    System.exit(SupervisorGoneExitCode)

  private def haltAfterGrace(): Unit =
    Thread.sleep(HaltGraceMillis)
    Runtime.getRuntime.halt(SupervisorGoneExitCode)

  private def startDaemon(name: String)(body: => Unit): Unit =
    val thread = Thread(() => body, name)
    thread.setDaemon(true)
    thread.start()

  /** Stop every process atom started, children and their descendants alike: SIGTERM first, then
    * SIGKILL for any still running after a short grace period.
    */
  private def stopHelpers(): Unit =
      stopProcesses(ProcessHandle.current().descendants().iterator().asScala.toList)

  private[atom] def stopProcesses(helpers: List[ProcessHandle]): Unit =
    helpers.foreach(_.destroy())
    val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(HelperGraceMillis)
    helpers.foreach { helper =>
      val remaining = deadline - System.nanoTime()
      if remaining > 0 then
        Try(helper.onExit().get(remaining, TimeUnit.NANOSECONDS))
    }
    helpers.filter(_.isAlive).foreach(_.destroyForcibly())
end ProcessSupervision
