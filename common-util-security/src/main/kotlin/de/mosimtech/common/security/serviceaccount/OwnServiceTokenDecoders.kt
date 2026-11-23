package de.mosimtech.common.security.serviceaccount

import de.mosimtech.common.security.validator.AzpValidator
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder

/**
 * Decoder für das Service-Token einer Nachricht, die ein Dienst sich selbst schickt (z. B. eine Invalidierung an alle
 * eigenen Instanzen): Signatur gegen den JWK-Satz des Ausstellers, dazu [AzpValidator.forOwnServiceToken].
 *
 * Aussteller und JWK-Adresse kommen aus der Registrierung des eigenen Dienstkontos (über `issuer-uri` beim Start
 * ermittelt). Die Schlüssel lädt der Decoder erst beim ersten Token und hält sie dann vor.
 */
object OwnServiceTokenDecoders {

    /** @throws IllegalStateException wenn der Registrierung `issuer-uri` oder `jwk-set-uri` fehlt */
    fun fromRegistration(registration: ClientRegistration): JwtDecoder {
        val provider = registration.providerDetails
        val issuer = provider.issuerUri
            ?: error("Client registration '${registration.registrationId}' has no issuer-uri")
        val jwkSetUri = provider.jwkSetUri
            ?: error("Client registration '${registration.registrationId}' has no jwk-set-uri")
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build()
            .apply { setJwtValidator(AzpValidator.forOwnServiceToken(issuer, registration.clientId)) }
    }
}
