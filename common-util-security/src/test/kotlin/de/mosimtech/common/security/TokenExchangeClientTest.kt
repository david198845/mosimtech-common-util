package de.mosimtech.common.security

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import de.mosimtech.common.security.dto.TokenExchangeConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import java.net.InetSocketAddress
import java.net.http.HttpClient
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenExchangeClientTest {

    private lateinit var server: HttpServer
    private lateinit var capturedBody: AtomicReference<String>
    private lateinit var tokenUri: String

    @BeforeEach
    fun setUp() {
        capturedBody = AtomicReference("")
        server = HttpServer.create(InetSocketAddress(0), 0).apply {
            createContext("/realms/momasoft/protocol/openid-connect/token") { exchange: HttpExchange ->
                capturedBody.set(String(exchange.requestBody.readAllBytes(), StandardCharsets.UTF_8))
                val responseBody = """{"access_token":"exchanged-token","expires_in":60,"token_type":"Bearer"}"""
                exchange.sendResponseHeaders(200, responseBody.toByteArray(StandardCharsets.UTF_8).size.toLong())
                exchange.responseBody.use { it.write(responseBody.toByteArray(StandardCharsets.UTF_8)) }
            }
            start()
        }
        tokenUri = "http://localhost:${server.address.port}/realms/momasoft/protocol/openid-connect/token"

        val jwt = Jwt.withTokenValue("frontend-user-token")
            .header("alg", "none")
            .claim("sub", "user-1")
            .build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        server.stop(0)
    }

    @Test
    fun `should exchange token with explicit scope`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        val token = client.exchangeTokenFor("mosimtech-document-api", "exchange-to-document")

        assertEquals("exchanged-token", token)
        assertTrue(capturedBody.get().contains("audience=mosimtech-document-api"))
        assertTrue(capturedBody.get().contains("scope=exchange-to-document"))
    }

    @Test
    fun `should derive sync scope from audience`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        client.exchangeTokenFor("mosimtech-document-api")

        assertTrue(capturedBody.get().contains("scope=exchange-to-document"))
    }

    @Test
    fun `should derive async scope from audience`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        client.exchangeAsyncTokenFor("momasoft-finance-api")

        assertTrue(capturedBody.get().contains("scope=async-exchange-to-finance"))
    }

    @Test
    fun `should use issuer uri when token uri is not set`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                issuerUri = "http://localhost:${server.address.port}/realms/momasoft",
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        val token = client.exchangeTokenFor("mosimtech-document-api")

        assertEquals("exchanged-token", token)
    }

    @Test
    fun `should combine primary scope with additional scopes`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        val token = client.exchangeTokenFor("mosimtech-document-api", listOf("openid", "profile"))

        assertEquals("exchanged-token", token)
        assertTrue(capturedBody.get().contains("scope=exchange-to-document+openid+profile"))
        assertTrue(capturedBody.get().contains("audience=mosimtech-document-api"))
    }

    @Test
    fun `should use only primary scope when additional scopes list is empty`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        client.exchangeTokenFor("mosimtech-document-api", emptyList())

        assertTrue(capturedBody.get().contains("scope=exchange-to-document"))
    }

    @Test
    fun `should fail for invalid audience naming convention`() {
        val client = TokenExchangeClient(
            TokenExchangeConfig(
                tokenUri = tokenUri,
                clientId = "momasoft-shiftcalc-api",
                clientSecret = "super-secret"
            ),
            HttpClient.newHttpClient()
        )

        val exception = assertThrows<IllegalStateException> {
            client.exchangeTokenFor("document")
        }

        assertEquals(
            "Die Audience 'document' folgt nicht dem erwarteten Muster '<tenant>-<service>-api'",
            exception.message
        )
    }
}
