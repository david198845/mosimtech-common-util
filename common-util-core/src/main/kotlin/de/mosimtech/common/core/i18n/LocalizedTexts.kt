package de.mosimtech.common.core.i18n

import java.util.concurrent.ConcurrentHashMap

/**
 * Einstieg eines Dienstes in einen Textkatalog, z. B.
 * `LocalizedTexts("i18n/notifications", MyService::class.java.classLoader)`.
 *
 * Liefert je Sprache einen [LocalizedTextCatalog]; ohne bekannte Sprache gilt
 * [AccountLanguage.DEFAULT].
 */
class LocalizedTexts(
    val baseName: String,
    private val classLoader: ClassLoader,
) {
    private val catalogs = ConcurrentHashMap<AccountLanguage, LocalizedTextCatalog>()

    fun forLanguage(language: AccountLanguage?): LocalizedTextCatalog =
        (language ?: AccountLanguage.DEFAULT).let { lang ->
            catalogs.computeIfAbsent(lang) { LocalizedTextCatalog(baseName, it, classLoader) }
        }

    /** Prüft den Katalog über alle Sprachen, siehe [LocalizedCatalogCheck]. */
    fun problems(): List<String> = LocalizedCatalogCheck.problems(baseName, classLoader)
}
