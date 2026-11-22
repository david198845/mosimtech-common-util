package de.mosimtech.common.core.i18n

import java.util.Locale

/** Gemeinsame Sprachcodes für MoMaSoft-Kontoeinstellungen und Benachrichtigungen. */
enum class AccountLanguage(val code: String) {
    GERMAN("de"),
    ENGLISH("en"),
    SPANISH("es"),
    RUSSIAN("ru"),
    UKRAINIAN("uk");

    val locale: Locale get() = Locale.forLanguageTag(code)

    companion object {
        val DEFAULT = GERMAN

        fun fromTag(tag: String?): AccountLanguage? {
            val code = tag?.trim()?.replace('_', '-')?.substringBefore('-')?.lowercase(Locale.ROOT)
            return entries.firstOrNull { it.code == code }
        }
    }
}
