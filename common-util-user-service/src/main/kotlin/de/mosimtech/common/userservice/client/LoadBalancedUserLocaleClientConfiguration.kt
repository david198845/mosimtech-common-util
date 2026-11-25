package de.mosimtech.common.userservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

/**
 * [UserLocaleClient] mit Dienstauflösung über Spring Cloud LoadBalancer (Consul-Discovery).
 *
 * Bewusst ohne `@Configuration`: Die Klasse wird nur über `@Import` aus
 * [UserServiceLanguageAutoConfiguration] eingebunden und damit erst in der
 * Autokonfigurationsphase ausgewertet. Mit `@Configuration` fände sie ein Komponentenscan über
 * `de.mosimtech` vorzeitig, und die LoadBalancer-Bedingung würde geprüft, bevor es den
 * LoadBalancer gibt.
 */
@ConditionalOnClass(name = ["org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction"])
@ConditionalOnBean(type = ["org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction"])
open class LoadBalancedUserLocaleClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun userLocaleClient(
        @Value("\${mosimtech.user-service.base-url:${UserServiceLanguageAutoConfiguration.DEFAULT_BASE_URL}}") baseUrl: String,
        loadBalancer: ReactorLoadBalancerExchangeFilterFunction,
    ): UserLocaleClient = UserLocaleClient(WebClient.builder().baseUrl(baseUrl).filter(loadBalancer).build())
}
