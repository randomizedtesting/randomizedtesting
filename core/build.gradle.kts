
plugins {
    id("java-library")
}

dependencies {
    api("org.junit.platform:junit-platform-engine:1.7.2")
    api("org.junit.jupiter:junit-jupiter-api:5.7.2")
    implementation("org.junit.jupiter:junit-jupiter-engine:5.7.2")

    // testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.7.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}