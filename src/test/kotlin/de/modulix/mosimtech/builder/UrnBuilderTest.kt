package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.namespace.DefaultNamespace
import de.modulix.mosimtech.namespace.TestNamespace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UrnBuilderTest {

    private val urnBuilder = UrnBuilder()

    @Test
    fun `build with default values should return DefaultNamespace with empty snid`() {
        val urn = urnBuilder.build()
        assertEquals(DefaultNamespace.Undefined.identifier, urn.namespace)
        assertTrue(urn.subNamespaceIdentifier.isEmpty())
    }

    @Test
    fun `build with namespace as Namespace should return set namespace`() {
        val testNamespace = TestNamespace.Test
        val urn = urnBuilder.withNamespace(testNamespace).build()
        assertEquals(testNamespace.identifier, urn.namespace)
    }

    @Test
    fun `build with namespace as String should return set namespace`() {
        val testNamespace = "Test"
        val urn = urnBuilder.withNamespace(testNamespace).build()
        assertEquals(testNamespace, urn.namespace)
    }

    @Test
    fun `build with snid should return set snid`() {
        val testNid = setOf("id1", "id2", "id3")
        val urn = urnBuilder.withSubNamespaceIdentifier(*testNid.toTypedArray()).build()
        assertEquals(testNid, urn.subNamespaceIdentifier)
    }
}