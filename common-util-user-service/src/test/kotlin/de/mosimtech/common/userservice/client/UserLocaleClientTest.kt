package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.urn.Urn
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserLocaleClientTest {
    private val userId = requireNotNull(Urn.of("urn:user:momasoft:person"))

    @Test
    fun `uses the locale endpoint and the supplied token`() {
        val client = UserLocaleClient(WebClient.builder()
            .baseUrl("https://user-service/api")
            .exchangeFunction { request ->
                assertEquals("/api/v1/users/urn:user:momasoft:person/locale", request.url().path)
                assertEquals("Bearer service-token", request.headers().getFirst(HttpHeaders.AUTHORIZATION))
                response("""{"data":{"language":"en-GB","timezone":"Europe/Berlin"}}""")
            }
            .build())

        assertEquals(AccountLanguage.ENGLISH, client.findLanguage(userId, "service-token").block())
    }

    @Test
    fun `returns no language when the profile value is unsupported`() {
        val client = UserLocaleClient(WebClient.builder()
            .baseUrl("https://user-service/api")
            .exchangeFunction { response("""{"data":{"language":"fr"}}""") }
            .build())

        assertNull(client.findLanguage(userId, "service-token").block())
    }

    private fun response(body: String): Mono<ClientResponse> =
        Mono.just(
            ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build(),
        )
}
