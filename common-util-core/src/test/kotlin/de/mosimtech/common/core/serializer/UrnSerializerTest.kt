package de.mosimtech.common.core.serializer

import de.mosimtech.common.core.namespace.DefaultNamespace
import de.mosimtech.common.core.urn.Urn
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext

internal class UrnSerializerTest {

    /*
    * Class under test: UrnSerializer
    * Method under test: serialize
    *
    * The UrnSerializer::serialize method takes a Urn instance, a JsonGenerator, and a JsonSerializerProvider,
    * and converts the Urn into its string representation, and writes it to the JsonGenerator.
    */

    private val jsonGenerator: JsonGenerator = mock(JsonGenerator::class.java)
    private val serializerProvider: SerializationContext = mock(SerializationContext::class.java)

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

        urnSerializer.serialize(urn, jsonGenerator, serializerProvider)

        verify(jsonGenerator).writeString(urnString)
    }
}
