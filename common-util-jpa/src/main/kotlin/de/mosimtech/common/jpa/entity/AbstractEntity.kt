package de.mosimtech.common.jpa.entity

import de.mosimtech.common.core.domain.Identifiable
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import de.mosimtech.common.jpa.converter.UrnStringConverter
import de.mosimtech.common.jpa.listener.UrnEntityListener
import jakarta.persistence.*
import org.springframework.data.domain.Persistable

@MappedSuperclass
@EntityListeners(value = [UrnEntityListener::class])
abstract class AbstractEntity : Identifiable, Persistable<Urn> {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Convert(converter = UrnStringConverter::class)
    private var _id: String? = null

    fun setId(id: Urn) {
        this._id = id.toUrnString()
    }

    override fun getId(): Urn? {
        return this._id?.toUrn()
    }

    @Version
    @Column(name = "revision", nullable = false)
    override var version: Long? = null

    @Column(name = "valid", nullable = false, columnDefinition = "boolean default true")
    override var valid: Boolean = true

    override fun isNew(): Boolean {
        return getId() == null || (getId() != null && getId()!!.isDefault())
    }

    override fun toString(): String {
        return "AbstractEntity(getId()=$id, version=$version, valid=$valid)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractEntity) return false
        return getId() == other.getId()
    }

    override fun hashCode(): Int {
        return getId()?.hashCode() ?: 0
    }


}
