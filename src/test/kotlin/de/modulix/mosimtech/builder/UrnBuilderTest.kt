package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.base.namespace.DefaultNamespace
import de.modulix.mosimtech.namespace.TestNamespace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UrnBuilderTest {

    private val urnBuilder = UrnBuilder()

    @Test
    fun `build with default values should return DefaultNamespace with empty nid`() {
        val urn = urnBuilder.build()
        assertEquals(DefaultNamespace.Undefined.identifier, urn.namespace)
        assertTrue(urn.nid.isEmpty())
    }

    @Test
    fun `build with namespace as Namespace should return set namespace`() {
        val testNamespace = TestNamespace.Test
        val urn = urnBuilder.namespace(testNamespace).build()
        assertEquals(testNamespace.identifier, urn.namespace)
    }

    @Test
    fun `build with namespace as String should return set namespace`() {
        val testNamespace = "Test"
        val urn = urnBuilder.namespace(testNamespace).build()
        assertEquals(testNamespace, urn.namespace)
    }

    @Test
    fun `build with nid should return set nid`() {
        val testNid = setOf("id1", "id2", "id3")
        val urn = urnBuilder.nid(*testNid.toTypedArray()).build()
        assertEquals(testNid, urn.nid)
    }
}