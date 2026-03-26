plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
  alias(libs.plugins.quarkus)
}

dependencies {
  implementation("io.quarkus:quarkus-scheduler")

  testImplementation("io.quarkus:quarkus-junit5")
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
}

