package de.mosimtech.common.jpa.repository

import de.mosimtech.common.jpa.entity.AbstractAuditableEntity
import org.springframework.data.repository.NoRepositoryBean

@NoRepositoryBean
interface AuditableRepository<T : AbstractAuditableEntity> : IdentifiableRepository<T> {

}
