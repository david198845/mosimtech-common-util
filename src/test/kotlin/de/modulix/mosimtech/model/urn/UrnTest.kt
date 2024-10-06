
package de.modulix.mosimtech.model.urn


import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrnTest {


    @Test
    fun `Test Urn creation and conversion to string without nid`() {
        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = urn.toUrnString()
        assertEquals("urn:test:nss", urnString)
    }

    @Test
    fun `Test Urn creation and conversion to string with nid`() {
        val urn = Urn(TestNamespace.Test, "nss", setOf("nid1", "nid2"))
        val urnString = urn.toUrnString()
        assertEquals("urn:test:nid1:nid2:nss", urnString)
    }

    @Test
    fun `Test namespace registration and parsing valid urn string with nid`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn.parse("urn:test:nid1:nid2:nss")
        assertNotNull(urn)
        assertEquals(TestNamespace.Test, urn?.namespace)
        assertEquals("nss", urn?.nss)
        assertEquals(setOf("nid1", "nid2"), urn?.nid)
    }

    @Test
    fun `Test namespace registration and parsing valid urn string without nid`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn.parse("urn:test:nss")
        assertNotNull(urn)
        assertEquals(TestNamespace.Test, urn?.namespace)
        assertEquals("nss", urn?.nss)
        assertNull(urn?.nid)
    }

    @Test
    fun `Test parsing invalid urn string`() {
        val urn = Urn.parse("notValidUrn")
        assertNull(urn)
    }

    @Test
    fun `Test parsing urn string with unregistered namespace`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn.parse("urn:unregistered:nss")
        assertNotNull(urn)
        assertNotEquals(TestNamespace.Test, urn?.namespace)
        assertEquals("nss", urn?.nss)
        assertNull(urn?.nid)
    }

    @Test
    fun `Test equals function with Urn object`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss2")
        val urn3 = Urn(TestNamespace.Test, "nss")
        assertFalse(urn1.equals(urn2))
        assertTrue(urn1.equals(urn3))
    }

    @Test
    fun `Test equals function with String object`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        val urnString1 = "urn:test:nss"
        val urnString2 = "urn:test:nss2"
        assertFalse(urn.equals(urnString2))
        assertTrue(urn.equals(urnString1))
    }

    @Test
    fun `Test knownNamespaces set after registering new namespaces`() {
        Urn.registerNamespace(TestNamespace.Test)
        assertTrue(Urn.knownNamespaces.contains(TestNamespace.Test))
    }

    @Test
    fun `Test parse function with invalid input`() {
        assertNull(Urn.parse("invalidUrn"))
    }

    @Test
    fun `Should convert valid Urn String to Urn object`() {
        Urn.registerNamespace(TestNamespace.Test)
        val expected = Urn(TestNamespace.Test, "1234")
        Assertions.assertEquals(expected, "urn:test:1234".toUrn())
    }

    @Test
    fun `Should convert valid Urn String with nid to Urn object`() {
        Urn.registerNamespace(TestNamespace.Test)
        val expected = Urn(TestNamespace.Test, "1234", setOf("01", "02"))
        Assertions.assertEquals(expected, "urn:test:01:02:1234".toUrn())
    }

    @Test
    fun `Should throw Exception when converting invalid Urn String to Urn object`() {
        assertNull("invalid:String".toUrn())
    }

    @Test
    fun `Test parse function with valid input containing nid`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn.parse("urn:test:nid:nss")
        assertNotNull(urn)
        assertEquals(TestNamespace.Test, urn?.namespace)
        assertEquals(setOf("nid"), urn?.nid)
        assertEquals("nss", urn?.nss)
    }

    @Test
    fun `Test parse function with valid input without nid`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn.parse("urn:test:nss")
        assertNotNull(urn)
        assertEquals(TestNamespace.Test, urn?.namespace)
        assertNull(urn?.nid)
        assertEquals("nss", urn?.nss)
    }

    @Test
    fun `Test toString function outputs correct URN string representation`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        assertEquals("urn:test:nss", urn.toString())
    }

    @Test
    fun `Test equals function returns false when an unrelated object type is compared`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        val unrelatedObject = "unrelated"
        assertFalse(urn.equals(unrelatedObject))
    }

    @Test
    fun `Test equals function comparing two identical Urn objects`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss")
        assertTrue(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing two different Urn objects`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn1 = Urn(TestNamespace.Test, "nss")
        val urn2 = Urn(TestNamespace.Test, "nss2")
        assertFalse(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing Urn object with its string representation`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = "urn:test:nss"
        assertTrue(urn.equals(urnString))
    }

    @Test
    fun `Test equals function comparing Urn object with different string representation`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        val urnString = "urn:test:nss2"
        assertFalse(urn.equals(urnString))
    }

    @Test
    fun `Test equals function returns false when a null object is compared`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn = Urn(TestNamespace.Test, "nss")
        val nullObject = null
        assertFalse(urn.equals(nullObject))
    }

    @Test
    fun `Test equals function comparing two URN with different nss`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn1: Urn = Urn(TestNamespace.Test, "nss")
        val urn2: Urn? = Urn(TestNamespace.Test, "nss2")
        assertFalse(urn1.equals(urn2))
    }

    @Test
    fun `Test equals function comparing two URN with same nss`() {
        Urn.registerNamespace(TestNamespace.Test)
        val urn1: Urn = Urn(TestNamespace.Test, "nss")
        val urn2: Urn? = Urn(TestNamespace.Test, "nss")
        assertTrue(urn1.equals(urn2))
    }

    @Test
    fun `Test Urn initialisation with default namespace defined in Urn object`() {
        val urn = Urn(DefaultNamespace.Undefined, "nss")
        assertEquals(DefaultNamespace.Undefined, urn.namespace)
        assertEquals("nss", urn.nss)
        assertNull(urn.nid)
    }

    @Test
    fun `Test Urn creation with single nid`() {
        val urn = Urn(TestNamespace.Test, "nss", setOf("nid1"))
        assertEquals("urn:test:nid1:nss", urn.toUrnString())
    }

    @Test
    fun `Test parsing invalid urn with empty namespace`() {
        val urn = Urn.parse("urn::nss")
        assertNull(urn)
    }

    @Test
    fun `Test parsing invalid urn with empty nss`() {
        val urn = Urn.parse("urn:test:")
        assertNull(urn)
    }

    @Test
    fun `Test equals function with unrelated object type`() {
        val urn = Urn(TestNamespace.Test, "nss")
        val unrelatedObject = 1234
        assertFalse(urn.equals(unrelatedObject))
    }
}
