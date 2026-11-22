package de.mosimtech.common.core.unit

import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.i18n.enums.UnitSystem
import tech.units.indriya.quantity.Quantities
import java.math.RoundingMode
import javax.measure.Quantity
import javax.measure.Unit

/**
 * Rechnet eine Größe in die Anzeigeeinheit eines Einheitensystems um: Masse in kg bzw. lb,
 * Temperatur in °C bzw. °F. Größen anderer Art bleiben unverändert.
 *
 * Temperaturen werden als Messwerte (absolute Skala) umgerechnet: 21,5 °C → 70,7 °F. Für
 * Temperatur*differenzen* ist das falsch (1 K Unterschied sind 1,8 °F, nicht 33,8 °F).
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

    /** Rundet den Wert auf [fractionDigits] Nachkommastellen, die Einheit bleibt. */
    fun <Q : Quantity<Q>> round(
        quantity: Quantity<Q>,
        fractionDigits: Int,
        roundingMode: RoundingMode = RoundingMode.HALF_UP,
    ): Quantity<Q> = Quantities.getQuantity(
        QuantityNumbers.toBigDecimal(quantity.value).setScale(fractionDigits, roundingMode),
        quantity.unit,
    )
}
