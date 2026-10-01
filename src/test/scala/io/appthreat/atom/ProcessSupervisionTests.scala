package io.appthreat.atom

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.TimeUnit
import scala.util.Properties

/** atom must not outlive the process that supervises it, nor leave its helper processes behind. */
class ProcessSupervisionTests extends AnyFunSuite with Matchers:

  private def startSleeper(): Process =
      if Properties.isWin then
        ProcessBuilder("powershell", "-NoProfile", "-Command", "Start-Sleep -Seconds 60").start()
      else ProcessBuilder("sleep", "60").start()

  test("the watchdog is off unless ATOM_PARENT_PID names a process"):
    ProcessSupervision.supervisorWatch(Map.empty) shouldBe None
    ProcessSupervision.supervisorWatch(Map("ATOM_PARENT_PID" -> "")) shouldBe None
    ProcessSupervision.supervisorWatch(Map("ATOM_PARENT_PID" -> "abc")) shouldBe None
    ProcessSupervision.supervisorWatch(Map("ATOM_PARENT_PID" -> "0")) shouldBe None
    ProcessSupervision.supervisorWatch(Map("ATOM_PARENT_PID" -> "-5")) shouldBe None

  test("a live supervisor that is still atom's parent is not gone"):
    val parent = ProcessHandle.current().parent().map(_.pid()).orElse(-1L)
    val watch  = ProcessSupervision.supervisorWatch(Map("ATOM_PARENT_PID" -> parent.toString))
    watch.map(_.isGone) shouldBe Some(false)

  test("a supervisor that exited is gone, even though atom's parent is unchanged"):
    val sleeper = startSleeper()
    try
      val watch = ProcessSupervision.SupervisorWatch(
        sleeper.pid(),
        Some(sleeper.toHandle),
        Some(1L),
        () => Some(1L)
      )
      watch.isGone shouldBe false
      sleeper.destroyForcibly().waitFor(10, TimeUnit.SECONDS)
      watch.isGone shouldBe true
    finally sleeper.destroyForcibly()

  test("being re-parented means the launcher is gone"):
    var parent = Option(42L)
    val watch = ProcessSupervision.SupervisorWatch(
      ProcessHandle.current().pid(),
      Some(ProcessHandle.current()),
      Some(42L),
      () => parent
    )
    watch.isGone shouldBe false
    parent = Some(1L)
    watch.isGone shouldBe true
    parent = None
    watch.isGone shouldBe true

  test("a supervisor that was never there is gone"):
    ProcessSupervision.SupervisorWatch(12345L, None, Some(1L), () => Some(1L)).isGone shouldBe true

  test("helper processes are stopped"):
    val helpers = List(startSleeper(), startSleeper())
    try
      ProcessSupervision.stopProcesses(helpers.map(_.toHandle))
      helpers.foreach(_.waitFor(10, TimeUnit.SECONDS))
      helpers.map(_.isAlive) shouldBe List(false, false)
    finally helpers.foreach(_.destroyForcibly())
end ProcessSupervisionTests
