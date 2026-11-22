package de.mosimtech.common.vault.database

import de.mosimtech.common.core.dto.vault.CredentialDTO
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockingDetails
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.vault.core.VaultOperations
import org.springframework.vault.core.VaultSysOperations
import org.springframework.vault.core.lease.SecretLeaseContainer
import org.springframework.vault.core.lease.domain.Lease
import org.springframework.vault.core.lease.domain.RequestedSecret
import org.springframework.vault.core.lease.event.LeaseListener
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent
import org.springframework.vault.core.lease.event.SecretLeaseRotatedEvent
import org.springframework.vault.support.VaultHealth
import java.time.Duration
import kotlin.test.assertEquals

class VaultDatabaseLeaseHandlerTest {

    private val secretPath = "database/creds/momasoft-test-service"

    private val secretLeaseContainer = mock<SecretLeaseContainer>()
    private val credentialRefresher = mock<VaultDatabaseCredentialRefresher>()
    private lateinit var handler: VaultDatabaseLeaseHandler

    private fun createListener(): LeaseListener {
        val vaultOperations = mock<VaultOperations>()
        val sysOperations = mock<VaultSysOperations>()
        val health = mock<VaultHealth>()
        whenever(vaultOperations.opsForSys()).thenReturn(sysOperations)
        whenever(sysOperations.health()).thenReturn(health)
        whenever(health.isInitialized).thenReturn(true)
        whenever(health.isSealed).thenReturn(false)

        handler = VaultDatabaseLeaseHandler(
            databaseBackend = "database",
            databaseRole = "momasoft-test-service",
            credentialRefresher = credentialRefresher,
            vaultOperations = vaultOperations,
            secretLeaseContainer = secretLeaseContainer,
            recoveryInterval = Duration.ofMillis(100),
        )
        val listener = argumentCaptor<LeaseListener>()
        verify(secretLeaseContainer).addLeaseListener(listener.capture())
        return listener.firstValue
    }

    @AfterEach
    fun tearDown() {
        if (::handler.isInitialized) handler.destroy()
    }

    private fun lease(id: String, path: String = secretPath) = Lease.of("$path/$id", Duration.ofHours(1), true)

    private fun secrets(username: String) = mapOf<String, Any>("username" to username, "password" to "s3cret")

    @Test
    fun `new lease credentials reach the connection pool`() {
        val listener = createListener()

        listener.onLeaseEvent(SecretLeaseCreatedEvent(RequestedSecret.renewable(secretPath), lease("a"), secrets("v-new")))

        verify(credentialRefresher).refreshCredentials(CredentialDTO("v-new", "s3cret"))
    }

    /** So kommen nach einem Token-Drop die neuen Zugangsdaten an (`SecretLeaseContainer.restartSecrets`). */
    @Test
    fun `rotated renewable lease credentials reach the connection pool`() {
        val listener = createListener()

        listener.onLeaseEvent(
            SecretLeaseRotatedEvent(RequestedSecret.renewable(secretPath), lease("old"), lease("new"), secrets("v-rotated")),
        )

        verify(credentialRefresher).refreshCredentials(CredentialDTO("v-rotated", "s3cret"))
    }

    @Test
    fun `leases of other secrets with the same prefix are ignored`() {
        val listener = createListener()
        val otherPath = "$secretPath-mongo"

        listener.onLeaseEvent(
            SecretLeaseCreatedEvent(RequestedSecret.renewable(otherPath), lease("a", otherPath), secrets("v-mongo")),
        )

        verify(credentialRefresher, never()).refreshCredentials(any())
    }

    @Test
    fun `expired lease restarts the lease container until new credentials arrive`() {
        val listener = createListener()

        listener.onLeaseEvent(SecretLeaseExpiredEvent(RequestedSecret.renewable(secretPath), lease("a")))

        verify(secretLeaseContainer, timeout(5000).atLeast(2)).start()
        verify(credentialRefresher, never()).refreshCredentials(any())
    }

    @Test
    fun `recovery stops once new credentials reached the pool`() {
        val listener = createListener()
        listener.onLeaseEvent(SecretLeaseExpiredEvent(RequestedSecret.renewable(secretPath), lease("a")))
        verify(secretLeaseContainer, timeout(5000)).start()

        listener.onLeaseEvent(SecretLeaseCreatedEvent(RequestedSecret.renewable(secretPath), lease("b"), secrets("v-new")))

        verify(credentialRefresher).refreshCredentials(CredentialDTO("v-new", "s3cret"))
        val startsAfterSuccess = countStarts()
        Thread.sleep(400)
        assertEquals(startsAfterSuccess, countStarts(), "Recovery lief nach erfolgreicher Erneuerung weiter")
    }

    private fun countStarts() = mockingDetails(secretLeaseContainer).invocations.count { it.method.name == "start" }
}
