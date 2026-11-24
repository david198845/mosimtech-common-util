package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DelegationContextTest {

    @Test
    fun `hasScope returns true only for a granted scope`() {
        val context = DelegationContext(
            ownerId = Urn.parse("urn:user:momasoft:owner")!!,
            actorId = Urn.parse("urn:user:momasoft:actor")!!,
            scopes = setOf("plan:read"),
            delegationId = "urn:delegation:d-1",
        )

        assertTrue(context.hasScope("plan:read"))
        assertFalse(context.hasScope("plan:write"))
        assertFalse(context.hasScope("PLAN:READ"))
    }
}
