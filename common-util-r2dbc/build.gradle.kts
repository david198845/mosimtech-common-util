plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "de.modulix.mosimtech"


dependencies {
    // Core Module
    api(project(":common-util-core"))

    // R2DBC
    api("org.springframework.boot:spring-boot-starter-data-r2dbc:3.4.5")
    api("org.springframework.data:spring-data-r2dbc:3.4.5")
    api("io.projectreactor:reactor-core:3.7.5")

    // Test Dependencies
    testImplementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    testImplementation("io.projectreactor:reactor-test:3.7.5")
    testImplementation("org.springframework.boot:spring-boot-test:3.4.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
