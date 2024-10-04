package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import de.modulix.mosimtech.model.urn.Urn
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertNull

internal class UrnDeserializerTest {

    private val jsonFactory = JsonFactory(ObjectMapper())


    @Test
    fun testDeserialize_withValidUrnString() {
        val input = "{ \"urn\" : \"urn:test:nss\" }"
        Urn.registerNamespace(TestNamespace.Test)
        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val result = deserializer.deserialize(parser, null)

        assertNotNull(result)
        assertEquals("test", result.namespace.identifier)
        assertEquals("nss", result.nss)
    }

    @Test
    fun testDeserialize_withIncompleteUrnString() {
        val input = "{ \"urn\" : \"urn:test:\" }"
        Urn.registerNamespace(TestNamespace.Test)
        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val result = deserializer.deserialize(parser, null)
        assertNotNull(result)
        assertEquals("test", result.namespace.identifier)
        assertEquals("", result.nss)
    }

    @Test
    fun testDeserialize_withEmptyUrnString() {
        val input = "{ \"urn\" : \"\" }"
        Urn.registerNamespace(TestNamespace.Test)
        val parser = jsonFactory.createParser(input)
        parser.nextToken()
        parser.nextToken()
        parser.nextToken()
        val deserializer = UrnDeserializer()
        val nullResult = runCatching { deserializer.deserialize(parser, null) }.getOrNull()

        assertNull(nullResult)
    }
}