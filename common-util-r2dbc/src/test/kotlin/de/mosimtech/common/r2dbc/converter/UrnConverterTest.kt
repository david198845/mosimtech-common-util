package de.mosimtech.common.r2dbc.converter

import de.mosimtech.common.core.urn.Urn
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UrnConverterTest {

    private val urnReadingConverter = UrnReadingConverter()
    private val urnWritingConverter = UrnWritingConverter()

    @Test
    fun `convert String to Urn`() {
        // Given
        val urnString = "urn:mosimtech:example:12345"
        
        // When
        val urn = urnReadingConverter.convert(urnString)
        
        // Then
        assertEquals("mosimtech", urn.namespace)
        assertEquals(listOf("example", "12345"), urn.subNamespaceIdentifier)
    }




}
