package de.mosimtech.common.r2dbc.mapper.domain

import de.mosimtech.common.core.domain.BaseModel
import de.mosimtech.common.r2dbc.entity.AbstractBaseEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Abstract base class for mapping between domain models and R2DBC entities in a reactive manner.
 * This class provides utility methods for converting between entity and domain types.
 *
 * @param T The entity type that extends AbstractBaseEntity
 * @param D The domain model type that extends BaseModel
 */
abstract class BaseDomainMapper<T : AbstractBaseEntity, D: BaseModel> {

    /**
     * Converts an entity to its domain model representation.
     *
     * @param entity The entity to convert
     * @return The corresponding domain model
     */
    abstract fun toDomain(entity: T): D

    /**
     * Converts a domain model to its entity representation.
     *
     * @param domain The domain model to convert
     * @return The corresponding entity
     */
    abstract fun toEntity(domain: D): T

    // Reactive converters for Mono
    fun toDomainMono(entityMono: Mono<T>): Mono<D> = entityMono.map { toDomain(it) }
    fun toEntityMono(domainMono: Mono<D>): Mono<T> = domainMono.map { toEntity(it) }

    // Reactive converters for Flux
    fun toDomainFlux(entityFlux: Flux<T>): Flux<D> = entityFlux.map { toDomain(it) }
    fun toEntityFlux(domainFlux: Flux<D>): Flux<T> = domainFlux.map { toEntity(it) }

    // Collection converters
    fun toDomainList(entities: List<T>): List<D> = entities.map { toDomain(it) }
    fun toDomainList(entities: Set<T>): List<D> = entities.map { toDomain(it) }

    fun toEntityList(domains: List<D>): List<T> = domains.map { toEntity(it) }
    fun toEntityList(domains: Set<D>): List<T> = domains.map { toEntity(it) }

    // Set converters
    fun toDomainSet(entities: Set<T>): Set<D> = entities.map { toDomain(it) }.toSet()
    fun toDomainSet(entities: List<T>): Set<D> = entities.map { toDomain(it) }.toSet()

    fun toEntitySet(domains: Set<D>): Set<T> = domains.map { toEntity(it) }.toMutableSet()
    fun toEntitySet(domains: List<D>): Set<T> = domains.map { toEntity(it) }.toMutableSet()

    // MutableList converters
    fun toDomainMutableList(entities: MutableList<T>): MutableList<D> = toDomainList(entities).toMutableList()
    fun toDomainMutableList(entities: MutableSet<T>): MutableList<D> = toDomainList(entities).toMutableList()

    fun toEntityMutableList(domains: MutableList<D>): MutableList<T> = toEntityList(domains).toMutableList()
    fun toEntityMutableList(domains: MutableSet<D>): MutableList<T> = toEntityList(domains).toMutableList()

    // MutableSet converters
    fun toDomainMutableSet(entities: MutableSet<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()
    fun toDomainMutableSet(entities: MutableList<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()

    fun toEntityMutableSet(domains: MutableSet<D>): MutableSet<T> = toEntitySet(domains).toMutableSet()
    fun toEntityMutableSet(domains: MutableList<D>): MutableSet<T> = toEntitySet(domains).toMutableSet()
}
