package de.mosimtech.common.userservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

/**
 * [UserLocaleClient] ohne LoadBalancer. Greift nur, wenn [LoadBalancedUserLocaleClientConfiguration]
 * keinen Client angelegt hat; `mosimtech.user-service.base-url` muss dann eine erreichbare
 * Adresse sein.
 *
 * Bewusst ohne `@Configuration`: Die Klasse wird nur über `@Import` aus
 * [UserServiceLanguageAutoConfiguration] eingebunden, nach der LoadBalancer-Variante. Ein
 * Komponentenscan über `de.mosimtech` darf sie nicht finden, sonst legt sie ihren Client an,
 * bevor die LoadBalancer-Variante überhaupt geprüft wird.
 */
open class PlainUserLocaleClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun userLocaleClient(
        @Value("\${mosimtech.user-service.base-url:${UserServiceLanguageAutoConfiguration.DEFAULT_BASE_URL}}") baseUrl: String,
    ): UserLocaleClient = UserLocaleClient(WebClient.builder().baseUrl(baseUrl).build())
}
