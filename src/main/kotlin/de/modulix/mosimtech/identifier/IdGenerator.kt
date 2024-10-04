// Datei: IdGenerator.kt
package de.modulix.mosimtech.identifier

import de.modulix.mosimtech.model.namespace.DefaultNamespace
import de.modulix.mosimtech.model.namespace.Namespace
import de.modulix.mosimtech.model.urn.Urn
import java.util.*

/**
 * An interface for generating Uniform Resource Names (URNs).
 * It provides methods to create URNs with specified namespaces and optional namespace identifiers.
 */
interface IdGenerator {

    /**
     * Generates a URN (Uniform Resource Name) string using the specified namespace and optional namespace identifiers.
     *
     * @param namespace The namespace to be used for the URN. If not specified, defaults to `DefaultNamespace.Undefined`.
     * @param nid Vararg parameter representing optional namespace identifiers.
     * @return A string representing the constructed URN.
     */
    fun generateUrnString(namespace: Namespace = DefaultNamespace.Undefined, vararg nid: String?): String {
        return generateUrn(namespace, *nid).toUrnString()
    }

    /**
     * Generates a URN (Uniform Resource Name) using the specified namespace and optional namespace identifiers.
     *
     * @param namespace The namespace to be used for the URN. Defaults to `DefaultNamespace.Undefined` if not specified.
     * @param nid Vararg parameter representing optional namespace identifiers.
     * @return An instance of the `Urn` class representing the constructed URN.
     */
    fun generateUrn(namespace: Namespace = DefaultNamespace.Undefined, vararg nid: String?): Urn {
        val nidSet = nid.filterNotNull().toSet().takeIf { it.isNotEmpty() }
        return Urn(namespace = namespace, nss = UUID.randomUUID().toString(), nid = nidSet)
    }
}

