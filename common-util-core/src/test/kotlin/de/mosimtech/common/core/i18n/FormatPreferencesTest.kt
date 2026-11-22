package de.mosimtech.common.core.i18n

import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.FirstDayOfWeekPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import java.time.DayOfWeek
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Spiegelt `resolve-formats.spec.ts` in ui-core: beide Seiten müssen AUTO gleich auflösen. */
class FormatPreferencesTest {

    private data class Row(
        val locale: String?,
        val date: DateFormatPreference,
        val time: TimeFormatPreference,
        val units: UnitSystem,
        val firstDay: DayOfWeek,
    )

    private val rows = listOf(
        Row(null, DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("de", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("de-AT", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("en", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("en-GB", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("en-US", DateFormatPreference.MDY_SLASH, TimeFormatPreference.H12, UnitSystem.IMPERIAL, DayOfWeek.SUNDAY),
        Row("en_CA", DateFormatPreference.ISO, TimeFormatPreference.H12, UnitSystem.METRIC, DayOfWeek.SUNDAY),
        Row("en-AU", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H12, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("en-IN", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H12, UnitSystem.METRIC, DayOfWeek.SUNDAY),
        Row("es", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("es-MX", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.SUNDAY),
        Row("es-419", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("ru", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("uk", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("sv", DateFormatPreference.ISO, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("fr", DateFormatPreference.DMY_SLASH, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
        Row("ja", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.SUNDAY),
        Row("he", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.SUNDAY),
        Row("my-MM", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.IMPERIAL, DayOfWeek.MONDAY),
        Row("zz", DateFormatPreference.DMY_DOT, TimeFormatPreference.H24, UnitSystem.METRIC, DayOfWeek.MONDAY),
    )

    @Test
    fun `AUTO follows the country of the language like ui-core`() {
        rows.forEach { row ->
            val preferences = FormatPreferences.forLanguageTag(row.locale)
            assertEquals(row.date, preferences.resolvedDateFormat(), "date for ${row.locale}")
            assertEquals(row.time, preferences.resolvedTimeFormat(), "time for ${row.locale}")
            assertEquals(row.units, preferences.resolvedUnitSystem(), "units for ${row.locale}")
            assertEquals(row.firstDay, preferences.resolvedFirstDayOfWeek(), "first day for ${row.locale}")
        }
    }

    @Test
    fun `an explicit value always wins over the language`() {
        val preferences = FormatPreferences.forLanguageTag("en-US").copy(
            dateFormat = DateFormatPreference.ISO,
            timeFormat = TimeFormatPreference.H24,
            unitSystem = UnitSystem.METRIC,
            firstDayOfWeek = FirstDayOfWeekPreference.MONDAY,
        )

        val resolved = preferences.resolved()

        assertEquals(DateFormatPreference.ISO, resolved.dateFormat)
        assertEquals(TimeFormatPreference.H24, resolved.timeFormat)
        assertEquals(UnitSystem.METRIC, resolved.unitSystem)
        assertEquals(FirstDayOfWeekPreference.MONDAY, resolved.firstDayOfWeek)
    }

    @Test
    fun `resolved replaces every AUTO`() {
        val resolved = FormatPreferences.forLanguageTag("en-US").resolved()

        assertEquals(DateFormatPreference.MDY_SLASH, resolved.dateFormat)
        assertEquals(TimeFormatPreference.H12, resolved.timeFormat)
        assertEquals(UnitSystem.IMPERIAL, resolved.unitSystem)
        assertEquals(FirstDayOfWeekPreference.SUNDAY, resolved.firstDayOfWeek)
    }

    @Test
    fun `unsupported languages fall back to German but keep their country`() {
        val preferences = FormatPreferences.forLanguageTag("fr-CA")

        assertEquals(AccountLanguage.DEFAULT, preferences.language)
        assertEquals("CA", preferences.region)
        assertEquals(DateFormatPreference.ISO, preferences.resolvedDateFormat())
    }

    @Test
    fun `currency and timezone are unknown until known, with platform defaults`() {
        val preferences = FormatPreferences()

        assertNull(preferences.currency)
        assertNull(preferences.timezone)
        assertEquals("EUR", preferences.currencyOrDefault())
        assertEquals(ZoneId.of("Europe/Berlin"), preferences.timezoneOrDefault())
        assertEquals("USD", preferences.copy(currency = "USD").currencyOrDefault())
    }

    @Test
    fun `reads claim values tolerantly`() {
        assertEquals(DateFormatPreference.MDY_SLASH, DateFormatPreference.fromValue("MDY_SLASH"))
        assertEquals(TimeFormatPreference.H12, TimeFormatPreference.fromValue(" h12 "))
        assertEquals(UnitSystem.AUTO, UnitSystem.fromValue(null))
        assertEquals(UnitSystem.AUTO, UnitSystem.fromValue("NAUTICAL"))
        assertEquals(FirstDayOfWeekPreference.SUNDAY, FirstDayOfWeekPreference.fromValue("SUNDAY"))
        assertEquals(FirstDayOfWeekPreference.AUTO, FirstDayOfWeekPreference.fromValue(""))
    }
}
