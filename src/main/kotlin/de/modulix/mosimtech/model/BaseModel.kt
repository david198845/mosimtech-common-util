package de.modulix.mosimtech.model

import de.modulix.mosimtech.model.urn.GenerateUrn
import de.modulix.mosimtech.identifier.IdGenerator
import de.modulix.mosimtech.model.urn.Urn
import java.time.ZonedDateTime



/**
 * Interface representing a base model in the system.
 *
 * This interface defines common properties like id, creationDate, lastModifiedDate, lastModifiedBy,
 * and createdBy that are typically present in entities managed by the persistence layer.
 * It also includes a `prePersist` method that is meant to be called before the object is persisted.
 *
 * Properties:
 * @property id The unique identifier for the object, represented as a URN (Uniform Resource Name).
 * @property creationDate The date and time when the object was created.
 * @property lastModifiedDate The date and time when the object was last modified.
 * @property lastModifiedBy The identifier (URN) of the user or system that last modified the object.
 * @property createdBy The identifier (URN) of the user or system that created the object.
 */
interface BaseModel {


    val id: Urn?
    val creationDate: ZonedDateTime
    val lastModifiedDate: ZonedDateTime?
    val lastModifiedBy: Urn?
    val createdBy: Urn

    /**
     * Method to be called before persisting the object.
     *
     * This method checks if the `id` field of the object is null. If it is null and the field has the
     * `@GenerateUrn` annotation, the method generates a new URN using `IdGenerator.generateUrn()`
     * and assigns it to the `id` field.
     */
    fun prePersist() {
        if (id == null) {
            val field = this::class.java.getDeclaredField("id")
            if (field.isAnnotationPresent(GenerateUrn::class.java)) {
                field.isAccessible = true
                field.set(this, IdGenerator.generateUrn())
            }
        }
    }

}