plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
  `maven-publish`
}

group = "de.chrgroth.starters"

dependencies {
  implementation(project(":api"))

  implementation(enforcedPlatform(libs.quarkusBom))
  implementation("io.quarkus:quarkus-arc")
  implementation("io.quarkus:quarkus-micrometer")
  implementation("io.quarkus:quarkus-mongodb-panache-kotlin")

  implementation(libs.kotlinLogging)

  testImplementation(libs.mockk)
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
  annotation("io.quarkus.mongodb.panache.kotlin.PanacheMongoCompanionBase")
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      from(components["java"])
    }
  }
}
