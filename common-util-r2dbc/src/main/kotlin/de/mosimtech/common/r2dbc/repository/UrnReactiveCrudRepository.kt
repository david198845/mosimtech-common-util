package de.mosimtech.common.r2dbc.repository

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.r2dbc.entity.AbstractBaseEntity
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository interface for performing reactive CRUD operations on entities identified by URN.
 *
 * This interface extends `ReactiveCrudRepository` and provides additional methods for
 * working with entities of type `AbstractBaseEntity`, using URN as the identifier type.
 *
 * The implementation of this interface is automatically provided by Spring Data R2DBC, except for
 * cases where custom specifications or behaviors are defined.
 *
 * Generic Type:
 * @param T The type of the entity, which must extend `AbstractBaseEntity`.
 *
 * Methods:
 * - `findById(id: Urn): Mono<T>`: Retrieves an entity by its URN identifier.
 * - `existsById(id: Urn): Mono<Boolean>`: Checks the existence of an entity by its URN identifier.
 * - `deleteById(id: Urn): Mono<Void>`: Deletes an entity by its URN identifier.
 * - `findByNamespace(namespace: String): Flux<T>`: Finds all entities by a specific namespace.
 * - `findByNamespace(namespace: Namespace): Flux<T>`: Finds all entities by a `Namespace` object.
 * - `findBySubNamespace(subNamespace: List<String>): Flux<T>`: Finds all entities with a sub-namespace from a list of strings.
 * - `findBySubNamespace(subNamespace: String): Flux<T>`: Finds all entities by a specific sub-namespace.
 * - `findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): Flux<T>`: Finds all entities by both namespace and sub-namespace.
 *
 * Note:
 * This is a Spring Data repository interface and should be used with Spring's R2DBC repository mechanism.
 * The `@NoRepositoryBean` annotation ensures that no instance of this interface is created directly.
 */
@NoRepositoryBean
interface UrnReactiveCrudRepository<T : AbstractBaseEntity> : ReactiveCrudRepository<T, String> {
    fun findById(id: Urn): Mono<T>
    fun findByUserId(userId: Urn): Flux<T>
    fun existsById(id: Urn): Mono<Boolean>
    fun deleteById(id: Urn): Mono<Void>
    fun findByNamespace(namespace: String): Flux<T>
    fun findByNamespace(namespace: Namespace): Flux<T>
    fun findBySubNamespace(subNamespace: List<String>): Flux<T>
    fun findBySubNamespace(subNamespace: String): Flux<T>
    fun findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): Flux<T>
}
