package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.model.urn.Urn
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

internal class UrnSerializerTest {

    /*
    * Class under test: UrnSerializer
    * Method under test: serialize
    *
    * The UrnSerializer::serialize method takes a Urn instance, a JsonGenerator, and a JsonSerializerProvider,
    * and converts the Urn into its string representation, and writes it to the JsonGenerator.
    */

    private val jsonGenerator: JsonGenerator = mock(JsonGenerator::class.java)
    private val serializerProvider: SerializerProvider = mock(SerializerProvider::class.java)

    private val urnSerializer = UrnSerializer()

    @Test
    fun `serialize given urn is null`() {
        assertThrows(Exception::class.java) {
            urnSerializer.serialize(null, jsonGenerator, serializerProvider)
        }
    }

    @Test
    fun `serialize given urn is not null`() {
        val urn = Urn(DefaultNamespace.Undefined, "testNSS")
        val urnString = urn.toUrnString()

        doNothing().`when`(jsonGenerator).writeString(urnString)

        urnSerializer.serialize(urn, jsonGenerator, serializerProvider)

        verify(jsonGenerator).writeString(urnString)
    }
}