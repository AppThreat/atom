package io.appthreat.atom.slicing

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.util.Random
import ReachableSlicing.dedupKeepMask

/** `dedupKeepMask` must keep exactly the entries the former string-signature dedup kept. */
class ReachableDedupTests extends AnyFunSuite with Matchers:

  /** The former formulation, verbatim but for the entry type. */
  private def referenceKeep(ids: IndexedSeq[Array[Long]]): Array[Boolean] =
    val signatures = ids.map(s => s.map(n => s"#$n").mkString + "#")
    val byNode     = scala.collection.mutable.HashMap.empty[Long, List[Int]]
    ids.indices.foreach(i =>
        ids(i).toSet.foreach(n => byNode.update(n, i :: byNode.getOrElse(n, Nil)))
    )
    val keep  = Array.fill(ids.size)(false)
    val order = ids.indices.sortBy(i => (-signatures(i).length, signatures(i)))
    order.foreach { i =>
      val sig        = signatures(i)
      val candidates = byNode.getOrElse(ids(i).head, Nil) ++ byNode.getOrElse(ids(i).last, Nil)
      keep(i) = !candidates.exists { j =>
          j != i && keep(j) && (signatures(j) == sig ||
              (signatures(j).length > sig.length && signatures(j).contains(sig)))
      }
    }
    keep

  test("contained, equal and distinct routes"):
    val ids = IndexedSeq(
      Array(1L, 2L, 3L, 4L),
      Array(2L, 3L),         // contained
      Array(1L, 2L, 3L, 4L), // duplicate
      Array(3L, 2L),         // reversed: distinct
      Array(11L, 2L),        // id prefix of another id: distinct
      Array(2L, 3L, 4L, 5L)  // overlaps but not contained
    )
    dedupKeepMask(ids).toSeq.shouldBe(referenceKeep(ids).toSeq)
    dedupKeepMask(ids).count(identity).shouldBe(4)

  test("agrees with the string formulation on random flows"):
    val rnd = new Random(42)
    (1 to 300).foreach { round =>
      val alphabet = 2 + rnd.nextInt(if round % 2 == 0 then 4 else 30)
      val base = IndexedSeq.fill(1 + rnd.nextInt(6))(
        Array.fill(2 + rnd.nextInt(12))(rnd.nextInt(alphabet).toLong + 1)
      )
      val ids = IndexedSeq.fill(2 + rnd.nextInt(40)) {
          val b = base(rnd.nextInt(base.size))
          if rnd.nextBoolean() && b.length > 2 then
            val from = rnd.nextInt(b.length - 1)
            b.slice(from, from + 2 + rnd.nextInt(b.length - from - 1))
          else Array.fill(2 + rnd.nextInt(8))(rnd.nextInt(alphabet).toLong + 1)
      }
      withClue(s"round $round: ${ids.map(_.mkString(",")).mkString(" | ")}") {
          dedupKeepMask(ids).toSeq.shouldBe(referenceKeep(ids).toSeq)
      }
    }
end ReachableDedupTests
