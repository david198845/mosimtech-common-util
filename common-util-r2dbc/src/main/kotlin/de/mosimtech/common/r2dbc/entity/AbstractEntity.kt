package de.mosimtech.common.r2dbc.entity

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Persistent
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Column

@Persistent
abstract class AbstractEntity {

    @Id
    @Column("id")
    private var _id: String? = null

    open var id: Urn?
        get() = _id?.toUrn()
        set(value) {
            _id = value?.toUrnString()
        }

    @Version
    @Column("revision")
    var version: Long? = null

    @Column("valid")
    var valid: Boolean? = true

    @Transient
    open fun isNew(): Boolean {
        return id == null || (id != null && id!!.isDefault())
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
