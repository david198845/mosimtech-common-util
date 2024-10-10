package de.modulix.mosimtech.database.mongodb

import de.modulix.mosimtech.database.base.BaseModel
import de.modulix.mosimtech.database.base.urn.Urn
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime

/**
 * Abstract base class for entities that provides common properties for tracking creation and
 * modification metadata.
 *
 * This class extends the `BaseModel` interface, implementing properties that store information
 * about the creation and modification timestamps, as well as the users or systems responsible
 * for these actions.
 *
 * Properties:
 * @property creationDate The date and time when the entity was created. This property is marked
 * with the `@CreatedDate` annotation to automatically handle the persistence of creation
 * timestamps.
 * @property createdBy The identifier (URN) of the user or system that created the entity. This property
 * is marked with the `@CreatedBy` annotation to automatically handle the persistence of the creator's
 * information.
 * @property lastModifiedBy The identifier (URN) of the user or system that last modified the entity.
 * This property is marked with the `@LastModifiedBy` annotation to automatically handle the
 * persistence of modification details.
 * @property lastModifiedDate The date and time when the entity was last modified. This property
 * is marked with the `@LastModifiedDate` annotation to automatically handle the persistence of
 * modification timestamps.
 */
abstract class AbstractBaseEntity : BaseModel {
    @CreatedDate
    @NotNull
    override var creationDate: ZonedDateTime? = null

    @CreatedBy
    @NotNull
    override var createdBy: Urn? = null

    @LastModifiedBy
    override var lastModifiedBy: Urn? = null

    @LastModifiedDate
    override var lastModifiedDate: ZonedDateTime? = null

    @NotNull
    override val userId: Urn? = null
}
