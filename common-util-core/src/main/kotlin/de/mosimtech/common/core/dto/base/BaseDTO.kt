package de.mosimtech.common.core.dto.base

/**
 * Represents a base Data Transfer Object (DTO) that defines common properties
 * and behavior for DTOs in the application.
 *
 * This interface enforces a set of standard fields typically present in entities
 * or data transfer objects, such as identifiers, creation and modification
 * metadata, user information, and validity status. It ensures consistency and
 * reusability across different DTO implementations.
 *
 * Properties:
 * - id: Represents the unique identifier for the entity or DTO.
 * - creationDate: The date and time at which the entity or data was created.
 * - lastModifiedDate: The date and time at which the entity or data was last modified.
 * - lastModifiedBy: The user or entity who last modified the data.
 * - createdBy: The user or entity who originally created the data.
 * - userId: The identifier of the user associated with this DTO.
 * - version: Represents the versioning information for concurrency control.
 * - valid: Indicates whether the entity or DTO is in a valid state.
 *
 * Functions:
 * - setInvalid(): Marks the entity or DTO as invalid by setting the `valid` property to false.
 */
interface BaseDTO : AuditableDTO {
    val userId: String?
}
