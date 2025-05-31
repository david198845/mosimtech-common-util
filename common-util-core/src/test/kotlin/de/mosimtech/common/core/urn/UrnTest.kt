package de.mosimtech.common.core.urn

import de.mosimtech.common.core.namespace.DefaultNamespace
import de.mosimtech.common.core.namespace.TestNamespace
import de.mosimtech.common.core.util.toUrn
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrnTest {

    @Test
    fun `Test SystemUser` () {
        val urn = SystemUser
        assertEquals("urn:user:SYSTEM", urn.toUrnString())
    }

    @Test
    fun `Test Urn creation and conversion to string without snid`() {
        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = urn.toUrnString()
        assertEquals("urn:test:nss", urnString)
    }

    @Test
    fun `Test Urn creation and conversion to string with snid`() {
        val urn = Urn(TestNamespace.Test, "nss", setOf("nid1", "nid2"))
        val urnString = urn.toUrnString()
        assertEquals("urn:test:nid1:nid2:nss", urnString)
    }

    @Test
    fun `Test namespace registration and parsing valid urn string with snid`() {
        val urn = Urn.parse("urn:test:nid1:nid2:nss")
        Assertions.assertNotNull(urn)
        assertEquals(TestNamespace.Test.identifier, urn?.namespace)
        assertEquals("nss", urn?.nameSpecificString)
        assertEquals(setOf("nid1", "nid2"), urn?.subNamespaceIdentifier)
    }

    @Test
    fun `Test namespace registration and parsing valid urn string without snid`() {
        val urn = Urn.parse("urn:test:nss")
        Assertions.assertNotNull(urn)
        assertEquals(TestNamespace.Test.identifier, urn?.namespace)
        assertEquals("nss", urn?.nameSpecificString)
        Assertions.assertTrue(urn?.subNamespaceIdentifier!!.isEmpty())
    }

    @Test
    fun `Test parsing invalid urn string`() {
        val urn = Urn.parse("notValidUrn")
        Assertions.assertNull(urn)
    }

    @Test
    fun `Test parsing urn string with unregistered namespace`() {
        val urn = Urn.parse("urn:unregistered:nss")
        Assertions.assertNotNull(urn)
        Assertions.assertNotEquals(TestNamespace.Test.identifier, urn?.namespace)
        assertEquals("nss", urn?.nameSpecificString)
        Assertions.assertTrue(urn?.subNamespaceIdentifier!!.isEmpty())
    }

    @Test
    fun `Test equals function with Urn object`() {
        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss2")
        val urn3 = Urn(TestNamespace.Test, "nss")
        Assertions.assertFalse(urn1.equals(urn2))
        Assertions.assertTrue(urn1.equals(urn3))
    }

    @Test
    fun `Test equals function with String object`() {

        val urn = Urn(TestNamespace.Test, "nss")
        val urnString1 = "urn:test:nss"
        val urnString2 = "urn:test:nss2"
        Assertions.assertFalse(urn.equals(urnString2))
        Assertions.assertTrue(urn.equals(urnString1))
    }

    @Test
    fun `Test parse function with invalid input`() {
        Assertions.assertNull(Urn.parse("invalidUrn"))
    }

    @Test
    fun `Should convert valid Urn String to Urn object`() {

        val expected = Urn(TestNamespace.Test, "1234")
        Assertions.assertEquals(expected, "urn:test:1234".toUrn())
    }

    @Test
    fun `Should convert valid Urn String with snid to Urn object`() {

        val expected = Urn(TestNamespace.Test, "1234", setOf("01", "02"))
        Assertions.assertEquals(expected, "urn:test:01:02:1234".toUrn())
    }

    @Test
    fun `Should convert valid Test Urn String with snid to Urn object`() {

        val expected = Urn("shift_calc", "b1248df9-aad0-4f1b-8761-8276c00c0118", setOf("shifts"))
        val targetUrn = Urn.parse("urn:shift_calc:shifts:b1248df9-aad0-4f1b-8761-8276c00c0118")
        Assertions.assertEquals(expected, targetUrn)
    }

    @Test
    fun `Should throw Exception when converting invalid Urn String to Urn object`() {
        Assertions.assertNull("invalid:String".toUrn())
    }

    @Test
    fun `Test parse function with valid input containing snid`() {

        val urn = Urn.parse("urn:test:snid:nss")
        Assertions.assertNotNull(urn)
        assertEquals(TestNamespace.Test.identifier, urn?.namespace)
        assertEquals(setOf("snid"), urn?.subNamespaceIdentifier)
        assertEquals("nss", urn?.nameSpecificString)
    }

    @Test
    fun `Test parse function with valid input without snid`() {

        val urn = Urn.parse("urn:test:nss")
        Assertions.assertNotNull(urn)
        assertEquals(TestNamespace.Test.identifier, urn?.namespace)
        Assertions.assertTrue(urn?.subNamespaceIdentifier!!.isEmpty())
        assertEquals("nss", urn?.nameSpecificString)
    }

    @Test
    fun `Test toString function outputs correct URN string representation`() {

        val urn = Urn(TestNamespace.Test, "nss")
        assertEquals("urn:test:nss", urn.toString())
    }

    @Test
    fun `Test equals function returns false when an unrelated object type is compared`() {

        val urn = Urn(TestNamespace.Test, "nss")
        val unrelatedObject = "unrelated"
        Assertions.assertFalse(urn.equals(unrelatedObject))
    }

    @Test
    fun `Test equals function comparing two identical Urn objects`() {

        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss")
        Assertions.assertTrue(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing two different Urn objects`() {

        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss2")
        Assertions.assertFalse(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing Urn object with its string representation`() {

        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = "urn:test:nss"
        Assertions.assertTrue(urn.equals(urnString))
    }

    @Test
    fun `Test equals function comparing Urn object with different string representation`() {

        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = "urn:test:nss2"
        Assertions.assertFalse(urn.equals(urnString))
    }

    @Test
    fun `Test equals function returns false when a null object is compared`() {

        val urn = Urn(TestNamespace.Test, "nss")
        val nullObject = null
        Assertions.assertFalse(urn.equals(nullObject))
    }

    @Test
    fun `Test equals function comparing two URN with different nss`() {

        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2: Urn? = Urn(TestNamespace.Test, "nss2")
        Assertions.assertFalse(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing two URN with same nss`() {

        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2: Urn? = Urn(TestNamespace.Test, "nss")
        Assertions.assertTrue(urn1.equals(urn2))
    }

    @Test
    fun `Test Urn initialisation with default namespace defined in Urn object`() {
        val urn = Urn(DefaultNamespace.Undefined, "nss")
        assertEquals(DefaultNamespace.Undefined.identifier, urn.namespace)
        assertEquals("nss", urn.nameSpecificString)
        Assertions.assertTrue(urn.subNamespaceIdentifier.isEmpty())
    }

    @Test
    fun `Test Urn creation with single snid`() {
        val urn = Urn(TestNamespace.Test, "nss", setOf("nid1"))
        assertEquals("urn:test:nid1:nss", urn.toUrnString())
    }

    @Test
    fun `Test parsing invalid urn with empty namespace`() {
        val urn = Urn.parse("urn::nss")
        Assertions.assertNull(urn)
    }

    @Test
    fun `Test parsing invalid urn with empty nss`() {
        val urn = Urn.parse("urn:test:")
        Assertions.assertNull(urn)
    }

    @Test
    fun `Test equals function with unrelated object type`() {
        val urn = Urn(TestNamespace.Test, "nss")
        val unrelatedObject = 1234
        Assertions.assertFalse(urn.equals(unrelatedObject))
    }
}
