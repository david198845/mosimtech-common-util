package de.modulix.mosimtech.database.jpa.repository.impl

import de.modulix.mosimtech.database.jpa.AbstractBaseEntity
import de.modulix.mosimtech.database.jpa.repository.UrnCrudRepository
import de.modulix.mosimtech.database.namespace.Namespace
import de.modulix.mosimtech.database.urn.Urn
import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl
import jakarta.persistence.EntityManager
import jakarta.persistence.TypedQuery
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*


/**
 * Implementation of the `UrnCrudRepository` interface, extending `SimpleJpaRepository<T, String>`.
 * This class provides additional functionalities for managing entities identified by URN.
 *
 * Generic Type:
 * @param T The type of the entity, which must extend `AbstractBaseEntity`.
 *
 * Constructor:
 * @param entityInformation Metadata information about the JPA entity.
 * @param entityManager JPA EntityManager used for executing database operations.
 *
 * Methods:
 * - `findById(id: Urn): Optional<T>`: Retrieves an entity by its URN identifier.
 * - `existsById(id: Urn): Boolean`: Checks the existence of an entity by its URN identifier.
 * - `deleteById(id: Urn)`: Deletes an entity by its URN identifier.
 * - `findByNamespace(namespace: String): List<T>`: Finds all entities by a specific namespace, extracted from the URN.
 * - `findByNamespace(namespace: Namespace): List<T>`: Finds all entities by a `Namespace` object.
 * - `findBySubNamespace(subNamespace: String): List<T>`: Finds all entities by matching the given sub-namespace in their URN.
 * - `findBySubNamespace(subNamespaces: List<String>): List<T>`: Finds all entities that match any sub-namespace from the given list of strings.
 * - `findByNamespaceAndSubNamespace(namespace: String, subNamespace: String): List<T>`: Finds all entities by both namespace and sub-namespace.
 *
 * Internal Methods:
 * - `getEntityName()`: Retrieves the name of the entity type managed by the repository.
 * - `getEntityClass()`: Retrieves the Java class of the entity type.
 *
 * Notes:
 * This implementation uses custom JPQL queries to perform operations based on URN structure
 * and string manipulation functions, such as `split_part` and `LIKE`.
 * It extends Spring Data's capabilities to handle domain-specific entity identifiers.
 */
open class UrnCrudRepositoryImpl<T : AbstractBaseEntity>(
    entityInformation: JpaEntityInformation<T, String>,
    private val entityManager: EntityManager
) : BaseJpaRepositoryImpl<T, String>(entityInformation, entityManager),
    UrnCrudRepository<T>{

    /**
     * Finds an entity by its unique identifier.
     *
     * @param id the unique identifier of the entity in the form of Urn.
     * @return an Optional containing the entity if found, or an empty Optional if no entity matches the id.
     */
    override fun findById(id: Urn): Optional<T> = super.findById(id.toUrnString())

    override fun findAll(): List<T> {
        val jpql = "SELECT t FROM ${getEntityName()} t"
        val query = entityManager.createQuery(jpql, getEntityClass())
        return query.resultList
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
        val query = entityManager.createQuery(jpql, getEntityClass())
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
    override fun findByNamespace(namespace: Namespace): List<T> = findBySubNamespace(namespace.identifier)

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
        val query = entityManager.createQuery(jpql, getEntityClass())
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
        val query = entityManager.createQuery(jpql, getEntityClass())
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
        val query: TypedQuery<T> = entityManager.createQuery(jpql, getEntityClass())
        query.setParameter("namespace", namespace)
        query.setParameter("subNamespace", subNamespace)
        return query.resultList
    }

    private fun getEntityName(): String =
        entityManager.metamodel.entity(getEntityClass()).name

    @Suppress("UNCHECKED_CAST")
    private fun getEntityClass(): Class<T> =
        AbstractBaseEntity::class.java as Class<T>

}