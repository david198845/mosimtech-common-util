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

/**
 * Performs a Keycloak Token Exchange (RFC 8693) on behalf of the currently authenticated user.
 *
 * The user token is read from the [org.springframework.security.core.context.SecurityContext],
 * exchanged at Keycloak, and returned as a new token scoped down to the target audience.
 * Scope names are derived from the audience according to the project convention
 * `exchange-to-<service>` / `async-exchange-to-<service>` unless an explicit scope is provided.
 *
 * @param config Keycloak client credentials and token endpoint configuration.
 * @param httpClient HTTP client used to call the token endpoint (default: [HttpClient.newHttpClient]).
 * @param objectMapper Jackson mapper for deserializing the Keycloak token response.
 */
class TokenExchangeClient(
    private val config: TokenExchangeConfig,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    // Jackson 3 ObjectMapper/JsonMapper Setup
    private val objectMapper: ObjectMapper = JsonMapper.builder()
        // Falls du das Jackson 3 Kotlin Modul hast: .addModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .build()
) {

    /**
     * Exchanges the current user token for [targetAudience] and appends [additionalScopes]
     * after the automatically derived primary sync scope.
     *
     * The primary scope is built from [targetAudience] following the convention
     * `exchange-to-<service>` (e.g. `exchange-to-document` for `mosimtech-document-api`).
     * The [additionalScopes] are appended space-separated.
     *
     * @param targetAudience Client ID of the target service (pattern: `<tenant>-<service>-api`).
     * @param additionalScopes Extra scopes to request in addition to the primary scope.
     * @return The exchanged access token.
     * @throws IllegalStateException if no user token is present in the SecurityContext,
     *   [targetAudience] does not match the expected naming pattern, or no token URI is configured.
     */
    fun exchangeTokenFor(targetAudience: String, additionalScopes: List<String>): String {
        val primaryScope = buildScopeName(targetAudience, async = false)
        val fullScope = (listOf(primaryScope) + additionalScopes).joinToString(" ")
        return exchangeTokenFor(targetAudience, fullScope)
    }

    /**
     * Exchanges the current user token for [targetAudience].
     * The scope is automatically derived as `exchange-to-<service>`.
     *
     * @param targetAudience Client ID of the target service (pattern: `<tenant>-<service>-api`).
     * @return The exchanged access token.
     * @throws IllegalStateException if no user token is present in the SecurityContext,
     *   [targetAudience] does not match the expected naming pattern, or no token URI is configured.
     */
    fun exchangeTokenFor(targetAudience: String): String =
        exchangeTokenFor(targetAudience, buildScopeName(targetAudience, async = false))

    /**
     * Exchanges the current user token for [targetAudience] in an asynchronous messaging context.
     * The scope is automatically derived as `async-exchange-to-<service>`.
     *
     * @param targetAudience Client ID of the target service (pattern: `<tenant>-<service>-api`).
     * @return The exchanged access token for the async channel.
     * @throws IllegalStateException if no user token is present in the SecurityContext,
     *   [targetAudience] does not match the expected naming pattern, or no token URI is configured.
     */
    fun exchangeAsyncTokenFor(targetAudience: String): String =
        exchangeTokenFor(targetAudience, buildScopeName(targetAudience, async = true))

    /**
     * Performs the actual token exchange request against the Keycloak token endpoint.
     *
     * @param targetAudience Client ID of the target service.
     * @param scope Full scope string (multiple scopes separated by spaces).
     * @return The exchanged access token.
     * @throws IllegalStateException if no user token is present in the SecurityContext
     *   or no token URI is configured.
     * @throws RuntimeException if Keycloak returns a non-200 status code.
     */
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
