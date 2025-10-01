package de.mosimtech.common.mongo.listener

import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.jpa.repository.annotations.UrnNamespace
import de.mosimtech.common.mongo.entity.AbstractEntity
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent

/**
 * Event listener that reacts to MongoDB's BeforeSaveEvent for entities extending AbstractEntity.
 *
 * This listener intercepts the lifecycle of MongoDB entities before they are converted and saved.
 * It ensures that identifier fields are correctly auto-generated for new entities.
 */
open class MongoBeforeSaveListener : AbstractMongoEventListener<AbstractEntity>() {

    /**
     * Intercepts the lifecycle of MongoDB entities before they are converted and saved.
     *
     * @param event the event that contains the entity to be converted
     */
    override fun onBeforeConvert(event: BeforeConvertEvent<AbstractEntity>) {
        val entity = event.source
        if (entity.id == null || entity.id!!.isDefault()) {
            val annotation = entity.javaClass.getAnnotation(UrnNamespace::class.java)
                ?: throw IllegalStateException("Entity ${entity.javaClass.simpleName} must be annotated with @UrnNamespace")

            entity.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
        }
    }

}
