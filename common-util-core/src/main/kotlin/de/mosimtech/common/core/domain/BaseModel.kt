package de.mosimtech.common.core.domain

import de.mosimtech.common.core.urn.Urn

/**
 * BaseModel is an interface that represents the foundational structure
 * for domain model objects within the system. It extends the Auditable
 * interface, inheriting properties related to the creation and modification
 * audit information. This structure ensures that all implementing classes
 * contain audit information and a unique identifier for the associated user.
 *
 * @property userId Represents the unique identifier associated with a user
 *                  in the form of a URN (Uniform Resource Name).
 */
interface BaseModel : Auditable {
    val userId: Urn?
}
