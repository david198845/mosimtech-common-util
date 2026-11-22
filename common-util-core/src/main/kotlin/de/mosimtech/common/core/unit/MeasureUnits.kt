package de.mosimtech.common.core.unit

import tech.units.indriya.format.SimpleUnitFormat
import tech.units.indriya.function.RationalNumber
import tech.units.indriya.unit.Units
import java.math.BigDecimal
import javax.measure.Unit
import javax.measure.quantity.Mass
import javax.measure.quantity.Temperature

/**
 * Die Einheiten der Plattform und ihre Zeichen, wie sie in der Datenbank und in der API stehen
 * (`kg`, `lb`, `°C`, `°F`).
 *
 * Indriya kennt nur SI-Einheiten; Pfund und Fahrenheit sind hier exakt definiert
 * (1 lb = 0,45359237 kg; °F = °R − 459,67 mit 1 °R = 5/9 K). Die Zeichen werden bewusst nicht in
 * Indriyas globalem [SimpleUnitFormat] registriert, damit andere Nutzer der Bibliothek im selben
 * Prozess unberührt bleiben.
 */
object MeasureUnits {

    val KILOGRAM: Unit<Mass> = Units.KILOGRAM
    val GRAM: Unit<Mass> = Units.GRAM
    val POUND: Unit<Mass> = Units.KILOGRAM.multiply(RationalNumber.of(45_359_237L, 100_000_000L))

    val CELSIUS: Unit<Temperature> = Units.CELSIUS
    val KELVIN: Unit<Temperature> = Units.KELVIN
    val FAHRENHEIT: Unit<Temperature> = Units.KELVIN.multiply(RationalNumber.of(5L, 9L)).shift(BigDecimal("459.67"))

    private val SYMBOLS: List<Pair<String, Unit<*>>> = listOf(
        "kg" to KILOGRAM,
        "g" to GRAM,
        "lb" to POUND,
        "°C" to CELSIUS,
        "°F" to FAHRENHEIT,
        "K" to KELVIN,
    )

    /** Schreibweisen, die beim Lesen zusätzlich akzeptiert werden. */
    private val ALIASES: Map<String, Unit<*>> = mapOf(
        "℃" to CELSIUS,
        "℉" to FAHRENHEIT,
        "lbs" to POUND,
    )

    /** Das Zeichen einer Einheit; Einheiten außerhalb der Tabelle in Indriyas Schreibweise. */
    fun symbolOf(unit: Unit<*>): String =
        SYMBOLS.firstOrNull { it.second == unit }?.first ?: SimpleUnitFormat.getInstance().format(unit)

    /** Die Einheit zu einem Zeichen; `null`, wenn es keine bekannte Einheit ist. */
    fun unitOf(symbol: String): Unit<*>? {
        val trimmed = symbol.trim()
        if (trimmed.isEmpty()) return null
        SYMBOLS.firstOrNull { it.first == trimmed }?.let { return it.second }
        ALIASES[trimmed]?.let { return it }
        return runCatching { SimpleUnitFormat.getInstance().parse(trimmed) }.getOrNull()
    }
}
