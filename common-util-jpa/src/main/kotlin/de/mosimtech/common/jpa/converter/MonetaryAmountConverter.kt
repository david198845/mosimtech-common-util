package de.mosimtech.common.jpa.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.javamoney.moneta.Money
import java.math.BigDecimal
import javax.money.Monetary
import javax.money.MonetaryAmount

/**
 * Speichert Geldbeträge als `VARCHAR` „<Betrag> <Währung>“, z. B. `500.00 EUR` oder `12.5 EUR`.
 *
 * Das Format ist dasselbe wie in den bisherigen Kopien der Dienste (momasoft-financial,
 * momasoft-pellets-tracker): Bestehende Spalten bleiben ohne Migration lesbar, und neu
 * geschriebene Werte sehen aus wie bisher. Der Betrag steht mit Punkt und **ohne**
 * wissenschaftliche Notation in der Spalte (`toPlainString()`; `NumberValue.toString()` liefert
 * bei Werten aus Rechenoperationen sonst z. B. `1E+1` statt `10`). Ältere Zeilen in
 * wissenschaftlicher Notation werden weiterhin gelesen.
 *
 * Beim Lesen wird genau eine Zahl, ein Leerzeichen und ein ISO-4217-Code erwartet; alles andere
 * ist ein [IllegalArgumentException] mit dem Spaltenwert, eine unbekannte Währung die Ausnahme von
 * [Monetary.getCurrency].
 */
@Converter(autoApply = false)
class MonetaryAmountConverter : AttributeConverter<MonetaryAmount?, String?> {

    override fun convertToDatabaseColumn(attribute: MonetaryAmount?): String? =
        attribute?.let { "${it.number.numberValue(BigDecimal::class.java).toPlainString()} ${it.currency.currencyCode}" }

    override fun convertToEntityAttribute(dbData: String?): MonetaryAmount? {
        if (dbData.isNullOrBlank()) return null
        val parts = dbData.split(" ")
        require(parts.size == 2) { "Invalid monetary amount format: $dbData" }
        return Money.of(parts[0].toBigDecimal(), Monetary.getCurrency(parts[1]))
    }
}
