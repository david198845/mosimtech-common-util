package de.mosimtech.common.vault.messaging

import java.time.Instant

/**
 * Signierte Wrapper-Klasse für asynchrone RabbitMQ-Nachrichten.
 *
 * **Receiver-Deserialisierung:** Da [SignedMessageEnvelope] generisch ist, muss der Empfänger
 * beim Deserialisieren den konkreten Payload-Typ explizit angeben, z.B.:
 * ```kotlin
 * val type = objectMapper.typeFactory.constructParametricType(
 *     SignedMessageEnvelope::class.java, IncomeMessage::class.java
 * )
 * val envelope: SignedMessageEnvelope<IncomeMessage> = objectMapper.readValue(json, type)
 * ```
 */
data class SignedMessageEnvelope<T>(
    val payload: T,
    val issuedAt: Instant,
    val exp: Instant? = null,
    val signature: String,
    val messageId: String,
) {
    /** Aus dem Vault-Signatur-Prefix abgeleitet (z.B. vault:v1:... → 1). Vault validiert die Version eigenständig über den Signature-String. */
    val keyVersion: Int
        get() = signature.split(":").getOrNull(1)
            ?.removePrefix("v")?.toIntOrNull()
            ?: error("Ungültiges Vault-Signatur-Format: $signature")
}
