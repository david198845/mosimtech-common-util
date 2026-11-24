package de.mosimtech.common.delegation.access

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.DelegationConstants
import jakarta.servlet.FilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DelegationAccessFilterTest {

    private val actor = Urn.parse("urn:user:momasoft:actor")!!
    private val owner = Urn.parse("urn:user:momasoft:owner")!!

    private val resolverCalls = mutableListOf<Pair<Urn, Urn>>()
    private var resolverResult: DelegationGrant? = null
    private var resolverFailure: RuntimeException? = null

    private var chainRan = false
    private var capturedContext: DelegationContext? = null
    private var chainFailure: RuntimeException? = null

    private val resolver = DelegationGrantResolver { a, o ->
        resolverCalls += a to o
        resolverFailure?.let { throw it }
        resolverResult
    }

    private val chain = FilterChain { _, _ ->
        chainRan = true
        capturedContext = DelegationAccessHolder.get()
        chainFailure?.let { throw it }
    }

    @AfterTest
    fun cleanup() {
        DelegationAccessHolder.clear()
    }

    private fun request(header: String? = null) = MockHttpServletRequest().apply {
        header?.let { addHeader(DelegationConstants.HEADER_TARGET_USER_ID, it) }
    }

    private fun run(
        header: String?,
        filter: DelegationAccessFilter = DelegationAccessFilter(resolver) { actor },
    ): MockHttpServletResponse {
        val response = MockHttpServletResponse()
        filter.doFilter(request(header), response, chain)
        return response
    }

    @Test
    fun `passes through without context when header is absent`() {
        run(null)

        assertTrue(chainRan)
        assertNull(capturedContext)
        assertTrue(resolverCalls.isEmpty())
    }

    @Test
    fun `passes through without context when header is blank`() {
        run("   ")

        assertTrue(chainRan)
        assertNull(capturedContext)
        assertTrue(resolverCalls.isEmpty())
    }

    @Test
    fun `rejects with 403 when header is not a urn`() {
        val response = run("not-a-urn")

        assertNull(DelegationAccessHolder.get())
        assertTrue(resolverCalls.isEmpty())

        assertEquals(403, response.status)
        assertFalse(chainRan)
    }

    @Test
    fun `rejects with 403 when no user is authenticated`() {
        val response = run(owner.toUrnString(), DelegationAccessFilter(resolver) { null })

        assertEquals(403, response.status)
        assertFalse(chainRan)
        assertTrue(resolverCalls.isEmpty())
    }

    @Test
    fun `ignores header that targets the caller itself`() {
        val response = run(actor.toUrnString())

        assertEquals(200, response.status)
        assertTrue(chainRan)
        assertNull(capturedContext)
        assertTrue(resolverCalls.isEmpty())
    }

    @Test
    fun `rejects with 403 when resolver finds no grant`() {
        resolverResult = null

        val response = run(owner.toUrnString())

        assertEquals(403, response.status)
        assertFalse(chainRan)
        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `sets verified context for the duration of the chain`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read", "salary:read"))

        run(owner.toUrnString())

        assertEquals(listOf(actor to owner), resolverCalls)
        val context = capturedContext
        assertEquals(owner, context?.ownerId)
        assertEquals(actor, context?.actorId)
        assertEquals(setOf("plan:read", "salary:read"), context?.scopes)
    }

    @Test
    fun `puts the delegation id of the resolved grant into the context`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))

        run(owner.toUrnString())

        assertEquals("urn:delegation:d-1", capturedContext?.delegationId)
    }

    @Test
    fun `clears context after the chain completed`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))

        run(owner.toUrnString())

        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `clears context when the chain throws`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))
        chainFailure = IllegalStateException("boom")

        assertFailsWith<IllegalStateException> { run(owner.toUrnString()) }

        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `leaves no context when the resolver throws`() {
        resolverFailure = RuntimeException("database down")

        assertFailsWith<RuntimeException> { run(owner.toUrnString()) }

        assertFalse(chainRan)
        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `second request on the same thread starts without context`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))
        chainFailure = IllegalStateException("boom")
        assertFailsWith<IllegalStateException> { run(owner.toUrnString()) }

        chainFailure = null
        capturedContext = DelegationContext(owner, actor, setOf("sentinel"), "urn:delegation:sentinel")
        run(null)

        assertNull(capturedContext)
    }

    @Test
    fun `rejects with 403 when resolver returns an empty scope set`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", emptySet())

        val response = run(owner.toUrnString())

        assertEquals(403, response.status)
        assertFalse(chainRan)
        assertNull(DelegationAccessHolder.get())
    }

    @Test
    fun `trims surrounding whitespace of the header`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))

        run("  ${owner.toUrnString()}  ")

        assertEquals(owner, capturedContext?.ownerId)
    }

    @Test
    fun `rejections are indistinguishable`() {
        resolverResult = null
        val invalidUrn = run("not-a-urn")
        val noUser = run(owner.toUrnString(), DelegationAccessFilter(resolver) { null })
        val noGrant = run(owner.toUrnString())

        listOf(invalidUrn, noUser, noGrant).forEach {
            assertEquals(403, it.status)
            assertNull(it.errorMessage)
        }
    }

    @Test
    fun `uses the first value when the header is sent twice`() {
        resolverResult = DelegationGrant("urn:delegation:d-1", setOf("plan:read"))
        val request = request(owner.toUrnString())
        request.addHeader(DelegationConstants.HEADER_TARGET_USER_ID, "urn:user:momasoft:other")

        DelegationAccessFilter(resolver) { actor }.doFilter(request, MockHttpServletResponse(), chain)

        assertEquals(owner, capturedContext?.ownerId)
        assertEquals(listOf(actor to owner), resolverCalls)
    }
}
