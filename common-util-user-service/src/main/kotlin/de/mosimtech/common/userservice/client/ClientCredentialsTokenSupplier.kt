package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.token.JwtIssuedAt
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import java.time.Instant

/**
 * [ServiceAccessTokenSupplier] über Client Credentials: das Token des eigenen Dienstkontos, ohne
 * HTTP-Request-Kontext – also auch aus Scheduler, RabbitMQ-Zuhörern und Streams. Das Token hält
 * [manager] vor und erneuert es erst kurz vor Ablauf.
 *
 * Als Bean bereitgestellt, richtet [UserServiceLanguageAutoConfiguration] die Resolver ein. Dasselbe
 * Token signiert auch asynchrone Nachrichten (System-Flow); `issuedAt` der Hülle muss dabei
 * mindestens der `iat` des Tokens sein, weil die Keycloak-Uhr der eigenen vorgehen kann:
 *
 * ```
 * val token = tokens.accessToken()
 * val envelope = signer.sign(
 *     payload = payload,
 *     serviceAccountToken = token,
 *     issuedAt = maxOf(Instant.now(), tokens.issuedAt(token)),
 *     context = applicationName,
 * )
 * ```
 *
 * Braucht `spring-security-oauth2-client` im Dienst (hier nur `compileOnly`).
 *
 * @param manager z. B. aus `BackgroundAuthorizedClientManagers.clientCredentials` (common-util-security)
 * @param registrationId die OAuth2-Client-Registrierung des Dienstkontos
 *   (`authorization-grant-type: client_credentials`); dient zugleich als Principal-Name
 */
class ClientCredentialsTokenSupplier(
    private val manager: OAuth2AuthorizedClientManager,
    private val registrationId: String,
) : ServiceAccessTokenSupplier {

    /** @throws IllegalStateException wenn Keycloak kein Token ausstellt */
    override fun accessToken(): String {
        val request = OAuth2AuthorizeRequest
            .withClientRegistrationId(registrationId)
            .principal(registrationId)
            .build()
        return manager.authorize(request)?.accessToken?.tokenValue
            ?: throw IllegalStateException("Could not obtain service account token for '$registrationId'")
    }

    /**
     * Der `iat`-Claim von [token], ohne Signaturprüfung – nur für das eigene, gerade geholte Token.
     *
     * @throws SecurityException wenn [token] keinen `iat`-Claim hat
     */
    fun issuedAt(token: String): Instant = JwtIssuedAt.of(token)
}
