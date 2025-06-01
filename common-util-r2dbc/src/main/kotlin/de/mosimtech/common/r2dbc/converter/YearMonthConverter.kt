package de.mosimtech.common.r2dbc.converter

import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.core.convert.converter.Converter
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * YearMonthReadingConverter converts a string from the database to a YearMonth object.
 * This is used for R2DBC database operations where YearMonth fields need to be read from the database.
 */
@ReadingConverter
class YearMonthReadingConverter : Converter<String, YearMonth> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /**
     * Converts a string representation of a YearMonth from the database to a YearMonth object.
     *
     * @param source The string representation of the YearMonth from the database.
     * @return A YearMonth object parsed from the string.
     */
    override fun convert(source: String): YearMonth {
        return YearMonth.parse(source, formatter)
    }
}

/**
 * YearMonthWritingConverter converts a YearMonth object to its string representation
 * suitable for database storage.
 * This is used for R2DBC database operations where YearMonth fields need to be written to the database.
 */
@WritingConverter
class YearMonthWritingConverter : Converter<YearMonth, String> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM")

    /**
     * Converts a YearMonth object to its string representation suitable for database storage.
     *
     * @param source The YearMonth object to be converted.
     * @return The string representation of the YearMonth suitable for database storage.
     */
    override fun convert(source: YearMonth): String {
        return source.format(formatter)
    }
}
