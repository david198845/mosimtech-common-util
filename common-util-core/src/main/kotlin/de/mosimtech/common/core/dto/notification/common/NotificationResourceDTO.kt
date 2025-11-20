package de.mosimtech.common.core.dto.notification.common

import de.mosimtech.common.core.dto.base.BaseDTO
import java.time.LocalDateTime

/**
 * Represents a notification resource in the system, providing metadata and user information
 * related to a specific notification.
 *
 * This class extends the `BaseDTO` interface and inherits its common properties, such as
 * identifier, creation date, modification details, user association, and validity status.
 * It serves as a data transfer object (DTO) for transferring notification-related
 * information within the application.
 *
 * Properties:
 * - creationDate: The date and time when the notification resource was created.
 * - lastModifiedDate: The date and time when the notification resource was last updated.
 * - lastModifiedBy: The identifier of the user who made the last modification.
 * - createdBy: The identifier of the user who originally created the notification resource.
 * - id: The unique identifier for the notification resource.
 * - version: The version number for concurrency control of the resource.
 * - valid: A boolean flag indicating whether the notification resource is in a valid state.
 * - userId: The identifier of the user associated with the notification.
 */
data class NotificationResourceDTO(
    override val creationDate: LocalDateTime? = null,
    override val lastModifiedDate: LocalDateTime? = null,
    override val lastModifiedBy: String? = null,
    override val createdBy: String? = null,
    override val id: String? = null,
    override val version: Long? = null,
    override val valid: Boolean = true,
    override val userId: String? = null,
) : BaseDTO
