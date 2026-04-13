package de.mosimtech.momasoft.shiftcalc.core.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

class AudienceValidator(private val requiredAudience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        if (audiences != null && audiences.contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The required audience '$requiredAudience' is missing", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}