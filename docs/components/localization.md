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
| | `LocalizedFormats` | Beträge, Zahlen, Daten in der Schreibweise der Sprache |
| | `LocalizedCatalogCheck` | Prüft Schlüssel, Platzhalter und Pluralformen aller Sprachen |
| `common-util-security` | `RequestLanguage`, `RequestTexts` | Sprache einer REST-Anfrage: Token-Claim `locale` → `Accept-Language` → Deutsch |
| | `ContentLanguageResponseAdvice` (Auto-Config) | Setzt `Content-Language`, sobald eine übersetzte Meldung in der Antwort steckt |
| `common-util-user-service` | `RecipientLanguageResolver` (Auto-Config) | Kontosprache eines Empfängers beim User Service, mit Cache |

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
@Bean
fun serviceAccessTokenSupplier(tokens: ServiceTokenProvider) =
    ServiceAccessTokenSupplier { tokens.serviceToken }
```

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

## 4. Katalog prüfen

```kotlin
@Test
fun `message catalog is complete in every language`() {
    assertEquals(emptyList<String>(), ApiMessages.CATALOG.problems())
}
```

Gemeldet werden fehlende Sprachdateien, fehlende oder überzählige Schlüssel, abweichende
Platzhalter und fehlende Pluralformen.
