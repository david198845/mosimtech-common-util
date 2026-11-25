package de.mosimtech.common.userservice.client

import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.ConfigurableApplicationContext
import kotlin.test.Test

/**
 * Dienste scannen oft ganz `de.mosimtech` und damit auch dieses Modul. Der Scan darf die
 * Client-Konfigurationen nicht vorzeitig einrichten, sonst wird ihre LoadBalancer-Bedingung
 * geprüft, bevor es den LoadBalancer gibt, und es bleibt immer beim Client ohne Dienstauflösung.
 */
class ComponentScanningConsumerTest {

    private val runner = ApplicationContextRunner()
        .withUserConfiguration(ScanningConsumerConfiguration::class.java)

    @Test
    fun `scanning the package registers no client configuration`() {
        runner.run { context ->
            assertThat(context).doesNotHaveBean(PlainUserLocaleClientConfiguration::class.java)
            assertThat(context).doesNotHaveBean(LoadBalancedUserLocaleClientConfiguration::class.java)
            assertThat(context).doesNotHaveBean(UserLocaleClient::class.java)
        }
    }

    @Test
    fun `scanning consumer gets the load balanced client when a load balancer is configured`() {
        runner
            .withConfiguration(
                AutoConfigurations.of(
                    FakeLoadBalancerAutoConfiguration::class.java,
                    UserServiceLanguageAutoConfiguration::class.java,
                    RecipientDefaultsAutoConfiguration::class.java,
                ),
            )
            .withBean(ServiceAccessTokenSupplier::class.java, { ServiceAccessTokenSupplier { "token" } })
            .run { context ->
                assertThat(context).hasSingleBean(UserLocaleClient::class.java)
                assertThat(declaringClassOf(context, UserLocaleClient::class.java))
                    .isEqualTo(LoadBalancedUserLocaleClientConfiguration::class.java.name)
                assertThat(declaringClassOf(context, RecipientLanguageResolver::class.java))
                    .isEqualTo(UserServiceLanguageAutoConfiguration::class.java.name)
                assertThat(declaringClassOf(context, RecipientFormatResolver::class.java))
                    .isEqualTo(UserServiceLanguageAutoConfiguration::class.java.name)
            }
    }

    @Test
    fun `scanning consumer gets the plain client without a load balancer`() {
        runner
            .withConfiguration(AutoConfigurations.of(UserServiceLanguageAutoConfiguration::class.java))
            .withBean(ServiceAccessTokenSupplier::class.java, { ServiceAccessTokenSupplier { "token" } })
            .run { context ->
                assertThat(declaringClassOf(context, UserLocaleClient::class.java))
                    .isEqualTo(PlainUserLocaleClientConfiguration::class.java.name)
            }
    }

    private fun declaringClassOf(context: ConfigurableApplicationContext, type: Class<*>): String? {
        val name = context.getBeanNamesForType(type).single()
        val definition = context.beanFactory.getBeanDefinition(name) as AnnotatedBeanDefinition
        return definition.factoryMethodMetadata?.declaringClassName
    }
}
