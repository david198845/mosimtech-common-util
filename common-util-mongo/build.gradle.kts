plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "de.modulix.mosimtech"

repositories {
    mavenCentral()
}

dependencies {
    // Core Module
    api(project(":common-util-core"))


    // MongoDB
    api("org.springframework.boot:spring-boot-starter-data-mongodb:3.4.5")
    api("org.springframework.data:spring-data-mongodb:4.4.5")
    api("org.mongodb:bson:5.4.0")

    // Test Dependencies
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:4.16.2")
    testImplementation("org.springframework.boot:spring-boot-test:3.4.5")
}
tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
