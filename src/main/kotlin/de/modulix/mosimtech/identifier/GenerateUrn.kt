package de.modulix.mosimtech.identifier


/**
 * Annotation to automatically generate a URN for the annotated field.
 *
 * This annotation is typically used on fields where a unique identifier (URN) needs to be generated
 * automatically before persisting the object. The presence of this annotation on a field indicates
 * that the system should generate a URN for that field.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class GenerateUrn