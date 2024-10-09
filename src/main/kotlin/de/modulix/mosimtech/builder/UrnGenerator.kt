package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.base.namespace.Namespace
import de.modulix.mosimtech.database.base.urn.Urn


/**
 * Utility class for generating Uniform Resource Names (URNs).
 *
 * This class extends `UrnBuilder` and provides a mechanism
 * for generating URNs based on specified namespaces and identifiers.
 */
class UrnGenerator : UrnBuilder() {

    companion object {
        /**
         * Generates a URN (Uniform Resource Name) using the provided namespace and name identifiers.
         *
         * This method combines the namespace and the variable number of name identifiers
         * to create a standardized URN.
         *
         * @param namespace The Namespace within which the URN is being generated.
         * @param nameIdentifiers A variable number of strings that serve as the unique name identifiers for the URN.
         * @return The generated URN based on the provided namespace and name identifiers.
         */
        @JvmStatic
        fun generateID(namespace: Namespace, vararg nameIdentifiers: String): Urn {
            return generateUrnBasedOnIdentifiers(namespace, *nameIdentifiers)
        }

        /**
         * Generates a URN (Uniform Resource Name) using the provided namespace and a variable number of name identifiers.
         *
         * This function combines the given namespace and name identifiers to create a standardized URN.
         *
         * @param namespace The Namespace within which the URN is being generated.
         * @param nameIdentifiers A variable number of strings that serve as the unique name identifiers for the URN.
         * @return The generated URN based on the provided namespace and name identifiers.
         */
        private fun generateUrnBasedOnIdentifiers(namespace: Namespace, vararg nameIdentifiers: String): Urn {
            val urnGenerator = UrnGenerator()
            return if (nameIdentifiers.isNotEmpty())
                urnGenerator.generateUrn(namespace, *nameIdentifiers)
            else
                urnGenerator.generateUrn(namespace)
        }
    }


}
