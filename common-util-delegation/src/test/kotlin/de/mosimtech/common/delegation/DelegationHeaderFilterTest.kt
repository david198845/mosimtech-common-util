package de.mosimtech.common.delegation

import org.mockito.kotlin.mock
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DelegationHeaderFilterTest {

    private val filter = DelegationHeaderFilter()
    private val response = MockHttpServletResponse()

    @AfterTest
    fun cleanup() {
        DelegationContextHolder.clear()
    }

    @Test
    fun `should set target user id in context during filter execution`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000")

        var capturedId: String? = null
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertEquals("urn:user:momasoft:550e8400-e29b-41d4-a716-446655440000", capturedId)
    }

    @Test
    fun `should clear context after filter execution`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:some-uuid")

        filter.doFilter(request, response, MockFilterChain())

        assertNull(DelegationContextHolder.get())
    }

    @Test
    fun `should not set context when header absent`() {
        val request = MockHttpServletRequest()

        var capturedId: String? = "sentinel"
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertNull(capturedId)
    }

    @Test
    fun `should not set context when header is blank`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "   ")

        var capturedId: String? = "sentinel"
        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { req, res, c ->
                capturedId = DelegationContextHolder.get()
                c.doFilter(req, res)
            }
        )

        filter.doFilter(request, response, chain)

        assertNull(capturedId)
    }

    @Test
    fun `should clear context even if chain throws exception`() {
        val request = MockHttpServletRequest()
        request.addHeader("X-Target-User-Id", "urn:user:momasoft:some-uuid")

        val chain = MockFilterChain(
            mock(),
            jakarta.servlet.Filter { _, _, _ -> throw RuntimeException("chain error") }
        )

        try {
            filter.doFilter(request, response, chain)
        } catch (_: RuntimeException) {}

        assertNull(DelegationContextHolder.get())
    }
}
