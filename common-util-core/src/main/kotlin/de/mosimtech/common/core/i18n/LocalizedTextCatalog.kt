package de.mosimtech.common.core.i18n

import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle

/** Liest die Textvorlagen des aufrufenden Dienstes und setzt deren Parameter ein. */
class LocalizedTextCatalog(
    baseName: String,
    language: AccountLanguage,
    classLoader: ClassLoader,
) {
    val locale: Locale = language.locale

    private val bundle = ResourceBundle.getBundle(baseName, locale, classLoader, CONTROL)

    fun text(key: String, vararg args: Any): String =
        MessageFormat(bundle.getString(key), locale).format(args.map(Any::toString).toTypedArray())

    private companion object {
        val CONTROL: ResourceBundle.Control =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
    }
}
