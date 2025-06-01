package de.mosimtech.common.core.builder

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import java.util.*

/**
 * An interface for generating Uniform Resource Names (URNs).
 * It provides methods to create URNs with specified namespaces and optional namespace identifiers.
 */
abstract class UrnGenerator {

    /**
     * Generates a URN string using the specified namespace, an optional namespace-specific string (NSS),
     * and optional namespace identifiers (snid).
     *
     * @param namespace The namespace to be used for the URN.
     * @param nss Optional namespace-specific string to be included in the URN. Defaults to an empty string.
     * @param snid Vararg parameter representing optional sub namespace identifiers.
     * @return A string representation of the constructed URN.
     */
    fun generateUrnString(namespace: Namespace, nss: String = "", vararg snid: String = emptyArray()): String {
        return generateUrn(namespace, nss, *snid).toUrnString()
    }

    /**
     * Generates a URN using the specified namespace, an optional namespace-specific string (NSS),
     * and optional namespace identifiers (snid).
     *
     * @param namespace The namespace to be used for the URN.
     * @param nss Optional namespace-specific string to be included in the URN. Defaults to an empty string.
     * @param snid Vararg parameter representing optional sub namespace identifiers.
     * @return A URN object constructed with the provided parameters.
     */
    fun generateUrn(namespace: Namespace, nss: String = "", vararg snid: String = emptyArray()): Urn {
        val nidSet = snid.toSet().takeIf { it.isNotEmpty() }.orEmpty()
        return if (nss.isNotEmpty()) Urn(namespace.identifier, nss, nidSet)
        else Urn(namespace, UUID.randomUUID().toString(), nidSet)
    }

    /**
     * Generates a URN (Uniform Resource Name) using the specified namespace, an optional
     * namespace-specific string (NSS), and optional namespace identifiers (snid).
     *
     * @param namespace The namespace to be used for the URN.
     * @param nss Optional namespace-specific string to be included in the URN. Defaults to an empty string.
     * @param snid Vararg parameter representing optional sub namespace identifiers.
     * @return A URN object constructed with the provided parameters.
     */
    fun generateUrn(namespace: String, nss: String = "", vararg snid: String = emptyArray()): Urn {
        val nidSet = snid.toSet().takeIf { it.isNotEmpty() }.orEmpty()
        return if (nss.isNotEmpty()) Urn(namespace, nss, nidSet)
        else Urn(namespace, UUID.randomUUID().toString(), nidSet)
    }
}

