package de.mosimtech.common.security.i18n

import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

/**
 * Setzt `Content-Language`, wenn während der Anfrage ein Text über [RequestTexts] entstanden
 * ist. Clients (z. B. die Web-Oberfläche) übernehmen Servermeldungen nur, wenn diese Angabe zu
 * ihrer Sprache passt; sonst zeigen sie einen eigenen Text.
 *
 * Greift auch für Antworten aus `@ExceptionHandler`-Methoden.
 */
@ControllerAdvice
class ContentLanguageResponseAdvice : ResponseBodyAdvice<Any> {

    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean = true

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse,
    ): Any? {
        RequestLanguage.used()?.let { response.headers.set(HttpHeaders.CONTENT_LANGUAGE, it.code) }
        return body
    }
}
