plugins {
  kotlin("jvm")
}

val libs = the<org.gradle.api.artifacts.VersionCatalogsExtension>().named("libs")

kotlin {
  jvmToolchain(17)
}

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(libs.findLibrary("junit").get())
  testImplementation(libs.findLibrary("assertJ").get())
  testRuntimeOnly(libs.findLibrary("junitPlatformLauncher").get())
}

tasks.withType<Test> {
  useJUnitPlatform()
}
