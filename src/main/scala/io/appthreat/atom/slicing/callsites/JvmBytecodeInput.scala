package io.appthreat.atom.slicing.callsites

import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.Try
import scala.util.control.NonFatal

/** One class of the bytecode input, with the file name the oracle derives for it. */
private[callsites] final case class JvmClassInfo(
  name: String,
  sourceFile: Option[String],
  sourceGuessed: Boolean
)

/** A method handle a JVM `invokedynamic` bootstraps (`String::length`, `Foo::new`), with the `kind`
  * and `via` the oracle gives it.
  */
private[callsites] final case class JvmMethodHandle(
  owner: String,
  name: String,
  kind: String,
  via: String
)

/** One `invokedynamic` instruction of a class: its enclosing method (name, and signature in the
  * graph's `ret(params)` form), its line, and the interface method it calls, so the slice can match
  * the graph call Soot lowered it to.
  */
private[callsites] final case class JvmIndySite(
  methodName: String,
  methodSignature: String,
  line: Int,
  indyName: String,
  handles: List[JvmMethodHandle]
)

/** The application boundary (atom#326): the packages the input declares as its own. */
private[callsites] final case class AppBoundary(
  prefixes: List[String],
  appAll: Boolean,
  source: String
):
  def isApp(className: String): Boolean =
      appAll || prefixes.exists(p => className == p || className.startsWith(p + "."))

  def isEmpty: Boolean = !appAll && prefixes.isEmpty

/** The synthetic line tables R8 compaction leaves behind (atom#328), counted per method by the
  * oracle's rule over the (address, line) pairs.
  */
private[callsites] final case class LineTables(synthetic: Int, withPositions: Int):
  def total: Int = synthetic + withPositions

  /** Whether the majority of the methods with positions carry a synthetic table. */
  def syntheticQuality: Boolean = synthetic > 0.5 * math.max(1, total)

private[callsites] object LineTables:
  val empty: LineTables = LineTables(0, 0)

  def sum(a: LineTables, b: LineTables): LineTables =
      LineTables(a.synthetic + b.synthetic, a.withPositions + b.withPositions)

/** What the input file says about itself, read once per slice run from the path the atom's metadata
  * carries. Everything here comes from the input itself, never from the graph: the application
  * boundary (atom#326), the `SourceFile` attribute of every class for the real file names
  * (atom#327), the synthetic line tables (atom#328), the Maven metadata of the bundled artifacts
  * (atom#329) and the method handles a JVM `invokedynamic` bootstraps (atom#327).
  *
  * An APK is a zip of `classes*.dex` files: the manifest is binary XML, decoded with the AXML
  * reader soot already pulls in (`org.soot-oss:axml`), and the dex entries are read with the smali
  * dexlib2 library soot itself uses. A jar or a class directory holds JVM class files, read with
  * ASM (a chen dependency). A file that is none of these yields an empty index, and the slice has
  * no boundary to derive.
  */
object JvmBytecodeInput:
  private[callsites] val log = org.slf4j.LoggerFactory.getLogger(getClass)

  def apply(inputPath: String): JvmBytecodeInput =
    val path  = java.nio.file.Paths.get(inputPath).toAbsolutePath.normalize()
    val file  = path.toFile
    val name  = path.toString.toLowerCase
    val isZip = name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".war")
    if file.isDirectory then new DirInput(path)
    else if !file.isFile then new MissingInput(path)
    else if name.endsWith(".apk") then new ApkInput(path, nested = false)
    else if name.endsWith(".apks") || name.endsWith(".apkm") || name.endsWith(".xapk") then
      new ApkInput(path, nested = true)
    else if name.endsWith(".dex") then new DexInput(path)
    else if isZip then new JarInput(path)
    else new MissingInput(path)

  /** The packages the binary manifest names: the `package` attribute plus, for each component whose
    * name shares the package's first two segments, its own package (three segments kept), as the
    * oracle reads them off `aapt2`. Relative component names (`.Main`) name the package itself.
    */
  private[callsites] def deriveApkPrefixes(pkg: String, components: Seq[String]): List[String] =
    val prefixes = mutable.LinkedHashSet.empty[String]
    if pkg.nonEmpty then prefixes.addOne(pkg)
    val first2 = pkg.split('.').take(2).mkString(".")
    components.foreach { c =>
        if c.contains('.') && !c.startsWith(".") &&
          c.split('.').take(2).mkString(".") == first2 && !c.startsWith(pkg + ".")
        then
          val segs = c.split('.').dropRight(1)
          prefixes.addOne(segs.take(3).mkString("."))
    }
    prefixes.filter(_.nonEmpty).toList.sorted

  /** The package of a class name (`com.foo.Bar$Baz` -> `com.foo`). */
  private[callsites] def packageOf(className: String): String =
      className.lastIndexOf('.') match
        case -1 => className
        case i  => className.substring(0, i)

  /** The oracle's file name for a class: `<package path>/<SourceFile attribute>` under the package
    * path, or, when R8 rewrote the attribute to the literal `SourceFile` or dropped it, the outer
    * class with a `.java` guess. A default-package class gets no prefix, so no slash.
    */
  private[callsites] def classInfoOf(name: String, sourceFile: Option[String]): JvmClassInfo =
    val pkgPath =
        name.lastIndexOf('.') match
          case -1 => ""
          case i  => name.substring(0, i).replace('.', '/')
    def underPath(file: String): String =
        if pkgPath.isEmpty then file else s"$pkgPath/$file"
    sourceFile match
      case Some(s) if s != "SourceFile" && s != "Unknown" && s.contains('.') =>
          JvmClassInfo(name, Some(underPath(s)), sourceGuessed = false)
      case _ =>
          val outer = name.split('.').last.split('$').head
          JvmClassInfo(name, Some(underPath(s"$outer.java")), sourceGuessed = true)

  private[callsites] def dexClassInfos(dex: com.android.tools.smali.dexlib2.iface.DexFile): List[
    JvmClassInfo
  ] =
      dex.getClasses.asScala.map { defn =>
        val name = dexTypeName(defn.getType)
        classInfoOf(name, Option(defn.getSourceFile))
      }.toList

  private[callsites] def dexTypeName(desc: String): String =
      desc.stripPrefix("L").stripSuffix(";").replace('/', '.')

  /** The oracle's synthetic-table rule over a method's (address, line) pairs. */
  private[callsites] def syntheticTable(pairs: List[(Int, Int)]): Boolean =
    val addrs = pairs.map(_._1)
    val lines = pairs.map(_._2)
    pairs.size >= 3 && lines.headOption.contains(1) && addrs.zipWithIndex.forall {
        case (a, i) => i == 0 || a - addrs(i - 1) == lines(i) - lines(i - 1)
    }

  /** Over the methods of the app classes, as the oracle counts it. */
  private[callsites] def dexLineTables(
    dex: com.android.tools.smali.dexlib2.iface.DexFile,
    isApp: String => Boolean
  ): LineTables =
    var withPositions = 0
    var synthetic     = 0
    dex.getClasses.asScala.foreach { defn =>
        if isApp(dexTypeName(defn.getType)) then
          defn.getMethods.asScala.foreach { m =>
              Option(m.getImplementation).foreach { impl =>
                val pairs = impl.getDebugItems.asScala.collect {
                    case ln: com.android.tools.smali.dexlib2.iface.debug.LineNumber =>
                        (ln.getCodeAddress, ln.getLineNumber)
                }.toList.sortBy(_._1)
                if pairs.size >= 3 then
                  withPositions += 1
                  if syntheticTable(pairs) then synthetic += 1
              }
          }
    }
    LineTables(synthetic, withPositions)
  end dexLineTables

  private[callsites] def dexTypeOfHandle(
    tag: Int,
    owner: String,
    name: String
  ): Option[JvmMethodHandle] =
    import org.objectweb.asm.Opcodes.*
    val ownerCls = dexTypeName(owner)
    tag match
      case H_INVOKESTATIC => Some(JvmMethodHandle(ownerCls, name, "call", "binding"))
      case H_INVOKEVIRTUAL | H_INVOKEINTERFACE | H_INVOKESPECIAL =>
          Some(JvmMethodHandle(ownerCls, name, "call", "type"))
      case H_NEWINVOKESPECIAL => Some(JvmMethodHandle(ownerCls, "<init>", "new", "binding"))
      case H_GETFIELD | H_GETSTATIC | H_PUTFIELD | H_PUTSTATIC =>
          Some(JvmMethodHandle(ownerCls, name, "ref", "binding"))
      case _ => None
end JvmBytecodeInput

/** The per-input reads, each of them lazy so a caller pays for what it uses. */
sealed private[callsites] trait JvmBytecodeInput:
  import JvmBytecodeInput.*

  def path: java.nio.file.Path

  /** What the input names as its own (atom#326): the APK manifest, the jar manifest, or every class
    * of a directory. Empty prefixes when it names nothing (an APK without a manifest, a jar without
    * manifest attributes).
    */
  def namedBoundary: AppBoundary

  /** The jar's package-prefix fallback (the prefixes holding at least 10 % of its classes); empty
    * for every other input.
    */
  def fallbackBoundary: AppBoundary = AppBoundary(Nil, appAll = false, "none")

  /** The classes of the input by name, with their real file names (atom#327). */
  lazy val classes: Map[String, JvmClassInfo] = readClasses

  /** The number of app methods whose line table is R8's synthetic compaction, and the number with a
    * table at all (atom#328), counted over the classes of the given boundary.
    */
  def lineTablesFor(boundary: AppBoundary): LineTables = readLineTables(boundary)

  /** The Maven artifacts the input bundles (atom#329): package prefix to `group:artifact:version`.
    */
  lazy val artifacts: Map[String, (String, String, String)] =
      ArtifactMap.fromMetaEntries(metaEntries, classPackages)

  /** The bundled library classes whose artifact the local Gradle or Maven cache can name: the
    * artifacts the `META-INF` metadata lists, opened in the cache, and every input class found in
    * exactly one of them (atom#329). Classes of the given boundary are the app's and are skipped.
    */
  def cachedClassArtifacts(boundary: AppBoundary): Map[String, (String, String, String)] =
      LocalArtifactClasses.classArtifacts(
        ArtifactMap.coordinatesOf(metaEntries, classPackages),
        classes.keySet.filterNot(boundary.isApp)
      )

  /** The `invokedynamic` sites of every class, for the method-handle records (atom#327). Empty for
    * Dalvik inputs, which the oracle leaves to `invoke-custom`.
    */
  lazy val bootstraps: Map[String, List[JvmIndySite]] = readBootstraps

  /** The packages of every class of the input, which decide whether a `pom.properties` matches its
    * artifact.
    */
  lazy val classPackages: Set[String] = classes.keySet.map(packageOf)

  /** Whether classes of the input sit in `prefix` or under it. */
  def holdsClasses(prefix: String): Boolean =
      classPackages.exists(p => p == prefix || p.startsWith(prefix + "."))

  /** The `META-INF` entries of the input, read for the artifact map (atom#329). */
  protected lazy val metaEntries: List[(String, Array[Byte])] = Nil

  protected def readClasses: Map[String, JvmClassInfo]
  protected def readLineTables(boundary: AppBoundary): LineTables
  protected def readBootstraps: Map[String, List[JvmIndySite]]
end JvmBytecodeInput

/** An APK, or an APK bundle (`.apks`, `.apkm`, `.xapk`) whose base APK holds the manifest. */
private final class ApkInput(val path: java.nio.file.Path, nested: Boolean)
    extends JvmBytecodeInput:
  import JvmBytecodeInput.*

  private val ManifestEntry = "AndroidManifest.xml"
  private val DexEntry      = "classes\\d*\\.dex".r

  private def withZip[A](f: java.util.zip.ZipFile => A): A =
    val zip = new java.util.zip.ZipFile(path.toFile)
    try f(zip)
    finally zip.close()

  /** The APK that carries the code: the file itself, or for a bundle the nested `.apk` entries that
    * hold a dex (the base among them names the manifest package).
    */
  private def apkEntries: List[String] =
      if !nested then List(ManifestEntry)
      else
        withZip { zip =>
            zip.entries().asScala.map(_.getName).filter(n =>
                n.endsWith(".apk") && Option(zip.getEntry(n)).exists { e =>
                    Try {
                        val zis = new java.util.zip.ZipInputStream(zip.getInputStream(e))
                        try
                            Iterator.continually(zis.getNextEntry()).takeWhile(_ != null)
                                .exists(n2 => n2.getName == ManifestEntry)
                        finally zis.close()
                    }.getOrElse(false)
                }
            ).toList.sorted
        }

  private def readEntryBytes(zip: java.util.zip.ZipFile, entry: String): Array[Byte] =
      Option(zip.getEntry(entry)).map(e => zip.getInputStream(e).readAllBytes()).getOrElse(
        Array.emptyByteArray
      )

  override def namedBoundary: AppBoundary =
    val derived = withZip { zip =>
        apkEntries.flatMap { apkEntry =>
            if nested then
              // a bundle: the manifest sits inside the nested apk entry
              List(readNested(zip, apkEntry) { nestedZip => manifestOf(nestedZip) })
            else List(manifestOf(zip))
        }
    }.headOption.map((pkg, comps) =>
        AppBoundary(deriveApkPrefixes(pkg, comps), appAll = false, "apk-manifest")
    ).getOrElse(AppBoundary(Nil, appAll = false, "none"))
    derived

  private def manifestOf(zip: java.util.zip.ZipFile): (String, List[String]) =
      ManifestReader.read(readEntryBytes(zip, ManifestEntry))

  private def readNested[A](zip: java.util.zip.ZipFile, entry: String)(
    f: java.util.zip.ZipFile => A
  ): A =
    val bytes = readEntryBytes(zip, entry)
    val tmp   = java.nio.file.Files.createTempFile("atom-apk-bundle", ".apk")
    try
      java.nio.file.Files.write(tmp, bytes)
      val nestedZip = new java.util.zip.ZipFile(tmp.toFile)
      try f(nestedZip)
      finally nestedZip.close()
    finally java.nio.file.Files.deleteIfExists(tmp)

  private lazy val dexBytes: List[Array[Byte]] =
      withZip { zip =>
          apkEntries.flatMap { apkEntry =>
            def dexesOf(z: java.util.zip.ZipFile): List[Array[Byte]] =
                z.entries().asScala.map(_.getName).filter(n2 => DexEntry.matches(n2)).toList
                    .sorted.map(readEntryBytes(z, _))
            // a bundle's nested apk is extracted once for all of its dex entries
            if nested then readNested(zip, apkEntry)(dexesOf) else dexesOf(zip)
          }
      }

  protected def readClasses: Map[String, JvmClassInfo] =
      dexBytes.flatMap(bytes => DexReader.classInfos(bytes)).toList
          .map(c => c.name -> c).toMap

  protected def readLineTables(boundary: AppBoundary): LineTables =
      dexBytes.foldLeft(LineTables.empty)((acc, b) =>
          LineTables.sum(acc, DexReader.lineTables(b, boundary.isApp))
      )

  protected def readBootstraps: Map[String, List[JvmIndySite]] = Map.empty

  override protected lazy val metaEntries: List[(String, Array[Byte])] =
      withZip { zip =>
          zip.entries().asScala
              .filter(_.getName.startsWith("META-INF/"))
              .map(e => e.getName -> zip.getInputStream(e).readAllBytes())
              .toList
      }
end ApkInput

/** A bare `.dex` file: no manifest, so the boundary is empty unless `--app-prefix` names one. */
private final class DexInput(val path: java.nio.file.Path) extends JvmBytecodeInput:
  import JvmBytecodeInput.*

  private lazy val bytes: Array[Byte] =
      Try(java.nio.file.Files.readAllBytes(path)).getOrElse(Array.emptyByteArray)

  override def namedBoundary: AppBoundary = AppBoundary(Nil, appAll = false, "none")

  protected def readClasses: Map[String, JvmClassInfo] =
      DexReader.classInfos(bytes).map(c => c.name -> c).toMap

  protected def readLineTables(boundary: AppBoundary): LineTables =
      DexReader.lineTables(bytes, boundary.isApp)

  protected def readBootstraps: Map[String, List[JvmIndySite]] = Map.empty

/** A jar (or war, or zip of class files). */
private final class JarInput(val path: java.nio.file.Path) extends JvmBytecodeInput:
  import JvmBytecodeInput.*

  private def withJar[A](f: java.util.jar.JarFile => A): A =
    val jar = new java.util.jar.JarFile(path.toFile)
    try f(jar)
    finally jar.close()

  private lazy val classEntryNames: List[String] =
      withJar(jar =>
          jar.entries().asScala.map(_.getName).filter(n =>
              n.endsWith(".class") && !n.startsWith("META-INF/") &&
                  !n.endsWith("module-info.class")
          ).toList
      )

  /** The packages the jar manifest names: `Main-Class` by its package, `Automatic-Module-Name` and
    * `Bundle-SymbolicName` as they are. A module or bundle name need not be a package
    * (`kotlin.stdlib`), so a name counts only when classes of the jar sit under it.
    */
  override def namedBoundary: AppBoundary =
    val named = withJar { jar =>
        Option(jar.getManifest).toList.flatMap { m =>
          val attrs = m.getMainAttributes
          Option(attrs.getValue("Main-Class")).map(c => packageOf(c.trim.replace('/', '.'))) ++
              List("Automatic-Module-Name", "Bundle-SymbolicName").flatMap(a =>
                  Option(attrs.getValue(a)).map(_.takeWhile(_ != ';').trim)
              )
        }
    }.filter(n => n.nonEmpty && holdsClasses(n)).distinct
    AppBoundary(named, appAll = false, "jar-manifest")

  override def fallbackBoundary: AppBoundary =
      AppBoundary(JarPrefixes.of(classEntryNames), appAll = false, "jar-prefixes")

  private lazy val classBytes: List[(String, Array[Byte])] =
      withJar(jar =>
          classEntryNames.map(n => n -> jar.getInputStream(jar.getEntry(n)).readAllBytes())
      )

  protected def readClasses: Map[String, JvmClassInfo] =
      classBytes.flatMap(entry => ClassFileReader.classInfos(entry._2))
          .map(c => c.name -> c).toMap

  // the oracle counts line-table shapes for Dalvik methods only
  protected def readLineTables(boundary: AppBoundary): LineTables = LineTables.empty

  protected def readBootstraps: Map[String, List[JvmIndySite]] =
      classBytes.flatMap(entry => ClassFileReader.bootstraps(entry._2)).toMap

  override protected lazy val metaEntries: List[(String, Array[Byte])] =
      withJar(jar =>
          jar.entries().asScala
              .filter(_.getName.startsWith("META-INF/"))
              .map(e => e.getName -> jar.getInputStream(e).readAllBytes())
              .toList
      )
end JarInput

/** A directory of class files: every class of it is application code (the oracle's `--app-all`). */
private final class DirInput(val path: java.nio.file.Path) extends JvmBytecodeInput:
  import JvmBytecodeInput.*

  private lazy val classFiles: List[java.nio.file.Path] =
      Try {
          val stream = java.nio.file.Files.walk(path)
          try
              stream.iterator().asScala
                  .filter(p =>
                      p.toString.endsWith(".class") && !p.toString.endsWith(
                        "module-info.class"
                      )
                  )
                  .toList
          finally stream.close()
      }.getOrElse(Nil)

  override def namedBoundary: AppBoundary = AppBoundary(Nil, appAll = true, "class-dir")

  protected def readClasses: Map[String, JvmClassInfo] =
      classFiles.flatMap(ClassFileReader.classInfosOfPath)
          .map(c => c.name -> c).toMap

  // the oracle counts line-table shapes for Dalvik methods only
  protected def readLineTables(boundary: AppBoundary): LineTables = LineTables.empty

  protected def readBootstraps: Map[String, List[JvmIndySite]] =
      classFiles.flatMap(ClassFileReader.bootstrapsOfPath).toMap
end DirInput

/** An input that is neither an APK, a dex, a jar nor a directory: no boundary, no records. */
private final class MissingInput(val path: java.nio.file.Path) extends JvmBytecodeInput:
  protected def readClasses: Map[String, JvmClassInfo]            = Map.empty
  protected def readLineTables(boundary: AppBoundary): LineTables = LineTables.empty
  protected def readBootstraps: Map[String, List[JvmIndySite]]    = Map.empty
  override def namedBoundary: AppBoundary = AppBoundary(Nil, appAll = false, "none")

private[callsites] object JarPrefixes:

  /** The package prefixes (depth 3) that hold at least 10 % of a jar's classes, collapsed to common
    * prefixes, as the oracle derives them from the jar's entry names.
    */
  def of(classEntryNames: List[String]): List[String] =
    // insertion order, so a tie for the most common prefix goes to the first seen, as the oracle's
    // Counter does
    val counts = mutable.LinkedHashMap.empty[String, Int]
    classEntryNames.foreach { n =>
      val pkg =
          if n.contains('/') then
            n.substring(0, n.lastIndexOf('/')).split('/').take(3).mkString(".")
          else ""
      counts.update(pkg, counts.getOrElse(pkg, 0) + 1)
    }
    val kept = counts.filter((p, c) => p.nonEmpty && c >= 0.10 * classEntryNames.size)
        .keys.toList.sorted
    val out = mutable.ListBuffer.empty[String]
    kept.foreach { p => if !out.exists(o => p.startsWith(o + ".")) then out.addOne(p) }
    if out.isEmpty && counts.nonEmpty then
      val top = counts.values.max
      List(counts.find(_._2 == top).get._1)
    else out.toList
end JarPrefixes

/** The Maven artifacts an input bundles (atom#329), from its `META-INF` entries. */
private[callsites] object ArtifactMap:

  /** `META-INF` files named `<group>_<artifact>.version` (`androidx.core_core.version`, whose
    * content is the version) and `pom.properties` files under `META-INF/maven` (any artifact
    * subdirectory). A `pom.properties` claims the package that equals its groupId, or else its
    * artifactId with the hyphens dotted, when the input has classes there; a `.version` file claims
    * its group. A package claimed by two different artifacts (the many `androidx.lifecycle_*`
    * files) is dropped: a purl is only given when exactly one artifact claims it. Shaded and
    * relocated classes belong to the jar that carries the metadata.
    */
  def fromMetaEntries(
    entries: List[(String, Array[Byte])],
    classPackages: Set[String]
  ): Map[String, (String, String, String)] =
      // one artifact may be named twice (a `.version` file and its `pom.properties`); keep the one
      // with a version, and drop the packages two artifacts claim
      claimsOf(entries, classPackages).view.mapValues { all =>
        val artifacts = all.groupBy((g, a, _) => (g, a))
        Option.when(artifacts.size == 1)(all.maxBy(_._3.length))
      }.collect { case (prefix, Some(coords)) => prefix -> coords }.toMap

  /** Every artifact the `META-INF` entries name, once each, with its version when one is given. */
  def coordinatesOf(
    entries: List[(String, Array[Byte])],
    classPackages: Set[String]
  ): List[(String, String, String)] =
      claimsOf(entries, classPackages).values.flatten.toList
          .groupBy((g, a, _) => (g, a)).values.map(_.maxBy(_._3.length)).toList
          .sortBy((g, a, _) => (g, a))

  /** The package prefix each artifact's metadata claims, in entry-name order. */
  private def claimsOf(
    entries: List[(String, Array[Byte])],
    classPackages: Set[String]
  ): mutable.LinkedHashMap[String, List[(String, String, String)]] =
    val claims = mutable.LinkedHashMap.empty[String, List[(String, String, String)]]
    def claim(prefix: String, coords: (String, String, String)): Unit =
        claims.update(prefix, claims.getOrElse(prefix, Nil) :+ coords)
    def hasClasses(prefix: String): Boolean =
        classPackages.exists(p => p == prefix || p.startsWith(prefix + "."))
    entries.sortBy(_._1).foreach { (name, bytes) =>
      val base = name.split('/').last
      if base.endsWith(".version") && base.contains("_") && name.count(_ == '/') == 1 then
        base.stripSuffix(".version").split("_", 2) match
          case Array(group, artifact) if group.contains('.') =>
              val version = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim
              claim(group, (group, artifact, if version.contains('\n') then "" else version))
          case _ => ()
      else if name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties") then
        parseProperties(bytes).foreach { (group, artifact, version) =>
            List(group, artifact.replace('-', '.')).find(hasClasses).foreach(prefix =>
                claim(prefix, (group, artifact, version))
            )
        }
    }
    claims
  end claimsOf

  private def parseProperties(bytes: Array[Byte]): Option[(String, String, String)] =
      Try {
          val props = new java.util.Properties()
          props.load(new java.io.ByteArrayInputStream(bytes))
          val group    = props.getProperty("groupId", "")
          val artifact = props.getProperty("artifactId", "")
          val version  = props.getProperty("version", "")
          Option.when(group.nonEmpty && artifact.nonEmpty)((group, artifact, version))
      }.toOption.flatten
end ArtifactMap

/** Reads the binary `AndroidManifest.xml` of an APK with the AXML reader soot pulls in. */
private[callsites] object ManifestReader:

  /** The `android:name` attribute, by the resource id the binary form carries or by its name. */
  private val AndroidNameResourceId = 0x01010003
  private val AndroidNsSuffix       = "apk/res/android"

  /** The manifest `package` and the `android:name` of every element, the values `aapt2 dump
    * xmltree` gives the oracle.
    */
  def read(bytes: Array[Byte]): (String, List[String]) =
      Try {
          val axml = new pxb.android.axml.Axml()
          new pxb.android.axml.AxmlReader(bytes).accept(axml)
          val pkg   = mutable.StringBuilder()
          val comps = mutable.ListBuffer.empty[String]
          def valueOf(a: pxb.android.axml.Axml.Node.Attr): String = a.value match
            case v: pxb.android.axml.ValueWrapper => v.raw
            case s: String                        => s
            case _                                => ""
          def visit(node: pxb.android.axml.Axml.Node): Unit =
            val attrNamed = node.attrs.asScala.find(a =>
                a.resourceId == AndroidNameResourceId ||
                    ((a.ns == null || a.ns.endsWith(AndroidNsSuffix)) && a.name == "name")
            )
            if node.name == "manifest" then
              node.attrs.asScala.find(_.name == "package").foreach(a => pkg.addAll(valueOf(a)))
            // every element's name, as the oracle reads `xmltree`: components, permissions,
            // intent actions and meta-data alike
            attrNamed.foreach(a => comps.addOne(valueOf(a)))
            node.children.asScala.foreach(visit)
          axml.firsts.asScala.foreach(visit)
          (pkg.toString, comps.toList)
      }.getOrElse(("", Nil))
end ManifestReader

/** Reads JVM class files with ASM: the `SourceFile` attribute, the `LineNumberTable` shapes and the
  * method handles of every `invokedynamic` (atom#327, atom#328).
  */
private[callsites] object ClassFileReader:

  def classInfosOfPath(file: java.nio.file.Path): List[JvmClassInfo] =
      withBytes(file, Nil)(bytes => classInfos(bytes))

  def bootstrapsOfPath(file: java.nio.file.Path): Map[String, List[JvmIndySite]] =
      withBytes(file, Map.empty)(bytes => bootstraps(bytes))

  private def withBytes[A](file: java.nio.file.Path, orElse: A)(f: Array[Byte] => A): A =
      Try(java.nio.file.Files.readAllBytes(file)).map(bytes => readOr(orElse)(f(bytes)))
          .getOrElse(orElse)

  /** `read`, or `orElse` when the class file is truncated or malformed: one unreadable class costs
    * its own entries, never the slice.
    */
  private def readOr[A](orElse: A)(read: => A): A =
      try read
      catch
        case NonFatal(e) =>
            JvmBytecodeInput.log.debug(s"callsites: could not read a class file: $e")
            orElse

  private def classReader(bytes: Array[Byte]): org.objectweb.asm.ClassReader =
      new org.objectweb.asm.ClassReader(bytes)

  /** The class's name and `SourceFile` attribute (`SKIP_CODE`: one pass, no bodies). */
  def classInfos(bytes: Array[Byte]): List[JvmClassInfo] = readOr(Nil) {
      val cn = new SourceReader
      classReader(bytes).accept(cn, org.objectweb.asm.ClassReader.SKIP_CODE)
      List(JvmBytecodeInput.classInfoOf(cn.className, cn.source))
  }

  /** The handles every `invokedynamic` of the class bootstraps, keyed by class, in instruction
    * order. The oracle reads them from the class file's `BootstrapMethods` through `javap`: the
    * handles of the bootstrap's method arguments, never the bootstrap handle itself.
    */
  def bootstraps(bytes: Array[Byte]): Map[String, List[JvmIndySite]] = readOr(Map.empty) {
      val cn = new IndyReader
      classReader(bytes).accept(cn, 0)
      if cn.sites.isEmpty then Map.empty else Map(cn.className.toString -> cn.sites.toList)
  }

  /** Collects the class's name and `SourceFile` attribute in one pass over the header. */
  private class SourceReader extends org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9):
    var className: String      = null
    var source: Option[String] = None
    override def visit(
      version: Int,
      access: Int,
      name: String,
      signature: String,
      superName: String,
      interfaces: Array[String]
    ): Unit = className = JvmBytecodeInput.dexTypeName(name)
    override def visitSource(source: String, debug: String): Unit =
        this.source = Option(source)

  /** A JVM method descriptor in the graph's signature form: `(Ljava/util/List;)I` is
    * `int(java.util.List)`.
    */
  private[callsites] def graphSignature(desc: String): String =
    val t = org.objectweb.asm.Type.getMethodType(desc)
    s"${t.getReturnType.getClassName}(${t.getArgumentTypes.map(_.getClassName).mkString(",")})"

  /** Collects the handles of every `invokedynamic` in one pass over the bodies. */
  private class IndyReader extends org.objectweb.asm.ClassVisitor(org.objectweb.asm.Opcodes.ASM9):
    val className = mutable.StringBuilder()
    val sites     = mutable.ListBuffer.empty[JvmIndySite]
    override def visit(
      version: Int,
      access: Int,
      name: String,
      signature: String,
      superName: String,
      interfaces: Array[String]
    ): Unit = className.addAll(JvmBytecodeInput.dexTypeName(name))
    override def visitMethod(
      access: Int,
      name: String,
      desc: String,
      signature: String,
      exceptions: Array[String]
    ): org.objectweb.asm.MethodVisitor =
      val methodSignature = graphSignature(desc)
      new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM9):
        // the line of the instructions that follow, from the line table
        private var line                                                           = -1
        override def visitLineNumber(l: Int, start: org.objectweb.asm.Label): Unit = line = l
        override def visitInvokeDynamicInsn(
          indyName: String,
          descriptor: String,
          bootstrapMethodHandle: org.objectweb.asm.Handle,
          bootstrapMethodArguments: AnyRef*
        ): Unit =
          val handles = bootstrapMethodArguments.collect {
              case h: org.objectweb.asm.Handle =>
                  JvmBytecodeInput.dexTypeOfHandle(h.getTag, h.getOwner, h.getName)
          }.flatten.toList
          if handles.nonEmpty then
            sites.addOne(JvmIndySite(name, methodSignature, line, indyName, handles))
    end visitMethod
  end IndyReader
end ClassFileReader

/** Reads a dex with the smali dexlib2 library soot itself uses. */
private[callsites] object DexReader:

  def classInfos(bytes: Array[Byte]): List[JvmClassInfo] =
      withDex(bytes, Nil)(JvmBytecodeInput.dexClassInfos)

  def lineTables(bytes: Array[Byte], isApp: String => Boolean): LineTables =
      withDex(bytes, LineTables.empty)(dex => JvmBytecodeInput.dexLineTables(dex, isApp))

  /** `f` over the dex, or `orElse` when the bytes are no readable dex. */
  private def withDex[A](bytes: Array[Byte], orElse: A)(
    f: com.android.tools.smali.dexlib2.iface.DexFile => A
  ): A =
      try
        val dex = new com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile(
          com.android.tools.smali.dexlib2.Opcodes.getDefault,
          bytes
        )
        f(dex)
      catch
        case NonFatal(e) =>
            JvmBytecodeInput.log.warn(s"callsites: could not read a dex input: $e")
            orElse
end DexReader

/** The class lists of Maven artifacts in the local caches (atom#329): Gradle's
  * (`$GRADLE_USER_HOME/caches/modules-2/files-2.1`, `~/.gradle` by default) and Maven's
  * (`~/.m2/repository`). An APK merges every library into its dex files, so its own metadata names
  * the artifacts but not which classes each one holds; the published `.aar` or `.jar` does. Only
  * artifacts already in a cache are read, nothing is downloaded.
  */
private[callsites] object LocalArtifactClasses:

  private def gradleRoot: Option[java.nio.file.Path] =
      Option(System.getenv("GRADLE_USER_HOME")).filter(_.nonEmpty)
          .orElse(Option(System.getProperty("user.home")).map(h => s"$h/.gradle"))
          .map(r => java.nio.file.Paths.get(r, "caches", "modules-2", "files-2.1"))

  private def mavenRoot: Option[java.nio.file.Path] =
      Option(System.getProperty("maven.repo.local")).filter(_.nonEmpty)
          .orElse(Option(System.getProperty("user.home")).map(h => s"$h/.m2/repository"))
          .map(java.nio.file.Paths.get(_))

  /** The artifact file of `group:artifact:version` in a cache, `.aar` before `.jar`. */
  def locate(
    group: String,
    artifact: String,
    version: String,
    gradle: Option[java.nio.file.Path] = gradleRoot,
    maven: Option[java.nio.file.Path] = mavenRoot
  ): Option[java.nio.file.Path] =
    val names    = List("aar", "jar").map(ext => s"$artifact-$version.$ext")
    val hashDirs = gradleDirs(group, artifact, version, gradle)
    val mavenDir = maven.map(
      _.resolve(group.replace('.', '/')).resolve(artifact).resolve(version)
    ).toList
    names.iterator
        .flatMap(n => (hashDirs ++ mavenDir).map(_.resolve(n)))
        .find(java.nio.file.Files.isRegularFile(_))

  /** The directories Gradle keeps an artifact's files in, one per file hash. */
  private def gradleDirs(
    group: String,
    artifact: String,
    version: String,
    gradle: Option[java.nio.file.Path]
  ): List[java.nio.file.Path] =
      gradle.map(_.resolve(group).resolve(artifact).resolve(version)).toList
          .filter(java.nio.file.Files.isDirectory(_))
          .flatMap(dir =>
              Try {
                  val stream = java.nio.file.Files.list(dir)
                  try stream.iterator().asScala.toList.sortBy(_.toString)
                  finally stream.close()
              }.getOrElse(Nil)
          )

  /** The artifact that holds the Android classes of a Kotlin Multiplatform library, whose root
    * artifact (`androidx.sqlite:sqlite`) is empty: the `available-at` of the Android variant in the
    * root's Gradle `.module` file (`androidx.sqlite:sqlite-android`), else of a JVM variant.
    */
  def androidTarget(
    group: String,
    artifact: String,
    version: String,
    gradle: Option[java.nio.file.Path] = gradleRoot
  ): Option[(String, String, String)] =
    val module = gradleDirs(group, artifact, version, gradle)
        .map(_.resolve(s"$artifact-$version.module"))
        .find(java.nio.file.Files.isRegularFile(_))
    module.flatMap { file =>
      val variants = Try(java.nio.file.Files.readString(file)).toOption
          .flatMap(io.circe.parser.parse(_).toOption)
          .flatMap(_.hcursor.downField("variants").as[List[io.circe.Json]].toOption)
          .getOrElse(Nil)
      def platform(v: io.circe.Json): String =
          v.hcursor.downField("attributes").downField("org.jetbrains.kotlin.platform.type")
              .as[String].getOrElse("")
      def name(v: io.circe.Json): String = v.hcursor.downField("name").as[String].getOrElse("")
      def target(v: io.circe.Json): Option[(String, String, String)] =
        val at = v.hcursor.downField("available-at")
        for
          g <- at.downField("group").as[String].toOption
          m <- at.downField("module").as[String].toOption
          r <- at.downField("version").as[String].toOption
        yield (g, m, r)
      val picks: List[io.circe.Json => Boolean] = List(
        v => platform(v) == "androidJvm",
        v => platform(v) == "jvm" && name(v).startsWith("android"),
        v => platform(v) == "jvm"
      )
      picks.iterator.flatMap(pick => variants.filter(pick).flatMap(target)).nextOption()
    }
  end androidTarget

  /** The class names an `.aar` (its `classes.jar` and the jars under `libs/`) or a `.jar` carries,
    * `$` kept.
    */
  def classesOf(file: java.nio.file.Path): Set[String] =
      Try {
          val zip = new java.util.zip.ZipFile(file.toFile)
          try
              if file.toString.endsWith(".aar") then
                zip.entries().asScala.map(_.getName)
                    .filter(n =>
                        n == "classes.jar" || (n.startsWith("libs/") && n.endsWith(".jar"))
                    )
                    .toList
                    .flatMap(n => classNames(zip.getInputStream(zip.getEntry(n))))
                    .toSet
              else zip.entries().asScala.map(_.getName).filter(isClassEntry).map(classOfEntry).toSet
          finally zip.close()
      }.getOrElse(Set.empty)

  private def classNames(in: java.io.InputStream): List[String] =
    val zis = new java.util.zip.ZipInputStream(in)
    try
        Iterator.continually(zis.getNextEntry).takeWhile(_ != null).map(_.getName)
            .filter(isClassEntry).map(classOfEntry).toList
    finally zis.close()

  private def isClassEntry(name: String): Boolean =
      name.endsWith(".class") && !name.startsWith("META-INF/") &&
          !name.endsWith("module-info.class")

  private def classOfEntry(name: String): String = name.stripSuffix(".class").replace('/', '.')

  /** Each of `wanted` that exactly one of the cached artifacts carries, with that artifact. */
  def classArtifacts(
    coordinates: List[(String, String, String)],
    wanted: Set[String],
    gradle: Option[java.nio.file.Path] = gradleRoot,
    maven: Option[java.nio.file.Path] = mavenRoot
  ): Map[String, (String, String, String)] =
    val holders = mutable.HashMap.empty[String, List[(String, String, String)]]
    def holds(coords: (String, String, String)): Unit =
      val (group, artifact, version) = coords
      locate(group, artifact, version, gradle, maven).foreach { file =>
          classesOf(file).filter(wanted.contains).foreach(cls =>
              holders.update(cls, coords :: holders.getOrElse(cls, Nil))
          )
      }
    val listed            = coordinates.filter(_._3.nonEmpty)
    val platformArtifacts = listed.flatMap((g, a, v) => androidTarget(g, a, v, gradle))
    (listed ++ platformArtifacts).distinct.foreach(holds)
    holders.collect { case (cls, single :: Nil) => cls -> single }.toMap
end LocalArtifactClasses
