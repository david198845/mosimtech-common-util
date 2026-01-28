package de.mosimtech.common.jpa.repository.impl

import de.mosimtech.common.core.namespace.Namespace
import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.entity.AbstractEntity
import de.mosimtech.common.jpa.repository.IdentifiableRepository
import de.mosimtech.common.jpa.repository.UrnCrudRepository
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation

/**
 * Implementation of the `UrnCrudRepository` interface, extending `SimpleJpaRepository<T, String>`.
 * This class provides additional functionalities for managing entities identified by URN.
 *
 * Generic NotificationType:
 * @param T The notificationType of the entity, which must extend `AbstractEntity`.
 *
 * Constructor:
 * @param entityInformation Metadata information about the JPA entity.
 * @param entityManager JPA EntityManager used for executing database operations.
 */
open class UrnCrudRepositoryImpl<T : AbstractEntity>(
    private val entityInformation: JpaEntityInformation<T, String>,
    private val entityManager: EntityManager
) : BaseJpaRepositoryImpl<T, String>(entityInformation, entityManager),
    UrnCrudRepository<T>,
    IdentifiableRepository<T>,
    JpaRepositoryImplementation<T, String> {

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the unique identifier of the entity in the form of Urn.
     * @return an Optional containing the entity if found, or an empty Optional if no entity matches the id.
     */
    override fun findById(id: Urn): T? = super.findById(id.toUrnString()).orElse(null)

    fun findByIdAndUserId(id: Urn, userId: Urn): T? {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE :id = t.id AND :userId = t.userId"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("id", id.toUrnString())
        query.setParameter("userId", userId)
        return try {
            query.singleResult
        } catch (e: jakarta.persistence.NoResultException) {
            null
        }

    }

    fun findByIdAndUserIdAndValidTrue(id: Urn, userId: Urn): T? {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE t.id = :id AND t.userId = :userId AND t.valid = true"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("id", id.toUrnString())
        query.setParameter("userId", userId)
        return try {
            query.singleResult
        } catch (e: jakarta.persistence.NoResultException) {
            null
        }
    }

    fun findByIdAndUserIdAndValidFalse(id: Urn, userId: Urn): T? {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE t.id = :id AND t.userId = :userId AND t.valid = false"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("id", id.toUrnString())
        query.setParameter("userId", userId)
        return try {
            query.singleResult
        } catch (e: jakarta.persistence.NoResultException) {
            null
        }
    }

    fun findByIdAndUserIdAndValid(id: Urn, userId: Urn, valid: Boolean): T? {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE t.id = :id AND t.userId = :userId AND t.valid = :valid"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("id", id.toUrnString())
        query.setParameter("userId", userId.toUrnString())
        query.setParameter("valid", valid)
        return try {
            query.singleResult
        } catch (e: jakarta.persistence.NoResultException) {
            null
        }
    }

    /**
     * Retrieves all entities managed by this repository.
     *
     * @return a list of all entities, or an empty list if no entities exist.
     */
    override fun findAll(): List<T> {
        return super<BaseJpaRepositoryImpl>.findAll()
    }

    /**
     * Checks whether an entity exists with the given unique identifier.
     *
     * @param id the unique identifier of the entity in the form of Urn.
     * @return true if an entity with the given identifier exists, false otherwise.
     */
    override fun existsById(id: Urn): Boolean = super.existsById(id.toUrnString())

    /**
     * Deletes an entity identified by its unique identifier.
     *
     * @param id the unique identifier of the entity in the form of Urn.
     */
    override fun deleteById(id: Urn) = super.deleteById(id.toUrnString())

    /**
     * Finds and returns a list of entities whose namespace matches the specified value.
     *
     * The namespace is derived from the entity's ID using a splitting function
     * based on a predefined delimiter.
     *
     * @param namespace the namespace to search by, extracted from the entity's unique identifier.
     * @return a list of entities matching the given namespace, or an empty list if no matching entities are found.
     */
    override fun findByNamespace(namespace: String): List<T> {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE FUNCTION('split_part', t.id, ':', 2) = :namespace"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("namespace", namespace)
        return query.resultList
    }

    /**
     * Finds and returns a list of entities that belong to the specified namespace.
     *
     * The namespace is represented by an identifier, and entities whose namespace matches the provided
     * identifier will be retrieved. This function internally utilizes the `findBySubNamespace` method.
     *
     * @param namespace the `Namespace` object containing the identifier used to filter entities.
     * @return a list of entities matching the given namespace, or an empty list if no matching entities are found.
     */
    override fun findByNamespace(namespace: Namespace): List<T> = findByNamespace(namespace.identifier)

    /**
     * Finds and returns a list of entities whose IDs contain the specified sub-namespace.
     *
     * The method performs a search using a JPQL query that filters entities based on their IDs,
     * matching them against the given sub-namespace value.
     *
     * @param subNamespace the sub-namespace to search for within the entity's unique identifier.
     * @return a list of entities whose IDs include the specified sub-namespace, or an empty list if no matching entities are found.
     */
    override fun findBySubNamespace(subNamespace: String): List<T> {
        val jpql = "SELECT t FROM ${getEntityName()} t WHERE t.id LIKE CONCAT('%', :subNamespace, '%')"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("subNamespace", subNamespace)
        return query.resultList
    }

    /**
     * Finds and returns a list of entities whose IDs contain any of the specified sub-namespaces.
     *
     * The search is performed using a JPQL query that checks for the existence of the sub-namespaces
     * within the IDs of the entities.
     *
     * @param subNamespaces a list of sub-namespace strings used to filter the entities.
     * @return a list of entities matching the given sub-namespaces, or an empty list if no matching entities are found.
     */
    override fun findBySubNamespace(subNamespaces: List<String>): List<T> {
        val jpql =
            "SELECT t FROM ${getEntityName()} t WHERE EXISTS (SELECT 1 FROM :subNamespaces sn WHERE t.id LIKE CONCAT('%', sn, '%'))"
        val query = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("subNamespaces", subNamespaces)
        return query.resultList
    }

    /**
     * Finds and returns a list of entities whose namespace and sub-namespace match the specified values.
     *
     * The namespace is extracted from the entity's unique identifier using a splitting function,
     * while the sub-namespace is matched as a substring within the identifier.
     *
     * @param namespace the namespace to search by, extracted from the entity's unique identifier.
     * @param subNamespace the sub-namespace to search for within the entity's unique identifier.
     * @return a list of entities matching the given namespace and sub-namespace, or an empty list if no matching entities are found.
     */
    override fun findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): List<T> {
        val jpql = """
            SELECT t FROM ${getEntityName()} t 
            WHERE FUNCTION('split_part', t.id, ':', 2) = :namespace 
            AND t.id LIKE CONCAT('%', :subNamespace, '%')
        """
        val query: TypedQuery<T> = entityManager.createQuery(jpql, entityInformation.javaType)
        query.setParameter("namespace", namespace)
        query.setParameter("subNamespace", subNamespace)
        return query.resultList
    }

    /**
     * Gets the entity name for use in JPQL queries.
     *
     * @return the name of the entity managed by this repository.
     */
    private fun getEntityName(): String {
        return entityManager.metamodel.entity(entityInformation.javaType).name
    }
}

