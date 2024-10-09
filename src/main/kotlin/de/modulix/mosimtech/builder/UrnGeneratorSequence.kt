package de.modulix.mosimtech.builder


/**
 * Annotation to generate a URN (Uniform Resource Name) based identifier sequence for a field in an entity.
 *
 * This annotation is used to mark a field within an entity that should be populated with a URN generated
 * based on the specified `Namespace` and optional namespace identifiers (`nid`).
 *
 * @property namespace Specifies the `Namespace` class used to define the context or scope for the URN generation.
 * @property nid Optional array of strings representing specific namespace identifiers. These identifiers help in further
 * categorizing or segmenting the generated URNs within the specified namespace.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
annotation class UrnGeneratorSequence(val namespace: String, vararg val nid: String = []) {
}