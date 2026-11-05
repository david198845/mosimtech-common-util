package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultTransitOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.time.Instant

/**
 * Große Payloads (Dokumente) überschreiten die JSON-String-Grenze von Vault, wenn sie vollständig in
 * die Signatur-Eingabe eingehen. Ab einer Schwelle wird deshalb nur der SHA-256 signiert.
 */
class PayloadDigestSigningTest {

    private val vaultOperations: VaultOperations = mock()
    private val transitOperations: VaultTransitOperations = mock()
    private val token = "svc-token"
    private val issuedAt = Instant.parse("2026-11-05T08:55:00Z")

    data class Document(val filename: String, val content: ByteArray)

    private val smallDocument = Document("beleg.txt", ByteArray(100) { 1 })
    private val largeDocument = Document("beleg.jpg", ByteArray(2 * 1024 * 1024) { (it % 251).toByte() })

    @BeforeEach
    fun setUp() {
        whenever(vaultOperations.opsForTransit()).thenReturn(transitOperations)
        whenever(transitOperations.sign(any(), any<Plaintext>())).thenReturn(Signature.of("vault:v7:sig"))
    }

    private fun signedInput(): String {
        val captor = argumentCaptor<Plaintext>()
        verify(transitOperations).sign(eq("test-key"), captor.capture())
        return String(captor.firstValue.plaintext, Charsets.UTF_8)
    }

    @Test
    fun `kleiner Payload wird wie bisher vollstaendig signiert`() {
        val signer = VaultTransitMessageSigner(vaultOperations, "test-key")

        val envelope = signer.sign(smallDocument, serviceAccountToken = token, issuedAt = issuedAt)

        assertThat(envelope.payloadSigning).isNull()
        val expected = buildSigningInput(
            CanonicalMessageMapper.mapper, smallDocument, issuedAt, null, envelope.messageId, token, null
        )
        assertThat(signedInput()).isEqualTo(String(expected, Charsets.UTF_8))
    }

    @Test
    fun `grosser Payload wird ueber den Hash signiert und bleibt klein`() {
        val signer = VaultTransitMessageSigner(vaultOperations, "test-key")

        val envelope = signer.sign(largeDocument, serviceAccountToken = token, issuedAt = issuedAt)

        assertThat(envelope.payloadSigning).isEqualTo(PayloadSigningMode.DIGEST)
        val input = signedInput()
        assertThat(input.substringBefore("|")).matches("sha256:[0-9a-f]{64}")
        assertThat(input.length).isLessThan(1024)
    }

    @Test
    fun `Modus pro Aufruf erzwingbar`() {
        val signer = VaultTransitMessageSigner(vaultOperations, "test-key")

        val envelope = signer.sign(smallDocument, serviceAccountToken = token, mode = PayloadSigningMode.DIGEST)

        assertThat(envelope.payloadSigning).isEqualTo(PayloadSigningMode.DIGEST)
        assertThat(signedInput()).startsWith(DIGEST_SEGMENT_PREFIX)
    }

    @Test
    fun `Schwelle Int MAX_VALUE schaltet den Hash-Modus ab`() {
        val signer = VaultTransitMessageSigner(vaultOperations, "test-key", digestThresholdBytes = Int.MAX_VALUE)

        val envelope = signer.sign(largeDocument, serviceAccountToken = token)

        assertThat(envelope.payloadSigning).isNull()
    }

    @Test
    fun `Verifier akzeptiert Hash-Signatur mit genau einem Vault-Aufruf`() {
        val envelope = digestEnvelope(largeDocument)
        stubVerifyTrueOnlyFor(digestInput(largeDocument, envelope))

        assertThat(VaultTransitMessageVerifier(vaultOperations, "test-key").verify(envelope, token)).isTrue()
        verify(transitOperations, times(1)).verify(any(), any<Plaintext>(), any<Signature>())
    }

    @Test
    fun `Verifier lehnt Hash-Signatur ab, wenn der Payload veraendert wurde`() {
        val original = digestEnvelope(largeDocument)
        stubVerifyTrueOnlyFor(digestInput(largeDocument, original))
        val tampered = original.copy(payload = largeDocument.copy(content = largeDocument.content.copyOf().also { it[0] = 99 }))

        assertThat(VaultTransitMessageVerifier(vaultOperations, "test-key").verify(tampered, token)).isFalse()
    }

    @Test
    fun `Verifier lehnt ab, wenn der Modus in der Envelope nachtraeglich entfernt wurde`() {
        val original = digestEnvelope(largeDocument)
        stubVerifyTrueOnlyFor(digestInput(largeDocument, original))
        val downgraded = original.copy(payloadSigning = null)

        assertThat(VaultTransitMessageVerifier(vaultOperations, "test-key").verify(downgraded, token)).isFalse()
    }

    @Test
    fun `vollstaendige und Hash-Eingabe koennen sich nie gleichen`() {
        // Base64 kennt keinen Doppelpunkt — das Hash-Segment beginnt mit einem.
        val full = String(
            buildSigningInput(CanonicalMessageMapper.mapper, smallDocument, issuedAt, null, "m", token, null),
            Charsets.UTF_8,
        )
        assertThat(full.substringBefore("|")).doesNotContain(":")
        assertThat(DIGEST_SEGMENT_PREFIX).contains(":")
    }

    @Test
    fun `Modus-Feld wird bei vollstaendiger Signatur nicht geschrieben und bei Hash mitgeschickt`() {
        val mapper = jacksonObjectMapper()
        val full = SignedMessageEnvelope("x", issuedAt, null, "vault:v1:s", "m")
        val digest = full.copy(payloadSigning = PayloadSigningMode.DIGEST)

        assertThat(mapper.writeValueAsString(full)).doesNotContain("payloadSigning")
        val json = mapper.writeValueAsString(digest)
        assertThat(json).contains("\"payloadSigning\":\"DIGEST\"")
        assertThat(mapper.readValue<SignedMessageEnvelope<String>>(json).payloadSigning)
            .isEqualTo(PayloadSigningMode.DIGEST)
    }

    private fun digestEnvelope(document: Document) = SignedMessageEnvelope(
        payload = document,
        issuedAt = issuedAt,
        signature = "vault:v7:sig",
        messageId = "urn:test:doc",
        payloadSigning = PayloadSigningMode.DIGEST,
    )

    private fun digestInput(document: Document, envelope: SignedMessageEnvelope<Document>) = Plaintext.of(
        buildSigningInput(
            CanonicalMessageMapper.mapper, document, envelope.issuedAt, envelope.exp, envelope.messageId,
            token, null, PayloadSigningMode.DIGEST,
        )
    )

    private fun stubVerifyTrueOnlyFor(input: Plaintext) {
        whenever(transitOperations.verify(any(), any<Plaintext>(), any<Signature>())).thenReturn(false)
        whenever(transitOperations.verify(eq("test-key"), eq(input), any<Signature>())).thenReturn(true)
    }
}
