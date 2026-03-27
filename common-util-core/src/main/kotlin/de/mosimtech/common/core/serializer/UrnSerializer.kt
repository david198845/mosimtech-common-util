package de.mosimtech.common.core.serializer

import de.mosimtech.common.core.urn.Urn
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer


/**
 * Custom serializer for the `Urn` class that extends Jackson's `StdSerializer`.
 *
 * This serializer converts `Urn` instances into their string representation using the `toUrnString` method.
 *
 * It throws an exception if the `Urn` instance provided to it is null.
 */
open class UrnSerializer : StdSerializer<Urn>(Urn::class.java) {

    /**
     * Serializes an `Urn` object into its string representation using a `JsonGenerator`.
     *
     * @param p0 The `Urn` object to be serialized. If null, an exception is thrown.
     * @param p1 The `JsonGenerator` used for writing the serialized string.
     * @param p2 The `SerializerProvider` that can be used to get serializers for serializing
     *           the object's properties. This parameter is currently not used in this method.
     * @throws Exception If the given `Urn` object is null.
     */
    override fun serialize(p0: Urn?, p1: JsonGenerator, p2: SerializationContext) {
        if (p0 == null) {
            throw Exception("given urn is null")
        }
        p1.writeString(p0.toUrnString())
    }
}
