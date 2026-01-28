package de.mosimtech.common.r2dbc.listener

import de.mosimtech.common.core.annotations.UrnNamespace
import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.r2dbc.entity.AbstractBaseEntity
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import org.springframework.data.relational.core.sql.SqlIdentifier
import reactor.core.publisher.Mono

/**
 * Reactive entity listener that handles the generation of URN identifiers for entities before they are saved.
 * This class implements BeforeConvertCallback to intercept entities before they are converted to database rows.
 */
class UrnEntityListener : BeforeConvertCallback<AbstractBaseEntity> {

    /**
     * Processes the entity before it is converted to a database row.
     * If the entity doesn't have an ID or has a default ID, a new URN identifier is generated based on the entity's @UrnNamespace annotation.
     *
     * @param entity The entity being processed
     * @param table The table where the entity will be stored
     * @return A Mono that emits the processed entity
     */
    override fun onBeforeConvert(entity: AbstractBaseEntity, table: SqlIdentifier): Mono<AbstractBaseEntity> {
        return Mono.just(entity).map { e ->
            if (e.id == null || e.id!!.isDefault()) {
                val annotation = e.javaClass.getAnnotation(UrnNamespace::class.java)
                    ?: throw IllegalStateException("Entity ${e.javaClass.simpleName} must be annotated with @UrnNamespace")

                e.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
            }
            e
        }
    }
}
