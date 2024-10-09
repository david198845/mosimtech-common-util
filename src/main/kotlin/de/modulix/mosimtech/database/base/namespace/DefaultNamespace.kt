package de.modulix.mosimtech.database.base.namespace

/**
 * Enum class representing the default namespaces available for URNs.
 *
 * This class implements the Namespace interface, providing a base set of predefined namespaces
 * for URN handling. The default namespace included is 'Undefined', which is used when no valid
 * namespace is identified.
 *
 * @property identifier The unique identifier for the namespace.
 */
enum class DefaultNamespace(namespaceId: String) : Namespace {

    /**
     * Represents the default namespace used when no valid namespace is identified.
     *
     * This is a predefined enum value in the DefaultNamespace class and has the identifier "undefined".
     */
    Undefined("undefined");

    override val identifier = namespaceId
}