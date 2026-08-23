package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTransitOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import java.time.Instant
import java.time.ZonedDateTime

/**
 * Der Empfaenger muss Nachrichten annehmen, die ein Sender mit aelterer Bibliothek (bis 3.3.7)
 * signiert hat. Ohne diesen Rueckfall muessten Sender und Empfaenger gleichzeitig aktualisiert
 * werden — bei zehn Diensten, die dieselbe Bibliothek nutzen, praktisch nicht durchhaltbar.
 */
class VerifierLegacyCompatibilityTest {

    private val vaultOperations: VaultOperations = mock()
    private val transitOperations: VaultTransitOperations = mock()
    private lateinit var verifier: VaultTransitMessageVerifier

    private val token = "svc-token"
    private val issuedAt = Instant.parse("2026-05-02T10:00:00Z")

    /** Traegt eine zonenbehaftete Zeit — nur dort unterscheiden sich alte und neue Serialisierung. */
    private data class Payload(val zeitpunkt: ZonedDateTime)

    private val envelope = SignedMessageEnvelope(
        payload = Payload(ZonedDateTime.parse("2026-05-02T12:00:00+02:00")),
        issuedAt = issuedAt,
        signature = "vault:v1:signatur",
        messageId = "urn:test:1",
    )

    @BeforeEach
    fun setUp() {
        whenever(vaultOperations.opsForTransit()).thenReturn(transitOperations)
        verifier = VaultTransitMessageVerifier(vaultOperations, "test-key")
    }

    private fun signingInput(mapper: tools.jackson.databind.ObjectMapper) = Plaintext.of(
        buildSigningInput(mapper, envelope.payload, envelope.issuedAt, envelope.exp, envelope.messageId, token, null),
    )

    @Test
    fun `akzeptiert eine mit der aktuellen Serialisierung signierte Nachricht`() {
        whenever(transitOperations.verify(any(), any<Plaintext>(), any<Signature>())).thenReturn(false)
        whenever(
            transitOperations.verify(eq("test-key"), eq(signingInput(CanonicalMessageMapper.mapper)), any<Signature>()),
        ).thenReturn(true)

        assertThat(verifier.verify(envelope, token)).isTrue()
    }

    @Test
    fun `akzeptiert eine mit der alten Serialisierung signierte Nachricht`() {
        whenever(transitOperations.verify(any(), any<Plaintext>(), any<Signature>())).thenReturn(false)
        whenever(
            transitOperations.verify(eq("test-key"), eq(signingInput(CanonicalMessageMapper.legacyMapper)), any<Signature>()),
        ).thenReturn(true)

        assertThat(verifier.verify(envelope, token)).isTrue()
    }

    @Test
    fun `lehnt ab, wenn keine der beiden Serialisierungen passt`() {
        // Der Rueckfall darf keine Nachricht durchlassen, deren Signatur zu keiner der beiden
        // definierten Serialisierungen passt.
        whenever(transitOperations.verify(any(), any<Plaintext>(), any<Signature>())).thenReturn(false)

        assertThat(verifier.verify(envelope, token)).isFalse()
        verify(transitOperations, times(2)).verify(any(), any<Plaintext>(), any<Signature>())
    }

    @Test
    fun `alte und neue Serialisierung unterscheiden sich bei zonenbehafteter Zeit`() {
        // Belegt die Notwendigkeit des Rueckfalls: Ohne Unterschied waere er ueberfluessig.
        assertThat(signingInput(CanonicalMessageMapper.mapper))
            .isNotEqualTo(signingInput(CanonicalMessageMapper.legacyMapper))
    }
}
