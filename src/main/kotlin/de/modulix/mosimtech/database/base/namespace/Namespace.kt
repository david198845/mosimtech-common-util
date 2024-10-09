package de.modulix.mosimtech.database.base.namespace

/**
 * Represents a named scope or context within which specific identifiers, such as those in URNs (Uniform Resource Names),
 * can be defined and used.
 *
 * The `Namespace` interface provides an abstraction for grouping related identifiers, offering a flexible
 * mechanism to categorize and manage different scopes in applications that require hierarchical or segmented identification schemas.
 *
 * Properties:
 * @property identifier A unique string that identifies the namespace, ensuring that identifiers within this namespace
 * are distinct from those in other namespaces.
 */
interface Namespace {
    val identifier: String
}