package de.modulix.mosimtech.database.annotations

/**
 * Annotation used to define a URN (Uniform Resource Name) namespace for an entity.
 *
 * This annotation is intended to be applied to classes that require auto-generation of
 * identifiers in a hierarchical structure based on namespaces and sub-namespaces.
 * It can be leveraged to ensure that entities conform to a consistent URN formatting standard.
 *
 * Attributes:
 * @property value The primary namespace for the entity.
 *                 This is typically a descriptive name that identifies the entity's domain.
 * @property subNamespaces Optional sub-namespaces that can be used to further specify
 *                         hierarchical classification within the primary namespace.
 *
 * Target:
 * - Can only be applied to classes.
 *
 * Retention:
 * - The annotation is retained at runtime and can be accessed via reflection.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class UrnNamespace(
    val value: String,
    val subNamespaces: Array<String> = []
)