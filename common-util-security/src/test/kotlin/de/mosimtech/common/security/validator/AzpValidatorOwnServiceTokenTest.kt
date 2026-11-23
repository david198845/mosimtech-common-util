package de.mosimtech.common.security.validator

import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AzpValidatorOwnServiceTokenTest {

    private val validator = AzpValidator.forOwnServiceToken("https://keycloak.example/realms/momasoft", "pellets")

    private fun jwt(issuer: String, azp: String, expiresAt: Instant = Instant.now().plusSeconds(60)) =
        Jwt.withTokenValue("t")
            .header("alg", "RS256")
            .issuer(issuer)
            .claim("azp", azp)
            .issuedAt(expiresAt.minusSeconds(300))
            .expiresAt(expiresAt)
            .build()

    @Test
    fun `accepts the own service account from the own realm`() {
        assertFalse(validator.validate(jwt("https://keycloak.example/realms/momasoft", "pellets")).hasErrors())
    }

    @Test
    fun `rejects a foreign issuer or a foreign client`() {
        assertTrue(validator.validate(jwt("https://keycloak.example/realms/other", "pellets")).hasErrors())
        assertTrue(validator.validate(jwt("https://keycloak.example/realms/momasoft", "financial")).hasErrors())
    }

    @Test
    fun `does not check expiry against now`() {
        val expired = jwt("https://keycloak.example/realms/momasoft", "pellets", Instant.now().minusSeconds(3600))
        assertFalse(validator.validate(expired).hasErrors())
    }
}
