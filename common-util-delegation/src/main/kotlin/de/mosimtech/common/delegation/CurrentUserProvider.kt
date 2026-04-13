package de.mosimtech.common.delegation

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.security.SecurityContextAdapter
import org.springframework.stereotype.Component

@Component
class CurrentUserProvider {

    fun getEffectiveUserId(): Urn =
        DelegationContextHolder.get()
            ?.let { Urn.parse(it) }
            ?: SecurityContextAdapter.getCurrentUserID()
            ?: throw IllegalStateException("No authenticated user found in SecurityContext")
}
