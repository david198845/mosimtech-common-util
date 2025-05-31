package de.mosimtech.common.jpa.repository

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.entity.AbstractBaseEntity
import io.hypersistence.utils.spring.repository.BaseJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * Repository interface for performing CRUD operations on entities identified by URN.
 *
 * This interface extends `JpaRepository` and provides additional methods for
 * working with entities of notificationType `AbstractBaseEntity`, using URN as the identifier notificationType.
 *
 * The implementation of this interface is automatically provided by Spring Data JPA, except for
 * cases where custom specifications or behaviors are defined.
 *
 * Generic NotificationType:
 * @param T The notificationType of the entity, which must extend `AbstractBaseEntity`.
 *
 * Methods:
 * - `findById(id: Urn): Optional<T>`: Retrieves an entity by its URN identifier.
 * - `existsById(id: Urn): Boolean`: Checks the existence of an entity by its URN identifier.
 * - `deleteById(id: Urn)`: Deletes an entity by its URN identifier.
 * - `findByNamespace(namespace: String): List<T>`: Finds all entities by a specific namespace.
 * - `findByNamespace(namespace: Namespace): List<T>`: Finds all entities by a `Namespace` object.
 * - `findBySubNamespace(subNamespace: List<String>): List<T>`: Finds all entities with a sub-namespace from a list of strings.
 * - `findBySubNamespace(subNamespace: String): List<T>`: Finds all entities by a specific sub-namespace.
 * - `findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): List<T>`: Finds all entities by both namespace and sub-namespace.
 *
 * Note:
 * This is a Spring Data repository interface and should be used with Spring's JPA repository mechanism.
 * The `@NoRepositoryBean` annotation ensures that no instance of this interface is created directly.
 */
@NoRepositoryBean
interface UrnCrudRepository<T : AbstractBaseEntity> : BaseJpaRepository<T, String> {
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
