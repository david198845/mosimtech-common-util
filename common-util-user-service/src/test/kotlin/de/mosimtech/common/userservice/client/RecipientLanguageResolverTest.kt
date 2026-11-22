package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.urn.Urn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipientLanguageResolverTest {
    private val userId = requireNotNull(Urn.of("urn:user:momasoft:person"))
    private val calls = AtomicInteger()

    @Test
    fun `reads the language once and serves it from the cache`() {
        val resolver = resolver(clockAt(0)) { response("""{"data":{"language":"ru"}}""") }

        assertEquals(AccountLanguage.RUSSIAN, resolver.resolve(userId))
        assertEquals(AccountLanguage.RUSSIAN, resolver.resolve(userId))
        assertEquals(1, calls.get())
    }

    @Test
    fun `asks again after the cache entry expired`() {
        val clock = MutableClock()
        val resolver = resolver(clock) { response("""{"data":{"language":"en"}}""") }

        resolver.resolve(userId)
        clock.advance(Duration.ofMinutes(11))
        resolver.resolve(userId)

        assertEquals(2, calls.get())
    }

    @Test
    fun `falls back to German when the user service fails and does not retry immediately`() {
        val resolver = resolver(clockAt(0)) { Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build()) }

        assertEquals(AccountLanguage.DEFAULT, resolver.resolve(userId))
        assertEquals(AccountLanguage.DEFAULT, resolver.resolve(userId))
        assertEquals(1, calls.get())
    }

    @Test
    fun `falls back to German when the profile has no supported language`() {
        val resolver = resolver(clockAt(0)) { response("""{"data":{"language":"fr"}}""") }

        assertEquals(AccountLanguage.DEFAULT, resolver.resolve(userId))
    }

    private fun resolver(clock: Clock, answer: () -> Mono<ClientResponse>): RecipientLanguageResolver {
        val client = UserLocaleClient(WebClient.builder()
            .baseUrl("https://user-service/api")
            .exchangeFunction { calls.incrementAndGet(); answer() }
            .build())
        return RecipientLanguageResolver(client, { "token" }, clock = clock)
    }

    private fun clockAt(seconds: Long): Clock = Clock.fixed(Instant.ofEpochSecond(seconds), ZoneOffset.UTC)

    private fun response(body: String): Mono<ClientResponse> =
        Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build(),
        )

}
