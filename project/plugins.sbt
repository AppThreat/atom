ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.1.1")
addSbtPlugin("com.github.sbt"  % "sbt-native-packager"   % "1.9.16")
addSbtPlugin("io.shiftleft" % "sbt-overflowdb" % "2.29")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.2")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")
