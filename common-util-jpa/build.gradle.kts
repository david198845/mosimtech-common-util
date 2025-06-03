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

    // JPA & Spring Data
    api(libs.spring.boot.starter.data.jpa)
    api(libs.spring.data.jpa)
    api(libs.jakarta.persistence.api)
    api(libs.hypersistence.utils)
    api(libs.hibernate.validator.jpa)
    api(libs.spring.data.commons)

    // Kotlin & Utilities
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.slf4j.api)
    api(libs.jakarta.annotation.api)

    // Test Dependencies
    testImplementation(libs.h2.database)
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
