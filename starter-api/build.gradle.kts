plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
}

dependencies {
  implementation("io.quarkus:quarkus-scheduler")
  implementation(project(":domain-api"))
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
}
