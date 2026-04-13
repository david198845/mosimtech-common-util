# Design: common-util-security & common-util-delegation

**Datum:** 2026-04-13  
**Projekt:** mosimtech-common-util  
**Status:** Genehmigt

---

## 1. Zielsetzung

Einführung von zwei neuen Sub-Modulen im `mosimtech-common-util` Multi-Modul-Projekt:

- `common-util-security` — Keycloak/OAuth2-Infrastruktur mit neuem Client-Rollen-Konzept
- `common-util-delegation` — UMA 2.0-basiertes Delegationssystem für User-Datenteilung

Gleichzeitig wird `common-util-core` von Spring Security-Abhängigkeiten befreit.

---

## 2. Modul-Struktur & Abhängigkeiten

```
common-util-core  ←  common-util-security  ←  common-util-delegation
```

| Modul                   | Abhängigkeiten              | Wer bindet es ein                        |
|-------------------------|-----------------------------|------------------------------------------|
| `common-util-core`      | —                           | Alle Projekte                            |
| `common-util-security`  | common-util-core            | Alle Projekte (Breaking Change)          |
| `common-util-delegation`| common-util-security        | Nur Projekte mit Delegations-Feature     |

---

## 3. common-util-security

### 3.1 Enthaltene Klassen

| Klasse / Datei                            | Beschreibung                                                                 |
|-------------------------------------------|------------------------------------------------------------------------------|
| `AudienceValidator`                       | Validiert den `aud`-Claim im JWT (unverändert aus core übernommen)           |
| `AzpValidator`                            | Validiert den `azp`-Claim (Whitelist von Clients) (unverändert)              |
| `KeycloakJwtGrantedAuthoritiesConverter`  | Extrahiert Realm-Rollen (`realm_access`) und Client-Rollen (`resource_access`) aus dem JWT |
| `SecurityContextAdapter`                  | Zentraler Accessor für den Security Context — erweitert um Client-Rollen-Methoden |
| `DelegationConstants`                     | Einzige globale Konstante: `SYSTEM_INVOKE = "system:invoke"`                 |

### 3.2 KeycloakJwtGrantedAuthoritiesConverter

Ersetzt den Standard-Spring-Converter. Erzeugt Authorities in diesen Formaten:

- Realm-Rollen: `ROLE_finance-admin`, `ROLE_core-user`
- Client-Rollen: `momasoft-finance-api:finance:manage`, `momasoft-shift-api:shift:view`

### 3.3 SecurityContextAdapter — neue Methoden

```kotlin
object SecurityContextAdapter {

    // Bestehend (unverändert):
    fun getCurrentUserID(): Urn?
    fun getCurrentToken(): Jwt?
    fun hasRole(role: String): Boolean
    fun isAdmin(): Boolean

    // Neu:
    fun hasClientRole(client: String, role: String): Boolean
    fun getClientRoles(client: String): List<String>
    fun isSystemInvocation(client: String): Boolean =
        hasClientRole(client, DelegationConstants.SYSTEM_INVOKE)
}
```

### 3.4 Rollen-Konstanten

Die Library definiert **keine** projektspezifischen Rollen-Konstanten. Jedes Projekt legt seine eigenen an:

```kotlin
// Beispiel im Shift-Service (projektspezifisch, nicht in der Library):
object ShiftClientRoles {
    const val VIEW   = "shift:view"
    const val EDIT   = "shift:edit"
    const val MANAGE = "shift:manage"
}
```

### 3.5 Dependencies (build.gradle.kts)

```kotlin
api(libs.spring.security.core)
api(libs.spring.security.oauth2.jose)
api(libs.spring.security.oauth2.resource.server)
api(project(":common-util-core"))
```

---

## 4. common-util-delegation

Basiert auf der Spezifikation in `delegation.md`.

### 4.1 Enthaltene Klassen

| Klasse                    | Beschreibung                                                                        |
|---------------------------|-------------------------------------------------------------------------------------|
| `DelegationContextHolder` | ThreadLocal-Holder für die Ziel-User-ID (`X-Target-User-Id`)                        |
| `DelegationHeaderFilter`  | `OncePerRequestFilter` — liest Header, befüllt Context, räumt in `finally` auf      |
| `CurrentUserProvider`     | `@Component` — liefert eigene oder delegierte User-ID als `Urn`                     |
| `UmaPermissionEvaluator`  | Implementiert `PermissionEvaluator` für `@PreAuthorize("hasPermission(...)")`       |
| `DelegationEvent`         | RabbitMQ-Payload für `delegation.status.changed`                                    |
| `DelegationGrantor`       | Eingebettetes DTO im `DelegationEvent`                                              |
| `DelegationGrantee`       | Eingebettetes DTO im `DelegationEvent`                                              |

### 4.2 DelegationContextHolder

```kotlin
object DelegationContextHolder {
    private val targetUserId = ThreadLocal<String?>()
    fun set(userId: String) { targetUserId.set(userId) }
    fun get(): String? = targetUserId.get()
    fun clear() { targetUserId.remove() }
}
```

### 4.3 DelegationHeaderFilter

- Liest Header `X-Target-User-Id`
- Befüllt `DelegationContextHolder` wenn Header vorhanden und nicht leer
- `clear()` wird **immer** im `finally`-Block aufgerufen (Memory-Leak-Schutz)

### 4.4 CurrentUserProvider

```kotlin
@Component
class CurrentUserProvider(private val securityContextAdapter: SecurityContextAdapter) {
    fun getEffectiveUserId(): Urn =
        DelegationContextHolder.get()
            ?.let { Urn.parse(it) }
            ?: securityContextAdapter.getCurrentUserID()!!
}
```

### 4.5 UmaPermissionEvaluator

Implementiert `org.springframework.security.access.PermissionEvaluator`. Validiert bei jedem `@PreAuthorize("hasPermission(targetUserId, 'urn:momasoft:shift', 'shift:view')")` das RPT-Token gegen Keycloaks Token Introspection Endpoint.

### 4.6 RabbitMQ-Event

```
Exchange:     iam.events.topic
Routing Key:  delegation.status.changed
```

`DelegationEvent` enthält: `eventId`, `timestamp`, `eventType` (DELEGATION_ACCEPTED / DELEGATION_REVOKED), `grantor` (`DelegationGrantor`), `grantee` (`DelegationGrantee`), `module`, `scopes`.

### 4.7 Dependencies (build.gradle.kts)

```kotlin
api(project(":common-util-security"))
api(libs.spring.web)  // OncePerRequestFilter
// Keycloak UMA Client für Protection API (Token Introspection)
```

---

## 5. Migration bestehender Konsumenten

### 5.1 build.gradle.kts

```kotlin
// Vorher:
implementation("de.mosimtech:common-util-core:2.x.x")

// Nachher (alle Projekte):
implementation("de.mosimtech:common-util-core:3.0.0")
implementation("de.mosimtech:common-util-security:3.0.0")

// Zusätzlich nur für Projekte mit Delegation:
implementation("de.mosimtech:common-util-delegation:3.0.0")
```

### 5.2 Imports anpassen

Alle `de.mosimtech.common.core.security.*` Importe werden zu `de.mosimtech.common.security.*`.

### 5.3 common-util-core aufräumen

Folgende Spring Security Dependencies werden aus `common-util-core/build.gradle.kts` entfernt:

```kotlin
// Werden entfernt:
api(libs.spring.security.core)
api(libs.spring.security.oauth2.jose)
api(libs.spring.security.oauth2.resource.server)
```

### 5.4 Versioning

Alle drei Module starten synchron bei `3.0.0`. Danach können sie unabhängig versioniert werden.

---

## 6. Sicherheitsbetrachtungen

- **Spoofing von `X-Target-User-Id`:** Der Header allein gewährt keinen Zugriff. Die `@PreAuthorize`-Annotation mit `UmaPermissionEvaluator` validiert zwingend das RPT-Token gegen Keycloak. Ein gefälschter Header ohne gültiges RPT führt zu HTTP 403.
- **ThreadLocal-Leaks:** `DelegationHeaderFilter` räumt den Context immer im `finally`-Block auf.
- **M2M-Vertrauen:** `isSystemInvocation(client)` prüft explizit die `system:invoke` Client-Rolle — Service Accounts bekommen keine Realm-Rollen.
