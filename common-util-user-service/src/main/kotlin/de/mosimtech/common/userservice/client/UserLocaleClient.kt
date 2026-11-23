package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import de.mosimtech.common.core.urn.Urn
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import tools.jackson.databind.JsonNode
import java.time.Duration
import java.time.ZoneId
import java.util.Currency
import java.util.Locale

/** Liest Sprache und Formatvorgaben. Dienstauflösung und Authentifizierung bleiben beim Aufrufer. */
class UserLocaleClient(
    private val client: WebClient,
    private val timeout: Duration = Duration.ofSeconds(2),
) {
    /** Auch für WebFlux-Aufrufer nutzbar. HTTP-Fehler werden an den Aufrufer weitergegeben. */
    fun findLanguage(userId: Urn, accessToken: String): Mono<AccountLanguage> =
        findLocale(userId, accessToken).mapNotNull { it.language }

    /**
     * Das ganze Locale-Profil. Fehlende oder unbekannte Felder (ältere User-Service-Fassung) werden
     * nicht als Fehler behandelt, sondern als `AUTO` bzw. `null`. HTTP-Fehler gehen an den Aufrufer.
     */
    fun findLocale(userId: Urn, accessToken: String): Mono<UserLocaleDTO> =
        client.get()
            .uri("/v1/users/{id}/locale", userId.toUrnString())
            .headers { it.setBearerAuth(accessToken) }
            .retrieve()
            .bodyToMono(JsonNode::class.java)
            .map { body -> toLocale(body.path("data")) }
            .timeout(timeout)

    private fun toLocale(data: JsonNode): UserLocaleDTO = UserLocaleDTO(
        languageTag = data.text("language"),
        timezone = data.text("timezone")?.let { runCatching { ZoneId.of(it) }.getOrNull() },
        timeFormat = TimeFormatPreference.fromValue(data.text("timeFormat")),
        dateFormat = DateFormatPreference.fromValue(data.text("dateFormat")),
        unitSystem = UnitSystem.fromValue(data.text("unitSystem")),
        firstDayOfWeek = FirstDayOfWeekPreference.fromValue(data.text("firstDayOfWeek")),
        currency = data.text("currency")?.uppercase(Locale.ROOT)
            ?.let { code -> runCatching { Currency.getInstance(code).currencyCode }.getOrNull() },
    )

    private fun JsonNode.text(field: String): String? =
        path(field).takeIf { it.isString }?.asString()?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        /**
         * Ein Client ohne Netzverbindung, der für jeden Nutzer kein Profil kennt (`204 No Content`).
         * Grundlage der Resolver `defaultsOnly()`, wenn der User Service nicht gefragt werden soll.
         */
        fun offline(): UserLocaleClient =
            UserLocaleClient(WebClient.builder().exchangeFunction { Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build()) }.build())
    }
}
