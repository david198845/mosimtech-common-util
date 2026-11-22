package de.mosimtech.common.jpa.converter

import de.mosimtech.common.core.unit.MeasureUnits
import de.mosimtech.common.core.unit.QuantityNumbers
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import tech.units.indriya.quantity.Quantities
import java.math.BigDecimal
import javax.measure.Quantity
import javax.measure.Unit

/**
 * Speichert eine Größe als `VARCHAR` „<Wert> <Einheit>“, analog zum `MonetaryAmountConverter`:
 * `50 kg`, `110 lb`, `21.5 °C`, `70.7 °F`.
 *
 * Der Wert steht immer mit Punkt und ohne wissenschaftliche Notation in der Spalte, unabhängig
 * von der Locale der JVM. Gespeichert wird die Größe in der Einheit, in der sie kommt — es wird
 * nicht umgerechnet.
 *
 * Beim Lesen wird nur exakt dieses Format angenommen (eine Zahl, ein Leerzeichen, ein bekanntes
 * Einheitenzeichen). Eine nackte Zahl, ein Komma als Dezimaltrennzeichen oder eine unbekannte
 * Einheit sind ein [IllegalArgumentException] mit dem Spaltenwert — Bestandsdaten bekommen ihre
 * Einheit per Migration, nicht durch Raten.
 *
 * [expectedUnit] prüft zusätzlich die Art der Größe (Masse, Temperatur); siehe
 * [MassQuantityConverter] und [TemperatureQuantityConverter].
 */
@Converter(autoApply = false)
open class QuantityConverter(
    private val expectedUnit: Unit<*>? = null,
) : AttributeConverter<Quantity<*>?, String?> {

    override fun convertToDatabaseColumn(attribute: Quantity<*>?): String? {
        if (attribute == null) return null
        checkKind(attribute.unit, attribute.toString())
        val symbol = MeasureUnits.symbolOf(attribute.unit)
        require(MeasureUnits.unitOf(symbol) == attribute.unit) {
            "Unit cannot be stored, its symbol '$symbol' does not read back: ${attribute.unit}"
        }
        return "${QuantityNumbers.toPlainString(attribute.value)} $symbol"
    }

    override fun convertToEntityAttribute(dbData: String?): Quantity<*>? {
        if (dbData.isNullOrBlank()) return null
        val match = requireNotNull(FORMAT.matchEntire(dbData.trim())) {
            "Invalid quantity format, expected '<value> <unit>': '$dbData'"
        }
        val (value, symbol) = match.destructured
        val unit = requireNotNull(MeasureUnits.unitOf(symbol)) { "Unknown unit '$symbol' in quantity: '$dbData'" }
        checkKind(unit, dbData)
        return quantity(BigDecimal(value), unit)
    }

    private fun checkKind(unit: Unit<*>, value: String) {
        val expected = expectedUnit ?: return
        require(unit.isCompatible(expected)) { "Quantity '$value' is not of the kind of $expected" }
    }

    @Suppress("UNCHECKED_CAST")
    private fun quantity(value: BigDecimal, unit: Unit<*>): Quantity<*> =
        Quantities.getQuantity(value, unit as Unit<Nothing>)

    private companion object {
        val FORMAT = Regex("""(-?\d+(?:\.\d+)?) (\S.*)""")
    }
}
