package de.mosimtech.common.security.validator

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Validates the audience claim in a JSON Web Token (JWT) against a required audience value.
 *
 * This validator ensures that the audience claim in the JWT includes the specified required audience.
 * If the audience claim is missing, null, or does not contain the required audience, the token
 * validation will fail with an appropriate error message. Otherwise, the validation will succeed.
 *
 * @property requiredAudience The audience value that must be present in the JWT audience claim.
 */
class AudienceValidator(private val requiredAudience: String) : OAuth2TokenValidator<Jwt> {
    /**
     * Validates the audience claim in the given JSON Web Token (JWT) against the required audience value.
     *
     * This method checks whether the audience claim in the JWT is not null and contains the required audience.
     * If the audience is valid, the validation succeeds. Otherwise, it returns a validation failure with an
     * appropriate error message.
     *
     * @param jwt The JSON Web Token (JWT) to validate.
     * @return The result of the token validation. Returns a successful result if the audience claim
     *         contains the required audience, or a failure result with an OAuth2Error if the claim
     *         is null or does not include the required audience.
     */
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val audiences = jwt.audience
        if (audiences != null && audiences.contains(requiredAudience)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The required audience '$requiredAudience' is missing", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}
