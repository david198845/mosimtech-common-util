package de.mosimtech.common.security.i18n

import de.mosimtech.common.core.i18n.LocalizedTextCatalog
import de.mosimtech.common.core.i18n.LocalizedTexts

/**
 * Texte in der Sprache der laufenden Anfrage, z. B. für Fehlermeldungen einer REST-API:
 *
 * ```
 * private val messages = RequestTexts(LocalizedTexts("i18n/messages", javaClass.classLoader))
 * throw IllegalOperationException(messages.text("expense.notFound", id))
 * ```
 *
 * Jeder Aufruf vermerkt die verwendete Sprache, damit die Antwort `Content-Language` trägt.
 */
class RequestTexts(val texts: LocalizedTexts) {

    fun text(key: String, vararg args: Any): String = catalog().text(key, *args)

    fun plural(key: String, count: Long, vararg args: Any): String = catalog().plural(key, count, *args)

    fun plural(key: String, count: Int, vararg args: Any): String = catalog().plural(key, count, *args)

    private fun catalog(): LocalizedTextCatalog {
        val language = RequestLanguage.current()
        RequestLanguage.markUsed(language)
        return texts.forLanguage(language)
    }
}
