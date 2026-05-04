package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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

class VaultTransitMessageSignerTest {

    private val vaultOperations: VaultOperations = mock()
    private val transitOperations: VaultTransitOperations = mock()
    private val objectMapper = jacksonObjectMapper()
    private lateinit var signer: VaultTransitMessageSigner

    @BeforeEach
    fun setUp() {
        whenever(vaultOperations.opsForTransit()).thenReturn(transitOperations)
        signer = VaultTransitMessageSigner(vaultOperations, objectMapper, "test-key")
    }

    @Test
    fun `sign creates envelope with correct signature and issuedAt`() {
        whenever(transitOperations.sign(eq("test-key"), any<Plaintext>())).thenReturn(Signature.of("vault:v1:abc123"))

        val result = signer.sign("hello")

        assertThat(result.payload).isEqualTo("hello")
        assertThat(result.signature).isEqualTo("vault:v1:abc123")
        assertThat(result.keyVersion).isEqualTo(1)
        assertThat(result.exp).isNull()
        assertThat(result.issuedAt).isBeforeOrEqualTo(Instant.now())
    }

    @Test
    fun `sign stores provided exp in envelope`() {
        val exp = Instant.now().plusSeconds(3600)
        whenever(transitOperations.sign(any(), any<Plaintext>())).thenReturn(Signature.of("vault:v1:xyz"))

        val result = signer.sign("payload", exp = exp)

        assertThat(result.exp).isEqualTo(exp)
    }

    @Test
    fun `sign uses correct key name`() {
        val keyCaptor = argumentCaptor<String>()
        whenever(transitOperations.sign(keyCaptor.capture(), any<Plaintext>())).thenReturn(Signature.of("vault:v1:sig"))

        signer.sign("data")

        assertThat(keyCaptor.firstValue).isEqualTo("test-key")
    }

    @Test
    fun `sign passes Base64-encoded payload in signing input`() {
        val plaintextCaptor = argumentCaptor<Plaintext>()
        whenever(transitOperations.sign(any(), plaintextCaptor.capture())).thenReturn(Signature.of("vault:v1:ok"))

        signer.sign(mapOf("amount" to 42), exp = null)

        val capturedString = String(plaintextCaptor.firstValue.plaintext, Charsets.UTF_8)
        val parts = capturedString.split("|")
        assertThat(parts).hasSize(3)
        // erstes Segment ist Base64 (kein | im Base64-Alphabet)
        assertThat(parts[0]).matches("[A-Za-z0-9+/=]+")
        assertThat(parts[2]).isEqualTo("∞")
    }

    @Test
    fun `sign propagates VaultException when vault is unreachable`() {
        whenever(transitOperations.sign(any(), any<Plaintext>()))
            .thenThrow(VaultException("connection refused"))

        assertThatThrownBy { signer.sign("data") }
            .isInstanceOf(VaultException::class.java)
    }
}
