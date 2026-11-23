# Mehrsprachigkeit (Kontosprache)

Dienste erzeugen Texte für Menschen (REST-Fehlermeldungen, Benachrichtigungen) in der
**Kontosprache** des Nutzers. Unterstützt sind Deutsch (Vorgabe), Englisch, Spanisch, Russisch und
Ukrainisch (`AccountLanguage`).

Alle Bausteine richten sich über Auto-Configuration ein; ein Dienst legt nur seine Textdateien an
und stellt, falls er Benachrichtigungen ohne Anfrage verschickt, sein Dienstkonto-Token bereit.

| Modul | Baustein | Wofür |
| --- | --- | --- |
| `common-util-core` | `AccountLanguage`, `PluralCategory` | Sprachcodes, CLDR-Pluralregeln |
| | `LocalizedTexts`, `LocalizedTextCatalog` | Textkatalog aus `.properties`, `text()` und `plural()` |
| | `LocalizedFormats` | Beträge, Zahlen, Daten, Uhrzeiten und Größen in der Schreibweise der Sprache bzw. des Profils |
| | `FormatPreferences`, `RegionFormatDefaults` | Formatvorgaben aus dem Profil, Auflösung von `AUTO` (Abschnitt 5) |
| | `MeasureUnits`, `DisplayUnits` | Einheiten (JSR 385 / Indriya), Umrechnung in die Profileinheit (Abschnitt 6) |
| | `LocalizedCatalogCheck` | Prüft Schlüssel, Platzhalter und Pluralformen aller Sprachen |
| `common-util-jpa` | `QuantityConverter`, `MassQuantityConverter`, `TemperatureQuantityConverter` | Größe als „50 kg“ in einer `VARCHAR`-Spalte |
| `common-util-security` | `RequestLanguage`, `RequestTexts` | Sprache einer REST-Anfrage: Token-Claim `locale` → `Accept-Language` → Deutsch |
| | `RequestFormats`, `FormatPreferenceClaims` | Formatvorgaben einer REST-Anfrage aus den Token-Claims |
| | `ContentLanguageResponseAdvice` (Auto-Config) | Setzt `Content-Language`, sobald eine übersetzte Meldung in der Antwort steckt |
| `common-util-user-service` | `RecipientLanguageResolver` (Auto-Config) | Kontosprache eines Empfängers beim User Service, mit Cache |
| | `RecipientFormatResolver` (Auto-Config) | Alle Formatvorgaben eines Empfängers (inkl. Währung, Zeitzone), mit Cache |

## 1. Textdateien anlegen

```
src/main/resources/i18n/messages.properties      # Deutsch, Referenz
src/main/resources/i18n/messages_en.properties
src/main/resources/i18n/messages_es.properties
src/main/resources/i18n/messages_ru.properties
src/main/resources/i18n/messages_uk.properties
```

- Syntax ist `java.text.MessageFormat`: Platzhalter `{0}`, ein Hochkomma wird `''` geschrieben.
- Parameter werden vor dem Einsetzen in Text gewandelt; Zahlen, Beträge und Daten vorher mit
  `LocalizedFormats` formatieren.
- Pluraltexte stehen unter `<schlüssel>.one` / `.few` / `.many` / `.other`, die Menge ist `{0}`.
  Deutsch, Englisch und Spanisch brauchen `one` und `other`, Russisch und Ukrainisch zusätzlich
  `few` und `many`. Andere Schlüssel dürfen deshalb nicht auf diese Wörter enden.

```properties
expense.notFound=Ausgabe {0} nicht gefunden
expense.overdue.one=1 Ausgabe überfällig
expense.overdue.other={0} Ausgaben überfällig
```

## 2. REST-Meldungen

```kotlin
object ApiMessages {
    val CATALOG = LocalizedTexts("i18n/messages", ApiMessages::class.java.classLoader)
    private val texts = RequestTexts(CATALOG)

    fun text(key: String, vararg args: Any): String = texts.text(key, *args)
}

throw IllegalOperationException(ApiMessages.text("expense.notFound", id))
```

Die Antwort trägt danach `Content-Language`. Oberflächen übernehmen eine Servermeldung nur, wenn
diese Angabe zu ihrer Sprache passt; deshalb **jede** nutzersichtbare Meldung über den Katalog
erzeugen, nie als festen String.

## 3. Benachrichtigungen ohne Anfrage

Für Jobs und RabbitMQ-Ereignisse gibt es keine Anfrage, also auch keinen Token-Claim. Die Sprache
kommt aus dem User Service (`GET /v1/users/{id}/locale`).

```kotlin
@Bean(name = ["backgroundOAuth2AuthorizedClientManager"])
fun backgroundClientManager(registrations: ClientRegistrationRepository) =
    BackgroundAuthorizedClientManagers.clientCredentials(registrations)   // common-util-security

@Bean
fun serviceAccessTokenSupplier(
    @Qualifier("backgroundOAuth2AuthorizedClientManager") manager: OAuth2AuthorizedClientManager,
) = ClientCredentialsTokenSupplier(manager, "service-account")             // Registrierungs-ID
```

`ClientCredentialsTokenSupplier` ist ein `ServiceAccessTokenSupplier` (braucht
`spring-security-oauth2-client` im Dienst). Ein eigenes Lambda
`ServiceAccessTokenSupplier { … }` funktioniert weiterhin.

Damit legt die Auto-Configuration `RecipientLanguageResolver` an:

```kotlin
val language = recipientLanguage.resolve(userId)          // nie Ausnahme, sonst Deutsch
val texts = NOTIFICATIONS.forLanguage(language)
val formats = LocalizedFormats(language)
gateway.send(title = texts.plural("expense.overdue", count), body = formats.money(amount), …)
```

Voraussetzungen in Keycloak und Consul:

- Das Dienstkonto hat am Client `mosimtech-user-service` die Rolle `system:invoke`, samt
  Scope-Mapping, wenn der Client `fullScopeAllowed: false` hat.
- Die Client-ID steht in `mosimtech.security.jwt.allowed-azp-clients` des User Service.

Eigenschaften (optional):

| Eigenschaft | Vorgabe |
| --- | --- |
| `mosimtech.user-service.base-url` | `https://mosimtech-user-service/api` (mit Spring Cloud LoadBalancer über Consul aufgelöst) |
| `mosimtech.user-service.language-cache-minutes` | `10` |

Der Abruf blockiert höchstens 2 s. Läuft der Versand in einer `@Transactional`-Methode, die
Sprache vorher außerhalb der Transaktion holen (z. B. in der Schleife des Jobs). Der Aufruf in der
Transaktion trifft dann nur den Cache.

### Ohne Dienstkonto: Vorgaben statt Fehler

Stellt der Dienst **keinen** `ServiceAccessTokenSupplier` bereit (z. B. weil sein Dienstkonto per
Schalter aus ist), legt `RecipientDefaultsAutoConfiguration` trotzdem `RecipientLanguageResolver`
und `RecipientFormatResolver` an. Sie fragen den User Service nie und liefern die Vorgaben
(Deutsch, alles `AUTO`, Währung/Zeitzone unbekannt). Aufrufer spritzen die Resolver also direkt
ein – kein `ObjectProvider`, kein eigener Ersatzweg. Eigene Beans gehen vor; abschaltbar mit
`mosimtech.user-service.defaults-fallback.enabled=false`. Von Hand: `RecipientFormatResolver.defaultsOnly()`.

### Hintergrundläufe für einen Empfänger: `using`

Rechnet ein Lauf ohne Anfrage für einen bestimmten Nutzer (Stream nach einer Änderung, Job), legt
er Sprache und Formatvorgaben für seinen Thread fest. Alles, was darin `RequestLanguage.current()`
bzw. `RequestFormats.current()` fragt, bekommt diese Werte statt der (fehlenden) Claims:

```kotlin
val preferences = recipientFormats.resolve(userId)
RequestLanguage.using(preferences.language) {
    RequestFormats.using(preferences) { dashboardAssembler.build(userId) }
}
```

Nach dem Block gelten wieder die vorigen Werte – auch bei Verschachtelung und Ausnahmen; andere
Threads sehen sie nie. `RequestFormats.current(profile)` ergänzt fehlende Währung/Zeitzone auch
unter `using` aus `profile`.

## 4. Katalog prüfen

```kotlin
@Test
fun `message catalog is complete in every language`() {
    assertEquals(emptyList<String>(), ApiMessages.CATALOG.problems())
}
```

Gemeldet werden fehlende Sprachdateien, fehlende oder überzählige Schlüssel, abweichende
Platzhalter und fehlende Pluralformen.

## 5. Formatvorgaben aus dem Profil

Der User Service (ab 0.6.0) führt je Nutzer Datum, Uhrzeit, Einheiten, Wochenbeginn, Währung und
Zeitzone. `FormatPreferences` bündelt sie:

| Feld | Token-Claim | REST `GET /v1/users/{id}/locale` | Werte |
| --- | --- | --- | --- |
| `dateFormat` | `date_format` | `dateFormat` | `AUTO`, `DMY_DOT` 31.12.2026, `DMY_SLASH` 31/12/2026, `MDY_SLASH` 12/31/2026, `ISO` 2026-12-31 |
| `timeFormat` | `time_format` | `timeFormat` | `AUTO`, `H24` 15:05, `H12` 3:05 PM |
| `unitSystem` | `unit_system` | `unitSystem` | `AUTO`, `METRIC` (kg, °C), `IMPERIAL` (lb, °F) |
| `firstDayOfWeek` | `first_day_of_week` | `firstDayOfWeek` | `AUTO`, `MONDAY`, `SUNDAY` |
| `timezone` | `zoneinfo` (sobald Keycloak ihn liefert) | `timezone` | IANA, unbekannt → `Europe/Berlin` |
| `currency` | – (bewusst nicht im Token) | `currency` | ISO 4217, unbekannt → `EUR` |

Ein fehlender oder unbekannter Wert ist `AUTO`. `AUTO` wird über das **Land der Sprache**
aufgelöst (`RegionFormatDefaults`), mit denselben Tabellen wie `resolve-formats.ts` in ui-core —
Änderungen immer an beiden Stellen:

1. Land = Länderteil des Sprach-Tags (`de-AT` → AT, `en_CA` → CA, `es-419` → 419). Ohne Land das
   Land der Sprache: de→DE, en→**GB**, es→ES, ru→RU, uk→UA, fr→FR, it→IT, pt→PT, nl→NL, pl→PL,
   cs→CZ, sv→SE, lt→LT, tr→TR, ja→JP, ko→KR, he→IL. Ohne Sprache gilt `de`.
2. Datum: `MDY_SLASH` für US; `ISO` für SE, LT, CA; `DMY_SLASH` für GB, IE, AU, NZ, IN, ES, FR,
   BE, LU, IT, PT, BR, GR, MX, AR, CL, CO, PE, VE, UY, EC; sonst `DMY_DOT`.
3. Uhrzeit: `H12` für US, CA, AU, NZ, IN, PH, PK, EG; sonst `H24`.
4. Einheiten: `IMPERIAL` für US, LR, MM; sonst `METRIC`.
5. Wochenbeginn: `SUNDAY` für US, CA, MX, BR, JP, KR, IL, PH, IN; sonst `MONDAY`.

Englisch ohne Land ist also britisch; amerikanisch wird es erst mit `en-US` oder ausdrücklich
gewählten Formaten. Ein gewählter Wert (≠ `AUTO`) gilt immer.

**Ohne `locale`-Claim gilt `de`, wie in ui-core** (DMY_DOT, H24, METRIC, MONDAY).
`RequestFormats` wertet `Accept-Language` bewusst nicht aus — sonst zeigten Backend und
Oberfläche bei fehlendem Claim verschiedene Formate. Texte (`RequestTexts`) folgen weiterhin
`RequestLanguage` mit `Accept-Language`.

Bei `H12` steht die Tageszeit-Kennung der Sprache hinter der Uhrzeit. Das JDK nimmt sie aus
CLDR: de und ru `3:05 PM`, uk `3:05 пп` / `9:05 дп`, es `3:05 p. m.` (mit geschütztem
Leerzeichen). Browser nutzen über ICU dieselben CLDR-Daten; im Browser gegengeprüft ist das
nicht. Weicht eine Kennung ab, gilt die Fassung von ui-core als Maßstab.

```kotlin
// REST-Anfrage: Claims des Tokens, Sprache wie RequestLanguage
val formats = RequestFormats.formats()
formats.date(LocalDate.of(2026, 12, 31))        // 31.12.2026 / 12/31/2026 / …
formats.time(LocalTime.of(15, 5))               // 15:05 / 3:05 PM
formats.dateTime(Instant.now())                 // "<Datum> <Uhrzeit>" in der Zeitzone

// Währung (und Zeitzone ohne zoneinfo-Claim) stehen nicht im Token:
val withProfile = RequestFormats.current(recipientFormats.resolve(userId))
withProfile.currencyOrDefault()                 // Vorgabe für neue Einträge

// Ohne Anfrage (Job, RabbitMQ): alles aus dem User Service, mit Cache, wirft nie
val push = recipientFormats.formats(userId)
```

`LocalizedFormats(language)` (nur Sprache) behält bei `date()` das bisherige mittlere Format der
Sprache („Nov 22, 2026“); erst `LocalizedFormats(preferences)` formatiert Daten nach dem Profil.

Der `RecipientFormatResolver` entsteht wie der `RecipientLanguageResolver`, sobald eine
`ServiceAccessTokenSupplier`-Bean da ist. Cache: `mosimtech.user-service.format-cache-minutes`
(Vorgabe wie `language-cache-minutes`). Fällt der User Service aus, gilt `FormatPreferences()`
(Deutsch, alles `AUTO`, Währung/Zeitzone unbekannt) für eine Minute. Abgelaufene Einträge
entfernt der Cache beim nächsten neuen Eintrag (beide Resolver).

## 6. Größen und Einheiten (JSR 385)

Größen sind `javax.measure.Quantity` (Implementierung Indriya), analog zu `MonetaryAmount`.
`MeasureUnits` liefert die Einheiten der Plattform: `KILOGRAM`, `GRAM`, `POUND`
(exakt 0,45359237 kg), `CELSIUS`, `FAHRENHEIT`, `KELVIN`.

**Speichern** — in der Einheit der Eingabe, ohne Umrechnung:

```kotlin
@Convert(converter = MassQuantityConverter::class)
@Column(name = "bag_weight", nullable = false)
lateinit var bagWeight: Quantity<Mass>                 // Spalte: "15 kg" oder "33 lb"
```

- Spalte `VARCHAR(50)`, Wert immer mit Punkt: `50 kg`, `110 lb`, `21.5 °C`, `70.7 °F`.
- Gelesen wird nur genau dieses Format. Eine nackte Zahl, `50,5 kg` oder eine unbekannte Einheit
  sind ein `IllegalArgumentException`. Bestandsspalten mit reinen Zahlen bekommen ihre Einheit
  per Migration (`UPDATE … SET bag_weight = bag_weight || ' kg'`).
- `MassQuantityConverter` / `TemperatureQuantityConverter` lehnen Größen anderer Art ab;
  `QuantityConverter` nimmt jede bekannte Einheit.

**Anzeigen** — immer in der Einheit des Profils:

```kotlin
val shown = DisplayUnits.toDisplay(bagWeight, preferences)   // 15 kg → 33.069… lb bei IMPERIAL
DisplayUnits.round(shown, 1)                                  // Runden ist Sache des Aufrufers
formats.displayQuantity(bagWeight)                            // "33,1 lb" (de) / "33.1 lb" (en)
DisplayUnits.toMetric(setpoint)                               // intern in kg / °C rechnen
```

**Messwert oder Abstand?** Eine Temperatur ist entweder ein Messwert (Ist-, Soll-, Außentemperatur)
oder ein Abstand (Hysterese, Abweichung vom Soll, Anstieg pro Stunde):

| Was | Funktion | 2 °C bei IMPERIAL |
| --- | --- | --- |
| Messwert | `DisplayUnits.toDisplay` (mit Nullpunkt) | 35,6 °F |
| Abstand | `DisplayUnits.toDisplayDifference` (nur × 9/5) | 3,6 °F |

```kotlin
DisplayUnits.toDisplayDifference(hysteresisCelsius, UnitSystem.IMPERIAL)   // 2 → 3.6 °F
```

Der `QuantityConverter` speichert die Skala nicht: Gelesene Größen sind immer absolut
(`Scale.ABSOLUTE`). Eine gespeicherte Hysterese muss der Dienst als Abstand kennen und mit
`toDisplayDifference` umrechnen.
