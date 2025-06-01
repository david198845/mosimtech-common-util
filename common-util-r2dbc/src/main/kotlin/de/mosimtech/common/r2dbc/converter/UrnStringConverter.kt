package de.mosimtech.common.r2dbc.converter

import de.mosimtech.common.core.urn.Urn
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.core.convert.converter.Converter

/**
 * UrnReadingConverter converts a string from the database to a Urn object.
 * This is used for R2DBC database operations where Urn fields need to be read from the database.
 */
@ReadingConverter
class UrnReadingConverter : Converter<String, Urn> {
    /**
     * Converts a string representation of a URN from the database to a Urn object.
     *
     * @param source The string representation of the URN from the database. Can be null.
     * @return An instance of the Urn class if the string can be successfully parsed, or null if the input is null or invalid.
     */
    override fun convert(source: String): Urn {
        return Urn.parse(source)!!
    }
}

/**
 * UrnWritingConverter converts a Urn object to its string representation suitable for database storage.
 * This is used for R2DBC database operations where Urn fields need to be written to the database.
 */
@WritingConverter
class UrnWritingConverter : Converter<Urn, String> {
    /**
     * Converts a URN object to its string representation suitable for database storage.
     *
     * @param source The URN object to be converted.
     * @return The string representation of the URN suitable for database storage.
     */
    override fun convert(source: Urn): String {
        return source.toUrnString()
    }
}
