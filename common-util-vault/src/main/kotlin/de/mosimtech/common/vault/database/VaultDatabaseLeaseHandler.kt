package de.mosimtech.common.vault.database

import de.mosimtech.common.core.dto.vault.CredentialDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.lease.SecretLeaseContainer
import org.springframework.vault.core.lease.domain.RequestedSecret
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent
import org.springframework.vault.core.lease.event.SecretLeaseErrorEvent
import org.springframework.vault.core.lease.event.SecretLeaseEvent
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent
import org.springframework.vault.core.lease.event.SecretLeaseRotatedEvent
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

/**
 * Haelt den Verbindungspool mit den Vault-Datenbank-Zugangsdaten in Gleichlauf.
 *
 * Spring Cloud Vault stellt neue Zugangsdaten aus, wenn eine Lease rotiert oder der AppRole-Token
 * nach `token_max_ttl` verworfen wird (dabei widerruft Vault die Leases des alten Tokens und
 * loescht die DB-Rolle). In den Pool gelangen sie nur ueber diesen Handler. Ohne ihn baut HikariCP
 * weiter Verbindungen mit der geloeschten Rolle auf, und jede scheitert mit
 * "password authentication failed".
 *
 * Laeuft eine Lease im Modus RENEW ab, holt der [SecretLeaseContainer] von sich aus nichts nach.
 * Dann startet der Handler den Container in festen Abstaenden neu, bis wieder Zugangsdaten
 * ankommen. Ein Neustart allein gilt nicht als Erfolg.
 */
class VaultDatabaseLeaseHandler(
    databaseBackend: String,
    databaseRole: String,
    private val credentialRefresher: VaultDatabaseCredentialRefresher,
    private val vaultOperations: VaultOperations,
    private val secretLeaseContainer: SecretLeaseContainer,
    private val recoveryInterval: Duration = Duration.ofSeconds(30),
) : DisposableBean {

    private val logger = LoggerFactory.getLogger(VaultDatabaseLeaseHandler::class.java)
    private val secretPath = "$databaseBackend/creds/$databaseRole"

    private val isRecovering = AtomicBoolean(false)
    private val currentLeaseId = AtomicReference<String?>(null)
    private val recoveryTask = AtomicReference<ScheduledFuture<*>?>(null)
    private val recoveryAttempts = AtomicInteger(0)

    private val recoverySchedulerDelegate = lazy<ScheduledExecutorService> {
        Executors.newSingleThreadScheduledExecutor { runnable ->
            Thread(runnable, "vault-db-recovery").apply { isDaemon = true }
        }
    }
    private val recoveryScheduler by recoverySchedulerDelegate

    init {
        logger.info("Vault database lease handler listening on {}", secretPath)
        secretLeaseContainer.addErrorListener { event, exception ->
            if (event is SecretLeaseErrorEvent && isOwnSecret(event)) onLeaseError(exception)
        }
        secretLeaseContainer.addLeaseListener { event ->
            if (!isOwnSecret(event)) return@addLeaseListener
            when (event) {
                is SecretLeaseRotatedEvent -> onLeaseRotated(event)
                is SecretLeaseCreatedEvent -> onLeaseCreated(event)
                is SecretLeaseExpiredEvent -> onLeaseExpired(event)
            }
        }
    }

    // Exakter Vergleich: Ein Praefix-Vergleich traefe auch fremde Secrets wie `database/creds/<rolle>-mongo`.
    private fun isOwnSecret(event: SecretLeaseEvent): Boolean = event.source.path == secretPath

    private fun onLeaseCreated(event: SecretLeaseCreatedEvent) {
        logger.info("Database lease created: {}", event.lease?.leaseId)
        applyCredentials(event.secrets, event.lease?.leaseId, "new lease created")
    }

    private fun onLeaseRotated(event: SecretLeaseRotatedEvent) {
        if (event.source.mode != RequestedSecret.Mode.RENEW) return
        logger.info("Database lease rotated: {}", event.lease?.leaseId)
        applyCredentials(event.secrets, event.lease?.leaseId, "lease rotated")
    }

    private fun onLeaseExpired(event: SecretLeaseExpiredEvent) {
        logger.warn("Database lease expired: {} - requesting new credentials", event.lease?.leaseId)
        currentLeaseId.set(null)
        startRecovery()
    }

    private fun onLeaseError(exception: Exception) {
        logger.error("Database lease error: {}", exception.message)
        startRecovery()
    }

    private fun applyCredentials(secrets: Map<String, Any>, leaseId: String?, reason: String) {
        val username = secrets["username"] as? String
        val password = secrets["password"] as? String
        if (username == null || password == null) {
            logger.warn("Lease {} carries no username/password - ignoring", leaseId)
            return
        }
        currentLeaseId.set(leaseId)
        credentialRefresher.refreshCredentials(CredentialDTO(username, password))
        if (isRecovering.get()) stopRecovery(reason)
    }

    private fun startRecovery() {
        if (!isRecovering.compareAndSet(false, true)) {
            logger.debug("Recovery already in progress")
            return
        }
        logger.warn("Starting Vault database credential recovery, current lease: {}", currentLeaseId.get() ?: "none")
        recoveryAttempts.set(0)
        val task = recoveryScheduler.scheduleWithFixedDelay(
            { runCatching { attemptRecovery() }.onFailure { logger.warn("Recovery attempt failed: {}", it.message) } },
            0,
            recoveryInterval.toMillis(),
            TimeUnit.MILLISECONDS,
        )
        recoveryTask.set(task)
    }

    private fun attemptRecovery() {
        if (!isVaultAvailable()) {
            logger.debug("Vault not available yet - retrying")
            return
        }
        val attempt = recoveryAttempts.incrementAndGet()
        logger.info("Requesting new database credentials (attempt {})", attempt)
        secretLeaseContainer.stop()
        secretLeaseContainer.start()
    }

    private fun isVaultAvailable(): Boolean = runCatching {
        vaultOperations.opsForSys().health().let { it.isInitialized && !it.isSealed }
    }.getOrElse {
        logger.debug("Vault not available: {}", it.message)
        false
    }

    private fun stopRecovery(reason: String) {
        logger.info("Recovery completed after {} attempt(s): {}", recoveryAttempts.get(), reason)
        isRecovering.set(false)
        recoveryTask.getAndSet(null)?.cancel(false)
    }

    override fun destroy() {
        recoveryTask.getAndSet(null)?.cancel(false)
        if (recoverySchedulerDelegate.isInitialized()) {
            recoveryScheduler.shutdown()
            if (!recoveryScheduler.awaitTermination(5, TimeUnit.SECONDS)) recoveryScheduler.shutdownNow()
        }
    }
}
