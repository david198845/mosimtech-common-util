package de.modulix.mosimtech.converter.jpa

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * A `YearMonthConverter` converts `YearMonth` objects to their `String` representation
 * in the format `uuuu-MM` for database storage and parses the stored `String` back to `YearMonth`.
 *
 * This class implements the `AttributeConverter` interface to support the conversion between
 * `YearMonth` and `String` for entity attribute handling in JPA.
 *
 * The conversion process:
 * - Converts `YearMonth` to a `String` using the `DateTimeFormatter` with the pattern `uuuu-MM`.
 * - Parses a `String` back into a `YearMonth` while supporting both one-digit and two-digit month formats
 *   such as `2025-1` and `2025-01`.
 *
 * Throws an `IllegalArgumentException` if the string format is invalid during parsing.
 *
 * This conversion is applied automatically in JPA through the `@Converter(autoApply = true)` annotation.
 */
@Converter(autoApply = true)
class YearMonthConverter: AttributeConverter<YearMonth, String> {
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM")

    /**
     * Converts a `YearMonth` object to its string representation in the format `uuuu-MM`.
     *
     * @param attribute The `YearMonth` object to be converted. Can be null.
     * @return The string representation of the `YearMonth` in the format `uuuu-MM`, or null if the input is null.
     */
    override fun convertToDatabaseColumn(attribute: YearMonth?): String? {
        // Konvertiert YearMonth in String mit Format "uuuu-MM"
        return attribute?.format(formatter)
    }

    /**
     * Converts the given string representation of a year and month to a `YearMonth` object.
     *
     * @param dbData The string representation of a year and month in the format `uuuu-M` or `uuuu-MM`. Can be null.
     * @return A `YearMonth` object parsed from the provided string, or null if the input is null.
     * @throws IllegalArgumentException if the string cannot be parsed into a `YearMonth` using the format `uuuu-M` or `uuuu-MM`.
     */
    override fun convertToEntityAttribute(dbData: String?): YearMonth? {
            return dbData?.let {
                try {
                    // Versuch: Parst zweistellige und einstellige Monate (z.B. 2025-1 oder 2025-01)
                    YearMonth.parse(it, DateTimeFormatter.ofPattern("uuuu-M"))
                } catch (e: DateTimeParseException) {
                    throw IllegalArgumentException("Ungültiges Datumsformat: $it. Erlaubt sind uuuu-M oder uuuu-MM.", e)
                }
            }
    }
}