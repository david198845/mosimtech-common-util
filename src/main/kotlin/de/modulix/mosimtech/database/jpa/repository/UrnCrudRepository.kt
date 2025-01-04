package de.modulix.mosimtech.database.jpa.repository

import de.modulix.mosimtech.database.jpa.AbstractBaseEntity
import de.modulix.mosimtech.database.namespace.Namespace
import de.modulix.mosimtech.database.urn.Urn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

/**
 * Repository interface for performing CRUD operations on entities identified by URN.
 *
 * This interface extends `JpaRepository` and provides additional methods for
 * working with entities of type `AbstractBaseEntity`, using URN as the identifier type.
 *
 * The implementation of this interface is automatically provided by Spring Data JPA, except for
 * cases where custom specifications or behaviors are defined.
 *
 * Generic Type:
 * @param T The type of the entity, which must extend `AbstractBaseEntity`.
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
interface UrnCrudRepository<T : AbstractBaseEntity> : JpaRepository<T, String> {
    fun findById(id: Urn): Optional<T>
    fun existsById(id: Urn): Boolean
    fun deleteById(id: Urn)
    fun findByNamespace(namespace: String): List<T>
    fun findByNamespace(namespace: Namespace): List<T>
    fun findBySubNamespace(subNamespace: List<String>): List<T>
    fun findBySubNamespace(subNamespace: String): List<T>
    fun findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): List<T>
}