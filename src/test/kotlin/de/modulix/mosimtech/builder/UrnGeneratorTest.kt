package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.base.urn.Urn
import de.modulix.mosimtech.namespace.TestNamespace
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

/**
 * This class tests the function 'generateID' of class 'UrnGenerator'
 * which generates the URN based on Namespace and name identifiers.
 */

class UrnGeneratorTest {

    val urnRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$".toRegex()

    companion object {
        private lateinit var testIdentifier: String
        private lateinit var expectedUrn: Urn

        @BeforeAll
        @JvmStatic
        fun setup() {
            testIdentifier = "testIdentifier"
            expectedUrn = Urn(
                namespace = "test",
                nss = "testIdentifier",
                nid = emptySet()
            )
        }
    }

    /**
     * Test 'generateID' to ensure it correctly generates a URN when name identifiers are provided.
     */
    @Test
    fun testGenerateIDWithIdentifiers() {
        val urn = UrnGenerator.generateID(TestNamespace.Test, testIdentifier)
        assertEquals(expectedUrn.namespace, urn.namespace)
        assertTrue(urn.nid.contains(testIdentifier))
        assertTrue(
            urn.nss.matches(urnRegex),
            "expectedUrn.nss does not match UUID schema"
        )
    }

    /**
     * Test 'generateID' to ensure it correctly generates a URN when no name identifiers are provided.
     */
    @Test
    fun testGenerateIDWithoutIdentifiers() {
        val urn = UrnGenerator.generateID(TestNamespace.Test)
        assertEquals(expectedUrn.namespace, urn.namespace)
        assertTrue(
            urn.nss.matches(urnRegex),
            "expectedUrn.nss does not match UUID schema"
        )
    }
}