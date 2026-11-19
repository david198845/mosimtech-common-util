package de.mosimtech.common.security.haapikey

import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.Clock
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HaApiKeyRateLimiterTest {

    private val clock = mock(Clock::class.java)
    private val limiter = HaApiKeyRateLimiter(120, clock)

    private fun at(time: String) {
        `when`(clock.instant()).thenReturn(Instant.parse(time))
    }

    @Test
    fun `allows the configured number of requests per minute`() {
        at("2026-11-11T10:00:05Z")
        repeat(120) { assertTrue(limiter.tryAcquire("key-a"), "request ${it + 1}") }
        assertFalse(limiter.tryAcquire("key-a"))
    }

    @Test
    fun `a new minute opens a new window`() {
        at("2026-11-11T10:00:59Z")
        repeat(120) { limiter.tryAcquire("key-a") }
        assertFalse(limiter.tryAcquire("key-a"))

        at("2026-11-11T10:01:00Z")
        assertTrue(limiter.tryAcquire("key-a"))
    }

    @Test
    fun `keys are counted separately`() {
        at("2026-11-11T10:00:05Z")
        repeat(120) { limiter.tryAcquire("key-a") }
        assertFalse(limiter.tryAcquire("key-a"))
        assertTrue(limiter.tryAcquire("key-b"))
    }
}
