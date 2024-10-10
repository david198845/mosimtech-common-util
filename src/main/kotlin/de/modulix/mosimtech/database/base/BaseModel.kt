package de.modulix.mosimtech.database.base

import de.modulix.mosimtech.database.base.urn.Urn
import java.io.Serializable
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
interface BaseModel : Serializable {
    val id: Urn?
    val creationDate: ZonedDateTime?
    val lastModifiedDate: ZonedDateTime?
    val lastModifiedBy: Urn?
    val createdBy: Urn?
    val userId: Urn?
}