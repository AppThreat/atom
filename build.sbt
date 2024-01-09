name                     := "atom"
ThisBuild / organization := "io.appthreat"
ThisBuild / version      := "1.8.2"
ThisBuild / scalaVersion := "3.3.1"

val chenVersion      = "1.1.4"

lazy val atom = Projects.atom

val astGenVersion = "3.5.0"

libraryDependencies ++= Seq(
  "com.github.pathikrit"    %% "better-files"      % "3.9.2",
  "com.github.scopt"        %% "scopt"             % "4.1.0",
  "org.apache.logging.log4j" % "log4j-core"        % "2.22.0" % Optional,
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.22.0" % Optional,
  "io.appthreat"                %% "c2cpg"             % Versions.chen excludeAll (
    ExclusionRule(organization = "com.ibm.icu", name = "icu4j"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface.text")
  ),
  "io.appthreat"      %% "dataflowengineoss" % Versions.chen,
  "io.appthreat"      %% "pysrc2cpg"         % Versions.chen,
  "io.appthreat"      %% "javasrc2cpg"       % Versions.chen,
  "io.appthreat"      %% "jssrc2cpg"         % Versions.chen,
  "io.appthreat"      %% "jimple2cpg"        % Versions.chen,
  "io.appthreat"      %% "php2atom"        % Versions.chen,
  "io.appthreat"      %% "semanticcpg"       % Versions.chen % Test classifier "tests",
  "io.appthreat"      %% "x2cpg"             % Versions.chen % Test classifier "tests",
  "io.appthreat"      %% "pysrc2cpg"         % Versions.chen % Test classifier "tests",
  "org.scalatest" %% "scalatest"         % "3.2.17"       % Test
)

Compile / doc / scalacOptions ++= Seq("-doc-title", "atom apidocs", "-doc-version", version.value)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "--release",
  "17",
)

ThisBuild / compile / javacOptions ++= Seq(
  "-g", // debug symbols
  "-Xlint",
  "--release=17"
) ++ {
  // fail early if users with JDK11 try to run this
  val javaVersion = sys.props("java.specification.version").toFloat
  assert(javaVersion.toInt >= 17, s"this build requires JDK17+ - you're using $javaVersion")
  Nil
}

Universal / topLevelDirectory := None

Universal / mappings := (Universal / mappings).value.filter {
  case (_, path) => !path.contains("org.scala-lang.scala3-compiler") && !path.contains("io.get-coursier") && !path.contains("com.michaelpollmeier.scala-repl-pp")
}

enablePlugins(JavaAppPackaging, ClasspathJarPlugin, GraalVMNativeImagePlugin)

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
rpmVendor := "AppThreat"

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
  Resolver.githubPackages("appthreat/chen"),
  "Sonatype OSS" at "https://oss.sonatype.org/content/repositories/public",
  "Atlassian" at "https://packages.atlassian.com/mvn/maven-atlassian-external",
  "Gradle Releases" at "https://repo.gradle.org/gradle/libs-releases/"
)

ThisBuild / assemblyMergeStrategy := {
  case "application.conf"                            => MergeStrategy.concat
  case x => MergeStrategy.preferProject
}

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / Test / fork                := true
Global / onChangedBuildSource          := ReloadOnSourceChanges
Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false

wartremoverWarnings ++= Seq(Wart.NoNeedImport, Wart.ArrayEquals, Wart.Any, Wart.FinalCaseClass, Wart.FinalVal, Wart.ToString, Wart.TryPartial)

githubOwner := "appthreat"
githubRepository := "atom"
githubSuppressPublicationWarning := true
credentials +=
  Credentials(
    "GitHub Package Registry",
    "maven.pkg.github.com",
    "appthreat",
    sys.env.getOrElse("GITHUB_TOKEN", "N/A")
  )
graalVMNativeImageGraalVersion := Some("22")
graalVMNativeImageOptions := Seq("-H:+UnlockExperimentalVMOptions", "--no-fallback")
