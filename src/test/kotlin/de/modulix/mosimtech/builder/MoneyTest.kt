package de.modulix.mosimtech.builder

import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import javax.money.Monetary
import javax.money.RoundingQueryBuilder
import javax.money.convert.MonetaryConversions


class MoneyTest {
    @Test
    fun `test successful update of income`() {

        // Erstellen von zwei Geldbeträgen in unterschiedlichen Währungen
        val usdAmount = Money.of(50, "USD")
        val eurAmount = Money.of(40, "EUR")
        val resultAmount = eurAmount.addCurrencyIndependent(usdAmount)
        val rateProvider = MonetaryConversions.getExchangeRateProvider("ECB", "IMF")
        val eurToChfRate = rateProvider.getExchangeRate("USD", "EUR")
        println("eurToChfRate: $eurToChfRate")
        println("resultAmount: $resultAmount")
    }


    @Test
    fun `test Get Nth Last Business Day`() {
        val date = getNthLastBusinessDay(2024, Month.OCTOBER, 3)
        println(date)
    }
}

fun getNthLastBusinessDay(year: Int, month: Month, n: Int): LocalDate {
    val yearMonth = YearMonth.of(year, month)
    var count = 0
    var currentDate = LocalDate.of(yearMonth.year, yearMonth.month, yearMonth.lengthOfMonth())

    while (count < n) {
        if (currentDate.dayOfWeek != DayOfWeek.SATURDAY && currentDate.dayOfWeek != DayOfWeek.SUNDAY) {
            count++
        }
        if (count < n) {
            currentDate = currentDate.minusDays(1)
        }
    }
    return currentDate
}


fun Money.addCurrencyIndependent(other: Money, roundingScale: Int = 3): Money {
    val rounding = Monetary.getRounding(
        RoundingQueryBuilder.of()
            .setScale(roundingScale)
            .set(RoundingMode.HALF_UP)
            .build()
    )
    return if (this.currency != other.currency) {
        this.add(other.with(MonetaryConversions.getConversion(this.currency)).with(rounding))
    } else {
        this.add(other)
    }
}