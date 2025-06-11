name                     := "atom"
ThisBuild / organization := "io.appthreat"
ThisBuild / version      := "2.2.5"
ThisBuild / scalaVersion := "3.7.1"

val chenVersion = "2.4.2"

lazy val atom = Projects.atom

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.github.scopt"     %% "scopt"        % "4.1.0",
  "org.slf4j"             % "slf4j-nop"    % "2.0.17" % Optional,
  ("io.appthreat"        %% "c2cpg"        % Versions.chen).excludeAll(
    ExclusionRule(organization = "com.ibm.icu", name = "icu4j"),
    ExclusionRule(organization = "org.jline", name = "jline"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface"),
    ExclusionRule(organization = "org.eclipse.platform", name = "org.eclipse.jface.text")
  ),
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
  "org.scalatest" %% "scalatest"         % "3.2.19"      % Test
)

excludeDependencies ++= Seq(
  ExclusionRule("dev.scalapy", "scalapy-core"),
  ExclusionRule("org.scala-lang", "scala3-compiler"),
  ExclusionRule("com.google.protobuf", "protobuf-java-util"),
  ExclusionRule("com.github.tototoshi", "scala-csv_3"),
  ExclusionRule("au.com.bytecode", "opencsv")
)

Compile / doc / scalacOptions ++= Seq("-doc-title", "atom apidocs", "-doc-version", version.value)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "--release",
  "21"
)

ThisBuild / compile / javacOptions ++= Seq(
  "-Xlint",
  "--release=21"
) ++ {
    // fail early if users with JDK11 try to run this
    val javaVersion = sys.props("java.specification.version").toFloat
    assert(javaVersion.toInt >= 21, s"this build requires JDK21+ - you're using $javaVersion")
    Nil
}

Universal / topLevelDirectory := None

// These jars can be excluded
lazy val excludedNS = Seq(
    "org.scala-lang.scala3-compiler",
    "io.get-coursier",
    "com.michaelpollmeier.scala-repl-pp",
    "dev.scalapy.scalapy-core",
    "dev.scalapy.scalapy-macros"
)

Universal / mappings := (Universal / mappings).value.filterNot {
    case (_, path) =>
        excludedNS.exists(path.contains)
}

enablePlugins(JavaAppPackaging, ClasspathJarPlugin, GraalVMNativeImagePlugin)

ThisBuild / licenses := List("MIT" -> url("https://opensource.org/license/MIT"))

Global / onChangedBuildSource := ReloadOnSourceChanges

maintainer     := "Team AppThreat <cloud@appthreat.com>"
packageSummary := "Create atom (⚛) representation"
packageDescription := """Create atom (⚛) representation for your application, packages and libraries."""
debianPackageDependencies := Seq("java21-runtime-headless")
rpmVendor                 := "AppThreat"

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
  "Sonatype OSS".at("https://oss.sonatype.org/content/repositories/public"),
  "Atlassian".at("https://packages.atlassian.com/mvn/maven-atlassian-external"),
  "Gradle Releases".at("https://repo.gradle.org/gradle/libs-releases/")
)

ThisBuild / versionScheme := Some("semver-spec")

ThisBuild / Test / fork                := true
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

githubOwner                      := "appthreat"
githubRepository                 := "atom"
githubSuppressPublicationWarning := true
credentials +=
    Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      "appthreat",
      sys.env.getOrElse("GITHUB_TOKEN", "N/A")
    )

// Mandrel-based builds are released under version 2 of the GNU General Public License with the “Classpath” Exception - https://github.com/graalvm/mandrel/blob/default/LICENSE
graalVMNativeImageOptions := Seq(
  "-H:+UnlockExperimentalVMOptions",
  "-R:MaximumHeapSizePercent=90", // Reduce for more predictable and deterministic slicing
  "--gc=epsilon",
  "--initialize-at-build-time=io.appthreat.*",
  "--no-fallback"
)

// Requires Oracle Enterprise License to use G1 and pgo
// graalVMNativeImageOptions := Seq("-H:+UnlockExperimentalVMOptions", "-H:-UseCompressedReferences", "-march=native", "--enable-preview", "--gc=G1", "--initialize-at-build-time=io.appthreat.*", "--no-fallback")
