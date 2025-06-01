package de.mosimtech.common.r2dbc.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class YearMonthConverterTest {

    private val yearMonthReadingConverter = YearMonthReadingConverter()
    private val yearMonthWritingConverter = YearMonthWritingConverter()

    @Test
    fun `convert String to YearMonth`() {
        // Given
        val yearMonthString = "2023-05"
        
        // When
        val yearMonth = yearMonthReadingConverter.convert(yearMonthString)
        
        // Then
        assertEquals(2023, yearMonth.year)
        assertEquals(5, yearMonth.monthValue)
    }

    @Test
    fun `convert YearMonth to String`() {
        // Given
        val yearMonth = YearMonth.of(2023, 5)
        
        // When
        val yearMonthString = yearMonthWritingConverter.convert(yearMonth)
        
        // Then
        assertEquals("2023-05", yearMonthString)
    }

    @Test
    fun `round trip conversion`() {
        // Given
        val originalYearMonth = YearMonth.of(2023, 5)
        
        // When
        val yearMonthString = yearMonthWritingConverter.convert(originalYearMonth)
        val convertedYearMonth = yearMonthReadingConverter.convert(yearMonthString)
        
        // Then
        assertEquals(originalYearMonth, convertedYearMonth)
    }
}
