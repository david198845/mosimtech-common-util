package de.modulix.mosimtech.model.urn

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.model.namespace.Namespace
import de.modulix.mosimtech.serializer.UrnDeserializer
import de.modulix.mosimtech.serializer.UrnSerializer

/**
 * Represents a Uniform Resource Name (URN) with a namespace, a namespace-specific string (NSS),
 * and an optional namespace identifier (NID).
 *
 * A URN (Uniform Resource Name) is an URI (Uniform Resource Identifier) that uniquely and permanently identifies a resource.
 * This class provides the structure and essential parts of an URN.
 *
 * @property namespace The namespace of the URN. This is a higher-level category or domain that helps
 *                     uniquely identify the resource. Example: "urn:isbn".
 * @property nss The namespace-specific string of the URN. This is the specific part of the URN that identifies
 *               a particular resource within the given namespace. Example: "978-3-16-148410-0" for a book in the ISBN namespace.
 * @property nid The optional namespace identifier of the URN. This could be used for additional identification purposes or specific
 *               implementation details. Example: An identifier for versions or special distinctions.
 *
 * Example of a complete URN without NID:
 * - Complete URN: "urn:isbn:978-3-16-148410-0"
 * - Namespace: "isbn"
 * - Namespace-specific string (NSS): "978-3-16-148410-0"
 * - Optional namespace identifier (NID): not present in this example
 *
 * Example of a complete URN with all components:
 * - Complete URN: "urn:user:keycloak:978-3-16-148410-0"
 * - Namespace: "user"
 * - Namespace-specific string (NSS): "978-3-16-148410-0"
 * - Optional namespace identifier (NID): keycloak
 *
 */
@JsonSerialize(using = UrnSerializer::class)
@JsonDeserialize(using = UrnDeserializer::class)
data class Urn(
    @JsonProperty("namespace") override val namespace: Namespace,
    @JsonProperty("nss") override val nss: String,
    @JsonProperty("nid") override val nid: Set<String>? = null
) : UrnDefinition {


    init {
        require(namespace in knownNamespaces) { "Unknown namespace: $namespace" }
    }


    /**
     * Constructs an instance of the Urn class from the given URN string.
     *
     * This constructor uses the `parse` function to parse the provided `urnString` and
     * initialize an Urn object. If the `urnString` cannot be parsed, the initialization
     * may result in a null object, depending on the implementation of the `parse` function.
     *
     * @param urnString The string representation of a URN to be parsed and used to initialize the Urn object.
     */
    constructor(urnString: String) : this(parse(urnString))

    /**
     * Private constructor for the Urn class that initializes the instance with
     * the namespace, namespace-specific string (NSS), and optional namespace identifier (NID)
     * from another Urn instance.
     *
     * @param data The Urn instance from which to copy the namespace, NSS, and NID values.
     */
    private constructor(data: Urn?) : this(data!!.namespace, data.nss, data.nid)


    /**
     * Converts the URN components to a string representation.
     *
     * @return A string in the format "urn:<namespace>:<nss>" if the NID is not specified;
     *         otherwise, the format will be "urn:<namespace>:<nss>:<nss>".
     */
    override fun toUrnString(): String {
        return if (nid == null) "urn:${namespace.identifier}:$nss"
        else {
            "urn:${namespace.identifier}:${nid.joinToString(":") { it }}:$nss"
        }
    }

    override fun toString(): String {
        return toUrnString()
    }

    companion object {
        private const val URN_PREFIX = "urn"
        private const val NID_URN = 4
        private const val MIN_URN_PARTS = 3
        private const val NID_START_INDEX = 2
        private const val NID_END_INDEX_OFFSET = 1

        val knownNamespaces: MutableSet<Namespace> = mutableSetOf(DefaultNamespace.Undefined)

        /**
         * Registers one or more namespaces to be recognized by the URN system.
         *
         * @param namespace Vararg parameter for one or more namespaces to be registered.
         */
        fun registerNamespace(vararg namespace: Namespace) {
            namespace.forEach { knownNamespaces.add(it) }
        }

        /**
         * Parses a string representation of a URN (Uniform Resource Name) and returns an instance of the Urn class.
         *
         * @param urnString The string representation of the URN to be parsed.
         * @return An instance of the Urn class if the string can be successfully parsed, or null if the format is invalid.
         */


        fun parse(urnString: String): Urn? {
            val parts = urnString.split(":")
            if (parts.size < MIN_URN_PARTS || parts[0] != URN_PREFIX) {
                return null
            }
            val namespace = extractNamespace(parts[1])

            return if (parts.size >= NID_URN) {
                Urn(
                    namespace = namespace,
                    nid = parts.subList(NID_START_INDEX, parts.size - NID_END_INDEX_OFFSET).toSet(),
                    nss = parts.last()
                )
            } else {
                Urn(namespace = namespace, nss = parts[NID_START_INDEX])
            }
        }

        private fun extractNamespace(part: String): Namespace {
            return knownNamespaces.firstOrNull { it.identifier == part } ?: DefaultNamespace.Undefined
        }
    }
}


/**
 * Converts the current string to a URN (Uniform Resource Name) instance.
 *
 * @return An instance of the Urn class parsed from the current string.
 */
fun String.toUrn(): Urn? = Urn.parse(this)