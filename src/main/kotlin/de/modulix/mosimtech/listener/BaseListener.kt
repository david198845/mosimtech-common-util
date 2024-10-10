package de.modulix.mosimtech.listener

import de.modulix.mosimtech.builder.UrnBuilder
import de.modulix.mosimtech.builder.UrnGeneratorSequence
import de.modulix.mosimtech.database.base.BaseModel

/**
 * Listener method to be invoked before an entity that extends `BaseModel` is persisted.
 *
 * This method ensures that the entity's ID is generated if it is not already set. It will use
 * the `UrnGenerator.generateID` to create a URN identifier for fields annotated with `UrnGeneratorSequence`.
 *
 * @param entity The entity implementing `BaseModel` that is about to be persisted.
 */
fun beforeSaveListener(entity: BaseModel) {
    entity.id?.takeIf { it.isDefault() }?.let {
        val field = entity::class.java.getDeclaredField("id")
        field.takeIf { it.isAnnotationPresent(UrnGeneratorSequence::class.java) }?.apply {
            isAccessible = true
            val annotation = getAnnotation(UrnGeneratorSequence::class.java)
            val namespace = annotation.namespace
            set(entity, UrnBuilder.generateID(namespace = namespace, nameIdentifiers = annotation.snid))
        }
    }
}