
plugins {
    id("java-library")
    // id("com.carrotsearch.gradle.randomizedtesting")
}

dependencies {
    implementation("org.junit.platform:junit-platform-engine:1.7.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.7.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}