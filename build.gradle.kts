plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
}

group = "de.modulix.mosimtech"
version = "1.3.1"

// Versions-Variablen
val jacksonDatabindVersion = "2.17.0"
val jacksonDataformatCsvVersion = "2.17.2"
val kotlinStdlibJdk8Version = "2.0.0"
val slf4jApiVersion = "2.0.16"
val mockitoCoreVersion = "5.0.0"
val mockitoJunitJupiterVersion = "5.0.0"
val mockitoKotlinVersion = "5.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonDataformatCsvVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinStdlibJdk8Version")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:$mockitoCoreVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitJupiterVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = group as String
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        maven {
            url = uri("${project.rootDir}/releases")
        }
    }
}