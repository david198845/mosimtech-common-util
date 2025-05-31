package de.mosimtech.common.mongo.converter.mongodb

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.WritingConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * A converter class that transforms a `ZonedDateTime` object to a `LocalDateTime` object.
 * The conversion will adjust the `ZonedDateTime` to UTC before extracting the `LocalDateTime`.
 *
 * Implements the `Converter` interface provided by the Spring Framework for notificationType conversion.
 */
@WritingConverter
open class ZonedDateTimeToLocalDateTimeConverter : Converter<ZonedDateTime, LocalDateTime> {
    /**
     * Converts a `ZonedDateTime` object to a `LocalDateTime` object in UTC.
     *
     * @param source the `ZonedDateTime` object to be converted.
     * @return the `LocalDateTime` object in UTC timezone.
     */
    override fun convert(source: ZonedDateTime): LocalDateTime {
        return source.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()
    }
}

