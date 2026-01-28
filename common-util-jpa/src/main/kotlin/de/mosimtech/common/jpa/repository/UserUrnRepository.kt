package de.mosimtech.common.jpa.repository

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.jpa.entity.AbstractBaseEntity
import org.springframework.data.repository.NoRepositoryBean

/**
 * Repository interface for URN entities that are scoped by a user.
 *
 * This interface is intended for entities that expose a `userId` field, such as
 * `AbstractBaseEntity`.
 */
@NoRepositoryBean
interface UserUrnRepository<T : AbstractBaseEntity> : UrnCrudRepository<T> {
    fun findByIdAndUserId(id: Urn, userId: Urn): T?
    fun findByIdAndUserIdAndValidTrue(id: Urn, userId: Urn): T?
    fun findByIdAndUserIdAndValidFalse(id: Urn, userId: Urn): T?
    fun findByIdAndUserIdAndValid(id: Urn, userId: Urn, valid: Boolean): T?
}
