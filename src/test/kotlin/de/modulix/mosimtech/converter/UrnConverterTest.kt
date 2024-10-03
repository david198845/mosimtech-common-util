package de.modulix.mosimtech.converter

import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.model.urn.Urn
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**

 * `UrnConverterTest` is to test converting `Urn` values to `String` (to be persisted in database)
 * and converting `String` values back to `Urn` (when reading from database).
 *
 * Each test focuses on testing the `convertToEntityAttribute` method in the `UrnConverter` class.

 */

class UrnConverterTest {
    private val urnConverter = object : UrnConverter() {}

    @Test
    fun testValidUrnToEntityConversion() {
        val testUrn = Urn(
            namespace = DefaultNamespace.Undefined,
            nss = "testString",
            nid = "TestId"
        )
        checkUrnConversion(testUrn)
    }

    @Test
    fun testNullToEntityConversion() {
        val urnFromDatabaseData = urnConverter.convertToEntityAttribute(null)
        Assertions.assertNull(urnFromDatabaseData)
    }

    @Test
    fun testInvalidUrnStringToEntityConversion() {
        val invalidUrn = "invalidUrnString"
        val result = urnConverter.convertToEntityAttribute(invalidUrn)
        Assertions.assertNull(result, "Expected the result to be null for an invalid URN string")
    }

    private fun checkUrnConversion(testUrn: Urn) {
        val urnAsDatabaseData = urnConverter.convertToDatabaseColumn(testUrn)
        Assertions.assertEquals(urnAsDatabaseData,"urn:undefined:TestId:testString")
        val urnFromDatabaseData = urnConverter.convertToEntityAttribute(urnAsDatabaseData)
        Assertions.assertEquals(testUrn, urnFromDatabaseData)
    }
}