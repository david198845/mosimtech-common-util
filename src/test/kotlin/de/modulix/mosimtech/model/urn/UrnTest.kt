package de.modulix.mosimtech.model.urn

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

}