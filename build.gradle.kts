// Ultra-einfache Version - garantiert funktionierend

plugins {
    kotlin("jvm") version "2.1.20"
    id("maven-publish")
    id("org.jetbrains.dokka") version "2.0.0"
    id("org.sonarqube") version "6.2.0.5505"
}

allprojects {
    group = "de.mosimtech"
    version = "2.6.3"

    repositories {
        mavenCentral()
    }
    sonar {
        properties {
            property("sonar.projectKey", "Common-Util")
            property("sonar.projectName", "Common-Util")
            property("sonar.token", "sqa_f08873c38b872bd5a87d0de33f7d7ac76f13b985")


        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply ( plugin = "org.jetbrains.dokka" )
    
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.21")
        implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.21")
        implementation("org.slf4j:slf4j-api:2.0.17")
        implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")

        testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.21")
        testImplementation("org.mockito:mockito-core:5.18.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    }

    tasks.dokkaJavadoc.configure {
        outputDirectory.set(layout.buildDirectory.dir("dokka"))
    }

    tasks.register<Jar>("dokkaJavadocJar") {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        archiveClassifier.set("javadoc")
        from(tasks.dokkaJavadoc)
    }

    tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }


    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "21"
            freeCompilerArgs = listOf("-Xjsr305=strict")
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
                    uri("https://192.168.2.33:9000/repository/maven-snapshots/")
                } else {
                    uri("https://192.168.2.33:9000/repository/maven-releases/")
                }
                isAllowInsecureProtocol = true
                credentials {
                    username = "admin"
                    password = "admin"
                }
            }
        }
    }
}
