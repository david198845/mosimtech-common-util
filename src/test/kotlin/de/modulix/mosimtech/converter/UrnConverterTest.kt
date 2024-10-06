package de.modulix.mosimtech.converter

import de.modulix.mosimtech.model.urn.Urn
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class UrnConverterTest {

    private val urnConverter = object : UrnConverter() {}

    private val testNamespace = TestNamespace.Test

    @Test
    fun `convertToEntityAttribute should parse URN string correctly`() {

        val urnMock: Urn = mock {
            on { namespace } doReturn testNamespace.identifier
            on { nss } doReturn "testNid"
            on { nid } doReturn setOf("test")
            on { toUrnString() } doReturn "urn:test:test:testNid"
        }

        val dbData = "urn:test:test:testNid"

        val urn = urnConverter.convertToEntityAttribute(dbData)

        assertEquals(urnMock.namespace, urn?.namespace)
        assertEquals(urnMock.nss, urn?.nss)
        assertEquals(urnMock.nid, urn?.nid)
    }

    @Test
    fun `convertToEntityAttribute should return null when dbData is null`() {
        val urn = urnConverter.convertToEntityAttribute(null)
        assertNull(urn)
    }

    @Test
    fun `convertToDatabaseColumn should convert URN to string correctly`() {
        val urnMock: Urn = mock {
            on { toUrnString() } doReturn "urn:test:test:testNid"
        }

        val result = urnConverter.convertToDatabaseColumn(urnMock)

        assertEquals("urn:test:test:testNid", result)
    }

    @Test
    fun `convertToDatabaseColumn should return null when attribute is null`() {
        val result = urnConverter.convertToDatabaseColumn(null)

        assertNull(result)
    }
}
