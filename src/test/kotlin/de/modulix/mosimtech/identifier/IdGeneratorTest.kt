package de.modulix.mosimtech.identifier

import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for the methods of the IdGenerator interface.
 */
class IdGeneratorTest {

    /**
     * The object under test.
     *
     * We use an anonymous class here to provide the most minimal implementation of the interface.
     */
    private val idGenerator = object : IdGenerator {}

    @Test
    fun `generateUrnString should create a URN string`() {
        val urnString = idGenerator.generateUrnString()
        println(urnString)
        assertNotNull(urnString)
        val urnPattern =
            Regex("^urn:[^:]+:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        assert(urnString.matches(urnPattern))
    }

    @Test
    fun `generateUrn should create a URN with the default Namespace and no NIDs by default`() {
        val urn = idGenerator.generateUrn()

        with(urn) {
            assertEquals(DefaultNamespace.Undefined, namespace)
            assertNotNull(nss)
            assertEquals(null, nid)
        }
    }

    @Test
    fun `generateUrn should create a URN with the provided Namespace and NIDs`() {
        val urn = idGenerator.generateUrn(TestNamespace.Test, "custom-nid")

        with(urn) {
            assertEquals(TestNamespace.Test, this.namespace)
            assertNotNull(nss)
            assertEquals(setOf("custom-nid"), nid)
        }
    }


    @Test
    fun `generateUrn should create a URN with multiple NIDs`() {
        val urn = idGenerator.generateUrn(TestNamespace.Test, "nid1", "nid2")

        with(urn) {
            assertEquals(TestNamespace.Test, this.namespace)
            assertNotNull(nss)
            assertEquals(setOf("nid1", "nid2"), nid)
        }
    }

    @Test
    fun `generateUrnString should create a URN string with the provided Namespace and no NIDs`() {
        val urnString = idGenerator.generateUrnString(TestNamespace.Test)
        println(urnString)
        assertNotNull(urnString)
        val urnPattern = Regex("^urn:test:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        assert(urnString.matches(urnPattern))
    }

    @Test
    fun `generateUrnString should create a URN string with the provided Namespace and a single NID`() {
        val urnString = idGenerator.generateUrnString(TestNamespace.Test, "custom-nid")
        println(urnString)
        assertNotNull(urnString)
        val urnPattern =
            Regex("^urn:test:custom-nid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        assert(urnString.matches(urnPattern))
    }

    @Test
    fun `generateUrnString should create a URN string with the provided Namespace and multiple NIDs`() {
        val urnString = idGenerator.generateUrnString(TestNamespace.Test, "nid1", "nid2")
        println(urnString)
        assertNotNull(urnString)
        val urnPattern =
            Regex("^urn:test:nid1:nid2:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        assert(urnString.matches(urnPattern))
    }
}
