package de.modulix.mosimtech.model.namespace

import de.modulix.mosimtech.database.namespace.DefaultNamespace
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultNamespaceTest {

    /**
     * This class tests the DefaultNamespace enum class, which represents namespace in the system.
     */

    @Test
    fun testIdentifierValueForUndefined() {

        /**
         * This method tests if the value of the identifier for the enum constant Undefined matches the expected value.
         */

        val expectedId = "undefined"
        assertEquals(expectedId, DefaultNamespace.Undefined.identifier)
    }
}