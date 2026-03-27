package de.mosimtech.common.core.serializer

import de.mosimtech.common.core.urn.Urn
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.deser.std.StdDeserializer

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
    override fun deserialize(parser: JsonParser, context: DeserializationContext): Urn? {
        val rootNode: JsonNode = context.readTree(parser)

        return if (rootNode.isString) {
            Urn.parse(rootNode.stringValue() ?: "")
        } else if (rootNode.isObject && rootNode.has("namespace")) {
            val namespace = rootNode["namespace"].stringValue() ?: ""
            val nss = rootNode["nss"].stringValue() ?: ""
            val snid = extractNid(rootNode["snid"])

            if (snid.isEmpty()) {
                Urn(namespace = namespace, nss = nss)
            } else {
                Urn(namespace = namespace, nss = nss, snid = snid)
            }
        } else {
            null
        }
    }

    protected fun extractNid(node: JsonNode?): Set<String> {
        val valuesSet = mutableSetOf<String>()
        node?.takeIf { it.isArray }?.let {
            for (arrayNode in it) {
                valuesSet.add(arrayNode.stringValue() ?: "")
            }
        }
        return valuesSet
    }
}
