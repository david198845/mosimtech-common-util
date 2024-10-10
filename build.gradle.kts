plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
}

group = "de.modulix.mosimtech"
version = "2.1.0"

// Versions-Variablen
val kotlinJvmPluginVersion = "2.0.20"
val jacksonDatabindVersion = "2.17.0"
val jacksonDataformatCsvVersion = "2.17.2"
val kotlinStdlibJdk8Version = "2.0.0"
val slf4jApiVersion = "2.0.16"
val mockitoCoreVersion = "5.0.0"
val mockitoJunitJupiterVersion = "5.0.0"
val mockitoKotlinVersion = "5.0.0"
val kotlinReflectVersion = "2.0.20"
val mongoDbBsonVersion = "5.2.0"
val springDataJpaVersion = "3.3.4"
val springDataMongoDbVersion = "4.3.4"
val springDataNeo4jVersion = "7.3.4"
val jakartaPersistenceApiVersion = "3.2.0"
val jakartaValidationApiVersion = "3.1.0"
val hibernateValidatorVersion = "8.0.1.Final"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonDataformatCsvVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinStdlibJdk8Version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.mongodb:bson:$mongoDbBsonVersion")
    implementation("org.springframework.data:spring-data-jpa:$springDataJpaVersion")
    implementation("org.springframework.data:spring-data-mongodb:$springDataMongoDbVersion")
    implementation("org.springframework.data:spring-data-neo4j:$springDataNeo4jVersion")
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceApiVersion")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
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