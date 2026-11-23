package de.mosimtech.common.vault.messaging

import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder

/**
 * Die Pflichtprüfung einer empfangenen [SignedMessageEnvelope], immer in dieser Reihenfolge:
 *
 * 1. Vault-Signatur und Verfall der Hülle ([VaultTransitMessageVerifier.verify]) – bindet Nutzlast
 *    und Tokens an die Nachricht,
 * 2. Idempotenz ([MessageIdempotencyGuard], `messageId`),
 * 3. Service-Token: mit [tokenDecoder] gelesen (Signatur gegen den JWK-Satz, dazu dessen
 *    Validatoren, z. B. `aud`/`azp` oder `AzpValidator.forOwnServiceToken`), zum Sendezeitpunkt der
 *    Hülle (`issuedAt`) gültig – **nicht** gegen jetzt –, danach die Prüfung des Aufrufers (Rolle),
 * 4. nur im Dual-Token-Flow: das User-Token genauso.
 *
 * Jede Ablehnung ist eine [SecurityException] (auch ein ungültiges Token, [BadJwtException]); der
 * Zuhörer macht daraus `AmqpRejectAndDontRequeueException`. Technische Fehler – Vault oder JWK-Satz
 * nicht erreichbar – gehen unverändert durch, damit der Zuhörer selbst über eine Wiederholung
 * entscheidet.
 *
 * Der [tokenDecoder] darf **keine** Zeitprüfung gegen jetzt enthalten (also nicht den
 * `JwtTimestampValidator` der Vorgabe von `NimbusJwtDecoder`), sonst scheitert jede Nachricht, die
 * länger in der Queue lag als das Token gültig ist. Braucht `spring-security-oauth2-jose` im Dienst
 * (hier nur `compileOnly`).
 */
class SignedMessageValidator(
    private val verifier: VaultTransitMessageVerifier,
    private val guard: MessageIdempotencyGuard,
    private val tokenDecoder: JwtDecoder,
) {

    /**
     * System-Flow: nur `X-Auth-Service-Token`.
     *
     * @param serviceToken der Header-Wert; `null`, wenn er fehlt
     * @param serviceTokenCheck fachliche Prüfung des gelesenen Tokens (z. B. Maschinen-Rolle);
     *   wirft bei Ablehnung eine [SecurityException]
     * @return das geprüfte Service-Token
     */
    fun <T> validateSystem(
        envelope: SignedMessageEnvelope<T>,
        serviceToken: String?,
        serviceTokenCheck: (Jwt) -> Unit = {},
    ): Jwt {
        val rawServiceToken = serviceToken ?: throw SecurityException("Missing header: $SERVICE_TOKEN_HEADER")
        checkEnvelope(envelope, rawServiceToken, null)
        return checkToken(envelope, rawServiceToken, "Service", serviceTokenCheck)
    }

    /**
     * Dual-Token-Flow: `X-Auth-Service-Token` und das per Token Exchange zugeschnittene
     * `X-Auth-User-Token`.
     *
     * @param userTokenCheck fachliche Prüfung des User-Tokens (z. B. `finance:write`)
     */
    fun <T> validateUser(
        envelope: SignedMessageEnvelope<T>,
        serviceToken: String?,
        userToken: String?,
        serviceTokenCheck: (Jwt) -> Unit = {},
        userTokenCheck: (Jwt) -> Unit = {},
    ): ValidatedMessageTokens {
        val rawServiceToken = serviceToken ?: throw SecurityException("Missing header: $SERVICE_TOKEN_HEADER")
        val rawUserToken = userToken ?: throw SecurityException("Missing header: $USER_TOKEN_HEADER")
        checkEnvelope(envelope, rawServiceToken, rawUserToken)
        return ValidatedMessageTokens(
            serviceToken = checkToken(envelope, rawServiceToken, "Service", serviceTokenCheck),
            userToken = checkToken(envelope, rawUserToken, "User", userTokenCheck),
        )
    }

    private fun <T> checkEnvelope(envelope: SignedMessageEnvelope<T>, serviceToken: String, userToken: String?) {
        if (!verifier.verify(envelope, serviceToken, userToken)) {
            throw SecurityException("Envelope signature or expiry invalid")
        }
        guard.checkOrReject(envelope)
    }

    private fun <T> checkToken(
        envelope: SignedMessageEnvelope<T>,
        token: String,
        kind: String,
        check: (Jwt) -> Unit,
    ): Jwt {
        val jwt = try {
            tokenDecoder.decode(token)
        } catch (e: BadJwtException) {
            throw SecurityException("$kind token rejected: ${e.message}", e)
        }
        val issuedAt = jwt.issuedAt ?: throw SecurityException("$kind token has no iat claim")
        val expiresAt = jwt.expiresAt ?: throw SecurityException("$kind token has no exp claim")
        if (!verifier.isTokenValidAtIssuance(envelope, issuedAt, expiresAt)) {
            throw SecurityException("$kind token was not valid when the message was sent")
        }
        check(jwt)
        return jwt
    }

    private companion object {
        // Wie X_AUTH_SERVICE_TOKEN / X_AUTH_USER_TOKEN in common-util-rabbitmq; nur für Meldungen
        const val SERVICE_TOKEN_HEADER = "X-Auth-Service-token"
        const val USER_TOKEN_HEADER = "X-Auth-User-Token"
    }
}
