package de.mosimtech.common.userservice.client

import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean

/**
 * Steht für die LoadBalancer-Autokonfiguration von Spring Cloud: Der Filter entsteht wie im
 * Betrieb erst in der Autokonfigurationsphase, nicht schon beim Scan. Bewusst ohne
 * `@Configuration`, damit der Scan in [ScanningConsumerConfiguration] die Klasse nicht findet.
 */
@AutoConfigureBefore(UserServiceLanguageAutoConfiguration::class)
open class FakeLoadBalancerAutoConfiguration {

    @Bean
    open fun loadBalancerExchangeFilterFunction(): ReactorLoadBalancerExchangeFilterFunction = mock()
}
