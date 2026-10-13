addSbtPlugin("org.scalameta"   % "sbt-scalafmt"        % "2.6.2")
addSbtPlugin("com.github.sbt"  % "sbt-native-packager" % "1.13.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover"     % "3.6.3")

// sbt-github-packages has no sbt 2.x build; project/GithubPackages.scala covers it instead.
