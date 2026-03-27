package de.mosimtech.common.core.urn

import com.fasterxml.jackson.annotation.JsonProperty
import de.mosimtech.common.core.namespace.DefaultNamespace
import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.serializer.UrnDeserializer
import de.mosimtech.common.core.serializer.UrnSerializer
import tools.jackson.databind.annotation.JsonDeserialize
import tools.jackson.databind.annotation.JsonSerialize
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serial
import java.io.Serializable

/**
 * Represents a Uniform Resource Name (URN) with a namespace, a namespace-specific string (NSS),
 * and an optional namespace identifier (SNID).
 *
 * A URN (Uniform Resource Name) is an URI (Uniform Resource Identifier) that uniquely and permanently identifies a resource.
 * This class provides the structure and essential parts of an URN.
 *
 * @property namespace The namespace or namespace identifier of the URN. This is a higher-level category or domain that helps
 *                     uniquely identify the resource. Example: "urn:isbn".
 * @property nameSpecificString The namespace-specific string of the URN. This is the specific part of the URN that identifies
 *               a particular resource within the given namespace. Example: "978-3-16-148410-0" for a book in the ISBN namespace.
 * @property subNamespaceIdentifier The optional sub namespace identifier of the URN. This could be used for additional identification purposes or specific
 *               implementation details. Example: An identifier for versions or special distinctions.
 *
 * Example of a complete URN without snid:
 * - Complete URN: "urn:isbn:978-3-16-148410-0"
 * - Namespace: "isbn"
 * - Namespace-specific string (NSS): "978-3-16-148410-0"
 * - Optional namespace identifier (snid): not present in this example
 *
 * Example of a complete URN with all components:
 * - Complete URN: "urn:user:keycloak:978-3-16-148410-0"
 * - Namespace: "user"
 * - Namespace-specific string (NSS): "978-3-16-148410-0"
 * - Optional sub namespace identifier (SNID): keycloak
 *
 */
@JsonSerialize(using = UrnSerializer::class)
@JsonDeserialize(using = UrnDeserializer::class)
open class Urn : UrnDefinition, Serializable {

    @JsonProperty("namespace")
    override lateinit var namespace: String

    @JsonProperty("snid")
    override var subNamespaceIdentifier: Set<String> = emptySet()

    @JsonProperty("nss")
    override lateinit var nameSpecificString: String

    /**
     * Primary constructor for the `Urn` class.
     *
     * Initializes the URN components with default values:
     * - `namespace` is set to the identifier of the `DefaultNamespace.Undefined`,
     * - `nss` is set to "undefined",
     * - `snid` is set to an empty set.
     */
    constructor() {
        this.namespace = DefaultNamespace.Undefined.identifier
        this.nameSpecificString = NSS_DEFAULT_VALUE
        this.subNamespaceIdentifier = emptySet()
    }

    /**
     * Constructs an instance of the Urn class with a specified namespace, NSS, and an optional snid.
     *
     * @param namespace The namespace component of the URN.
     * @param nss The Namespace Specific String (NSS) component of the URN.
     * @param snid An optional set of SNIDs (Sub Namespace Identifiers) associated with the URN. Defaults to null if not provided.
     */
    constructor(namespace: String, nss: String, snid: Set<String> = emptySet()) : this() {
        this.namespace = namespace
        this.nameSpecificString = nss
        this.subNamespaceIdentifier = snid
    }

    /**
     * Constructs an instance of the `Urn` class by assigning the provided `namespace`, `nss`,
     * and optionally `snid` values.
     *
     * @param namespace The `Namespace` object representing the namespace identifier.
     * @param nss The namespace-specific string (NSS) part of the URN.
     * @param snid A set of namespace identifiers (SNID) which are optional.
     */
    constructor(namespace: Namespace, nss: String, snid: Set<String> = emptySet()) : this(
        namespace = namespace.identifier,
        nss = nss,
        snid = snid
    )


    /**
     * Constructs an instance of the Urn class from the given URN string.
     *
     * This constructor uses the `parse` function to parse the provided `urnString` and
     * initialize an Urn object. If the `urnString` cannot be parsed, the initialization
     * may result in a null object, depending on the implementation of the `parse` function.
     *
     * @param urnString The string representation of a URN to be parsed and used to initialize the Urn object.
     */
    constructor(urnString: String) : this(parse(urnString)!!)

    /**
     * Private constructor for the Urn class that initializes the instance with
     * the namespace, namespace-specific string (NSS), and optional namespace identifier (snid)
     * from another Urn instance.
     *
     * @param data The Urn instance from which to copy the namespace, NSS, and snid values.
     */
    constructor(data: Urn) : this(data.namespace, data.nameSpecificString, data.subNamespaceIdentifier)


    /**
     * Converts the URN components to a string representation.
     *
     * @return A string in the format "urn:<namespace>:<nss>" if the snid is not specified;
     *         otherwise, the format will be "urn:<namespace>:<nss>:<nss>".
     */
    override fun toUrnString(): String {
        return if (subNamespaceIdentifier.isEmpty()) "urn:${namespace}:$nameSpecificString"
        else {
            "urn:${namespace}:${
                subNamespaceIdentifier.takeIf { it.isNotEmpty() }.orEmpty().joinToString(":") { it }
            }:$nameSpecificString"
        }
    }

    /**
     * Creates a string representation of the URN.
     *
     * @return A string representing the URN in the format defined by `toUrnString`.
     */
    override fun toString(): String {
        return toUrnString()
    }

    /**
     * Compares this `Urn` instance with another `Urn` instance for equality.
     *
     * The equality is determined by comparing the string representation of this URN,
     * generated by the `toUrnString` method, with the provided `Urn` instance's string representation.
     *
     * @param other The `Urn` instance to compare with this instance.
     * @return `true` if the provided `Urn` instance's string representation is equal to this URN's string representation,
     *         `false` otherwise.
     */
    fun equals(other: Urn): Boolean {
        return this.toUrnString() == other.toUrnString()
    }

    /**
     * Checks if this `Urn` instance is equal to a given string representation of a URN.
     *
     * The equality is determined by comparing the string representation of this URN,
     * generated by the `toUrnString` method, with the provided string.
     *
     * @param other The string representation of a URN to compare with this instance.
     * @return `true` if the provided string is equal to the string representation of this URN, `false` otherwise.
     */
    fun equals(other: String): Boolean {
        return this.toUrnString() == other
    }

    /**
     * Checks if this `Urn` instance is equal to another object.
     *
     * The equality of two `Urn` instances is determined by comparing their `namespace`, `nss`,
     * and `snid` properties.
     *
     * @param other The object to compare with this `Urn` instance.
     * @return `true` if the provided object is equal to this `Urn` instance, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Urn) return false

        if (namespace != other.namespace) return false
        if (nameSpecificString != other.nameSpecificString) return false
        if (subNamespaceIdentifier != other.subNamespaceIdentifier) return false

        return true
    }

    /**
     * Generates the hash code for the current `Urn` instance.
     * The hash code is calculated based on the `namespace`, `nss`, and `snid` properties
     * ensuring a consistent and unique value representative of the URN's state.
     *
     * @return The generated hash code as an integer.
     */
    override fun hashCode(): Int {
        var result = namespace.hashCode()
        result = 31 * result + nameSpecificString.hashCode()
        result = 31 * result + subNamespaceIdentifier.hashCode()
        return result
    }

    /**
     * Creates a copy of the current `Urn` instance.
     *
     * @return A new `Urn` instance with the same namespace, NSS, and snid values as the original.
     */
    fun copy(): Urn {
        return Urn(this)
    }

    @Serial
    private fun writeObject(out: ObjectOutputStream) {
        out.writeUTF(this.toUrnString())
    }

    @Serial
    private fun readObject(input: ObjectInputStream) {
        val urnString = input.readUTF()
        val parsed = parse(urnString)
        parsed?.let {
            namespace = it.namespace
            nameSpecificString = it.nameSpecificString
            subNamespaceIdentifier = it.subNamespaceIdentifier
        }
        // Setzen Sie hier die internen Felder der Urn-Klasse
    }

    override fun isDefault() =
        namespace == DefaultNamespace.Undefined.identifier && nameSpecificString == NSS_DEFAULT_VALUE

    companion object {
        const val NSS_DEFAULT_VALUE = "undefined"
        private const val URN_PREFIX = "urn"
        private const val SNID_URN = 4
        private const val MIN_URN_PARTS = 3
        private const val NID_START_INDEX = 2
        private const val NID_END_INDEX_OFFSET = 1

        /**
         * Checks if the given URN is considered the default value.
         *
         * @param urn The `Urn` instance to be checked.
         * @return `true` if the provided URN matches the default conditions, `false` otherwise.
         */
        fun isDefault(urn: Urn) = urn.isDefault()

        /**
         * Creates an instance of `Urn` using the specified namespace and namespace-specific string (NSS).
         *
         * @param namespace The namespace component of the URN.
         * @param nss The Namespace Specific String (NSS) component of the URN.
         * @return A new `Urn` instance constructed using the given namespace and NSS.
         */
        fun of(namespace: String, nss: String): Urn = Urn(namespace, nss)

        /**
         * Creates a new instance of the `Urn` class using the specified namespace, namespace-specific string (NSS),
         * and a set of sub-namespace identifiers (SNID).
         *
         * @param namespace The namespace component of the URN. This is a higher-level category or domain
         *                  used to uniquely identify the resource.
         * @param nss The namespace-specific string component of the URN. This is a specific string
         *            used to identify a resource within the given namespace.
         * @param snid The set of sub-namespace identifiers associated with the URN. This is used for
         *             additional identification purposes or specific implementation distinctions.
         * @return A new `Urn` instance constructed with the given parameters.
         */
        fun of(namespace: String, nss: String, snid: Set<String>): Urn = Urn(namespace, nss, snid)

        /**
         * Creates a new instance of the `Urn` class with the specified namespace, NSS, and SNIDs.
         *
         * @param namespace The namespace component of the URN.
         * @param nss The Namespace Specific String (NSS) component of the URN.
         * @param snid Optional sub namespace identifiers for additional identification or categorization.
         * @return A new `Urn` instance representing the specified components.
         */
        fun of(namespace: String, nss: String, vararg snid: String): Urn = Urn(namespace, nss, snid.toSet())

        /**
         * Creates an `Urn` instance from the provided string representation.
         *
         * Parses the given `urnString` and attempts to convert it into an instance of the `Urn` class.
         * If the string is invalid or cannot be parsed, the function returns null.
         *
         * @param urnString The string representation of the URN to be parsed.
         * @return An `Urn` instance if the string can be successfully parsed, or null if the format is invalid.
         */
        fun of(urnString: String): Urn? = parse(urnString)

        /**
         * Parses a string representation of a URN (Uniform Resource Name) and returns an instance of the Urn class.
         *
         * @param urnString The string representation of the URN to be parsed.
         * @return An instance of the Urn class if the string can be successfully parsed, or null if the format is invalid.
         */
        fun parse(urnString: String): Urn? {
            val parts = urnString.split(":").filter { it.isNotBlank() }
            if (parts.size < MIN_URN_PARTS || parts[0] != URN_PREFIX) {
                return null
            }
            return if (parts.size >= SNID_URN) {
                Urn(
                    namespace = parts[1],
                    snid = parts.subList(NID_START_INDEX, parts.size - NID_END_INDEX_OFFSET).toSet(),
                    nss = parts.last()
                )
            } else {
                Urn(namespace = parts[1], nss = parts[NID_START_INDEX])
            }
        }

    }
}


