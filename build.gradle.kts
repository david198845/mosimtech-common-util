plugins {
    kotlin("jvm") version "2.1.10"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.0"
}

group = "de.modulix.mosimtech"
version = "2.4.18"

// Versions-Variablen-
val jacksonDatabindVersion = "2.18.2"
val jacksonDataformatCsvVersion = "2.18.2"
val kotlinStdlibJdk8Version = "2.1.10"
val slf4jApiVersion = "2.0.16"
val mockitoCoreVersion = "5.15.2"
val mockitoJunitJupiterVersion = "5.15.2"
val mockitoKotlinVersion = "5.4.0"
val kotlinReflectVersion = "2.1.10"
val mongoDbBsonVersion = "5.3.1"
val springDataJpaVersion = "3.4.2"
val springDataMongoDbVersion = "4.4.2"
val jakartaPersistenceApiVersion = "3.2.0"
val jakartaValidationApiVersion = "3.1.0"
val hibernateValidatorVersion = "8.0.1.Final"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.annotation:jakarta.annotation-api:3.0.0")
    implementation("org.springframework.security:spring-security-core:6.4.2")
    implementation("org.springframework.security:spring-security-oauth2-jose:6.4.2")
    implementation("org.springframework.security:spring-security-oauth2-resource-server:6.4.2")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("io.hypersistence:hypersistence-utils-hibernate-63:3.9.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:$jacksonDataformatCsvVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinStdlibJdk8Version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinReflectVersion")
    implementation("org.slf4j:slf4j-api:$slf4jApiVersion")
    implementation("org.mongodb:bson:$mongoDbBsonVersion")
    implementation("org.springframework.data:spring-data-jpa:$springDataJpaVersion")
    implementation("org.springframework.data:spring-data-mongodb:$springDataMongoDbVersion")
    implementation("org.javamoney:moneta:1.4.4")
    implementation("org.zalando:jackson-datatype-money:1.3.0")
    implementation("jakarta.persistence:jakarta.persistence-api:$jakartaPersistenceApiVersion")
    implementation("jakarta.validation:jakarta.validation-api:$jakartaValidationApiVersion")
    implementation("org.hibernate.validator:hibernate-validator:$hibernateValidatorVersion")
    testImplementation(kotlin("test"))
    testImplementation("com.h2database:h2:2.3.232")
    testImplementation("org.springframework.data:spring-data-jpa:$springDataJpaVersion")
    testImplementation("org.mockito:mockito-core:$mockitoCoreVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitJupiterVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    implementation(kotlin("reflect"))
}

tasks.test {
    useJUnitPlatform()
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


kotlin {
    jvmToolchain(21)
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
                uri("http://192.168.2.31:9000/repository/maven-local-snapshot/")
            } else {
                uri("http://192.168.2.31:9000/repository/maven-local-release/")
            }
            isAllowInsecureProtocol = true
            credentials {
                username = "admin"
                password = "admin"
            }
        }
    }
}