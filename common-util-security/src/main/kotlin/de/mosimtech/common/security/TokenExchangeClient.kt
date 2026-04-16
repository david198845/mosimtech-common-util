package de.mosimtech.common.security

import de.mosimtech.common.security.dto.TokenExchangeConfig
import de.mosimtech.common.security.dto.TokenExchangeResponse
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TokenExchangeClient(
    private val config: TokenExchangeConfig,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    // Jackson 3 ObjectMapper/JsonMapper Setup
    private val objectMapper: ObjectMapper = JsonMapper.builder()
        // Falls du das Jackson 3 Kotlin Modul hast: .addModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()
) {

    fun exchangeTokenFor(targetAudience: String): String {
        // 1. Spring Security JWT aus deinem Adapter holen
        val currentToken = SecurityContextAdapter.getCurrentToken()?.tokenValue
            ?: throw IllegalStateException("Kein aktives User-Token im SecurityContext gefunden!")

        // 2. Den Body nach RFC 8693 zusammenbauen
        val formParams = mapOf(
            "client_id" to config.clientId,
            "client_secret" to config.clientSecret,
            "grant_type" to "urn:ietf:params:oauth:grant-type:token-exchange",
            "subject_token" to currentToken,
            "subject_token_type" to "urn:ietf:params:oauth:token-type:access_token",
            "audience" to targetAudience,
            "scope" to "exchange-to-${targetAudience.substringAfterLast("-")}",
            "requested_token_type" to "urn:ietf:params:oauth:token-type:access_token"
        )

        // Map url-encodieren
        val formData = formParams.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, StandardCharsets.UTF_8)}=${
                URLEncoder.encode(
                    it.value,
                    StandardCharsets.UTF_8
                )
            }"
        }

        // 3. Request bauen und senden
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${config.issuerUri}/protocol/openid-connect/token"))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw RuntimeException("Token Exchange fehlgeschlagen. Status: ${response.statusCode()}, Body: ${response.body()}")
        }

        // 4. JSON mit Jackson 3 parsen
        val tokenResponse = objectMapper.readValue(response.body(), TokenExchangeResponse::class.java)

        return tokenResponse.accessToken
    }
}