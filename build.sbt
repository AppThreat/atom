name                     := "atom"
ThisBuild / organization := "io.appthreat"
ThisBuild / version      := "4.0.0"
ThisBuild / scalaVersion := "3.10.0"

val chenVersion = "4.0.0"

lazy val atom = Projects.atom
resolvers ++= Seq(
  // Google's mirror of Maven Central: resolves the same artifacts without repo1's rate
  // limiting, which has made CI builds fail while the artifact demonstrably existed.
  "gcs-maven-central-mirror".at("https://maven-central.storage-download.googleapis.com/maven2"),
  "Google Maven".at("https://maven.google.com/")
)
libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.github.scopt"     %% "scopt"        % "4.1.0",
  "org.slf4j"             % "slf4j-nop"    % "2.0.18" % Optional,
  ("io.appthreat"        %% "c2cpg"        % Versions.chen).excludeAll(
    ExclusionRule(organization = "com.ibm.icu", name = "icu4j"),
    ExclusionRule(organization = "org.jline", name = "jline"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface.text")
  ),
  "io.appthreat"  %% "edg2atom"          % Versions.chen,
  "io.appthreat"  %% "dataflowengineoss" % Versions.chen,
  "io.appthreat"  %% "pysrc2cpg"         % Versions.chen,
  "io.appthreat"  %% "javasrc2cpg"       % Versions.chen,
  "io.appthreat"  %% "jssrc2cpg"         % Versions.chen,
  "io.appthreat"  %% "jimple2cpg"        % Versions.chen,
  "io.appthreat"  %% "php2atom"          % Versions.chen,
  "io.appthreat"  %% "ruby2atom"         % Versions.chen,
  ("io.appthreat" %% "semanticcpg"       % Versions.chen % Test).classifier("tests"),
  ("io.appthreat" %% "x2cpg"             % Versions.chen % Test).classifier("tests"),
  ("io.appthreat" %% "pysrc2cpg"         % Versions.chen % Test).classifier("tests"),
  // the memory-safety overlay is C/C++ only, so its command needs a C fixture to test against
  ("io.appthreat" %% "c2cpg"     % Versions.chen % Test).classifier("tests"),
  "org.scalatest" %% "scalatest" % "3.2.20"      % Test,
  // validates the SARIF the memory-safety command writes against the official schema
  "com.networknt" % "json-schema-validator" % "1.5.9" % Test
)

excludeDependencies ++= Seq(
  ExclusionRule("dev.scalapy", "scalapy-core"),
  ExclusionRule("org.scala-lang", "scala3-compiler"),
  ExclusionRule("com.google.protobuf", "protobuf-java-util"),
  ExclusionRule("au.com.bytecode", "opencsv")
)

Compile / doc / scalacOptions ++= Seq("-doc-title", "atom apidocs", "-doc-version", version.value)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "--release",
  "23"
)

ThisBuild / compile / javacOptions ++= Seq(
  "-Xlint",
  "--release=23"
) ++ {
    // fail early if users with JDK11 try to run this
    val javaVersion = sys.props("java.specification.version").toFloat
    assert(javaVersion.toInt >= 23, s"this build requires JDK23+ - you're using $javaVersion")
    Nil
}

Universal / topLevelDirectory := None

// These jars can be excluded from the distribution
lazy val excludedNS = Seq(
  // Build-time / REPL dependencies
  "org.scala-lang.scala3-compiler",
  "io.get-coursier",
  "com.michaelpollmeier.scala-repl-pp",
  "dev.scalapy.scalapy-core",
  "dev.scalapy.scalapy-macros",
  // Eclipse platform packages not needed by CDT at runtime
  "org.eclipse.platform.org.eclipse.equinox.app",
  "org.eclipse.platform.org.eclipse.equinox.preferences",
  "org.eclipse.platform.org.eclipse.equinox.registry",
  "org.eclipse.platform.org.eclipse.core.expressions",
  "org.eclipse.platform.org.eclipse.core.contenttype",
  "org.eclipse.platform.org.eclipse.core.filesystem",
  "org.eclipse.platform.org.eclipse.core.resources",
  // JSON/XML libraries not used by atom at runtime.
  "org.json4s",
  "org.glassfish.jaxb",
  "org.reflections.reflections",
  "org.scala-lang.modules.scala-xml"
)

Universal / mappings := (Universal / mappings).value.filterNot {
    case (_, path) =>
        excludedNS.exists(path.contains)
}

enablePlugins(JavaAppPackaging, ClasspathJarPlugin, GraalVMNativeImagePlugin)

// sbt 2 moves `target` to target/out/jvm/scala-<version>/atom; atom.sh, CI and the release
// scripts expect the staged app and the native image where sbt 1 put them
Universal / UniversalPlugin.autoImport.stagingDirectory := baseDirectory.value / "target" / "universal" / "stage"
GraalVMNativeImage / target := baseDirectory.value / "target" / "graalvm-native-image"

ThisBuild / licenses := List("MIT" -> uri("https://opensource.org/license/MIT"))

Global / onChangedBuildSource := ReloadOnSourceChanges

maintainer     := "Team AppThreat <cloud@appthreat.com>"
packageSummary := "Create atom (⚛) representation"
packageDescription := """Create atom (⚛) representation for your application, packages and libraries."""
debianPackageDependencies := Seq("java21-runtime-headless")
rpmVendor                 := "AppThreat"

lazy val createDistribution = taskKey[File]("Create a complete atom distribution")
createDistribution := Def.uncached {
    val distributionFile = file("target/atom.zip")
    val zip              = (atom / (Universal / packageBin)).value
    // sbt 2 packages to virtual-file refs; materialize via the file converter
    IO.copyFile(fileConverter.value.toPath(zip).toFile, distributionFile)
    println(s"created distribution - resulting files: $distributionFile")
    distributionFile
}

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  githubPackagesResolver("appthreat", "chen"),
  "Sonatype OSS".at("https://oss.sonatype.org/content/repositories/public"),
  "Atlassian".at("https://packages.atlassian.com/mvn/maven-atlassian-external"),
  "Gradle Releases".at("https://repo.gradle.org/gradle/libs-releases/")
)

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / Test / fork := true
// sbt 2 runs the suites of a forked test JVM in parallel by default; the chen frontends keep
// JVM-global state (soot's Scene, shared AST caches) that concurrent suites corrupt
ThisBuild / Test / testForkedParallel := false
// executeTests reads it through a dynamic task, which lintUnused cannot see
Global / excludeLintKeys += testForkedParallel
// sbt 2 packs test classes+resources into jars (exportJars := true); keep exploded dirs so
// tests can read their fixtures as plain files
ThisBuild / Test / exportJars := false
// The PHP/Ruby frontends run in-process inside the forked test JVM and resolve their generator
// binary from the environment, so propagate the generator overrides from the launching shell.
ThisBuild / Test / envVars ++=
    Seq("PHP_PARSER_BIN", "RBASTGEN_PATH", "RUBY_ASTGEN_BIN")
        .flatMap(name => sys.env.get(name).map(name -> _))
        .toMap
Global / onChangedBuildSource          := ReloadOnSourceChanges
Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false

wartremoverWarnings ++= Seq(
  Wart.NoNeedImport,
  Wart.ArrayEquals,
  Wart.FinalCaseClass,
  Wart.FinalVal,
  Wart.ToString,
  Wart.TryPartial
)

githubOwner      := "appthreat"
githubRepository := "atom"
credentials +=
    Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      "appthreat",
      sys.env.getOrElse("GITHUB_TOKEN", "N/A")
    )

// GraalVM CE builds are released under version 2 of the GNU General Public License with the "Classpath" Exception (GPLv2+CPE)
val libcOptions =
    if (sys.env.getOrElse("ATOM_GRAALVM_LIBC", "glibc") == "musl") Seq("--libc=musl") else Seq.empty
val niOpt   = sys.env.getOrElse("ATOM_NI_OPT", "-O2")
val niMarch = sys.env.getOrElse("ATOM_NI_MARCH", "compatibility")
val niGc    = sys.env.getOrElse("ATOM_NI_GC", "serial")
graalVMNativeImageOptions := Seq(
  "-H:+UnlockExperimentalVMOptions",
  "-R:MaximumHeapSizePercent=90", // Reduce for more predictable and deterministic slicing
  s"--gc=$niGc",
  "-H:+CompactingOldGen", // Mark-and-compact for the old generation to reduce memory fragmentation
  niOpt,
  s"-march=$niMarch",
  "--initialize-at-build-time=io.appthreat.*",
  // CDT's bundle lookup outside OSGi: the framework loads this service while the image is built
  "--initialize-at-build-time=io.appthreat.c2cpg.parser.CdtBundleLocator,io.appthreat.c2cpg.parser.CdtCoreBundle$",
  // Every charset, not only the standard six: sources are decoded in the encoding their file or
  // build declares (Shift_JIS, GBK, windows-1252, ...), which is known only at run time
  "-H:+AddAllCharsets",
  "--no-fallback"
) ++ libcOptions

// Requires Oracle Enterprise License to use G1 and pgo
// graalVMNativeImageOptions := Seq("-H:+UnlockExperimentalVMOptions", "-H:-UseCompressedReferences", "-march=native", "--enable-preview", "--gc=G1", "--initialize-at-build-time=io.appthreat.*", "--no-fallback")
