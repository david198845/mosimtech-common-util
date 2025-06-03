// Ultra-einfache Version - garantiert funktionierend



plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    alias(libs.plugins.dokka)
    alias(libs.plugins.sonarqube)
}

repositories {
    mavenCentral()
}

sonar {
    properties {
        property("sonar.projectKey", "MMS-COMMON-UTIL")
        property("sonar.projectName", "MoMaSoft-Common-Util")
        property("sonar.token", "sqa_f08873c38b872bd5a87d0de33f7d7ac76f13b985")


    }
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
            jvmTarget = libs.versions.jvmTarget.get()
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
//}
