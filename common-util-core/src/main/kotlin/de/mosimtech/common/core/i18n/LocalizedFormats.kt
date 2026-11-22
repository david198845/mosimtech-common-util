package de.mosimtech.common.core.i18n

import de.mosimtech.common.core.unit.DisplayUnits
import de.mosimtech.common.core.unit.MeasureUnits
import de.mosimtech.common.core.unit.QuantityNumbers
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import javax.measure.Quantity
import javax.money.MonetaryAmount

/**
 * Zahlen, Beträge, Daten, Uhrzeiten und Größen so, wie sie der Nutzer lesen will.
 *
 * Mit [FormatPreferences] gelten die Formate aus dem Profil: Datum `31.12.2026`, `31/12/2026`,
 * `12/31/2026` oder `2026-12-31`, Uhrzeit `15:05` oder `3:05 PM`, Datum und Uhrzeit durch ein
 * Leerzeichen getrennt — genau wie die Pipes von ui-core. Zahlen und Beträge folgen der Sprache
 * (Deutsch „1.234,50 €“, Englisch „€1,234.50“).
 *
 * Wird nur mit einer Sprache erzeugt, bleibt [date] beim bisherigen mittleren Datumsformat der
 * Sprache („22.11.2026“, „Nov 22, 2026“); alle übrigen Methoden lösen `AUTO` über die Sprache auf.
 */
class LocalizedFormats private constructor(
    val preferences: FormatPreferences,
    private val profileDates: Boolean,
) {

    constructor(language: AccountLanguage) : this(FormatPreferences(language), false)

    constructor(preferences: FormatPreferences) : this(preferences, true)

    val language: AccountLanguage get() = preferences.language

    private val mediumDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(language.locale)

    private val profileDateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(requireNotNull(preferences.resolvedDateFormat().pattern), language.locale)

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern(requireNotNull(preferences.resolvedTimeFormat().pattern), language.locale)

    fun money(amount: MonetaryAmount): String =
        money(amount.number.numberValue(BigDecimal::class.java), amount.currency.currencyCode)

    fun money(amount: BigDecimal, currencyCode: String): String {
        val format = NumberFormat.getCurrencyInstance(language.locale)
        runCatching { Currency.getInstance(currencyCode) }.getOrNull()?.let { format.currency = it }
        return format.format(amount.setScale(2, RoundingMode.HALF_UP))
    }

    fun number(value: Number): String = NumberFormat.getNumberInstance(language.locale).format(value)

    /** Zahl mit höchstens [maxFractionDigits] Nachkommastellen (kaufmännisch gerundet). */
    fun number(value: Number, maxFractionDigits: Int): String {
        val format = NumberFormat.getNumberInstance(language.locale)
        format.maximumFractionDigits = maxFractionDigits
        format.roundingMode = RoundingMode.HALF_UP
        return format.format(QuantityNumbers.toBigDecimal(value))
    }

    fun date(date: LocalDate): String =
        date.format(if (profileDates) profileDateFormatter else mediumDateFormatter)

    fun time(time: LocalTime): String = time.format(timeFormatter)

    /** Datum und Uhrzeit eines Zeitpunkts in der Zeitzone des Profils (unbekannt: Europe/Berlin). */
    fun dateTime(instant: Instant): String =
        dateTime(instant.atZone(preferences.timezoneOrDefault()).toLocalDateTime())

    /**
     * Datum und Uhrzeit; ist die Zeitzone des Profils bekannt, wird in sie umgerechnet, sonst gilt
     * die Zeitzone des Werts.
     */
    fun dateTime(dateTime: ZonedDateTime): String {
        val zone = preferences.timezone
        return dateTime((if (zone != null) dateTime.withZoneSameInstant(zone) else dateTime).toLocalDateTime())
    }

    fun dateTime(dateTime: LocalDateTime): String =
        "${dateTime.toLocalDate().format(profileDateFormatter)} ${dateTime.toLocalTime().format(timeFormatter)}"

    /**
     * Eine Größe in ihrer eigenen Einheit, Zahl nach der Sprache: `33,1 lb` (de), `33.1 lb` (en).
     * Rechnet **nicht** um — dafür [displayQuantity].
     */
    fun quantity(quantity: Quantity<*>, maxFractionDigits: Int = 1): String =
        "${number(quantity.value, maxFractionDigits)} ${MeasureUnits.symbolOf(quantity.unit)}"

    /** Eine Größe, umgerechnet in die Einheit des Profils (15 kg bei imperial → `33,1 lb`). */
    fun <Q : Quantity<Q>> displayQuantity(quantity: Quantity<Q>, maxFractionDigits: Int = 1): String =
        quantity(DisplayUnits.toDisplay(quantity, preferences), maxFractionDigits)

    companion object {
        fun of(language: AccountLanguage?): LocalizedFormats = LocalizedFormats(language ?: AccountLanguage.DEFAULT)
    }
}
