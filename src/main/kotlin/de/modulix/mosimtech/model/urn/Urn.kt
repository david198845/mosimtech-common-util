package de.modulix.mosimtech.model.urn

import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.model.namespace.Namespace

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
 *  * Example of a complete URN with all components:
 *  * - Complete URN: "urn:user:keycloak:978-3-16-148410-0"
 *  * - Namespace: "user"
 *  * - Namespace-specific string (NSS): "978-3-16-148410-0"
 *  * - Optional namespace identifier (NID): keycloak
 *
 */
data class Urn(
    override val namespace: Namespace,
    override val nss: String,
    override val nid: String? = null
) : UrnDefinition {
    /**
     * Converts the URN components to a string representation.
     *
     * @return A string in the format "urn:<namespace>:<nss>" if the NID is not specified;
     *         otherwise, the format will be "urn:<namespace>:<nss>:<nss>".
     */
    override fun toUrnString(): String {
        return if (nid == null) "urn:${namespace.identifier}:$nss"
        else "urn:${namespace.identifier}:$nid:$nss"
    }

    companion object {

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
            val parts = urnString.split(":", limit = 4)
            if (parts.size < 3 || parts[0] != "urn") {
                return null
            }
            val namespace = knownNamespaces.firstOrNull { it.identifier == parts[1] } ?: DefaultNamespace.Undefined
            // Handle cases urn:keycloak:user:123-123-123 and urn:user:132-123-465
            return if (parts.size == 4) {
                Urn(
                    namespace = namespace,
                    nid = parts[2],
                    nss = parts[3]
                )
            } else {
                Urn(namespace = namespace, nss = parts[2])
            }
        }
    }
}