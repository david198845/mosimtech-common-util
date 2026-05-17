package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant

class SignedMessageEnvelopeTest {

    @Test
    fun `envelope stores all fields correctly`() {
        val now = Instant.now()
        val exp = now.plusSeconds(3600)
        val envelope = SignedMessageEnvelope("payload", now, exp, "vault:v1:sig", "urn:rabbitmq:test:1", "svc-token")

        assertThat(envelope.payload).isEqualTo("payload")
        assertThat(envelope.issuedAt).isEqualTo(now)
        assertThat(envelope.exp).isEqualTo(exp)
        assertThat(envelope.signature).isEqualTo("vault:v1:sig")
    }

    @Test
    fun `keyVersion is derived from signature`() {
        val envelope =
            SignedMessageEnvelope("data", Instant.now(), null, "vault:v3:abc", "urn:rabbitmq:test:2", "svc-token")

        assertThat(envelope.keyVersion).isEqualTo(3)
    }

    @Test
    fun `envelope exp defaults to null`() {
        val envelope =
            SignedMessageEnvelope("data", Instant.now(), null, "vault:v1:sig", "urn:rabbitmq:test:3", "svc-token")

        assertThat(envelope.exp).isNull()
    }

    @Test
    fun `envelope copy creates independent copy with changed field`() {
        val original =
            SignedMessageEnvelope("data", Instant.now(), null, "vault:v1:sig", "urn:rabbitmq:test:4", "svc-token")
        val copy = original.copy(payload = "changed")

        assertThat(copy.payload).isEqualTo("changed")
        assertThat(original.payload).isEqualTo("data")
    }
}
