package de.mosimtech.common.jpa.converter.jpa

import de.mosimtech.common.core.unit.MeasureUnits
import de.mosimtech.common.core.unit.QuantityNumbers
import de.mosimtech.common.jpa.converter.MassQuantityConverter
import de.mosimtech.common.jpa.converter.QuantityConverter
import de.mosimtech.common.jpa.converter.TemperatureQuantityConverter
import tech.units.indriya.quantity.Quantities
import java.math.BigDecimal
import java.util.Locale
import javax.measure.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class QuantityConverterTest {
    private val converter = QuantityConverter()

    @Test
    fun `stores value and unit symbol`() {
        assertEquals("50 kg", converter.convertToDatabaseColumn(Quantities.getQuantity(50, MeasureUnits.KILOGRAM)))
        assertEquals("110 lb", converter.convertToDatabaseColumn(Quantities.getQuantity(110, MeasureUnits.POUND)))
        assertEquals("21.5 °C", converter.convertToDatabaseColumn(Quantities.getQuantity(21.5, MeasureUnits.CELSIUS)))
        assertEquals("70.7 °F", converter.convertToDatabaseColumn(Quantities.getQuantity(BigDecimal("70.70"), MeasureUnits.FAHRENHEIT)))
        assertEquals("-3.25 °C", converter.convertToDatabaseColumn(Quantities.getQuantity(BigDecimal("-3.25"), MeasureUnits.CELSIUS)))
        assertEquals("10 kg", converter.convertToDatabaseColumn(Quantities.getQuantity(BigDecimal("1E+1"), MeasureUnits.KILOGRAM)))
    }

    @Test
    fun `round-trips every platform unit including decimals`() {
        listOf("50 kg", "0.5 kg", "110 lb", "33.0693 lb", "21.5 °C", "-40 °C", "70.7 °F", "250 g", "294.65 K").forEach { stored ->
            val quantity = requireNotNull(converter.convertToEntityAttribute(stored))
            assertEquals(stored, converter.convertToDatabaseColumn(quantity))
        }
    }

    @Test
    fun `reads the unit and an exact decimal value`() {
        val quantity = requireNotNull(converter.convertToEntityAttribute("33.0693 lb"))

        assertEquals(MeasureUnits.POUND, quantity.unit)
        assertEquals(BigDecimal("33.0693"), QuantityNumbers.toBigDecimal(quantity.value))
    }

    @Test
    fun `always uses a dot regardless of the JVM locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("21.5 °C", converter.convertToDatabaseColumn(Quantities.getQuantity(21.5, MeasureUnits.CELSIUS)))
            assertEquals("21.5 °C", converter.convertToDatabaseColumn(converter.convertToEntityAttribute("21.5 °C")))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `accepts the Unicode degree signs as aliases`() {
        assertEquals("21.5 °C", converter.convertToDatabaseColumn(converter.convertToEntityAttribute("21.5 ℃")))
    }

    @Test
    fun `maps null and blank to null`() {
        assertNull(converter.convertToDatabaseColumn(null))
        assertNull(converter.convertToEntityAttribute(null))
        assertNull(converter.convertToEntityAttribute("  "))
    }

    @Test
    fun `rejects garbage instead of guessing`() {
        listOf("50", "50kg", "50,5 kg", "kg 50", "abc kg", "50 bananas", "1e3 kg", "50  kg", ".5 kg").forEach { stored ->
            assertFailsWith<IllegalArgumentException>("should reject '$stored'") { converter.convertToEntityAttribute(stored) }
        }
    }

    @Test
    fun `typed converters reject quantities of another kind`() {
        assertEquals("15 kg", MassQuantityConverter().convertToDatabaseColumn(Quantities.getQuantity(15, MeasureUnits.KILOGRAM)))
        assertFailsWith<IllegalArgumentException> { MassQuantityConverter().convertToEntityAttribute("21.5 °C") }
        assertFailsWith<IllegalArgumentException> { TemperatureQuantityConverter().convertToEntityAttribute("50 kg") }
        assertFailsWith<IllegalArgumentException> {
            TemperatureQuantityConverter().convertToDatabaseColumn(Quantities.getQuantity(15, MeasureUnits.KILOGRAM) as Quantity<*>)
        }
    }
}
