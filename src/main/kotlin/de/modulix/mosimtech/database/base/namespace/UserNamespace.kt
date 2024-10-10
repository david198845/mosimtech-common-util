package de.modulix.mosimtech.database.base.namespace

/**
 * An object that implements the Namespace interface to represent a specific user-related scope.
 *
 * UserNamespace provides a unique identifier for user-related contexts to distinguish identifiers
 * within the user namespace from those in other namespaces.
 */
object UserNamespace : Namespace {
    const val USER_NAMESPACE_IDENTIFIER = "user"
    override val identifier: String
        get() = USER_NAMESPACE_IDENTIFIER
}