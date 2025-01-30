package de.modulix.mosimtech.dto.momasoft

import java.time.LocalDateTime

data class UserProfileDTO(
    val id: String?,
    val creationDate: LocalDateTime?,
    val createdBy: String?,
    val lastModifiedBy: String? = null,
    val lastModifiedDate: LocalDateTime? = null,
    val version: Long,
    val valid: Boolean = true,
)
