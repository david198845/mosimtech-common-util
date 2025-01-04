package de.modulix.mosimtech.database.jpa.repository

import de.modulix.mosimtech.database.jpa.AbstractBaseEntity
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository

@NoRepositoryBean
interface UrnPagingAndSortingRepository<T : AbstractBaseEntity> : PagingAndSortingRepository<T, String> {

}