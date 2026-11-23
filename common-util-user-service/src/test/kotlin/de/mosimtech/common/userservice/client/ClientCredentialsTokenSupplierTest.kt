package de.mosimtech.common.userservice.client

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.Instant
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ClientCredentialsTokenSupplierTest {

    private val manager: OAuth2AuthorizedClientManager = mock()
    private val supplier = ClientCredentialsTokenSupplier(manager, "service-account")

    @Test
    fun `authorizes the registration with its id as principal`() {
        val token = jwt(1_700_000_000)
        whenever(manager.authorize(any())).thenReturn(authorizedClient(token))

        assertEquals(token, supplier.accessToken())

        val captor = argumentCaptor<OAuth2AuthorizeRequest>()
        verify(manager).authorize(captor.capture())
        assertEquals("service-account", captor.firstValue.clientRegistrationId)
        assertEquals("service-account", captor.firstValue.principal.name)
    }

    @Test
    fun `issued at comes from the iat claim`() {
        assertEquals(Instant.ofEpochSecond(1_700_000_000), supplier.issuedAt(jwt(1_700_000_000)))
    }

    @Test
    fun `fails clearly when no token is issued`() {
        whenever(manager.authorize(any())).thenReturn(null)

        val error = assertFailsWith<IllegalStateException> { supplier.accessToken() }
        assertEquals("Could not obtain service account token for 'service-account'", error.message)
    }

    private fun authorizedClient(token: String): OAuth2AuthorizedClient {
        val registration = ClientRegistration.withRegistrationId("service-account")
            .clientId("pellets")
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri("https://keycloak.example/token")
            .build()
        val accessToken = OAuth2AccessToken(
            OAuth2AccessToken.TokenType.BEARER, token, Instant.now(), Instant.now().plusSeconds(300),
        )
        return OAuth2AuthorizedClient(registration, "service-account", accessToken)
    }

    private fun jwt(iat: Long): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        return "${encoder.encodeToString("{}".toByteArray())}.${encoder.encodeToString("""{"iat":$iat}""".toByteArray())}.s"
    }
}
