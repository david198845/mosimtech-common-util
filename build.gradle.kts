plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
}

group = "de.modulix.mosimtech"
version = "1.0.2-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group as String
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            url = uri("${project.rootDir}/releases")
        }
    }
}