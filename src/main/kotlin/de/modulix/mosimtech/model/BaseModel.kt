package de.modulix.mosimtech.model

import de.modulix.mosimtech.model.urn.GenerateUrn
import de.modulix.mosimtech.identifier.IdGenerator
import de.modulix.mosimtech.model.urn.Urn
import java.time.ZonedDateTime


/**
 * BaseModel serves as a base class for entities that require common properties such as URN-based ID,
 * creation date, last modified date, and information about the creator and last modifier.
 *
 * @property id The unique identifier for the entity, represented as a URN. This is nullable and can be
 * generated automatically if annotated with `@GenerateUrn`.
 * @property creationDate The date and time when the entity was created.
 * @property lastModifiedDate The date and time when the entity was last modified, nullable.
 * @property lastModifiedBy The URN of the user or system that last modified the entity, nullable.
 * @property createdBy The URN of the user or system that created the entity.
 */
open class BaseModel(
    open val id: Urn? = null,
    open val creationDate: ZonedDateTime,
    open val lastModifiedDate: ZonedDateTime? = null,
    open val lastModifiedBy: Urn? = null,
    open val createdBy: Urn
    ) {

    /**
     * Method to be called before persisting the object.
     *
     * This method checks if the `id` field of the object is null. If it is null and the field has the
     * `@GenerateUrn` annotation, the method generates a new URN using `IdGenerator.generateUrn()`
     * and assigns it to the `id` field.
     */
    open fun prePersist() {
        if (id == null) {
            val field = this::class.java.getDeclaredField("id")
            if (field.isAnnotationPresent(GenerateUrn::class.java)) {
                field.isAccessible = true
                field.set(this, IdGenerator.generateUrn())
            }
        }
    }


    /**
     * Compares this BaseModel instance with another object for equality.
     *
     * @param other The object to compare with this instance.
     * @return `true` if the specified object is equal to this instance, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseModel

        if (id != other.id) return false
        if (creationDate != other.creationDate) return false
        if (lastModifiedDate != other.lastModifiedDate) return false
        if (lastModifiedBy != other.lastModifiedBy) return false
        if (createdBy != other.createdBy) return false

        return true
    }

    /**
     * Computes a hash code for this BaseModel instance.
     *
     * The hash code is generated using the `id`, `creationDate`, `lastModifiedDate`, `lastModifiedBy`,
     * and `createdBy` fields of the instance. If any of these fields are nullable, their respective
     * hash codes are substituted with default values.
     *
     * @return The hash code value for this instance.
     */
    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + creationDate.hashCode()
        result = 31 * result + (lastModifiedDate?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedBy?.hashCode() ?: 0)
        result = 31 * result + createdBy.hashCode()
        return result
    }


    /**
     * Creates a copy of the current BaseModel instance with optional new values for its properties.
     *
     * @param id the new ID of the instance, or the current ID if not specified.
     * @param creationDate the new creation date of the instance, or the current creation date if not specified.
     * @param lastModifiedDate the new last modified date of the instance, or the current last modified date if not specified.
     * @param lastModifiedBy the new identifier for who last modified the instance, or the current identifier if not specified.
     * @param createdBy the new identifier for who created the instance, or the current identifier if not specified.
     * @return a new instance of BaseModel with the specified properties.
     */
    open fun copy(
        id: Urn? = this.id,
        creationDate: ZonedDateTime = this.creationDate,
        lastModifiedDate: ZonedDateTime? = this.lastModifiedDate,
        lastModifiedBy: Urn? = this.lastModifiedBy,
        createdBy: Urn = this.createdBy
    ): BaseModel {
        return BaseModel(id, creationDate, lastModifiedDate, lastModifiedBy, createdBy)
    }
}