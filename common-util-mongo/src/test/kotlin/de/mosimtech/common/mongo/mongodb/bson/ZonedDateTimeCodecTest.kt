package de.mosimtech.common.mongo.mongodb.bson

import de.mosimtech.common.mongo.converter.mongodb.bson.ZonedDateTimeCodec
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * This class tests the functionality of the ZonedDateTimeCodec class,
 * specifically focusing on the encode method
 */
class ZonedDateTimeCodecTest {

    /**
     * The ZonedDateTimeCodec's encode function is tested here.
     * Given a ZonedDateTime object, the encode method should write it to BSON
     * using the BSON writer. The output string should be formatted
     * according to ISO_ZONED_DATE_TIME format.
     */
    @Test
    fun testEncode() {
        val zonedDateTime = ZonedDateTime.now()
        val bsonWriter = Mockito.mock(BsonWriter::class.java)
        val encoderContext = Mockito.mock(EncoderContext::class.java)

        val codec = ZonedDateTimeCodec()
        codec.encode(bsonWriter, zonedDateTime, encoderContext)

        Mockito.verify(bsonWriter).writeString(
            zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        )
    }


    /**
     * The ZonedDateTimeCodec's decode function is tested here.
     * Given a BSON reader that contains a string representing
     * a ZonedDateTime, the decode method should read it and return
     * the corresponding ZonedDateTime.
     */
    @Test
    fun testDecode() {
        val zonedDateTime = ZonedDateTime.now()
        val bsonReader = Mockito.mock(BsonReader::class.java)
        val decoderContext = Mockito.mock(DecoderContext::class.java)

        Mockito.`when`(bsonReader.readString()).thenReturn(
            zonedDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)
        )

        val codec = ZonedDateTimeCodec()
        val result = codec.decode(bsonReader, decoderContext)

        assertEquals(zonedDateTime.withNano(0), result.withNano(0))
    }

}
