package de.mosimtech.common.core.dto.momasoft

import de.mosimtech.common.core.dto.base.BaseDTO
import java.time.LocalDateTime

/**
 * Data Transfer Object (DTO) representing a user's profile information in the system.
 *
 * This class provides a structured definition for user profile data,
 * adhering to the standardized structure defined in the `BaseDTO` interface.
 * It includes metadata related to creation, modification, and validity, as well as
 * specific user-associated properties.
 *
 * Implements the `BaseDTO` interface to support common properties and behaviors
 * such as identifiers, timestamps, versioning, and validity.
 *
 * Key characteristics:
 * - Inherits all standard properties from `BaseDTO`, such as `id`, `creationDate`,
 *   `createdBy`, `lastModifiedDate`, `lastModifiedBy`, `userId`, `version`, and `valid`.
 * - Represents the profile of a user by including a `userId` property
 *   to associate the profile with a specific user entity.
 */
data class UserProfileDTO(
    override val id: String?,
    override val creationDate: LocalDateTime?,
    override val createdBy: String?,
    override val lastModifiedBy: String? = null,
    override val lastModifiedDate: LocalDateTime? = null,
    override val version: Long,
    override val valid: Boolean = true,
    override val userId: String?,
): BaseDTO
