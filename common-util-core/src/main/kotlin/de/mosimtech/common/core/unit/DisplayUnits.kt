package de.mosimtech.common.core.unit

import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.UnitSystem
import tech.units.indriya.quantity.Quantities
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.measure.Quantity
import javax.measure.quantity.Temperature
import javax.measure.Unit

/**
 * Rechnet eine Größe in die Anzeigeeinheit eines Einheitensystems um: Masse in kg bzw. lb,
 * Temperatur in °C bzw. °F. Größen anderer Art bleiben unverändert.
 *
 * Temperaturen gibt es in zwei Bedeutungen:
 * - **Messwert** (Ist- oder Solltemperatur): [toDisplay] — mit Nullpunktverschiebung,
 *   21,5 °C → 70,7 °F.
 * - **Abstand** (Hysterese, Abweichung vom Soll, Anstieg pro Stunde): [toDisplayDifference] — nur
 *   der Faktor 9/5, 2 °C → 3,6 °F. Ein Abstand über [toDisplay] wäre falsch (2 °C → 35,6 °F).
 *
 * `AUTO` wird wie [UnitSystem.METRIC] behandelt; vorher über
 * [FormatPreferences.resolvedUnitSystem] auflösen oder die Überladung mit [FormatPreferences]
 * nehmen. Gerundet wird nicht — dafür gibt es [round].
 */
object DisplayUnits {

    fun <Q : Quantity<Q>> displayUnit(unit: Unit<Q>, system: UnitSystem): Unit<Q> {
        val imperial = system == UnitSystem.IMPERIAL
        @Suppress("UNCHECKED_CAST")
        return when {
            unit.isCompatible(MeasureUnits.KILOGRAM) ->
                (if (imperial) MeasureUnits.POUND else MeasureUnits.KILOGRAM) as Unit<Q>
            unit.isCompatible(MeasureUnits.KELVIN) ->
                (if (imperial) MeasureUnits.FAHRENHEIT else MeasureUnits.CELSIUS) as Unit<Q>
            else -> unit
        }
    }

    fun <Q : Quantity<Q>> toDisplay(quantity: Quantity<Q>, system: UnitSystem): Quantity<Q> {
        val target = displayUnit(quantity.unit, system)
        return if (target == quantity.unit) quantity else quantity.to(target)
    }

    fun <Q : Quantity<Q>> toDisplay(quantity: Quantity<Q>, preferences: FormatPreferences): Quantity<Q> =
        toDisplay(quantity, preferences.resolvedUnitSystem())

    /** In kg bzw. °C, z. B. um intern mit einer in lb oder °F gespeicherten Größe zu rechnen. */
    fun <Q : Quantity<Q>> toMetric(quantity: Quantity<Q>): Quantity<Q> = toDisplay(quantity, UnitSystem.METRIC)

    /**
     * Ein Temperatur*abstand* in der Anzeigeeinheit: °C bei METRIC (und AUTO), °F bei IMPERIAL.
     * Umgerechnet wird nur mit dem Faktor 9/5, ohne Nullpunkt: 2 °C → 3,6 °F, 2 K → 2 °C,
     * 3,6 °F → 2 °C. Das Ergebnis trägt `Scale.RELATIVE`; gespeichert wird die Skala nicht.
     */
    fun toDisplayDifference(delta: Quantity<Temperature>, system: UnitSystem): Quantity<Temperature> {
        val kelvin = kelvinDifference(delta)
        return if (system == UnitSystem.IMPERIAL) {
            Quantities.getQuantity(kelvin.multiply(NINE_FIFTHS), MeasureUnits.FAHRENHEIT, Quantity.Scale.RELATIVE)
        } else {
            Quantities.getQuantity(kelvin, MeasureUnits.CELSIUS, Quantity.Scale.RELATIVE)
        }
    }

    /** Ein Abstand in Grad Celsius (bzw. Kelvin), z. B. eine intern gerechnete Hysterese. */
    fun toDisplayDifference(celsiusDelta: Number, system: UnitSystem): Quantity<Temperature> =
        toDisplayDifference(Quantities.getQuantity(celsiusDelta, MeasureUnits.CELSIUS, Quantity.Scale.RELATIVE), system)

    fun toDisplayDifference(delta: Quantity<Temperature>, preferences: FormatPreferences): Quantity<Temperature> =
        toDisplayDifference(delta, preferences.resolvedUnitSystem())

    /** Rundet den Wert auf [fractionDigits] Nachkommastellen, die Einheit bleibt. */
    fun <Q : Quantity<Q>> round(
        quantity: Quantity<Q>,
        fractionDigits: Int,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ): Quantity<Q> = Quantities.getQuantity(
        QuantityNumbers.toBigDecimal(quantity.value).setScale(fractionDigits, roundingMode),
        quantity.unit,
    )

    /** Der Abstand in Kelvin: °F-Grade sind 5/9 K, °C- und K-Grade gleich groß. */
    private fun kelvinDifference(delta: Quantity<Temperature>): BigDecimal {
        val value = QuantityNumbers.toBigDecimal(delta.value)
        if (delta.unit == MeasureUnits.FAHRENHEIT) {
            return value.multiply(BigDecimal.valueOf(5)).divide(BigDecimal.valueOf(9), MathContext.DECIMAL64)
        }
        val converter = delta.unit.getConverterTo(MeasureUnits.KELVIN)
        val factor = QuantityNumbers.toBigDecimal(converter.convert(1))
            .subtract(QuantityNumbers.toBigDecimal(converter.convert(0)))
        return value.multiply(factor)
    }

    private val NINE_FIFTHS = BigDecimal("1.8")
}
