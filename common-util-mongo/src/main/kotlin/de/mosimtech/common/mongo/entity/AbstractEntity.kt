package de.mosimtech.common.mongo.entity

import de.mosimtech.common.core.domain.Identifiable
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Persistent
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.mapping.Field

/**
 * Mongo base entity providing id, version and valid fields consistent with JPA/R2DBC modules.
 * Uses a backing String field for MongoDB @Id while exposing Urn as the public id type.
 */
@Persistent
abstract class AbstractEntity() : Identifiable {

    @Id
    @Field("id")
    private var _id: String? = null

    @get:Transient
    @set:Transient
    override var id: Urn?
        get() = _id?.toUrn()
        set(value) {
            _id = value?.toUrnString()
        }

    @Version
    @Field("revision")
    override var version: Long? = null

    @Field("valid")
    override var valid: Boolean = true

    @Transient
    open fun isNew(): Boolean {
        return id == null || (id != null && id!!.isDefault())
    }

    override fun toString(): String {
        return "AbstractEntity(id=$id, version=$version, valid=$valid)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
