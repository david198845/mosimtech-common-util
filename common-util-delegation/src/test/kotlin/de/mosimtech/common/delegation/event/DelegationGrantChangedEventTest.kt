package de.mosimtech.common.delegation.event

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DelegationGrantChangedEventTest {

    @Test
    fun `carries the full grant state`() {
        val changedAt = Instant.parse("2026-11-24T10:15:30Z")

        val event = DelegationGrantChangedEvent(
            delegationId = "urn:delegation:d1",
            module = "SHIFT_CALENDAR",
            grantorId = "urn:user:momasoft:grantor",
            granteeId = "urn:user:momasoft:grantee",
            grantorDisplayName = "Gerda Geber",
            granteeDisplayName = "Nils Nehmer",
            scopes = listOf("plan:read", "plan:write"),
            active = true,
            validUntil = null,
            grantVersion = 7,
            changedAt = changedAt,
        )

        assertEquals("urn:delegation:d1", event.delegationId)
        assertEquals("SHIFT_CALENDAR", event.module)
        assertEquals("urn:user:momasoft:grantor", event.grantorId)
        assertEquals("urn:user:momasoft:grantee", event.granteeId)
        assertEquals("Gerda Geber", event.grantorDisplayName)
        assertEquals("Nils Nehmer", event.granteeDisplayName)
        assertEquals(listOf("plan:read", "plan:write"), event.scopes)
        assertTrue(event.active)
        assertNull(event.validUntil)
        assertEquals(7L, event.grantVersion)
        assertEquals(changedAt, event.changedAt)
        assertEquals(event, event.copy())
    }
}
