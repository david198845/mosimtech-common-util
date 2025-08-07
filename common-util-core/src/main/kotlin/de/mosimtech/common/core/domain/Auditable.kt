package de.mosimtech.common.core.domain

import de.mosimtech.common.core.urn.Urn
import java.time.ZonedDateTime

/**
 * Represents an entity capable of maintaining audit information such as creation and
 * modification details. Extends Identifiable interface to include unique identity
 * properties essential for the domain model entities.
 *
 * The auditing properties provide visibility into when the entity was created, last
 * modified, and by whom these actions were performed. This facilitates tracking
 * and accountability for significant actions performed on the entity.
 *
 * @property creationDate The date and time when the entity was initially created.
 * @property lastModifiedDate The date and time when the entity was last modified.
 * @property lastModifiedBy The identifier of the user who made the most recent modification.
 * @property createdBy The identifier of the user who created the entity.
 */
interface Auditable : Identifiable {
    val creationDate: ZonedDateTime?
    var lastModifiedDate: ZonedDateTime?
    var lastModifiedBy: Urn?
    val createdBy: Urn?
}
