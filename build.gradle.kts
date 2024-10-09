plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
}

group = "de.modulix.mosimtech"
version = "2.0.0"

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
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.20")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.mongodb:bson:5.2.0")
    implementation("org.springframework.data:spring-data-jpa:3.3.4")
    implementation("org.springframework.data:spring-data-mongodb:4.3.4")
    implementation("org.springframework.data:spring-data-neo4j:7.3.4")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("jakarta.validation:jakarta.validation-api:3.1.0")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    testImplementation(kotlin("test"))
    testImplementation("org.mockito:mockito-core:$mockitoCoreVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitJupiterVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    implementation(kotlin("reflect"))
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