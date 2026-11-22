package de.mosimtech.common.vault.database

import com.zaxxer.hikari.HikariConfigMXBean
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.HikariPoolMXBean
import de.mosimtech.common.core.dto.vault.CredentialDTO
import de.mosimtech.common.core.dto.vault.VaultCredentialsRefreshedEvent
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher

class VaultDatabaseCredentialRefresherTest {

    private val config = mock<HikariConfigMXBean>()
    private val pool = mock<HikariPoolMXBean>()
    private val dataSource = mock<HikariDataSource>().also {
        whenever(it.hikariConfigMXBean).thenReturn(config)
        whenever(it.hikariPoolMXBean).thenReturn(pool)
    }
    private val eventPublisher = mock<ApplicationEventPublisher>()

    @Test
    fun `credentials are set on the pool without borrowing a connection`() {
        VaultDatabaseCredentialRefresher(eventPublisher, dataSource).refreshCredentials(CredentialDTO("v-new", "s3cret"))

        verify(config).setUsername("v-new")
        verify(config).setPassword("s3cret")
        verify(pool).softEvictConnections()
        verify(dataSource, never()).connection
        verify(eventPublisher).publishEvent(any<VaultCredentialsRefreshedEvent>())
    }
}
