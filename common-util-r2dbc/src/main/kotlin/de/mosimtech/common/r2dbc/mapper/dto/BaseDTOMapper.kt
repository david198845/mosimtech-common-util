package de.mosimtech.common.r2dbc.mapper.dto

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.r2dbc.entity.AbstractBaseEntity
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

abstract class BaseDTOMapper<T : AbstractBaseEntity, D, U> {

    abstract fun toDto(entity: T): D


    abstract fun toEntity(dto: D, entity: Any? = null): T

    abstract fun updateEntity(entity: T, dto: U): T


    // List
    fun toDtoList(entities: List<T>): List<D> = entities.map { toDto(it) }
    fun toDtoList(entities: Set<T>): List<D> = entities.map { toDto(it) }

    fun toEntityList(dtos: List<D>): List<T> = dtos.map { toEntity(it) }
    fun toEntityList(dtos: Set<D>): List<T> = dtos.map { toEntity(it) }

    // Set
    fun toDtoSet(entities: Set<T>): Set<D> = entities.map { toDto(it) }.toSet()
    fun toDtoSet(entities: List<T>): Set<D> = entities.map { toDto(it) }.toSet()

    fun toEntitySet(dtos: Set<D>): Set<T> = dtos.map { toEntity(it, null) }.toMutableSet()
    fun toEntitySet(dtos: List<D>): Set<T> = dtos.map { toEntity(it, null) }.toMutableSet()

    // MutableList
    fun toDtoMutableList(entities: MutableList<T>): MutableList<D> = toDtoList(entities).toMutableList()
    fun toDtoMutableList(entities: MutableSet<T>): MutableList<D> = toDtoList(entities).toMutableList()

    fun toEntityMutableList(dtos: MutableList<D>): MutableList<T> = toEntityList(dtos).toMutableList()
    fun toEntityMutableList(dtos: MutableSet<D>): MutableList<T> = toEntityList(dtos).toMutableList()

    // MutableSet
    fun toDtoMutableSet(entities: MutableSet<T>): MutableSet<D> = toDtoSet(entities).toMutableSet()
    fun toDtoMutableSet(entities: MutableList<T>): MutableSet<D> = toDtoSet(entities).toMutableSet()

    fun toEntityMutableSet(dtos: MutableSet<D>): MutableSet<T> = toEntitySet(dtos).toMutableSet()
    fun toEntityMutableSet(dtos: MutableList<D>): MutableSet<T> = toEntitySet(dtos).toMutableSet()


    protected fun urnToString(urn: Urn?): String? = urn?.toString()

    protected fun stringToUrn(value: String?): Urn? = value?.let { Urn.parse(it) }

    protected fun offsetToZoned(ts: OffsetDateTime?): ZonedDateTime? =
        ts?.toZonedDateTime()             // oder deine Default-Zone

    protected fun zonedToOffset(ts: ZonedDateTime?): OffsetDateTime? =
        ts?.toOffsetDateTime()

    protected fun localDateTimeToOffset(ts: LocalDateTime?, zoneOffset: ZoneOffset): OffsetDateTime? =
        ts?.atOffset(zoneOffset)

    protected fun offsetToLocalDateTime(ts: OffsetDateTime?): LocalDateTime? =
        ts?.toLocalDateTime()
}
