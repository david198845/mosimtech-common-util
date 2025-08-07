package de.mosimtech.common.core.dto.base

import java.time.LocalDateTime

interface AuditableDTO : IdentifiableDTO {
    val creationDate: LocalDateTime?
    val lastModifiedDate: LocalDateTime?
    val lastModifiedBy: String?
    val createdBy: String?
}
