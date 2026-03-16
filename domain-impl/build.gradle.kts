plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
}

dependencies {
  implementation(project(":domain-api"))

  implementation("io.quarkus:quarkus-micrometer")
  implementation(libs.kotlinLogging)

  testImplementation(libs.mockk)
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
}
