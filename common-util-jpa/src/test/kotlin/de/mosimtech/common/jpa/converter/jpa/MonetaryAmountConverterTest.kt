package de.mosimtech.common.jpa.converter.jpa

import de.mosimtech.common.jpa.converter.MonetaryAmountConverter
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.money.MonetaryAmount
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class MonetaryAmountConverterTest {

    private val converter = MonetaryAmountConverter()

    private fun MonetaryAmount.value(): BigDecimal = number.numberValue(BigDecimal::class.java)

    @Test
    fun `writes amount and currency like the former service copies`() {
        assertEquals("12.5 EUR", converter.convertToDatabaseColumn(Money.of(BigDecimal("12.50"), "EUR")))
        assertEquals("-3.99 USD", converter.convertToDatabaseColumn(Money.of(BigDecimal("-3.99"), "USD")))
    }

    @Test
    fun `never writes scientific notation`() {
        assertEquals("10 EUR", converter.convertToDatabaseColumn(Money.of(BigDecimal("1E+1"), "EUR")))
        val computed = Money.of(BigDecimal("2.5"), "EUR").multiply(4)
        assertEquals("10 EUR", converter.convertToDatabaseColumn(computed))
    }

    @Test
    fun `reads existing rows unchanged`() {
        val amount = converter.convertToEntityAttribute("500.00 EUR")!!
        assertEquals(0, BigDecimal("500.00").compareTo(amount.value()))
        assertEquals("EUR", amount.currency.currencyCode)
        assertEquals(0, BigDecimal("10").compareTo(converter.convertToEntityAttribute("1E+1 EUR")!!.value()))
    }

    @Test
    fun `round trips different currencies`() {
        listOf("EUR", "USD", "GBP", "CHF", "JPY").forEach { code ->
            val original = Money.of(BigDecimal("1234.56"), code)
            val restored = converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original))!!
            assertEquals(0, original.value().compareTo(restored.value()))
            assertEquals(code, restored.currency.currencyCode)
        }
    }

    @Test
    fun `null and blank stay null`() {
        assertNull(converter.convertToDatabaseColumn(null))
        assertNull(converter.convertToEntityAttribute(null))
        assertNull(converter.convertToEntityAttribute(""))
        assertNull(converter.convertToEntityAttribute("  "))
    }

    @Test
    fun `invalid format is rejected with the column value`() {
        val error = assertFailsWith<IllegalArgumentException> { converter.convertToEntityAttribute("invalid") }
        assertEquals("Invalid monetary amount format: invalid", error.message)
        assertFailsWith<IllegalArgumentException> { converter.convertToEntityAttribute("12,50 EUR") }
    }
}
