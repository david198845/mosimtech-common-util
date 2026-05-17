package de.mosimtech.common.vault.messaging

import java.time.Duration

class MessageIdempotencyGuard(
    private val store: MessageIdStore,
    private val ttl: Duration = Duration.ofDays(10),
) {
    fun checkOrReject(envelope: SignedMessageEnvelope<*>) {
        if (!store.storeIfAbsent(envelope.messageId, ttl)) {
            throw DuplicateMessageException("Duplicate messageId rejected: ${envelope.messageId}")
        }
    }
}
