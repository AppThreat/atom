import DebianConstants._

name                     := "atom"
ThisBuild / organization := "io.appthreat"
ThisBuild / version      := "1.0.0"
ThisBuild / scalaVersion := "3.3.0"

val joernVersion      = "1.2.26"

lazy val atom = Projects.atom

val astGenVersion = "3.1.0"

libraryDependencies ++= Seq(
  "com.github.pathikrit"    %% "better-files"      % "3.9.2",
  "com.github.scopt"        %% "scopt"             % "4.1.0",
  "org.apache.logging.log4j" % "log4j-core"        % "2.19.0" % Optional,
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.19.0" % Optional,
  "io.joern"                %% "c2cpg"             % Versions.joern excludeAll (
    ExclusionRule(organization = "com.ibm.icu", name = "icu4j"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface.text")
  ),
  "io.joern"      %% "dataflowengineoss" % Versions.joern,
  "io.joern"      %% "pysrc2cpg"         % Versions.joern,
  "io.joern"      %% "javasrc2cpg"       % Versions.joern,
  "io.joern"      %% "jssrc2cpg"         % Versions.joern,
  "io.joern"      %% "jimple2cpg"        % Versions.joern,
  "io.joern"      %% "semanticcpg"       % Versions.joern % Test classifier "tests",
  "io.joern"      %% "x2cpg"             % Versions.joern % Test classifier "tests",
  "io.joern"      %% "pysrc2cpg"         % Versions.joern % Test classifier "tests",
  "org.scalatest" %% "scalatest"         % "3.2.15"       % Test
)

Compile / doc / scalacOptions ++= Seq("-doc-title", "atom apidocs", "-doc-version", version.value)

scalacOptions ++= Seq() ++ (
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((3, _)) => Seq()
    case _ =>
      Seq(
        "-deprecation", // Emit warning and location for usages of deprecated APIs.
        "-encoding",
        "utf-8",         // Specify character encoding used by source files.
        "-explaintypes", // Explain type errors in more detail.
        "-feature",      // Emit warning and location for usages of features that should be imported explicitly.
        "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
        "-language:experimental.macros", // Allow macro definition (besides implementation and application)
        "-language:higherKinds",         // Allow higher-kinded types
        "-language:implicitConversions", // Allow definition of implicit functions called views
        "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
        "-Xcheckinit",                   // Wrap field accessors to throw an exception on uninitialized access.
        // "-Xfatal-warnings",              // Fail the compilation if there are any warnings.
        "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
        "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
        "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
        "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
        "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
        "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
        "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
        "-Xlint:option-implicit",        // Option.apply used implicit view.
        "-Xlint:package-object-classes", // Class or object defined in package object.
        "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
        "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
        "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
        "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
        "-Ywarn-dead-code",              // Warn when dead code is identified.
        "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
        "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
        "-Ywarn-numeric-widen",          // Warn when numerics are widened.
        "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
        "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
        "-Ywarn-unused:locals",          // Warn if a local definition is unused.
        "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
        "-Ywarn-unused:privates"         // Warn if a private member is unused.
      )
  }
)

ThisBuild / compile / javacOptions ++= Seq(
  "-g", // debug symbols
  "-Xlint",
  "--release=11"
) ++ {
  // fail early if users with JDK8 try to run this
  val javaVersion = sys.props("java.specification.version").toFloat
  assert(javaVersion.toInt >= 11, s"this build requires JDK11+ - you're using $javaVersion")
  Nil
}

enablePlugins(JavaAppPackaging, ClasspathJarPlugin, DebianPlugin, RpmPlugin, JDebPackaging)

lazy val AstgenWin      = "astgen-win.exe"
lazy val AstgenLinux    = "astgen-linux"
lazy val AstgenLinuxArm = "astgen-linux-arm"
lazy val AstgenMac      = "astgen-macos"
lazy val AstgenMacArm   = "astgen-macos-arm"

lazy val astGenDlUrl = settingKey[String]("astgen download url")
astGenDlUrl := s"https://github.com/joernio/astgen/releases/download/v${astGenVersion}/"

lazy val astGenBinaryNames = taskKey[Seq[String]]("astgen binary names")
astGenBinaryNames := {
  if (sys.props.get("ALL_PLATFORMS").contains("TRUE")) {
    Seq(AstgenWin, AstgenLinux, AstgenMac, AstgenMacArm)
  } else {
    Environment.operatingSystem match {
      case Environment.OperatingSystemType.Windows =>
        Seq(AstgenWin)
      case Environment.OperatingSystemType.Linux =>
        Environment.architecture match {
          case Environment.ArchitectureType.X86 => Seq(AstgenLinux)
          case Environment.ArchitectureType.ARM => Seq(AstgenLinuxArm)
        }
      case Environment.OperatingSystemType.Mac =>
        Environment.architecture match {
          case Environment.ArchitectureType.X86 => Seq(AstgenMac)
          case Environment.ArchitectureType.ARM => Seq(AstgenMacArm)
        }
      case Environment.OperatingSystemType.Unknown =>
        Seq(AstgenWin, AstgenLinux, AstgenMac, AstgenMacArm)
    }
  }
}

lazy val astGenDlTask = taskKey[Unit](s"Download astgen binaries")
astGenDlTask := {
  val astGenDir = baseDirectory.value / "bin" / "astgen"
  astGenDir.mkdirs()

  astGenBinaryNames.value.foreach { fileName =>
    val dest = astGenDir / fileName
    if (!dest.exists) {
      val url            = s"${astGenDlUrl.value}$fileName"
      val downloadedFile = SimpleCache.downloadMaybe(url)
      IO.copyFile(downloadedFile, dest)
    }
  }

  val distDir = (Universal / stagingDirectory).value / "bin" / "astgen"
  distDir.mkdirs()
  IO.copyDirectory(astGenDir, distDir)

  // permissions are lost during the download; need to set them manually
  astGenDir.listFiles().foreach(_.setExecutable(true, false))
  distDir.listFiles().foreach(_.setExecutable(true, false))
}

lazy val astGenSetAllPlatforms = taskKey[Unit](s"Set ALL_PLATFORMS")
astGenSetAllPlatforms := { System.setProperty("ALL_PLATFORMS", "TRUE") }

stage := Def
  .sequential(astGenSetAllPlatforms, Universal / stage)
  .andFinally(System.setProperty("ALL_PLATFORMS", "FALSE"))
  .value

// Also remove astgen binaries with clean, e.g., to allow for updating them.
// Sadly, we can't define the bin/ folders globally,
// as .value can only be used within a task or setting macro
cleanFiles ++= Seq(
  baseDirectory.value / "bin" / "astgen",
  (Universal / stagingDirectory).value / "bin" / "astgen"
) ++ astGenBinaryNames.value.map(fileName => SimpleCache.encodeFile(s"${astGenDlUrl.value}$fileName"))

ThisBuild / licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

Global / onChangedBuildSource := ReloadOnSourceChanges

maintainer := "Team AppThreat <cloud@appthreat.com>"
packageSummary := "Create atom (⚛) representation"
packageDescription := """Create atom (⚛) representation for your application, packages and libraries."""
debianPackageDependencies := Seq("java17-runtime-headless")
Rpm / packageName  := "appthreat-atom"
Rpm / version := "1.0.0"
rpmVendor := "AppThreat"
rpmLicense := Some("Apache-2.0")
rpmBrpJavaRepackJars := true
rpmRelease := "1"
Debian / name := "appthreat-atom"
Debian / version := "1.0.0"

debianPackageDependencies := Seq("java17-runtime-headless")

lazy val createDistribution = taskKey[File]("Create a complete atom distribution")
createDistribution := {
  val distributionFile = file("target/atom.zip")
  val zip              = (atom / Universal / packageBin).value
  IO.copyFile(zip, distributionFile)
  println(s"created distribution - resulting files: $distributionFile")
  distributionFile
}

ThisBuild / resolvers ++= Seq(
  Resolver.mavenLocal,
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/public",
  "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external",
  "Gradle Releases" at "https://repo.gradle.org/gradle/libs-releases/"
)
ThisBuild / Test / fork                := true
Global / onChangedBuildSource          := ReloadOnSourceChanges
Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false
