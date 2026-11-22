package de.mosimtech.common.core.unit

import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.UnitSystem
import tech.units.indriya.quantity.Quantities
import tech.units.indriya.unit.Units
import java.math.BigDecimal
import javax.measure.Quantity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DisplayUnitsTest {

    @Test
    fun `converts mass between kilograms and pounds`() {
        val pounds = DisplayUnits.toDisplay(Quantities.getQuantity(15, MeasureUnits.KILOGRAM), UnitSystem.IMPERIAL)

        assertEquals(MeasureUnits.POUND, pounds.unit)
        assertValue("33.07", DisplayUnits.round(pounds, 2))

        val kilograms = DisplayUnits.toDisplay(Quantities.getQuantity(110, MeasureUnits.POUND), UnitSystem.METRIC)
        assertEquals(MeasureUnits.KILOGRAM, kilograms.unit)
        assertValue("49.895", DisplayUnits.round(kilograms, 3))
    }

    @Test
    fun `one pound is exactly 0,45359237 kg`() {
        val kilograms = DisplayUnits.toMetric(Quantities.getQuantity(1, MeasureUnits.POUND))

        assertEquals(0, BigDecimal("0.45359237").compareTo(QuantityNumbers.toBigDecimal(kilograms.value)))
    }

    @Test
    fun `converts measured temperatures between Celsius and Fahrenheit`() {
        assertValue("70.7", DisplayUnits.round(DisplayUnits.toDisplay(celsius("21.5"), UnitSystem.IMPERIAL), 1))
        assertValue("32", DisplayUnits.round(DisplayUnits.toDisplay(celsius("0"), UnitSystem.IMPERIAL), 0))
        assertValue("-40", DisplayUnits.round(DisplayUnits.toDisplay(celsius("-40"), UnitSystem.IMPERIAL), 0))
        val back = DisplayUnits.toMetric(Quantities.getQuantity(BigDecimal("70.7"), MeasureUnits.FAHRENHEIT))
        assertEquals(MeasureUnits.CELSIUS, back.unit)
        assertValue("21.5", DisplayUnits.round(back, 1))
    }

    @Test
    fun `brings other SI units of a kind to the display unit`() {
        val grams = Quantities.getQuantity(1500, MeasureUnits.GRAM)
        assertValue("1.5", DisplayUnits.round(DisplayUnits.toDisplay(grams, UnitSystem.METRIC), 1))

        val kelvin = Quantities.getQuantity(BigDecimal("294.65"), MeasureUnits.KELVIN)
        assertValue("21.5", DisplayUnits.round(DisplayUnits.toDisplay(kelvin, UnitSystem.METRIC), 1))
    }

    @Test
    fun `leaves quantities of other kinds and already fitting ones unchanged`() {
        val length = Quantities.getQuantity(3, Units.METRE)
        assertSame(length, DisplayUnits.toDisplay(length, UnitSystem.IMPERIAL))

        val kilograms = Quantities.getQuantity(15, MeasureUnits.KILOGRAM)
        assertSame(kilograms, DisplayUnits.toDisplay(kilograms, UnitSystem.METRIC))
        assertSame(kilograms, DisplayUnits.toDisplay(kilograms, UnitSystem.AUTO))
    }

    @Test
    fun `resolves AUTO over the preferences`() {
        val pounds = DisplayUnits.toDisplay(Quantities.getQuantity(15, MeasureUnits.KILOGRAM), FormatPreferences.forLanguageTag("en-US"))

        assertEquals(MeasureUnits.POUND, pounds.unit)
    }

    @Test
    fun `rounds half up and keeps the unit`() {
        val rounded = DisplayUnits.round(Quantities.getQuantity(BigDecimal("2.25"), MeasureUnits.KILOGRAM), 1)

        assertValue("2.3", rounded)
        assertEquals(MeasureUnits.KILOGRAM, rounded.unit)
    }

    private fun celsius(value: String) = Quantities.getQuantity(BigDecimal(value), MeasureUnits.CELSIUS)

    private fun assertValue(expected: String, quantity: Quantity<*>) =
        assertEquals(0, BigDecimal(expected).compareTo(QuantityNumbers.toBigDecimal(quantity.value)), "was ${quantity.value}")
}
