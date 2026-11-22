package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import de.mosimtech.common.core.urn.Urn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipientFormatResolverTest {
    private val userId = requireNotNull(Urn.of("urn:user:momasoft:person"))
    private val calls = AtomicInteger()

    @Test
    fun `reads the profile once and serves it from the cache`() {
        val resolver = resolver(MutableClock()) {
            response("""{"data":{"language":"en-US","timezone":"America/New_York","dateFormat":"ISO","unitSystem":"AUTO","currency":"USD"}}""")
        }

        val preferences = resolver.resolve(userId)
        resolver.resolve(userId)

        assertEquals(AccountLanguage.ENGLISH, preferences.language)
        assertEquals(DateFormatPreference.ISO, preferences.dateFormat)
        assertEquals(UnitSystem.IMPERIAL, preferences.resolvedUnitSystem())
        assertEquals("USD", preferences.currency)
        assertEquals(ZoneId.of("America/New_York"), preferences.timezone)
        assertEquals("2026-12-31", resolver.formats(userId).date(LocalDate.of(2026, 12, 31)))
        assertEquals(1, calls.get())
    }

    @Test
    fun `asks again after the cache entry expired or was evicted`() {
        val clock = MutableClock()
        val resolver = resolver(clock) { response("""{"data":{"language":"de"}}""") }

        resolver.resolve(userId)
        clock.advance(Duration.ofMinutes(11))
        resolver.resolve(userId)
        resolver.evict(userId)
        resolver.resolve(userId)

        assertEquals(3, calls.get())
    }

    @Test
    fun `never throws and uses the defaults while the user service fails`() {
        val clock = MutableClock()
        val resolver = resolver(clock) { Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build()) }

        assertEquals(FormatPreferences(), resolver.resolve(userId))
        assertEquals(FormatPreferences(), resolver.resolve(userId))
        assertEquals(1, calls.get())
        clock.advance(Duration.ofMinutes(2))
        resolver.resolve(userId)
        assertEquals(2, calls.get())
    }

    @Test
    fun `survives a broken token supplier`() {
        val client = UserLocaleClient(WebClient.builder().baseUrl("https://user-service/api")
            .exchangeFunction { response("{}") }.build())
        val resolver = RecipientFormatResolver(client, { error("no token") })

        assertEquals(FormatPreferences(), resolver.resolve(userId))
    }

    private fun resolver(clock: MutableClock, answer: () -> Mono<ClientResponse>): RecipientFormatResolver {
        val client = UserLocaleClient(WebClient.builder()
            .baseUrl("https://user-service/api")
            .exchangeFunction { calls.incrementAndGet(); answer() }
            .build())
        return RecipientFormatResolver(client, { "token" }, clock = clock)
    }

    private fun response(body: String): Mono<ClientResponse> =
        Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build(),
        )
}
