package de.modulix.mosimtech.database.jpa.repository.impl

import de.modulix.mosimtech.database.jpa.AbstractBaseEntity
import de.modulix.mosimtech.database.jpa.repository.UrnPagingAndSortingRepository
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class UrnPagingAndSortingRepositoryImpl<T : AbstractBaseEntity> : UrnPagingAndSortingRepository<T> {

    private val em: EntityManager
    private val domainClass: Class<T>

    constructor(em: EntityManager, domainClass: Class<T>) {
        this.em = em
        this.domainClass = domainClass
    }

    override fun findAll(sort: Sort): List<T> {
        var query = "SELECT e FROM ${domainClass.simpleName} e"
        val orderBy = buildOrderBy(sort)

        if (orderBy.isNotEmpty()) {
            query += " ORDER BY $orderBy"
        }

        return em.createQuery(query, domainClass).resultList
    }

    override fun findAll(pageable: Pageable): Page<T> {
        var query = "SELECT e FROM ${domainClass.simpleName} e"
        val orderBy = buildOrderBy(pageable.sort)

        if (orderBy.isNotEmpty()) {
            query += " ORDER BY $orderBy"
        }

        val typedQuery = em.createQuery(query, domainClass)
        typedQuery.firstResult = pageable.offset.toInt()
        typedQuery.maxResults = pageable.pageSize

        return PageImpl(typedQuery.resultList, pageable, count())
    }

    private fun buildOrderBy(sort: Sort?): String {
        if (sort == null || sort.isUnsorted) {
            return ""
        }

        val orders = sort.map { "${it.property} ${it.direction.name}" }
        return orders.joinToString(", ")
    }

    private fun count(): Long {
        val query = "SELECT COUNT(e) FROM ${domainClass.simpleName} e"
        return em.createQuery(query, Long::class.java).singleResult
    }

}