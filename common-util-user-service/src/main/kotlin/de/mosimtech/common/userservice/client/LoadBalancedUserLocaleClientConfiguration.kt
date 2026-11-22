package de.mosimtech.common.userservice.client

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/** [UserLocaleClient] mit Dienstauflösung über Spring Cloud LoadBalancer (Consul-Discovery). */
@Configuration(proxyBeanMethods = false)
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
