package de.mosimtech.common.security.serviceaccount

import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class OwnServiceTokenDecodersTest {

    private fun registration(issuer: String?, jwkSetUri: String?) = ClientRegistration.withRegistrationId("service-account")
        .clientId("pellets")
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .tokenUri("https://keycloak.example/token")
        .issuerUri(issuer)
        .jwkSetUri(jwkSetUri)
        .build()

    @Test
    fun `builds a jwk set decoder from the own registration`() {
        val decoder = OwnServiceTokenDecoders.fromRegistration(
            registration("https://keycloak.example/realms/momasoft", "https://keycloak.example/certs"),
        )
        assertIs<NimbusJwtDecoder>(decoder)
    }

    @Test
    fun `names the registration when issuer or jwk set uri is missing`() {
        val noIssuer = assertFailsWith<IllegalStateException> {
            OwnServiceTokenDecoders.fromRegistration(registration(null, "https://keycloak.example/certs"))
        }
        assertEquals("Client registration 'service-account' has no issuer-uri", noIssuer.message)

        val noJwks = assertFailsWith<IllegalStateException> {
            OwnServiceTokenDecoders.fromRegistration(registration("https://keycloak.example/realms/momasoft", null))
        }
        assertEquals("Client registration 'service-account' has no jwk-set-uri", noJwks.message)
    }
}
