package de.mosimtech.common.security.haapikey

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.ExpectedCount.once
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class HaApiKeyVerifierTest {

    private val rawKey = "mha_geheimer-testschluessel"
    private val builder = RestClient.builder().baseUrl("http://auth.test/api")
    private val server = MockRestServiceServer.bindTo(builder).build()
    private var now: Instant = Instant.parse("2026-11-19T10:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant(): Instant = now
    }

    private fun verifier(token: () -> String = { "t1" }) =
        HaApiKeyVerifier(builder.build(), token, clock, Duration.ofMinutes(1))

    private fun expectVerify(status: HttpStatus, body: String? = null) {
        val response = if (body != null) {
            withStatus(status).contentType(MediaType.APPLICATION_JSON).body(body)
        } else {
            withStatus(status)
        }
        server.expect(once(), requestTo("http://auth.test/api/v1/ha-api-key/verify"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(response)
    }

    @Test
    fun `accepted key yields the user urn`() {
        server.expect(requestTo("http://auth.test/api/v1/ha-api-key/verify"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer t1"))
            .andExpect(content().string("""{"key":"$rawKey"}"""))
            .andRespond(withSuccess("""{"userId":"urn:momasoft:user:abc"}""", MediaType.APPLICATION_JSON))

        val result = verifier().verify(rawKey)

        val accepted = assertIs<HaApiKeyVerification.Accepted>(result)
        assertEquals("urn:momasoft:user:abc", accepted.userId.toString())
        server.verify()
    }

    @Test
    fun `an accepted key is asked again on every call`() {
        repeat(2) {
            server.expect(requestTo("http://auth.test/api/v1/ha-api-key/verify"))
                .andRespond(withSuccess("""{"userId":"urn:momasoft:user:abc"}""", MediaType.APPLICATION_JSON))
        }
        val verifier = verifier()

        assertIs<HaApiKeyVerification.Accepted>(verifier.verify(rawKey))
        assertIs<HaApiKeyVerification.Accepted>(verifier.verify(rawKey))
        server.verify()
    }

    @Test
    fun `an unknown key is remembered for one minute`() {
        expectVerify(HttpStatus.NOT_FOUND)
        val verifier = verifier()

        assertEquals(HaApiKeyVerification.Unknown, verifier.verify(rawKey))
        assertEquals(HaApiKeyVerification.Unknown, verifier.verify(rawKey))
        server.verify()

        server.reset()
        now = now.plusSeconds(61)
        expectVerify(HttpStatus.NOT_FOUND)
        assertEquals(HaApiKeyVerification.Unknown, verifier.verify(rawKey))
        server.verify()
    }

    @Test
    fun `forbidden caller is unavailable, not unknown`() {
        expectVerify(HttpStatus.FORBIDDEN)

        val result = verifier().verify(rawKey)

        val unavailable = assertIs<HaApiKeyVerification.Unavailable>(result)
        assertEquals("auth service rejected caller: 403", unavailable.reason)
    }

    @Test
    fun `server error is unavailable`() {
        expectVerify(HttpStatus.INTERNAL_SERVER_ERROR)

        assertIs<HaApiKeyVerification.Unavailable>(verifier().verify(rawKey))
    }

    @Test
    fun `token supplier failure is unavailable without a request`() {
        val result = verifier { throw IllegalStateException("keycloak down") }.verify(rawKey)

        assertIs<HaApiKeyVerification.Unavailable>(result)
        server.verify()
    }

    @Test
    fun `unreadable urn is unavailable`() {
        expectVerify(HttpStatus.OK, """{"userId":"kein-urn"}""")

        assertIs<HaApiKeyVerification.Unavailable>(verifier().verify(rawKey))
    }

    @Test
    fun `reason never contains the key`() {
        expectVerify(HttpStatus.INTERNAL_SERVER_ERROR)

        val unavailable = assertIs<HaApiKeyVerification.Unavailable>(verifier().verify(rawKey))

        assertFalse(unavailable.reason.contains(rawKey))
        assertFalse(unavailable.reason.contains(HaApiKeyHash.sha256Hex(rawKey)))
        assertTrue(unavailable.reason.isNotBlank())
    }
}
