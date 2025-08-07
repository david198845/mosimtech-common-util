package de.mosimtech.common.core.domain.factory

import de.mosimtech.common.core.domain.Identifiable
import de.mosimtech.common.core.urn.Urn

interface DomainFactory<T : Identifiable> {
    fun createNew(): T
    fun fromExisting(id: Urn): T
}
