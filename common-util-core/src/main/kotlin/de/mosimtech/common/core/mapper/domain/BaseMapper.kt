package de.mosimtech.common.core.mapper.domain

import de.mosimtech.common.core.domain.BaseModel


/**
 * BaseMapper is an abstract class that serves as a generic template for mapping between
 * domain models and data transfer objects (DTOs). This class provides the contract and
 * default implementations for performing conversions between these two types, including
 * individual objects, collections, and mutable collections.
 *
 * The generic parameters are:
 * - [D]: Type of the domain model, which must extend [BaseModel].
 * - [T]: Type of the data transfer object (DTO).
 */
abstract class BaseMapper<D : BaseModel, T> {

    /**
     * Converts a given data transfer object (DTO) of type [T] into its corresponding domain model of type [D].
     *
     * @param dto The data transfer object to be converted.
     * @return The corresponding domain model representation of the given DTO.
     */
    abstract fun toDomain(dto: T): D

    /**
     * Converts a given domain model of type [D] into its corresponding data transfer object (DTO) of type [T].
     *
     * @param domain The domain model to be converted.
     * @return The corresponding data transfer object representation of the given domain model.
     */
    abstract fun toDto(domain: D): T

    /**
     * Converts a list of data transfer objects (DTOs) of type [T] into their corresponding domain models of type [D].
     *
     * @param entities The list of data transfer objects to be converted.
     * @return A list of domain models corresponding to the given list of data transfer objects.
     */
// List
    fun toDomainList(entities: List<T>): List<D> = entities.map { toDomain(it) }

    /**
     * Converts a set of data transfer objects (DTOs) of type [T] into their corresponding domain models of type [D].
     *
     * @param entities The set of data transfer objects to be converted.
     * @return A list of domain models corresponding to the given set of data transfer objects.
     */
    fun toDomainList(entities: Set<T>): List<D> = entities.map { toDomain(it) }

    /**
     * Converts a list of domain models of type [D] into their corresponding
     * data transfer object (DTO) representation of type [T].
     *
     * @param domains The list of domain models to be converted.
     * @return A list of data transfer objects corresponding to the given domain models.
     */
    fun toDtoList(domains: List<D>): List<T> = domains.map { toDto(it) }

    /**
     * Converts a set of domain models of type [D] into their corresponding
     * data transfer object (DTO) representation of type [T].
     *
     * @param domains The set of domain models to be converted.
     * @return A list of data transfer objects corresponding to the given domain models.
     */
    fun toDtoList(domains: Set<D>): List<T> = domains.map { toDto(it) }

    /**
     * Converts a set of data transfer objects (DTOs) of type [T] into their corresponding domain models of type [D].
     *
     * @param entities The set of data transfer objects to be converted.
     * @return A set of domain models corresponding to the given set of data transfer objects.
     */
// Set
    fun toDomainSet(entities: Set<T>): Set<D> = entities.map { toDomain(it) }.toSet()

    /**
     * Converts a list of data transfer objects (DTOs) of type [T] into their corresponding domain models of type [D].
     *
     * @param entities The list of data transfer objects to be converted.
     * @return A set of domain models corresponding to the given list of data transfer objects.
     */
    fun toDomainSet(entities: List<T>): Set<D> = entities.map { toDomain(it) }.toSet()

    /**
     * Converts a set of domain models of type [D] into their corresponding
     * data transfer object (DTO) representation of type [T].
     *
     * @param domains The set of domain models to be converted.
     * @return A set of data transfer objects corresponding to the given domain models.
     */
    fun toDtoSet(domains: Set<D>): Set<T> = domains.map { toDto(it) }.toMutableSet()

    /**
     * Converts a list of domain models of type [D] into a set of their corresponding
     * data transfer object (DTO) representations of type [T].
     *
     * @param domains The list of domain models to be converted.
     * @return A set of data transfer objects corresponding to the given list of domain models.
     */
    fun toDtoSet(domains: List<D>): Set<T> = domains.map { toDto(it) }.toMutableSet()

    /**
     * Converts a mutable list of data transfer objects (DTOs) of type [T] into a mutable list
     * of their corresponding domain models of type [D].
     *
     * @param entities The mutable list of data transfer objects to be converted.
     * @return A mutable list of domain models corresponding to the given mutable list of data transfer objects.
     */
    fun toDomainMutableList(entities: MutableList<T>): MutableList<D> = toDomainList(entities).toMutableList()

    /**
     * Converts a mutable set of data transfer objects (DTOs) of type [T] into a mutable list
     * of their corresponding domain models of type [D].
     *
     * @param entities The mutable set of data transfer objects to be converted.
     * @return A mutable list of domain models corresponding to the given mutable set of data transfer objects.
     */
    fun toDomainMutableList(entities: MutableSet<T>): MutableList<D> = toDomainList(entities).toMutableList()

    /**
     * Converts a mutable list of domain models of type [D] into a mutable list
     * of their corresponding data transfer object (DTO) representations of type [T].
     *
     * @param domains The mutable list of domain models to be converted.
     * @return A mutable list of data transfer objects corresponding to the given domain models.
     */
    fun toDtoMutableList(domains: MutableList<D>): MutableList<T> = toDtoList(domains).toMutableList()

    /**
     * Converts a mutable set of domain models of type [D] into a mutable list
     * of their corresponding data transfer object (DTO) representations of type [T].
     *
     * @param domains The mutable set of domain models to be converted.
     * @return A mutable list of data transfer objects corresponding to the given domain models.
     */
    fun toDtoMutableList(domains: MutableSet<D>): MutableList<T> = toDtoList(domains).toMutableList()

    /**
     * Converts a mutable set of data transfer objects (DTOs) of type [T] into a mutable set
     * of their corresponding domain models of type [D].
     *
     * @param entities The mutable set of data transfer objects to be converted.
     * @return A mutable set of domain models corresponding to the given mutable set of data transfer objects.
     */
    fun toDomainMutableSet(entities: MutableSet<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()

    /**
     * Converts a mutable list of data transfer objects (DTOs) of type [T] into a mutable set
     * of their corresponding domain models of type [D].
     *
     * @param entities The mutable list of data transfer objects to be converted.
     * @return A mutable set of domain models corresponding to the given mutable list of data transfer objects.
     */
    fun toDomainMutableSet(entities: MutableList<T>): MutableSet<D> = toDomainSet(entities).toMutableSet()

    /**
     * Converts a mutable set of domain models of type [D] into a mutable set of their corresponding
     * data transfer object (DTO) representations of type [T].
     *
     * @param domains The mutable set of domain models to be converted.
     * @return A mutable set of data transfer objects corresponding to the given mutable set of domain models.
     */
    fun toDtoMutableSet(domains: MutableSet<D>): MutableSet<T> = toDtoSet(domains).toMutableSet()

    /**
     * Converts a mutable list of domain models of type [D] into a mutable set of their corresponding
     * data transfer object (DTO) representations of type [T].
     *
     * @param domains The mutable list of domain models to be converted.
     * @return A mutable set of data transfer objects corresponding to the given list of domain models.
     */
    fun toDtoMutableSet(domains: MutableList<D>): MutableSet<T> = toDtoSet(domains).toMutableSet()


}
