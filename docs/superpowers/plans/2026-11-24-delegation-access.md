# Delegation Access (common-util 3.9.0) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `common-util-delegation` liefert einen geprüften Delegationskontext: Ein Request darf nur dann im Namen eines anderen Nutzers arbeiten, wenn das Fachmodul die Freigabe bestätigt hat und der Endpunkt dafür markiert ist.

**Architecture:** Neues Paket `de.mosimtech.common.delegation.access`. Ein Servlet-Filter liest `X-Target-User-Id`, fragt einen vom Fachmodul gelieferten `DelegationGrantResolver` und legt bei Erfolg einen `DelegationContext` in einen Thread-gebundenen Holder. Ein `HandlerInterceptor` lässt delegierte Requests nur auf Methoden mit `@DelegatedAccess` zu. `CurrentUserProvider` liefert daraus den Eigentümer. Alles Bestehende bleibt unverändert und wird als veraltet markiert.

**Tech Stack:** Kotlin, Spring Web/WebMVC, Jakarta Servlet, JUnit 5 über `kotlin.test`, Mockito-Kotlin, `spring-boot-test` (Mock-Request/-Response). Gradle Version Catalog.

**Spec:** `C:\workspace-privat\mosimtech\MoMaSoft\Shift\momasoft-shiftcalc-api\docs\superpowers\specs\2026-11-24-delegation-design.md` (Abschnitte 7.2, 7.3, 8, 11). Ticket: **MoMaSoft-27**, übergeordnet MSC-64.

## Global Constraints

- **Keine Git-Commits, kein Push, kein Branch, kein Tag.** Änderungen bleiben im Working Tree; der Auftraggeber committet selbst. Auch kein `gradlew publish`.
- **Rein additiv.** Keine bestehende öffentliche Signatur ändern oder entfernen, kein bestehendes Verhalten ändern. Veraltetes bekommt `@Deprecated`, sonst nichts.
- Fassung `3.8.1` → `3.9.0` in `gradle.properties`. Die Minor-Stelle ist vom Auftraggeber genehmigt. Keine andere Fassung vergeben.
- Eine Klasse, ein Interface, eine Annotation je Datei, benannt nach dem Typ. Nichts in `event/DelegationEvent.kt` ergänzen.
- Bezeichner und Log-Ausgaben englisch; Kommentare und KDoc deutsch; keine Ticketnummern im Code.
- Abhängigkeiten nur über den Version Catalog (`libs.…`), keine Fassungen in `build.gradle.kts`.
- Header-Name ausschließlich über `de.mosimtech.common.security.DelegationConstants.HEADER_TARGET_USER_ID` (`X-Target-User-Id`).
- Keine Klasse über 300 Zeilen.
- Tests im Stil des Moduls: `kotlin.test`-Annotationen und -Assertions, Mockito-Kotlin, Testnamen englisch in Backticks.
- Arbeitsverzeichnis: `C:\workspace-privat\mosimtech\Common\mosimtech-common-util`. Die unversionierte Datei `docs/superpowers/task-1-2-report.md` gehört nicht zu dieser Aufgabe und bleibt unberührt.

## Review Focus

1. **Header gesetzt, aber niemand angemeldet** (Filter läuft vor der Authentifizierung oder auf einem offenen Pfad): Der Request muss mit 403 enden. Stilles Ignorieren würde eine Schreibaktion, die für den Eigentümer gedacht war, im Plan des Aufrufers ausführen. → Task 2.
2. **Resolver wirft eine Ausnahme** (Datenbank weg): Der Holder muss danach leer sein, der Fehler geht nach oben, es entsteht kein Kontext. → Task 2.
3. **Thread wird wiederverwendet:** Nach einem Request mit Fehler in der Kette darf der nächste Request auf demselben Thread keinen Kontext vorfinden. → Task 2.
4. **Handler ist keine Controller-Methode** (statische Ressource, Fehlerseite) bei gesetztem Kontext: 403, nicht durchlassen. → Task 3.
5. **Alter und neuer Holder gleichzeitig gesetzt:** Der geprüfte Kontext gewinnt, sonst ließe sich die Prüfung über den veralteten Filter umgehen. → Task 4.

## File Structure

Alle Pfade relativ zu `common-util-delegation/`.

| Datei | Aufgabe |
| --- | --- |
| `src/main/kotlin/de/mosimtech/common/delegation/access/DelegationContext.kt` | Wertobjekt: Eigentümer, Aufrufer, Rechte |
| `…/access/DelegationAccessHolder.kt` | Thread-gebundener Träger des geprüften Kontexts |
| `…/access/DelegationGrantResolver.kt` | Schnittstelle, die das Fachmodul implementiert |
| `…/access/DelegationAccessFilter.kt` | Header lesen, Freigabe auflösen, Kontext setzen und räumen |
| `…/access/DelegatedAccess.kt` | Annotation für delegiert erreichbare Controller-Methoden |
| `…/access/DelegatedAccessInterceptor.kt` | Standard „verboten" für delegierte Requests |
| `…/event/DelegationGrantChangedEvent.kt` | zustandstragendes Event, zugleich Zeile des Abgleichs |
| `…/CurrentUserProvider.kt` | ändern: geprüfter Kontext zuerst, `getActorUserId()` |
| `…/DelegationHeaderFilter.kt`, `…/DelegationContextHolder.kt`, `…/uma/UmaPermissionEvaluator.kt`, `…/event/DelegationEvent.kt` | nur `@Deprecated` ergänzen |
| `build.gradle.kts` | `compileOnly(libs.spring.webmvc)`, `testImplementation(libs.spring.webmvc)` |

Tests spiegelbildlich unter `src/test/kotlin/de/mosimtech/common/delegation/…`.

---

### Task 1: DelegationContext, Holder und Resolver-Schnittstelle

**Files:**
- Create: `…/access/DelegationContext.kt`, `…/access/DelegationAccessHolder.kt`, `…/access/DelegationGrantResolver.kt`
- Test: `src/test/kotlin/de/mosimtech/common/delegation/access/DelegationContextTest.kt`, `…/access/DelegationAccessHolderTest.kt`

**Interfaces:**
- Consumes: `de.mosimtech.common.core.urn.Urn`
- Produces:
  ```kotlin
  data class DelegationContext(val ownerId: Urn, val actorId: Urn, val scopes: Set<String>) {
      fun hasScope(scope: String): Boolean
  }
  object DelegationAccessHolder {
      fun set(context: DelegationContext)
      fun get(): DelegationContext?
      fun clear()
  }
  fun interface DelegationGrantResolver {
      /** Rechte des Aufrufers im Bestand des Eigentümers; `null`, wenn keine gültige Freigabe besteht. */
      fun resolve(actorId: Urn, ownerId: Urn): Set<String>?
  }
  ```

- [ ] **Step 1: Tests schreiben**

  `DelegationContextTest`:
  - `` `hasScope returns true only for a granted scope` ``: Kontext mit `setOf("plan:read")` → `hasScope("plan:read")` wahr, `hasScope("plan:write")` falsch, `hasScope("PLAN:READ")` falsch (exakter Vergleich).

  `DelegationAccessHolderTest` (mit `@AfterTest` → `clear()`):
  - `` `get returns null when nothing was set` ``
  - `` `get returns the context that was set` ``
  - `` `clear removes the context` ``
  - `` `context is not visible on another thread` ``: im Haupt-Thread setzen, in einem neuen `Thread` lesen → `null`.

- [ ] **Step 2: Tests laufen lassen, Fehlschlag bestätigen**

  Run: `./gradlew :common-util-delegation:test --tests "*access.DelegationContextTest" --tests "*access.DelegationAccessHolderTest"`
  Expected: Kompilierfehler, die Typen existieren nicht.

- [ ] **Step 3: Die drei Typen anlegen**

  `DelegationAccessHolder` nutzt ein einfaches `ThreadLocal`, kein `InheritableThreadLocal`: Der Kontext darf nicht in asynchron gestartete Verarbeitung wandern. KDoc an `DelegationGrantResolver` hält fest: Die Implementierung prüft selbst Aktivität und Ablaufdatum der Freigabe und liefert bei einer gültigen Freigabe nie eine leere Menge.

- [ ] **Step 4: Tests laufen lassen**

  Run: wie Step 2. Expected: 5 Tests, alle grün.

---

### Task 2: DelegationAccessFilter

**Files:**
- Create: `…/access/DelegationAccessFilter.kt`
- Test: `src/test/kotlin/de/mosimtech/common/delegation/access/DelegationAccessFilterTest.kt`

**Interfaces:**
- Consumes: Task 1; `DelegationConstants.HEADER_TARGET_USER_ID`; `SecurityContextAdapter.getCurrentUserID(): Urn?`; `Urn.parse(String): Urn?`
- Produces:
  ```kotlin
  class DelegationAccessFilter(
      private val grantResolver: DelegationGrantResolver,
      private val actorProvider: () -> Urn? = { SecurityContextAdapter.getCurrentUserID() },
  ) : OncePerRequestFilter()
  ```
  Das Fachmodul registriert ihn **hinter** der Authentifizierung in der Security-Filterkette.

**Verhalten (Entscheidungstabelle):**

| Header | Aufrufer | Auflösung | Ergebnis |
| --- | --- | --- | --- |
| fehlt oder leer | beliebig | – | Kette läuft, kein Kontext |
| nicht als URN lesbar | beliebig | – | 403, Kette läuft nicht |
| gültig | `null` | – | 403, Kette läuft nicht |
| gültig, gleich Aufrufer | gesetzt | – | Kette läuft, kein Kontext, Resolver wird nicht gefragt |
| gültig, anderer Nutzer | gesetzt | `null` | 403, Kette läuft nicht |
| gültig, anderer Nutzer | gesetzt | Menge | Kontext gesetzt, Kette läuft, danach geräumt |

403 über `response.sendError(HttpServletResponse.SC_FORBIDDEN)` ohne Text, der „Freigabe fehlt" von „Nutzer unbekannt" unterscheidet. Abgelehnte Requests werden auf WARN mit Aufrufer und Zielnutzer geloggt, nie mit Token.

- [ ] **Step 1: Tests schreiben**

  Hilfsaufbau: `MockHttpServletRequest`, `MockHttpServletResponse`, eine Kette, die den Holder-Inhalt während des Durchlaufs festhält (Muster siehe bestehender `DelegationHeaderFilterTest`). `actor = urn:user:momasoft:actor`, `owner = urn:user:momasoft:owner`. `@AfterTest` → `DelegationAccessHolder.clear()`.

  - `` `passes through without context when header is absent` ``: Kette lief, erfasster Kontext `null`, Resolver nie aufgerufen.
  - `` `passes through without context when header is blank` ``: Header `"   "`.
  - `` `rejects with 403 when header is not a urn` ``: Header `"not-a-urn"` → Status 403, Kette lief nicht.
  - `` `rejects with 403 when no user is authenticated` ``: `actorProvider = { null }` → 403, Kette lief nicht, Resolver nie aufgerufen.
  - `` `ignores header that targets the caller itself` ``: Header = `actor` → Kette lief, Kontext `null`, Resolver nie aufgerufen.
  - `` `rejects with 403 when resolver finds no grant` ``: Resolver liefert `null` → 403, Kette lief nicht.
  - `` `sets verified context for the duration of the chain` ``: Resolver liefert `setOf("plan:read", "salary:read")` → erfasster Kontext hat `ownerId == owner`, `actorId == actor`, genau diese Rechte; Resolver wurde mit `(actor, owner)` aufgerufen.
  - `` `clears context after the chain completed` ``: nach `doFilter` ist `DelegationAccessHolder.get()` `null`.
  - `` `clears context when the chain throws` ``: Kette wirft `IllegalStateException` → Ausnahme kommt an, Holder danach leer.
  - `` `leaves no context when the resolver throws` ``: Resolver wirft `RuntimeException` → Ausnahme kommt an, Kette lief nicht, Holder leer.
  - `` `second request on the same thread starts without context` ``: erst ein Request mit gültiger Freigabe, dessen Kette wirft; dann ein Request ohne Header → erfasster Kontext `null`.

- [ ] **Step 2: Fehlschlag bestätigen**

  Run: `./gradlew :common-util-delegation:test --tests "*access.DelegationAccessFilterTest"`
  Expected: Kompilierfehler.

- [ ] **Step 3: Filter umsetzen** nach der Entscheidungstabelle. Der Kontext wird in `try { chain } finally { clear() }` gesetzt; Auflösen des Resolvers liegt **vor** dem `try`, damit ein Fehler dort keinen Kontext hinterlässt.

- [ ] **Step 4: Tests laufen lassen**

  Run: wie Step 2. Expected: 11 Tests, alle grün.

---

### Task 3: @DelegatedAccess und DelegatedAccessInterceptor

**Files:**
- Modify: `build.gradle.kts` (`compileOnly(libs.spring.webmvc)`, `testImplementation(libs.spring.webmvc)`)
- Create: `…/access/DelegatedAccess.kt`, `…/access/DelegatedAccessInterceptor.kt`
- Test: `src/test/kotlin/de/mosimtech/common/delegation/access/DelegatedAccessInterceptorTest.kt`

**Interfaces:**
- Consumes: Task 1
- Produces:
  ```kotlin
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.RUNTIME)
  annotation class DelegatedAccess(val scope: String)

  class DelegatedAccessInterceptor : HandlerInterceptor   // preHandle
  ```
  Das Fachmodul registriert den Interceptor für alle Pfade. Die Annotation steht an der Methode des Controllers, auch wenn dieser ein generiertes Interface implementiert.

**Verhalten:**

| Kontext im Holder | Handler | Ergebnis |
| --- | --- | --- |
| keiner | beliebig | `true` |
| gesetzt | kein `HandlerMethod` | 403, `false` |
| gesetzt | Methode ohne `@DelegatedAccess` | 403, `false` |
| gesetzt | Annotation, Recht fehlt im Kontext | 403, `false` |
| gesetzt | Annotation, Recht vorhanden | `true` |

- [ ] **Step 1: Tests schreiben**

  Testhilfe in der Testdatei: eine Schnittstelle `SampleApi` mit `read()`, `write()`, `plain()` und eine Klasse `SampleController : SampleApi`, deren Überschreibungen `read()` mit `@DelegatedAccess("plan:read")` und `write()` mit `@DelegatedAccess("plan:write")` tragen; `plain()` ohne Annotation. `HandlerMethod(controller, "read")` usw.

  - `` `allows any handler when no delegation context is set` ``: `plain` → `true`, Status 200.
  - `` `rejects delegated request to a method without annotation` ``: Kontext mit `plan:read`, Handler `plain` → `false`, 403.
  - `` `rejects delegated request when the scope is missing` ``: Kontext mit `plan:read`, Handler `write` → `false`, 403.
  - `` `allows delegated request when the scope is granted` ``: Kontext mit `plan:read`, Handler `read` → `true`.
  - `` `finds annotation on a method that overrides an interface method` ``: derselbe Fall wie zuvor, ausdrücklich über `SampleController`, das `SampleApi` implementiert → `true`.
  - `` `rejects delegated request when handler is not a controller method` ``: Kontext gesetzt, Handler ist ein beliebiges `Any()` → `false`, 403.

- [ ] **Step 2: Fehlschlag bestätigen**

  Run: `./gradlew :common-util-delegation:test --tests "*access.DelegatedAccessInterceptorTest"`
  Expected: Kompilierfehler.

- [ ] **Step 3: Abhängigkeit, Annotation und Interceptor umsetzen.** Annotation über `HandlerMethod.getMethodAnnotation(DelegatedAccess::class.java)` lesen. Ablehnung über `response.sendError(403)`, Log auf WARN mit Methode und gefordertem Recht.

- [ ] **Step 4: Tests laufen lassen**

  Run: wie Step 2. Expected: 6 Tests, alle grün.

---

### Task 4: CurrentUserProvider

**Files:**
- Modify: `src/main/kotlin/de/mosimtech/common/delegation/CurrentUserProvider.kt`
- Test: `src/test/kotlin/de/mosimtech/common/delegation/CurrentUserProviderTest.kt` (ergänzen)

**Interfaces:**
- Consumes: Task 1
- Produces:
  ```kotlin
  class CurrentUserProvider {
      /** Eigentümer der Daten, auf denen der Request arbeitet. */
      fun getEffectiveUserId(): Urn
      /** Der tatsächlich angemeldete Nutzer, unabhängig von einer Delegation. */
      fun getActorUserId(): Urn
  }
  ```

**Reihenfolge in `getEffectiveUserId()`:** geprüfter Kontext (`DelegationAccessHolder`) → veralteter `DelegationContextHolder` (unverändert, nur damit sich Bestehendes nicht ändert) → angemeldeter Nutzer → `IllegalStateException`. `getActorUserId()` liest ausschließlich den angemeldeten Nutzer und wirft dieselbe Ausnahme, wenn keiner da ist.

- [ ] **Step 1: Tests ergänzen** (bestehende Tests bleiben unverändert und müssen weiter grün sein; `cleanup` räumt zusätzlich `DelegationAccessHolder`)

  - `` `returns owner from verified delegation context` ``: angemeldet `own-uuid`, geprüfter Kontext mit `ownerId = grantorUrn` → `grantorUrn`.
  - `` `verified context wins over the deprecated holder` ``: geprüfter Kontext mit `grantorUrn`, veralteter Holder mit `urn:user:momasoft:other` → `grantorUrn`.
  - `` `getActorUserId returns the authenticated user even when delegated` ``: angemeldet `own-uuid`, geprüfter Kontext mit `grantorUrn` → `ownUrn`.
  - `` `getActorUserId throws when nobody is authenticated` `` → `IllegalStateException`.

- [ ] **Step 2: Fehlschlag bestätigen**

  Run: `./gradlew :common-util-delegation:test --tests "*CurrentUserProviderTest"`
  Expected: Kompilierfehler (`getActorUserId` fehlt).

- [ ] **Step 3: Umsetzen.** KDoc der Klasse neu fassen: erklärt Eigentümer gegen Aufrufer und dass Audit-Felder immer den Aufrufer tragen.

- [ ] **Step 4: Tests laufen lassen**

  Run: wie Step 2. Expected: alle bestehenden und die 4 neuen Tests grün.

---

### Task 5: DelegationGrantChangedEvent

**Files:**
- Create: `src/main/kotlin/de/mosimtech/common/delegation/event/DelegationGrantChangedEvent.kt`
- Test: `src/test/kotlin/de/mosimtech/common/delegation/event/DelegationGrantChangedEventTest.kt`

**Interfaces:**
- Produces:
  ```kotlin
  data class DelegationGrantChangedEvent(
      val delegationId: String,
      val module: String,
      val grantorId: String,
      val granteeId: String,
      val grantorDisplayName: String,
      val granteeDisplayName: String,
      val scopes: List<String>,
      val active: Boolean,
      val validUntil: Instant?,
      val grantVersion: Long,
      val changedAt: Instant,
  )
  ```
  IDs sind URN-Zeichenketten (`urn:user:<realm>:<uuid>`, `urn:delegation:<uuid>`), damit das DTO ohne eigenen Serialisierer über RabbitMQ und REST geht. Dasselbe DTO ist die Zeile der Abgleich-Antwort des User-Service.

- [ ] **Step 1: Test schreiben**

  Prüfen, ob im Testklassenpfad des Moduls ein Jackson-`ObjectMapper` mit Kotlin- und JavaTime-Modul verfügbar ist (`spring-boot-test` bringt ihn nicht zwingend mit). Wenn ja:
  - `` `survives a json round trip including null validUntil` ``: Objekt mit `validUntil = null`, `scopes = listOf("plan:read", "plan:write")`, `grantVersion = 7` → serialisieren, lesen, `assertEquals`.

  Wenn nein: keine Jackson-Abhängigkeit nur für diesen Test einführen. Stattdessen `` `carries the full grant state` `` mit direkter Konstruktion und Feldvergleich, und im Bericht vermerken, dass der JSON-Rundlauf erst beim ersten Verbraucher (User-Service) getestet wird.

- [ ] **Step 2: Fehlschlag bestätigen, Step 3: Klasse anlegen** mit KDoc: zustandstragend, `grantVersion` als Ordnungsmerkmal (kleinere oder gleiche Version wird beim Empfänger verworfen), `active` nur bei angenommener, nicht ruhender Freigabe wahr.

- [ ] **Step 4: Test laufen lassen**

  Run: `./gradlew :common-util-delegation:test --tests "*event.DelegationGrantChangedEventTest"`
  Expected: grün.

---

### Task 6: Veraltetes markieren, Fassung, Gesamtlauf

**Files:**
- Modify: `…/DelegationHeaderFilter.kt`, `…/DelegationContextHolder.kt`, `…/uma/UmaPermissionEvaluator.kt`, `…/event/DelegationEvent.kt`
- Modify: `gradle.properties`
- Modify: `C:\workspace-privat\mosimtech\Common\mosimtech-common-util\delegation.md`

- [ ] **Step 1: `@Deprecated` setzen**, jeweils mit Begründung und Ersatz:
  - `DelegationHeaderFilter`: „Übernimmt den Header ungeprüft. In keinem Dienst registrieren." → `access.DelegationAccessFilter`
  - `DelegationContextHolder` → `access.DelegationAccessHolder`
  - `UmaPermissionEvaluator`: „UMA wird nicht verwendet; der Vergleich über Teilzeichenketten ist kein Zugriffsschutz." → `access.DelegatedAccessInterceptor`
  - In `event/DelegationEvent.kt`: `DelegationEvent`, `DelegationEventType`, `DelegationGrantor`, `DelegationGrantee`, `DelegationEventConstants` → `DelegationGrantChangedEvent`. `DelegationModule` bleibt ohne Markierung.

  Die bestehenden Tests dieser Klassen bleiben unverändert; wo der Compiler die Nutzung veralteter Typen in Tests anmahnt, `@Suppress("DEPRECATION")` an der Testklasse.

- [ ] **Step 2: `delegation.md` im Wurzelverzeichnis** am Anfang um einen deutlich abgesetzten Hinweis ergänzen: Das Dokument beschreibt den verworfenen UMA-Ansatz; gültig ist die Spec (Pfad siehe oben). Den übrigen Text nicht umschreiben.

- [ ] **Step 3: Fassung** in `gradle.properties` auf `version=3.9.0`.

- [ ] **Step 4: Prüfen, dass die Fassung im Paketspeicher frei ist.**

  Run: `curl -s -o /dev/null -w "%{http_code}" https://dev.momasoft.de/nexus/repository/maven-releases/de/mosimtech/common-util-delegation/3.9.0/common-util-delegation-3.9.0.pom`
  Expected: `404`. Bei `200` anhalten und berichten, nichts weiter ändern.

- [ ] **Step 5: Volle Testsuite aller Module**

  Run: `./gradlew clean build`
  Expected: `BUILD SUCCESSFUL`, keine fehlgeschlagenen Tests. Im Bericht die Zahl der Tests in `common-util-delegation` vor und nach der Änderung nennen.

- [ ] **Step 6: Additivität belegen**

  Run: `git diff --stat` und `git diff -- common-util-delegation/src/main`
  Expected: In bestehenden Dateien unter `src/main` nur hinzugefügte `@Deprecated`-Zeilen und KDoc, dazu die Änderung an `CurrentUserProvider`. Keine entfernte oder geänderte öffentliche Signatur.

---

## Übergabe an die Folgepläne

Diese Namen verbrauchen die Pläne für User-Service (MUS-53) und ShiftCalc (MSC-65, MSC-66):

- `de.mosimtech.common.delegation.access.DelegationContext`, `DelegationAccessHolder`, `DelegationGrantResolver`, `DelegationAccessFilter`, `DelegatedAccess`, `DelegatedAccessInterceptor`
- `de.mosimtech.common.delegation.CurrentUserProvider.getEffectiveUserId()` / `getActorUserId()`
- `de.mosimtech.common.delegation.event.DelegationGrantChangedEvent`

Veröffentlicht wird durch den Jenkins-Lauf auf `main` nach dem Commit des Auftraggebers. Die Folgepläne setzen `common = "3.9.0"` erst, wenn die Fassung im Nexus liegt.
