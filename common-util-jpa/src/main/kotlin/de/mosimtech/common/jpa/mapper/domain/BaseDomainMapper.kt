package de.mosimtech.common.jpa.mapper.domain

import de.mosimtech.common.core.domain.Identifiable
import de.mosimtech.common.jpa.entity.AbstractEntity

abstract class BaseDomainMapper<T : AbstractEntity, D : Identifiable> {

    abstract fun toDomain(entity: T): D


    abstract fun  toEntity(domain: D): T

    // List
    fun toDomainList(entities: List<T>): List<D> = entities.map { toDomain(it) }
    fun toDomainList(entities: Set<T>): List<D> = entities.map { toDomain(it) }

    fun toEntityList(domains: List<D>): List<T> = domains.map { toEntity(it) }
    fun toEntityList(domains: Set<D>): List<T> = domains.map { toEntity(it) }

    // Set
    fun toDomainSet(entities: Set<T>): Set<D> = entities.map { toDomain(it) }.toSet()
    fun toDomainSet(entities: List<T>): Set<D> = entities.map { toDomain(it) }.toSet()

    fun toEntitySet(domains: Set<D>): Set<T> = domains.map { toEntity(it) }.toMutableSet()
    fun toEntitySet(domains: List<D>): Set<T> = domains.map { toEntity(it) }.toMutableSet()

    // MutableList
    fun toDomainMutableList(entities: MutableList<T>): MutableList<D> = toDomainList(entities).toMutableList()
    fun toDomainMutableList(entities: MutableSet<T>): MutableList<D> = toDomainList(entities).toMutableList()

    fun toEntityMutableList(domains: MutableList<D>): MutableList<T> = toEntityList(domains).toMutableList()
    fun toEntityMutableList(domains: MutableSet<D>): MutableList<T> = toEntityList(domains).toMutableList()

    // MutableSet
    fun toDomainMutableSet(entities: MutableSet<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()
    fun toDomainMutableSet(entities: MutableList<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()

    fun toEntityMutableSet(domains: MutableSet<D>): MutableSet<T> = toEntitySet(domains).toMutableSet()
    fun toEntityMutableSet(domains: MutableList<D>): MutableSet<T> = toEntitySet(domains).toMutableSet()


}
