package de.modulix.mosimtech.listener.jpa

import de.modulix.mosimtech.builder.UrnBuilder
import de.modulix.mosimtech.database.annotations.UrnNamespace
import de.modulix.mosimtech.database.jpa.AbstractBaseEntity
import jakarta.persistence.PrePersist

class UrnEntityListener {


    @PrePersist
    fun onPrePersist(entity: AbstractBaseEntity) {
        if (entity.id == null || entity.id!!.isDefault()) {
            val annotation = entity.javaClass.getAnnotation(UrnNamespace::class.java)
                ?: throw IllegalStateException("Entity ${entity.javaClass.simpleName} must be annotated with @UrnNamespace")

            entity.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
        }
    }

}