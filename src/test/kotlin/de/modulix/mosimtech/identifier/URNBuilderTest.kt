package de.modulix.mosimtech.identifier

import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class URNBuilderTest {

    @Test
    fun `generateUrnString should create URN string with namespace and UUID as NSS and included NID`() {
        val generator = URNBuilder()
        val urnString = generator.generateUrnString(TestNamespace.Test, "nid1", "nid2")

        assertNotNull(urnString)
        Assertions.assertTrue(urnString.startsWith("urn:test"))
        Assertions.assertTrue(urnString.contains("nid1"))
        Assertions.assertTrue(urnString.contains("nid2"))
    }

    @Test
    fun `generateUrn should create Urn object with namespace identifier and UUID as NSS and included NID`() {
        val generator = URNBuilder()
        val urn = generator.generateUrn(TestNamespace.Test, "nid1", "nid2")

        assertNotNull(urn)
        assertEquals("test", urn.namespace)
        assertNotNull(urn.nss)
        Assertions.assertTrue(urn.nid!!.contains("nid1"))
        Assertions.assertTrue(urn.nid!!.contains("nid2"))
    }
}