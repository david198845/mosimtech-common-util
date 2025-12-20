import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
