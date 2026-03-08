plugins {
  alias(libs.plugins.buildTimeTracker)
  alias(libs.plugins.versionCatalogUpdate)

  alias(libs.plugins.detekt)
  alias(libs.plugins.kover)

  alias(libs.plugins.release)
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

detekt {
  config.setFrom("detekt-config.yaml")
  buildUponDefaultConfig = true
}

kover {
  reports {
    verify {
      rule {
        minBound(40)
      }
    }
  }
}

release {
  git {
    requireBranch = "main"
  }
}

// publish wird nach dem Tag-Push automatisch aufgerufen
tasks.named("afterReleaseBuild") {
  dependsOn(subprojects.map { "${it.path}:publish" })
}

subprojects {
  apply(plugin = "maven-publish")

  extensions.configure<PublishingExtension> {
    repositories {
      maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/christiangroth/quarkus-one-time-starters")
        credentials {
          username = System.getenv("GITHUB_ACTOR")
          password = System.getenv("GITHUB_TOKEN")
        }
      }
    }
  }
}
