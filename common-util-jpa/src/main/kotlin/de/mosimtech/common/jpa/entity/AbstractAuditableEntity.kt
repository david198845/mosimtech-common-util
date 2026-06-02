package de.mosimtech.common.jpa.entity

import de.mosimtech.common.core.domain.Auditable
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.converter.UrnStringConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.OffsetDateTime

@MappedSuperclass
@EntityListeners(value = [AuditingEntityListener::class])
abstract class AbstractAuditableEntity : AbstractEntity(), Auditable {

    @Column(name = "creation_date", nullable = false)
    @CreatedDate
    override var creationDate: OffsetDateTime? = null

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
    override var lastModifiedDate: OffsetDateTime? = null


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
