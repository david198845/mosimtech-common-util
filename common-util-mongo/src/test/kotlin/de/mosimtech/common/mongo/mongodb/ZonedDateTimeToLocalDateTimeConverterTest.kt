package de.mosimtech.common.mongo.mongodb

import de.mosimtech.common.mongo.converter.mongodb.ZonedDateTimeToLocalDateTimeConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime


class ZonedDateTimeToLocalDateTimeConverterTest {

    private lateinit var zonedDateTimeToLocalDateTimeConverter: ZonedDateTimeToLocalDateTimeConverter

    @BeforeEach
    fun initialize() {
        zonedDateTimeToLocalDateTimeConverter = ZonedDateTimeToLocalDateTimeConverter()
    }

    @Test
    fun `convert function should return LocalDatetime equivalent of ZonedDateTime in UTC`() {
        val zonedDateTime = ZonedDateTime.now()
        val utcLocalDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime()

        val convertedLocalDateTime = zonedDateTimeToLocalDateTimeConverter.convert(zonedDateTime)

        assertEquals(utcLocalDateTime, convertedLocalDateTime)
    }

    @Test
    fun `convert function should return same LocalDatetime if ZonedDateTime is already in UTC`() {
        val zonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))
        val convertedLocalDateTime = zonedDateTimeToLocalDateTimeConverter.convert(zonedDateTime)

        assertEquals(zonedDateTime.toLocalDateTime(), convertedLocalDateTime)
    }
}
