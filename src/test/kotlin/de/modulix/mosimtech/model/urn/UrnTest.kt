package de.modulix.mosimtech.model.urn

import de.modulix.mosimtech.model.namespace.Namespace
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UrnTest {

    private data class TestNamespace(override val identifier: String) : Namespace

    @Test
    fun toUrnStringWithoutNid() {
        val testUrn = Urn(TestNamespace("testspace"), "nss")

        val urnString = testUrn.toUrnString()

        assertEquals("urn:testspace:nss", urnString)
    }

    @Test
    fun toUrnStringWithNid() {
        val testUrn = Urn(TestNamespace("testspace"), "nss", "nid")

        val urnString = testUrn.toUrnString()

        assertEquals("urn:testspace:nid:nss", urnString)
    }

    @Test
    fun registerNamespaceTest() {
        val testNamespace = TestNamespace("newNamespace")
        Urn.registerNamespace(testNamespace)
        assert(Urn.knownNamespaces.contains(testNamespace))
    }

    @Test
    fun parseWithNid() {
        val testNamespace = TestNamespace("parseSpace")
        Urn.registerNamespace(testNamespace)
        val testString = "urn:parseSpace:nid:nss"

        val parsedUrn = Urn.parse(testString)

        assertEquals(Urn(testNamespace, "nss", "nid"), parsedUrn)
    }

    @Test
    fun parseWithoutNid() {
        val testNamespace = TestNamespace("parseWithout")
        Urn.registerNamespace(testNamespace)
        val testString = "urn:parseWithout:nss"

        val parsedUrn = Urn.parse(testString)

        assertEquals(Urn(testNamespace, "nss"), parsedUrn)
    }

    @Test
    fun parseInvalidFormat() {
        val testString = "invalid:format:string"
        val parsedUrn = Urn.parse(testString)

        assertEquals(null, parsedUrn)
    }
}