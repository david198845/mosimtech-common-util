package de.mosimtech.common.vault.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper
import java.util.TimeZone

/**
 * Deterministischer ObjectMapper für die Signatur-Serialisierung asynchroner Nachrichten.
 *
 * Signer und Verifier nutzen ausschließlich diesen Mapper, damit die signierten Bytes
 * unabhängig von DTO-Version und service-spezifischer Jackson-Config sind:
 *  - NON_NULL: neu hinzugefügte optionale null-Felder ändern die Bytes nicht
 *  - Properties + Map-Keys alphabetisch sortiert: stabile Reihenfolge
 *  - ISO-8601 (kein Epoch) für Zeittypen: stabil und voll präzise
 *  - alle Zeiten in UTC: siehe unten
 *
 * Zur UTC-Festlegung: Die Signatur wird über das RE-SERIALISIERTE JSON gebildet — der Empfänger
 * deserialisiert den Payload und serialisiert ihn erneut. Ein ZonedDateTime mit lokaler Zone
 * ergibt dabei andere Bytes als beim Sender ("…T12:00+02:00" wird beim Deserialisieren zu
 * "…T10:00Z" normalisiert), und die Signatur schlägt fehl, obwohl niemand manipuliert hat.
 * Beide Seiten müssen deshalb dieselbe Zeitzone schreiben.
 */
object CanonicalMessageMapper {

    val mapper: ObjectMapper = create()

    /**
     * Serialisierung vor der UTC-Festlegung (bis 3.3.7). Ausschliesslich fuer die VERIFIKATION:
     * Ein Empfaenger mit neuer Bibliothek muss Nachrichten annehmen, die ein Sender mit alter
     * Bibliothek signiert hat — sonst muessten alle Services gleichzeitig aktualisiert werden.
     * Signiert wird immer nur mit [mapper].
     */
    val legacyMapper: ObjectMapper = createLegacy()

    fun create(): ObjectMapper =
        legacyBuilder()
            .defaultTimeZone(TimeZone.getTimeZone("UTC"))
            .enable(DateTimeFeature.WRITE_DATES_WITH_CONTEXT_TIME_ZONE)
            .enable(DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .build()

    fun createLegacy(): ObjectMapper = legacyBuilder().build()

    private fun legacyBuilder() =
        JsonMapper.builder()
            .findAndAddModules()
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
}
