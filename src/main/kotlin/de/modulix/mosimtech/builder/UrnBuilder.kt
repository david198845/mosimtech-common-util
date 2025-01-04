package de.modulix.mosimtech.builder

import de.modulix.mosimtech.database.namespace.DefaultNamespace
import de.modulix.mosimtech.database.namespace.Namespace
import de.modulix.mosimtech.database.urn.Urn


/**
 * A builder class for constructing URNs (Uniform Resource Names) with specified namespaces and name identifiers.
 *
 * The `UrnBuilder` class extends `UrnGenerator` and provides a fluent interface for setting up and generating URNs.
 */
open class UrnBuilder : UrnGenerator() {

    protected var namespace: String = DefaultNamespace.Undefined.identifier
    protected var snid: Set<String> = emptySet()
    protected var nss: String = ""


    /**
     * Sets the namespace for the URN (Uniform Resource Name) being built.
     *
     * @param namespace The `Namespace` object representing the namespace to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    open fun withNamespace(namespace: Namespace): UrnBuilder {
        this.namespace = namespace.identifier
        return this
    }

    /**
     * Sets the namespace for the URN (Uniform Resource Name) being built.
     *
     * @param namespace The string representing the namespace to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    open fun withNamespace(namespace: String): UrnBuilder {
        this.namespace = namespace
        return this
    }

    /**
     * Sets the namespace identifiers (NIDs) for the URN (Uniform Resource Name) being built.
     *
     * @param snid Variable number of subnamespace identifiers to be used for the URN.
     * @return The `UrnBuilder` instance to allow method chaining.
     */
    open fun withSubNamespaceIdentifier(vararg snid: String): UrnBuilder {
        this.snid = snid.toSet()
        return this
    }

    open fun withNamespaceSpecificString(namespaceSpecificString: String): UrnBuilder {
        this.nss = namespaceSpecificString
        return this
    }

    /**
     * Constructs a new `Urn` instance using the provided namespace string and namespace identifiers (NIDs).
     *
     * @return A new `Urn` instance that represents the constructed Uniform Resource Name.
     */
    open fun build(): Urn {
        return generateUrn(namespace, nss, *snid.toTypedArray())
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
        fun generateID(
            namespace: Namespace,
            namespaceSpecificString: String = "",
            vararg nameIdentifiers: String
        ): Urn {
            return generateUrnBasedOnIdentifiers(namespace.identifier, namespaceSpecificString, *nameIdentifiers)
        }

        @JvmStatic
        fun generateID(namespace: String, namespaceSpecificString: String = "", vararg nameIdentifiers: String): Urn {
            return generateUrnBasedOnIdentifiers(namespace, namespaceSpecificString, *nameIdentifiers)
        }

        /**
         * Generates a URN (Uniform Resource Name) using the provided namespace and a variable number of name identifiers.
         *
         * This function combines the given namespace and name identifiers to create a standardized URN.
         *
         * @param namespace The Namespace within which the URN is being generated.
         * @param subNamespaceIdentifier A variable number of strings that serve as the unique name identifiers for the URN.
         * @return The generated URN based on the provided namespace and name identifiers.
         */
        protected fun generateUrnBasedOnIdentifiers(
            namespace: String,
            nameSpecificString: String = "",
            vararg subNamespaceIdentifier: String
        ): Urn {
            val urnGenerator = UrnBuilder()
            return if (subNamespaceIdentifier.isNotEmpty())
                urnGenerator.generateUrn(namespace, nameSpecificString, *subNamespaceIdentifier)
            else
                urnGenerator.generateUrn(namespace, nameSpecificString)
        }
    }


}
