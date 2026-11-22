package de.mosimtech.common.vault.database

import com.zaxxer.hikari.HikariDataSource
import de.mosimtech.common.core.dto.vault.CredentialDTO
import de.mosimtech.common.core.dto.vault.VaultCredentialsRefreshedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher

/**
 * Setzt neue Vault-Datenbank-Zugangsdaten in den HikariCP-Pool und mustert die bestehenden
 * Verbindungen aus.
 *
 * Bewusst ohne `@Transactional`: Der Aufruf kommt oft erst, wenn Vault die alten Zugangsdaten
 * schon widerrufen hat. Eine Transaktion holte vorher eine Verbindung aus genau diesem Pool,
 * scheiterte daran, und die neuen Zugangsdaten kaemen nie an.
 */
class VaultDatabaseCredentialRefresher(
    private val eventPublisher: ApplicationEventPublisher,
    private val dataSource: HikariDataSource,
) {
    private val logger = LoggerFactory.getLogger(VaultDatabaseCredentialRefresher::class.java)

    fun refreshCredentials(credential: CredentialDTO) {
        try {
            dataSource.hikariConfigMXBean.setUsername(credential.username)
            dataSource.hikariConfigMXBean.setPassword(credential.password)
            dataSource.hikariPoolMXBean.softEvictConnections()
            logger.info("Database credentials updated, user: {}", credential.username)
        } catch (e: Exception) {
            logger.error("Error refreshing database credentials: {}", e.message, e)
        }
        eventPublisher.publishEvent(VaultCredentialsRefreshedEvent(this))
    }
}
