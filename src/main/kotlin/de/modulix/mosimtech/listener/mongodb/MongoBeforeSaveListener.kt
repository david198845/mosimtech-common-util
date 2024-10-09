package de.modulix.mosimtech.listener.mongodb

import de.modulix.mosimtech.database.mongodb.AbstractBaseEntity
import de.modulix.mosimtech.listener.beforeSaveListener
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent

/**
 * Event listener that reacts to MongoDB's BeforeSaveEvent for entities extending AbstractBaseEntity.
 *
 * This listener intercepts the lifecycle of MongoDB entities before they are converted and saved.
 * It ensures that identifier fields are correctly auto-generated for new entities.
 */
class MongoBeforeSaveListener : AbstractMongoEventListener<AbstractBaseEntity>() {

    /**
     * Intercepts the lifecycle of MongoDB entities before they are converted and saved.
     *
     * @param event the event that contains the entity to be converted
     */
    override fun onBeforeConvert(event: BeforeConvertEvent<AbstractBaseEntity>) {
        val entity = event.getSource()
        beforeSaveListener(entity)
    }

}