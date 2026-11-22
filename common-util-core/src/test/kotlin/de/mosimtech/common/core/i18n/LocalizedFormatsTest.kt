package de.mosimtech.common.core.i18n

import de.mosimtech.common.core.i18n.enums.DateFormatPreference
import de.mosimtech.common.core.i18n.enums.TimeFormatPreference
import de.mosimtech.common.core.i18n.enums.UnitSystem
import de.mosimtech.common.core.unit.MeasureUnits
import org.javamoney.moneta.Money
import tech.units.indriya.quantity.Quantities
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedFormatsTest {
    private val amount = Money.of(BigDecimal("1234.5"), "EUR")
    private val date = LocalDate.of(2026, 12, 31)
    private val afternoon = LocalTime.of(15, 5)

    @Test
    fun `formats amounts the way readers of the language expect`() {
        assertEquals("1.234,50 €", LocalizedFormats(AccountLanguage.GERMAN).money(amount).normalizeSpaces())
        assertEquals("€1,234.50", LocalizedFormats(AccountLanguage.ENGLISH).money(amount).normalizeSpaces())
        assertEquals("1 234,50 €", LocalizedFormats(AccountLanguage.RUSSIAN).money(amount).normalizeSpaces())
    }

    @Test
    fun `keeps the medium date style when only the language is known`() {
        val day = LocalDate.of(2026, 11, 22)
        assertEquals("22.11.2026", LocalizedFormats(AccountLanguage.GERMAN).date(day))
        assertEquals("Nov 22, 2026", LocalizedFormats(AccountLanguage.ENGLISH).date(day))
    }

    @Test
    fun `formats dates by the profile date format`() {
        assertEquals("31.12.2026", formats(DateFormatPreference.DMY_DOT).date(date))
        assertEquals("31/12/2026", formats(DateFormatPreference.DMY_SLASH).date(date))
        assertEquals("12/31/2026", formats(DateFormatPreference.MDY_SLASH).date(date))
        assertEquals("2026-12-31", formats(DateFormatPreference.ISO).date(date))
    }

    @Test
    fun `resolves AUTO dates over the language`() {
        assertEquals("31.12.2026", LocalizedFormats(FormatPreferences.forLanguageTag("de")).date(date))
        assertEquals("31/12/2026", LocalizedFormats(FormatPreferences.forLanguageTag("en")).date(date))
        assertEquals("12/31/2026", LocalizedFormats(FormatPreferences.forLanguageTag("en-US")).date(date))
    }

    @Test
    fun `formats times in 24 or 12 hours`() {
        assertEquals("15:05", formats(time = TimeFormatPreference.H24).time(afternoon))
        assertEquals("09:05", formats(time = TimeFormatPreference.H24).time(LocalTime.of(9, 5)))
        assertEquals("3:05 PM", formats(time = TimeFormatPreference.H12, tag = "en").time(afternoon))
        assertEquals("12:00 AM", formats(time = TimeFormatPreference.H12, tag = "en").time(LocalTime.MIDNIGHT))
        assertEquals("3:05 PM", LocalizedFormats(FormatPreferences.forLanguageTag("en-US")).time(afternoon))
    }

    @Test
    fun `formats instants in the profile timezone, Berlin when unknown`() {
        val instant = Instant.parse("2026-12-31T14:05:00Z")

        assertEquals("31.12.2026 15:05", LocalizedFormats(FormatPreferences()).dateTime(instant))
        assertEquals(
            "12/31/2026 9:05 AM",
            LocalizedFormats(FormatPreferences.forLanguageTag("en-US").copy(timezone = ZoneId.of("America/New_York")))
                .dateTime(instant),
        )
    }

    @Test
    fun `converts zoned values only when the profile timezone is known`() {
        val zoned = ZonedDateTime.of(LocalDateTime.of(2026, 12, 31, 15, 5), ZoneId.of("Europe/Berlin"))

        assertEquals("31.12.2026 15:05", LocalizedFormats(FormatPreferences()).dateTime(zoned))
        assertEquals(
            "31.12.2026 14:05",
            LocalizedFormats(FormatPreferences(timezone = ZoneId.of("Europe/London"))).dateTime(zoned),
        )
    }

    @Test
    fun `formats quantities with the number format of the language`() {
        val weight = Quantities.getQuantity(BigDecimal("33.069"), MeasureUnits.POUND)

        assertEquals("33,1 lb", LocalizedFormats(FormatPreferences()).quantity(weight))
        assertEquals("33.1 lb", LocalizedFormats(FormatPreferences.forLanguageTag("en")).quantity(weight))
        assertEquals("33.07 lb", LocalizedFormats(FormatPreferences.forLanguageTag("en")).quantity(weight, 2))
        assertEquals("21,5 °C", LocalizedFormats(FormatPreferences()).quantity(Quantities.getQuantity(21.5, MeasureUnits.CELSIUS)))
    }

    @Test
    fun `display quantities are converted into the unit of the profile`() {
        val imperial = LocalizedFormats(FormatPreferences(unitSystem = UnitSystem.IMPERIAL))

        assertEquals("33,1 lb", imperial.displayQuantity(Quantities.getQuantity(15, MeasureUnits.KILOGRAM)))
        assertEquals("70,7 °F", imperial.displayQuantity(Quantities.getQuantity(BigDecimal("21.5"), MeasureUnits.CELSIUS)))
        assertEquals(
            "15 kg",
            LocalizedFormats(FormatPreferences()).displayQuantity(Quantities.getQuantity(BigDecimal("33.0693393"), MeasureUnits.POUND)),
        )
    }

    private fun formats(
        date: DateFormatPreference = DateFormatPreference.AUTO,
        time: TimeFormatPreference = TimeFormatPreference.AUTO,
        tag: String = "de",
    ) = LocalizedFormats(FormatPreferences.forLanguageTag(tag).copy(dateFormat = date, timeFormat = time))

    /** Das JDK setzt geschützte bzw. schmale Leerzeichen; für den Vergleich genügt ein normales. */
    private fun String.normalizeSpaces() = replace(' ', ' ').replace(' ', ' ')
}
