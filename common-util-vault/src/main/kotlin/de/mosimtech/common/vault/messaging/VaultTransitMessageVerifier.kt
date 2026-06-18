package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import java.time.Instant

class VaultTransitMessageVerifier(
    private val vaultOperations: VaultOperations,
    private val keyName: String,
) {
    private val objectMapper = CanonicalMessageMapper.mapper

    fun <T> verify(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean =
        isSignatureValid(envelope, serviceAccountToken, userToken)
            && isMessageNotExpired(envelope)

    private fun <T> isSignatureValid(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean {
        val input = buildSigningInput(
            objectMapper,
            envelope.payload,
            envelope.issuedAt,
            envelope.exp,
            envelope.messageId,
            serviceAccountToken,
            userToken
        )
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
