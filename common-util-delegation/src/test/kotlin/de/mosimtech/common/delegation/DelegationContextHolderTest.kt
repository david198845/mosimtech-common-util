package de.mosimtech.common.delegation

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DelegationContextHolderTest {

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
    }

    @Test
    fun `get returns null when nothing set`() {
        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `get returns value after set`() {
        DelegationContextHolder.set("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000")
        assertEquals("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000", DelegationContextHolder.get())
    }

    @Test
    fun `clear removes the stored value`() {
        DelegationContextHolder.set("urn:user:momasoft:some-uuid")
        DelegationContextHolder.clear()
        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `is isolated per thread`() {
        DelegationContextHolder.set("urn:user:momasoft:thread-main")
        var threadValue: String? = "not-cleared"
        val thread = Thread { threadValue = DelegationContextHolder.get() }
        thread.start()
        thread.join()
        assertNull(threadValue)
    }
}
