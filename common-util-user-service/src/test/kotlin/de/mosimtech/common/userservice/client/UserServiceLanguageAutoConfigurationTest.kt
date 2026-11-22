package de.mosimtech.common.userservice.client

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test

class UserServiceLanguageAutoConfigurationTest {
    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(UserServiceLanguageAutoConfiguration::class.java))

    @Test
    fun `stays away without a service token supplier`() {
        runner.run { context -> assertThat(context).doesNotHaveBean(RecipientLanguageResolver::class.java) }
    }

    @Test
    fun `creates the resolver when the service provides its token`() {
        runner.withBean(ServiceAccessTokenSupplier::class.java, { ServiceAccessTokenSupplier { "token" } })
            .run { context ->
                assertThat(context).hasSingleBean(RecipientLanguageResolver::class.java)
                assertThat(context).hasSingleBean(UserLocaleClient::class.java)
            }
    }
}
