package de.mosimtech.common.security.haapikey

import de.mosimtech.common.core.util.toUrn
import jakarta.servlet.FilterChain
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import java.time.Clock
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HaApiKeyAuthenticationFilterTest {

    private val verifier = mock<HaApiKeyVerifier>()
    private val chain = mock<FilterChain>()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()

    private fun filter(requestsPerMinute: Int = 120) =
        HaApiKeyAuthenticationFilter(verifier, HaApiKeyRateLimiter(requestsPerMinute, Clock.systemUTC()))

    private fun run(filter: HaApiKeyAuthenticationFilter = filter()) = filter.doFilter(request, response, chain)

    @AfterTest
    fun clearContext() = SecurityContextHolder.clearContext()

    @Test
    fun `missing key is 401`() {
        run()

        assertEquals(401, response.status)
        verify(chain, never()).doFilter(any(), any())
    }

    @Test
    fun `blank key is 401`() {
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, "   ")

        run()

        assertEquals(401, response.status)
    }

    @Test
    fun `key longer than 128 is 401 without verify`() {
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, "k".repeat(129))

        run()

        assertEquals(401, response.status)
        verify(verifier, never()).verify(any())
    }

    @Test
    fun `rate limit is 429 before verify`() {
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, "mha_x")

        run(filter(requestsPerMinute = 0))

        assertEquals(429, response.status)
        verify(verifier, never()).verify(any())
    }

    @Test
    fun `unknown is 401`() {
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, "mha_x")
        whenever(verifier.verify("mha_x")).thenReturn(HaApiKeyVerification.Unknown)

        run()

        assertEquals(401, response.status)
        assertEquals("", response.contentAsString)
        verify(chain, never()).doFilter(any(), any())
    }

    @Test
    fun `unavailable is 503`() {
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, "mha_x")
        whenever(verifier.verify("mha_x")).thenReturn(HaApiKeyVerification.Unavailable("down"))

        run()

        assertEquals(503, response.status)
        verify(chain, never()).doFilter(any(), any())
    }

    @Test
    fun `accepted sets principal and authority and continues`() {
        val userId = "urn:momasoft:user:abc".toUrn()!!
        request.addHeader(HaApiKeyAuthenticationFilter.HEADER, " mha_x ")
        whenever(verifier.verify("mha_x")).thenReturn(HaApiKeyVerification.Accepted(userId))
        var seenPrincipal: Any? = null
        var seenAuthorities: List<String> = emptyList()
        val capturingChain = FilterChain { _, _ ->
            val auth = SecurityContextHolder.getContext().authentication
            seenPrincipal = auth?.principal
            seenAuthorities = auth?.authorities?.mapNotNull { it.authority } ?: emptyList()
        }

        filter().doFilter(request, response, capturingChain)

        assertIs<HaApiKeyPrincipal>(seenPrincipal)
        assertEquals(userId, (seenPrincipal as HaApiKeyPrincipal).userId)
        assertTrue(seenAuthorities == listOf("ingest:write"))
    }
}
