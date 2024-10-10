package de.modulix.mosimtech.converter.jpa

import de.modulix.mosimtech.database.base.urn.Urn
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class UrnStringConverterTest {

    private val urnConverter = object : UrnStringConverter() {}

    private val testNamespace = TestNamespace.Test

    @Test
    fun `convertToEntityAttribute should parse URN string correctly`() {

        val urnMock: Urn = mock {
            on { namespace } doReturn testNamespace.identifier
            on { nameSpecificString } doReturn "testNid"
            on { subNamespaceIdentifier } doReturn setOf("test")
            on { toUrnString() } doReturn "urn:test:test:testNid"
        }

        val dbData = "urn:test:test:testNid"

        val urn = urnConverter.convertToEntityAttribute(dbData)

        Assertions.assertEquals(urnMock.namespace, urn?.namespace)
        Assertions.assertEquals(urnMock.nameSpecificString, urn?.nameSpecificString)
        Assertions.assertEquals(urnMock.subNamespaceIdentifier, urn?.subNamespaceIdentifier)
    }

    @Test
    fun `convertToEntityAttribute should return null when dbData is null`() {
        val urn = urnConverter.convertToEntityAttribute(null)
        Assertions.assertNull(urn)
    }

    @Test
    fun `convertToDatabaseColumn should convert URN to string correctly`() {
        val urnMock: Urn = mock {
            on { toUrnString() } doReturn "urn:test:test:testNid"
        }

        val result = urnConverter.convertToDatabaseColumn(urnMock)

        Assertions.assertEquals("urn:test:test:testNid", result)
    }

    @Test
    fun `convertToDatabaseColumn should return null when attribute is null`() {
        val result = urnConverter.convertToDatabaseColumn(null)

        Assertions.assertNull(result)
    }
}