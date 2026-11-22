package de.mosimtech.common.core.i18n

import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizedFormatsTest {
    private val amount = Money.of(BigDecimal("1234.5"), "EUR")
    private val date = LocalDate.of(2026, 11, 22)

    @Test
    fun `formats amounts the way readers of the language expect`() {
        assertEquals("1.234,50 €", LocalizedFormats(AccountLanguage.GERMAN).money(amount).normalizeSpaces())
        assertEquals("€1,234.50", LocalizedFormats(AccountLanguage.ENGLISH).money(amount).normalizeSpaces())
        assertEquals("1 234,50 €", LocalizedFormats(AccountLanguage.RUSSIAN).money(amount).normalizeSpaces())
    }

    @Test
    fun `formats dates per language`() {
        assertEquals("22.11.2026", LocalizedFormats(AccountLanguage.GERMAN).date(date))
        assertEquals("Nov 22, 2026", LocalizedFormats(AccountLanguage.ENGLISH).date(date))
    }

    /** Das JDK setzt geschützte bzw. schmale Leerzeichen; für den Vergleich genügt ein normales. */
    private fun String.normalizeSpaces() = replace(' ', ' ').replace(' ', ' ')
}
