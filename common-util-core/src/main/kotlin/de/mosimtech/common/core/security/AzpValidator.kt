package de.mosimtech.momasoft.shiftcalc.core.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AzpValidator(private val allowedAzp: List<String>) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val azp = jwt.getClaimAsString("azp")
        if (azp != null && allowedAzp.contains(azp)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The calling client (azp) '$azp' is not whitelisted", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}