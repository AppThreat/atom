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

/** One `invokedynamic` instruction of a class, named by its enclosing method and the interface
  * method the instruction calls, so the slice can match a graph call to it.
  */
private[callsites] final case class JvmIndySite(
  methodName: String,
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

  /** The oracle's counts over JVM line tables: a table of at least three entries counts, and one
    * with the compaction shape is synthetic.
    */
  private[callsites] def countSynthetic(
    tables: List[List[(Int, Int)]]
  ): LineTables =
    val counted = tables.map(_.sortBy(_._1)).filter(_.size >= 3)
    LineTables(
      synthetic = counted.count(syntheticTable),
      withPositions = counted.size
    )

  /** One class file's name, `SourceFile` attribute and per-method line tables, read from the bytes.
    * The JVM class format is walked by hand for the two things no visitor callback gives: the
    * `LineNumberTable` program counters and the attribute of every method in one pass.
    */
  private[callsites] final case class JvmClassFile(
    name: String,
    sourceFile: Option[String],
    tables: List[List[(Int, Int)]]
  )

  private[callsites] object JvmClassFile:

    /** One pass over the bytes: the constant pool, the fields (skipped), the methods with the
      * `LineNumberTable` of their `Code` attribute, then the class attributes for `SourceFile`.
      */
    def read(bytes: Array[Byte]): JvmClassFile =
      val in = new Reader(bytes)
      in.u4() // magic
      in.u4() // minor and major version
      val utf8 = in.constantPool()
      in.u2() // access flags
      val thisClass = utf8(in.u2())
      in.u2()              // superclass
      in.skip(in.u2() * 2) // interfaces
      skipMembers(in, utf8)
      val tables = readMembers(in, utf8)
      val source = readClassAttributes(in, utf8)
      JvmClassFile(dexTypeName(thisClass), source, tables)

    private def skipMembers(in: Reader, utf8: Array[String]): Unit =
      val count = in.u2()
      var i     = 0
      while i < count do
        in.u2() // access flags
        in.u2() // name
        in.u2() // descriptor
        skipAttributes(in, utf8)
        i += 1

    private def readMembers(in: Reader, utf8: Array[String]): List[List[(Int, Int)]] =
      val out   = mutable.ListBuffer.empty[List[(Int, Int)]]
      val count = in.u2()
      var i     = 0
      while i < count do
        in.u2() // access flags
        in.u2() // name
        in.u2() // descriptor
        val attrs = in.u2()
        var j     = 0
        while j < attrs do
          val name = utf8(in.u2())
          val len  = in.u4()
          if name == "Code" then
            in.u2()              // max stack
            in.u2()              // max locals
            in.skip(in.u4())     // the code itself
            in.skip(in.u2() * 8) // the exception table
            val subAttrs = in.u2()
            var k        = 0
            while k < subAttrs do
              val subName = utf8(in.u2())
              val subLen  = in.u4()
              if subName == "LineNumberTable" then
                val entries = in.u2()
                out.addOne(List.fill(entries)((in.u2(), in.u2())))
              else in.skip(subLen)
              k += 1
          else in.skip(len)
          j += 1
        end while
        i += 1
      end while
      out.toList
    end readMembers

    private def skipAttributes(in: Reader, utf8: Array[String]): Unit =
      val count = in.u2()
      var i     = 0
      while i < count do
        in.u2() // name
        in.skip(in.u4())
        i += 1

    private def readClassAttributes(in: Reader, utf8: Array[String]): Option[String] =
      val count  = in.u2()
      var source = Option.empty[String]
      var i      = 0
      while i < count do
        val name = utf8(in.u2())
        val len  = in.u4()
        if name == "SourceFile" then source = Some(utf8(in.u2()))
        else in.skip(len)
        i += 1
      source

    /** A forward reader over the class file bytes. */
    private final class Reader(bytes: Array[Byte]):
      import java.nio.charset.StandardCharsets.UTF_8
      private var pos = 0
      def u2(): Int =
        val v = ((bytes(pos) & 0xff) << 8) | (bytes(pos + 1) & 0xff)
        pos += 2
        v
      def u4(): Int =
        val v = ((bytes(pos) & 0xff) << 24) | ((bytes(pos + 1) & 0xff) << 16) |
            ((bytes(pos + 2) & 0xff) << 8) | (bytes(pos + 3) & 0xff)
        pos += 4
        v
      def skip(n: Int): Unit = pos += n

      /** The Utf8 constants, indexed by their constant-pool slot. Long and double constants take
        * two slots.
        */
      def constantPool(): Array[String] =
        val count = u2()
        val utf8s = mutable.ArrayBuffer.fill(count)("")
        var i     = 1
        while i < count do
          bytes(pos) & 0xff match
            case 1 => // Utf8
                pos += 1
                val len = u2()
                utf8s(i) = new String(java.util.Arrays.copyOfRange(bytes, pos, pos + len), UTF_8)
                pos += len
            case 3 | 4                      => pos += 1; skip(4)
            case 5 | 6                      => pos += 1; skip(8); i += 1
            case 7 | 8 | 16 | 19 | 20       => pos += 1; skip(2)
            case 9 | 10 | 11 | 12 | 17 | 18 => pos += 1; skip(4)
            case 15                         => pos += 1; skip(3)
            case _                          => pos += 1; skip(2)
          i += 1
        utf8s.toArray
    end Reader
  end JvmClassFile

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

  /** The input's own boundary, what it names or, failing that, the fallback. */
  def boundary: AppBoundary =
      if !namedBoundary.isEmpty then namedBoundary else fallbackBoundary

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

  /** The `invokedynamic` sites of every class, for the method-handle records (atom#327). Empty for
    * Dalvik inputs, which the oracle leaves to `invoke-custom`.
    */
  lazy val bootstraps: Map[String, List[JvmIndySite]] = readBootstraps

  /** The packages of every class of the input, which decide whether a `pom.properties` matches its
    * artifact.
    */
  lazy val classPackages: Set[String] = classes.keySet.map(packageOf)

  /** The `META-INF` entries of the input, read for the artifact map (atom#329). */
  protected def metaEntries: List[(String, Array[Byte])] = Nil

  protected def readClasses: Map[String, JvmClassInfo]
  protected def readLineTables(boundary: AppBoundary): LineTables
  protected def readBootstraps: Map[String, List[JvmIndySite]]

  /** The real file name of a class (`<package path>/<SourceFile>`): the oracle's derivation from
    * the class's own attribute.
    */
  def sourceFileOf(className: String): Option[String] =
      classes.get(className).map(_.sourceFile.getOrElse(s"$className.class"))
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
            val inZip = if nested then
              readNested(zip, apkEntry)(n =>
                  n.entries().asScala.map(_.getName).filter(n2 => DexEntry.matches(n2)).toList
              )
            else zip.entries().asScala.map(_.getName).filter(n2 => DexEntry.matches(n2)).toList
            inZip.map { dexEntry =>
                if nested then readNested(zip, apkEntry)(n => readEntryBytes(n, dexEntry))
                else readEntryBytes(zip, dexEntry)
            }
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

  override protected def metaEntries: List[(String, Array[Byte])] =
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

  private def classEntryNames: List[String] =
      withJar(jar =>
          jar.entries().asScala.map(_.getName).filter(n =>
              n.endsWith(".class") && !n.startsWith("META-INF/") &&
                  !n.endsWith("module-info.class")
          ).toList
      )

  override def namedBoundary: AppBoundary =
    val named = withJar { jar =>
        Option(jar.getManifest).toList.flatMap { m =>
            List(
              "Main-Class",
              "Automatic-Module-Name",
              "Bundle-SymbolicName"
            ).flatMap(attr => Option(m.getMainAttributes.getValue(attr)))
        }.filter(_.nonEmpty).map(_.takeWhile(_ != ';')).map(_.replace('/', '.')).distinct
    }
    AppBoundary(named, appAll = false, "jar-manifest")

  override def fallbackBoundary: AppBoundary =
      AppBoundary(JarPrefixes.of(classEntryNames), appAll = false, "jar-prefixes")

  private def classBytes: List[(String, Array[Byte])] =
      withJar(jar =>
          classEntryNames.map(n => n -> jar.getInputStream(jar.getEntry(n)).readAllBytes())
      )

  protected def readClasses: Map[String, JvmClassInfo] =
      classBytes.flatMap(entry => ClassFileReader.classInfos(entry._2))
          .map(c => c.name -> c).toMap

  protected def readLineTables(boundary: AppBoundary): LineTables =
      classBytes.foldLeft(LineTables.empty)((acc, entry) =>
          LineTables.sum(acc, ClassFileReader.lineTables(entry._2, boundary.isApp))
      )

  protected def readBootstraps: Map[String, List[JvmIndySite]] =
      classBytes.flatMap(entry => ClassFileReader.bootstraps(entry._2)).toMap

  override protected def metaEntries: List[(String, Array[Byte])] =
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

  private def classFiles: List[java.nio.file.Path] =
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

  protected def readLineTables(boundary: AppBoundary): LineTables =
      classFiles.foldLeft(LineTables.empty)((acc, f) =>
          LineTables.sum(acc, ClassFileReader.lineTablesOfPath(f, boundary.isApp))
      )

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
    val counts = mutable.HashMap.empty[String, Int]
    classEntryNames.foreach { n =>
      val pkg = n.stripSuffix(".class").split('/').take(3).mkString(".")
      counts.update(pkg, counts.getOrElse(pkg, 0) + 1)
    }
    val kept = counts.view.filter((p, c) => p.nonEmpty && c >= 0.10 * classEntryNames.size)
        .keySet.toList.sorted
    val out = mutable.ListBuffer.empty[String]
    kept.foreach { p => if !out.exists(o => p.startsWith(o + ".")) then out.addOne(p) }
    if out.isEmpty && counts.nonEmpty then List(counts.maxBy((_, c) => c)._1)
    else out.toList

/** The Maven artifacts an input bundles (atom#329), from its `META-INF` entries. */
private[callsites] object ArtifactMap:

  /** `META-INF` files named `<group>_<artifact>.version` (`androidx.core_core.version`) and
    * `pom.properties` files under `META-INF/maven` (any artifact subdirectory). A `pom.properties`
    * matches when a class package equals its groupId, or its artifactId with the hyphens dotted; a
    * `.version` file names its artifact in the file name (`group_artifact.version`). Shaded and
    * relocated classes belong to the jar that carries the metadata.
    */
  def fromMetaEntries(
    entries: List[(String, Array[Byte])],
    classPackages: Set[String]
  ): Map[String, (String, String, String)] =
    val table = mutable.HashMap.empty[String, (String, String, String)]
    entries.foreach { (name, bytes) =>
      val base = name.split('/').last
      if base.endsWith(".version") && base.contains("_") && name.count(_ == '/') == 1 then
        base.stripSuffix(".version").split("_", 2) match
          case Array(group, artifact) if group.contains('.') =>
              table.update(group, (group, artifact, ""))
          case _ => ()
      else if name.startsWith("META-INF/maven/") && name.endsWith("/pom.properties") then
        parseProperties(bytes).foreach { (group, artifact, version) =>
          val candidates = List(group, artifact.replace('-', '.'))
          if candidates.exists(c =>
                classPackages.exists(p => p == c || p.startsWith(c + "."))
            )
          then table.update(group, (group, artifact, version))
        }
    }
    table.toMap
  end fromMetaEntries

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

  /** The manifest `package` and the `android:name` of every component element (and of the
    * `application`), the values `aapt2 dump badging` and `xmltree` give the oracle.
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
            node.name match
              case "manifest" =>
                  node.attrs.asScala.find(_.name == "package").foreach(a =>
                      pkg.addAll(valueOf(a))
                  )
              case "application" | "activity" | "activity-alias" | "service" | "receiver" |
                  "provider" =>
                  attrNamed.foreach(a => comps.addOne(valueOf(a)))
              case _ => ()
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
      withBytes(file)(bytes => classInfos(bytes))

  def lineTablesOfPath(file: java.nio.file.Path, isApp: String => Boolean): LineTables =
      withBytes(file)(bytes => lineTables(bytes, isApp))

  def bootstrapsOfPath(file: java.nio.file.Path): Map[String, List[JvmIndySite]] =
      withBytes(file)(bytes => bootstraps(bytes))

  private def withBytes[A](file: java.nio.file.Path)(f: Array[Byte] => A): A =
      f(java.nio.file.Files.readAllBytes(file))

  private def classReader(bytes: Array[Byte]): org.objectweb.asm.ClassReader =
      new org.objectweb.asm.ClassReader(bytes)

  /** The class's name and `SourceFile` attribute (`SKIP_CODE`: one pass, no bodies). */
  def classInfos(bytes: Array[Byte]): List[JvmClassInfo] =
    val cn = new SourceReader
    classReader(bytes).accept(cn, org.objectweb.asm.ClassReader.SKIP_CODE)
    List(JvmBytecodeInput.classInfoOf(cn.className, cn.source))

  /** The oracle's rule over the app class's methods' `LineNumberTable`, read straight from the
    * class file: the start program counters of the table are read raw, which no visitor callback
    * offers (ASM resolves a label's offset only when the reader has passed it, and R8's compacted
    * tables point past the current position).
    */
  def lineTables(bytes: Array[Byte], isApp: String => Boolean): LineTables =
    val parsed = JvmBytecodeInput.JvmClassFile.read(bytes)
    if isApp(parsed.name) then JvmBytecodeInput.countSynthetic(parsed.tables) else LineTables.empty

  /** The handles every `invokedynamic` of the class bootstraps, keyed by class, in instruction
    * order. The oracle reads them from the class file's `BootstrapMethods` through `javap`: the
    * handles of the bootstrap's method arguments, never the bootstrap handle itself.
    */
  def bootstraps(bytes: Array[Byte]): Map[String, List[JvmIndySite]] =
    val cn = new IndyReader
    classReader(bytes).accept(cn, 0)
    if cn.sites.isEmpty then Map.empty else Map(cn.className.toString -> cn.sites.toList)

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
        new org.objectweb.asm.MethodVisitor(org.objectweb.asm.Opcodes.ASM9):
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
              sites.addOne(JvmIndySite(name, indyName, handles))
  end IndyReader
end ClassFileReader

/** Reads a dex with the smali dexlib2 library soot itself uses. */
private[callsites] object DexReader:

  def classInfos(bytes: Array[Byte]): List[JvmClassInfo] =
      withDex(bytes)(JvmBytecodeInput.dexClassInfos)

  def lineTables(bytes: Array[Byte], isApp: String => Boolean): LineTables =
      withDex(bytes)(dex => JvmBytecodeInput.dexLineTables(dex, isApp))

  private def withDex[A](bytes: Array[Byte])(f: com.android.tools.smali.dexlib2.iface.DexFile => A)
    : A =
      try
        val dex = new com.android.tools.smali.dexlib2.dexbacked.DexBackedDexFile(
          com.android.tools.smali.dexlib2.Opcodes.getDefault,
          bytes
        )
        f(dex)
      catch
        case NonFatal(e) =>
            JvmBytecodeInput.log.warn(s"callsites: could not read a dex input: $e")
            null.asInstanceOf[A]
end DexReader
