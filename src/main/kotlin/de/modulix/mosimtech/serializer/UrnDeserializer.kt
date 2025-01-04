package de.modulix.mosimtech.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import de.modulix.mosimtech.database.urn.Urn

/**
 * A custom deserializer for parsing `Urn` objects from JSON strings.
 *
 * This deserializer handles the conversion of a JSON string representation of a URN
 * into an instance of the `Urn` class by leveraging the `Urn.parse` method.
 */
open class UrnDeserializer : StdDeserializer<Urn>(Urn::class.java) {

    /**
     * Deserializes the JSON content into an `Urn` object.
     *
     * @param parser the `JsonParser` used to parse the JSON content.
     * @param context the `DeserializationContext` that can be used to access additional deserialization context.
     * @return an `Urn` object parsed from the JSON content, or null if the content is invalid or cannot be parsed.
     */
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): Urn? {
        val rootNode: JsonNode = parser?.codec?.readTree(parser) ?: return null

        if (rootNode.isTextual) {
            return Urn.parse(rootNode.asText())
        } else if (rootNode.isObject && rootNode.has("namespace")) {
            val namespace = rootNode["namespace"].asText()
            val nss = rootNode["nss"].asText()
            val snid = extractNid(rootNode["snid"])

            return if (snid.isEmpty()) {
                Urn(namespace = namespace, nss = nss)
            } else {
                Urn(namespace = namespace, nss = nss, snid = snid)
            }
        } else {
            return null
        }
    }

    protected fun extractNid(node: JsonNode?): Set<String> {
        val valuesSet = mutableSetOf<String>()
        node?.takeIf { it.isArray }?.let {
            for (arrayNode in it) {
                valuesSet.add(arrayNode.asText())
            }
        }
        return valuesSet
    }
}