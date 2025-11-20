package de.mosimtech.common.r2dbc.config

import de.mosimtech.common.core.urn.Urn
import de.mosimtech.common.core.util.toUrn
import de.mosimtech.common.r2dbc.listener.UrnEntityListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.ReactiveAuditorAware
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

/**
 * Configuration class for R2DBC-specific beans and callbacks.
 * This class registers the necessary components for reactive database operations.
 */
@Configuration
open class R2dbcConfiguration {

    /**
     * Creates a UrnEntityListener bean to handle URN generation for entities.
     * This listener is registered as a BeforeConvertCallback to automatically generate 
     * URN identifiers for entities before they are persisted.
     *
     * @return A UrnEntityListener instance that implements BeforeConvertCallback
     */
    @Bean
    open fun urnEntityListener(): BeforeConvertCallback<*> {
        return UrnEntityListener()
    }


    @Bean
    open fun auditorAware(): ReactiveAuditorAware<Urn> {
        return ReactiveAuditorAware {
            // Aktueller User aus SecurityContext
            ReactiveSecurityContextHolder.getContext()
                .map { it.authentication?.principal as UserDetails }
                .map { it.username.toUrn()!! }
        }
    }
}
