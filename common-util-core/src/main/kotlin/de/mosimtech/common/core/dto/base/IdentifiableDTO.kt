package de.mosimtech.common.core.dto.base

import java.io.Serializable

interface IdentifiableDTO : Serializable {
    val id: String?
    val version: Long?
    val valid: Boolean
}
