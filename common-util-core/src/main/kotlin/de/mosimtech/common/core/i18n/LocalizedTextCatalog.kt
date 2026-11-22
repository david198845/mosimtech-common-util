package de.mosimtech.common.core.i18n

import java.text.MessageFormat
import java.util.Locale
import java.util.ResourceBundle

/**
 * Textvorlagen eines Dienstes in genau einer Sprache.
 *
 * Die Vorlagen liegen als `<baseName>.properties` (Deutsch, Vorgabe) und
 * `<baseName>_<code>.properties` im Klassenpfad. Parameter werden vor dem Einsetzen in Text
 * gewandelt, damit `MessageFormat` Jahreszahlen nicht gruppiert („2.026“). Zahlen und Beträge
 * formatiert der Aufrufer vorher mit [LocalizedFormats].
 *
 * Pluraltexte stehen unter `<key>.<kategorie>` (`one`, `few`, `many`, `other`), siehe
 * [PluralCategory]. Fehlt eine Kategorie, gilt `<key>.other`.
 */
class LocalizedTextCatalog(
    baseName: String,
    val language: AccountLanguage,
    classLoader: ClassLoader,
) {
    val locale: Locale = language.locale

    private val bundle = ResourceBundle.getBundle(baseName, locale, classLoader, CONTROL)

    fun text(key: String, vararg args: Any): String = format(bundle.getString(key), args)

    /** Pluraltext; die Menge steht in der Vorlage als `{0}`, weitere Parameter ab `{1}`. */
    fun plural(key: String, count: Long, vararg args: Any): String {
        val category = PluralCategory.of(language, count)
        val pattern = lookup("$key.${category.suffix}") ?: bundle.getString("$key.${PluralCategory.OTHER.suffix}")
        return format(pattern, arrayOf<Any>(count, *args))
    }

    fun plural(key: String, count: Int, vararg args: Any): String = plural(key, count.toLong(), *args)

    fun contains(key: String): Boolean = bundle.containsKey(key)

    private fun lookup(key: String): String? = if (bundle.containsKey(key)) bundle.getString(key) else null

    private fun format(pattern: String, args: Array<out Any>): String =
        MessageFormat(pattern, locale).format(args.map(Any::toString).toTypedArray())

    private companion object {
        val CONTROL: ResourceBundle.Control =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
    }
}
