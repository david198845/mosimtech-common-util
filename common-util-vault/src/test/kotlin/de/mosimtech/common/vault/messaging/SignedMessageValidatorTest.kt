package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.vault.VaultException
import java.time.Duration
import java.time.Instant

class SignedMessageValidatorTest {

    private val sentAt = Instant.parse("2026-11-23T10:00:00Z")
    private val verifier: VaultTransitMessageVerifier = mock()
    private val decoder: JwtDecoder = mock()
    private val store = InMemoryMessageIdStore()
    private val validator = SignedMessageValidator(verifier, MessageIdempotencyGuard(store, Duration.ofMinutes(5)), decoder)

    private fun envelope(id: String = "id-1") = SignedMessageEnvelope("payload", sentAt, null, "vault:v1:s", id)

    private fun jwt(value: String, iat: Instant = sentAt.minusSeconds(10), exp: Instant = sentAt.plusSeconds(290)) =
        Jwt.withTokenValue(value).header("alg", "RS256").claim("azp", "pellets").issuedAt(iat).expiresAt(exp).build()

    private fun acceptSignature() {
        whenever(verifier.verify(any<SignedMessageEnvelope<String>>(), any(), anyOrNull())).thenReturn(true)
        whenever(verifier.isTokenValidAtIssuance(any<SignedMessageEnvelope<String>>(), any(), any())).thenAnswer {
            val iat = it.getArgument<Instant>(1)
            val exp = it.getArgument<Instant>(2)
            !sentAt.isBefore(iat) && sentAt.isBefore(exp)
        }
    }

    @Test
    fun `system flow passes all checks and returns the decoded token`() {
        acceptSignature()
        whenever(decoder.decode("svc")).thenReturn(jwt("svc"))
        var checked: Jwt? = null

        val result = validator.validateSystem(envelope(), "svc") { checked = it }

        assertThat(result.tokenValue).isEqualTo("svc")
        assertThat(checked).isSameAs(result)
        verify(verifier).verify(any<SignedMessageEnvelope<String>>(), eq("svc"), eq(null))
    }

    @Test
    fun `missing header is rejected before anything else`() {
        assertThatThrownBy { validator.validateSystem(envelope(), null) }
            .isInstanceOf(SecurityException::class.java).hasMessage("Missing header: X-Auth-Service-token")
        verify(verifier, never()).verify(any<SignedMessageEnvelope<String>>(), any(), anyOrNull())
    }

    @Test
    fun `invalid signature is rejected and the message id stays unused`() {
        whenever(verifier.verify(any<SignedMessageEnvelope<String>>(), any(), anyOrNull())).thenReturn(false)

        assertThatThrownBy { validator.validateSystem(envelope(), "svc") }
            .isInstanceOf(SecurityException::class.java).hasMessage("Envelope signature or expiry invalid")
        assertThat(store.storeIfAbsent("id-1", Duration.ofMinutes(1))).isTrue()
        verify(decoder, never()).decode(any())
    }

    @Test
    fun `a replayed message is rejected before the token is decoded`() {
        acceptSignature()
        whenever(decoder.decode("svc")).thenReturn(jwt("svc"))
        validator.validateSystem(envelope(), "svc")

        assertThatThrownBy { validator.validateSystem(envelope(), "svc") }.isInstanceOf(DuplicateMessageException::class.java)
    }

    @Test
    fun `a token that was expired when sent is rejected, one expired since is accepted`() {
        acceptSignature()
        whenever(decoder.decode("old")).thenReturn(jwt("old", sentAt.minusSeconds(600), sentAt.minusSeconds(1)))
        whenever(decoder.decode("ok")).thenReturn(jwt("ok", sentAt.minusSeconds(10), sentAt.plusSeconds(1)))

        assertThatThrownBy { validator.validateSystem(envelope("a"), "old") }
            .isInstanceOf(SecurityException::class.java).hasMessage("Service token was not valid when the message was sent")
        assertThat(validator.validateSystem(envelope("b"), "ok").tokenValue).isEqualTo("ok")
    }

    @Test
    fun `an invalid token becomes a security exception, technical failures pass through`() {
        acceptSignature()
        whenever(decoder.decode("bad")).thenThrow(BadJwtException("wrong azp"))
        whenever(decoder.decode("down")).thenThrow(JwtException("jwk set unreachable"))
        whenever(verifier.verify(any<SignedMessageEnvelope<String>>(), eq("vault"), anyOrNull())).thenThrow(VaultException("down"))

        assertThatThrownBy { validator.validateSystem(envelope("a"), "bad") }
            .isInstanceOf(SecurityException::class.java).hasMessageContaining("wrong azp")
        assertThatThrownBy { validator.validateSystem(envelope("b"), "down") }
            .isExactlyInstanceOf(JwtException::class.java)
        assertThatThrownBy { validator.validateSystem(envelope("c"), "vault") }
            .isInstanceOf(VaultException::class.java)
    }

    @Test
    fun `caller check rejects the message`() {
        acceptSignature()
        whenever(decoder.decode("svc")).thenReturn(jwt("svc"))

        assertThatThrownBy { validator.validateSystem(envelope(), "svc") { throw SecurityException("no role") } }
            .isInstanceOf(SecurityException::class.java).hasMessage("no role")
    }

    @Test
    fun `user flow checks both tokens and binds both to the signature`() {
        acceptSignature()
        whenever(decoder.decode("svc")).thenReturn(jwt("svc"))
        whenever(decoder.decode("usr")).thenReturn(jwt("usr"))
        val checks = mutableListOf<String>()

        val tokens = validator.validateUser(envelope(), "svc", "usr", { checks += "service" }, { checks += "user" })

        assertThat(tokens.serviceToken.tokenValue).isEqualTo("svc")
        assertThat(tokens.userToken.tokenValue).isEqualTo("usr")
        assertThat(checks).containsExactly("service", "user")
        verify(verifier).verify(any<SignedMessageEnvelope<String>>(), eq("svc"), eq("usr"))
    }

    @Test
    fun `user flow requires the user token header and a token without iat is rejected`() {
        assertThatThrownBy { validator.validateUser(envelope(), "svc", null) }
            .isInstanceOf(SecurityException::class.java).hasMessage("Missing header: X-Auth-User-Token")

        acceptSignature()
        whenever(decoder.decode("svc")).thenReturn(jwt("svc"))
        whenever(decoder.decode("usr")).thenReturn(
            Jwt.withTokenValue("usr").header("alg", "RS256").claim("sub", "u").expiresAt(sentAt.plusSeconds(60)).build(),
        )
        assertThatThrownBy { validator.validateUser(envelope("x"), "svc", "usr") }
            .isInstanceOf(SecurityException::class.java).hasMessage("User token has no iat claim")
    }
}
