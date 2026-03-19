import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    jvmToolchain(25)
}


tasks.register<Jar>("dokkaJavadocJar") {
    description = "Assembles Kotlin docs with Dokka"
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

tasks.register<Jar>("sourcesJar") {
    description = "Assembles Kotlin sources with Dokka"
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.jvmTarget.get()))
        freeCompilerArgs.set(listOf("-Xjsr305=strict"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["dokkaJavadocJar"])
            artifact(tasks["sourcesJar"])
            groupId = group as String
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            url = if (version.toString().endsWith("SNAPSHOT")) {
                uri("https://dev.momasoft.de/nexus/repository/maven-snapshots/")
            } else {
                uri("https://dev.momasoft.de/nexus/repository/maven-releases/")
            }
            isAllowInsecureProtocol = true
            credentials {
                username = "admin"
                password = "admin"
            }
        }
    }
}
