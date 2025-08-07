package de.mosimtech.common.jpa.entity

import de.mosimtech.common.core.domain.BaseModel
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.converter.UrnStringConverter
import de.mosimtech.common.jpa.listener.UrnEntityListener
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.ZonedDateTime

/**
 * Abstract base class representing a base entity in the system.
 *
 * This class provides common properties and mappings for entities such as creation date, created by,
 * last modified by, and last modified date. It extends `BaseModel` interface ensuring adherence
 * to common entity field requirements.
 *
 * Annotations:
 * @MappedSuperclass: Indicates that this class is a JPA mapped superclass.
 *
 * Properties:
 * @property creationDate The date and time when the object was created.
 * @property createdBy The identifier (URN) of the user or system that created the object.
 * @property lastModifiedBy The identifier (URN) of the user or system that last modified the object.
 * @property lastModifiedDate The date and time when the object was last modified.
 */
@MappedSuperclass
@EntityListeners(value = [AuditingEntityListener::class, UrnEntityListener::class])
abstract class AbstractBaseEntity() : AbstractAuditableEntity(), BaseModel {

    @Column(name = "user_id", nullable = false)
    @Convert(converter = UrnStringConverter::class)
    override lateinit var userId: Urn

    constructor(
        creationDate: ZonedDateTime,
        createdBy: Urn,
        lastModifiedBy: Urn?,
        lastModifiedDate: ZonedDateTime?,
        userId: Urn
    ) : this() {
        this.creationDate = creationDate
        this.createdBy = createdBy
        this.lastModifiedBy = lastModifiedBy
        this.lastModifiedDate = lastModifiedDate
        this.userId = userId
    }

    override fun toString(): String {
        return "AbstractBaseEntity(id=$id, creationDate=$creationDate, createdBy=$createdBy, " +
                "lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, " +
                "userId=$userId, version=$version, valid=$valid)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractBaseEntity) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
