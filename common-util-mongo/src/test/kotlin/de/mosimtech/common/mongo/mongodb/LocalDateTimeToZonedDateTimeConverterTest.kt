package de.mosimtech.common.mongo.mongodb

import de.mosimtech.common.mongo.converter.mongodb.LocalDateTimeToZonedDateTimeConverter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class LocalDateTimeToZonedDateTimeConverterTest {

    private val converter = LocalDateTimeToZonedDateTimeConverter()

    @Test
    fun testConvertShouldReturnZonedDateTimeInUTC() {
        val localDateTime = LocalDateTime.now()
        val zonedDateTimeConverted = converter.convert(localDateTime)

        val zonedDateTimeExpected = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))

        Assertions.assertEquals(zonedDateTimeExpected, zonedDateTimeConverted)
    }

    @Test
    fun testConvertWithFixedDateShouldReturnExpectedZonedDateTimeInUTC() {
        val localDateTime = LocalDateTime.of(2025, 12, 31, 23, 59)
        val zonedDateTimeConverted = converter.convert(localDateTime)

        val zonedDateTimeExpected = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))

        Assertions.assertEquals(zonedDateTimeExpected, zonedDateTimeConverted)
    }

    @Test
    fun testConvertShouldHandleLeapYearsCorrectly() {
        val leapYearDateTime = LocalDateTime.of(2024, 2, 29, 14, 0)
        val zonedDateTimeConverted = converter.convert(leapYearDateTime)

        val zonedDateTimeExpected = ZonedDateTime.of(leapYearDateTime, ZoneId.of("UTC"))

        Assertions.assertEquals(zonedDateTimeExpected, zonedDateTimeConverted)
    }
}
