package de.mosimtech.common.core.serializer

import de.mosimtech.common.core.urn.Urn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import kotlin.test.assertNull

internal class UrnDeserializerTest {

    private val objectMapper = JsonMapper.builder().build()

    @Test
    fun testDeserialize_Urn_object() {
        val input = """{"namespace":"user","nss":"0bfad384-1e92-4395-b7ad-022192fc46cd","snid":["momasoft"]}"""
        val result = objectMapper.readValue(input, Urn::class.java)
        assertNotNull(result)
        assertEquals("user", result?.namespace)
        assertEquals("0bfad384-1e92-4395-b7ad-022192fc46cd", result?.nameSpecificString)
    }

    @Test
    fun testDeserialize_withValidUrnString() {
        val result = objectMapper.readValue("\"urn:test:nss\"", Urn::class.java)

        assertNotNull(result)
        assertEquals("test", result?.namespace)
        assertEquals("nss", result?.nameSpecificString)
    }

    @Test
    fun testDeserialize_withIncompleteUrnString() {
        val result = objectMapper.readValue<Urn?>("\"urn:test:\"", Urn::class.java)
        assertNull(result)
    }

    @Test
    fun testDeserialize_withEmptyUrnString() {
        val nullResult = runCatching { objectMapper.readValue<Urn?>("\"\"", Urn::class.java) }.getOrNull()
        assertNull(nullResult)
    }
}
