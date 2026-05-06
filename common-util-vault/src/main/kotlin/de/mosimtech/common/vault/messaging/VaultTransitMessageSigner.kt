package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import tools.jackson.databind.ObjectMapper
import java.time.Instant

class VaultTransitMessageSigner(
    private val vaultOperations: VaultOperations,
    private val objectMapper: ObjectMapper,
    private val keyName: String,
) {
    fun <T> sign(payload: T, exp: Instant? = null, issuedAt: Instant = Instant.now()): SignedMessageEnvelope<T> {
        val input = buildSigningInput(objectMapper, payload, issuedAt, exp)
        val vaultSignature = vaultOperations.opsForTransit()
            .sign(keyName, Plaintext.of(input))
        return SignedMessageEnvelope(
            payload = payload,
            issuedAt = issuedAt,
            exp = exp,
            signature = vaultSignature.signature,
        )
    }
}
