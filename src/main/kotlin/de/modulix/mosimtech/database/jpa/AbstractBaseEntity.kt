package de.modulix.mosimtech.database.jpa

import de.modulix.mosimtech.converter.jpa.UrnStringConverter
import de.modulix.mosimtech.database.base.BaseModel
import de.modulix.mosimtech.database.urn.Urn
import de.modulix.mosimtech.database.urn.toUrn
import de.modulix.mosimtech.listener.jpa.UrnEntityListener
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
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
abstract class AbstractBaseEntity() : BaseModel {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Convert(converter = UrnStringConverter::class)
    private var _id: String? = null

    override var id: Urn?
        get() = _id?.toUrn()
        set(value) {
            _id = value?.toUrnString()
        }

    @Column(name = "creation_date", nullable = false)
    @CreatedDate
    override var creationDate: ZonedDateTime? = null

    @Column(name = "created_by", nullable = false)
    @Convert(converter = UrnStringConverter::class)
    @CreatedBy
    override var createdBy: Urn? = null

    @Column(name = "last_modified_by", nullable = true)
    @Convert(converter = UrnStringConverter::class)
    @LastModifiedBy
    override var lastModifiedBy: Urn? = null

    @Column(name = "last_modified_date", nullable = true)
    @LastModifiedDate
    override var lastModifiedDate: ZonedDateTime? = null

    @Column(name = "user_id", nullable = false)
    @Convert(converter = UrnStringConverter::class)
    override lateinit var userId: Urn

    @Version
    @Column(name = "revision", nullable = false)
    override var version: Long? = null

    @Column(name = "valid", nullable = false, columnDefinition = "boolean default true")
    override var valid: Boolean? = true


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
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}