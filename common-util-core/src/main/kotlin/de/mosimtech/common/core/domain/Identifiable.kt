package de.mosimtech.common.core.domain

import de.mosimtech.common.core.urn.Urn
import java.io.Serializable

/**
 * Represents an identifiable entity that has a unique identifier, versioning, and validity state.
 *
 * This interface is designed to ensure that implementing entities have a unique identifier (URN),
 * a version indicating the state of the entity, and a valid/invalid status for tracking its usability.
 * It extends the Serializable interface, enabling its instances to be serialized.
 */
interface Identifiable : Serializable {
    var id: Urn?
    var version: Long?
    var valid: Boolean

    fun isNew(): Boolean {
        return id == null || (id != null && id!!.isDefault())
    }

    fun setInvalid() {
        this.valid = false
    }


}
