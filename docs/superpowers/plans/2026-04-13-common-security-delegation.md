# common-util-security & common-util-delegation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Spring Security aus `common-util-core` extrahieren in ein neues `common-util-security`-Modul und ein UMA-2.0-basiertes Delegationssystem als `common-util-delegation`-Modul hinzufügen.

**Architecture:** Zwei neue Gradle-Sub-Module werden dem Multi-Modul-Projekt hinzugefügt. `common-util-security` hält alle JWT/OAuth2-Infrastruktur inklusive eines Keycloak-bewussten Authority Converters. `common-util-delegation` baut darauf auf und stellt ThreadLocal-Context-Switching, einen Request-Filter für den `X-Target-User-Id`-Header sowie einen UMA-PermissionEvaluator bereit. `common-util-core` verliert alle Spring-Security-Abhängigkeiten.

**Tech Stack:** Kotlin, Spring Security 7 (OAuth2 Resource Server), Spring Web 7, Gradle Kotlin DSL, kotlin.test, Mockito Kotlin 6, spring-boot-test 4

---

## File Map

### Neue Dateien — common-util-security
- `common-util-security/build.gradle.kts`
- `common-util-security/src/main/kotlin/de/mosimtech/common/security/DelegationConstants.kt`
- `common-util-security/src/main/kotlin/de/mosimtech/common/security/converter/KeycloakJwtGrantedAuthoritiesConverter.kt`
- `common-util-security/src/main/kotlin/de/mosimtech/common/security/SecurityContextAdapter.kt`
- `common-util-security/src/main/kotlin/de/mosimtech/common/security/validator/AudienceValidator.kt`
- `common-util-security/src/main/kotlin/de/mosimtech/common/security/validator/AzpValidator.kt`
- `common-util-security/src/test/kotlin/de/mosimtech/common/security/converter/KeycloakJwtGrantedAuthoritiesConverterTest.kt`
- `common-util-security/src/test/kotlin/de/mosimtech/common/security/SecurityContextAdapterTest.kt`
- `common-util-security/src/test/kotlin/de/mosimtech/common/security/validator/AudienceValidatorTest.kt`
- `common-util-security/src/test/kotlin/de/mosimtech/common/security/validator/AzpValidatorTest.kt`

### Neue Dateien — common-util-delegation
- `common-util-delegation/build.gradle.kts`
- `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/DelegationContextHolder.kt`
- `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/DelegationHeaderFilter.kt`
- `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/CurrentUserProvider.kt`
- `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/uma/UmaPermissionEvaluator.kt`
- `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/event/DelegationEvent.kt`
- `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/DelegationContextHolderTest.kt`
- `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/DelegationHeaderFilterTest.kt`
- `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/CurrentUserProviderTest.kt`
- `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/uma/UmaPermissionEvaluatorTest.kt`

### Geänderte Dateien
- `settings.gradle.kts` — zwei neue Module registrieren
- `gradle/libs.versions.toml` — `spring-web` hinzufügen
- `common-util-core/build.gradle.kts` — Spring-Security-Dependencies entfernen
- `gradle.properties` — Version auf `3.0.0` hochsetzen

### Gelöschte Dateien
- `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AudienceValidator.kt`
- `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AzpValidator.kt`
- `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/SecurityContextAdapter.kt`
- `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/RealmSecurityRoles.kt`

---

## Task 1: Neue Module in settings.gradle.kts registrieren + spring-web in Catalog

**Files:**
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: `settings.gradle.kts` um neue Module erweitern**

```kotlin
rootProject.name = "common-util"

include("common-util-jpa")
include("common-util-core")
include("common-util-r2dbc")
include("common-util-mongo")
include("common-util-rabbitmq")
include("common-util-security")
include("common-util-delegation")
```

- [ ] **Step 2: `spring-web` zur `libs.versions.toml` hinzufügen**

In `[libraries]` nach `spring-context` einfügen:

```toml
spring-web = { module = "org.springframework:spring-web", version.ref = "springContext" }
```

- [ ] **Step 3: Committen**

```bash
git add settings.gradle.kts gradle/libs.versions.toml
git commit -m "build: register common-util-security and common-util-delegation modules, add spring-web to catalog"
```

---

## Task 2: `common-util-security` Gradle-Konfiguration

**Files:**
- Create: `common-util-security/build.gradle.kts`

- [ ] **Step 1: `build.gradle.kts` erstellen**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    `maven-publish`
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":common-util-core"))

    api(libs.spring.security.core)
    api(libs.spring.security.oauth2.jose)
    api(libs.spring.security.oauth2.resource.server)

    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.slf4j.api)

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
```

- [ ] **Step 2: Gradle-Sync prüfen**

```bash
cd C:/workspace-privat/mosimtech/Common/mosimtech-common-util
./gradlew :common-util-security:dependencies --configuration compileClasspath
```

Erwartet: Dependency-Tree ohne Fehler, `common-util-core` und Spring Security sichtbar.

- [ ] **Step 3: Committen**

```bash
git add common-util-security/build.gradle.kts
git commit -m "build: add common-util-security Gradle module configuration"
```

---

## Task 3: `DelegationConstants` erstellen

**Files:**
- Create: `common-util-security/src/main/kotlin/de/mosimtech/common/security/DelegationConstants.kt`

- [ ] **Step 1: Datei erstellen**

```kotlin
package de.mosimtech.common.security

const val SYSTEM_INVOKE = "system:invoke"
```

- [ ] **Step 2: Committen**

```bash
git add common-util-security/src/main/kotlin/de/mosimtech/common/security/DelegationConstants.kt
git commit -m "feat(security): add SYSTEM_INVOKE constant"
```

---

## Task 4: `KeycloakJwtGrantedAuthoritiesConverter` — TDD

**Files:**
- Create: `common-util-security/src/test/kotlin/de/mosimtech/common/security/converter/KeycloakJwtGrantedAuthoritiesConverterTest.kt`
- Create: `common-util-security/src/main/kotlin/de/mosimtech/common/security/converter/KeycloakJwtGrantedAuthoritiesConverter.kt`

Hintergrund: Keycloak liefert Realm-Rollen unter `realm_access.roles` und Client-Rollen unter `resource_access.<client>.roles`. Spring Security liest standardmäßig nur `realm_access`. Dieser Converter liest beides und erzeugt Authorities im Format `ROLE_<realm-role>` bzw. `<client>:<role>`.

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.security.converter

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KeycloakJwtGrantedAuthoritiesConverterTest {

    private val converter = KeycloakJwtGrantedAuthoritiesConverter()

    @Test
    fun `should extract realm roles as ROLE_ prefixed authorities`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access"))
            .thenReturn(mapOf("roles" to listOf("finance-admin", "core-user")))
        whenever(jwt.getClaim<Map<String, Any>>("resource_access")).thenReturn(null)

        val authorities = converter.convert(jwt)

        assertTrue(authorities.any { it.authority == "ROLE_finance-admin" })
        assertTrue(authorities.any { it.authority == "ROLE_core-user" })
        assertEquals(2, authorities.size)
    }

    @Test
    fun `should extract client roles as client-colon-role authorities`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf("momasoft-finance-api" to mapOf("roles" to listOf("finance:manage", "finance:read"))))

        val authorities = converter.convert(jwt)

        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:manage" })
        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:read" })
        assertEquals(2, authorities.size)
    }

    @Test
    fun `should combine realm and client roles`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access"))
            .thenReturn(mapOf("roles" to listOf("core-user")))
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf("momasoft-shift-api" to mapOf("roles" to listOf("shift:view"))))

        val authorities = converter.convert(jwt)

        assertEquals(2, authorities.size)
        assertTrue(authorities.any { it.authority == "ROLE_core-user" })
        assertTrue(authorities.any { it.authority == "momasoft-shift-api:shift:view" })
    }

    @Test
    fun `should return empty list when both claims are null`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access")).thenReturn(null)

        val authorities = converter.convert(jwt)

        assertTrue(authorities.isEmpty())
    }

    @Test
    fun `should handle multiple clients in resource_access`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("realm_access")).thenReturn(null)
        whenever(jwt.getClaim<Map<String, Any>>("resource_access"))
            .thenReturn(mapOf(
                "momasoft-finance-api" to mapOf("roles" to listOf("finance:manage")),
                "momasoft-shift-api" to mapOf("roles" to listOf("system:invoke"))
            ))

        val authorities = converter.convert(jwt)

        assertEquals(2, authorities.size)
        assertTrue(authorities.any { it.authority == "momasoft-finance-api:finance:manage" })
        assertTrue(authorities.any { it.authority == "momasoft-shift-api:system:invoke" })
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-security:test --tests "de.mosimtech.common.security.converter.KeycloakJwtGrantedAuthoritiesConverterTest"
```

Erwartet: Compilation error — `KeycloakJwtGrantedAuthoritiesConverter` existiert nicht.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.security.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class KeycloakJwtGrantedAuthoritiesConverter : Converter<Jwt, Collection<GrantedAuthority>> {

    override fun convert(jwt: Jwt): Collection<GrantedAuthority> {
        val authorities = mutableListOf<GrantedAuthority>()

        val realmAccess = jwt.getClaim<Map<String, Any>>("realm_access")
        val realmRoles = realmAccess?.get("roles") as? List<*> ?: emptyList<Any>()
        realmRoles.filterIsInstance<String>().forEach { role ->
            authorities.add(SimpleGrantedAuthority("ROLE_$role"))
        }

        val resourceAccess = jwt.getClaim<Map<String, Any>>("resource_access")
        resourceAccess?.forEach { (client, value) ->
            val clientRoles = (value as? Map<*, *>)?.get("roles") as? List<*> ?: emptyList<Any>()
            clientRoles.filterIsInstance<String>().forEach { role ->
                authorities.add(SimpleGrantedAuthority("$client:$role"))
            }
        }

        return authorities
    }
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-security:test --tests "de.mosimtech.common.security.converter.KeycloakJwtGrantedAuthoritiesConverterTest"
```

Erwartet: 5 Tests PASS.

- [ ] **Step 5: Committen**

```bash
git add common-util-security/src/
git commit -m "feat(security): add KeycloakJwtGrantedAuthoritiesConverter for realm and client roles"
```

---

## Task 5: `SecurityContextAdapter` migrieren und erweitern — TDD

**Files:**
- Create: `common-util-security/src/test/kotlin/de/mosimtech/common/security/SecurityContextAdapterTest.kt`
- Create: `common-util-security/src/main/kotlin/de/mosimtech/common/security/SecurityContextAdapter.kt`

Hinweis: `isAdmin()`, `ADMIN_ROLE_LIST`, `hasRoleOrIsAdmin()` und `hasAnyRoleOrIsAdmin()` werden **nicht** migriert — diese sind projektspezifisch. Neu hinzukommen: `hasClientRole()`, `getClientRoles()`, `isSystemInvocation()`.

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.security

import de.mosimtech.common.security.converter.KeycloakJwtGrantedAuthoritiesConverter
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecurityContextAdapterTest {

    @BeforeTest
    fun setup() {
        SecurityContextAdapter.REALM = "momasoft"
    }

    @AfterTest
    fun cleanup() {
        SecurityContextHolder.clearContext()
    }

    private fun setupAuthentication(
        subject: String = "550e8400-e29b-41d4-a716-446655440000",
        authorities: List<SimpleGrantedAuthority> = emptyList()
    ) {
        val jwt = mock<Jwt>()
        whenever(jwt.subject).thenReturn(subject)
        val auth = JwtAuthenticationToken(jwt, authorities)
        SecurityContextHolder.getContext().authentication = auth
    }

    @Test
    fun `getCurrentUserID returns URN when authenticated`() {
        setupAuthentication()
        val urn = SecurityContextAdapter.getCurrentUserID()
        assertNotNull(urn)
        assertTrue(urn.toUrnString().contains("550e8400-e29b-41d4-a716-446655440000"))
    }

    @Test
    fun `getCurrentUserID returns null when not authenticated`() {
        assertNull(SecurityContextAdapter.getCurrentUserID())
    }

    @Test
    fun `hasRole returns true when authority matches`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_finance-admin")))
        assertTrue(SecurityContextAdapter.hasRole("ROLE_finance-admin"))
    }

    @Test
    fun `hasRole returns false when authority does not match`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_core-user")))
        assertFalse(SecurityContextAdapter.hasRole("ROLE_finance-admin"))
    }

    @Test
    fun `hasClientRole returns true when client-colon-role authority present`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-finance-api:finance:manage")))
        assertTrue(SecurityContextAdapter.hasClientRole("momasoft-finance-api", "finance:manage"))
    }

    @Test
    fun `hasClientRole returns false when authority missing`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-finance-api:finance:read")))
        assertFalse(SecurityContextAdapter.hasClientRole("momasoft-finance-api", "finance:manage"))
    }

    @Test
    fun `getClientRoles returns all roles for a given client`() {
        setupAuthentication(authorities = listOf(
            SimpleGrantedAuthority("momasoft-finance-api:finance:manage"),
            SimpleGrantedAuthority("momasoft-finance-api:finance:read"),
            SimpleGrantedAuthority("momasoft-shift-api:shift:view")
        ))
        val roles = SecurityContextAdapter.getClientRoles("momasoft-finance-api")
        assertEquals(2, roles.size)
        assertTrue(roles.contains("finance:manage"))
        assertTrue(roles.contains("finance:read"))
    }

    @Test
    fun `getClientRoles returns empty list when no roles for client`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("ROLE_core-user")))
        val roles = SecurityContextAdapter.getClientRoles("momasoft-finance-api")
        assertTrue(roles.isEmpty())
    }

    @Test
    fun `isSystemInvocation returns true when system-invoke present for client`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-shift-api:system:invoke")))
        assertTrue(SecurityContextAdapter.isSystemInvocation("momasoft-shift-api"))
    }

    @Test
    fun `isSystemInvocation returns false when system-invoke missing`() {
        setupAuthentication(authorities = listOf(SimpleGrantedAuthority("momasoft-shift-api:shift:view")))
        assertFalse(SecurityContextAdapter.isSystemInvocation("momasoft-shift-api"))
    }

    @Test
    fun `getRoles returns all authority strings`() {
        setupAuthentication(authorities = listOf(
            SimpleGrantedAuthority("ROLE_core-user"),
            SimpleGrantedAuthority("momasoft-finance-api:finance:read")
        ))
        val roles = SecurityContextAdapter.getRoles()
        assertEquals(2, roles.size)
        assertTrue(roles.contains("ROLE_core-user"))
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-security:test --tests "de.mosimtech.common.security.SecurityContextAdapterTest"
```

Erwartet: Compilation error — `SecurityContextAdapter` existiert nicht.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.security

import de.mosimtech.common.core.converter.keycloak.KeycloakUserUrnConverter
import de.mosimtech.common.core.urn.Urn
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

object SecurityContextAdapter {

    lateinit var REALM: String

    private val authentication: Authentication?
        get() = SecurityContextHolder.getContext().authentication

    fun getCurrentUserID(): Urn? = (authentication as? JwtAuthenticationToken)?.token?.subject
        ?.let { KeycloakUserUrnConverter.convertToUrn(it, REALM) }

    fun getCurrentToken() = (authentication as? JwtAuthenticationToken)?.token

    fun getCurrentUserIDAsString(): String? = getCurrentUserID()?.toUrnString()

    fun hasRole(role: String): Boolean =
        authentication?.authorities?.any { it.authority == role } ?: false

    fun hasAnyRole(roles: List<String>): Boolean = roles.any { hasRole(it) }

    fun getRoles(): List<String> =
        authentication?.authorities?.map { it.authority } ?: emptyList()

    fun hasClientRole(client: String, role: String): Boolean =
        hasRole("$client:$role")

    fun getClientRoles(client: String): List<String> =
        authentication?.authorities
            ?.map { it.authority }
            ?.filter { it.startsWith("$client:") }
            ?.map { it.removePrefix("$client:") }
            ?: emptyList()

    fun isSystemInvocation(client: String): Boolean =
        hasClientRole(client, SYSTEM_INVOKE)
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-security:test --tests "de.mosimtech.common.security.SecurityContextAdapterTest"
```

Erwartet: 11 Tests PASS.

- [ ] **Step 5: Committen**

```bash
git add common-util-security/src/
git commit -m "feat(security): add SecurityContextAdapter with client role support"
```

---

## Task 6: `AudienceValidator` und `AzpValidator` migrieren — TDD

**Files:**
- Create: `common-util-security/src/main/kotlin/de/mosimtech/common/security/validator/AudienceValidator.kt`
- Create: `common-util-security/src/main/kotlin/de/mosimtech/common/security/validator/AzpValidator.kt`
- Create: `common-util-security/src/test/kotlin/de/mosimtech/common/security/validator/AudienceValidatorTest.kt`
- Create: `common-util-security/src/test/kotlin/de/mosimtech/common/security/validator/AzpValidatorTest.kt`

- [ ] **Step 1: `AudienceValidatorTest` schreiben**

```kotlin
package de.mosimtech.common.security.validator

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertTrue

class AudienceValidatorTest {

    @Test
    fun `validate returns success when required audience present`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("momasoft-shift-api", "account"))

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors().not())
    }

    @Test
    fun `validate returns failure when required audience missing`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(listOf("other-service"))

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
    }

    @Test
    fun `validate returns failure when audience is null`() {
        val validator = AudienceValidator("momasoft-shift-api")
        val jwt = mock<Jwt>()
        whenever(jwt.audience).thenReturn(null)

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
    }
}
```

- [ ] **Step 2: `AzpValidatorTest` schreiben**

```kotlin
package de.mosimtech.common.security.validator

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import kotlin.test.Test
import kotlin.test.assertTrue

class AzpValidatorTest {

    @Test
    fun `validate returns success when azp is in whitelist`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui", "momasoft-admin-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn("momasoft-shiftcalc-ui")

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors().not())
    }

    @Test
    fun `validate returns failure when azp is not in whitelist`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn("unknown-client")

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
    }

    @Test
    fun `validate returns failure when azp is null`() {
        val validator = AzpValidator(listOf("momasoft-shiftcalc-ui"))
        val jwt = mock<Jwt>()
        whenever(jwt.getClaimAsString("azp")).thenReturn(null)

        val result = validator.validate(jwt)

        assertTrue(result.hasErrors())
    }
}
```

- [ ] **Step 3: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-security:test --tests "de.mosimtech.common.security.validator.*"
```

Erwartet: Compilation error — Klassen existieren nicht.

- [ ] **Step 4: `AudienceValidator` schreiben**

```kotlin
package de.mosimtech.common.security.validator

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(private val requiredAudience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        if (audiences != null && audiences.contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The required audience '$requiredAudience' is missing", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}
```

- [ ] **Step 5: `AzpValidator` schreiben**

```kotlin
package de.mosimtech.common.security.validator

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AzpValidator(private val allowedAzp: List<String>) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val azp = jwt.getClaimAsString("azp")
        if (azp != null && allowedAzp.contains(azp)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The calling client (azp) '$azp' is not whitelisted", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}
```

- [ ] **Step 6: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-security:test
```

Erwartet: Alle Tests PASS, kein Fehler.

- [ ] **Step 7: Committen**

```bash
git add common-util-security/src/
git commit -m "feat(security): migrate AudienceValidator and AzpValidator from core"
```

---

## Task 7: `common-util-core` bereinigen — Spring Security entfernen

**Files:**
- Delete: `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AudienceValidator.kt`
- Delete: `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AzpValidator.kt`
- Delete: `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/SecurityContextAdapter.kt`
- Delete: `common-util-core/src/main/kotlin/de/mosimtech/common/core/security/RealmSecurityRoles.kt`
- Modify: `common-util-core/build.gradle.kts`

- [ ] **Step 1: Security-Klassen aus core löschen**

```bash
rm "C:/workspace-privat/mosimtech/Common/mosimtech-common-util/common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AudienceValidator.kt"
rm "C:/workspace-privat/mosimtech/Common/mosimtech-common-util/common-util-core/src/main/kotlin/de/mosimtech/common/core/security/AzpValidator.kt"
rm "C:/workspace-privat/mosimtech/Common/mosimtech-common-util/common-util-core/src/main/kotlin/de/mosimtech/common/core/security/SecurityContextAdapter.kt"
rm "C:/workspace-privat/mosimtech/Common/mosimtech-common-util/common-util-core/src/main/kotlin/de/mosimtech/common/core/security/RealmSecurityRoles.kt"
```

- [ ] **Step 2: Spring Security aus `common-util-core/build.gradle.kts` entfernen**

Die drei folgenden Zeilen aus dem `dependencies`-Block entfernen:

```kotlin
// Diese Zeilen entfernen:
api(libs.spring.security.core)
api(libs.spring.security.oauth2.jose)
api(libs.spring.security.oauth2.resource.server)
```

- [ ] **Step 3: `common-util-core` bauen — muss erfolgreich sein**

```bash
./gradlew :common-util-core:build
```

Erwartet: BUILD SUCCESSFUL — keine Abhängigkeit auf Spring Security mehr.

- [ ] **Step 4: Alle Module bauen**

```bash
./gradlew build
```

Erwartet: BUILD SUCCESSFUL für alle Module.

- [ ] **Step 5: Committen**

```bash
git add common-util-core/
git commit -m "refactor(core): remove Spring Security dependencies and classes — moved to common-util-security"
```

---

## Task 8: `common-util-delegation` Gradle-Konfiguration

**Files:**
- Create: `common-util-delegation/build.gradle.kts`

- [ ] **Step 1: `build.gradle.kts` erstellen**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    `maven-publish`
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":common-util-security"))

    api(libs.spring.web)
    api(libs.spring.security.core)

    api(libs.kotlin.stdlib)
    api(libs.kotlin.reflect)
    api(libs.slf4j.api)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.spring.boot.test)
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
```

- [ ] **Step 2: Gradle-Sync prüfen**

```bash
./gradlew :common-util-delegation:dependencies --configuration compileClasspath
```

Erwartet: `common-util-security` und `spring-web` sichtbar.

- [ ] **Step 3: Committen**

```bash
git add common-util-delegation/build.gradle.kts
git commit -m "build: add common-util-delegation Gradle module configuration"
```

---

## Task 9: `DelegationContextHolder` — TDD

**Files:**
- Create: `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/DelegationContextHolderTest.kt`
- Create: `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/DelegationContextHolder.kt`

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.delegation

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DelegationContextHolderTest {

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
    }

    @Test
    fun `get returns null when nothing set`() {
        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `get returns value after set`() {
        DelegationContextHolder.set("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000")
        assertEquals("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000", DelegationContextHolder.get())
    }

    @Test
    fun `clear removes the stored value`() {
        DelegationContextHolder.set("urn:user:momasoft:some-uuid")
        DelegationContextHolder.clear()
        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `is isolated per thread`() {
        DelegationContextHolder.set("urn:user:momasoft:thread-main")
        var threadValue: String? = "not-cleared"
        val thread = Thread { threadValue = DelegationContextHolder.get() }
        thread.start()
        thread.join()
        assertNull(threadValue)
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.DelegationContextHolderTest"
```

Erwartet: Compilation error.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.delegation

object DelegationContextHolder {
    private val targetUserId = ThreadLocal<String?>()

    fun set(userId: String) = targetUserId.set(userId)
    fun get(): String? = targetUserId.get()
    fun clear() = targetUserId.remove()
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.DelegationContextHolderTest"
```

Erwartet: 4 Tests PASS.

- [ ] **Step 5: Committen**

```bash
git add common-util-delegation/src/
git commit -m "feat(delegation): add DelegationContextHolder (ThreadLocal)"
```

---

## Task 10: `DelegationHeaderFilter` — TDD

**Files:**
- Create: `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/DelegationHeaderFilterTest.kt`
- Create: `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/DelegationHeaderFilter.kt`

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.delegation

import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DelegationHeaderFilterTest {

    private val filter = DelegationHeaderFilter()
    private val response = MockHttpServletResponse()

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
    }

    @Test
    fun `should set target user id in context during filter execution`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000")

        var capturedId: String? = null
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertEquals("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000", capturedId)
    }

    @Test
    fun `should clear context after filter execution`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:some-uuid")

        filter.doFilter(request, response, MockFilterChain())

        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `should not set context when header absent`() {
        val request = MockHttpServletRequest()

        var capturedId: String? = "sentinel"
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertNull(capturedId)
    }

    @Test
    fun `should not set context when header is blank`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "   ")

        var capturedId: String? = "sentinel"
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertNull(capturedId)
    }

    @Test
    fun `should clear context even if chain throws exception`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:some-uuid")

        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { _, _, _ -> throw RuntimeException("chain error") }
        )

        try {
            filter.doFilter(request, response, chain)
        } catch (_: RuntimeException) {}

        assertNull(DelegationContextHolder.get())
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.DelegationHeaderFilterTest"
```

Erwartet: Compilation error.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.delegation

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class DelegationHeaderFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val targetUserId = request.getHeader("X-Target-User-Id")
        if (!targetUserId.isNullOrBlank()) {
            DelegationContextHolder.set(targetUserId)
        }
        try {
            filterChain.doFilter(request, response)
        } finally {
            DelegationContextHolder.clear()
        }
    }
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.DelegationHeaderFilterTest"
```

Erwartet: 5 Tests PASS.

- [ ] **Step 5: Committen**

```bash
git add common-util-delegation/src/
git commit -m "feat(delegation): add DelegationHeaderFilter (X-Target-User-Id)"
```

---

## Task 11: `CurrentUserProvider` — TDD

**Files:**
- Create: `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/CurrentUserProviderTest.kt`
- Create: `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/CurrentUserProvider.kt`

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.delegation

import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.core.namespace.UserNamespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.SecurityContextAdapter
import org.mockito.kotlin.mock
import org.mockito.kotlin.mockStatic
import org.mockito.kotlin.whenever
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CurrentUserProviderTest {

    private val ownUrn: Urn = UrnBuilder()
        .withNamespace(UserNamespace)
        .withSubNamespaceIdentifier("momasoft")
        .withNamespaceSpecificString("own-uuid")
        .build()

    private val grantorUrn: Urn = UrnBuilder()
        .withNamespace(UserNamespace)
        .withSubNamespaceIdentifier("momasoft")
        .withNamespaceSpecificString("grantor-uuid")
        .build()

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
    }

    @Test
    fun `getEffectiveUserId returns own user id when no delegation active`() {
        mockStatic(SecurityContextAdapter::class).use { staticMock ->
            staticMock.`when`<Urn?> { SecurityContextAdapter.getCurrentUserID() }.thenReturn(ownUrn)
            val provider = CurrentUserProvider()

            val result = provider.getEffectiveUserId()

            assertEquals(ownUrn, result)
        }
    }

    @Test
    fun `getEffectiveUserId returns grantor user id when delegation active`() {
        DelegationContextHolder.set(grantorUrn.toUrnString())
        mockStatic(SecurityContextAdapter::class).use { staticMock ->
            staticMock.`when`<Urn?> { SecurityContextAdapter.getCurrentUserID() }.thenReturn(ownUrn)
            val provider = CurrentUserProvider()

            val result = provider.getEffectiveUserId()

            assertEquals(grantorUrn, result)
        }
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.CurrentUserProviderTest"
```

Erwartet: Compilation error.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.delegation

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.SecurityContextAdapter
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {

    fun getEffectiveUserId(): Urn =
        DelegationContextHolder.get()
            ?.let { Urn.parse(it) }
            ?: SecurityContextAdapter.getCurrentUserID()!!
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.CurrentUserProviderTest"
```

Erwartet: 2 Tests PASS.

- [ ] **Step 5: Committen**

```bash
git add common-util-delegation/src/
git commit -m "feat(delegation): add CurrentUserProvider (effective user resolution)"
```

---

## Task 12: `DelegationEvent` DTOs erstellen

**Files:**
- Create: `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/event/DelegationEvent.kt`

- [ ] **Step 1: Alle Event-DTOs in einer Datei erstellen**

```kotlin
package de.mosimtech.common.delegation.event

import java.time.Instant

enum class DelegationEventType {
    DELEGATION_ACCEPTED,
    DELEGATION_REVOKED
}

enum class DelegationModule {
    SHIFT_CALENDAR,
    FINANCE
}

data class DelegationGrantor(
    val userId: String,
    val name: String
)

data class DelegationGrantee(
    val userId: String,
    val email: String
)

data class DelegationEvent(
    val eventId: String,
    val timestamp: Instant,
    val eventType: DelegationEventType,
    val grantor: DelegationGrantor,
    val grantee: DelegationGrantee,
    val module: DelegationModule,
    val scopes: List<String>
)
```

- [ ] **Step 2: Kompilierung prüfen**

```bash
./gradlew :common-util-delegation:compileKotlin
```

Erwartet: BUILD SUCCESSFUL.

- [ ] **Step 3: Committen**

```bash
git add common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/event/
git commit -m "feat(delegation): add DelegationEvent DTOs for RabbitMQ"
```

---

## Task 13: `UmaPermissionEvaluator` — TDD

**Files:**
- Create: `common-util-delegation/src/test/kotlin/de/mosimtech/common/delegation/uma/UmaPermissionEvaluatorTest.kt`
- Create: `common-util-delegation/src/main/kotlin/de/mosimtech/common/delegation/uma/UmaPermissionEvaluator.kt`

Hintergrund: Das RPT-Token (Requesting Party Token) enthält einen `authorization`-Claim mit den erteilten UMA-Permissions. Der Evaluator liest diesen Claim und prüft, ob der angeforderte Scope für die Ressource des Grantors vorhanden ist. Kein HTTP-Call nötig — das RPT wird von Spring Security beim Request bereits validiert.

RPT `authorization`-Claim Struktur:
```json
{
  "authorization": {
    "permissions": [
      { "rsname": "res_shift_user_grantor-uuid", "scopes": ["shift:view"] }
    ]
  }
}
```

- [ ] **Step 1: Failing Tests schreiben**

```kotlin
package de.mosimtech.common.delegation.uma

import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UmaPermissionEvaluatorTest {

    private val evaluator = UmaPermissionEvaluator()

    private fun buildAuth(permissions: List<Map<String, Any>>): JwtAuthenticationToken {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("authorization"))
            .thenReturn(mapOf("permissions" to permissions))
        return JwtAuthenticationToken(jwt, emptyList())
    }

    @Test
    fun `returns true when scope and grantor match`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_grantor-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertTrue(result)
    }

    @Test
    fun `returns false when scope does not match`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_grantor-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:edit")

        assertFalse(result)
    }

    @Test
    fun `returns false when grantor id not in resource name`() {
        val auth = buildAuth(listOf(
            mapOf("rsname" to "res_shift_user_other-uuid", "scopes" to listOf("shift:view"))
        ))

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `returns false when authorization claim missing`() {
        val jwt = mock<Jwt>()
        whenever(jwt.getClaim<Map<String, Any>>("authorization")).thenReturn(null)
        val auth = JwtAuthenticationToken(jwt, emptyList())

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `returns false when authentication is not JwtAuthenticationToken`() {
        val auth = mock<org.springframework.security.core.Authentication>()

        val result = evaluator.hasPermission(auth, "grantor-uuid", "urn:momasoft:shift", "shift:view")

        assertFalse(result)
    }

    @Test
    fun `hasPermission with domain object always returns false`() {
        val auth = mock<org.springframework.security.core.Authentication>()

        val result = evaluator.hasPermission(auth, Any(), "shift:view")

        assertFalse(result)
    }
}
```

- [ ] **Step 2: Tests ausführen — müssen FAIL sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.uma.UmaPermissionEvaluatorTest"
```

Erwartet: Compilation error.

- [ ] **Step 3: Implementierung schreiben**

```kotlin
package de.mosimtech.common.delegation.uma

import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.io.Serializable

class UmaPermissionEvaluator : PermissionEvaluator {

    override fun hasPermission(authentication: Authentication, targetDomainObject: Any?, permission: Any?): Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun hasPermission(
        authentication: Authentication,
        targetId: Serializable?,
        targetType: String?,
        permission: Any?
    ): Boolean {
        val jwt = (authentication as? JwtAuthenticationToken)?.token ?: return false
        val grantorId = targetId?.toString() ?: return false
        val requiredScope = permission?.toString() ?: return false

        val authorization = jwt.getClaim<Map<String, Any>>("authorization") ?: return false
        val permissions = authorization["permissions"] as? List<Map<String, Any>> ?: return false

        return permissions.any { perm ->
            val rsName = perm["rsname"] as? String ?: ""
            val scopes = perm["scopes"] as? List<*> ?: emptyList<Any>()
            rsName.contains(grantorId) && scopes.contains(requiredScope)
        }
    }
}
```

- [ ] **Step 4: Tests ausführen — müssen PASS sein**

```bash
./gradlew :common-util-delegation:test --tests "de.mosimtech.common.delegation.uma.UmaPermissionEvaluatorTest"
```

Erwartet: 6 Tests PASS.

- [ ] **Step 5: Alle Tests des gesamten Projekts ausführen**

```bash
./gradlew test
```

Erwartet: Alle Tests in allen Modulen PASS.

- [ ] **Step 6: Committen**

```bash
git add common-util-delegation/src/
git commit -m "feat(delegation): add UmaPermissionEvaluator for @PreAuthorize hasPermission checks"
```

---

## Task 14: Version auf 3.0.0 hochsetzen

**Files:**
- Modify: `gradle.properties`

- [ ] **Step 1: Version ändern**

```properties
kotlin.code.style=official
org.jetbrains.dokka.experimental.gradle.pluginMode=V2EnabledWithHelpers
ksp.useKSP2=true
group=de.modulix.mosimtech
version=3.0.0
```

- [ ] **Step 2: Finalen Build ausführen**

```bash
./gradlew build
```

Erwartet: BUILD SUCCESSFUL, alle Module kompilieren, alle Tests PASS.

- [ ] **Step 3: Committen**

```bash
git add gradle.properties
git commit -m "chore: bump version to 3.0.0 — breaking: security extracted to common-util-security"
```

---

## Selbst-Review

**Spec-Abdeckung:**
- ✅ `common-util-security` als Sub-Modul → Task 2
- ✅ `KeycloakJwtGrantedAuthoritiesConverter` (realm + client roles) → Task 4
- ✅ `SecurityContextAdapter` erweitert (hasClientRole, getClientRoles, isSystemInvocation) → Task 5
- ✅ `AudienceValidator` + `AzpValidator` migriert → Task 6
- ✅ `DelegationConstants` mit `SYSTEM_INVOKE` → Task 3
- ✅ Spring Security aus `common-util-core` entfernt → Task 7
- ✅ `common-util-delegation` als Sub-Modul → Task 8
- ✅ `DelegationContextHolder` (ThreadLocal) → Task 9
- ✅ `DelegationHeaderFilter` (`X-Target-User-Id`) → Task 10
- ✅ `CurrentUserProvider` (effective user) → Task 11
- ✅ `DelegationEvent` DTOs (RabbitMQ) → Task 12
- ✅ `UmaPermissionEvaluator` (`@PreAuthorize hasPermission`) → Task 13
- ✅ Version 3.0.0 → Task 14
