package de.mosimtech.common.mongo.entity

import de.mosimtech.common.core.domain.Auditable
import de.mosimtech.common.core.urn.Urn
import org.springframework.data.annotation.*
import java.time.ZonedDateTime

/**
 * Mongo Auditable base entity mirroring the JPA AbstractAuditableEntity structure.
 */
@Persistent
abstract class AbstractAuditableEntity() : AbstractEntity(), Auditable {

    @CreatedDate
    override var creationDate: ZonedDateTime? = null

    @CreatedBy
    override var createdBy: Urn? = null

    @LastModifiedBy
    override var lastModifiedBy: Urn? = null

    @LastModifiedDate
    override var lastModifiedDate: ZonedDateTime? = null

    override fun toString(): String {
        return "AbstractAuditableEntity(id=$id, creationDate=$creationDate, createdBy=$createdBy, lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, version=$version, valid=$valid)"
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
