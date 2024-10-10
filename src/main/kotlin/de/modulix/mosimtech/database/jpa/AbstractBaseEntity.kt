package de.modulix.mosimtech.database.jpa

import de.modulix.mosimtech.converter.jpa.UrnStringConverter
import de.modulix.mosimtech.database.base.BaseModel
import de.modulix.mosimtech.database.base.urn.Urn
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.ZonedDateTime

/**
 * Abstract base class representing a base entity in the system.
 *
 * This class provides common properties and mappings for entities such as creation date, created by,
 * last modified by, and last modified date. It extends `BaseModel` interface ensuring adherence
 * to common entity field requirements.
 *
 * Annotations:
 * @MappedSuperclass: Indicates that this class is a JPA mapped superclass.
 *
 * Properties:
 * @property creationDate The date and time when the object was created.
 * @property createdBy The identifier (URN) of the user or system that created the object.
 * @property lastModifiedBy The identifier (URN) of the user or system that last modified the object.
 * @property lastModifiedDate The date and time when the object was last modified.
 */
@MappedSuperclass
abstract class AbstractBaseEntity : BaseModel {

    @Column(name = "creationDate", nullable = false)
    @CreatedDate
    override lateinit var creationDate: ZonedDateTime

    @Column(name = "createdBy", nullable = false)
    @Convert(converter = UrnStringConverter::class)
    @CreatedBy
    override lateinit var createdBy: Urn

    @Column(name = "lastModifiedBy", nullable = false)
    @Convert(converter = UrnStringConverter::class)
    @LastModifiedBy
    override var lastModifiedBy: Urn? = null

    @Column(name = "lastModifiedDate", nullable = false)
    @LastModifiedDate
    override var lastModifiedDate: ZonedDateTime? = null

    @Column(name = "userId", nullable = false)
    override lateinit var userId: Urn

}