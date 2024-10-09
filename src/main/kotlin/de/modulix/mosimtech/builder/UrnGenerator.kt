package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.base.namespace.Namespace
import de.modulix.mosimtech.database.base.urn.Urn
import java.util.*

/**
 * An interface for generating Uniform Resource Names (URNs).
 * It provides methods to create URNs with specified namespaces and optional namespace identifiers.
 */
abstract class UrnGenerator {

    /**
     * Generates a URN (Uniform Resource Name) string using the specified namespace and optional namespace identifiers.
     *
     * @param namespace The namespace to be used for the URN. If not specified, defaults to `DefaultNamespace.Undefined`.
     * @param nid Vararg parameter representing optional namespace identifiers.
     * @return A string representing the constructed URN.
     */
    fun generateUrnString(namespace: Namespace, vararg nid: String = emptyArray()): String {
        return generateUrn(namespace, *nid).toUrnString()
    }

    /**
     * Generates a URN (Uniform Resource Name) using the specified namespace and optional namespace identifiers.
     *
     * @param namespace The namespace to be used for the URN. Defaults to `DefaultNamespace.Undefined` if not specified.
     * @param nid Vararg parameter representing optional namespace identifiers.
     * @return An instance of the `Urn` class representing the constructed URN.
     */
    fun generateUrn(namespace: Namespace, vararg nid: String = emptyArray()): Urn {
        val nidSet = nid.toSet().takeIf { it.isNotEmpty() }.orEmpty()
        return Urn(namespace, UUID.randomUUID().toString(), nidSet)
    }

    /**
     * Generates a URN (Uniform Resource Name) using the specified namespace and optional namespace identifiers.
     *
     * @param namespace The namespace to be used for the URN.
     * @param nid Vararg parameter representing optional namespace identifiers.
     * @return An instance of the `Urn` class representing the constructed URN.
     */
    fun generateUrn(namespace: String, vararg nid: String = emptyArray()): Urn {
        val nidSet = nid.toSet().takeIf { it.isNotEmpty() }.orEmpty()
        return Urn(namespace, UUID.randomUUID().toString(), nidSet)
    }
}

