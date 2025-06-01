package de.mosimtech.common.core.aware

import de.mosimtech.common.core.namespace.UserNamespace
import de.mosimtech.common.core.urn.Urn
import org.springframework.data.domain.AuditorAware
import java.util.*

/**
 * Implementation of AuditorAware interface to provide current auditor information for Spring Security.
 *
 * This class is a Spring component that aids in capturing the user performing an action, typically for use in audit logging.
 * It customizes the retrieval of auditor information.
 *
 * In this case, the current auditor is always set to a default system identifier.
 */
open class SpringSecurityAuditorAware : AuditorAware<Urn> {

    /**
     * Retrieves the current auditor's identifier.
     *
     * This method provides a consistent identifier for auditing purposes,
     * returning a default system identifier when invoked.
     *
     * @return an Optional containing the Urn representing the system auditor.
     */
    override fun getCurrentAuditor(): Optional<Urn> {
        return Optional.of(Urn(UserNamespace, "SYSTEM"))
    }
}
