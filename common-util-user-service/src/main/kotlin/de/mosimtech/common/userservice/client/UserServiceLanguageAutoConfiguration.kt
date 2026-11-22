package de.mosimtech.common.userservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import java.time.Duration

/**
 * Richtet [UserLocaleClient], [RecipientLanguageResolver] und [RecipientFormatResolver] ein,
 * sobald der Dienst eine [ServiceAccessTokenSupplier]-Bean bereitstellt.
 *
 * Eigenschaften (alle optional):
 * - `mosimtech.user-service.base-url` — Vorgabe `https://mosimtech-user-service/api`; mit Spring
 *   Cloud LoadBalancer wird der Name über Consul-Discovery aufgelöst
 * - `mosimtech.user-service.language-cache-minutes` — Vorgabe 10
 * - `mosimtech.user-service.format-cache-minutes` — Vorgabe wie `language-cache-minutes`
 */
@AutoConfiguration(afterName = ["org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerClientAutoConfiguration"])
@ConditionalOnBean(ServiceAccessTokenSupplier::class)
@Import(LoadBalancedUserLocaleClientConfiguration::class, PlainUserLocaleClientConfiguration::class)
open class UserServiceLanguageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun recipientLanguageResolver(
        client: UserLocaleClient,
        tokens: ServiceAccessTokenSupplier,
        @Value("\${mosimtech.user-service.language-cache-minutes:10}") cacheMinutes: Long,
    ): RecipientLanguageResolver = RecipientLanguageResolver(client, tokens, Duration.ofMinutes(cacheMinutes))

    @Bean
    @ConditionalOnMissingBean
    open fun recipientFormatResolver(
        client: UserLocaleClient,
        tokens: ServiceAccessTokenSupplier,
        @Value("\${mosimtech.user-service.format-cache-minutes:\${mosimtech.user-service.language-cache-minutes:10}}")
        cacheMinutes: Long,
    ): RecipientFormatResolver = RecipientFormatResolver(client, tokens, Duration.ofMinutes(cacheMinutes))

    companion object {
        const val DEFAULT_BASE_URL = "https://mosimtech-user-service/api"
    }
}
