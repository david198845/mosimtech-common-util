package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.vault.VaultException
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTransitOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant

class VaultTransitMessageVerifierTest {

    private val vaultOperations: VaultOperations = mock()
    private val transitOperations: VaultTransitOperations = mock()
    private val objectMapper = jacksonObjectMapper()
    private lateinit var verifier: VaultTransitMessageVerifier

    private val issuedAt = Instant.parse("2026-05-02T10:00:00Z")
    private val jwtIat = Instant.parse("2026-05-02T09:59:50Z")
    private val jwtExp = Instant.parse("2026-05-02T11:00:00Z")

    @BeforeEach
    fun setUp() {
        whenever(vaultOperations.opsForTransit()).thenReturn(transitOperations)
        verifier = VaultTransitMessageVerifier(vaultOperations, objectMapper, "test-key")
    }

    private fun envelope(exp: Instant? = null) = SignedMessageEnvelope(
        payload = "test-payload",
        issuedAt = issuedAt,
        exp = exp,
        signature = "vault:v1:sig",
    )

    @Test
    fun `verify returns true when all checks pass (exp null)`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)

        assertThat(verifier.verify(envelope(), jwtIat, jwtExp)).isTrue()
    }

    @Test
    fun `verify returns true when all checks pass (exp in future)`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)
        val exp = Instant.now().plusSeconds(3600)

        assertThat(verifier.verify(envelope(exp), jwtIat, jwtExp)).isTrue()
    }

    @Test
    fun `verify returns false when vault signature is invalid`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(false)

        assertThat(verifier.verify(envelope(), jwtIat, jwtExp)).isFalse()
    }

    @Test
    fun `verify returns false when exp is in the past`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)
        val expiredExp = Instant.now().minusSeconds(60)

        assertThat(verifier.verify(envelope(expiredExp), jwtIat, jwtExp)).isFalse()
    }

    @Test
    fun `verify returns false when token was not yet issued at issuedAt`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)
        val lateJwtIat = issuedAt.plusSeconds(10)
        val lateJwtExp = issuedAt.plusSeconds(3610)

        assertThat(verifier.verify(envelope(), lateJwtIat, lateJwtExp)).isFalse()
    }

    @Test
    fun `verify returns false when token was already expired at issuedAt`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)
        val earlyJwtIat = issuedAt.minusSeconds(3610)
        val earlyJwtExp = issuedAt.minusSeconds(10)

        assertThat(verifier.verify(envelope(), earlyJwtIat, earlyJwtExp)).isFalse()
    }

    @Test
    fun `verify returns false when issuedAt equals jwtExp (exp ist exklusiv per JWT RFC 7519)`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>())).thenReturn(true)

        assertThat(verifier.verify(envelope(), jwtIat, issuedAt)).isFalse()
    }

    @Test
    fun `verify propagates VaultException when vault is unreachable`() {
        whenever(transitOperations.verify(eq("test-key"), any<Plaintext>(), any<Signature>()))
            .thenThrow(VaultException("connection refused"))

        assertThatThrownBy { verifier.verify(envelope(), jwtIat, jwtExp) }
            .isInstanceOf(VaultException::class.java)
    }
}
