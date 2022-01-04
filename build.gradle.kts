plugins {
  id("base")

  id("com.diffplug.spotless").version("5.14.2").apply(false)
  id("de.thetaphi.forbiddenapis").version("3.1").apply(false)

  id("com.carrotsearch.gradle.randomizedtesting").version("0.0.5").apply(false)
}

allprojects {
  repositories {
    mavenLocal()
    mavenCentral()
  }
}

with(rootProject) {
  version = "3.0.0-SNAPSHOT"
  description = "RandomizedTesting JUnit5 framework"

  apply(from = "gradle/ide/idea.gradle")
}

subprojects {
  plugins.withType<JavaPlugin> {
    configure<JavaPluginExtension> {
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = sourceCompatibility
    }
  }
}
