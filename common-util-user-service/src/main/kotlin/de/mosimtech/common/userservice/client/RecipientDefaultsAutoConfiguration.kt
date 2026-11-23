package de.mosimtech.common.userservice.client

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Ersatz, wenn [UserServiceLanguageAutoConfiguration] nichts eingerichtet hat, weil der Dienst
 * kein [ServiceAccessTokenSupplier] bereitstellt (etwa weil sein Dienstkonto abgeschaltet ist):
 * [RecipientLanguageResolver] und [RecipientFormatResolver] gibt es dann trotzdem, sie fragen nur
 * nie den User Service und liefern die Vorgaben (Deutsch, alles `AUTO`). Aufrufer können die
 * Resolver damit direkt einspritzen, ohne `ObjectProvider` und eigenen Ersatzweg.
 *
 * Eigene Beans des Dienstes gehen vor. Abschaltbar mit
 * `mosimtech.user-service.defaults-fallback.enabled=false`.
 */
@AutoConfiguration(after = [UserServiceLanguageAutoConfiguration::class])
@ConditionalOnProperty(prefix = "mosimtech.user-service.defaults-fallback", name = ["enabled"], matchIfMissing = true)
open class RecipientDefaultsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun recipientLanguageResolver(): RecipientLanguageResolver = RecipientLanguageResolver.defaultsOnly()

    @Bean
    @ConditionalOnMissingBean
    open fun recipientFormatResolver(): RecipientFormatResolver = RecipientFormatResolver.defaultsOnly()
}
