package de.mosimtech.common.jpa.listener

import de.mosimtech.common.core.annotations.UrnNamespace
import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.jpa.entity.AbstractEntity
import jakarta.persistence.PrePersist

class UrnEntityListener {


    @PrePersist
    fun onPrePersist(entity: AbstractEntity) {
        if (entity.id == null || entity.id!!.isDefault()) {
            val annotation = entity.javaClass.getAnnotation(UrnNamespace::class.java)
                ?: throw _root_ide_package_.kotlin.IllegalStateException("Entity ${entity.javaClass.simpleName} must be annotated with @UrnNamespace")

            entity.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
        }
    }

}
