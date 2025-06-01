plugins {
    kotlin("jvm")
    `maven-publish`
    id("org.jetbrains.dokka") version "2.0.0"
}

group = "de.modulix.mosimtech"

repositories {
    mavenCentral()
}

dependencies {

    // Logging
    api("org.slf4j:slf4j-api:2.0.17")

    // Spring Framework Basics
    api("org.springframework:spring-context:6.2.5")
    api("org.springframework.security:spring-security-core:6.4.5")
    api("org.springframework.security:spring-security-oauth2-jose:6.4.5")
    api("org.springframework.security:spring-security-oauth2-resource-server:6.4.5")
    api("org.springframework.data:spring-data-commons:3.4.5")

    // Jackson für Serialisierung
    api("com.fasterxml.jackson.core:jackson-databind:2.19.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.19.0")

    // Kotlin & Utilities
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
    api("org.jetbrains.kotlin:kotlin-reflect:2.1.20")
    api("org.slf4j:slf4j-api:2.0.17")

    // Jakarta Validation
    api("jakarta.validation:jakarta.validation-api:3.1.1")
    api("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // Money API
    api("org.javamoney:moneta:1.4.5")
    api("org.zalando:jackson-datatype-money:1.3.0")

    // RabbitMQ (ohne Spring Integration)
    api("org.springframework.boot:spring-boot-starter-amqp:3.4.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
