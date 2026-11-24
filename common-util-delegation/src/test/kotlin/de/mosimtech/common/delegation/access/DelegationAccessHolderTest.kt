package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DelegationAccessHolderTest {

    private val context = DelegationContext(
        ownerId = Urn.parse("urn:user:momasoft:owner")!!,
        actorId = Urn.parse("urn:user:momasoft:actor")!!,
        scopes = setOf("plan:read"),
        delegationId = "urn:delegation:d-1",
    )

    @AfterTest
    fun cleanup() {
        DelegationAccessHolder.clear()
    }

    @Test
    fun `get returns null when nothing was set`() {
        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `get returns the context that was set`() {
        DelegationAccessHolder.set(context)

        assertEquals(context, DelegationAccessHolder.get())
    }

    @Test
    fun `clear removes the context`() {
        DelegationAccessHolder.set(context)

        DelegationAccessHolder.clear()

        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `context is not visible on another thread`() {
        DelegationAccessHolder.set(context)
        var seen: DelegationContext? = context

        val thread = Thread { seen = DelegationAccessHolder.get() }
        thread.start()
        thread.join()

        assertNull(seen)
    }
}
