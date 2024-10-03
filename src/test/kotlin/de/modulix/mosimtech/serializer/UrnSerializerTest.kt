package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import de.modulix.mosimtech.model.urn.Urn
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringWriter

/**
 * Test class for the UrnSerializer class.
 */
class UrnSerializerTest {

    /**
     * Serializer used in tests.
     */
    private val serializer = UrnSerializer()

    /**
     * Test that serialization produces correct results.
     */
    @Test
    fun testSerialize() {
        val urn = Urn(TestNamespace.Test, "abc", "def")
        val writer = StringWriter()
        val generator: JsonGenerator = JsonFactory().createGenerator(writer)

        serializer.serialize(urn, generator, null)
        generator.close()

        Assertions.assertEquals("\"urn:Test:def:abc\"", writer.toString())
    }

    /**
     * Test that serializer throws exception for null urn
     */
    @Test
    fun testSerializeThrowsExceptionWhenUrnIsNull() {
        val exception = Assertions.assertThrows(Exception::class.java) {
            serializer.serialize(null, JsonFactory().createGenerator(StringWriter()), null)
        }

        Assertions.assertEquals("given urn is null", exception.message)
    }

    /**
     * Test that serializer does not throw exception for urns with null nid
     */
    @Test
    fun testSerializeDoesNotThrowExceptionWhenNidIsNull() {
        val urnWithoutNid = Urn(TestNamespace.Test, "abc")
        val stringWriter = StringWriter()
        val generatorWithoutNid: JsonGenerator = JsonFactory().createGenerator(stringWriter)
        Assertions.assertDoesNotThrow {
            serializer.serialize(urnWithoutNid, generatorWithoutNid, null)
            generatorWithoutNid.flush() // Ensure all content is written to the StringWriter
        }
        Assertions.assertEquals("\"urn:Test:abc\"", stringWriter.toString())
    }
}