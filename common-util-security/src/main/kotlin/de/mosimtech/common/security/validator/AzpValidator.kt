package de.mosimtech.common.security.validator

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Validates the "azp" (Authorized Party) claim in a JSON Web Token (JWT) against a list of allowed values.
 *
 * This validator ensures that the calling client, identified by the "azp" claim, is included in the predefined whitelist
 * of allowed clients. If the "azp" claim is not present in the JWT or its value is not in the allowed list, the token
 * validation fails with an error.
 *
 * @property allowedAzp A list of allowed values for the "azp" claim.
 */
class AzpValidator(private val allowedAzp: List<String>) : OAuth2TokenValidator<Jwt> {
    /**
     * Validates the "azp" (Authorized Party) claim in the given JSON Web Token (JWT).
     *
     * This method checks if the "azp" claim is present and its value is included in the
     * predefined list of allowed values. If the claim is valid, the validation succeeds.
     * Otherwise, it returns a validation failure with an appropriate error message.
     *
     * @param jwt The JSON Web Token (JWT) to validate.
     * @return The result of the token validation. Returns a successful result if the "azp"
     *         claim is valid, or a failure result with an OAuth2Error if the claim is missing
     *         or not in the allowed list.
     */
    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        val azp = jwt.getClaimAsString("azp")
        if (azp != null && allowedAzp.contains(azp)) {
            return OAuth2TokenValidatorResult.success()
        }
        val error = OAuth2Error("invalid_token", "The calling client (azp) '$azp' is not whitelisted", null)
        return OAuth2TokenValidatorResult.failure(error)
    }
}
