package de.mosimtech.common.security.serviceaccount

import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.TokenExchangeOAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

/**
 * Baut einen [OAuth2AuthorizedClientManager], der ohne HTTP-Request-Kontext arbeitet (Scheduler, Zuhörer, Streams).
 *
 * Bewusst nur eine Fabrik und keine Auto-Konfiguration: Dienste halten oft mehrere benannte Manager (etwa einen je
 * Verwendungszweck) und spritzen sie per `@Qualifier` ein. Eine zusätzliche, automatisch angelegte Bean desselben
 * Typs würde jede Injektion ohne Qualifier mehrdeutig machen.
 */
object BackgroundAuthorizedClientManagers {

    /** Nur Client Credentials – das Token des eigenen Dienstkontos. */
    fun clientCredentials(registrations: ClientRegistrationRepository): OAuth2AuthorizedClientManager =
        create(registrations, tokenExchange = false)

    /**
     * Client Credentials und Token Exchange: Der Manager tauscht außerdem das Token des angemeldeten Nutzers gegen
     * eines für ein anderes Backend (Registrierung mit `grant-type: urn:ietf:params:oauth:grant-type:token-exchange`).
     */
    fun withTokenExchange(registrations: ClientRegistrationRepository): OAuth2AuthorizedClientManager =
        create(registrations, tokenExchange = true)

    private fun create(registrations: ClientRegistrationRepository, tokenExchange: Boolean): OAuth2AuthorizedClientManager {
        val providers = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials()
        if (tokenExchange) providers.provider(TokenExchangeOAuth2AuthorizedClientProvider())
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            registrations,
            InMemoryOAuth2AuthorizedClientService(registrations),
        ).apply { setAuthorizedClientProvider(providers.build()) }
    }
}
