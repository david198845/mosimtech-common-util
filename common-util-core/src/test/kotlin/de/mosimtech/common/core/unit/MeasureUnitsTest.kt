package de.mosimtech.common.core.unit

import tech.units.indriya.function.RationalNumber
import tech.units.indriya.unit.Units
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MeasureUnitsTest {

    @Test
    fun `uses the plain symbols of the platform`() {
        assertEquals("kg", MeasureUnits.symbolOf(MeasureUnits.KILOGRAM))
        assertEquals("lb", MeasureUnits.symbolOf(MeasureUnits.POUND))
        assertEquals("°C", MeasureUnits.symbolOf(MeasureUnits.CELSIUS))
        assertEquals("°F", MeasureUnits.symbolOf(MeasureUnits.FAHRENHEIT))
    }

    @Test
    fun `reads symbols, aliases and other SI units`() {
        assertEquals(MeasureUnits.POUND, MeasureUnits.unitOf("lb"))
        assertEquals(MeasureUnits.CELSIUS, MeasureUnits.unitOf("℃"))
        assertEquals(MeasureUnits.FAHRENHEIT, MeasureUnits.unitOf(" °F "))
        assertEquals(Units.METRE, MeasureUnits.unitOf("m"))
        assertNull(MeasureUnits.unitOf("bananas"))
        assertNull(MeasureUnits.unitOf(""))
    }

    @Test
    fun `turns every number type into a plain decimal`() {
        assertEquals("50", QuantityNumbers.toPlainString(50))
        assertEquals("50", QuantityNumbers.toPlainString(50.0))
        assertEquals("21.5", QuantityNumbers.toPlainString(21.5))
        assertEquals("10", QuantityNumbers.toPlainString(BigDecimal("1E+1")))
        assertEquals("0.5", QuantityNumbers.toPlainString(RationalNumber.of(1L, 2L)))
    }
}
