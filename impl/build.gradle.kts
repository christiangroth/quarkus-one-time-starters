plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
}

dependencies {
  implementation(project(":api"))

  implementation("io.quarkus:quarkus-micrometer")
  implementation("io.quarkus:quarkus-mongodb-panache-kotlin")
  implementation(libs.kotlinLogging)

  testImplementation(libs.mockk)
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
  annotation("io.quarkus.mongodb.panache.kotlin.PanacheMongoCompanionBase")
}
