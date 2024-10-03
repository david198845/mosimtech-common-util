package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.modulix.mosimtech.model.urn.Urn

/**
 * A custom deserializer for parsing `Urn` objects from JSON strings.
 *
 * This deserializer handles the conversion of a JSON string representation of a URN
 * into an instance of the `Urn` class by leveraging the `Urn.parse` method.
 */
class UrnDeserializer : StdDeserializer<Urn>(Urn::class.java) {

    /**
     * Deserializes a JSON string into an `Urn` object.
     *
     * @param p0 The `JsonParser` that reads the JSON content.
     * @param p1 The `DeserializationContext` that can be used to access contextual information.
     * @return An instance of `Urn` parsed from the input JSON string.
     */
    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): Urn {
        val input = p0!!.text
        return Urn.parse(input)!!
    }
}