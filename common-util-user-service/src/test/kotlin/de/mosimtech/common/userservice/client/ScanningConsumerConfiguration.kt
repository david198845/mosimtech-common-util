package de.mosimtech.common.userservice.client

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

/**
 * Bildet einen Dienst nach, der mit `@SpringBootApplication(scanBasePackages = ["de.mosimtech"])`
 * die Pakete dieser Bibliothek mitscannt: derselbe Scan mit denselben Ausschlussfiltern wie
 * Spring Boot, nur auf das Paket dieses Moduls beschränkt.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = ["de.mosimtech.common.userservice"],
    excludeFilters = [
        ComponentScan.Filter(type = FilterType.CUSTOM, classes = [TypeExcludeFilter::class]),
        ComponentScan.Filter(type = FilterType.CUSTOM, classes = [AutoConfigurationExcludeFilter::class]),
    ],
)
open class ScanningConsumerConfiguration
