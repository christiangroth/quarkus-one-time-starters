plugins {
  id("kotlin-project")
  alias(libs.plugins.allopen)
}

dependencies {
  implementation("io.quarkus:quarkus-scheduler")
}

allOpen {
  annotation("jakarta.enterprise.context.ApplicationScoped")
}

