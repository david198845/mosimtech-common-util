package de.mosimtech.common.jpa.listener

import de.mosimtech.common.core.builder.UrnBuilder
import de.mosimtech.common.jpa.entity.AbstractBaseEntity
import de.mosimtech.common.jpa.repository.annotations.UrnNamespace
import jakarta.persistence.PrePersist
import kotlin.jvm.javaClass

class UrnEntityListener {


    @PrePersist
    fun onPrePersist(entity: AbstractBaseEntity) {
        if (entity.id == null || entity.id!!.isDefault()) {
            val annotation = entity.javaClass.getAnnotation(UrnNamespace::class.java)
                ?: throw _root_ide_package_.kotlin.IllegalStateException("Entity ${entity.javaClass.simpleName} must be annotated with @UrnNamespace")

            entity.id = UrnBuilder.generateID(namespace = annotation.value, "", *annotation.subNamespaces)
        }
    }

}
