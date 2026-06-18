package de.mosimtech.common.vault.messaging

import com.fasterxml.jackson.annotation.JsonInclude
import tools.jackson.databind.MapperFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.json.JsonMapper

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
            .findAndAddModules()
            .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .disable(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build()
}
