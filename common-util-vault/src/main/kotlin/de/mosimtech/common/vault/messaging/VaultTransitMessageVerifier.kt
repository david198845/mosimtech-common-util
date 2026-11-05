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
     * Der Modus kommt aus der Envelope; fehlt er, wurde vollständig signiert.
     *
     * Bei vollständiger Signatur wird gegen die aktuelle Serialisierung und, falls das fehlschlaegt,
     * gegen die bis 3.3.7 gueltige geprueft. So kann jeder Service einzeln aktualisiert werden, statt
     * dass Sender und Empfaenger gleichzeitig umgestellt werden muessen. Die Signatur muss in beiden
     * Faellen stimmen — akzeptiert werden nur diese zwei fest definierten Serialisierungen.
     *
     * Der Hash-Modus kam erst nach der UTC-Festlegung und kennt deshalb nur die aktuelle.
     */
    private fun <T> isSignatureValid(
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String? = null
    ): Boolean = when (envelope.payloadSigning ?: PayloadSigningMode.FULL) {
        PayloadSigningMode.FULL ->
            matchesSignature(CanonicalMessageMapper.mapper, envelope, serviceAccountToken, userToken, PayloadSigningMode.FULL)
                || matchesSignature(CanonicalMessageMapper.legacyMapper, envelope, serviceAccountToken, userToken, PayloadSigningMode.FULL)

        PayloadSigningMode.DIGEST ->
            matchesSignature(CanonicalMessageMapper.mapper, envelope, serviceAccountToken, userToken, PayloadSigningMode.DIGEST)
    }

    private fun <T> matchesSignature(
        objectMapper: tools.jackson.databind.ObjectMapper,
        envelope: SignedMessageEnvelope<T>,
        serviceAccountToken: String,
        userToken: String?,
        mode: PayloadSigningMode,
    ): Boolean {
        // Der Hash wird hier aus dem empfangenen Payload berechnet — nie aus einem mitgeschickten Wert.
        val input = buildSigningInput(
            objectMapper,
            envelope.payload,
            envelope.issuedAt,
            envelope.exp,
            envelope.messageId,
            serviceAccountToken,
            userToken,
            mode,
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
