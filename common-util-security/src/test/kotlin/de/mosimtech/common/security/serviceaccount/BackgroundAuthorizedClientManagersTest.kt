package de.mosimtech.common.security.serviceaccount

import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

class BackgroundAuthorizedClientManagersTest {

    private val registrations = InMemoryClientRegistrationRepository(
        ClientRegistration.withRegistrationId("service-account")
            .clientId("pellets")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("https://keycloak.example/token")
            .build(),
        ClientRegistration.withRegistrationId("login")
            .clientId("ui")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("https://app.example/callback")
            .authorizationUri("https://keycloak.example/auth")
            .tokenUri("https://keycloak.example/token")
            .build(),
    )

    @Test
    fun `builds managers that work without a request context`() {
        assertIs<AuthorizedClientServiceOAuth2AuthorizedClientManager>(BackgroundAuthorizedClientManagers.clientCredentials(registrations))
        assertIs<AuthorizedClientServiceOAuth2AuthorizedClientManager>(BackgroundAuthorizedClientManagers.withTokenExchange(registrations))
    }

    @Test
    fun `does not authorize grant types it was not built for`() {
        val manager = BackgroundAuthorizedClientManagers.clientCredentials(registrations)
        val request = OAuth2AuthorizeRequest.withClientRegistrationId("login").principal("someone").build()

        assertNull(manager.authorize(request))
    }

    @Test
    fun `rejects unknown registrations`() {
        val manager = BackgroundAuthorizedClientManagers.clientCredentials(registrations)
        val request = OAuth2AuthorizeRequest.withClientRegistrationId("missing").principal("someone").build()

        assertFailsWith<IllegalArgumentException> { manager.authorize(request) }
    }
}
