package de.mosimtech.common.userservice.client

import de.mosimtech.common.core.i18n.AccountLanguage
import de.mosimtech.common.core.i18n.FormatPreferences
import de.mosimtech.common.core.urn.Urn
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class RecipientDefaultsAutoConfigurationTest {
    private val userId = requireNotNull(Urn.of("urn:user:momasoft:person"))
    private val runner = ApplicationContextRunner().withConfiguration(
        AutoConfigurations.of(UserServiceLanguageAutoConfiguration::class.java, RecipientDefaultsAutoConfiguration::class.java),
    )

    @Test
    fun `provides offline resolvers without a service token supplier`() {
        runner.run { context ->
            assertThat(context).hasSingleBean(RecipientFormatResolver::class.java)
            assertThat(context).hasSingleBean(RecipientLanguageResolver::class.java)
            assertThat(context).doesNotHaveBean(UserLocaleClient::class.java)
            assertEquals(FormatPreferences(), context.getBean(RecipientFormatResolver::class.java).resolve(userId))
            assertEquals(AccountLanguage.DEFAULT, context.getBean(RecipientLanguageResolver::class.java).resolve(userId))
        }
    }

    @Test
    fun `steps back when the user service resolvers are configured`() {
        runner.withBean(ServiceAccessTokenSupplier::class.java, { ServiceAccessTokenSupplier { "token" } })
            .run { context ->
                assertThat(context).hasSingleBean(RecipientFormatResolver::class.java)
                assertThat(context).hasSingleBean(UserLocaleClient::class.java)
            }
    }

    @Test
    fun `steps back for own beans and can be switched off`() {
        val own = RecipientFormatResolver.defaultsOnly()
        runner.withBean(RecipientFormatResolver::class.java, { own }).run { context ->
            assertThat(context.getBean(RecipientFormatResolver::class.java)).isSameAs(own)
        }
        runner.withPropertyValues("mosimtech.user-service.defaults-fallback.enabled=false").run { context ->
            assertThat(context).doesNotHaveBean(RecipientFormatResolver::class.java)
            assertThat(context).doesNotHaveBean(RecipientLanguageResolver::class.java)
        }
    }

    @Test
    fun `defaults only resolvers never call out`() {
        val formats = RecipientFormatResolver.defaultsOnly()
        assertEquals(FormatPreferences(), formats.resolve(userId))
        assertEquals(AccountLanguage.DEFAULT, RecipientLanguageResolver.defaultsOnly().resolve(userId))
        assertNotSame(formats, RecipientFormatResolver.defaultsOnly())
    }

    @Test
    fun `offline client knows no profile`() {
        assertEquals(null, UserLocaleClient.offline().findLocale(userId, "").block())
    }
}
