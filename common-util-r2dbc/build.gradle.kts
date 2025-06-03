plugins {
    kotlin("jvm")
    alias(libs.plugins.dokka)
}


repositories {
    mavenCentral()
}

dependencies {
    // Core Module
    api(project(":common-util-core"))

    // R2DBC
    api(libs.spring.boot.starter.data.r2dbc)
    api(libs.spring.data.r2dbc)
    api(libs.reactor.core)

    // Kotlin & Utilities
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.slf4j.api)
    api(libs.jakarta.annotation.api)

    // Test Dependencies
    testImplementation(libs.r2dbc.h2)
    testImplementation(libs.reactor.test)
    testImplementation(libs.spring.boot.test)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
