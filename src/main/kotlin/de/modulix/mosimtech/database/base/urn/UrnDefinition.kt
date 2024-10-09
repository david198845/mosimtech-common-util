package de.modulix.mosimtech.database.base.urn

import java.io.Serializable

/**
 * Represents a Uniform Resource Name (URN) with a namespace, a namespace-specific string (NSS),
 * and an optional namespace identifier (NID).
 *
 * A URN (Uniform Resource Name) is an URI (Uniform Resource Identifier) that uniquely and permanently identifies a resource.
 * This interface provides the structure and essential parts of an URN.
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
interface UrnDefinition : Serializable {
    /**
     * The namespace of the URN.
     */
    val namespace: String

    /**
     * The namespace-specific string of the URN.
     */
    val nss: String

    /**
     * The optional namespace identifier of the URN.
     */
    val nid: Set<String>?

    /**
     * Converts the URN components to a string representation.
     *
     * @return A string in the format "urn:<namespace>:<nss>" if the NID is not specified;
     *         otherwise, the format will be "urn:<namespace>:<nss>:<nss>".
     */
    fun toUrnString(): String

    fun isDefault(): Boolean

}
