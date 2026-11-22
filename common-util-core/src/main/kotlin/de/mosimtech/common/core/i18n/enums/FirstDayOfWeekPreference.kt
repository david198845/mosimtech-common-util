package de.mosimtech.common.core.i18n.enums

import java.time.DayOfWeek

/**
 * Erster Tag der Woche aus dem Profil (Claim `first_day_of_week`); [AUTO] leitet ihn aus der
 * Sprache ab.
 */
enum class FirstDayOfWeekPreference(val dayOfWeek: DayOfWeek?) {
    AUTO(null),
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY);

    companion object {
        /** Liest einen Claim- oder REST-Wert; fehlend oder unbekannt bedeutet [AUTO]. */
        fun fromValue(value: String?): FirstDayOfWeekPreference =
            entries.firstOrNull { it.name.equals(value?.trim(), ignoreCase = true) } ?: AUTO
    }
}
