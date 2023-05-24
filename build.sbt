name := "atom"
ThisBuild/organization := "io.appthreat"
ThisBuild/scalaVersion := "2.13.8"

// parsed by project/Versions.scala, updated by updateDependencies.sh
val cpgVersion = "1.3.600"
val joernVersion = "1.1.1730"
val overflowdbVersion = "1.171"

lazy val atom          = Projects.atom

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.2",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.apache.logging.log4j" % "log4j-core"        % "2.19.0" % Optional,
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.19.0" % Optional,
  "io.joern" %% "c2cpg" % Versions.joern,
  "io.joern" %% "x2cpg" % Versions.joern,
  "io.joern" %% "pysrc2cpg" % Versions.joern,
  "io.joern" %% "javasrc2cpg" % Versions.joern,
  "io.joern" %% "jssrc2cpg" % Versions.joern,
  "io.joern" %% "jimple2cpg" % Versions.joern,
  "io.joern" %% "joern-cli" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern,
  "io.joern" %% "semanticcpg" % Versions.joern % Test classifier "tests",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
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

ThisBuild/Compile/scalacOptions ++= Seq(
  "-deprecation",
  "--release",
  "11"
)

enablePlugins(JavaAppPackaging)

ThisBuild/licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

Global/onChangedBuildSource := ReloadOnSourceChanges

maintainer := "prabhu@appthreat.com"

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
ThisBuild / Test / fork := true
Global / onChangedBuildSource := ReloadOnSourceChanges
Compile/doc/sources := Seq.empty
Compile/packageDoc/publishArtifact := false
