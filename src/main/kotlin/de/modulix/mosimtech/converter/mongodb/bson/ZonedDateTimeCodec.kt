package de.modulix.mosimtech.converter.mongodb.bson

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * A codec for encoding and decoding `ZonedDateTime` instances to and from BSON.
 *
 * This codec uses the `ISO_ZONED_DATE_TIME` formatter for converting
 * `ZonedDateTime` objects to their string representation and vice versa.
 */
open class ZonedDateTimeCodec : Codec<ZonedDateTime> {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    /**
     * Encodes a `ZonedDateTime` object to a BSON string using the provided `BsonWriter`.
     * The date-time is formatted according to `ISO_ZONED_DATE_TIME` formatter before
     * being written to the BSON output.
     *
     * @param writer the `BsonWriter` to write the string representation of the `ZonedDateTime` to. Can be null.
     * @param value the `ZonedDateTime` object to encode. Can be null.
     * @param encoderContext the encoder context to use. Can be null.
     */
    override fun encode(writer: BsonWriter?, value: ZonedDateTime?, encoderContext: EncoderContext?) {
        writer?.writeString(value?.format(formatter))
    }

    /**
     * Decodes a BSON string into a `ZonedDateTime` instance using the provided `BsonReader`.
     * The string is expected to be in the `ISO_ZONED_DATE_TIME` format.
     *
     * @param reader the `BsonReader` from which the string representation of the `ZonedDateTime` is read. Can be null.
     * @param decoderContext the decoder context to use. Can be null.
     * @return the decoded `ZonedDateTime` instance.
     */
    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): ZonedDateTime {
        val dateString = reader?.readString() ?: throw IllegalArgumentException("reader returned null")
        return ZonedDateTime.parse(dateString, formatter)
    }

    /**
     * Returns the `Class` object associated with the `ZonedDateTime` type that this codec encodes and decodes.
     *
     * @return the `Class` instance representing the `ZonedDateTime` type
     */
    override fun getEncoderClass(): Class<ZonedDateTime> {
        return ZonedDateTime::class.java
    }
}