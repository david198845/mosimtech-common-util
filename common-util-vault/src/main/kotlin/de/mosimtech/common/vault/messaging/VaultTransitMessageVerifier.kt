package de.mosimtech.common.vault.messaging

import org.springframework.vault.core.VaultOperations
import org.springframework.vault.support.Plaintext
import org.springframework.vault.support.Signature
import java.time.Instant

class VaultTransitMessageVerifier(
    private val vaultOperations: VaultOperations,
    private val keyName: String,
) {
    fun <T> verify(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean =
        isSignatureValid(envelope, serviceAccountToken, userToken)
            && isMessageNotExpired(envelope)

    /**
     * Prueft gegen die aktuelle Serialisierung und, falls das fehlschlaegt, gegen die bis 3.3.7
     * gueltige. So kann jeder Service einzeln aktualisiert werden, statt dass Sender und
     * Empfaenger gleichzeitig umgestellt werden muessen. Die Signatur muss in beiden Faellen
     * stimmen — akzeptiert werden nur diese zwei fest definierten Serialisierungen.
     */
    private fun <T> isSignatureValid(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean =
        matchesSignature(CanonicalMessageMapper.mapper, envelope, serviceAccountToken, userToken)
            || matchesSignature(CanonicalMessageMapper.legacyMapper, envelope, serviceAccountToken, userToken)

    private fun <T> matchesSignature(
        objectMapper: tools.jackson.databind.ObjectMapper,
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String?,
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
