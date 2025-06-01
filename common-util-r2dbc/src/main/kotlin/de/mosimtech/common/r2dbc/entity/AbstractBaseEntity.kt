package de.mosimtech.common.r2dbc.entity

import de.mosimtech.common.core.domain.BaseModel
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Persistent
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column
import java.time.ZonedDateTime

/**
 * Abstract base class representing a base entity in the system for R2DBC.
 *
 * This class provides common properties and mappings for reactive entities such as creation date, created by,
 * last modified by, and last modified date. It extends `BaseModel` interface ensuring adherence
 * to common entity field requirements.
 *
 * Properties:
 * @property creationDate The date and time when the object was created.
 * @property createdBy The identifier (URN) of the user or system that created the object.
 * @property lastModifiedBy The identifier (URN) of the user or system that last modified the object.
 * @property lastModifiedDate The date and time when the object was last modified.
 */
@Persistent
abstract class AbstractBaseEntity() : BaseModel {

    @Id
    @Column("id")
    private var _id: String? = null

    override var id: Urn?
        get() = _id?.toUrn()
        set(value) {
            _id = value?.toUrnString()
        }

    @Column("creation_date")
    @CreatedDate
    override var creationDate: ZonedDateTime? = null

    @Column("created_by")
    @CreatedBy
    private var _createdBy: String? = null

    override var createdBy: Urn?
        get() = _createdBy?.toUrn()
        set(value) {
            _createdBy = value?.toUrnString()
        }

    @Column("last_modified_by")
    @LastModifiedBy
    private var _lastModifiedBy: String? = null

    override var lastModifiedBy: Urn?
        get() = _lastModifiedBy?.toUrn()
        set(value) {
            _lastModifiedBy = value?.toUrnString()
        }

    @Column("last_modified_date")
    @LastModifiedDate
    override var lastModifiedDate: ZonedDateTime? = null

    @Column("user_id")
    private var _userId: String? = null

    override var userId: Urn
        get() = _userId?.toUrn() ?: throw IllegalStateException("userId not set")
        set(value) {
            _userId = value.toUrnString()
        }

    @Version
    @Column("revision")
    override var version: Long? = null

    @Column("valid")
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

    @Transient
    override fun isNew(): Boolean {
        return id == null || (id != null && id!!.isDefault())
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
