package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import tools.jackson.databind.ObjectMapper
import java.time.Instant

class VaultTransitMessageVerifier(
    private val vaultOperations: VaultOperations,
    private val objectMapper: ObjectMapper,
    private val keyName: String,
) {
    fun <T> verify(envelope: SignedMessageEnvelope<T>): Boolean =
        isSignatureValid(envelope)
            && isMessageNotExpired(envelope)


    private fun <T> isSignatureValid(envelope: SignedMessageEnvelope<T>): Boolean {
        val input = buildSigningInput(objectMapper, envelope.payload, envelope.issuedAt, envelope.exp)
        return vaultOperations.opsForTransit().verify(
            keyName,
            Plaintext.of(input),
            Signature.of(envelope.signature),
        )
    }

    private fun isMessageNotExpired(envelope: SignedMessageEnvelope<*>): Boolean =
        envelope.exp == null || Instant.now().isBefore(envelope.exp)

    // jwt.iat <= envelope.issuedAt < jwt.exp (exp ist exklusiv gemäß JWT RFC 7519 §4.1.4)
    fun <T> isTokenValidAtIssuance(envelope: SignedMessageEnvelope<T>, jwtIat: Instant, jwtExp: Instant): Boolean =
        !envelope.issuedAt.isBefore(jwtIat) && envelope.issuedAt.isBefore(jwtExp)
}
