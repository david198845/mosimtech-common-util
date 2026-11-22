package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.security.AccountLanguageClaims
import org.springframework.util.ClassUtils
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.Locale

/**
 * Sprache der laufenden Anfrage: zuerst der `locale`-Claim des Tokens (Kontoeinstellung),
 * dann `Accept-Language`, sonst [AccountLanguage.DEFAULT].
 *
 * Merkt sich außerdem, ob in dieser Anfrage ein übersetzter Text entstanden ist. Nur dann setzt
 * [ContentLanguageResponseAdvice] den Header `Content-Language` — eine nicht übersetzte
 * Meldung darf nicht unter einer fremden Sprachangabe ausgeliefert werden.
 */
object RequestLanguage {

    private const val USED_ATTRIBUTE = "de.mosimtech.common.security.i18n.RequestLanguage.used"
    private const val ACCEPT_LANGUAGE = "Accept-Language"
    private val WEB_PRESENT = ClassUtils.isPresent(
        "org.springframework.web.context.request.RequestContextHolder",
        RequestLanguage::class.java.classLoader,
    )

    fun current(): AccountLanguage =
        AccountLanguageClaims.current() ?: fromAcceptLanguage(acceptLanguageHeader()) ?: AccountLanguage.DEFAULT

    /** Wählt die höchstgewichtete unterstützte Sprache aus einem `Accept-Language`-Wert. */
    fun fromAcceptLanguage(header: String?): AccountLanguage? {
        if (header.isNullOrBlank()) return null
        val ranges = runCatching { Locale.LanguageRange.parse(header) }.getOrNull() ?: return null
        return ranges.firstNotNullOfOrNull { AccountLanguage.fromTag(it.range) }
    }

    fun markUsed(language: AccountLanguage) {
        if (!WEB_PRESENT) return
        RequestContextHolder.getRequestAttributes()
            ?.setAttribute(USED_ATTRIBUTE, language, RequestAttributes.SCOPE_REQUEST)
    }

    fun used(): AccountLanguage? {
        if (!WEB_PRESENT) return null
        return RequestContextHolder.getRequestAttributes()
            ?.getAttribute(USED_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) as? AccountLanguage
    }

    private fun acceptLanguageHeader(): String? {
        if (!WEB_PRESENT) return null
        return (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)
            ?.request?.getHeader(ACCEPT_LANGUAGE)
    }
}
