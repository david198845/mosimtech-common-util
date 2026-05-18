package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.time.Duration
import java.time.Instant
import java.util.*

class MessageIdempotencyGuardTest {

    private val store: MessageIdStore = mock()
    private val guard = MessageIdempotencyGuard(store, ttl = Duration.ofDays(10))

    private fun envelope(messageId: String = "urn:rabbitmq:test:${UUID.randomUUID()}") =
        SignedMessageEnvelope(
            payload = "payload",
            issuedAt = Instant.now(),
            signature = "vault:v1:sig",
            messageId = messageId
        )

    @BeforeEach
    fun setUp() {
        whenever(store.storeIfAbsent(any(), any())).thenReturn(true)
    }

    @Test
    fun `checkOrReject passes when messageId is new`() {
        val env = envelope()

        assertThatCode { guard.checkOrReject(env) }.doesNotThrowAnyException()
    }

    @Test
    fun `checkOrReject throws DuplicateMessageException when messageId already exists`() {
        val env = envelope()
        whenever(store.storeIfAbsent(eq(env.messageId), any())).thenReturn(false)

        assertThatThrownBy { guard.checkOrReject(env) }
            .isInstanceOf(DuplicateMessageException::class.java)
            .hasMessageContaining(env.messageId)
    }

    @Test
    fun `checkOrReject calls store with correct messageId and ttl`() {
        val id = "urn:rabbitmq:svc:${UUID.randomUUID()}"
        val env = envelope(id)

        guard.checkOrReject(env)

        verify(store).storeIfAbsent(id, Duration.ofDays(10))
    }

    @Test
    fun `default ttl is 10 days`() {
        val guardDefault = MessageIdempotencyGuard(store)
        val env = envelope()

        guardDefault.checkOrReject(env)

        verify(store).storeIfAbsent(any(), eq(Duration.ofDays(10)))
    }

    @Test
    fun `custom ttl is forwarded to store`() {
        val customGuard = MessageIdempotencyGuard(store, ttl = Duration.ofDays(14))
        val env = envelope()

        customGuard.checkOrReject(env)

        verify(store).storeIfAbsent(any(), eq(Duration.ofDays(14)))
    }
}
