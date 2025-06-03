plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
    alias(libs.plugins.dokka)
}


repositories {
    mavenCentral()
}

dependencies {

    // Logging
    api(libs.slf4j.api)

    // Spring Framework Basics
    api(libs.spring.context)
    api(libs.spring.security.core)
    api(libs.spring.security.oauth2.jose)
    api(libs.spring.security.oauth2.resource.server)
    api(libs.spring.data.commons)

    // Jackson für Serialisierung
    api(libs.jackson.databind)
    api(libs.jackson.dataformat.csv)

    // Kotlin & Utilities
    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.slf4j.api)
    api(libs.jakarta.annotation.api)
    // Jakarta Validation
    api(libs.jakarta.validation.api)
    api(libs.hibernate.validator)

    // Money API
    api(libs.moneta)
    api(libs.jackson.datatype.money)

    // RabbitMQ (ohne Spring Integration)
    api(libs.spring.boot.starter.amqp)
    testApi(libs.kotlin.test)
    testApi(libs.mockito.core)
    testApi(libs.mockito.kotlin)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

