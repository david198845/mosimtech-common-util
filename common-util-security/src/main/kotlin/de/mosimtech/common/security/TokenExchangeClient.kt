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

    fun exchangeTokenFor(targetAudience: String): String =
        exchangeTokenFor(targetAudience, buildScopeName(targetAudience, async = false))

    fun exchangeAsyncTokenFor(targetAudience: String): String =
        exchangeTokenFor(targetAudience, buildScopeName(targetAudience, async = true))

    fun exchangeTokenFor(targetAudience: String, scope: String): String {
        val currentToken = SecurityContextAdapter.getCurrentToken()?.tokenValue
            ?: throw IllegalStateException("Kein aktives User-Token im SecurityContext gefunden!")

        val formParams = linkedMapOf(
            "client_id" to config.clientId,
            "client_secret" to config.clientSecret,
            "grant_type" to "urn:ietf:params:oauth:grant-type:token-exchange",
            "subject_token" to currentToken,
            "subject_token_type" to "urn:ietf:params:oauth:token-type:access_token",
            "audience" to targetAudience,
            "scope" to scope,
            "requested_token_type" to "urn:ietf:params:oauth:token-type:access_token"
        )

        val formData = formParams.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, StandardCharsets.UTF_8)}=${
                URLEncoder.encode(
                    it.value,
                    StandardCharsets.UTF_8
                )
            }"
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(resolveTokenUri()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw RuntimeException("Token Exchange fehlgeschlagen. Status: ${response.statusCode()}, Body: ${response.body()}")
        }

        val tokenResponse = objectMapper.readValue(response.body(), TokenExchangeResponse::class.java)

        return tokenResponse.accessToken
    }

    private fun resolveTokenUri(): String =
        config.tokenUri
            ?.takeIf { it.isNotBlank() }
            ?: config.issuerUri
                ?.takeIf { it.isNotBlank() }
                ?.let { "${it.trimEnd('/')}/protocol/openid-connect/token" }
            ?: throw IllegalStateException("Weder tokenUri noch issuerUri fuer den Token Exchange konfiguriert")

    private fun buildScopeName(targetAudience: String, async: Boolean): String {
        val serviceName = targetAudience
            .removeSuffix("-api")
            .substringAfterLast('-')
            .takeIf { it.isNotBlank() && it != targetAudience }
            ?: throw IllegalStateException(
                "Die Audience '$targetAudience' folgt nicht dem erwarteten Muster '<tenant>-<service>-api'"
            )

        return if (async) "async-exchange-to-$serviceName" else "exchange-to-$serviceName"
    }
}
