package de.mosimtech.common.core.unit

import tech.units.indriya.function.RationalNumber
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Wandelt den Wert einer Größe in ein [BigDecimal], unabhängig davon, welchen Zahltyp Indriya
 * nach einer Umrechnung liefert (Ganzzahl, Double, Bruch).
 */
object QuantityNumbers {

    fun toBigDecimal(value: Number): BigDecimal = when (value) {
        is BigDecimal -> value
        is BigInteger -> BigDecimal(value)
        is Long, is Int, is Short, is Byte -> BigDecimal.valueOf(value.toLong())
        is RationalNumber -> value.bigDecimalValue()
        is Double, is Float -> BigDecimal.valueOf(value.toDouble())
        else -> runCatching { BigDecimal(value.toString()) }.getOrElse { BigDecimal.valueOf(value.toDouble()) }
    }

    /** Ohne wissenschaftliche Notation und ohne überflüssige Nullen: `50`, `21.5`, `-3.25`. */
    fun toPlainString(value: Number): String = toBigDecimal(value).stripTrailingZeros().toPlainString()
}
