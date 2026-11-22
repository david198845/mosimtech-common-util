package de.mosimtech.common.vault.database

import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.lease.SecretLeaseContainer

class VaultDatabaseLeaseAutoConfigurationTest {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(VaultDatabaseLeaseAutoConfiguration::class.java))
        .withPropertyValues("spring.application.name=momasoft-test-service")

    private fun ApplicationContextRunner.withVaultAndPool() = this
        .withBean(HikariDataSource::class.java, { mock<HikariDataSource>() })
        .withBean(SecretLeaseContainer::class.java, { mock<SecretLeaseContainer>() })
        .withBean(VaultOperations::class.java, { mock<VaultOperations>() })

    @Test
    fun `service with vault database credentials gets the lease handler`() {
        runner.withVaultAndPool().run { context ->
            assertThat(context).hasSingleBean(VaultDatabaseLeaseHandler::class.java)
            assertThat(context).hasSingleBean(VaultDatabaseCredentialRefresher::class.java)
        }
    }

    @Test
    fun `own lease handler of a service takes precedence`() {
        runner.withVaultAndPool()
            .withBean("vaultSecretLeaseHandler", Any::class.java, { Any() })
            .run { context -> assertThat(context).doesNotHaveBean(VaultDatabaseLeaseHandler::class.java) }
    }

    @Test
    fun `no handler when the vault database backend is switched off`() {
        runner.withVaultAndPool()
            .withPropertyValues("spring.cloud.vault.database.enabled=false")
            .run { context -> assertThat(context).doesNotHaveBean(VaultDatabaseLeaseHandler::class.java) }
    }

    @Test
    fun `no handler when vault is switched off`() {
        runner.withVaultAndPool()
            .withPropertyValues("spring.cloud.vault.enabled=false")
            .run { context -> assertThat(context).doesNotHaveBean(VaultDatabaseLeaseHandler::class.java) }
    }

    @Test
    fun `no handler without a lease container`() {
        runner.withBean(HikariDataSource::class.java, { mock<HikariDataSource>() })
            .run { context -> assertThat(context).doesNotHaveBean(VaultDatabaseLeaseHandler::class.java) }
    }
}
