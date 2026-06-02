package de.mosimtech.common.mongo.entity

import de.mosimtech.common.core.domain.BaseModel
import de.mosimtech.common.core.urn.Urn
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.Persistent
import java.time.OffsetDateTime

/**
 * Abstract base class for Mongo entities aligning with the JPA structure.
 *
 * Extends AbstractAuditableEntity and adds the userId field. Version and valid are provided by
 * AbstractEntity; auditing fields are provided by AbstractAuditableEntity.
 */
@Persistent
abstract class AbstractBaseEntity() : AbstractAuditableEntity(), BaseModel {

    @NotNull
    override lateinit var userId: Urn

    constructor(
        creationDate: OffsetDateTime,
        createdBy: Urn,
        lastModifiedBy: Urn?,
        lastModifiedDate: OffsetDateTime?,
        userId: Urn
    ) : this() {
        this.creationDate = creationDate
        this.createdBy = createdBy
        this.lastModifiedBy = lastModifiedBy
        this.lastModifiedDate = lastModifiedDate
        this.userId = userId
    }

    override fun toString(): String {
        return "AbstractBaseEntity(id=$id, creationDate=$creationDate, createdBy=$createdBy, lastModifiedBy=$lastModifiedBy, lastModifiedDate=$lastModifiedDate, userId=$userId, version=$version, valid=$valid)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractBaseEntity) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
