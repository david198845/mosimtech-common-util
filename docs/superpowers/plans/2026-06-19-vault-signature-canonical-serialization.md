# Vault-Signatur: Kanonische Serialisierung — Implementierungsplan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Signatur-Bildung/-Prüfung der `SignedMessageEnvelope` unabhängig von DTO-Version und service-spezifischer Jackson-Config machen, damit optionale Feld-Zusätze (Default `null`) die Vault-Signatur nicht mehr brechen.

**Architecture:** Ein neuer, bibliotheks-interner deterministischer ObjectMapper (`CanonicalMessageMapper`) in `common-util-vault` wird ausschließlich von Signer und Verifier für `buildSigningInput` genutzt. Da beide Seiten denselben internen Mapper verwenden, entfällt die Abhängigkeit vom injizierten App-Mapper. `SignedMessageEnvelope<T>`, Listener und Publisher bleiben unverändert.

**Tech Stack:** Kotlin, Jackson 3 (`tools.jackson`), Spring Vault, JUnit 5, AssertJ, Mockito-Kotlin, Gradle.

## Global Constraints

- **Commits nur auf ausdrückliche Anweisung des Nutzers** (globale Regel des Nutzers). Die unten angegebenen Commit-Steps sind vorbereitet; vor jedem `git commit` den Nutzer um Freigabe bitten oder ihn manuell committen lassen.
- Jackson 3 verwendet `tools.jackson.*`; Annotationen liegen weiterhin in `com.fasterxml.jackson.annotation.*`.
- `common-util-vault` hängt bereits via `api(project(":common-util-core"))` an den Urn-Serializern; `Urn` serialisiert über `@JsonSerialize(using = UrnSerializer::class)` (keine Modul-Registrierung nötig).
- Jackson 3 hat java.time-Unterstützung eingebaut (kein separates JavaTime-Modul nötig) und schreibt Zeittypen per Default als ISO-8601.
- Test-Coverage-Mindestgrenze: 80 %.
- Signatur-Input-Format (Trennzeichen-Layout in `buildSigningInput`) bleibt **unverändert** — nur die Mapper-Quelle ändert sich.
- Versions-Bump: `common-util` `gradle.properties` aktuell `3.3.6` → **`3.3.7`**.

---

## File Structure

**`common-util-vault` (Bibliothek):**
- Create: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapper.kt` — deterministischer ObjectMapper.
- Modify: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageSigner.kt` — `objectMapper`-Konstruktorparameter entfernen, intern Canonical Mapper nutzen.
- Modify: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageVerifier.kt` — dito.
- Create: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapperTest.kt`
- Modify: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageSignerTest.kt` — an neuen Konstruktor anpassen.
- Modify: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageVerifierTest.kt` — an neuen Konstruktor anpassen + Regressionstest (Versions-Skew).
- Modify: `gradle.properties` — Versions-Bump.

**Konsumenten:**
- Modify: `mosimtech-notification-service/gradle/libs.versions.toml` — `common-util` + `common-util-core` auf `3.3.7`.
- Modify: `mosimtech-notification-service/src/main/kotlin/de/mosimtech/notificationservice/config/VaultTransitMessagingConfig.kt` — Verifier-Konstruktor anpassen.
- Modify: `Shift-Time-Tracker/ShiftCalc-Backend/gradle/libs.versions.toml` — `common` auf `3.3.7`.
- Modify: `Shift-Time-Tracker/ShiftCalc-Backend/shiftcalc-core/src/main/kotlin/de/mosimtech/momasoft/shiftcalc/core/config/VaultTransitMessagingConfig.kt` — Signer+Verifier-Konstruktor anpassen.

---

## Task 1: CanonicalMessageMapper

**Files:**
- Create: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapper.kt`
- Test: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapperTest.kt`

**Interfaces:**
- Produces: `object CanonicalMessageMapper { val mapper: ObjectMapper; fun create(): ObjectMapper }` — `mapper` ist die geteilte, thread-sichere Instanz; `create()` erzeugt eine frische Instanz (für Tests).

- [ ] **Step 1: Failing Test schreiben**

`common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapperTest.kt`:

```kotlin
package de.mosimtech.common.vault.messaging

import de.mosimtech.common.core.urn.Urn
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class CanonicalMessageMapperTest {

    private val mapper = CanonicalMessageMapper.create()

    data class WithOptional(val a: String, val b: String? = null)
    data class WithoutField(val a: String)
    data class Unsorted(val zebra: Int, val apple: Int)
    data class WithMap(val data: Map<String, String>)
    data class WithTime(val t: Instant)
    data class WithUrn(val id: Urn)

    @Test
    fun `null fields are omitted`() {
        assertThat(mapper.writeValueAsString(WithOptional("x"))).isEqualTo("""{"a":"x"}""")
    }

    @Test
    fun `object with null field and object without the field produce identical bytes`() {
        val withNull = mapper.writeValueAsString(WithOptional("x", null))
        val without = mapper.writeValueAsString(WithoutField("x"))
        assertThat(withNull).isEqualTo(without)
    }

    @Test
    fun `properties are sorted alphabetically`() {
        assertThat(mapper.writeValueAsString(Unsorted(zebra = 1, apple = 2)))
            .isEqualTo("""{"apple":2,"zebra":1}""")
    }

    @Test
    fun `map keys are sorted`() {
        val json = mapper.writeValueAsString(WithMap(linkedMapOf("z" to "1", "a" to "2")))
        assertThat(json).isEqualTo("""{"data":{"a":"2","z":"1"}}""")
    }

    @Test
    fun `instant is serialized as ISO-8601 and round-trips`() {
        val t = Instant.parse("2026-06-18T23:10:00.062404389Z")
        val json = mapper.writeValueAsString(WithTime(t))
        assertThat(json).isEqualTo("""{"t":"2026-06-18T23:10:00.062404389Z"}""")
        assertThat(mapper.readValue(json, WithTime::class.java).t).isEqualTo(t)
    }

    @Test
    fun `urn is serialized via toUrnString`() {
        val urn = Urn.parse("urn:user:keycloak:abc")
        val json = mapper.writeValueAsString(WithUrn(urn))
        assertThat(json).isEqualTo("""{"id":"urn:user:keycloak:abc"}""")
    }
}
```

- [ ] **Step 2: Test ausführen, Fehlschlag bestätigen**

Run: `./gradlew :common-util-vault:test --tests "*.CanonicalMessageMapperTest"`
Expected: FAIL — `CanonicalMessageMapper` nicht aufgelöst (Kompilierfehler).

- [ ] **Step 3: Minimale Implementierung**

`common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapper.kt`:

```kotlin
package de.mosimtech.common.vault.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule

/**
 * Deterministischer ObjectMapper für die Signatur-Serialisierung asynchroner Nachrichten.
 *
 * Signer und Verifier nutzen ausschließlich diesen Mapper, damit die signierten Bytes
 * unabhängig von DTO-Version und service-spezifischer Jackson-Config sind:
 *  - NON_NULL: neu hinzugefügte optionale null-Felder ändern die Bytes nicht
 *  - Properties + Map-Keys alphabetisch sortiert: stabile Reihenfolge
 *  - ISO-8601 (kein Epoch) für Zeittypen: stabil und voll präzise
 */
object CanonicalMessageMapper {

    val mapper: ObjectMapper = create()

    fun create(): ObjectMapper =
        JsonMapper.builder()
            .addModule(kotlinModule())
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()
}
```

- [ ] **Step 4: Test ausführen, Erfolg bestätigen**

Run: `./gradlew :common-util-vault:test --tests "*.CanonicalMessageMapperTest"`
Expected: PASS (alle 6 Tests grün).

> Falls der Instant-Test wider Erwarten an einem abweichenden String-Format scheitert (z.B. Nanosekunden-Darstellung), die erwartete Zeichenkette an die tatsächliche Jackson-3-Ausgabe angleichen — das ISO-8601-Verhalten an sich ist der Vertrag, nicht die exakte Stelligkeit.

- [ ] **Step 5: Commit (erst nach Nutzer-Freigabe)**

```bash
git add common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapper.kt \
        common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/CanonicalMessageMapperTest.kt
git commit -m "$(cat <<'EOF'
feat(vault): add deterministic CanonicalMessageMapper for signature serialization

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
)"
```

---

## Task 2: Signer/Verifier auf Canonical Mapper umstellen + Regressionstest

**Files:**
- Modify: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageSigner.kt`
- Modify: `common-util-vault/src/main/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageVerifier.kt`
- Modify: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageSignerTest.kt`
- Modify: `common-util-vault/src/test/kotlin/de/mosimtech/common/vault/messaging/VaultTransitMessageVerifierTest.kt`
- Modify: `gradle.properties`

**Interfaces:**
- Consumes: `CanonicalMessageMapper.mapper` (Task 1).
- Produces:
  - `class VaultTransitMessageSigner(vaultOperations: VaultOperations, keyName: String)` — **kein** `objectMapper`-Parameter mehr; `fun <T> sign(payload, serviceAccountToken, userToken?, exp?, issuedAt, context?): SignedMessageEnvelope<T>` unverändert.
  - `class VaultTransitMessageVerifier(vaultOperations: VaultOperations, keyName: String)` — **kein** `objectMapper`-Parameter mehr; `fun <T> verify(envelope, serviceAccountToken, userToken?): Boolean` und `fun <T> isTokenValidAtIssuance(...)` unverändert.

- [ ] **Step 1: Regressionstest schreiben (Versions-Skew simulieren)**

In `VaultTransitMessageVerifierTest.kt` neuen Test ergänzen. Er bildet den 3.3.4→3.3.5-Skew nach: Es wird über ein Payload **ohne** optionales Feld signiert (Sender) und über eines **mit** `null`-Feld verifiziert (Empfänger). Der Test nutzt einen **echten** (nicht gemockten) Mapper-Pfad, indem er die `buildSigningInput`-Bytes beider Varianten vergleicht — die Vault-Operation bleibt gemockt, aber die Plaintext-Bytes werden geprüft.

Imports oben in der Datei ergänzen:

```kotlin
import org.mockito.kotlin.argumentCaptor
import org.springframework.vault.support.SignatureValidation
```

Test (ans Ende der Klasse einfügen):

```kotlin
    data class SenderPayload(val application: String, val title: String)
    data class ReceiverPayload(val application: String, val title: String, val androidId: String? = null)

    @Test
    fun `signing input is identical when receiver has an extra optional null field (version skew)`() {
        // Sender-Bytes
        val senderInput = buildSigningInput(
            CanonicalMessageMapper.create(),
            SenderPayload("shiftcalc", "Schicht morgen"),
            issuedAt, null, "urn:rabbitmq:mosimtech:fixed", "svc-token", null
        )
        // Empfänger-Bytes (DTO um optionales null-Feld erweitert)
        val receiverInput = buildSigningInput(
            CanonicalMessageMapper.create(),
            ReceiverPayload("shiftcalc", "Schicht morgen", null),
            issuedAt, null, "urn:rabbitmq:mosimtech:fixed", "svc-token", null
        )
        assertThat(receiverInput).isEqualTo(senderInput)
    }
```

> Hinweis: `buildSigningInput` ist `internal` und liegt im selben Modul/Package — im Test direkt aufrufbar.

- [ ] **Step 2: Test ausführen, Fehlschlag bestätigen**

Run: `./gradlew :common-util-vault:test --tests "*.VaultTransitMessageVerifierTest"`
Expected: FAIL — Kompilierfehler, weil der Verifier-Konstruktor in `setUp()` noch `objectMapper` erwartet bzw. (nach Anpassung) zunächst noch das alte Verhalten hat. (Primär belegt dieser Schritt, dass der neue Test bzw. der neue Konstruktor noch nicht existiert.)

- [ ] **Step 3: Signer umstellen**

`VaultTransitMessageSigner.kt` vollständig ersetzen:

```kotlin
package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import java.time.Instant

class VaultTransitMessageSigner(
    private val vaultOperations: VaultOperations,
    private val keyName: String,
) {
    private val objectMapper = CanonicalMessageMapper.mapper

    fun <T> sign(
        payload: T,
        serviceAccountToken: String,
        userToken: String? = null,
        exp: Instant? = null,
        issuedAt: Instant = Instant.now(),
        context: String? = null,
    ): SignedMessageEnvelope<T> {
        val messageId = generateMessageId(context)
        val input = buildSigningInput(objectMapper, payload, issuedAt, exp, messageId, serviceAccountToken, userToken)
        val vaultSignature = vaultOperations.opsForTransit()
            .sign(keyName, Plaintext.of(input))
        return SignedMessageEnvelope(
            payload = payload,
            issuedAt = issuedAt,
            exp = exp,
            signature = vaultSignature.signature,
            messageId = messageId
        )
    }
}
```

- [ ] **Step 4: Verifier umstellen**

`VaultTransitMessageVerifier.kt` vollständig ersetzen:

```kotlin
package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import java.time.Instant

class VaultTransitMessageVerifier(
    private val vaultOperations: VaultOperations,
    private val keyName: String,
) {
    private val objectMapper = CanonicalMessageMapper.mapper

    fun <T> verify(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean =
        isSignatureValid(envelope, serviceAccountToken, userToken)
            && isMessageNotExpired(envelope)

    private fun <T> isSignatureValid(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean {
        val input = buildSigningInput(
            objectMapper,
            envelope.payload,
            envelope.issuedAt,
            envelope.exp,
            envelope.messageId,
            serviceAccountToken,
            userToken
        )
        return vaultOperations.opsForTransit().verify(
            keyName,
            Plaintext.of(input),
            Signature.of(envelope.signature),
        )
    }

    private fun isMessageNotExpired(envelope: SignedMessageEnvelope<*>): Boolean =
        envelope.exp == null || Instant.now().isBefore(envelope.exp)

    // jwt.iat <= envelope.issuedAt < jwt.exp (exp ist exklusiv gemäß JWT RFC 7519 §4.1.4)
    fun <T> isTokenValidAtIssuance(envelope: SignedMessageEnvelope<T>, jwtIat: Instant, jwtExp: Instant): Boolean =
        !envelope.issuedAt.isBefore(jwtIat) && envelope.issuedAt.isBefore(jwtExp)
}
```

- [ ] **Step 5: Bestehende Tests an neuen Konstruktor anpassen**

In `VaultTransitMessageVerifierTest.kt`:
- Den `objectMapper`-Feld-Import/-Deklaration entfernen, falls ungenutzt (`jacksonObjectMapper` wird nicht mehr für den Konstruktor gebraucht).
- `setUp()` ändern:

```kotlin
    @BeforeEach
    fun setUp() {
        whenever(vaultOperations.opsForTransit()).thenReturn(transitOperations)
        verifier = VaultTransitMessageVerifier(vaultOperations, "test-key")
    }
```

In `VaultTransitMessageSignerTest.kt`:
- Konstruktoraufruf analog auf `VaultTransitMessageSigner(vaultOperations, "test-key")` ändern (das gemockte/ungenutzte `objectMapper`-Argument streichen).

- [ ] **Step 6: Versions-Bump**

`gradle.properties`: `version=3.3.6` → `version=3.3.7`.

- [ ] **Step 7: Alle Vault-Modul-Tests ausführen**

Run: `./gradlew :common-util-vault:test`
Expected: PASS — inkl. neuem Skew-Regressionstest und angepasstem Signer/Verifier-Test.

- [ ] **Step 8: Commit (erst nach Nutzer-Freigabe)**

```bash
git add common-util-vault/src gradle.properties
git commit -m "$(cat <<'EOF'
refactor(vault): use CanonicalMessageMapper in signer/verifier, drop injected ObjectMapper

Macht die Signatur unabhängig von DTO-Version und service-spezifischer Jackson-Config.
Bump version to 3.3.7.

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
)"
```

---

## Task 3: Bibliothek veröffentlichen (Nexus)

**Files:** keine (Build-/Publish-Schritt).

- [ ] **Step 1: Artefakte bauen & veröffentlichen**

Run: `./gradlew :common-util-vault:publish :common-util-core:publish`
Expected: Upload von `de.mosimtech:common-util-vault:3.3.7` (und ggf. abhängiger Module) nach `https://dev.momasoft.de/nexus/repository/maven-releases/` ohne Fehler.

> Falls die Konsumenten lokal über `mavenLocal()` aufgelöst werden, stattdessen
> `./gradlew publishToMavenLocal` ausführen. Vor dem Heben der Konsumenten sicherstellen,
> dass `3.3.7` im aufgelösten Repository verfügbar ist.

---

## Task 4: notification-service (Empfänger) auf 3.3.7 heben

**Files:**
- Modify: `mosimtech-notification-service/gradle/libs.versions.toml`
- Modify: `mosimtech-notification-service/src/main/kotlin/de/mosimtech/notificationservice/config/VaultTransitMessagingConfig.kt`

**Interfaces:**
- Consumes: `VaultTransitMessageVerifier(vaultOperations, keyName)` (Task 2).

- [ ] **Step 1: Versionen anheben**

In `mosimtech-notification-service/gradle/libs.versions.toml`:
- `common-util = "3.3.2"` → `common-util = "3.3.7"`
- `common-util-core = "3.3.5"` → `common-util-core = "3.3.7"`

- [ ] **Step 2: VaultTransitMessagingConfig anpassen**

`mosimtech-notification-service/src/main/kotlin/de/mosimtech/notificationservice/config/VaultTransitMessagingConfig.kt` — den `objectMapper`-Konstruktorparameter entfernen und Verifier ohne ihn bauen:

```kotlin
package de.mosimtech.notificationservice.config

import de.mosimtech.common.vault.messaging.VaultTransitMessageVerifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.core.VaultOperations

@Configuration
@EnableConfigurationProperties(AppSecurityProperties::class)
class VaultTransitMessagingConfig(
    private val vaultOperations: VaultOperations,
    private val securityProperties: AppSecurityProperties,
) {

    @Bean
    fun vaultTransitMessageVerifier(): VaultTransitMessageVerifier =
        VaultTransitMessageVerifier(vaultOperations, securityProperties.transit.keyName)
}
```

- [ ] **Step 3: Kompilieren & Tests**

Run: `./gradlew compileKotlin test` (im notification-service-Repo)
Expected: PASS — Build löst `3.3.7` auf, Config kompiliert ohne `objectMapper`.

- [ ] **Step 4: Commit (erst nach Nutzer-Freigabe)**

```bash
git add gradle/libs.versions.toml src/main/kotlin/de/mosimtech/notificationservice/config/VaultTransitMessagingConfig.kt
git commit -m "$(cat <<'EOF'
chore: bump common-util to 3.3.7 and adapt VaultTransitMessagingConfig

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
)"
```

---

## Task 5: ShiftCalc-Backend (Sender) auf 3.3.7 heben

**Files:**
- Modify: `Shift-Time-Tracker/ShiftCalc-Backend/gradle/libs.versions.toml`
- Modify: `Shift-Time-Tracker/ShiftCalc-Backend/shiftcalc-core/src/main/kotlin/de/mosimtech/momasoft/shiftcalc/core/config/VaultTransitMessagingConfig.kt`

**Interfaces:**
- Consumes: `VaultTransitMessageSigner(vaultOperations, keyName)` und `VaultTransitMessageVerifier(vaultOperations, keyName)` (Task 2).

- [ ] **Step 1: Version anheben**

In `Shift-Time-Tracker/ShiftCalc-Backend/gradle/libs.versions.toml`: `common = "3.3.4"` → `common = "3.3.7"`.

- [ ] **Step 2: VaultTransitMessagingConfig anpassen**

`shiftcalc-core/.../config/VaultTransitMessagingConfig.kt` — `objectMapper`-Parameter entfernen:

```kotlin
package de.mosimtech.momasoft.shiftcalc.core.config

import de.mosimtech.common.vault.messaging.VaultTransitMessageSigner
import de.mosimtech.common.vault.messaging.VaultTransitMessageVerifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.vault.core.VaultOperations

@Configuration
class VaultTransitMessagingConfig(
    private val vaultOperations: VaultOperations,
    private val securityProperties: AppSecurityProperties,
) {
    @Bean
    fun vaultTransitMessageSigner(): VaultTransitMessageSigner =
        VaultTransitMessageSigner(vaultOperations, securityProperties.transit.keyName)

    @Bean
    fun vaultTransitMessageVerifier(): VaultTransitMessageVerifier =
        VaultTransitMessageVerifier(vaultOperations, securityProperties.transit.keyName)
}
```

- [ ] **Step 3: Kompilieren & Tests**

Run: `./gradlew compileKotlin test` (im ShiftCalc-Backend-Repo)
Expected: PASS — Build löst `3.3.7` auf, Config kompiliert ohne `objectMapper`.

- [ ] **Step 4: Commit (erst nach Nutzer-Freigabe)**

```bash
git add gradle/libs.versions.toml shiftcalc-core/src/main/kotlin/de/mosimtech/momasoft/shiftcalc/core/config/VaultTransitMessagingConfig.kt
git commit -m "$(cat <<'EOF'
chore: bump common-util to 3.3.7 and adapt VaultTransitMessagingConfig

Co-Authored-By: Claude Opus 4.8 <noreply@anthropic.com>
EOF
)"
```

---

## Task 6: End-to-End-Verifikation

**Files:** keine (manuelle/integrative Prüfung).

- [ ] **Step 1: Beide Services starten (notification-service + ShiftCalc) gegen dieselbe Vault-Instanz/denselben Key (`async-message-signing`).**

- [ ] **Step 2: Eine Schicht-Reminder-Notification über ShiftCalc auslösen** (Pfad `ShiftNotificationPublisher.publish(...)`), die eine `notification.push.shiftcalc`-Nachricht erzeugt.

- [ ] **Step 3: notification-service-Logs prüfen.**
Expected: **Keine** `SecurityException: Vault-Signatur ungültig` mehr; stattdessen `Push-Benachrichtigung empfangen: application=... userId=...` und erfolgreicher Versand. Die ursprüngliche Fehlersignatur (`NotificationListener.kt:82`) tritt nicht mehr auf.

---

## Self-Review-Ergebnis

- **Spec-Coverage:** Canonical Mapper (Task 1) ↔ Spec §3.1/§3.2; Signer/Verifier-Umstellung + entfallener Parameter (Task 2) ↔ §3.2; Tests inkl. Skew-Regression (Task 1/2) ↔ §5; Rollout/Publish + beide Konsumenten (Task 3–5) ↔ §6; E2E (Task 6) ↔ §1-Fehlerbild. Abgedeckt.
- **Abweichung von der Spec:** Spec nennt „JavaTime-Modul registriert"; in Jackson 3 ist java.time eingebaut und wird per Default als ISO-8601 serialisiert — daher keine explizite Modul-Registrierung, nur `disable(WRITE_DATES_AS_TIMESTAMPS)` zur Absicherung. Funktional identisch.
- **Placeholder-Scan:** keine offenen TBD/TODO; alle Code-Steps enthalten vollständigen Code.
- **Typ-Konsistenz:** `VaultTransitMessageSigner(vaultOperations, keyName)` / `VaultTransitMessageVerifier(vaultOperations, keyName)` werden in Task 2 definiert und in Task 4/5 exakt so konsumiert; `CanonicalMessageMapper.mapper`/`create()` konsistent zwischen Task 1 und 2.
