package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.RegionFormatDefaults
import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import java.time.ZoneId

/**
 * Antwort von `GET /v1/users/{id}/locale`. Ältere User-Service-Fassungen liefern nur `language`
 * und `timezone`; fehlende oder unbekannte Formatfelder sind deshalb `AUTO`, fehlende Währung
 * und Zeitzone `null`.
 */
data class UserLocaleDTO(
    /** Sprach-Tag, wie im Profil gespeichert (z. B. `en-GB`). */
    val languageTag: String? = null,
    val timezone: ZoneId? = null,
    val timeFormat: TimeFormatPreference = TimeFormatPreference.AUTO,
    val dateFormat: DateFormatPreference = DateFormatPreference.AUTO,
    val unitSystem: UnitSystem = UnitSystem.AUTO,
    val firstDayOfWeek: FirstDayOfWeekPreference = FirstDayOfWeekPreference.AUTO,
    /** ISO-4217-Code; nur die Vorgabe für neue Einträge. */
    val currency: String? = null,
) {
    /** Die unterstützte Kontosprache; `null`, wenn das Profil eine andere Sprache hat. */
    val language: AccountLanguage? get() = AccountLanguage.fromTag(languageTag)

    fun toFormatPreferences(): FormatPreferences = FormatPreferences(
        language = language ?: AccountLanguage.DEFAULT,
        region = RegionFormatDefaults.regionOf(languageTag),
        dateFormat = dateFormat,
        timeFormat = timeFormat,
        unitSystem = unitSystem,
        firstDayOfWeek = firstDayOfWeek,
        currency = currency,
        timezone = timezone,
    )
}
