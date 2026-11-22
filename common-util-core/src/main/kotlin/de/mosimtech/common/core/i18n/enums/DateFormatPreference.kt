package de.mosimtech.common.core.i18n.enums

/**
 * Datumsformat aus dem Profil (Claim `date_format`); [AUTO] leitet es aus der Sprache ab.
 *
 * Entscheidend ist die Reihenfolge von Tag und Monat (03/04 ist in den USA der 4. März, in
 * Großbritannien der 3. April). Die Werte entsprechen denen des User Service.
 */
enum class DateFormatPreference(val pattern: String?) {
    AUTO(null),

    /** 31.12.2026 */
    DMY_DOT("dd.MM.uuuu"),

    /** 31/12/2026 */
    DMY_SLASH("dd/MM/uuuu"),

    /** 12/31/2026 */
    MDY_SLASH("MM/dd/uuuu"),

    /** 2026-12-31 */
    ISO("uuuu-MM-dd");

    companion object {
        /** Liest einen Claim- oder REST-Wert; fehlend oder unbekannt bedeutet [AUTO]. */
        fun fromValue(value: String?): DateFormatPreference =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: AUTO
    }
}
