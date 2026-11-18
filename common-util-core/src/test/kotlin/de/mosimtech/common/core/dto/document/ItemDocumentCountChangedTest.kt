package de.mosimtech.common.core.dto.document

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant

internal class ItemDocumentCountChangedTest {

    private val objectMapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

    @Test
    fun roundTripsThroughJackson() {
        val event = ItemDocumentCountChanged(
            externalUrn = "urn:momasoft.financial:expense:abc",
            userId = "urn:momasoft:user:123",
            documentCount = 3,
            observedAt = Instant.parse("2026-11-18T10:15:30Z"),
        )

        val json = objectMapper.writeValueAsString(event)
        val result = objectMapper.readValue(json, ItemDocumentCountChanged::class.java)

        assertEquals(event, result)
        assertTrue(json.contains("\"2026-11-18T10:15:30Z\""), json)
    }
}
