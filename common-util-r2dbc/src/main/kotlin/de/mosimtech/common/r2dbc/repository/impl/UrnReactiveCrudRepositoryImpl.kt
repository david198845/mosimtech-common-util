package de.mosimtech.common.r2dbc.repository.impl

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.r2dbc.entity.AbstractBaseEntity
import de.mosimtech.common.r2dbc.repository.UrnReactiveCrudRepository
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.support.SimpleR2dbcRepository
import org.springframework.data.relational.repository.support.MappingRelationalEntityInformation
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Implementation of the `UrnReactiveCrudRepository` interface, extending `SimpleR2dbcRepository<T, String>`.
 * This class provides additional functionalities for managing entities identified by URN in a reactive manner.
 *
 * Generic Type:
 * @param T The type of the entity, which must extend `AbstractBaseEntity`.
 *
 * Constructor:
 * @param entity Metadata information about the R2DBC entity.
 * @param entityTemplate R2DBC EntityTemplate used for executing database operations.
 */
open class UrnReactiveCrudRepositoryImpl<T : AbstractBaseEntity>(
    entityInformation: MappingRelationalEntityInformation<T, String>,
    entityOperations: R2dbcEntityOperations,
    converter: R2dbcConverter,
    private val entityTemplate: R2dbcEntityTemplate
) : SimpleR2dbcRepository<T, String>(entityInformation, entityOperations, converter),
    UrnReactiveCrudRepository<T> {

    private val entityClass = entityInformation.javaType

    override fun findById(urn: Urn): Mono<T> {
        return super.findById(urn.toUrnString())
    }

    override fun existsById(urn: Urn): Mono<Boolean> {
        return super.existsById(urn.toUrnString())
    }

    override fun deleteById(urn: Urn): Mono<Void> {
        return super.deleteById(urn.toUrnString())
    }

    override fun findByNamespace(namespace: String): Flux<T> {
        val sql = """
            SELECT * FROM ${getTableName()} 
            WHERE split_part(id, ':', 2) = ?
        """.trimIndent()

        return entityTemplate.databaseClient
            .sql(sql)
            .bind(0, namespace)
            .map { row, _ -> entityTemplate.converter.read(entityClass, row) }
            .all()
    }

    override fun findByNamespace(namespace: Namespace): Flux<T> {
        return findBySubNamespace(namespace.identifier)
    }

    override fun findBySubNamespace(subNamespace: String): Flux<T> {
        val sql = """
            SELECT * FROM ${getTableName()} 
            WHERE id LIKE ?
        """.trimIndent()

        return entityTemplate.databaseClient
            .sql(sql)
            .bind(0, "%$subNamespace%")
            .map { row, _ -> entityTemplate.converter.read(entityClass, row) }
            .all()
    }

    override fun findBySubNamespace(subNamespaces: List<String>): Flux<T> {
        if (subNamespaces.isEmpty()) {
            return Flux.empty()
        }

        val placeholders = subNamespaces.indices.joinToString(",") { "?" }
        val sql = """
            SELECT * FROM ${getTableName()} 
            WHERE EXISTS (
                SELECT 1 FROM unnest(ARRAY[$placeholders]) AS sn(value) 
                WHERE id LIKE '%' || sn.value || '%'
            )
        """.trimIndent()

        var bindSpec = entityTemplate.databaseClient.sql(sql)
        subNamespaces.forEachIndexed { index, subNamespace ->
            bindSpec = bindSpec.bind(index, subNamespace)
        }

        return bindSpec
            .map { row, _ -> entityTemplate.converter.read(entityClass, row) }
            .all()
    }

    override fun findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): Flux<T> {
        val sql = """
            SELECT * FROM ${getTableName()} 
            WHERE split_part(id, ':', 2) = ? 
            AND id LIKE ?
        """.trimIndent()

        return entityTemplate.databaseClient
            .sql(sql)
            .bind(0, namespace)
            .bind(1, "%$subNamespace%")
            .map { row, _ -> entityTemplate.converter.read(entityClass, row) }
            .all()
    }

    override fun findByUserId(userId: Urn): Flux<T> {
        val sql = """
            SELECT * FROM ${getTableName()} 
            WHERE user_id = ?
        """.trimIndent()

        return entityTemplate.databaseClient
            .sql(sql)
            .bind(0, userId.toUrnString())
            .map { row, _ -> entityTemplate.converter.read(entityClass, row) }
            .all()
    }


    private fun getTableName(): String {
        // Vereinfachte Implementierung - könnte erweitert werden
        return entityClass.simpleName.lowercase().replace("entity", "")
    }
}
