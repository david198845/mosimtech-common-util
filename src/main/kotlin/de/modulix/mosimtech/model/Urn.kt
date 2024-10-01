package de.modulix.mosimtech.model

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
 * Example of a complete URN with all components:
 * - Complete URN: "urn:isbn:978-3-16-148410-0"
 * - Namespace: "isbn"
 * - Namespace-specific string (NSS): "978-3-16-148410-0"
 * - Optional namespace identifier (NID): not present in this example
 */
data class Urn(
    val namespace: String,
    val nss: String,
    val nid: String? = null
) {
    /**
     * Converts the URN components to a string representation.
     *
     * @return A string in the format "urn:<namespace>:<nss>" if the NID is not specified;
     *         otherwise, the format will be "urn:<namespace>:<nss>:<nss>".
     */
    fun toUrnString(): String {
        return if (nid != null) "urn:$namespace:$nss"
        else "urn:$namespace:$nss:$nss"
    }

    companion object {
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

            // Handle cases urn:keycloak:user:123-123-123 and urn:user:132-123-465
            return if (parts.size == 4) {
                Urn(
                    namespace = parts[1],
                    nid = parts[2],
                    nss = parts[3]
                )
            } else {
                Urn(namespace = parts[1], nss = parts[2])
            }
        }
    }
}