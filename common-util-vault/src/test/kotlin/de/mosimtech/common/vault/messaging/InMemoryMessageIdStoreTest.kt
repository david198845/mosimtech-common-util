package de.mosimtech.common.vault.messaging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class InMemoryMessageIdStoreTest {

    private var now = Instant.parse("2026-11-23T10:00:00Z")
    private val clock = object : Clock() {
        override fun getZone() = ZoneOffset.UTC
        override fun withZone(zone: java.time.ZoneId?) = this
        override fun instant() = now
    }
    private val store = InMemoryMessageIdStore(clock)

    @Test
    fun `accepts a message id once within its ttl`() {
        assertThat(store.storeIfAbsent("a", Duration.ofMinutes(1))).isTrue()
        assertThat(store.storeIfAbsent("a", Duration.ofMinutes(1))).isFalse()
        assertThat(store.storeIfAbsent("b", Duration.ofMinutes(1))).isTrue()
    }

    @Test
    fun `forgets ids after their ttl and does not grow`() {
        store.storeIfAbsent("a", Duration.ofMinutes(1))
        now = now.plus(Duration.ofMinutes(1))

        assertThat(store.storeIfAbsent("a", Duration.ofMinutes(1))).isTrue()
        now = now.plus(Duration.ofMinutes(2))
        store.storeIfAbsent("b", Duration.ofMinutes(1))
        assertThat(store.size()).isEqualTo(1)
    }

    @Test
    fun `works with the idempotency guard`() {
        val guard = MessageIdempotencyGuard(store, Duration.ofMinutes(1))
        val envelope = SignedMessageEnvelope("x", now, null, "vault:v1:s", "id-1")
        guard.checkOrReject(envelope)
        org.assertj.core.api.Assertions.assertThatThrownBy { guard.checkOrReject(envelope) }
            .isInstanceOf(DuplicateMessageException::class.java)
    }
}
