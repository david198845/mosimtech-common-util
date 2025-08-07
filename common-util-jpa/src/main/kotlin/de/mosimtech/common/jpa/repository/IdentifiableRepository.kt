package de.mosimtech.common.jpa.repository

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.entity.AbstractEntity
import io.hypersistence.utils.spring.repository.BaseJpaRepository
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface IdentifiableRepository<T : AbstractEntity> : BaseJpaRepository<T, String> {

    fun findById(id: Urn): T?
    fun existsById(id: Urn): Boolean
    fun deleteById(id: Urn)
    fun findAll(): List<T>

    fun findByNamespace(namespace: String): List<T>
    fun findByNamespace(namespace: Namespace): List<T>
    fun findBySubNamespace(subNamespace: List<String>): List<T>
    fun findBySubNamespace(subNamespace: String): List<T>
    fun findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): List<T>

}
