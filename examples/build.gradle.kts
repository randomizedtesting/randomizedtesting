plugins {
    id("java-library")
}

dependencies {
    testImplementation(project(":core"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}