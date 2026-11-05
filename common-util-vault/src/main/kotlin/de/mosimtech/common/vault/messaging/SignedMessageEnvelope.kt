package de.mosimtech.common.vault.messaging

import com.fasterxml.jackson.annotation.JsonInclude
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
    /**
     * Wie der Payload signiert wurde; `null` heißt [PayloadSigningMode.FULL]. Bei FULL bleibt das Feld
     * bewusst leer und wird nicht geschrieben — so sieht die Nachricht für Empfänger mit älterer
     * Bibliothek genauso aus wie bisher. Manipulationssicher, obwohl nicht selbst signiert: Ein
     * geänderter Modus ergibt eine andere Signatur-Eingabe, und die Prüfung schlägt fehl.
     */
    @field:JsonInclude(JsonInclude.Include.NON_NULL)
    val payloadSigning: PayloadSigningMode? = null,
) {
    /** Aus dem Vault-Signatur-Prefix abgeleitet (z.B. vault:v1:... → 1). Vault validiert die Version eigenständig über den Signature-String. */
    val keyVersion: Int
        get() = signature.split(":").getOrNull(1)
            ?.removePrefix("v")?.toIntOrNull()
            ?: error("Ungültiges Vault-Signatur-Format: $signature")
}
