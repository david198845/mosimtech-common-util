package de.mosimtech.common.core.i18n

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import javax.money.MonetaryAmount

/**
 * Zahlen, Beträge und Daten so, wie sie Nutzer der jeweiligen Sprache lesen:
 * Deutsch „1.234,50 €“ und „22.11.2026“, Englisch „€1,234.50“ und „Nov 22, 2026“.
 */
class LocalizedFormats(val language: AccountLanguage) {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(language.locale)

    fun money(amount: MonetaryAmount): String =
        money(amount.number.numberValue(BigDecimal::class.java), amount.currency.currencyCode)

    fun money(amount: BigDecimal, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance(language.locale)
        runCatching { Currency.getInstance(currencyCode) }.getOrNull()?.let { format.currency = it }
        return format.format(amount.setScale(2, RoundingMode.HALF_UP))
    }

    fun number(value: Number): String = NumberFormat.getNumberInstance(language.locale).format(value)

    fun date(date: LocalDate): String = date.format(dateFormatter)

    companion object {
        fun of(language: AccountLanguage?): LocalizedFormats = LocalizedFormats(language ?: AccountLanguage.DEFAULT)
    }
}
