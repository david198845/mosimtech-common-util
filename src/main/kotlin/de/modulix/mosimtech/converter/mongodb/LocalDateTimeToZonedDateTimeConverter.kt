package de.modulix.mosimtech.converter.mongodb

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import java.time.LocalDateTime
import java.time.ZonedDateTime

/**
 * Converter class that transforms a LocalDateTime instance into a ZonedDateTime instance utilizing the UTC time zone.
 *
 * Notes:
 * - It implements the `Converter` interface for conversion logic.
 * - The designated time zone for conversion is always UTC.
 */
@ReadingConverter
open class LocalDateTimeToZonedDateTimeConverter : Converter<LocalDateTime, ZonedDateTime> {
    /**
     * Converts a given LocalDateTime instance to a ZonedDateTime instance in the UTC time zone.
     *
     * @param source the LocalDateTime instance to be converted
     * @return the converted ZonedDateTime instance in the UTC time zone
     */
    override fun convert(source: LocalDateTime): ZonedDateTime {
        return source.atZone(java.time.ZoneId.of("UTC"))
    }
}