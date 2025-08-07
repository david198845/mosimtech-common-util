package de.mosimtech.common.r2dbc.entity

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import org.springframework.data.annotation.*
import org.springframework.data.relational.core.mapping.Column
import java.time.ZonedDateTime

@Persistent
abstract class AbstractAuditableEntity : AbstractEntity() {

    @Column("creation_date")
    @CreatedDate
    var creationDate: ZonedDateTime? = null

    @Column("created_by")
    @CreatedBy
    private var _createdBy: String? = null

    var createdBy: Urn?
        get() = _createdBy?.toUrn()
        set(value) {
            _createdBy = value?.toUrnString()
        }

    @Column("last_modified_by")
    @LastModifiedBy
    private var _lastModifiedBy: String? = null

    var lastModifiedBy: Urn?
        get() = _lastModifiedBy?.toUrn()
        set(value) {
            _lastModifiedBy = value?.toUrnString()
        }

    @Column("last_modified_date")
    @LastModifiedDate
    var lastModifiedDate: ZonedDateTime? = null


    override fun toString(): String {
        return "AbstractAuditableEntity(id=$id, creationDate=$creationDate, createdBy=$createdBy, " +
                "lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, " +
                "version=$version, valid=$valid)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractAuditableEntity) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (creationDate?.hashCode() ?: 0)
        result = 31 * result + (createdBy?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedBy?.hashCode() ?: 0)
        result = 31 * result + (lastModifiedDate?.hashCode() ?: 0)
        return result
    }
}
