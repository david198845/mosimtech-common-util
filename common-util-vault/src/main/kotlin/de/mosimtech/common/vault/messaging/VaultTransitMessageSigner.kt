package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import java.time.Instant

/**
 * Signiert asynchrone Nachrichten über Vault Transit.
 *
 * @param digestThresholdBytes ab dieser Größe des serialisierten Payloads wird nur dessen SHA-256
 *   signiert ([PayloadSigningMode.DIGEST]), darunter der vollständige Payload. Der Standard liegt
 *   weit unter der JSON-String-Grenze von Vault (die Eingabe wächst durch zweifaches Base64 auf
 *   rund das 1,8-Fache). `Int.MAX_VALUE` schaltet den Hash-Modus praktisch ab, `0` erzwingt ihn.
 */
class VaultTransitMessageSigner(
    private val vaultOperations: VaultOperations,
    private val keyName: String,
    private val digestThresholdBytes: Int = DEFAULT_DIGEST_THRESHOLD_BYTES,
) {
    private val objectMapper = CanonicalMessageMapper.mapper

    /**
     * @param mode erzwingt einen Modus für diese Nachricht; `null` wählt ihn nach [digestThresholdBytes].
     */
    fun <T> sign(
        payload: T,
        serviceAccountToken: String,
        userToken: String? = null,
        exp: Instant? = null,
        issuedAt: Instant = Instant.now(),
        context: String? = null,
        mode: PayloadSigningMode? = null,
    ): SignedMessageEnvelope<T> {
        val messageId = generateMessageId(context)
        val payloadBytes = objectMapper.writeValueAsBytes(payload)
        val effectiveMode = mode ?: chooseMode(payloadBytes.size)
        val input = buildSigningInput(
            payloadBytes, issuedAt, exp, messageId, serviceAccountToken, userToken, effectiveMode
        )
        val vaultSignature = vaultOperations.opsForTransit()
            .sign(keyName, Plaintext.of(input))
        return SignedMessageEnvelope(
            payload = payload,
            issuedAt = issuedAt,
            exp = exp,
            signature = vaultSignature.signature,
            messageId = messageId,
            payloadSigning = effectiveMode.takeIf { it == PayloadSigningMode.DIGEST },
        )
    }

    private fun chooseMode(payloadSize: Int): PayloadSigningMode =
        if (payloadSize >= digestThresholdBytes) PayloadSigningMode.DIGEST else PayloadSigningMode.FULL

    companion object {
        const val DEFAULT_DIGEST_THRESHOLD_BYTES: Int = 64 * 1024
    }
}
