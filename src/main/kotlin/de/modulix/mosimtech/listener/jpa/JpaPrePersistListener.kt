package de.modulix.mosimtech.listener.jpa

import de.modulix.mosimtech.database.base.BaseModel
import de.modulix.mosimtech.listener.beforeSaveListener
import jakarta.persistence.PrePersist

/**
 * Entity listener class for JPA entities implementing `BaseModel`.
 *
 * This listener ensures that certain actions are taken before an entity is persisted to the database.
 * Specifically, it invokes the `beforeSaveListener` method, which performs necessary preparations,
 * such as generating an ID if required.
 */
open class JpaPrePersistListener {

    /**
     * Method to be invoked before an entity that extends `BaseModel` is persisted.
     *
     * This method ensures that any necessary preparations are performed on the entity
     * before it is saved to the database. Specifically, it calls the `beforeSaveListener`
     * method to handle tasks such as generating a unique ID if required.
     *
     * @param entity The entity implementing `BaseModel` that is about to be persisted.
     * @throws IllegalAccessException if there is an issue accessing the properties of the entity.
     */
    @PrePersist
    @Throws(IllegalAccessException::class)
    open fun prePersist(entity: BaseModel) {
        beforeSaveListener(entity)
    }
}