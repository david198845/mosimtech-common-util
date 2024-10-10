package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class UrnDeserializerTest {

    private val jsonFactory = JsonFactory(ObjectMapper())

    @Test
    fun testDeserialize_Urn_object() {
        val input = "{ \"urn\" : {\n" +
                "    \"namespace\": \"user\",\n" +
                "    \"nss\": \"0bfad384-1e92-4395-b7ad-022192fc46cd\",\n" +
                "    \"snid\": [\"momasoft\"]\n" +
                "  } }"
        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val result = deserializer.deserialize(parser, null)
        assertNotNull(result)
        assertEquals("user", result?.namespace)
        assertEquals("0bfad384-1e92-4395-b7ad-022192fc46cd", result?.nameSpecificString)
    }

    @Test
    fun testDeserialize_withValidUrnString() {
        val input = "{ \"urn\" : \"urn:test:nss\" }"
        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val result = deserializer.deserialize(parser, null)

        assertNotNull(result)
        assertEquals("test", result?.namespace)
        assertEquals("nss", result?.nameSpecificString)
    }

    @Test
    fun testDeserialize_withIncompleteUrnString() {
        val input = "{ \"urn\" : \"urn:test:\" }"

        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val result = deserializer.deserialize(parser, null)
        assertNull(result)
    }

    @Test
    fun testDeserialize_withEmptyUrnString() {
        val input = "{ \"urn\" : \"\" }"

        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val nullResult = runCatching { deserializer.deserialize(parser, null) }.getOrNull()

        assertNull(nullResult)
    }
}