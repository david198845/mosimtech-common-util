package de.mosimtech.common.vault.database

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.lease.SecretLeaseContainer

/**
 * Richtet [VaultDatabaseLeaseHandler] in jedem Dienst ein, der seine Datenbank-Zugangsdaten
 * von Vault bezieht. Ein Dienst muss dafuer nichts tun, als die Bibliothek einzubinden.
 *
 * Zieht sich zurueck, solange der Dienst noch einen eigenen `vaultSecretLeaseHandler` mitbringt
 * (die frueher je Dienst kopierte Fassung). So laufen nach dem Hochziehen nicht zwei Handler auf
 * derselben Lease.
 */
@AutoConfiguration(afterName = ["org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration"])
@ConditionalOnClass(HikariDataSource::class, SecretLeaseContainer::class)
@ConditionalOnProperty(
    name = ["spring.cloud.vault.enabled", "spring.cloud.vault.database.enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
@ConditionalOnBean(HikariDataSource::class, SecretLeaseContainer::class, VaultOperations::class)
@ConditionalOnMissingBean(name = ["vaultSecretLeaseHandler"])
open class VaultDatabaseLeaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    open fun vaultDatabaseCredentialRefresher(
        eventPublisher: ApplicationEventPublisher,
        dataSource: HikariDataSource,
    ): VaultDatabaseCredentialRefresher = VaultDatabaseCredentialRefresher(eventPublisher, dataSource)

    @Bean
    @ConditionalOnMissingBean
    open fun vaultDatabaseLeaseHandler(
        @Value("\${spring.cloud.vault.database.backend:database}") databaseBackend: String,
        @Value("\${spring.cloud.vault.database.role:\${spring.application.name}}") databaseRole: String,
        credentialRefresher: VaultDatabaseCredentialRefresher,
        vaultOperations: VaultOperations,
        secretLeaseContainer: SecretLeaseContainer,
    ): VaultDatabaseLeaseHandler = VaultDatabaseLeaseHandler(
        databaseBackend = databaseBackend,
        databaseRole = databaseRole,
        credentialRefresher = credentialRefresher,
        vaultOperations = vaultOperations,
        secretLeaseContainer = secretLeaseContainer,
    )
}
