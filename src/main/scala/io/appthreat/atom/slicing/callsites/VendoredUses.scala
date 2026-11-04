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
  * marks together with the files' paths ([[VendoredCode]]). Once marked, vendored code is a
  * dependency to the language sources too, and they record its uses the way they record any other
  * dependency's, with their own kinds and positions. This class gives those records their vendored
  * target ([[targetOf]]) and, where a language source has no record for a use, makes one from the
  * graph's own edges:
  *   - `call` or `new`: a call whose resolved callee (CALL edge) is a vendored method;
  *   - `extends`: a project type declaration whose inherited type is a vendored type.
  *
  * Only methods the external-paths pass marked are read: package-manager directories (PHP's
  * `vendor/`, a venv, `node_modules`) were external already and stay with their own source, so the
  * pattern works beside them. Every lookup is indexed once; no node queries the whole graph.
  */
private[callsites] final class VendoredUses(
  atom: Cpg,
  vendored: VendoredCode,
  methodOf: AstNode => Option[Method]
):

  /** The methods the external-paths pass marked (it tags them), so that a dependency a frontend
    * already knew (Composer's vendor/, a venv, node_modules) is left to its own source.
    */
  private lazy val markedMethods: List[Method] =
      if vendored.isEmpty then Nil
      else
        atom.tag.nameExact(io.appthreat.x2cpg.passes.ExternalPathsPass.TagName)._taggedByIn
            .collect { case m: Method => m }.l

  private lazy val vendoredMethodIds: Set[Long] = markedMethods.map(_.id()).toSet

  /** A name to the one vendored code it names; a name two vendored places share names neither. */
  private def unique[A](pairs: Iterable[(String, (A, Vendored))]): Map[String, (A, Vendored)] =
      pairs.groupBy(_._1).collect {
          case (name, hits) if hits.map(_._2._2).toSet.size == 1 => name -> hits.head._2
      }

  private lazy val methodsByName: Map[String, (Method, Vendored)] =
      unique(markedMethods.flatMap(m => vendored.of(m.filename).map(v => m.fullName -> (m, v))))

  /** The types in the files of marked methods (a type declaration cannot carry the tag), by full
    * name and by the names the languages write for them: PHP's are case-insensitive and may start
    * with `\`, and Ruby inherits a class by its constant path (`Vend.Tool`) while the declaration's
    * full name starts with its file (`third_party/lib/tool.rb:<main>.Vend.Tool`).
    */
  private lazy val typesByName: Map[String, (TypeDecl, Vendored)] =
    val files = markedMethods.map(_.filename).toSet
    val types = atom.typeDecl.isExternal(true).filter(t => files.contains(t.filename)).l
        .flatMap(t => vendored.of(t.filename).map(t -> _))
    unique(types.flatMap { (t, v) =>
        VendoredUses.namesOf(t.fullName).map(_ -> (t, v))
    })

  private def typeNamed(name: String): Option[(TypeDecl, Vendored)] =
      VendoredUses.namesOf(name).iterator.flatMap(typesByName.get).nextOption()

  /** The vendored code a language source's record uses, by the method or type it resolved to:
    * `com.vend.Helper.run:int(int)`, `Vend\Tool->go`, or a declared type `com.vend.Helper`. The
    * record keeps its kind, position and member; its `module` and `purl` become the vendored
    * code's, and a record that named only the type (its module was the type) names it as its path,
    * as the records made here do: the member for a call, the type for `new`, `extends` and
    * `decltype`.
    */
  def attribute(record: CallSiteRecord): Option[CallSiteRecord] =
    val target = Option(record.resolvedMethod).filter(_.nonEmpty).flatMap { name =>
        methodsByName.get(name).map((m, v) => (VendoredUses.nameOf(m), v))
            .orElse(typeNamed(name).map((t, v) => (t.name, v)))
    }
    target.map { (typeName, v) =>
      val path = if record.path.nonEmpty then record.path else typeName
      record.copy(module = v.module, path = path, purl = v.purl)
    }

  /** The records for the uses no language source recorded. */
  def records(): List[CallSiteRecord] =
      if markedMethods.isEmpty then Nil
      else
        val out = mutable.ListBuffer.empty[CallSiteRecord]
        atom.call.filterNot(_.name.startsWith("<operator>.")).foreach { c =>
          // A call the linkers tie to several vendored methods (dynamic dispatch) is a use of the
          // one it names, else of the smallest by name; the order of the call's edges, which the
          // linkers' passes decide, never does.
          val callee = c._callOut.collect {
              case m: Method if vendoredMethodIds.contains(m.id()) => m
          }.toList.minByOption(m => (m.fullName != c.methodFullName, m.fullName))
          for
            m      <- callee
            v      <- vendored.of(m.filename)
            holder <- projectMethodOf(c)
          do
            val constructor = VendoredUses.isConstructor(m)
            out += CallSiteRecord(
              holder.filename,
              c.lineNumber.getOrElse(-1),
              c.columnNumber.getOrElse(-1),
              holder.fullName,
              v.module,
              VendoredUses.nameOf(m),
              if constructor then "new" else "call",
              "binding",
              m.fullName,
              false,
              "",
              v.purl
            )
          end for
        }
        atom.typeDecl.isExternal(false).filterNot(t => vendored.isVendored(t.filename)).foreach {
            t =>
                t.inheritsFromTypeFullName.flatMap(typeNamed).foreach { (bt, v) =>
                    out += CallSiteRecord(
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
                }
        }
        out.toList

  /** The method a call sits in, when that method is the project's own. */
  private def projectMethodOf(c: Call): Option[Method] =
      methodOf(c).filterNot(m => m.isExternal || vendored.isVendored(m.filename))
end VendoredUses

private[callsites] object VendoredUses:

  /** The names a type is written by: its full name, without a leading `\`, lower-cased (PHP), and
    * after the file prefix a Ruby declaration's full name carries.
    */
  def namesOf(fullName: String): List[String] =
    val bare = fullName.stripPrefix("\\")
    val ruby = bare.indexOf(":<main>.") match
      case -1 => Nil
      case i  => List(bare.substring(i + ":<main>.".length))
    (bare :: bare.toLowerCase :: ruby).distinct

  /** The constructors the frontends name: Java and JavaScript `<init>`, PHP `__construct`, Ruby
    * `initialize`.
    */
  def isConstructor(m: Method): Boolean =
      m.name == "<init>" || m.name == "__construct" || m.name == "initialize"

  /** What a record of a use of `m` names as its path: the type for a constructor, else the method
    * (a free function, or a member).
    */
  def nameOf(m: Method): String =
      if isConstructor(m) then m.definingTypeDecl.map(_.name).headOption.getOrElse(m.name)
      else m.name
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
      val uses = inner.vendoredUses(vendored)
      // uses inside vendored code are not records, as for any other dependency; the language
      // source's own records of vendored code keep their kind and position and name its module
      val project = slice.callsites.filterNot(r => vendored.isVendored(r.file)).map { r =>
          uses.attribute(r).orElse(
            inner.vendoredModuleOf(r, vendored).map(v => r.copy(module = v.module, purl = v.purl))
          ).getOrElse(r)
      }
      // the graph's uses the language source made no record of: none at the same position, and
      // none of the same member (or type, for `new` and heritage) of that module on that line
      def key(r: CallSiteRecord) = r.kind match
        case "extends" | "implements" => (r.file, r.line.intValue, r.module, "heritage", "")
        case "new"                    => (r.file, r.line.intValue, r.module, "new", "")
        case _                        => (r.file, r.line.intValue, r.module, "use", r.path)
      val positions = project.map(r => (r.file, r.line.intValue, r.col.intValue)).toSet
      val keys      = project.map(key).toSet
      // the slice names files with `/` (CallSiteRecord.withPortableFile), the graph with the
      // frontend's separator, so the graph's records take the slice's form before they compare
      val extra = uses.records().map(CallSiteRecord.withPortableFile).filterNot(r =>
          positions.contains((r.file, r.line.intValue, r.col.intValue)) || keys.contains(key(r))
      )
      if extra.isEmpty && project == slice.callsites then slice
      else slice.copy(callsites = CallSiteRecord.dedupe(project ++ extra))
    end if
  end calculate

  override def metadata: Option[io.appthreat.atom.slicing.CallSitesMetadata] = inner.metadata
end VendoredCallSiteSource
