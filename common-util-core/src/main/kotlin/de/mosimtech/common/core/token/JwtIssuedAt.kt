package de.mosimtech.common.core.token

import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.util.Base64

/**
 * Liest den `iat`-Claim aus einem JWT, ohne die Signatur zu prüfen.
 *
 * Nur für Tokens, deren Herkunft feststeht (das eigene Dienstkonto-Token vor dem Signieren einer Nachricht). Ein
 * empfangenes Token wird mit einem `JwtDecoder` geprüft, nicht hiermit.
 */
object JwtIssuedAt {

    private val mapper = JsonMapper.builder().build()

    /** @throws SecurityException wenn [token] kein JWT ist oder keinen numerischen `iat`-Claim hat */
    fun of(token: String): Instant {
        val parts = token.split('.')
        if (parts.size < 2) throw SecurityException("Token is not a JWT")
        val claims = try {
            mapper.readTree(Base64.getUrlDecoder().decode(parts[1]))
        } catch (e: Exception) {
            throw SecurityException("Token payload is not readable", e)
        }
        val iat = claims.path("iat")
        if (!iat.isNumber) throw SecurityException("Token has no iat claim")
        return Instant.ofEpochSecond(iat.asLong())
    }
}
