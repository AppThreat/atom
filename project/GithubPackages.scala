import sbt.*
import sbt.Keys.*

/** Publishes to and resolves from GitHub Packages, replacing the unmaintained
  * `com.codecommit:sbt-github-packages` plugin, which has no sbt 2.x build.
  *
  * Keeps the same `githubOwner` / `githubRepository` settings and the same project settings the
  * plugin applied: the publish target, the owner's package registry as a resolver, and the POM
  * metadata. Credentials for "GitHub Package Registry" / maven.pkg.github.com are still supplied by
  * build.sbt.
  */
object GithubPackages extends AutoPlugin {

    object autoImport {
        val githubOwner      = settingKey[String]("GitHub user or organization owning the repository")
        val githubRepository = settingKey[String]("GitHub repository to publish to")

        /** The package registry of a GitHub owner, or of one of its repositories. */
        def githubPackagesResolver(owner: String, repository: String = "_"): MavenRepository =
            s"GitHub Package Registry ($owner/$repository)"
                .at(s"https://maven.pkg.github.com/$owner/$repository")
    }
    import autoImport.*

    override def requires: Plugins      = plugins.JvmPlugin
    override def trigger: PluginTrigger = allRequirements

    override def projectSettings: Seq[Setting[?]] = Seq(
      publishTo := Some(
        "GitHub Package Registry"
            .at(s"https://maven.pkg.github.com/${githubOwner.value}/${githubRepository.value}")
      ),
      resolvers += githubPackagesResolver(githubOwner.value),
      scmInfo := Some(
        ScmInfo(
          uri(s"https://github.com/${githubOwner.value}/${githubRepository.value}"),
          s"scm:git@github.com:${githubOwner.value}/${githubRepository.value}.git"
        )
      ),
      homepage             := Some(uri(s"https://github.com/${githubOwner.value}/${githubRepository.value}")),
      pomIncludeRepository := (_ => false),
      publishMavenStyle    := true
    )
}
