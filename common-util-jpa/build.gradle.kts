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

    // JPA & Spring Data
    api("org.springframework.boot:spring-boot-starter-data-jpa:3.4.5")
    api("org.springframework.data:spring-data-jpa:3.4.5")
    api("jakarta.persistence:jakarta.persistence-api:3.2.0")
    api("io.hypersistence:hypersistence-utils-hibernate-63:3.9.10")
    api("org.hibernate.validator:hibernate-validator:9.0.0.Final")
    api("org.springframework.data:spring-data-commons:3.4.5")

    // Test Dependencies
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.springframework.boot:spring-boot-test:3.4.5")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
