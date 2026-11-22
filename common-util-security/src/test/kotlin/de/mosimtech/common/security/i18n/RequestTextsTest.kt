package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.LocalizedTexts
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.http.server.ServletServerHttpResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RequestTextsTest {
    private val texts = RequestTexts(LocalizedTexts("i18n/request-texts", javaClass.classLoader))
    private val request = MockHttpServletRequest()

    @AfterTest
    fun cleanUp() {
        RequestContextHolder.resetRequestAttributes()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `the account language in the token wins over the browser header`() {
        request.addHeader("Accept-Language", "en")
        bind()
        authenticate(locale = "ru")

        assertEquals("Не найдено", texts.text("notFound"))
    }

    @Test
    fun `uses Accept-Language when the token carries no language`() {
        request.addHeader("Accept-Language", "fr;q=1.0, es;q=0.8, en;q=0.5")
        bind()

        assertEquals("No encontrado", texts.text("notFound"))
    }

    @Test
    fun `falls back to German without any hint`() {
        bind()

        assertEquals("Nicht gefunden", texts.text("notFound"))
    }

    @Test
    fun `declares the content language only after a localized text was produced`() {
        request.addHeader("Accept-Language", "en")
        bind()
        assertNull(writeHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE))

        texts.text("notFound")

        assertEquals("en", writeHeaders().getFirst(HttpHeaders.CONTENT_LANGUAGE))
    }

    @Test
    fun `parses weighted Accept-Language values`() {
        assertEquals(AccountLanguage.UKRAINIAN, RequestLanguage.fromAcceptLanguage("fr-FR, uk-UA;q=0.9, de;q=0.8"))
        assertNull(RequestLanguage.fromAcceptLanguage("fr, it"))
        assertNull(RequestLanguage.fromAcceptLanguage("??"))
    }

    private fun bind() = RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

    private fun authenticate(locale: String) {
        val jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user").claim("locale", locale).build()
        SecurityContextHolder.getContext().authentication = JwtAuthenticationToken(jwt)
    }

    private fun writeHeaders(): HttpHeaders {
        val response = ServletServerHttpResponse(MockHttpServletResponse())
        ContentLanguageResponseAdvice().beforeBodyWrite(
            "body",
            MethodParameter(String::class.java.getMethod("toString"), -1),
            MediaType.TEXT_PLAIN,
            StringHttpMessageConverter::class.java,
            ServletServerHttpRequest(request),
            response,
        )
        return response.headers
    }
}
