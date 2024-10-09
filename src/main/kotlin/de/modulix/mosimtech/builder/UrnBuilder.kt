package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.base.namespace.DefaultNamespace
import de.modulix.mosimtech.database.base.namespace.Namespace
import de.modulix.mosimtech.database.base.urn.Urn


/**
 * A builder class for constructing URNs (Uniform Resource Names) with specified namespaces and name identifiers.
 *
 * The `UrnBuilder` class extends `UrnGenerator` and provides a fluent interface for setting up and generating URNs.
 */
open class UrnBuilder : UrnGenerator() {

    private var namespaceString: String = DefaultNamespace.Undefined.identifier
    private var nid: Set<String> = emptySet()


    /**
     * Sets the namespace for the URN (Uniform Resource Name) being built.
     *
     * @param namespace The `Namespace` object representing the namespace to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    fun namespace(namespace: Namespace): UrnBuilder {
        this.namespaceString = namespace.identifier
        return this
    }

    /**
     * Sets the namespace for the URN (Uniform Resource Name) being built.
     *
     * @param namespace The string representing the namespace to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    fun namespace(namespace: String): UrnBuilder {
        this.namespaceString = namespace
        return this
    }

    /**
     * Sets the namespace identifiers (NIDs) for the URN (Uniform Resource Name) being built.
     *
     * @param nid Variable number of namespace identifiers to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    fun nid(vararg nid: String): UrnBuilder {
        this.nid = nid.toSet()
        return this
    }

    /**
     * Constructs a new `Urn` instance using the provided namespace string and namespace identifiers (NIDs).
     *
     * @return A new `Urn` instance that represents the constructed Uniform Resource Name.
     */
    fun build(): Urn {
        return generateUrn(namespaceString, *nid.toTypedArray())
    }

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
            return generateUrnBasedOnIdentifiers(namespace.identifier, *nameIdentifiers)
        }

        @JvmStatic
        fun generateID(namespace: String, vararg nameIdentifiers: String): Urn {
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
        private fun generateUrnBasedOnIdentifiers(namespace: String, vararg nameIdentifiers: String): Urn {
            val urnGenerator = UrnBuilder()
            return if (nameIdentifiers.isNotEmpty())
                urnGenerator.generateUrn(namespace, *nameIdentifiers)
            else
                urnGenerator.generateUrn(namespace)
        }
    }


}
