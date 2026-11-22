package de.mosimtech.common.core.i18n

import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import java.time.DayOfWeek
import java.time.ZoneId

/**
 * Formatvorgaben eines Nutzers aus dem Profil des User Service: Datum, Uhrzeit, Einheiten,
 * Wochenbeginn, Währung und Zeitzone.
 *
 * `AUTO` wird erst bei den `resolved…()`-Methoden aufgelöst, und zwar über das Land der Sprache
 * ([RegionFormatDefaults]): [region] aus dem Sprach-Tag (`en-US` → `US`), sonst das Land der
 * Sprache (`en` → `GB`).
 *
 * [currency] und [timezone] stehen nicht im Token; `null` heißt „unbekannt“. Wer sie braucht,
 * holt sie über den User Service oder nimmt [currencyOrDefault] / [timezoneOrDefault].
 */
data class FormatPreferences(
    val language: AccountLanguage = AccountLanguage.DEFAULT,
    val region: String? = null,
    val dateFormat: DateFormatPreference = DateFormatPreference.AUTO,
    val timeFormat: TimeFormatPreference = TimeFormatPreference.AUTO,
    val unitSystem: UnitSystem = UnitSystem.AUTO,
    val firstDayOfWeek: FirstDayOfWeekPreference = FirstDayOfWeekPreference.AUTO,
    /** ISO-4217-Code, z. B. `EUR`; nur die Vorgabe für neue Einträge. */
    val currency: String? = null,
    val timezone: ZoneId? = null,
) {

    /** Das Land, nach dem `AUTO` aufgelöst wird. */
    val effectiveRegion: String? get() = region ?: RegionFormatDefaults.defaultRegion(language)

    fun resolvedDateFormat(): DateFormatPreference =
        dateFormat.takeUnless { it == DateFormatPreference.AUTO } ?: RegionFormatDefaults.dateFormat(effectiveRegion)

    fun resolvedTimeFormat(): TimeFormatPreference =
        timeFormat.takeUnless { it == TimeFormatPreference.AUTO } ?: RegionFormatDefaults.timeFormat(effectiveRegion)

    fun resolvedUnitSystem(): UnitSystem =
        unitSystem.takeUnless { it == UnitSystem.AUTO } ?: RegionFormatDefaults.unitSystem(effectiveRegion)

    fun resolvedFirstDayOfWeek(): DayOfWeek =
        (firstDayOfWeek.takeUnless { it == FirstDayOfWeekPreference.AUTO }
            ?: RegionFormatDefaults.firstDayOfWeek(effectiveRegion)).dayOfWeek ?: DayOfWeek.MONDAY

    /** Dieselben Vorgaben ohne `AUTO`. */
    fun resolved(): FormatPreferences = copy(
        dateFormat = resolvedDateFormat(),
        timeFormat = resolvedTimeFormat(),
        unitSystem = resolvedUnitSystem(),
        firstDayOfWeek = FirstDayOfWeekPreference.valueOf(resolvedFirstDayOfWeek().name),
    )

    fun currencyOrDefault(): String = currency ?: DEFAULT_CURRENCY

    fun timezoneOrDefault(): ZoneId = timezone ?: DEFAULT_TIMEZONE

    companion object {
        /** Vorgaben des User Service für ein Profil ohne Wert. */
        const val DEFAULT_CURRENCY = "EUR"
        val DEFAULT_TIMEZONE: ZoneId = ZoneId.of("Europe/Berlin")

        /**
         * Vorgaben aus einem Sprach-Tag wie `en-US` (Claim `locale`, Profilfeld `language`): Sprache
         * und Land; alles Übrige bleibt `AUTO` bzw. unbekannt. Eine nicht unterstützte Sprache
         * ergibt [AccountLanguage.DEFAULT], ihr Land gilt trotzdem für `AUTO`.
         */
        fun forLanguageTag(languageTag: String?): FormatPreferences = FormatPreferences(
            language = AccountLanguage.fromTag(languageTag) ?: AccountLanguage.DEFAULT,
            region = RegionFormatDefaults.regionOf(languageTag),
        )
    }
}
