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

allprojects {
    group = "de.mosimtech"
    version = project.findProperty("version") ?: "unspecified"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")


}
