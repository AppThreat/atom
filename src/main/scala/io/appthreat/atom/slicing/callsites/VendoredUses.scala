package io.appthreat.atom.slicing.callsites

import io.appthreat.atom.slicing.ProgramCallSites
import io.shiftleft.codepropertygraph.Cpg
import io.shiftleft.codepropertygraph.generated.nodes.*
import io.shiftleft.semanticcpg.language.*

import scala.collection.mutable

/** The uses of vendored code (decision 0001, P5) for every source language: a use from the
  * project's own code of a method or type that lives in a vendored file is a record whose `module`
  * is the vendored directory (or, for a BOM component's, the component's name) and whose `purl` is
  * the component's, empty otherwise. Uses inside vendored code are no records, as for any other
  * dependency.
  *
  * The frontends decide nothing here: `--external-paths` and the BOM's component locations marked
  * the vendored files' methods and types `isExternal` when the atom was built, and this reads those
  * marks together with the files' paths ([[VendoredCode]]). What it records, by the graph's own
  * edges and nothing more:
  *   - `call`: a call whose resolved callee (CALL edge) is a vendored method, at the call;
  *   - `extends`: a project type declaration whose inherited type is a vendored type.
  * Only methods the external-paths pass marked are read: package-manager directories (PHP's
  * `vendor/`, a venv, `node_modules`) were external already and stay with their own source, so the
  * pattern works beside them. A record the language source made at the position of a use of marked
  * code (Python names an import of `third_party.lib` after the import) is replaced.
  *
  * Every lookup is indexed once (vendored methods by id, vendored types by full name); no node
  * queries the whole graph.
  */
private[callsites] final class VendoredUses(
  atom: Cpg,
  vendored: VendoredCode,
  methodOf: AstNode => Option[Method]
):

  /** The records for the uses of vendored code. */
  def records(): List[CallSiteRecord] =
      if vendored.isEmpty then Nil
      else
        val methods = vendoredMethods
        val types   = vendoredTypes
        if methods.isEmpty && types.isEmpty then Nil
        else
          val out                          = mutable.ListBuffer.empty[CallSiteRecord]
          def add(r: CallSiteRecord): Unit = out += r

          atom.call.filterNot(_.name.startsWith("<operator>.")).foreach { c =>
            val callee = c._callOut.collectFirst { case m: Method if methods.contains(m.id()) => m }
            for
              m      <- callee
              v      <- vendored.of(m.filename)
              holder <- projectMethodOf(c)
            do
              add(
                CallSiteRecord(
                  holder.filename,
                  c.lineNumber.getOrElse(-1),
                  c.columnNumber.getOrElse(-1),
                  holder.fullName,
                  v.module,
                  m.name,
                  if c.name == "<operator>.new" || m.name == "<init>" then "new" else "call",
                  "binding",
                  m.fullName,
                  false,
                  "",
                  v.purl
                )
              )
            end for
          }

          atom.typeDecl.isExternal(false).filterNot(t => vendored.isVendored(t.filename)).foreach {
              t =>
                  t.inheritsFromTypeFullName.foreach { base =>
                      types.get(base).foreach { (bt, v) =>
                          add(
                            CallSiteRecord(
                              t.filename,
                              t.lineNumber.getOrElse(-1),
                              t.columnNumber.getOrElse(-1),
                              t.fullName,
                              v.module,
                              bt.name,
                              "extends",
                              "binding",
                              "",
                              false,
                              "",
                              v.purl
                            )
                          )
                      }
                  }
          }
          out.toList
        end if
  end records

  /** The method a call sits in, when that method is the project's own. */
  private def projectMethodOf(c: Call): Option[Method] =
      methodOf(c).filterNot(m => m.isExternal || vendored.isVendored(m.filename))

  /** The methods the external-paths pass marked (it tags them), so that a dependency a frontend
    * already knew (Composer's vendor/, a venv, node_modules) is left to its own source.
    */
  private lazy val markedMethods: List[Method] =
      atom.tag.nameExact(io.appthreat.x2cpg.passes.ExternalPathsPass.TagName)._taggedByIn
          .collect { case m: Method => m }.l

  private lazy val vendoredMethods: Set[Long] = markedMethods.map(_.id()).toSet

  /** The types in the files of marked methods (a type declaration cannot carry the tag). */
  private lazy val vendoredTypes: Map[String, (TypeDecl, Vendored)] =
    val files = markedMethods.map(_.filename).toSet
    atom.typeDecl.isExternal(true).filter(t => files.contains(t.filename))
        .flatMap(t => vendored.of(t.filename).map(v => t.fullName -> (t, v))).toMap
end VendoredUses

/** A language source plus the uses of vendored code. Without `--external-paths` and without BOM
  * component locations the vendored code is empty and the source's slice is returned untouched.
  */
private[callsites] final class VendoredCallSiteSource(
  inner: BaseCallSiteSource,
  externalPaths: Option[String]
) extends CallSiteSource:

  def calculate(): ProgramCallSites =
    val slice    = inner.calculate()
    val vendored = inner.vendoredCode(externalPaths)
    if vendored.isEmpty then slice
    else
      // uses inside vendored code are not records, as for any other dependency
      val project = slice.callsites.filterNot(r => vendored.isVendored(r.file)).map { r =>
          inner.vendoredModuleOf(r, vendored)
              .fold(r)(v => r.copy(module = v.module, purl = v.purl))
      }
      val lines = project.map(r => (r.file, r.line.intValue, r.module)).toSet
      // a language source that already named the vendored module (JavaScript, through its
      // import bindings) keeps its record
      val extra = inner.vendoredUses(externalPaths)
          .filterNot(r => lines.contains((r.file, r.line.intValue, r.module)))
      val sites    = extra.map(r => (r.file, r.line.intValue, r.col.intValue)).toSet
      val heritage = extra.filter(_.kind == "extends").map(r => (r.file, r.line.intValue)).toSet
      val kept = project.filterNot { r =>
          sites.contains((r.file, r.line.intValue, r.col.intValue)) ||
          (Set("extends", "implements")(r.kind) && heritage.contains((r.file, r.line.intValue)))
      }
      if extra.isEmpty && kept == slice.callsites then slice
      else slice.copy(callsites = CallSiteRecord.dedupe(kept ++ extra))
    end if
  end calculate

  override def metadata: Option[io.appthreat.atom.slicing.CallSitesMetadata] = inner.metadata
end VendoredCallSiteSource
