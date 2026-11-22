package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.urn.Urn
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import tools.jackson.databind.JsonNode
import java.time.Duration

/** Liest die Kontosprache. Dienstauflösung und Authentifizierung bleiben beim Aufrufer. */
class UserLocaleClient(
    private val client: WebClient,
    private val timeout: Duration = Duration.ofSeconds(2),
) {
    /** Auch für WebFlux-Aufrufer nutzbar. HTTP-Fehler werden an den Aufrufer weitergegeben. */
    fun findLanguage(userId: Urn, accessToken: String): Mono<AccountLanguage> =
        client.get()
            .uri("/v1/users/{id}/locale", userId.toUrnString())
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .mapNotNull { body ->
                AccountLanguage.fromTag(body.path("data").path("language").asText(null))
            }
            .timeout(timeout)
}
