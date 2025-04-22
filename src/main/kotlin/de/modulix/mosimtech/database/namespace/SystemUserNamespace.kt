package de.modulix.mosimtech.database.namespace

/**
 * An object that implements the Namespace interface to represent a system-related scope.
 *
 * SystemUserNamespace provides a unique identifier for system-level contexts, making it
 * possible to differentiate identifiers within the system namespace from those in other
 * namespaces.
 */
object SystemUserNamespace : Namespace {
    const val SYSTEM_USER_NAMESPACE_IDENTIFIER = "SYSTEM"
    override val identifier: String
        get() = SYSTEM_USER_NAMESPACE_IDENTIFIER
}