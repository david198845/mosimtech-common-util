# Design: Deterministische (kanonische) Signatur-Serialisierung für SignedMessageEnvelope

**Datum:** 2026-06-19
**Modul:** `common-util-vault` (`de.mosimtech.common.vault.messaging`)
**Status:** Entwurf zur Freigabe

## 1. Problem

Asynchrone RabbitMQ-Nachrichten werden in eine `SignedMessageEnvelope<T>` verpackt und
über Vault Transit signiert. Die Signatur wird in `buildSigningInput` u.a. über die
**re-serialisierten Payload-Bytes** gebildet:

```kotlin
val payloadBase64 = Base64.getEncoder().encodeToString(objectMapper.writeValueAsBytes(payload))
"$payloadBase64|$issuedAt|${exp ?: "∞"}|$messageId|$serviceAccountToken|${userToken ?: ""}"
```

`VaultTransitMessageVerifier` re-serialisiert den bereits deserialisierten Payload erneut
mit dem **per Spring injizierten App-`ObjectMapper`**. Damit die Signatur passt, müssen
Sender und Empfänger **byte-identisches JSON** für den Payload erzeugen.

Das ist in der Praxis gebrochen:

| | Sender `momasoft-shift-calc` | Empfänger `momasoft-notification-service` |
| --- | --- | --- |
| `common-util-core` (DTO) | 3.3.4 | 3.3.5 |
| `NotificationRequestDTO.androidId` | nicht vorhanden | vorhanden (`String? = null`) |

`androidId` wurde in 3.3.5 (Commit `38fbd02`, 2026-06-15) ergänzt. Der Empfänger
re-serialisiert mit `"androidId":null` (kein `NON_NULL` konfiguriert), der Sender hat das
Feld nie ausgegeben → abweichende Bytes → `transit/verify` → `false` → `SecurityException`
("Vault-Signatur ungültig"). Jede Nachricht schlägt fehl (beobachtet 2026-06-18).

**Tiefere Ursache:** Die Signatur hängt von DTO-Version *und* der ObjectMapper-Config jedes
einzelnen Service ab (Feldreihenfolge, Null-Handling, Datumsformat, registrierte Module).
Das ist über Service-Grenzen hinweg inhärent fragil.

## 2. Ziel & Nicht-Ziele

**Ziel:** Signatur-Bildung und -Prüfung unabhängig von DTO-Version und von der
service-spezifischen ObjectMapper-Config machen, sodass das Hinzufügen optionaler
Felder (Default `null`) sowie Konfig-Drift zwischen Services die Signatur **nicht** brechen.

**Nicht-Ziele:**
- Keine Änderung der öffentlichen `SignedMessageEnvelope<T>`-Form (Wire-Format,
  Listener-/Publisher-Signaturen bleiben unverändert).
- Keine bit-genaue Bindung an die exakten Wire-Bytes (das wäre der separat dokumentierte
  Ansatz 2 mit API-Bruch; bewusst verworfen).

## 3. Lösung: Kanonischer, bibliotheks-interner Serializer

`common-util-vault` definiert einen **eigenen, deterministischen** `ObjectMapper`
(im Folgenden *Canonical Mapper*). Signer **und** Verifier nutzen ausschließlich diesen
für `buildSigningInput` — **nicht** mehr den injizierten App-Mapper. Da beide Seiten denselben
bibliotheks-internen Mapper verwenden, ist die Serialisierung des Payloads unabhängig von
der Spring-Config des jeweiligen Service.

### 3.1 Kanonische Einstellungen

Der Canonical Mapper (Jackson 3 / `tools.jackson`) wird deterministisch konfiguriert:

| Einstellung | Wert | Zweck |
| --- | --- | --- |
| Kotlin-Modul | registriert | Korrekte (De-)Serialisierung der Kotlin-Data-Class-DTOs |
| JavaTime-Modul | registriert | `Instant`/Zeittypen als stabiles ISO-8601 |
| `SerializationFeature.WRITE_DATES_AS_TIMESTAMPS` | aus | ISO-8601 statt Epoch (deterministisch, volle Präzision) |
| `JsonInclude.Include.NON_NULL` | gesetzt | Null-Felder weglassen → neu hinzugefügte optionale `null`-Felder brechen die Signatur nicht |
| `MapperFeature.SORT_PROPERTIES_ALPHABETICALLY` | an | Stabile Feldreihenfolge unabhängig von Kotlin/Jackson-Version |
| `SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS` | an | Stabile Schlüsselreihenfolge für `Map`-Felder (z.B. `data: Map<String,String>`) |
| Einrückung | aus (kompakt) | Keine Whitespace-Varianz |

`Urn` benötigt **keine** zusätzliche Modul-Registrierung: `Urn` ist mit
`@JsonSerialize(using = UrnSerializer::class)` / `@JsonDeserialize(using = UrnDeserializer::class)`
annotiert; jeder Jackson-3-Mapper wendet das automatisch an. `common-util-vault` hängt bereits
über `api(project(":common-util-core"))` an diesen Klassen.

### 3.2 Komponenten

**Neu:** `CanonicalMessageMapper` (Factory in `de.mosimtech.common.vault.messaging`)
- Eine Funktion/`object`, die einen frisch konfigurierten Canonical Mapper liefert
  (`fun create(): ObjectMapper`), bzw. eine Singleton-Instanz bereitstellt.
- Einzige Verantwortung: den deterministischen Mapper gemäß 3.1 erzeugen. Eigenständig
  testbar, ohne Vault/Spring.

**Geändert:** `buildSigningInput` (`SigningInputBuilder.kt`)
- Bleibt inhaltlich gleich (gleiches Trennzeichen-Format, gleiche Felder), nutzt aber den
  übergebenen Mapper weiterhin. Das Format-Layout ändert sich **nicht** — nur die
  *Quelle* des Mappers (Canonical statt App-Mapper) ändert sich beim Aufrufer.

**Geändert:** `VaultTransitMessageSigner` / `VaultTransitMessageVerifier`
- Der Konstruktor-Parameter `objectMapper: ObjectMapper` **entfällt**. Beide bauen intern
  über `CanonicalMessageMapper` ihren Mapper (bzw. teilen die Singleton-Instanz).
- Signatur-/Verify-Logik (Vault `sign`/`verify`, `exp`-Prüfung, `isTokenValidAtIssuance`)
  bleibt unverändert.

> Hinweis: Das Entfernen des `objectMapper`-Konstruktor-Parameters ist eine Änderung der
> *Wiring-API* (betrifft pro Service die `VaultTransitMessagingConfig`-Bean), **nicht** der
> Nachrichten-/Listener-API. Bewusst entfernt statt deprecaten, damit kein Service versehentlich
> wieder seinen App-Mapper unterschiebt und die Determinismus-Garantie aushebelt.

### 3.3 Datenfluss (nachher)

```
Sender (shift-calc, core 3.3.4)
  DTO(ohne androidId) --CanonicalMapper--> kanonisches JSON (NON_NULL, sortiert)
                                            └─> buildSigningInput --> Vault sign --> signature
  Envelope (payload als Wire-JSON via App-Converter) + signature --> RabbitMQ

Empfänger (notification-service, core 3.3.5)
  Wire-JSON --App-Converter--> DTO(androidId = null)
  DTO --CanonicalMapper--> kanonisches JSON (androidId via NON_NULL weggelassen, sortiert)
                            └─> buildSigningInput --> Vault verify(signature) --> TRUE
```

Beide Seiten erzeugen identische kanonische Bytes → Signatur gültig.

## 4. Fehlerbehandlung

Unverändert: `verify` liefert `false` bei ungültiger Signatur oder abgelaufenem `exp`;
`VaultException` wird durchgereicht. Im Listener führt eine fehlgeschlagene Prüfung weiterhin
zu `SecurityException` → `AmqpRejectAndDontRequeueException` (Dead Letter Queue).

## 5. Tests

**Unit (`CanonicalMessageMapper`):**
- Zwei logisch gleiche Objekte mit **unterschiedlicher Feld-/Map-Reihenfolge** erzeugen
  identische Bytes.
- Ein Objekt mit gesetztem optionalem Feld vs. dasselbe ohne (Feld `null`) — das `null`-Feld
  wird weggelassen; ein Objekt **ohne** das Feld und eines **mit** `null` ergeben identische Bytes.
- `Instant` wird deterministisch (ISO-8601, volle Präzision) und round-trip-stabil serialisiert.
- `Urn`-Feld wird als `toUrnString()` serialisiert.

**Regression (Signer ↔ Verifier, Kern des Bugfixes):**
- Signieren über einen Payload, dann Verifizieren über eine Repräsentation, die ein
  **zusätzliches `null`-Feld** enthält (simuliert den 3.3.4→3.3.5-Skew) → `verify` = `true`.
  Umsetzung z.B. über zwei DTO-Varianten in den Testfixtures oder über generische `Map`-Payloads,
  die den Versionsunterschied nachbilden.

**Bestehende Tests:** `VaultTransitMessageSignerTest` / `VaultTransitMessageVerifierTest`
an den entfallenen `objectMapper`-Konstruktorparameter anpassen (Mapper kommt nun intern).

Coverage-Mindestgrenze (80 %) muss erhalten bleiben.

## 6. Rollout

1. `common-util-vault` Änderung implementieren, Versions-Bump (`gradle.properties`).
2. **`momasoft-notification-service`** (Empfänger): `common-util`-Versionen auf den neuen
   Stand heben (vault aktuell 3.3.2, core 3.3.5 → einheitlich neue Version);
   `VaultTransitMessagingConfig` an den geänderten Verifier-Konstruktor anpassen.
3. **`Shift-Time-Tracker/ShiftCalc-Backend`** (Sender): `common`-Version (aktuell 3.3.4) heben;
   `VaultTransitMessagingConfig` an den geänderten Signer-Konstruktor anpassen.
4. Weitere Publisher/Listener von `SignedMessageEnvelope` analog heben (Financial etc.),
   sobald sie produktiv signieren/verifizieren — die Determinismus-Garantie gilt erst, wenn
   beide Seiten den Canonical Mapper nutzen (= neue Lib-Version).

> Übergang: Solange ein Sender noch die alte Lib nutzt und der Empfänger die neue (oder
> umgekehrt), kann die Signatur weiter abweichen. Empfehlung: Empfänger und zugehörige
> Sender möglichst gemeinsam ausrollen. Für den akuten Fall reicht das Paar
> notification-service ↔ shift-calc.

## 7. Bewusst verworfene Alternative

**Ansatz 2 – exakte Wire-Bytes signieren:** Payload als rohes vorab-serialisiertes JSON im
Envelope führen, Verify ohne Re-Serialisierung. Maximal robust, aber API-Bruch an
`SignedMessageEnvelope<T>` → alle Listener/Publisher müssten angepasst werden. Für die
aufgetretene Fehlerklasse (optionale Feld-Zusätze, Config-Drift) liefert Ansatz 1 dieselbe
Robustheit ohne diesen Blast-Radius.
