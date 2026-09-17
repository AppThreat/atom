package io.appthreat.atom

import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.cpgloading.CpgLoaderConfig
import io.shiftleft.codepropertygraph.generated.DispatchTypes
import io.shiftleft.semanticcpg.language.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

/** Scratch harness: opens an already-generated `.atom` and prints call-graph health metrics. Run
  * with `ATOM_PROBE=/path/to/app.atom`. Not part of the normal suite - it no-ops without that
  * variable, so a plain `sbt test` never opens anything.
  *
  * Three further variables each dump a detail to a file when set: `ATOM_PROBE_DUMP_GRAPH` (calls
  * and tags), `ATOM_PROBE_DUMP_METHODS`, `ATOM_PROBE_DUMP_SUMMARIES`.
  */
class ScratchCorpusProbe extends AnyFreeSpec with Matchers:

  private def open(filename: String): Cpg =
    val odbConfig = overflowdb.Config.withDefaults().withStorageLocation(filename)
    io.shiftleft.codepropertygraph.cpgloading.CpgLoader.loadFromOverflowDb(
      CpgLoaderConfig().withOverflowConfig(odbConfig).doNotCreateIndexesOnLoad
    )

  "probe" in {
      sys.env.get("ATOM_PROBE") match
        case None => succeed
        case Some(path) =>
            val cpg            = open(path)
            val calls          = cpg.call.nameNot("<operator>.*", "<operators>.*").l
            val dyn            = calls.filter(_.dispatchType == DispatchTypes.DYNAMIC_DISPATCH)
            val stat           = calls.filter(_.dispatchType == DispatchTypes.STATIC_DISPATCH)
            val dynUnresolved  = dyn.filter(_.callee(using NoResolve).isEmpty)
            val statUnresolved = stat.filter(_.callee(using NoResolve).isEmpty)
            println(s"== $path ==")
            // `tags` counts TAG nodes, `taggedNodes` the nodes wearing at least one of them - a
            // node commonly wears several, so the two differ and printing the same number twice
            // hid that.
            val taggedNodes = cpg.tag.l.flatMap(_._taggedByIn.l).distinct.size
            println(s"tags=${cpg.tag.size} taggedNodes=$taggedNodes")
            sys.env.get("ATOM_PROBE_DUMP_GRAPH").foreach { out =>
              val lines = (
                cpg.call.l.map(c =>
                    s"CALL\t${c.methodFullName}\t${c.dispatchType}\t${c.code.replace('\n', ' ')}"
                ) ++
                    cpg.tag.l.flatMap(t =>
                        t._taggedByIn.l.map(n => s"TAG\t${t.name}\t${t.value}\t${n.label}")
                    ) ++
                    cpg.typeDecl.l.map(t =>
                        s"TYPE\t${t.fullName}\t${t.inheritsFromTypeFullName.mkString(",")}"
                    )
              ).sorted
              java.nio.file.Files.write(java.nio.file.Paths.get(out), lines.mkString("\n").getBytes)
              println(s"graph written to $out (${lines.size})")
            }
            sys.env.get("ATOM_PROBE_DUMP_METHODS").foreach { out =>
              val lines = cpg.method.fullName.l.sorted
              java.nio.file.Files.write(java.nio.file.Paths.get(out), lines.mkString("\n").getBytes)
              println(s"methods written to $out (${lines.size})")
            }
            sys.env.get("ATOM_PROBE_DUMP_SUMMARIES").foreach { out =>
              val lines = cpg.method.internal.l.flatMap { m =>
                  m.tag.nameExact("flow-summary").value.headOption.map(v => s"${m.fullName}\t$v")
              }.sorted
              java.nio.file.Files.write(java.nio.file.Paths.get(out), lines.mkString("\n").getBytes)
              println(s"summaries written to $out (${lines.size})")
            }
            cpg.tag.name.groupCount.toList.sortBy(-_._2).take(12).foreach { case (n, c) =>
                println(f"  tag $n%-28s $c%6d")
            }
            println(
              s"methods=${cpg.method.size} internal=${cpg.method.internal.size} typeDecl=${cpg.typeDecl.size}"
            )
            println(s"calls=${calls.size} dynamic=${dyn.size} static=${stat.size}")
            println(
              s"dynamic unresolved=${dynUnresolved.size} static unresolved=${statUnresolved.size}"
            )
            println(s"dynamic multi-target=${dyn.count(_.callee(using NoResolve).size > 1)}")
            println(s"calls with <unresolved/ambiguous> fullName=${calls.count(c =>
                    c.methodFullName.contains("unresolved") || c.methodFullName.contains("ANY") || c.methodFullName.startsWith("<empty>")
                )}")
            println("-- top unresolved dynamic callees --")
            dynUnresolved.groupBy(_.methodFullName).toList.sortBy(-_._2.size).take(25).foreach {
                case (fn, cs) => println(f"  ${cs.size}%5d  $fn")
            }
            println("-- top unresolved static callees --")
            statUnresolved.groupBy(_.methodFullName).toList.sortBy(-_._2.size).take(25).foreach {
                case (fn, cs) => println(f"  ${cs.size}%5d  $fn")
            }
            cpg.close()
            succeed
  }
end ScratchCorpusProbe
